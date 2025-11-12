package com.laby.module.wms.controller.admin.report.vo;

import com.laby.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 库存库龄报表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AgeReportPageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "1")
    private Long warehouseId;

    @Schema(description = "商品名称", example = "iPhone 14")
    private String goodsName;

    @Schema(description = "库龄范围", example = "0-30")
    private String ageRange;

}
