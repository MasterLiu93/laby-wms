package com.laby.module.wms.service.stockmove;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.laby.framework.common.pojo.PageResult;
import com.laby.module.wms.controller.admin.stockmove.vo.StockMovePageReqVO;
import com.laby.module.wms.controller.admin.stockmove.vo.StockMoveRespVO;
import com.laby.module.wms.controller.admin.stockmove.vo.StockMoveSaveReqVO;
import com.laby.module.wms.convert.stockmove.StockMoveConvert;
import com.laby.module.wms.dal.dataobject.stockmove.StockMoveDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryLogDO;
import com.laby.module.wms.dal.mysql.stockmove.StockMoveMapper;
import com.laby.module.wms.dal.mysql.warehouse.WarehouseMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryLogMapper;
import com.laby.module.wms.enums.StockMoveStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laby.framework.security.core.LoginUser;
import com.laby.framework.security.core.util.SecurityFrameworkUtils;

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

import static com.laby.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.laby.module.wms.enums.ErrorCodeConstants.*;

/**
 * 移库管理 Service 实现类
 *
 * @author laby
 */
@Service
@Validated
@Slf4j
public class StockMoveServiceImpl implements StockMoveService {

    @Resource
    private StockMoveMapper stockMoveMapper;

    @Resource
    private WarehouseMapper warehouseMapper;
    
    @Resource
    private InventoryMapper inventoryMapper;
    
    @Resource
    private InventoryLogMapper inventoryLogMapper;

    /**
     * 创建移库单
     *
     * @param createReqVO 创建信息
     * @return 移库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockMove(StockMoveSaveReqVO createReqVO) {
        // 1. 校验源库位和目标库位不能相同
        if (createReqVO.getFromLocationId().equals(createReqVO.getToLocationId())) {
            throw exception(STOCK_MOVE_SAME_LOCATION);
        }

        // 2. 转换为 DO 对象
        StockMoveDO stockMove = StockMoveConvert.INSTANCE.convert(createReqVO);

        // 3. 生成移库单号（格式：MOVE + yyyyMMdd + 4位序列号）
        String moveNo = generateMoveNo();
        stockMove.setMoveNo(moveNo);

        // 4. 设置初始状态为待执行
        stockMove.setStatus(StockMoveStatusEnum.PENDING.getStatus());

        // 5. 插入数据库
        stockMoveMapper.insert(stockMove);

        log.info("[移库管理] 创建移库单，移库单号：{}，商品：{}，数量：{}", 
                moveNo, stockMove.getGoodsName(), stockMove.getQuantity());

        return stockMove.getId();
    }

    /**
     * 更新移库单
     *
     * @param updateReqVO 更新信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockMove(StockMoveSaveReqVO updateReqVO) {
        // 1. 校验移库单是否存在
        StockMoveDO stockMove = validateStockMoveExists(updateReqVO.getId());

        // 2. 校验移库单状态是否允许修改（只有待执行状态可以修改）
        if (!StockMoveStatusEnum.PENDING.getStatus().equals(stockMove.getStatus())) {
            throw exception(STOCK_MOVE_NOT_ALLOW_UPDATE);
        }

        // 3. 校验源库位和目标库位不能相同
        if (updateReqVO.getFromLocationId().equals(updateReqVO.getToLocationId())) {
            throw exception(STOCK_MOVE_SAME_LOCATION);
        }

        // 4. 转换为 DO 对象并更新
        StockMoveDO updateObj = StockMoveConvert.INSTANCE.convert(updateReqVO);
        updateObj.setId(updateReqVO.getId());
        stockMoveMapper.updateById(updateObj);

        log.info("[移库管理] 更新移库单，移库单号：{}", stockMove.getMoveNo());
    }

    /**
     * 删除移库单
     *
     * @param id 移库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockMove(Long id) {
        // 1. 校验移库单是否存在
        StockMoveDO stockMove = validateStockMoveExists(id);

        // 2. 校验移库单状态是否允许删除（只有待执行状态可以删除）
        if (!StockMoveStatusEnum.PENDING.getStatus().equals(stockMove.getStatus())) {
            throw exception(STOCK_MOVE_NOT_ALLOW_DELETE);
        }

        // 3. 删除移库单
        stockMoveMapper.deleteById(id);

        log.info("[移库管理] 删除移库单，移库单号：{}", stockMove.getMoveNo());
    }

    /**
     * 获得移库单
     *
     * @param id 移库单ID
     * @return 移库单
     */
    @Override
    public StockMoveRespVO getStockMove(Long id) {
        // 1. 查询移库单
        StockMoveDO stockMove = validateStockMoveExists(id);

        // 2. 转换为 VO
        StockMoveRespVO respVO = StockMoveConvert.INSTANCE.convert(stockMove);

        // 3. 填充仓库名称
        if (stockMove.getWarehouseId() != null) {
            WarehouseDO warehouse = warehouseMapper.selectById(stockMove.getWarehouseId());
            if (warehouse != null) {
                respVO.setWarehouseName(warehouse.getWarehouseName());
            }
        }

        return respVO;
    }

