package com.laby.module.wms.service.stocktaking;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laby.framework.codegen.core.enums.CodePrefixEnum;
import com.laby.framework.codegen.core.service.CodeGeneratorService;
import com.laby.framework.common.pojo.PageResult;
import com.laby.module.wms.controller.admin.stocktaking.vo.StockTakingPlanPageReqVO;
import com.laby.module.wms.controller.admin.stocktaking.vo.StockTakingPlanRespVO;
import com.laby.module.wms.controller.admin.stocktaking.vo.StockTakingPlanSaveReqVO;
import com.laby.module.wms.convert.stocktaking.StockTakingPlanConvert;
import com.laby.module.wms.dal.dataobject.goods.GoodsDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryDO;
import com.laby.module.wms.dal.dataobject.stocktaking.StockTakingDO;
import com.laby.module.wms.dal.dataobject.stocktaking.StockTakingPlanDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseLocationDO;
import com.laby.module.wms.dal.mysql.goods.GoodsMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryMapper;
import com.laby.module.wms.dal.mysql.stocktaking.StockTakingMapper;
import com.laby.module.wms.dal.mysql.stocktaking.StockTakingPlanMapper;
import com.laby.module.wms.dal.mysql.warehouse.WarehouseLocationMapper;
import com.laby.module.wms.dal.mysql.warehouse.WarehouseMapper;
import com.laby.module.wms.enums.StockTakingPlanStatusEnum;
import com.laby.module.wms.enums.StockTakingScopeTypeEnum;
import com.laby.module.wms.enums.StockTakingStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.laby.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.laby.module.wms.enums.ErrorCodeConstants.*;

/**
 * 盘点计划 Service 实现类
 *
 * @author laby
 */
@Service
@Validated
@Slf4j
public class StockTakingPlanServiceImpl implements StockTakingPlanService {

    @Resource
    private StockTakingPlanMapper stockTakingPlanMapper;

    @Resource
    private StockTakingMapper stockTakingMapper;

    @Resource
    private WarehouseMapper warehouseMapper;

