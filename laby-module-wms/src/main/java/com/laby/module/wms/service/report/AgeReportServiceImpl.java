package com.laby.module.wms.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.laby.framework.common.pojo.PageResult;
import com.laby.module.wms.controller.admin.report.vo.AgeReportPageReqVO;
import com.laby.module.wms.controller.admin.report.vo.AgeReportRespVO;
import com.laby.module.wms.dal.dataobject.goods.GoodsDO;
import com.laby.module.wms.dal.dataobject.inventory.InventoryDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseLocationDO;
import com.laby.module.wms.dal.dataobject.warehouse.WarehouseDO;
import com.laby.module.wms.dal.mysql.goods.GoodsMapper;
import com.laby.module.wms.dal.mysql.inventory.InventoryMapper;
import com.laby.module.wms.dal.mysql.warehouse.WarehouseLocationMapper;
import com.laby.module.wms.dal.mysql.warehouse.WarehouseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 库存库龄报表 Service 实现类
 *
 * @author laby
 */
@Service
@Slf4j
public class AgeReportServiceImpl implements AgeReportService {

    @Resource
    private InventoryMapper inventoryMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private WarehouseMapper warehouseMapper;

    @Resource
    private WarehouseLocationMapper warehouseLocationMapper;

    @Override
    public PageResult<AgeReportRespVO> getAgeReportPage(AgeReportPageReqVO pageVO) {
        // 获取数据列表
        List<AgeReportRespVO> list = getAgeReportList(pageVO);
        
        // 计算汇总数据
        Map<String, Object> summary = calculateSummary(list);
        
        // 分页处理
        int start = (pageVO.getPageNo() - 1) * pageVO.getPageSize();
        int end = Math.min(start + pageVO.getPageSize(), list.size());
        List<AgeReportRespVO> pageList = list.subList(start, end);
        
        // 设置汇总数据到每条记录
        pageList.forEach(item -> item.setSummary(summary));
        
        return new PageResult<>(pageList, (long) list.size());
    }

    @Override
    public List<AgeReportRespVO> getAgeReportList(AgeReportPageReqVO exportReqVO) {
        // 查询库存数据
        List<InventoryDO> inventoryList = inventoryMapper.selectList();
        
        List<AgeReportRespVO> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (InventoryDO inventory : inventoryList) {
            // 仓库筛选
            if (exportReqVO.getWarehouseId() != null 
                    && !inventory.getWarehouseId().equals(exportReqVO.getWarehouseId())) {
                continue;
            }

            // 获取商品信息
            GoodsDO goods = goodsMapper.selectById(inventory.getGoodsId());
            if (goods == null) {
                continue;
            }

            // 商品名称筛选
            if (StrUtil.isNotBlank(exportReqVO.getGoodsName()) 
                    && !goods.getGoodsName().contains(exportReqVO.getGoodsName())) {
                continue;
            }

            // 获取仓库信息
            WarehouseDO warehouse = warehouseMapper.selectById(inventory.getWarehouseId());
            if (warehouse == null) {
                continue;
            }

            // 获取库位信息
            WarehouseLocationDO location = null;
            if (inventory.getLocationId() != null) {
                location = warehouseLocationMapper.selectById(inventory.getLocationId());
            }

            // 计算库龄（使用创建时间作为入库日期）
            LocalDate inboundDate = inventory.getCreateTime().toLocalDate();
            long age = ChronoUnit.DAYS.between(inboundDate, now);

            // 库龄范围筛选
            if (StrUtil.isNotBlank(exportReqVO.getAgeRange())) {
                if (!matchAgeRange(age, exportReqVO.getAgeRange())) {
                    continue;
                }
            }

            // 判断库龄等级
            String ageLevel = getAgeLevel((int) age);

            // 构建响应对象
            AgeReportRespVO vo = new AgeReportRespVO();
            vo.setGoodsCode(goods.getSkuCode());
            vo.setGoodsName(goods.getGoodsName());
            vo.setWarehouseId(warehouse.getId());
            vo.setWarehouseName(warehouse.getWarehouseName());
            vo.setLocationCode(location != null ? location.getLocationCode() : "");
            vo.setBatchNo(inventory.getBatchNo() != null ? inventory.getBatchNo() : "");
            vo.setQuantity(inventory.getQuantity());
            vo.setInboundDate(inboundDate);
            vo.setAge((int) age);
            vo.setAgeLevel(ageLevel);
            vo.setRemark("");

            result.add(vo);
        }

        // 按库龄降序排序
        result.sort((a, b) -> b.getAge().compareTo(a.getAge()));

        return result;
    }

