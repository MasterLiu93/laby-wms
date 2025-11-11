package com.laby.module.wms.service.outbound;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.laby.framework.common.pojo.PageResult;
import com.laby.module.wms.controller.admin.outbound.vo.OutboundItemRespVO;
import com.laby.module.wms.controller.admin.outbound.vo.OutboundPageReqVO;
import com.laby.module.wms.controller.admin.outbound.vo.OutboundRespVO;
import com.laby.module.wms.controller.admin.outbound.vo.OutboundSaveReqVO;
import com.laby.module.wms.convert.outbound.OutboundConvert;
import com.laby.module.wms.dal.dataobject.goods.GoodsDO;
import com.laby.module.wms.dal.dataobject.outbound.OutboundDO;
import com.laby.module.wms.dal.dataobject.outbound.OutboundItemDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseLocationDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryLogDO;
import com.laby.module.wms.dal.mysql.outbound.OutboundItemMapper;
import com.laby.module.wms.dal.mysql.outbound.OutboundMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryLogMapper;
import com.laby.module.wms.enums.OutboundStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laby.framework.security.core.LoginUser;
import com.laby.framework.security.core.util.SecurityFrameworkUtils;
import com.laby.module.wms.service.goods.GoodsService;
import com.laby.module.wms.service.warehouse.WarehouseAreaService;
import com.laby.module.wms.service.warehouse.WarehouseLocationService;
import com.laby.module.wms.service.warehouse.WarehouseService;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseAreaDO;
import com.laby.framework.codegen.core.enums.CodePrefixEnum;
import com.laby.framework.codegen.core.service.CodeGeneratorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.laby.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.laby.module.wms.enums.ErrorCodeConstants.*;

/**
 * 出库单 Service 实现类
 *
 * 实现说明：
 * - 所有公共方法都经过参数校验（@Validated）
 * - 创建和修改出库单需要校验状态
 * - 状态流转严格按照：待审核 → 已审核 → 拣货中 → 待发货 → 已发货
 * - 发货时会扣减库存并记录流水
 * - 使用 MapStruct 进行对象转换
 * - 异常统一使用 ServiceException
 *
 * @author laby
 */
@Service
@Validated
@Slf4j
public class OutboundServiceImpl implements OutboundService {

    @Resource
    private OutboundMapper outboundMapper;

    @Resource
    private OutboundItemMapper outboundItemMapper;

    @Resource
    private WarehouseService warehouseService;
    
    @Resource
    private CodeGeneratorService codeGeneratorService;

    @Resource
    private WarehouseLocationService warehouseLocationService;
    
    @Resource
    private WarehouseAreaService warehouseAreaService;

    @Resource
    private GoodsService goodsService;
    
    @Resource
    private com.laby.module.wms.service.customer.CustomerService customerService;
    
    @Resource
    private InventoryMapper inventoryMapper;
    
    @Resource
    private InventoryLogMapper inventoryLogMapper;