    /**
     * 获得移库单分页
     *
     * @param pageReqVO 分页查询
     * @return 移库单分页
     */
    @Override
    public PageResult<StockMoveRespVO> getStockMovePage(StockMovePageReqVO pageReqVO) {
        // 1. 分页查询
        PageResult<StockMoveDO> pageResult = stockMoveMapper.selectPage(pageReqVO);

        // 2. 转换为 VO
        PageResult<StockMoveRespVO> voPageResult = StockMoveConvert.INSTANCE.convertPage(pageResult);

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

    /**
     * 执行移库
     *
     * @param id 移库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeStockMove(Long id) {
        // 1. 校验移库单是否存在
        StockMoveDO stockMove = validateStockMoveExists(id);

        // 2. 校验移库单状态是否允许执行（只有待执行状态可以执行）
        if (!StockMoveStatusEnum.PENDING.getStatus().equals(stockMove.getStatus())) {
            throw exception(STOCK_MOVE_NOT_ALLOW_EXECUTE);
        }

        // 3. 更新移库单状态为执行中
        StockMoveDO updateObj = new StockMoveDO();
        updateObj.setId(id);
        updateObj.setStatus(StockMoveStatusEnum.PROCESSING.getStatus());
        stockMoveMapper.updateById(updateObj);

        log.info("[移库管理] 执行移库，移库单号：{}", stockMove.getMoveNo());
    }

    /**
     * 完成移库
     *
     * @param id 移库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeStockMove(Long id) {
        // 1. 校验移库单是否存在
        StockMoveDO stockMove = validateStockMoveExists(id);

        // 2. 校验移库单状态是否允许完成（只有执行中状态可以完成）
        if (!StockMoveStatusEnum.PROCESSING.getStatus().equals(stockMove.getStatus())) {
            throw exception(STOCK_MOVE_NOT_ALLOW_COMPLETE);
        }

        // 3. 更新移库单状态为已完成，并记录操作人和操作时间
        StockMoveDO updateObj = new StockMoveDO();
        updateObj.setId(id);
        updateObj.setStatus(StockMoveStatusEnum.COMPLETED.getStatus());
        updateObj.setOperateTime(LocalDateTime.now());
        
        // 获取当前登录用户名作为操作人
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String operator = loginUser != null && loginUser.getInfo() != null 
            ? loginUser.getInfo().get(LoginUser.INFO_KEY_NICKNAME) 
            : "系统";
        updateObj.setOperator(operator);
        stockMoveMapper.updateById(updateObj);

        // 4. 更新库存（从源库位扣减，目标库位增加）
        moveInventory(stockMove);

        log.info("[移库管理] 完成移库，移库单号：{}，商品：{}，数量：{}, 操作人：{}", 
                stockMove.getMoveNo(), stockMove.getGoodsName(), stockMove.getQuantity(), operator);
    }

    /**
     * 取消移库
     *
     * @param id 移库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelStockMove(Long id) {
        // 1. 校验移库单是否存在
        StockMoveDO stockMove = validateStockMoveExists(id);

        // 2. 校验移库单状态是否允许取消（待执行和执行中状态可以取消）
        if (!StockMoveStatusEnum.PENDING.getStatus().equals(stockMove.getStatus()) 
                && !StockMoveStatusEnum.PROCESSING.getStatus().equals(stockMove.getStatus())) {
            throw exception(STOCK_MOVE_NOT_ALLOW_CANCEL);
        }

        // 3. 更新移库单状态为已取消
        StockMoveDO updateObj = new StockMoveDO();
        updateObj.setId(id);
        updateObj.setStatus(StockMoveStatusEnum.CANCELLED.getStatus());
        stockMoveMapper.updateById(updateObj);

        log.info("[移库管理] 取消移库，移库单号：{}", stockMove.getMoveNo());
    }

    /**
     * 校验移库单是否存在
     *
     * @param id 移库单ID
     * @return 移库单
     */
    private StockMoveDO validateStockMoveExists(Long id) {
        StockMoveDO stockMove = stockMoveMapper.selectById(id);
        if (stockMove == null) {
            throw exception(STOCK_MOVE_NOT_EXISTS);
        }
        return stockMove;
    }

    /**
     * 生成移库单号
     * 格式：MOVE + yyyyMMdd + 4位序列号
     *
     * @return 移库单号
     */
    private String generateMoveNo() {
        String date = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
        String sequence = String.format("%04d", IdUtil.getSnowflakeNextId() % 10000);
        return "MOVE" + date + sequence;
    }
    
    /**
     * 移库更新库存
     * 
     * 业务规则：
     * 1. 从源库位扣减库存
     * 2. 向目标库位增加库存
     * 3. 记录两条库存流水（一条扣减，一条增加）
     * 4. 使用乐观锁防止并发问题
     * 
     * @param stockMove 移库单
     */
    private void moveInventory(StockMoveDO stockMove) {
        log.info("[移库更新库存] 移库单号={}, 商品ID={}, 数量={}, 源库位ID={}, 目标库位ID={}", 
            stockMove.getMoveNo(), stockMove.getGoodsId(), stockMove.getQuantity(), 
            stockMove.getFromLocationId(), stockMove.getToLocationId());
        
        // 1. 查找源库位库存
        LambdaQueryWrapper<InventoryDO> fromQuery = new LambdaQueryWrapper<>();
        fromQuery.eq(InventoryDO::getWarehouseId, stockMove.getWarehouseId())
                .eq(InventoryDO::getLocationId, stockMove.getFromLocationId())
                .eq(InventoryDO::getGoodsId, stockMove.getGoodsId());
        
        // 批次号可能为空
        if (stockMove.getBatchNo() != null && !stockMove.getBatchNo().isEmpty()) {
            fromQuery.eq(InventoryDO::getBatchNo, stockMove.getBatchNo());
        } else {
            fromQuery.isNull(InventoryDO::getBatchNo);
        }
        
        InventoryDO fromInventory = inventoryMapper.selectOne(fromQuery);
        
        // 校验源库位库存是否存在
        if (fromInventory == null) {
            log.error("[移库失败] 源库位库存不存在: 库位ID={}, 商品ID={}", 
                stockMove.getFromLocationId(), stockMove.getGoodsId());
            throw exception(INVENTORY_NOT_EXISTS);
        }
        
        // 校验源库位库存是否足够
        if (fromInventory.getQuantity().compareTo(stockMove.getQuantity()) < 0) {
            log.error("[移库失败] 源库位库存不足: 可用数量={}, 需要数量={}", 
                fromInventory.getQuantity(), stockMove.getQuantity());
            throw exception(STOCK_MOVE_INSUFFICIENT_STOCK);
        }
        
        // 2. 扣减源库位库存
        BigDecimal fromQuantityBefore = fromInventory.getQuantity();
        BigDecimal fromQuantityAfter = fromQuantityBefore.subtract(stockMove.getQuantity());
        
        InventoryDO updateFrom = new InventoryDO();
        updateFrom.setId(fromInventory.getId());
        updateFrom.setQuantity(fromQuantityAfter);
        updateFrom.setVersion(fromInventory.getVersion());
        int fromUpdateCount = inventoryMapper.updateById(updateFrom);
        
        if (fromUpdateCount == 0) {
            log.error("[移库失败] 源库位库存更新失败（乐观锁冲突）: 库存ID={}", fromInventory.getId());
            throw exception(INVENTORY_LOCK_FAILED);
        }
        
        log.info("[移库] 源库位扣减: 库存ID={}, 原数量={}, 移库数量={}, 新数量={}", 
            fromInventory.getId(), fromQuantityBefore, stockMove.getQuantity(), fromQuantityAfter);
        
        // 记录源库位流水（扣减）
        recordMoveInventoryLog(stockMove, fromInventory.getId(), fromQuantityBefore, 
            stockMove.getQuantity().negate(), fromQuantityAfter, "FROM");
        
        // 3. 查找或创建目标库位库存
        LambdaQueryWrapper<InventoryDO> toQuery = new LambdaQueryWrapper<>();
        toQuery.eq(InventoryDO::getWarehouseId, stockMove.getWarehouseId())
                .eq(InventoryDO::getLocationId, stockMove.getToLocationId())
                .eq(InventoryDO::getGoodsId, stockMove.getGoodsId());
        
        if (stockMove.getBatchNo() != null && !stockMove.getBatchNo().isEmpty()) {
            toQuery.eq(InventoryDO::getBatchNo, stockMove.getBatchNo());
        } else {
            toQuery.isNull(InventoryDO::getBatchNo);
        }
        
        InventoryDO toInventory = inventoryMapper.selectOne(toQuery);
        
        if (toInventory != null) {
            // 目标库位库存已存在，累加数量
            BigDecimal toQuantityBefore = toInventory.getQuantity();
            BigDecimal toQuantityAfter = toQuantityBefore.add(stockMove.getQuantity());
            
            InventoryDO updateTo = new InventoryDO();
            updateTo.setId(toInventory.getId());
            updateTo.setQuantity(toQuantityAfter);
            updateTo.setVersion(toInventory.getVersion());
            int toUpdateCount = inventoryMapper.updateById(updateTo);
            
            if (toUpdateCount == 0) {
                log.error("[移库失败] 目标库位库存更新失败（乐观锁冲突）: 库存ID={}", toInventory.getId());
                throw exception(INVENTORY_LOCK_FAILED);
            }
            
            log.info("[移库] 目标库位增加: 库存ID={}, 原数量={}, 移库数量={}, 新数量={}", 
                toInventory.getId(), toQuantityBefore, stockMove.getQuantity(), toQuantityAfter);
            
            // 记录目标库位流水（增加）
            recordMoveInventoryLog(stockMove, toInventory.getId(), toQuantityBefore, 
                stockMove.getQuantity(), toQuantityAfter, "TO");
        } else {
            // 目标库位库存不存在，创建新记录
            InventoryDO newInventory = new InventoryDO();
            newInventory.setWarehouseId(stockMove.getWarehouseId());
            newInventory.setLocationId(stockMove.getToLocationId());
            newInventory.setGoodsId(stockMove.getGoodsId());
            newInventory.setBatchNo(stockMove.getBatchNo());
            newInventory.setQuantity(stockMove.getQuantity());
            newInventory.setLockQuantity(BigDecimal.ZERO);
            newInventory.setProductionDate(fromInventory.getProductionDate());
            newInventory.setExpireDate(fromInventory.getExpireDate());
            newInventory.setInboundDate(fromInventory.getInboundDate());
            newInventory.setSupplierId(fromInventory.getSupplierId());
            newInventory.setStatus(fromInventory.getStatus());
            newInventory.setVersion(0);
            inventoryMapper.insert(newInventory);
            
            log.info("[移库] 目标库位创建新库存: 库存ID={}, 库位ID={}, 商品ID={}, 数量={}", 
                newInventory.getId(), stockMove.getToLocationId(), stockMove.getGoodsId(), stockMove.getQuantity());
            
            // 记录目标库位流水（新建）
            recordMoveInventoryLog(stockMove, newInventory.getId(), BigDecimal.ZERO, 
                stockMove.getQuantity(), stockMove.getQuantity(), "TO");
        }
        
        log.info("[移库更新库存] 完成，移库单号={}", stockMove.getMoveNo());
    }
    
    /**
     * 记录移库库存流水
     * 
     * 业务说明：
     * - 移库会产生两条流水记录
     * - 源库位：扣减（变化量为负数）
     * - 目标库位：增加（变化量为正数）
     * 
     * @param stockMove 移库单
     * @param inventoryId 库存ID
     * @param quantityBefore 操作前数量
     * @param quantityChange 变化数量（负数表示扣减，正数表示增加）
     * @param quantityAfter 操作后数量
     * @param direction 方向（FROM-源库位，TO-目标库位）
     */
    private void recordMoveInventoryLog(StockMoveDO stockMove, Long inventoryId,
                                       BigDecimal quantityBefore, BigDecimal quantityChange, 
                                       BigDecimal quantityAfter, String direction) {
        InventoryLogDO log = new InventoryLogDO();
        log.setWarehouseId(stockMove.getWarehouseId());
        log.setGoodsId(stockMove.getGoodsId());
        log.setLocationId("FROM".equals(direction) ? stockMove.getFromLocationId() : stockMove.getToLocationId());
        log.setBatchNo(stockMove.getBatchNo());
        log.setOperationType("MOVE");  // 移库操作
        log.setQuantityBefore(quantityBefore);
        log.setQuantityChange(quantityChange);
        log.setQuantityAfter(quantityAfter);
        log.setBusinessType("STOCK_MOVE");  // 业务类型：库位移库
        log.setBusinessNo(stockMove.getMoveNo());  // 业务单号：移库单号
        
        // 获取操作人信息
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        String operator = loginUser != null && loginUser.getInfo() != null 
            ? loginUser.getInfo().get(LoginUser.INFO_KEY_NICKNAME) 
            : "系统";
        log.setOperator(operator);
        
        log.setRemark("FROM".equals(direction) ? "移库-源库位扣减" : "移库-目标库位增加");
        
        inventoryLogMapper.insert(log);
        
        StockMoveServiceImpl.log.info("[记录移库流水] 移库单号={}, 方向={}, 库存ID={}, 操作前={}, 变化量={}, 操作后={}", 
            stockMove.getMoveNo(), direction, inventoryId, quantityBefore, quantityChange, quantityAfter);
    }

}

