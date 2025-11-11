package com.laby.module.wms.service.stocktaking;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.laby.framework.codegen.core.enums.CodePrefixEnum;
import com.laby.framework.codegen.core.service.CodeGeneratorService;
import com.laby.framework.common.pojo.PageResult;
import com.laby.module.wms.controller.admin.stocktaking.vo.StockTakingPageReqVO;
import com.laby.module.wms.controller.admin.stocktaking.vo.StockTakingRespVO;
import com.laby.module.wms.controller.admin.stocktaking.vo.StockTakingSaveReqVO;
import com.laby.module.wms.convert.stocktaking.StockTakingConvert;
import com.laby.module.wms.dal.dataobject.inventory.InventoryDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryLogDO;
import com.laby.module.wms.dal.dataobject.stocktaking.StockTakingDO;
import com.laby.module.wms.dal.dataobject.stocktaking.StockTakingPlanDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseDO;
import com.laby.module.wms.dal.mysql.inventory.InventoryLogMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryMapper;
import com.laby.module.wms.dal.mysql.stocktaking.StockTakingMapper;
import com.laby.module.wms.dal.mysql.stocktaking.StockTakingPlanMapper;
import com.laby.module.wms.dal.mysql.warehouse.WarehouseMapper;
import com.laby.module.wms.enums.StockTakingPlanStatusEnum;
import com.laby.module.wms.enums.StockTakingStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.laby.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.laby.module.wms.enums.ErrorCodeConstants.*;

/**
 * 盘点单 Service 实现类
 *
 * @author laby
 */
@Service
@Validated
@Slf4j
public class StockTakingServiceImpl implements StockTakingService {

    @Resource
    private StockTakingMapper stockTakingMapper;

    @Resource
    private StockTakingPlanMapper stockTakingPlanMapper;

    @Resource
    private WarehouseMapper warehouseMapper;

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private InventoryLogMapper inventoryLogMapper;

