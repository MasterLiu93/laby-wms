package com.laby.module.wms.service.inventory;

import cn.hutool.core.collection.CollUtil;
import com.laby.framework.common.util.collection.CollectionUtils;
import com.laby.module.wms.controller.admin.inventory.vo.warning.InventoryWarningRespVO;
import com.laby.module.wms.dal.dataobject.goods.GoodsDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseDO;
import com.laby.module.wms.dal.mysql.inventory.InventoryMapper;
import com.laby.module.wms.service.goods.GoodsService;
import com.laby.module.wms.service.warehouse.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 库存预警 Service 实现类
 * 
 * 功能说明：
 * - 实现库存预警功能
 * - 检测低库存和即将过期商品
 * - 提供预警数据查询
 *
 * @author laby
 */
@Service
@Validated
@Slf4j
public class InventoryWarningServiceImpl implements InventoryWarningService {

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private GoodsService goodsService;

    /**
     * 获取低库存预警列表
     * 
     * 实现步骤：
     * 1. 查询所有库存数据
     * 2. 批量获取商品信息（含安全库存）
     * 3. 过滤低库存商品（可用数量 < 安全库存）
     * 4. 填充关联字段
     * 
     * @return 低库存预警列表
     */
    @Override
    public List<InventoryWarningRespVO> getLowStockWarnings() {
        log.info("[库存预警] 开始查询低库存预警");
        
        // 1. 查询所有库存
        List<InventoryDO> inventoryList = inventoryMapper.selectList();
        if (CollUtil.isEmpty(inventoryList)) {
            log.info("[库存预警] 没有库存数据");
            return List.of();
        }
        log.info("[库存预警] 查询到{}条库存记录", inventoryList.size());

        // 2. 按仓库和商品分组汇总
        Map<String, BigDecimal> quantityMap = new java.util.HashMap<>();
        Map<String, BigDecimal> lockQuantityMap = new java.util.HashMap<>();
        Map<String, Long> warehouseIdMap = new java.util.HashMap<>();
        Map<String, Long> goodsIdMap = new java.util.HashMap<>();
        
        for (InventoryDO inventory : inventoryList) {
            String key = inventory.getWarehouseId() + "_" + inventory.getGoodsId();
            BigDecimal currentQty = quantityMap.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal currentLockQty = lockQuantityMap.getOrDefault(key, BigDecimal.ZERO);
            
            BigDecimal qty = inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO;
            BigDecimal lockQty = inventory.getLockQuantity() != null ? inventory.getLockQuantity() : BigDecimal.ZERO;
            
            quantityMap.put(key, currentQty.add(qty));
            lockQuantityMap.put(key, currentLockQty.add(lockQty));
            warehouseIdMap.put(key, inventory.getWarehouseId());
            goodsIdMap.put(key, inventory.getGoodsId());
        }
        log.info("[库存预警] 汇总后共{}个仓库-商品组合", quantityMap.size());

        // 3. 获取商品和仓库信息
        List<Long> goodsIds = new ArrayList<>(new java.util.HashSet<>(goodsIdMap.values()));
        List<Long> warehouseIds = new ArrayList<>(new java.util.HashSet<>(warehouseIdMap.values()));
        
        Map<Long, GoodsDO> goodsMap = goodsService.getGoodsMap(goodsIds);
        Map<Long, WarehouseDO> warehouseMap = warehouseService.getWarehouseMap(warehouseIds);
        log.info("[库存预警] 查询到{}个商品，{}个仓库", goodsMap.size(), warehouseMap.size());

        // 4. 检查低库存
        List<InventoryWarningRespVO> warnings = new ArrayList<>();
        
        for (Map.Entry<String, BigDecimal> entry : quantityMap.entrySet()) {
            String key = entry.getKey();
            Long warehouseId = warehouseIdMap.get(key);
            Long goodsId = goodsIdMap.get(key);
            
            GoodsDO goods = goodsMap.get(goodsId);
            if (goods == null) {
                log.warn("[库存预警] 商品{}不存在", goodsId);
                continue;
            }
            
            // 只检查设置了安全库存的商品（安全库存 > 0）
            if (goods.getSafetyStock() == null || goods.getSafetyStock().compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("[库存预警] 商品{}({})未设置安全库存，跳过", goods.getGoodsName(), goods.getSkuCode());
                continue;
            }
            
            BigDecimal totalQty = entry.getValue();
            BigDecimal totalLockQty = lockQuantityMap.get(key);
            BigDecimal availableQty = totalQty.subtract(totalLockQty != null ? totalLockQty : BigDecimal.ZERO);
            
            log.debug("[库存预警] 商品{}({}) - 仓库{} - 总量:{} 锁定:{} 可用:{} 安全库存:{}", 
                goods.getGoodsName(), goods.getSkuCode(), warehouseId,
                totalQty, totalLockQty, availableQty, goods.getSafetyStock());
            
            // 可用数量 < 安全库存
            if (availableQty.compareTo(goods.getSafetyStock()) < 0) {
                InventoryWarningRespVO warning = new InventoryWarningRespVO();
                warning.setWarehouseId(warehouseId);
                warning.setGoodsId(goodsId);
                warning.setWarningType("LOW_STOCK");
                warning.setQuantity(totalQty);
                warning.setLockQuantity(totalLockQty != null ? totalLockQty : BigDecimal.ZERO);
                warning.setAvailableQuantity(availableQty);
                warning.setSafetyStock(goods.getSafetyStock());
                
                // 填充关联字段
                WarehouseDO warehouse = warehouseMap.get(warehouseId);
                if (warehouse != null) {
                    warning.setWarehouseName(warehouse.getWarehouseName());
                }
                warning.setGoodsName(goods.getGoodsName());
                warning.setSkuCode(goods.getSkuCode());
                
                warnings.add(warning);
                log.info("[库存预警] 发现低库存预警 - 商品:{}({}) 仓库:{} 可用:{} < 安全:{}",
                    goods.getGoodsName(), goods.getSkuCode(), 
                    warehouse != null ? warehouse.getWarehouseName() : warehouseId,
                    availableQty, goods.getSafetyStock());
            }
        }
        
        log.info("[库存预警] 共发现{}条低库存预警", warnings.size());
        return warnings;
    }

