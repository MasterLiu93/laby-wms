package com.laby.module.wms.service.inbound;

import cn.hutool.core.collection.CollUtil;
import com.laby.framework.common.pojo.PageResult;
import com.laby.framework.common.util.collection.CollectionUtils;
import com.laby.framework.security.core.LoginUser;
import com.laby.framework.security.core.util.SecurityFrameworkUtils;
import com.laby.module.wms.controller.admin.inbound.vo.InboundPageReqVO;
import com.laby.module.wms.controller.admin.inbound.vo.InboundRespVO;
import com.laby.module.wms.controller.admin.inbound.vo.InboundSaveReqVO;
import com.laby.module.wms.convert.inbound.InboundConvert;
import com.laby.module.wms.dal.dataobject.goods.GoodsDO;
import com.laby.module.wms.dal.dataobject.inbound.InboundDO;
import com.laby.module.wms.dal.dataobject.inbound.InboundItemDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseAreaDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseLocationDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryDO;
import com.laby.module.wms.dal.mysql.inbound.InboundItemMapper;
import com.laby.module.wms.dal.mysql.inbound.InboundMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryLogMapper;
import com.laby.module.wms.dal.dataobject.inventory.InventoryLogDO;
import com.laby.module.wms.enums.InboundStatusEnum;
import com.laby.module.wms.enums.InboundTypeEnum;
import com.laby.module.wms.enums.InventoryStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laby.module.wms.service.goods.GoodsService;
import com.laby.module.wms.service.inventory.InventoryService;
import com.laby.module.wms.service.supplier.SupplierService;
import com.laby.module.wms.service.warehouse.WarehouseAreaService;
import com.laby.module.wms.service.warehouse.WarehouseLocationService;
import com.laby.module.wms.service.warehouse.WarehouseService;
import com.laby.framework.codegen.core.enums.CodePrefixEnum;
import com.laby.framework.codegen.core.service.CodeGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.laby.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.laby.module.wms.enums.ErrorCodeConstants.*;

/**
 * 入库单 Service 实现类
 *
 * 实现说明：
 * - 所有公共方法都经过参数校验（@Validated）
 * - 创建和修改入库单需要校验状态
 * - 状态流转严格按照：待审核 → 已审核 → 收货中 → 已完成
 * - 完成收货时会更新库存并记录流水
 * - 使用 MapStruct 进行对象转换
 * - 异常统一使用 ServiceException
 *
 * @author laby
 */
@Service
@Validated
@Slf4j
public class InboundServiceImpl implements InboundService {

    @Resource
    private InboundMapper inboundMapper;
    @Resource
    private InboundItemMapper inboundItemMapper;
    @Resource
    private InventoryMapper inventoryMapper;
    @Resource
    private InventoryLogMapper inventoryLogMapper;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WarehouseAreaService warehouseAreaService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private SupplierService supplierService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private CodeGeneratorService codeGeneratorService;

