package com.laby.module.wms.controller.admin.goods.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 商品分类新增/修改 Request VO
 * 
 * 功能说明：
 * 1. 新增时，id 为空，分类编码由后端自动生成，层级由后端根据父分类自动计算
 * 2. 修改时，id 必填，分类编码不可修改，层级不可修改
 * 3. parentId 为 null 或 0 表示顶级分类
 */
@Schema(description = "管理后台 - 商品分类新增/修改 Request VO")
@Data
public class GoodsCategorySaveReqVO {

    @Schema(description = "主键ID（修改时必填）", example = "1")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "电子产品")
    @NotEmpty(message = "分类名称不能为空")
    private String categoryName;

    @Schema(description = "父分类ID（不填或填0表示顶级分类）", example = "0")
    private Long parentId;

    @Schema(description = "显示排序（数字越小越靠前）", example = "0")
    private Integer sort;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    private Integer status;

}

