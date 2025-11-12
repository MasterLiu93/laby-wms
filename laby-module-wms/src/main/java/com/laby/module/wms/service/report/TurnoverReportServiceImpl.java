package com.laby.module.wms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.laby.framework.common.pojo.PageResult;
import com.laby.module.wms.controller.admin.report.vo.TurnoverReportPageReqVO;
import com.laby.module.wms.controller.admin.report.vo.TurnoverReportRespVO;
import com.laby.module.wms.dal.dataobject.goods.GoodsDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryDO;
import com.laby.module.wms.dal.dataobject.outbound.OutboundItemDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseDO;
import com.laby.module.wms.dal.mysql.goods.GoodsMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryMapper;
import com.laby.module.wms.dal.mysql.outbound.OutboundItemMapper;
import com.laby.module.wms.dal.mysql.outbound.OutboundMapper;
import com.laby.module.wms.dal.mysql.warehouse.WarehouseMapper;
import com.laby.module.wms.dal.dataobject.outbound.OutboundDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存周转率报表 Service 实现类
 *
 * @author laby
 */
@Service
@Slf4j
public class TurnoverReportServiceImpl implements TurnoverReportService {

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private OutboundItemMapper outboundItemMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private WarehouseMapper warehouseMapper;

    @Resource
    private OutboundMapper outboundMapper;

    @Override
    public PageResult<TurnoverReportRespVO> getTurnoverReportPage(TurnoverReportPageReqVO pageVO) {
        // 获取数据列表
        List<TurnoverReportRespVO> list = getTurnoverReportList(pageVO);
        
        // 计算汇总数据
        Map<String, Object> summary = calculateSummary(list);
        
        // 分页处理
        int start = (pageVO.getPageNo() - 1) * pageVO.getPageSize();
        int end = Math.min(start + pageVO.getPageSize(), list.size());
        List<TurnoverReportRespVO> pageList = list.subList(start, end);
        
        // 设置汇总数据到每条记录
        pageList.forEach(item -> item.setSummary(summary));
        
        return new PageResult<>(pageList, (long) list.size());
    }