    /**
     * 创建入库单
     *
     * 实现步骤：
     * 1. 校验仓库是否存在
     * 2. 校验明细数量（至少一条）
     * 3. 校验商品是否存在
     * 4. 采购入库时校验供应商是否填写
     * 5. 生成入库单号
     * 6. 计算总数量和总金额
     * 7. 插入入库单主表
     * 8. 插入入库单明细
     * 9. 返回入库单ID
     *
     * @param createReqVO 创建信息
     * @return 入库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInbound(InboundSaveReqVO createReqVO) {
        // 1. 校验仓库是否存在
        warehouseService.validateWarehouseExists(createReqVO.getWarehouseId());

        // 2. 校验明细数量
        if (CollUtil.isEmpty(createReqVO.getItems())) {
            throw exception(INBOUND_QUANTITY_INVALID);
        }

        // 3. 校验商品是否存在
        Set<Long> goodsIds = CollectionUtils.convertSet(createReqVO.getItems(), item -> item.getGoodsId());
        Map<Long, GoodsDO> goodsMap = goodsService.getGoodsMap(new ArrayList<>(goodsIds));
        createReqVO.getItems().forEach(item -> {
            if (!goodsMap.containsKey(item.getGoodsId())) {
                throw exception(GOODS_NOT_EXISTS);
            }
        });

        // 4. 采购入库时校验供应商
        if (InboundTypeEnum.PURCHASE.getType().equals(createReqVO.getInboundType())) {
            if (createReqVO.getSupplierId() == null) {
                throw exception(INBOUND_QUANTITY_INVALID); // TODO: 添加专门的供应商错误码
            }
        }

        // 5. 生成入库单号（使用编码生成器）
        String inboundNo = codeGeneratorService.generateCode(CodePrefixEnum.INBOUND);

        // 6. 计算总数量和总金额
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var item : createReqVO.getItems()) {
            totalQuantity = totalQuantity.add(item.getPlanQuantity());
            if (item.getPrice() != null) {
                BigDecimal amount = item.getPlanQuantity().multiply(item.getPrice());
                totalAmount = totalAmount.add(amount);
            }
        }

        // 7. 插入入库单主表
        InboundDO inbound = InboundConvert.INSTANCE.convert(createReqVO);
        inbound.setInboundNo(inboundNo);
        inbound.setStatus(InboundStatusEnum.PENDING.getStatus()); // 初始状态：待审核
        inbound.setTotalQuantity(totalQuantity);
        inbound.setReceivedQuantity(BigDecimal.ZERO);
        inbound.setTotalAmount(totalAmount);
        inboundMapper.insert(inbound);

        // 8. 插入入库单明细
        List<InboundItemDO> items = InboundConvert.INSTANCE.convertItemList(createReqVO.getItems());
        items.forEach(item -> {
            item.setInboundId(inbound.getId());
            item.setReceivedQuantity(BigDecimal.ZERO);
            item.setQualifiedQuantity(BigDecimal.ZERO);
            item.setUnqualifiedQuantity(BigDecimal.ZERO);
            // 计算金额
            if (item.getPrice() != null) {
                item.setAmount(item.getPlanQuantity().multiply(item.getPrice()));
            }
            inboundItemMapper.insert(item);
        });

        log.info("[创建入库单] 入库单号={}, ID={}, 明细数={}", inboundNo, inbound.getId(), items.size());
        return inbound.getId();
    }

    /**
     * 更新入库单
     *
     * 实现步骤：
     * 1. 校验入库单是否存在
     * 2. 校验入库单状态（只有待审核状态可以修改）
     * 3. 校验明细数量
     * 4. 删除旧明细
     * 5. 更新入库单主表
     * 6. 插入新明细
     *
     * @param updateReqVO 更新信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInbound(InboundSaveReqVO updateReqVO) {
        // 1. 校验入库单是否存在
        InboundDO inbound = validateInboundExists(updateReqVO.getId());

        // 2. 校验状态
        if (!InboundStatusEnum.PENDING.getStatus().equals(inbound.getStatus())) {
            throw exception(INBOUND_STATUS_ERROR);
        }

        // 3. 校验明细数量
        if (CollUtil.isEmpty(updateReqVO.getItems())) {
            throw exception(INBOUND_QUANTITY_INVALID);
        }

        // 4. 删除旧明细
        inboundItemMapper.deleteByInboundId(updateReqVO.getId());

        // 5. 计算总数量和总金额
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var item : updateReqVO.getItems()) {
            totalQuantity = totalQuantity.add(item.getPlanQuantity());
            if (item.getPrice() != null) {
                BigDecimal amount = item.getPlanQuantity().multiply(item.getPrice());
                totalAmount = totalAmount.add(amount);
            }
        }

        // 6. 更新入库单主表
        InboundDO updateObj = InboundConvert.INSTANCE.convert(updateReqVO);
        updateObj.setTotalQuantity(totalQuantity);
        updateObj.setTotalAmount(totalAmount);
        inboundMapper.updateById(updateObj);

        // 7. 插入新明细
        List<InboundItemDO> items = InboundConvert.INSTANCE.convertItemList(updateReqVO.getItems());
        items.forEach(item -> {
            item.setInboundId(updateReqVO.getId());
            item.setReceivedQuantity(BigDecimal.ZERO);
            item.setQualifiedQuantity(BigDecimal.ZERO);
            item.setUnqualifiedQuantity(BigDecimal.ZERO);
            if (item.getPrice() != null) {
                item.setAmount(item.getPlanQuantity().multiply(item.getPrice()));
            }
            inboundItemMapper.insert(item);
        });

        log.info("[更新入库单] 入库单号={}, ID={}, 明细数={}", inbound.getInboundNo(), inbound.getId(), items.size());
    }

    /**
     * 删除入库单
     *
     * 实现步骤：
     * 1. 校验入库单是否存在
     * 2. 校验状态（只有待审核状态可以删除）
     * 3. 删除明细
     * 4. 删除主表
     *
     * @param id 入库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInbound(Long id) {
        // 1. 校验入库单是否存在
        InboundDO inbound = validateInboundExists(id);

        // 2. 校验状态
        if (!InboundStatusEnum.PENDING.getStatus().equals(inbound.getStatus())) {
            throw exception(INBOUND_STATUS_ERROR);
        }

        // 3. 删除明细
        inboundItemMapper.deleteByInboundId(id);

        // 4. 删除主表（逻辑删除）
        inboundMapper.deleteById(id);

        log.info("[删除入库单] 入库单号={}, ID={}", inbound.getInboundNo(), id);
    }

    /**
     * 获得入库单详情
     *
     * 实现步骤：
     * 1. 查询入库单主表
     * 2. 查询入库单明细
     * 3. 查询关联数据（仓库、商品、库位）
     * 4. 转换VO并填充关联字段
     * 5. 返回详情
     *
     * @param id 入库单ID
     * @return 入库单详情（包含明细和关联字段）
     */
    @Override
    public InboundRespVO getInbound(Long id) {
        // 1. 查询入库单主表
        InboundDO inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            return null;
        }