    /**
     * 创建出库单
     *
     * 实现步骤：
     * 1. 数据校验
     * 2. 生成出库单号（使用编码生成器）
     * 3. 创建出库单主表，初始状态为"待审核"
     * 4. 创建出库单明细，初始化拣货和发货数量为0
     * 5. 返回出库单ID
     *
     * @param createReqVO 创建信息
     * @return 出库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutbound(OutboundSaveReqVO createReqVO) {
        // 1. 数据校验
        validateOutboundForCreate(createReqVO);

        // 2. 生成出库单号（使用编码生成器）
        String outboundNo = codeGeneratorService.generateCode(CodePrefixEnum.OUTBOUND);
        
        // 3. 创建出库单主表
        OutboundDO outbound = OutboundConvert.INSTANCE.convert(createReqVO);
        outbound.setOutboundNo(outboundNo);
        outbound.setStatus(OutboundStatusEnum.PENDING.getStatus());
        outbound.setTotalQuantity(calculateTotalQuantity(createReqVO.getItems()));
        outbound.setPickedQuantity(BigDecimal.ZERO);
        outboundMapper.insert(outbound);

        // 4. 创建出库单明细
        if (CollUtil.isNotEmpty(createReqVO.getItems())) {
            List<OutboundItemDO> items = OutboundConvert.INSTANCE.convertItemReqList(createReqVO.getItems());
            items.forEach(item -> {
                item.setOutboundId(outbound.getId());
                item.setPickedQuantity(BigDecimal.ZERO);
                item.setShippedQuantity(BigDecimal.ZERO);
                outboundItemMapper.insert(item);
            });
        }

        log.info("[createOutbound] 创建出库单成功，出库单号：{}, ID：{}", outbound.getOutboundNo(), outbound.getId());
        return outbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOutbound(OutboundSaveReqVO updateReqVO) {
        // 1. 校验出库单是否存在
        OutboundDO existOutbound = validateOutboundExists(updateReqVO.getId());

        // 2. 仅待审核状态可以修改
        if (!OutboundStatusEnum.PENDING.getStatus().equals(existOutbound.getStatus())) {
            throw exception(OUTBOUND_NOT_ALLOW_UPDATE);
        }

        // 3. 数据校验
        validateOutboundForUpdate(updateReqVO);

        // 4. 更新出库单主表
        OutboundDO updateObj = OutboundConvert.INSTANCE.convert(updateReqVO);
        updateObj.setTotalQuantity(calculateTotalQuantity(updateReqVO.getItems()));
        outboundMapper.updateById(updateObj);

        // 5. 更新出库单明细（先删除后新增）
        outboundItemMapper.deleteByOutboundId(updateReqVO.getId());
        if (CollUtil.isNotEmpty(updateReqVO.getItems())) {
            List<OutboundItemDO> items = OutboundConvert.INSTANCE.convertItemReqList(updateReqVO.getItems());
            items.forEach(item -> {
                item.setOutboundId(updateReqVO.getId());
                item.setPickedQuantity(BigDecimal.ZERO);
                item.setShippedQuantity(BigDecimal.ZERO);
                outboundItemMapper.insert(item);
            });
        }

        log.info("[updateOutbound] 更新出库单成功，出库单号：{}, ID：{}", existOutbound.getOutboundNo(), updateReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOutbound(Long id) {
        // 1. 校验出库单是否存在
        OutboundDO outbound = validateOutboundExists(id);

        // 2. 仅待审核状态可以删除
        if (!OutboundStatusEnum.PENDING.getStatus().equals(outbound.getStatus())) {
            throw exception(OUTBOUND_NOT_ALLOW_DELETE);
        }

        // 3. 删除出库单（软删除）
        outboundMapper.deleteById(id);

        // 4. 删除出库单明细
        outboundItemMapper.deleteByOutboundId(id);

        log.info("[deleteOutbound] 删除出库单成功，出库单号：{}, ID：{}", outbound.getOutboundNo(), id);
    }

    @Override
    public OutboundRespVO getOutbound(Long id) {
        // 1. 查询出库单主表
        OutboundDO outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            return null;
        }

        // 2. 转换为响应对象
        OutboundRespVO respVO = OutboundConvert.INSTANCE.convert(outbound);

        // 3. 填充仓库信息
        if (outbound.getWarehouseId() != null) {
            WarehouseDO warehouse = warehouseService.getWarehouse(outbound.getWarehouseId());
            if (warehouse != null) {
                respVO.setWarehouseName(warehouse.getWarehouseName());
            }
        }

        // 4. 填充客户信息
        if (outbound.getCustomerId() != null) {
            Map<Long, String> customerMap = customerService.getCustomerMap(Collections.singletonList(outbound.getCustomerId()));
            if (customerMap.containsKey(outbound.getCustomerId())) {
                respVO.setCustomerName(customerMap.get(outbound.getCustomerId()));
            }
        }

        // 5. 查询并填充明细信息
        List<OutboundItemDO> items = outboundItemMapper.selectListByOutboundId(id);
        if (CollUtil.isNotEmpty(items)) {
            // 批量查询商品信息
            List<Long> goodsIds = items.stream().map(OutboundItemDO::getGoodsId).distinct().collect(Collectors.toList());
            Map<Long, GoodsDO> goodsMap = goodsService.getGoodsMap(goodsIds);

            // 批量查询库位信息
            List<Long> locationIds = items.stream()
                    .map(OutboundItemDO::getLocationId)
                    .filter(locationId -> locationId != null)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, WarehouseLocationDO> locationMap = warehouseLocationService.getWarehouseLocationMap(locationIds);

            // 批量查询库区信息（从库位中提取）
            Set<Long> areaIds = locationMap.values().stream()
                    .map(WarehouseLocationDO::getAreaId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, WarehouseAreaDO> areaMapDO = warehouseAreaService.getWarehouseAreaMap(areaIds);
            // 转换为 Map<Long, String>（库区ID -> 库区名称）
            Map<Long, String> areaMap = areaMapDO.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAreaName()));

            // 转换并填充明细信息
            List<OutboundItemRespVO> itemRespVOList = OutboundConvert.INSTANCE.convertItemList(items);
            itemRespVOList.forEach(itemRespVO -> 
                OutboundConvert.INSTANCE.fillOutboundItemRespVO(itemRespVO, goodsMap, locationMap, areaMap)
            );
            respVO.setItems(itemRespVOList);
            
            // 计算总金额（所有明细的 price * planQuantity 之和）
            BigDecimal totalAmount = items.stream()
                    .map(item -> {
                        BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                        BigDecimal quantity = item.getPlanQuantity() != null ? item.getPlanQuantity() : BigDecimal.ZERO;
                        return price.multiply(quantity);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            respVO.setTotalAmount(totalAmount);
        }

        return respVO;
    }

    @Override
    public PageResult<OutboundRespVO> getOutboundPage(OutboundPageReqVO pageReqVO) {
        // 1. 分页查询出库单
        PageResult<OutboundDO> pageResult = outboundMapper.selectPage(pageReqVO);

        // 2. 转换为响应对象
        PageResult<OutboundRespVO> result = OutboundConvert.INSTANCE.convertPage(pageResult);

        // 3. 批量填充仓库信息
        if (CollUtil.isNotEmpty(result.getList())) {
            List<Long> warehouseIds = result.getList().stream()
                    .map(OutboundRespVO::getWarehouseId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, WarehouseDO> warehouseMap = warehouseService.getWarehouseMap(warehouseIds);
            result.getList().forEach(respVO -> 
                OutboundConvert.INSTANCE.fillOutboundRespVO(respVO, warehouseMap)
            );
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditOutbound(Long id, Long auditBy, String auditByName) {
        // 1. 校验出库单是否存在
        OutboundDO outbound = validateOutboundExists(id);

        // 2. 仅待审核状态可以审核
        if (!OutboundStatusEnum.PENDING.getStatus().equals(outbound.getStatus())) {
            throw exception(OUTBOUND_NOT_ALLOW_AUDIT);
        }

        // 3. 更新状态为已审核
        OutboundDO updateObj = new OutboundDO();
        updateObj.setId(id);
        updateObj.setStatus(OutboundStatusEnum.APPROVED.getStatus());
        updateObj.setAuditBy(auditBy);
        updateObj.setAuditByName(auditByName);
        updateObj.setAuditTime(LocalDateTime.now());
        outboundMapper.updateById(updateObj);

        log.info("[auditOutbound] 审核出库单成功，出库单号：{}, ID：{}, 审核人：{}", outbound.getOutboundNo(), id, auditByName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startPicking(Long id) {
        // 1. 校验出库单是否存在
        OutboundDO outbound = validateOutboundExists(id);

        // 2. 仅已审核状态可以开始拣货
        if (!OutboundStatusEnum.APPROVED.getStatus().equals(outbound.getStatus())) {
            throw exception(OUTBOUND_NOT_ALLOW_PICK);
        }

        // 3. 更新状态为拣货中
        OutboundDO updateObj = new OutboundDO();
        updateObj.setId(id);
        updateObj.setStatus(OutboundStatusEnum.PICKING.getStatus());
        outboundMapper.updateById(updateObj);

        log.info("[startPicking] 开始拣货，出库单号：{}, ID：{}", outbound.getOutboundNo(), id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completePicking(Long id, Long itemId, BigDecimal pickedQuantity) {
        // 1. 校验出库单是否存在
        OutboundDO outbound = validateOutboundExists(id);

        // 2. 仅已审核或拣货中状态可以拣货
        if (!OutboundStatusEnum.APPROVED.getStatus().equals(outbound.getStatus()) &&
            !OutboundStatusEnum.PICKING.getStatus().equals(outbound.getStatus())) {
            throw exception(OUTBOUND_NOT_ALLOW_PICK);
        }

        // 3. 校验明细是否存在
        OutboundItemDO item = outboundItemMapper.selectById(itemId);
        if (item == null || !item.getOutboundId().equals(id)) {
            throw exception(OUTBOUND_ITEM_NOT_EXISTS);
        }

        // 4. 校验拣货数量
        if (pickedQuantity == null || pickedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("[拣货失败] 拣货数量无效: 明细ID={}, 拣货数量={}", itemId, pickedQuantity);
            throw exception(OUTBOUND_QUANTITY_EXCEED);
        }
        if (pickedQuantity.compareTo(item.getPlanQuantity()) > 0) {
            log.error("[拣货失败] 拣货数量超过计划数量: 明细ID={}, 计划数量={}, 拣货数量={}", 
                itemId, item.getPlanQuantity(), pickedQuantity);
            throw exception(OUTBOUND_QUANTITY_EXCEED);
        }

        // 5. 更新明细拣货数量
        OutboundItemDO updateItem = new OutboundItemDO();
        updateItem.setId(itemId);
        updateItem.setPickedQuantity(pickedQuantity);
        outboundItemMapper.updateById(updateItem);

        log.info("[拣货明细] 出库单ID={}, 明细ID={}, 商品ID={}, 拣货数量={}", 
            id, itemId, item.getGoodsId(), pickedQuantity);

        // 6. 查询所有明细，计算总拣货数量
        List<OutboundItemDO> allItems = outboundItemMapper.selectListByOutboundId(id);
        BigDecimal totalPicked = allItems.stream()
                .map(i -> i.getPickedQuantity() != null ? i.getPickedQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. 检查是否所有明细都拣货完成（拣货数量 >= 计划数量）
        boolean allPicked = allItems.stream().allMatch(i -> 
            i.getPickedQuantity() != null && i.getPickedQuantity().compareTo(i.getPlanQuantity()) >= 0
        );

        // 8. 更新出库单状态和拣货数量
        OutboundDO updateObj = new OutboundDO();
        updateObj.setId(id);
        updateObj.setPickedQuantity(totalPicked);

        if (allPicked) {
            // 所有明细拣货完成，状态改为待发货
            updateObj.setStatus(OutboundStatusEnum.TO_SHIP.getStatus());
            log.info("[拣货完成] 出库单号={}, 总数量={}, 已拣货={}, 状态改为待发货", 
                outbound.getOutboundNo(), outbound.getTotalQuantity(), totalPicked);
        } else if (OutboundStatusEnum.APPROVED.getStatus().equals(outbound.getStatus())) {
            // 第一次拣货，状态改为拣货中
            updateObj.setStatus(OutboundStatusEnum.PICKING.getStatus());
            log.info("[开始拣货] 出库单号={}, 状态改为拣货中", outbound.getOutboundNo());
        }

        outboundMapper.updateById(updateObj);
        
        log.info("[拣货更新] 出库单号={}, 已拣货总数={}/{}", 
            outbound.getOutboundNo(), totalPicked, outbound.getTotalQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOutbound(Long id, Long completeBy, String completeByName) {
        // 1. 校验出库单是否存在
        OutboundDO outbound = validateOutboundExists(id);

        // 2. 仅待发货状态可以发货
        if (!OutboundStatusEnum.TO_SHIP.getStatus().equals(outbound.getStatus())) {
            throw exception(OUTBOUND_NOT_ALLOW_SHIP);
        }

        // 3. 更新状态为已发货
        OutboundDO updateObj = new OutboundDO();
        updateObj.setId(id);
        updateObj.setStatus(OutboundStatusEnum.SHIPPED.getStatus());
        updateObj.setCompleteBy(completeBy);
        updateObj.setCompleteByName(completeByName);
        updateObj.setCompleteTime(LocalDateTime.now());
        updateObj.setActualShipmentTime(LocalDateTime.now());
        outboundMapper.updateById(updateObj);

        // 4. 查询出库明细
        List<OutboundItemDO> items = outboundItemMapper.selectListByOutboundId(id);
        
        // 5. 校验拣货数量
        long unpickedCount = items.stream()
                .filter(item -> item.getPickedQuantity() == null || item.getPickedQuantity().compareTo(BigDecimal.ZERO) <= 0)
                .count();
        if (unpickedCount > 0) {
            log.error("[发货失败] 出库单号={}, 原因：还有{}个明细未拣货", outbound.getOutboundNo(), unpickedCount);
            throw exception(OUTBOUND_NOT_ALLOW_SHIP);
        }
        
        // 6. 更新所有明细的已发货数量
        items.forEach(item -> {
            OutboundItemDO updateItem = new OutboundItemDO();
            updateItem.setId(item.getId());
            updateItem.setShippedQuantity(item.getPickedQuantity());
            outboundItemMapper.updateById(updateItem);
        });

        // 7. 扣减库存并记录流水
        reduceInventory(outbound, items);

        log.info("[shipOutbound] 发货成功，出库单号：{}, ID：{}, 操作人：{}", outbound.getOutboundNo(), id, completeByName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOutbound(Long id) {
        // 1. 校验出库单是否存在
        OutboundDO outbound = validateOutboundExists(id);

        // 2. 已发货状态不能取消
        if (OutboundStatusEnum.SHIPPED.getStatus().equals(outbound.getStatus())) {
            throw exception(OUTBOUND_NOT_ALLOW_CANCEL);
        }

        // 3. 更新状态为已取消
        OutboundDO updateObj = new OutboundDO();
        updateObj.setId(id);
        updateObj.setStatus(OutboundStatusEnum.CANCELLED.getStatus());
        outboundMapper.updateById(updateObj);

        log.info("[cancelOutbound] 取消出库单成功，出库单号：{}, ID：{}", outbound.getOutboundNo(), id);
    }

    // ==================== 私有方法 ====================

    /**
     * 校验出库单是否存在
     *
     * @param id 出库单ID
     * @return 出库单DO
     */
    private OutboundDO validateOutboundExists(Long id) {
        OutboundDO outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw exception(OUTBOUND_NOT_EXISTS);
        }
        return outbound;
    }

    /**
     * 校验创建出库单的数据
     *
     * @param createReqVO 创建请求
     */
    private void validateOutboundForCreate(OutboundSaveReqVO createReqVO) {
        // 1. 校验仓库是否存在
        Assert.notNull(createReqVO.getWarehouseId(), "仓库ID不能为空");
        WarehouseDO warehouse = warehouseService.getWarehouse(createReqVO.getWarehouseId());
        if (warehouse == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }

        // 2. 校验明细数据
        if (CollUtil.isEmpty(createReqVO.getItems())) {
            throw exception(OUTBOUND_ITEM_EMPTY);
        }

        // 3. 校验商品是否存在
        List<Long> goodsIds = createReqVO.getItems().stream()
                .map(item -> item.getGoodsId())
                .distinct()
                .collect(Collectors.toList());
        Map<Long, GoodsDO> goodsMap = goodsService.getGoodsMap(goodsIds);
        createReqVO.getItems().forEach(item -> {
            if (!goodsMap.containsKey(item.getGoodsId())) {
                throw exception(GOODS_NOT_EXISTS);
            }
        });
    }

    /**
     * 校验更新出库单的数据
     *
     * @param updateReqVO 更新请求
     */
    private void validateOutboundForUpdate(OutboundSaveReqVO updateReqVO) {
        validateOutboundForCreate(updateReqVO);
    }

    /**
     * 计算总数量
     *
     * @param items 明细列表
     * @return 总数量
     */
    private BigDecimal calculateTotalQuantity(List<?> items) {
        if (CollUtil.isEmpty(items)) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .filter(item -> item instanceof com.laby.module.wms.controller.admin.outbound.vo.OutboundItemSaveReqVO)
                .map(item -> ((com.laby.module.wms.controller.admin.outbound.vo.OutboundItemSaveReqVO) item).getPlanQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePickedQuantity(Long outboundId, BigDecimal pickedQuantity) {
        // 1. 查询出库单
        OutboundDO outbound = outboundMapper.selectById(outboundId);
        if (outbound == null) {
            throw exception(OUTBOUND_NOT_EXISTS);
        }

        // 2. 更新已拣货数量（累加）
        OutboundDO updateObj = new OutboundDO();
        updateObj.setId(outboundId);
        updateObj.setPickedQuantity(outbound.getPickedQuantity().add(pickedQuantity));
        outboundMapper.updateById(updateObj);

        log.info("[出库单] 更新拣货数量，出库单ID：{}，本次拣货：{}，累计拣货：{}", 
                outboundId, pickedQuantity, updateObj.getPickedQuantity());

        // 3. 检查并更新拣货状态
        checkAndUpdatePickingStatus(outboundId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpdatePickingStatus(Long outboundId) {
        // 1. 查询出库单
        OutboundDO outbound = outboundMapper.selectById(outboundId);
        if (outbound == null) {
            throw exception(OUTBOUND_NOT_EXISTS);
        }

        // 2. 如果出库单不是"已审核"或"拣货中"状态，不处理
        if (!OutboundStatusEnum.APPROVED.getStatus().equals(outbound.getStatus()) &&
            !OutboundStatusEnum.PICKING.getStatus().equals(outbound.getStatus())) {
            return;
        }

        // 3. 比较已拣货数量和总数量
        // 如果已拣货数量 >= 总数量，说明拣货完成
        boolean allPicked = outbound.getPickedQuantity().compareTo(outbound.getTotalQuantity()) >= 0;

        // 4. 更新状态
        OutboundDO updateObj = new OutboundDO();
        updateObj.setId(outboundId);
        
        if (allPicked) {
            // 拣货完成，更新为"待发货"
            updateObj.setStatus(OutboundStatusEnum.TO_SHIP.getStatus());
            log.info("[出库单] 拣货完成，出库单ID：{}，总数量：{}，已拣货：{}", 
                    outboundId, outbound.getTotalQuantity(), outbound.getPickedQuantity());
        } else {
            // 部分拣货，更新为"拣货中"
            if (outbound.getPickedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                updateObj.setStatus(OutboundStatusEnum.PICKING.getStatus());
                log.info("[出库单] 拣货中，出库单ID：{}，总数量：{}，已拣货：{}", 
                        outboundId, outbound.getTotalQuantity(), outbound.getPickedQuantity());
            }
        }

        outboundMapper.updateById(updateObj);
    }
    
    /**
     * 扣减库存
     * 
     * 业务规则：
     * 1. 遍历出库明细
     * 2. 根据 仓库+库位+商品+批次+序列号 查找库存记录
     * 3. 校验库存是否足够
     * 4. 扣减库存数量
     * 5. 使用拣货数量（pickedQuantity）扣减库存
     * 6. 记录库存流水
     * 
     * @param outbound 出库单
     * @param items 出库明细列表
     */
    private void reduceInventory(OutboundDO outbound, List<OutboundItemDO> items) {
        log.info("[扣减库存] 出库单号={}, 明细数={}", outbound.getOutboundNo(), items.size());
        
        for (OutboundItemDO item : items) {
            // 只扣减拣货数量大于0的明细
            if (item.getPickedQuantity() == null || item.getPickedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[扣减库存] 跳过明细，拣货数量为0或空: 商品ID={}, 库位ID={}", item.getGoodsId(), item.getLocationId());
                continue;
            }
            
            // 查找库存记录（根据 仓库+库位+商品+批次+序列号）
            LambdaQueryWrapper<InventoryDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(InventoryDO::getWarehouseId, outbound.getWarehouseId())
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
            
            InventoryDO inventory = inventoryMapper.selectOne(queryWrapper);
            
            // 校验库存是否存在
            if (inventory == null) {
                log.error("[扣减库存失败] 库存不存在: 商品ID={}, 库位ID={}, 批次号={}", 
                    item.getGoodsId(), item.getLocationId(), item.getBatchNo());
                throw exception(INVENTORY_NOT_EXISTS);
            }
            
            // 校验库存是否足够（可用数量 = 库存数量 - 锁定数量）
            BigDecimal availableQuantity = inventory.getQuantity().subtract(inventory.getLockQuantity());
            if (availableQuantity.compareTo(item.getPickedQuantity()) < 0) {
                log.error("[扣减库存失败] 库存不足: 商品ID={}, 可用数量={}, 需要数量={}", 
                    item.getGoodsId(), availableQuantity, item.getPickedQuantity());
                throw exception(INVENTORY_NOT_ENOUGH);
            }
            
            // 扣减库存
            BigDecimal quantityBefore = inventory.getQuantity();
            BigDecimal quantityAfter = quantityBefore.subtract(item.getPickedQuantity());
            
            InventoryDO updateObj = new InventoryDO();
            updateObj.setId(inventory.getId());
            updateObj.setQuantity(quantityAfter);
            updateObj.setVersion(inventory.getVersion());
            int updateCount = inventoryMapper.updateById(updateObj);
            
            // 乐观锁失败
            if (updateCount == 0) {
                log.error("[扣减库存失败] 乐观锁冲突: 库存ID={}, 版本号={}", inventory.getId(), inventory.getVersion());
                throw exception(INVENTORY_LOCK_FAILED);
            }
            
            log.info("[扣减库存] 库存ID={}, 原数量={}, 出库数量={}, 新数量={}", 
                inventory.getId(), quantityBefore, item.getPickedQuantity(), quantityAfter);
            
            // 记录库存流水
            recordOutboundInventoryLog(outbound, item, quantityBefore, item.getPickedQuantity(), quantityAfter);
        }
        
        log.info("[扣减库存] 完成，出库单号={}", outbound.getOutboundNo());
    }
    
    /**
     * 记录出库库存流水
     * 
     * 业务说明：
     * - 每次出库扣减库存都要记录流水
     * - 变化数量为负数（表示减少）
     * - 记录操作类型、业务单号、操作人等信息
     * 
     * @param outbound 出库单
     * @param item 出库明细
     * @param quantityBefore 操作前数量
     * @param quantityChange 变化数量（正数，实际记录时取负）
     * @param quantityAfter 操作后数量
     */
    private void recordOutboundInventoryLog(OutboundDO outbound, OutboundItemDO item,
                                           BigDecimal quantityBefore, BigDecimal quantityChange, BigDecimal quantityAfter) {
        InventoryLogDO log = new InventoryLogDO();
        log.setWarehouseId(outbound.getWarehouseId());
        log.setGoodsId(item.getGoodsId());
        log.setLocationId(item.getLocationId());
        log.setBatchNo(item.getBatchNo());
        log.setOperationType("OUTBOUND");  // 出库操作
        log.setQuantityBefore(quantityBefore);
        log.setQuantityChange(quantityChange.negate());  // 负数表示减少
        log.setQuantityAfter(quantityAfter);
        log.setBusinessType("SALES_OUTBOUND");  // 业务类型：销售出库
        log.setBusinessNo(outbound.getOutboundNo());  // 业务单号：出库单号
        
        // 获取操作人信息
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String operator = loginUser != null && loginUser.getInfo() != null 
            ? loginUser.getInfo().get(LoginUser.INFO_KEY_NICKNAME) 
            : "系统";
        log.setOperator(operator);
        
        log.setRemark("出库单发货，扣减库存");
        
        inventoryLogMapper.insert(log);
        
        OutboundServiceImpl.log.info("[记录库存流水] 出库单号={}, 商品ID={}, 操作前={}, 变化量={}, 操作后={}", 
            outbound.getOutboundNo(), item.getGoodsId(), quantityBefore, log.getQuantityChange(), quantityAfter);
    }
}