    @Override
    public List<TurnoverReportRespVO> getTurnoverReportList(TurnoverReportPageReqVO exportReqVO) {
        // 设置默认日期范围（最近30天）
        LocalDate startDate = exportReqVO.getStartDate();
        LocalDate endDate = exportReqVO.getEndDate();
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // 计算日期天数
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // 查询库存数据（按商品和仓库分组）
        List<InventoryDO> inventoryList = inventoryMapper.selectList();
        
        // 按商品和仓库分组
        Map<String, List<InventoryDO>> inventoryMap = inventoryList.stream()
                .filter(inv -> exportReqVO.getWarehouseId() == null || inv.getWarehouseId().equals(exportReqVO.getWarehouseId()))
                .collect(Collectors.groupingBy(inv -> inv.getGoodsId() + "_" + inv.getWarehouseId()));

        // 查询出库数据 - 先查询出库单，然后关联明细
        List<OutboundDO> outboundList = outboundMapper.selectList();
        Map<Long, Long> outboundWarehouseMap = outboundList.stream()
                .collect(Collectors.toMap(OutboundDO::getId, OutboundDO::getWarehouseId));
        
        List<OutboundItemDO> outboundItems = outboundItemMapper.selectList();
        Map<String, BigDecimal> outboundMap = outboundItems.stream()
                .filter(item -> {
                    Long warehouseId = outboundWarehouseMap.get(item.getOutboundId());
                    return warehouseId != null && (exportReqVO.getWarehouseId() == null || warehouseId.equals(exportReqVO.getWarehouseId()));
                })
                .collect(Collectors.groupingBy(
                        item -> {
                            Long warehouseId = outboundWarehouseMap.get(item.getOutboundId());
                            return item.getGoodsId() + "_" + warehouseId;
                        },
                        Collectors.reducing(BigDecimal.ZERO, OutboundItemDO::getShippedQuantity, BigDecimal::add)
                ));

        // 构建报表数据
        List<TurnoverReportRespVO> result = new ArrayList<>();
        
        for (Map.Entry<String, List<InventoryDO>> entry : inventoryMap.entrySet()) {
            List<InventoryDO> invList = entry.getValue();
            if (CollUtil.isEmpty(invList)) {
                continue;
            }

            InventoryDO firstInv = invList.get(0);
            
            // 获取商品信息
            GoodsDO goods = goodsMapper.selectById(firstInv.getGoodsId());
            if (goods == null) {
                continue;
            }
            
            // 如果有商品名称筛选
            if (StrUtil.isNotBlank(exportReqVO.getGoodsName()) 
                    && !goods.getGoodsName().contains(exportReqVO.getGoodsName())) {
                continue;
            }

            // 获取仓库信息
            WarehouseDO warehouse = warehouseMapper.selectById(firstInv.getWarehouseId());
            if (warehouse == null) {
                continue;
            }

            // 计算平均库存
            BigDecimal totalQuantity = invList.stream()
                    .map(InventoryDO::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgInventory = totalQuantity.divide(BigDecimal.valueOf(invList.size()), 2, RoundingMode.HALF_UP);

            // 获取出库数量
            BigDecimal outboundQuantity = outboundMap.getOrDefault(entry.getKey(), BigDecimal.ZERO);

            // 计算周转率和周转天数
            BigDecimal turnoverRate = BigDecimal.ZERO;
            Integer turnoverDays = 0;
            String turnoverLevel = "STAGNANT";

            if (avgInventory.compareTo(BigDecimal.ZERO) > 0) {
                turnoverRate = outboundQuantity.divide(avgInventory, 2, RoundingMode.HALF_UP);
                if (turnoverRate.compareTo(BigDecimal.ZERO) > 0) {
                    turnoverDays = BigDecimal.valueOf(days)
                            .divide(turnoverRate, 0, RoundingMode.HALF_UP)
                            .intValue();
                }
                
                // 判断周转等级
                if (turnoverRate.compareTo(BigDecimal.valueOf(10)) >= 0) {
                    turnoverLevel = "FAST";
                } else if (turnoverRate.compareTo(BigDecimal.valueOf(5)) >= 0) {
                    turnoverLevel = "NORMAL";
                } else if (turnoverRate.compareTo(BigDecimal.valueOf(2)) >= 0) {
                    turnoverLevel = "SLOW";
                }
            }

            // 构建响应对象
            TurnoverReportRespVO vo = new TurnoverReportRespVO();
            vo.setGoodsCode(goods.getSkuCode());
            vo.setGoodsName(goods.getGoodsName());
            vo.setWarehouseId(warehouse.getId());
            vo.setWarehouseName(warehouse.getWarehouseName());
            vo.setAvgInventory(avgInventory);
            vo.setOutboundQuantity(outboundQuantity);
            vo.setTurnoverRate(turnoverRate);
            vo.setTurnoverDays(turnoverDays);
            vo.setTurnoverLevel(turnoverLevel);
            vo.setStartDate(startDate);
            vo.setEndDate(endDate);

            result.add(vo);
        }

        // 按周转率降序排序
        result.sort((a, b) -> b.getTurnoverRate().compareTo(a.getTurnoverRate()));

        return result;
    }

    /**
     * 计算汇总数据
     */
    private Map<String, Object> calculateSummary(List<TurnoverReportRespVO> list) {
        Map<String, Object> summary = new HashMap<>();
        
        if (CollUtil.isEmpty(list)) {
            summary.put("avgTurnoverRate", 0);
            summary.put("avgTurnoverDays", 0);
            summary.put("fastMovingCount", 0);
            summary.put("slowMovingCount", 0);
            return summary;
        }

        // 计算平均周转率
        BigDecimal totalTurnoverRate = list.stream()
                .map(TurnoverReportRespVO::getTurnoverRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgTurnoverRate = totalTurnoverRate.divide(
                BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);

        // 计算平均周转天数
        int totalTurnoverDays = list.stream()
                .mapToInt(TurnoverReportRespVO::getTurnoverDays)
                .sum();
        int avgTurnoverDays = totalTurnoverDays / list.size();

        // 统计快速周转商品数量
        long fastMovingCount = list.stream()
                .filter(item -> "FAST".equals(item.getTurnoverLevel()))
                .count();

        // 统计慢速周转商品数量
        long slowMovingCount = list.stream()
                .filter(item -> "SLOW".equals(item.getTurnoverLevel()) 
                        || "STAGNANT".equals(item.getTurnoverLevel()))
                .count();

        summary.put("avgTurnoverRate", avgTurnoverRate);
        summary.put("avgTurnoverDays", avgTurnoverDays);
        summary.put("fastMovingCount", fastMovingCount);
        summary.put("slowMovingCount", slowMovingCount);

        return summary;
    }

}