    @Resource
    private WarehouseLocationMapper warehouseLocationMapper;

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private CodeGeneratorService codeGeneratorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockTakingPlan(StockTakingPlanSaveReqVO createReqVO) {
        // 1. 转换为 DO 对象
        StockTakingPlanDO plan = StockTakingPlanConvert.INSTANCE.convert(createReqVO);

        // 2. 生成计划编号（使用编码生成器）
        String planNo = codeGeneratorService.generateCode(CodePrefixEnum.STOCK_TAKING_PLAN);
        plan.setPlanNo(planNo);

        // 3. 设置初始状态为待审核，初始化计数器
        plan.setStatus(StockTakingPlanStatusEnum.PENDING_AUDIT.getStatus());
        plan.setTotalCount(0);
        plan.setCompletedCount(0);
        plan.setDiffCount(0);

        // 4. 插入数据库
        stockTakingPlanMapper.insert(plan);

        log.info("[盘点计划] 创建盘点计划，计划编号：{}，计划名称：{}", planNo, plan.getPlanName());

        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockTakingPlan(StockTakingPlanSaveReqVO updateReqVO) {
        // 1. 校验盘点计划是否存在
        StockTakingPlanDO plan = validateStockTakingPlanExists(updateReqVO.getId());

        // 2. 校验状态是否允许修改（只有待审核状态可以修改）
        if (!StockTakingPlanStatusEnum.PENDING_AUDIT.getStatus().equals(plan.getStatus())) {
            throw exception(STOCK_TAKING_PLAN_NOT_ALLOW_UPDATE);
        }

        // 3. 转换并更新
        StockTakingPlanDO updateObj = StockTakingPlanConvert.INSTANCE.convert(updateReqVO);
        updateObj.setId(updateReqVO.getId());
        stockTakingPlanMapper.updateById(updateObj);

        log.info("[盘点计划] 更新盘点计划，计划编号：{}", plan.getPlanNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockTakingPlan(Long id) {
        // 1. 校验盘点计划是否存在
        StockTakingPlanDO plan = validateStockTakingPlanExists(id);

        // 2. 校验状态是否允许删除（只有待审核状态可以删除）
        if (!StockTakingPlanStatusEnum.PENDING_AUDIT.getStatus().equals(plan.getStatus())) {
            throw exception(STOCK_TAKING_PLAN_NOT_ALLOW_DELETE);
        }

        // 3. 删除
        stockTakingPlanMapper.deleteById(id);

        log.info("[盘点计划] 删除盘点计划，计划编号：{}", plan.getPlanNo());
    }

    @Override
    public StockTakingPlanRespVO getStockTakingPlan(Long id) {
        // 1. 查询盘点计划
        StockTakingPlanDO plan = validateStockTakingPlanExists(id);

        // 2. 转换为 VO
        StockTakingPlanRespVO respVO = StockTakingPlanConvert.INSTANCE.convert(plan);

        // 3. 填充仓库名称
        if (plan.getWarehouseId() != null) {
            WarehouseDO warehouse = warehouseMapper.selectById(plan.getWarehouseId());
            if (warehouse != null) {
                respVO.setWarehouseName(warehouse.getWarehouseName());
            }
        }

        return respVO;
    }

    @Override
    public PageResult<StockTakingPlanRespVO> getStockTakingPlanPage(StockTakingPlanPageReqVO pageReqVO) {
        // 1. 分页查询
        PageResult<StockTakingPlanDO> pageResult = stockTakingPlanMapper.selectPage(pageReqVO);

        // 2. 转换为 VO
        PageResult<StockTakingPlanRespVO> voPageResult = StockTakingPlanConvert.INSTANCE.convertPage(pageResult);

        // 3. 填充仓库名称
        voPageResult.getList().forEach(item -> {
            if (item.getWarehouseId() != null) {
                WarehouseDO warehouse = warehouseMapper.selectById(item.getWarehouseId());
                if (warehouse != null) {
                    item.setWarehouseName(warehouse.getWarehouseName());
                }
            }
        });

        return voPageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditStockTakingPlan(Long id) {
        // 1. 校验盘点计划是否存在
        StockTakingPlanDO plan = validateStockTakingPlanExists(id);

        // 2. 校验状态是否允许审核（只有待审核状态可以审核）
        if (!StockTakingPlanStatusEnum.PENDING_AUDIT.getStatus().equals(plan.getStatus())) {
            throw exception(STOCK_TAKING_PLAN_NOT_ALLOW_AUDIT);
        }

        // 3. 更新状态为待执行
        StockTakingPlanDO updateObj = new StockTakingPlanDO();
        updateObj.setId(id);
        updateObj.setStatus(StockTakingPlanStatusEnum.PENDING.getStatus());
        updateObj.setAuditTime(LocalDateTime.now());
        // TODO: 获取当前登录用户名作为审核人
        updateObj.setAuditUser("系统");
        stockTakingPlanMapper.updateById(updateObj);

        log.info("[盘点计划] 审核盘点计划，计划编号：{}", plan.getPlanNo());

        // 4. 自动生成盘点单
        int count = generateStockTaking(id);
        log.info("[盘点计划] 自动生成{}条盘点单", count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelStockTakingPlan(Long id) {
        // 1. 校验盘点计划是否存在
        StockTakingPlanDO plan = validateStockTakingPlanExists(id);

        // 2. 校验状态是否允许取消（待审核、待执行、执行中状态可以取消）
        if (!StockTakingPlanStatusEnum.PENDING_AUDIT.getStatus().equals(plan.getStatus()) 
                && !StockTakingPlanStatusEnum.PENDING.getStatus().equals(plan.getStatus())
                && !StockTakingPlanStatusEnum.PROCESSING.getStatus().equals(plan.getStatus())) {
            throw exception(STOCK_TAKING_PLAN_NOT_ALLOW_CANCEL);
        }

        // 3. 更新状态为已取消
        StockTakingPlanDO updateObj = new StockTakingPlanDO();
        updateObj.setId(id);
        updateObj.setStatus(StockTakingPlanStatusEnum.CANCELLED.getStatus());
        stockTakingPlanMapper.updateById(updateObj);

        log.info("[盘点计划] 取消盘点计划，计划编号：{}", plan.getPlanNo());
    }

    /**
     * 校验盘点计划是否存在
     */
    private StockTakingPlanDO validateStockTakingPlanExists(Long id) {
        StockTakingPlanDO plan = stockTakingPlanMapper.selectById(id);
        if (plan == null) {
            throw exception(STOCK_TAKING_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateStockTaking(Long id) {
        // 1. 校验盘点计划是否存在
        StockTakingPlanDO plan = validateStockTakingPlanExists(id);

        // 2. 校验状态（待执行、执行中状态可以生成）
        if (!StockTakingPlanStatusEnum.PENDING.getStatus().equals(plan.getStatus())
                && !StockTakingPlanStatusEnum.PROCESSING.getStatus().equals(plan.getStatus())) {
            throw exception(STOCK_TAKING_PLAN_NOT_ALLOW_GENERATE);
        }

        log.info("[盘点计划] 开始生成盘点单，计划编号：{}，范围类型：{}", plan.getPlanNo(), plan.getScopeType());

        // 3. 根据范围类型查询库存
        List<InventoryDO> inventoryList = queryInventoryByScope(plan);
        if (CollUtil.isEmpty(inventoryList)) {
            log.warn("[盘点计划] 没有查询到库存数据，计划编号：{}", plan.getPlanNo());
            return 0;
        }

        log.info("[盘点计划] 查询到{}条库存记录", inventoryList.size());

        // 4. 批量查询关联信息
        List<Long> goodsIds = inventoryList.stream().map(InventoryDO::getGoodsId).distinct().collect(Collectors.toList());
        List<Long> locationIds = inventoryList.stream().map(InventoryDO::getLocationId).distinct().collect(Collectors.toList());
        
        Map<Long, GoodsDO> goodsMap = goodsMapper.selectByIds(goodsIds).stream()
                .collect(Collectors.toMap(GoodsDO::getId, g -> g));
        Map<Long, WarehouseLocationDO> locationMap = warehouseLocationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(WarehouseLocationDO::getId, l -> l));

        // 5. 生成盘点单
        int count = 0;
        for (InventoryDO inventory : inventoryList) {
            StockTakingDO taking = new StockTakingDO();
            taking.setTakingNo(codeGeneratorService.generateCode(CodePrefixEnum.STOCK_TAKING));
            taking.setPlanId(plan.getId());
            taking.setPlanNo(plan.getPlanNo());
            taking.setWarehouseId(inventory.getWarehouseId());
            taking.setLocationId(inventory.getLocationId());
            taking.setGoodsId(inventory.getGoodsId());
            taking.setBatchNo(inventory.getBatchNo());
            
            // 填充库位信息
            WarehouseLocationDO location = locationMap.get(inventory.getLocationId());
            if (location != null) {
                taking.setLocationCode(location.getLocationCode());
            }
            
            // 填充商品信息
            GoodsDO goods = goodsMap.get(inventory.getGoodsId());
            if (goods != null) {
                taking.setSkuCode(goods.getSkuCode());
                taking.setGoodsName(goods.getGoodsName());
            }
            
            // 设置账面数量（可用数量）
            BigDecimal bookQuantity = (inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO)
                    .subtract(inventory.getLockQuantity() != null ? inventory.getLockQuantity() : BigDecimal.ZERO);
            taking.setBookQuantity(bookQuantity);
            
            // 设置初始状态
            taking.setStatus(StockTakingStatusEnum.PENDING.getStatus());
            
            stockTakingMapper.insert(taking);
            count++;
        }

        // 6. 更新计划状态和总数
        StockTakingPlanDO updateObj = StockTakingPlanDO.builder()
                .id(plan.getId())
                .totalCount(count)
                .status(StockTakingPlanStatusEnum.PROCESSING.getStatus())
                .actualStartTime(LocalDateTime.now())
                .build();
        stockTakingPlanMapper.updateById(updateObj);

        log.info("[盘点计划] 生成盘点单完成，计划编号：{}，生成数量：{}", plan.getPlanNo(), count);
        return count;
    }

    /**
     * 根据盘点计划的范围查询库存
     */
    private List<InventoryDO> queryInventoryByScope(StockTakingPlanDO plan) {
        Integer scopeType = plan.getScopeType();
        String scopeValue = plan.getScopeValue();
        
        // 1-全仓
        if (StockTakingScopeTypeEnum.WAREHOUSE.getType().equals(scopeType)) {
            return inventoryMapper.selectList(InventoryDO::getWarehouseId, plan.getWarehouseId());
        }
        
        // 2-库区
        if (StockTakingScopeTypeEnum.AREA.getType().equals(scopeType)) {
            List<Long> areaIds = parseIdList(scopeValue);
            if (CollUtil.isEmpty(areaIds)) {
                return new ArrayList<>();
            }
            // 查询库区下的所有库位
            LambdaQueryWrapper<WarehouseLocationDO> locationWrapper = new LambdaQueryWrapper<>();
            locationWrapper.in(WarehouseLocationDO::getAreaId, areaIds);
            List<WarehouseLocationDO> locations = warehouseLocationMapper.selectList(locationWrapper);
            if (CollUtil.isEmpty(locations)) {
                return new ArrayList<>();
            }
            List<Long> locationIds = locations.stream().map(WarehouseLocationDO::getId).collect(Collectors.toList());
            LambdaQueryWrapper<InventoryDO> inventoryWrapper = new LambdaQueryWrapper<>();
            inventoryWrapper.in(InventoryDO::getLocationId, locationIds);
            return inventoryMapper.selectList(inventoryWrapper);
        }
        
        // 3-库位
        if (StockTakingScopeTypeEnum.LOCATION.getType().equals(scopeType)) {
            List<Long> locationIds = parseIdList(scopeValue);
            if (CollUtil.isEmpty(locationIds)) {
                return new ArrayList<>();
            }
            LambdaQueryWrapper<InventoryDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(InventoryDO::getLocationId, locationIds);
            return inventoryMapper.selectList(wrapper);
        }
        
        // 4-商品
        if (StockTakingScopeTypeEnum.GOODS.getType().equals(scopeType)) {
            List<Long> goodsIds = parseIdList(scopeValue);
            if (CollUtil.isEmpty(goodsIds)) {
                return new ArrayList<>();
            }
            LambdaQueryWrapper<InventoryDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InventoryDO::getWarehouseId, plan.getWarehouseId())
                    .in(InventoryDO::getGoodsId, goodsIds);
            return inventoryMapper.selectList(wrapper);
        }
        
        return new ArrayList<>();
    }

    /**
     * 解析ID列表
     * 支持JSON数组格式：[1,2,3] 或逗号分隔格式：1,2,3
     */
    private List<Long> parseIdList(String value) {
        if (StrUtil.isBlank(value)) {
            return new ArrayList<>();
        }
        
        try {
            // 尝试解析JSON数组
            if (value.trim().startsWith("[")) {
                return JSONUtil.toList(value, Long.class);
            }
            
            // 否则按逗号分隔
            List<Long> result = new ArrayList<>();
            String[] parts = value.split(",");
            for (String part : parts) {
                if (StrUtil.isNotBlank(part)) {
                    result.add(Long.parseLong(part.trim()));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("[盘点计划] 解析ID列表失败，value：{}", value, e);
            return new ArrayList<>();
        }
    }

}