        // 2. 查询入库单明细
        List<InboundItemDO> items = inboundItemMapper.selectListByInboundId(id);

        // 3. 查询关联数据
        // 3.1 仓库
        Map<Long, WarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                Collections.singletonList(inbound.getWarehouseId())
        );

        // 3.2 供应商（如果有）
        Map<Long, String> supplierMap = new HashMap<>();
        if (inbound.getSupplierId() != null) {
            supplierMap = supplierService.getSupplierMap(
                    Collections.singletonList(inbound.getSupplierId())
            );
        }

        // 3.3 商品
        Set<Long> goodsIds = CollectionUtils.convertSet(items, InboundItemDO::getGoodsId);
        Map<Long, GoodsDO> goodsMap = goodsService.getGoodsMap(new ArrayList<>(goodsIds));

        // 3.4 库位
        Set<Long> locationIds = CollectionUtils.convertSet(items, InboundItemDO::getLocationId);
        Map<Long, WarehouseLocationDO> locationMap = warehouseLocationService.getWarehouseLocationMap(new ArrayList<>(locationIds));

        // 3.5 库区（从库位中提取）
        Set<Long> areaIds = locationMap.values().stream()
                .map(WarehouseLocationDO::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, WarehouseAreaDO> areaMapDO = warehouseAreaService.getWarehouseAreaMap(areaIds);
        // 转换为 Map<Long, String>（库区ID -> 库区名称）
        Map<Long, String> areaMap = areaMapDO.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAreaName()));

        // 4. 转换VO并填充关联字段
        InboundRespVO respVO = InboundConvert.INSTANCE.convert(inbound);
        // 填充仓库名称
        if (warehouseMap.containsKey(inbound.getWarehouseId())) {
            respVO.setWarehouseName(warehouseMap.get(inbound.getWarehouseId()).getWarehouseName());
        }
        // 填充供应商名称
        if (inbound.getSupplierId() != null && supplierMap.containsKey(inbound.getSupplierId())) {
            respVO.setSupplierName(supplierMap.get(inbound.getSupplierId()));
        }
        respVO.setItems(InboundConvert.INSTANCE.convertItemList(items, goodsMap, locationMap, areaMap));

        return respVO;
    }

    /**
     * 获得入库单分页列表
     *
     * 实现步骤：
     * 1. 查询分页数据（Mapper 返回 DO）
     * 2. 提取仓库ID和供应商ID
     * 3. 批量查询仓库名称和供应商名称
     * 4. 转换 VO 并填充关联字段
     *
     * @param pageReqVO 分页查询条件
     * @return 分页列表（包含关联字段）
     */
    @Override
    public PageResult<InboundRespVO> getInboundPage(InboundPageReqVO pageReqVO) {
        // 1. 查询分页数据
        PageResult<InboundDO> pageResult = inboundMapper.selectPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }

        // 2. 提取仓库ID和供应商ID
        Set<Long> warehouseIds = CollectionUtils.convertSet(pageResult.getList(), InboundDO::getWarehouseId);
        Set<Long> supplierIds = CollectionUtils.convertSet(pageResult.getList(), InboundDO::getSupplierId);

        // 3. 批量查询仓库名称和供应商名称
        Map<Long, WarehouseDO> warehouseMap = warehouseService.getWarehouseMap(warehouseIds);
        Map<Long, String> supplierMap = supplierService.getSupplierMap(supplierIds);

        // 4. 转换 VO 并填充关联字段
        return InboundConvert.INSTANCE.convertPage(pageResult, warehouseMap, supplierMap);
    }

    /**
     * 审核入库单
     *
     * 实现步骤：
     * 1. 校验入库单是否存在
     * 2. 校验状态（只有待审核状态可以审核）
     * 3. 更新状态为"已审核"
     * 4. 记录审核人和审核时间
     *
     * @param id 入库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditInbound(Long id) {
        // 1. 校验入库单是否存在
        InboundDO inbound = validateInboundExists(id);

        // 2. 校验状态
        if (!InboundStatusEnum.PENDING.getStatus().equals(inbound.getStatus())) {
            throw exception(INBOUND_STATUS_ERROR);
        }

        // 3. 更新状态
        InboundDO updateObj = new InboundDO();
        updateObj.setId(id);
        updateObj.setStatus(InboundStatusEnum.APPROVED.getStatus());
        updateObj.setAuditBy(SecurityFrameworkUtils.getLoginUserId());
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String username = loginUser != null && loginUser.getInfo() != null 
            ? loginUser.getInfo().get(LoginUser.INFO_KEY_NICKNAME) 
            : "系统管理员";
        updateObj.setAuditByName(username != null ? username : "系统管理员");
        updateObj.setAuditTime(LocalDateTime.now());
        inboundMapper.updateById(updateObj);

        log.info("[审核入库单] 入库单号={}, ID={}, 审核人={}", inbound.getInboundNo(), id, updateObj.getAuditByName());
    }

    /**
     * 开始收货
     *
     * 实现步骤：
     * 1. 校验入库单是否存在
     * 2. 校验状态（只有已审核状态可以开始收货）
     * 3. 更新状态为"收货中"
     * 4. 记录实际到货时间
     *
     * @param id 入库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startReceiving(Long id) {
        // 1. 校验入库单是否存在
        InboundDO inbound = validateInboundExists(id);

        // 2. 校验状态
        if (!InboundStatusEnum.APPROVED.getStatus().equals(inbound.getStatus())) {
            throw exception(INBOUND_STATUS_ERROR);
        }

        // 3. 更新状态
        InboundDO updateObj = new InboundDO();
        updateObj.setId(id);
        updateObj.setStatus(InboundStatusEnum.RECEIVING.getStatus());
        updateObj.setActualArrivalTime(LocalDateTime.now());
        inboundMapper.updateById(updateObj);

        log.info("[开始收货] 入库单号={}, ID={}", inbound.getInboundNo(), id);
    }

    /**
     * 确认收货（单个明细）
     *
     * 实现步骤：
     * 1. 校验入库单是否存在且状态为"收货中"
     * 2. 校验明细是否存在且属于该入库单
     * 3. 更新明细的实收数量、合格数量、不合格数量
     * 4. 计算金额（合格数量 * 单价）
     *
     * @param id 入库单ID
     * @param itemId 明细ID
     * @param receivedQuantity 实收数量
     * @param qualifiedQuantity 合格数量
     * @param unqualifiedQuantity 不合格数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveItem(Long id, Long itemId, Integer receivedQuantity, Integer qualifiedQuantity, Integer unqualifiedQuantity) {
        // 1. 校验入库单是否存在
        InboundDO inbound = validateInboundExists(id);

        // 2. 校验状态
        if (!InboundStatusEnum.RECEIVING.getStatus().equals(inbound.getStatus())) {
            throw exception(INBOUND_STATUS_ERROR);
        }

        // 3. 校验明细是否存在
        InboundItemDO item = inboundItemMapper.selectById(itemId);
        if (item == null || !item.getInboundId().equals(id)) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }

        // 4. 更新明细收货数据
        InboundItemDO updateObj = new InboundItemDO();
        updateObj.setId(itemId);
        updateObj.setReceivedQuantity(new BigDecimal(receivedQuantity));
        updateObj.setQualifiedQuantity(new BigDecimal(qualifiedQuantity));
        updateObj.setUnqualifiedQuantity(new BigDecimal(unqualifiedQuantity));
        
        // 计算金额（合格数量 * 单价）
        if (item.getPrice() != null) {
            updateObj.setAmount(new BigDecimal(qualifiedQuantity).multiply(item.getPrice()));
        }
        
        inboundItemMapper.updateById(updateObj);

        log.info("[确认收货] 入库单号={}, 明细ID={}, 实收={}, 合格={}, 不合格={}", 
            inbound.getInboundNo(), itemId, receivedQuantity, qualifiedQuantity, unqualifiedQuantity);
        
        // 5. 更新入库单主表的已收货数量（汇总所有明细）
        updateInboundReceivedQuantity(id);
    }
    
    /**
     * 更新入库单主表的已收货数量
     * 
     * 业务说明：
     * - 每次确认收货明细后，重新汇总所有明细的已收货数量
     * - 更新到入库单主表的 receivedQuantity 字段
     * - 用于列表页显示收货进度
     * 
     * @param inboundId 入库单ID
     */
    private void updateInboundReceivedQuantity(Long inboundId) {
        // 查询该入库单的所有明细
        List<InboundItemDO> items = inboundItemMapper.selectListByInboundId(inboundId);
        
        // 汇总已收货数量
        BigDecimal totalReceived = items.stream()
                .map(InboundItemDO::getReceivedQuantity)
                .filter(qty -> qty != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 更新入库单主表
        InboundDO updateInbound = new InboundDO();
        updateInbound.setId(inboundId);
        updateInbound.setReceivedQuantity(totalReceived);
        inboundMapper.updateById(updateInbound);
        
        log.info("[更新已收货数量] 入库单ID={}, 已收货总数={}", inboundId, totalReceived);
    }

    /**
     * 完成收货
     *
     * 实现步骤：
     * 1. 校验入库单是否存在
     * 2. 校验状态（只有收货中状态可以完成）
     * 3. 查询入库明细
     * 4. 更新库存（调用InventoryService）
     * 5. 更新状态为"已完成"
     * 6. 记录完成人和完成时间
     *
     * @param id 入库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeInbound(Long id) {
        // 1. 校验入库单是否存在
        InboundDO inbound = validateInboundExists(id);

        // 2. 校验状态
        if (!InboundStatusEnum.RECEIVING.getStatus().equals(inbound.getStatus())) {
            throw exception(INBOUND_STATUS_ERROR);
        }

        // 3. 查询入库明细
        List<InboundItemDO> items = inboundItemMapper.selectListByInboundId(id);
        
        // 3.1 校验明细是否为空
        if (CollUtil.isEmpty(items)) {
            log.error("[完成收货失败] 入库单号={}, 原因：没有入库明细", inbound.getInboundNo());
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
        
        // 3.2 校验是否所有明细都已确认收货
        long unreceivedCount = items.stream()
                .filter(item -> item.getReceivedQuantity() == null || item.getReceivedQuantity().compareTo(BigDecimal.ZERO) <= 0)
                .count();
        if (unreceivedCount > 0) {
            log.error("[完成收货失败] 入库单号={}, 原因：还有{}个明细未确认收货", inbound.getInboundNo(), unreceivedCount);
            throw exception(INBOUND_NOT_ALL_RECEIVED);
        }
        
        // 3.3 统计合格数量
        long qualifiedCount = items.stream()
                .filter(item -> item.getQualifiedQuantity() != null && item.getQualifiedQuantity().compareTo(BigDecimal.ZERO) > 0)
                .count();
        if (qualifiedCount == 0) {
            log.error("[完成收货失败] 入库单号={}, 原因：所有明细的合格数量都为0", inbound.getInboundNo());
            throw exception(INBOUND_NO_QUALIFIED);
        }
        
        log.info("[完成收货] 入库单号={}, 明细总数={}, 有合格数量的明细数={}", 
            inbound.getInboundNo(), items.size(), qualifiedCount);

        // 4. 更新库存
        updateInventory(inbound, items);

        // 5. 更新状态
        InboundDO updateObj = new InboundDO();
        updateObj.setId(id);
        updateObj.setStatus(InboundStatusEnum.COMPLETED.getStatus());
        updateObj.setCompleteBy(SecurityFrameworkUtils.getLoginUserId());
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String username = loginUser != null && loginUser.getInfo() != null 
            ? loginUser.getInfo().get(LoginUser.INFO_KEY_NICKNAME) 
            : "系统管理员";
        updateObj.setCompleteByName(username != null ? username : "系统管理员");
        updateObj.setCompleteTime(LocalDateTime.now());
        inboundMapper.updateById(updateObj);

        log.info("[完成收货] 入库单号={}, ID={}, 完成人={}, 状态已更新为已完成", 
            inbound.getInboundNo(), id, updateObj.getCompleteByName());
    }

    /**
     * 取消入库单
     *
     * 实现步骤：
     * 1. 校验入库单是否存在
     * 2. 校验状态（已完成状态不能取消）
     * 3. 更新状态为"已取消"
     *
     * @param id 入库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelInbound(Long id) {
        // 1. 校验入库单是否存在
        InboundDO inbound = validateInboundExists(id);

        // 2. 校验状态
        if (InboundStatusEnum.COMPLETED.getStatus().equals(inbound.getStatus())) {
            throw exception(INBOUND_STATUS_ERROR);
        }

        // 3. 更新状态
        InboundDO updateObj = new InboundDO();
        updateObj.setId(id);
        updateObj.setStatus(InboundStatusEnum.CANCELLED.getStatus());
        inboundMapper.updateById(updateObj);

        log.info("[取消入库单] 入库单号={}, ID={}", inbound.getInboundNo(), id);
    }

    /**
     * 批量获取入库单Map
     *
     * @param ids 入库单ID列表
     * @return 入库单Map
     */
    @Override
    public Map<Long, InboundDO> getInboundMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<InboundDO> list = inboundMapper.selectBatchIds(ids);
        return CollectionUtils.convertMap(list, InboundDO::getId);
    }

    // ========== 私有方法 ==========

    /**
     * 更新库存
     *
     * 业务规则：
     * 1. 遍历入库明细
     * 2. 根据 仓库+库位+商品+批次+序列号 查找库存记录
     * 3. 如果找到，增加库存数量（累加合格数量）
     * 4. 如果没找到，创建新库存记录
     * 5. 使用合格数量（qualifiedQuantity）更新库存
     * 6. 记录供应商、生产日期、过期日期等信息
     *
     * @param inbound 入库单
     * @param items 入库明细列表
     */
    private void updateInventory(InboundDO inbound, List<InboundItemDO> items) {
        log.info("[更新库存] 入库单号={}, 明细数={}", inbound.getInboundNo(), items.size());
        
        for (InboundItemDO item : items) {
            // 只有合格数量才入库
            if (item.getQualifiedQuantity() == null || item.getQualifiedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[更新库存] 跳过明细，合格数量为0或空: 商品ID={}, 库位ID={}", item.getGoodsId(), item.getLocationId());
                continue;
            }

            // 查找已存在的库存记录（根据 仓库+库位+商品+批次+序列号）
            LambdaQueryWrapper<InventoryDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(InventoryDO::getWarehouseId, inbound.getWarehouseId())
                    .eq(InventoryDO::getGoodsId, item.getGoodsId());
            
            // 库位可能为空
            if (item.getLocationId() != null) {
                queryWrapper.eq(InventoryDO::getLocationId, item.getLocationId());
            } else {
                queryWrapper.isNull(InventoryDO::getLocationId);
            }
            
            // 批次号可能为空
            if (item.getBatchNo() != null && !item.getBatchNo().isEmpty()) {
                queryWrapper.eq(InventoryDO::getBatchNo, item.getBatchNo());
            } else {
                queryWrapper.isNull(InventoryDO::getBatchNo);
            }
            
            // 序列号可能为空
            if (item.getSerialNo() != null && !item.getSerialNo().isEmpty()) {
                queryWrapper.eq(InventoryDO::getSerialNo, item.getSerialNo());
            } else {
                queryWrapper.isNull(InventoryDO::getSerialNo);
            }

            InventoryDO existingInventory = inventoryMapper.selectOne(queryWrapper);

            if (existingInventory != null) {
                // 库存记录已存在，累加数量
                BigDecimal quantityBefore = existingInventory.getQuantity();
                BigDecimal quantityAfter = quantityBefore.add(item.getQualifiedQuantity());
                
                InventoryDO updateObj = new InventoryDO();
                updateObj.setId(existingInventory.getId());
                updateObj.setQuantity(quantityAfter);
                // 更新版本号（乐观锁）
                updateObj.setVersion(existingInventory.getVersion());
                inventoryMapper.updateById(updateObj);
                
                log.info("[更新库存] 累加库存: 库存ID={}, 原数量={}, 入库数量={}, 新数量={}", 
                    existingInventory.getId(), quantityBefore, item.getQualifiedQuantity(), quantityAfter);
                
                // ✅ 记录库存流水（累加库存）
                recordInventoryLog(inbound, item, quantityBefore, item.getQualifiedQuantity(), quantityAfter);
            } else {
                // 库存记录不存在，创建新记录
                InventoryDO newInventory = new InventoryDO();
                newInventory.setWarehouseId(inbound.getWarehouseId());
                newInventory.setLocationId(item.getLocationId());
                newInventory.setGoodsId(item.getGoodsId());
                newInventory.setBatchNo(item.getBatchNo());
                newInventory.setSerialNo(item.getSerialNo());
                newInventory.setQuantity(item.getQualifiedQuantity());
                newInventory.setLockQuantity(BigDecimal.ZERO);
                newInventory.setProductionDate(item.getProductionDate());
                newInventory.setExpireDate(item.getExpireDate());
                newInventory.setInboundDate(LocalDateTime.now());
                newInventory.setSupplierId(inbound.getSupplierId());
                newInventory.setStatus(InventoryStatusEnum.NORMAL.getStatus());
                newInventory.setVersion(0);
                inventoryMapper.insert(newInventory);
                
                log.info("[更新库存] 创建新库存: 库存ID={}, 商品ID={}, 库位ID={}, 数量={}", 
                    newInventory.getId(), item.getGoodsId(), item.getLocationId(), item.getQualifiedQuantity());
                
                // ✅ 记录库存流水（新建库存）
                recordInventoryLog(inbound, item, BigDecimal.ZERO, item.getQualifiedQuantity(), item.getQualifiedQuantity());
            }
        }
        
        log.info("[更新库存] 完成，入库单号={}", inbound.getInboundNo());
    }

    /**
     * 记录库存流水
     * 
     * 业务说明：
     * - 每次库存变动都要记录流水，用于追溯和审计
     * - 记录操作类型、操作前后数量、业务单号等信息
     * - 流水表只增不删，永久保存
     * 
     * @param inbound 入库单
     * @param item 入库明细
     * @param quantityBefore 操作前数量
     * @param quantityChange 变化数量
     * @param quantityAfter 操作后数量
     */
    private void recordInventoryLog(InboundDO inbound, InboundItemDO item, 
                                    BigDecimal quantityBefore, BigDecimal quantityChange, BigDecimal quantityAfter) {
        InventoryLogDO log = new InventoryLogDO();
        log.setWarehouseId(inbound.getWarehouseId());
        log.setGoodsId(item.getGoodsId());
        log.setLocationId(item.getLocationId());
        log.setBatchNo(item.getBatchNo());
        log.setOperationType("INBOUND");  // 入库操作
        log.setQuantityBefore(quantityBefore);
        log.setQuantityChange(quantityChange);
        log.setQuantityAfter(quantityAfter);
        log.setBusinessType("PURCHASE_INBOUND");  // 业务类型：采购入库
        log.setBusinessNo(inbound.getInboundNo());  // 业务单号：入库单号
        
        // 获取操作人信息
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String operator = loginUser != null && loginUser.getInfo() != null 
            ? loginUser.getInfo().get(LoginUser.INFO_KEY_NICKNAME) 
            : "系统";
        log.setOperator(operator);
        
        log.setRemark("入库单完成收货，更新库存");
        
        inventoryLogMapper.insert(log);
        
        InboundServiceImpl.log.info("[记录库存流水] 入库单号={}, 商品ID={}, 操作前={}, 变化量={}, 操作后={}", 
            inbound.getInboundNo(), item.getGoodsId(), quantityBefore, quantityChange, quantityAfter);
    }

    /**
     * 校验入库单是否存在
     *
     * @param id 入库单ID
     * @return 入库单DO
     * @throws com.laby.framework.common.exception.ServiceException 如果不存在
     */
    private InboundDO validateInboundExists(Long id) {
        InboundDO inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw exception(INBOUND_NOT_EXISTS);
        }
        return inbound;
    }

}