    @Resource
    private CodeGeneratorService codeGeneratorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockTaking(StockTakingSaveReqVO createReqVO) {
        // 1. 转换为 DO 对象
        StockTakingDO taking = StockTakingConvert.INSTANCE.convert(createReqVO);

        // 2. 生成盘点单号（使用编码生成器）
        String takingNo = codeGeneratorService.generateCode(CodePrefixEnum.STOCK_TAKING);
        taking.setTakingNo(takingNo);

        // 3. 设置初始状态为待盘点
        taking.setStatus(StockTakingStatusEnum.PENDING.getStatus());

        // 4. 插入数据库
        stockTakingMapper.insert(taking);

        log.info("[盘点单] 创建盘点单，盘点单号：{}，商品：{}", takingNo, taking.getGoodsName());

        return taking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockTaking(StockTakingSaveReqVO updateReqVO) {
        // 1. 校验盘点单是否存在
        StockTakingDO taking = validateStockTakingExists(updateReqVO.getId());

        // 2. 校验状态是否允许修改（只有待盘点状态可以修改）
        if (!StockTakingStatusEnum.PENDING.getStatus().equals(taking.getStatus())) {
            throw exception(STOCK_TAKING_NOT_ALLOW_UPDATE);
        }

        // 3. 转换并更新
        StockTakingDO updateObj = StockTakingConvert.INSTANCE.convert(updateReqVO);
        updateObj.setId(updateReqVO.getId());
        stockTakingMapper.updateById(updateObj);

        log.info("[盘点单] 更新盘点单，盘点单号：{}", taking.getTakingNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockTaking(Long id) {
        // 1. 校验盘点单是否存在
        StockTakingDO taking = validateStockTakingExists(id);

        // 2. 校验状态是否允许删除（只有待盘点状态可以删除）
        if (!StockTakingStatusEnum.PENDING.getStatus().equals(taking.getStatus())) {
            throw exception(STOCK_TAKING_NOT_ALLOW_UPDATE);
        }

        // 3. 删除
        stockTakingMapper.deleteById(id);

        log.info("[盘点单] 删除盘点单，盘点单号：{}", taking.getTakingNo());
    }

    @Override
    public StockTakingRespVO getStockTaking(Long id) {
        // 1. 查询盘点单
        StockTakingDO taking = validateStockTakingExists(id);

        // 2. 转换为 VO
        StockTakingRespVO respVO = StockTakingConvert.INSTANCE.convert(taking);

        // 3. 填充仓库名称
        if (taking.getWarehouseId() != null) {
            WarehouseDO warehouse = warehouseMapper.selectById(taking.getWarehouseId());
            if (warehouse != null) {
                respVO.setWarehouseName(warehouse.getWarehouseName());
            }
        }

        return respVO;
    }

    @Override
    public PageResult<StockTakingRespVO> getStockTakingPage(StockTakingPageReqVO pageReqVO) {
        // 1. 分页查询
        PageResult<StockTakingDO> pageResult = stockTakingMapper.selectPage(pageReqVO);

        // 2. 转换为 VO
        PageResult<StockTakingRespVO> voPageResult = StockTakingConvert.INSTANCE.convertPage(pageResult);

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
    public void submitStockTaking(Long id, BigDecimal actualQuantity, String diffReason) {
        // 1. 校验盘点单是否存在
        StockTakingDO taking = validateStockTakingExists(id);

        // 2. 校验状态是否允许提交（只有待盘点状态可以提交）
        if (!StockTakingStatusEnum.PENDING.getStatus().equals(taking.getStatus())) {
            throw exception(STOCK_TAKING_NOT_ALLOW_UPDATE);
        }

        // 3. 更新实盘数量和状态
        StockTakingDO updateObj = new StockTakingDO();
        updateObj.setId(id);
        updateObj.setActualQuantity(actualQuantity);
        updateObj.setDiffReason(diffReason);
        updateObj.setStatus(StockTakingStatusEnum.COUNTED.getStatus());
        updateObj.setOperateTime(LocalDateTime.now());
        // TODO: 获取当前登录用户名作为盘点人
        updateObj.setOperator("系统");
        stockTakingMapper.updateById(updateObj);

        log.info("[盘点单] 提交盘点，盘点单号：{}，账面：{}，实盘：{}", 
                taking.getTakingNo(), taking.getBookQuantity(), actualQuantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewStockTaking(Long id) {
        // 1. 校验盘点单是否存在
        StockTakingDO taking = validateStockTakingExists(id);

        // 2. 校验状态是否允许复核（只有已盘点状态可以复核）
        if (!StockTakingStatusEnum.COUNTED.getStatus().equals(taking.getStatus())) {
            throw exception(STOCK_TAKING_NOT_ALLOW_UPDATE);
        }

        // 3. 更新状态为已复核
        StockTakingDO updateObj = new StockTakingDO();
        updateObj.setId(id);
        updateObj.setStatus(StockTakingStatusEnum.REVIEWED.getStatus());
        updateObj.setReviewTime(LocalDateTime.now());
        // TODO: 获取当前登录用户名作为复核人
        updateObj.setReviewer("系统");
        stockTakingMapper.updateById(updateObj);

        log.info("[盘点单] 复核盘点，盘点单号：{}", taking.getTakingNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStockTaking(Long id) {
        // 1. 校验盘点单是否存在
        StockTakingDO taking = validateStockTakingExists(id);

        // 2. 校验状态是否允许调整（只有已复核状态可以调整）
        if (!StockTakingStatusEnum.REVIEWED.getStatus().equals(taking.getStatus())) {
            throw exception(STOCK_TAKING_NOT_ALLOW_ADJUST);
        }

        // 3. 计算差异数量
        BigDecimal actualQuantity = taking.getActualQuantity() != null ? taking.getActualQuantity() : BigDecimal.ZERO;
        BigDecimal bookQuantity = taking.getBookQuantity() != null ? taking.getBookQuantity() : BigDecimal.ZERO;
        BigDecimal diffQuantity = actualQuantity.subtract(bookQuantity);

        log.info("[盘点单] 开始调整库存，盘点单号：{}，账面：{}，实盘：{}，差异：{}", 
                taking.getTakingNo(), bookQuantity, actualQuantity, diffQuantity);

        // 4. 如果有差异，调整库存
        if (diffQuantity.compareTo(BigDecimal.ZERO) != 0) {
            adjustInventory(taking, diffQuantity);
        }

        // 5. 更新盘点单状态为已调整
        StockTakingDO updateObj = new StockTakingDO();
        updateObj.setId(id);
        updateObj.setStatus(StockTakingStatusEnum.ADJUSTED.getStatus());
        stockTakingMapper.updateById(updateObj);

        // 6. 更新盘点计划进度
        if (taking.getPlanId() != null) {
            updatePlanProgress(taking.getPlanId());
        }

        log.info("[盘点单] 调整库存完成，盘点单号：{}，差异：{}", taking.getTakingNo(), diffQuantity);
    }

    /**
     * 调整库存
     */
    private void adjustInventory(StockTakingDO taking, BigDecimal diffQuantity) {
        // 1. 查询库存记录
        LambdaQueryWrapper<InventoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryDO::getWarehouseId, taking.getWarehouseId())
                .eq(InventoryDO::getLocationId, taking.getLocationId())
                .eq(InventoryDO::getGoodsId, taking.getGoodsId());
        
        // 如果有批次号，则加入条件
        if (taking.getBatchNo() != null) {
            wrapper.eq(InventoryDO::getBatchNo, taking.getBatchNo());
        } else {
            wrapper.isNull(InventoryDO::getBatchNo);
        }
        
        InventoryDO inventory = inventoryMapper.selectOne(wrapper);

        // 2. 调整库存数量
        if (inventory == null) {
            // 库存不存在，创建新库存记录（盘盈情况）
            if (diffQuantity.compareTo(BigDecimal.ZERO) > 0) {
                inventory = new InventoryDO();
                inventory.setWarehouseId(taking.getWarehouseId());
                inventory.setLocationId(taking.getLocationId());
                inventory.setGoodsId(taking.getGoodsId());
                inventory.setBatchNo(taking.getBatchNo());
                inventory.setQuantity(diffQuantity);
                inventory.setLockQuantity(BigDecimal.ZERO);
                inventoryMapper.insert(inventory);
                log.info("[盘点调整] 创建库存记录，仓库：{}，库位：{}，商品：{}，数量：{}", 
                        taking.getWarehouseId(), taking.getLocationId(), taking.getGoodsId(), diffQuantity);
            } else {
                log.warn("[盘点调整] 库存不存在且盘亏，无法调整，盘点单号：{}", taking.getTakingNo());
                return;
            }
        } else {
            // 库存存在，更新数量
            BigDecimal oldQuantity = inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO;
            BigDecimal newQuantity = oldQuantity.add(diffQuantity);
            
            // 检查新数量是否合法（不能为负数）
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                log.error("[盘点调整] 调整后库存数量为负数，盘点单号：{}，原数量：{}，调整：{}", 
                        taking.getTakingNo(), oldQuantity, diffQuantity);
                throw exception(INVENTORY_QUANTITY_INVALID);
            }
            
            // 使用乐观锁更新库存
            LambdaUpdateWrapper<InventoryDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(InventoryDO::getId, inventory.getId())
                    .eq(InventoryDO::getVersion, inventory.getVersion())
                    .set(InventoryDO::getQuantity, newQuantity);
            
            int updateResult = inventoryMapper.update(null, updateWrapper);
            
            if (updateResult <= 0) {
                log.error("[盘点调整] 库存更新失败（乐观锁），盘点单号：{}", taking.getTakingNo());
                throw exception(INVENTORY_UPDATE_FAIL);
            }
            
            log.info("[盘点调整] 更新库存，仓库：{}，库位：{}，商品：{}，原数量：{}，新数量：{}", 
                    taking.getWarehouseId(), taking.getLocationId(), taking.getGoodsId(), 
                    oldQuantity, newQuantity);
        }

        // 3. 记录库存流水
        BigDecimal quantityBefore = inventory != null && inventory.getQuantity() != null ? 
                inventory.getQuantity() : BigDecimal.ZERO;
        BigDecimal quantityAfter = quantityBefore.add(diffQuantity);
        
        InventoryLogDO logDO = new InventoryLogDO();
        logDO.setWarehouseId(taking.getWarehouseId());
        logDO.setLocationId(taking.getLocationId());
        logDO.setGoodsId(taking.getGoodsId());
        logDO.setBatchNo(taking.getBatchNo());
        logDO.setOperationType("STOCK_TAKING"); // 盘点调整
        logDO.setQuantityBefore(quantityBefore);
        logDO.setQuantityChange(diffQuantity);
        logDO.setQuantityAfter(quantityAfter);
        logDO.setBusinessNo(taking.getTakingNo());
        logDO.setOperator("系统");
        logDO.setRemark(String.format("盘点调整，账面：%s，实盘：%s，差异：%s", 
                taking.getBookQuantity(), taking.getActualQuantity(), diffQuantity));
        inventoryLogMapper.insert(logDO);
    }

    /**
     * 更新盘点计划进度
     */
    private void updatePlanProgress(Long planId) {
        // 1. 查询计划
        StockTakingPlanDO plan = stockTakingPlanMapper.selectById(planId);
        if (plan == null) {
            log.warn("[盘点单] 盘点计划不存在，planId：{}", planId);
            return;
        }

        // 2. 统计已完成和有差异的盘点单数量
        LambdaQueryWrapper<StockTakingDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTakingDO::getPlanId, planId);
        
        // 已完成数量（状态为已调整）
        wrapper.eq(StockTakingDO::getStatus, StockTakingStatusEnum.ADJUSTED.getStatus());
        Long completedCount = stockTakingMapper.selectCount(wrapper);
        
        // 有差异的数量（账面数量不等于实盘数量）
        wrapper.clear();
        wrapper.eq(StockTakingDO::getPlanId, planId)
                .eq(StockTakingDO::getStatus, StockTakingStatusEnum.ADJUSTED.getStatus())
                .apply("ABS(actual_quantity - book_quantity) > 0.001");  // 使用SQL判断差异
        Long diffCount = stockTakingMapper.selectCount(wrapper);

        // 3. 更新计划进度
        StockTakingPlanDO updateObj = StockTakingPlanDO.builder()
                .id(planId)
                .completedCount(completedCount.intValue())
                .diffCount(diffCount.intValue())
                .build();
        
        // 4. 如果全部完成，更新计划状态为已完成
        if (completedCount.equals(plan.getTotalCount().longValue())) {
            updateObj.setStatus(StockTakingPlanStatusEnum.COMPLETED.getStatus());
            updateObj.setActualEndTime(LocalDateTime.now());
        }
        
        stockTakingPlanMapper.updateById(updateObj);

        log.info("[盘点单] 更新计划进度，计划ID：{}，总数：{}，已完成：{}，有差异：{}", 
                planId, plan.getTotalCount(), completedCount, diffCount);
    }

    /**
     * 校验盘点单是否存在
     */
    private StockTakingDO validateStockTakingExists(Long id) {
        StockTakingDO taking = stockTakingMapper.selectById(id);
        if (taking == null) {
            throw exception(STOCK_TAKING_NOT_EXISTS);
        }
        return taking;
    }

}