    /**
     * 判断库龄是否在指定范围内
     */
    private boolean matchAgeRange(long age, String ageRange) {
        switch (ageRange) {
            case "0-30":
                return age >= 0 && age <= 30;
            case "31-60":
                return age >= 31 && age <= 60;
            case "61-90":
                return age >= 61 && age <= 90;
            case "91-180":
                return age >= 91 && age <= 180;
            case "180+":
                return age > 180;
            default:
                return true;
        }
    }

    /**
     * 获取库龄等级
     */
    private String getAgeLevel(int age) {
        if (age <= 30) {
            return "FRESH";
        } else if (age <= 60) {
            return "NORMAL";
        } else if (age <= 90) {
            return "AGING";
        } else {
            return "OLD";
        }
    }

    /**
     * 计算汇总数据
     */
    private Map<String, Object> calculateSummary(List<AgeReportRespVO> list) {
        Map<String, Object> summary = new HashMap<>();
        
        if (CollUtil.isEmpty(list)) {
            summary.put("age0to30", 0);
            summary.put("age0to30Percent", 0);
            summary.put("age31to60", 0);
            summary.put("age31to60Percent", 0);
            summary.put("age61to90", 0);
            summary.put("age61to90Percent", 0);
            summary.put("age91to180", 0);
            summary.put("age91to180Percent", 0);
            summary.put("age180Plus", 0);
            summary.put("age180PlusPercent", 0);
            summary.put("avgAge", 0);
            return summary;
        }

        // 统计各库龄段数量
        BigDecimal age0to30 = list.stream()
                .filter(item -> item.getAge() <= 30)
                .map(AgeReportRespVO::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal age31to60 = list.stream()
                .filter(item -> item.getAge() > 30 && item.getAge() <= 60)
                .map(AgeReportRespVO::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal age61to90 = list.stream()
                .filter(item -> item.getAge() > 60 && item.getAge() <= 90)
                .map(AgeReportRespVO::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal age91to180 = list.stream()
                .filter(item -> item.getAge() > 90 && item.getAge() <= 180)
                .map(AgeReportRespVO::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal age180Plus = list.stream()
                .filter(item -> item.getAge() > 180)
                .map(AgeReportRespVO::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算总数量
        BigDecimal totalQuantity = age0to30.add(age31to60).add(age61to90).add(age91to180).add(age180Plus);

        // 计算占比
        BigDecimal age0to30Percent = totalQuantity.compareTo(BigDecimal.ZERO) > 0 
                ? age0to30.divide(totalQuantity, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) 
                : BigDecimal.ZERO;
        BigDecimal age31to60Percent = totalQuantity.compareTo(BigDecimal.ZERO) > 0 
                ? age31to60.divide(totalQuantity, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) 
                : BigDecimal.ZERO;
        BigDecimal age61to90Percent = totalQuantity.compareTo(BigDecimal.ZERO) > 0 
                ? age61to90.divide(totalQuantity, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) 
                : BigDecimal.ZERO;
        BigDecimal age91to180Percent = totalQuantity.compareTo(BigDecimal.ZERO) > 0 
                ? age91to180.divide(totalQuantity, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) 
                : BigDecimal.ZERO;
        BigDecimal age180PlusPercent = totalQuantity.compareTo(BigDecimal.ZERO) > 0 
                ? age180Plus.divide(totalQuantity, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) 
                : BigDecimal.ZERO;

        // 计算平均库龄
        int totalAge = list.stream().mapToInt(AgeReportRespVO::getAge).sum();
        int avgAge = list.size() > 0 ? totalAge / list.size() : 0;

        summary.put("age0to30", age0to30);
        summary.put("age0to30Percent", age0to30Percent.setScale(2, RoundingMode.HALF_UP));
        summary.put("age31to60", age31to60);
        summary.put("age31to60Percent", age31to60Percent.setScale(2, RoundingMode.HALF_UP));
        summary.put("age61to90", age61to90);
        summary.put("age61to90Percent", age61to90Percent.setScale(2, RoundingMode.HALF_UP));
        summary.put("age91to180", age91to180);
        summary.put("age91to180Percent", age91to180Percent.setScale(2, RoundingMode.HALF_UP));
        summary.put("age180Plus", age180Plus);
        summary.put("age180PlusPercent", age180PlusPercent.setScale(2, RoundingMode.HALF_UP));
        summary.put("avgAge", avgAge);

        return summary;
    }

}
