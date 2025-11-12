package com.laby.module.wms.controller.admin.report;

import com.laby.framework.common.pojo.CommonResult;
import com.laby.framework.common.pojo.PageResult;
import com.laby.framework.excel.core.util.ExcelUtils;
import com.laby.module.wms.controller.admin.report.vo.AgeReportPageReqVO;
import com.laby.module.wms.controller.admin.report.vo.AgeReportRespVO;
import com.laby.module.wms.service.report.AgeReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.laby.framework.common.pojo.CommonResult.success;

/**
 * 库存库龄报表 Controller
 *
 * @author laby
 */
@Tag(name = "管理后台 - 库存库龄报表")
@RestController
@RequestMapping("/wms/report/age")
@Validated
public class AgeReportController {

    @Resource
    private AgeReportService ageReportService;

    @GetMapping("/page")
    @Operation(summary = "获得库存库龄报表分页")
    @PreAuthorize("@ss.hasPermission('wms:report:query')")
    public CommonResult<PageResult<AgeReportRespVO>> getAgeReportPage(@Valid AgeReportPageReqVO pageVO) {
        PageResult<AgeReportRespVO> pageResult = ageReportService.getAgeReportPage(pageVO);
        return success(pageResult);
    }

    @GetMapping("/export")
    @Operation(summary = "导出库存库龄报表 Excel")
    @PreAuthorize("@ss.hasPermission('wms:report:export')")
    public void exportAgeReport(@Valid AgeReportPageReqVO exportReqVO,
                                HttpServletResponse response) throws IOException {
        List<AgeReportRespVO> list = ageReportService.getAgeReportList(exportReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "库存库龄报表.xls", "数据", AgeReportRespVO.class, list);
    }

}
