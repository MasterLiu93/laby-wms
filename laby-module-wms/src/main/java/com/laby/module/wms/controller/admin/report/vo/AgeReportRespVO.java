package com.laby.module.wms.controller.admin.report.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "管理后台 - 库存库龄报表 Response VO")
@Data
public class AgeReportRespVO {

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

    @Schema(description = "库位编码", example = "A-01-01")
    @ExcelProperty("库位编码")
    private String locationCode;

    @Schema(description = "批次号", example = "B20240101")
    @ExcelProperty("批次号")
    private String batchNo;

    @Schema(description = "数量", example = "100.00")
    @ExcelProperty("数量")
    private BigDecimal quantity;

    @Schema(description = "入库日期", example = "2024-01-01")
    @ExcelProperty("入库日期")
    private LocalDate inboundDate;

    @Schema(description = "库龄（天）", example = "30")
    @ExcelProperty("库龄（天）")
    private Integer age;

    @Schema(description = "库龄等级", example = "FRESH")
    @ExcelProperty("库龄等级")
    private String ageLevel;

    @Schema(description = "备注", example = "正常库存")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "汇总数据")
    private Map<String, Object> summary;

}
