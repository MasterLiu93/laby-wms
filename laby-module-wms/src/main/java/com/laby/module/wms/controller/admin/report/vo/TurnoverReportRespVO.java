package com.laby.module.wms.controller.admin.report.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "管理后台 - 库存周转率报表 Response VO")
@Data
public class TurnoverReportRespVO {

    @Schema(description = "商品编码", example = "G001")
    @ExcelProperty("商品编码")
    private String goodsCode;

    @Schema(description = "商品名称", example = "iPhone 14")
    @ExcelProperty("商品名称")
    private String goodsName;

    @Schema(description = "仓库ID", example = "1")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "主仓库")
    @ExcelProperty("仓库名称")
    private String warehouseName;

    @Schema(description = "平均库存", example = "100.00")
    @ExcelProperty("平均库存")
    private BigDecimal avgInventory;

    @Schema(description = "出库数量", example = "500.00")
    @ExcelProperty("出库数量")
    private BigDecimal outboundQuantity;

    @Schema(description = "周转率", example = "5.00")
    @ExcelProperty("周转率")
    private BigDecimal turnoverRate;

    @Schema(description = "周转天数", example = "6")
    @ExcelProperty("周转天数")
    private Integer turnoverDays;

    @Schema(description = "周转等级", example = "FAST")
    @ExcelProperty("周转等级")
    private String turnoverLevel;

    @Schema(description = "开始日期", example = "2024-01-01")
    @ExcelProperty("开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2024-01-31")
    @ExcelProperty("结束日期")
    private LocalDate endDate;

    @Schema(description = "汇总数据")
    private Map<String, Object> summary;

}