    /**
     * 获取即将过期预警列表
     * 
     * 实现步骤：
     * 1. 查询有过期日期的库存
     * 2. 过滤距离过期日期 <= 7天的商品
     * 3. 填充关联字段
     * 
     * @return 即将过期预警列表
     */
    @Override
    public List<InventoryWarningRespVO> getExpiringWarnings() {
        // 1. 查询所有库存
        List<InventoryDO> inventoryList = inventoryMapper.selectList();
        if (CollUtil.isEmpty(inventoryList)) {
            return List.of();
        }

        // 2. 获取关联信息
        List<Long> goodsIds = CollectionUtils.convertList(inventoryList, InventoryDO::getGoodsId);
        List<Long> warehouseIds = CollectionUtils.convertList(inventoryList, InventoryDO::getWarehouseId);
        
        Map<Long, GoodsDO> goodsMap = goodsService.getGoodsMap(goodsIds);
        Map<Long, WarehouseDO> warehouseMap = warehouseService.getWarehouseMap(warehouseIds);

        // 3. 检查即将过期
        List<InventoryWarningRespVO> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (InventoryDO inventory : inventoryList) {
            if (inventory.getExpireDate() == null) {
                continue;
            }
            
            // 计算距离过期天数
            long daysToExpire = ChronoUnit.DAYS.between(today, inventory.getExpireDate());
            
            // 距离过期 <= 7天且未过期
            if (daysToExpire >= 0 && daysToExpire <= 7) {
                InventoryWarningRespVO warning = new InventoryWarningRespVO();
                warning.setWarehouseId(inventory.getWarehouseId());
                warning.setGoodsId(inventory.getGoodsId());
                warning.setBatchNo(inventory.getBatchNo());
                warning.setWarningType("EXPIRING");
                warning.setQuantity(inventory.getQuantity());
                warning.setLockQuantity(inventory.getLockQuantity());
                warning.setAvailableQuantity(
                    (inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO)
                        .subtract(inventory.getLockQuantity() != null ? inventory.getLockQuantity() : BigDecimal.ZERO)
                );
                warning.setExpireDate(inventory.getExpireDate());
                warning.setDaysToExpire((int) daysToExpire);
                
                // 填充关联字段
                GoodsDO goods = goodsMap.get(inventory.getGoodsId());
                if (goods != null) {
                    warning.setGoodsName(goods.getGoodsName());
                    warning.setSkuCode(goods.getSkuCode());
                }
                
                WarehouseDO warehouse = warehouseMap.get(inventory.getWarehouseId());
                if (warehouse != null) {
                    warning.setWarehouseName(warehouse.getWarehouseName());
                }
                
                warnings.add(warning);
            }
        }
        
        return warnings;
    }

    /**
     * 获取所有预警列表
     * 
     * 实现步骤：
     * 1. 获取低库存预警
     * 2. 获取即将过期预警
     * 3. 合并结果
     * 
     * @return 所有预警列表
     */
    @Override
    public List<InventoryWarningRespVO> getAllWarnings() {
        List<InventoryWarningRespVO> allWarnings = new ArrayList<>();
        allWarnings.addAll(getLowStockWarnings());
        allWarnings.addAll(getExpiringWarnings());
        return allWarnings;
    }

}

