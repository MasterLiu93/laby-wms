package com.laby.module.wms.controller.admin.report;

import com.laby.framework.common.pojo.CommonResult;
import com.laby.framework.common.pojo.PageResult;
import com.laby.framework.excel.core.util.ExcelUtils;
import com.laby.module.wms.controller.admin.report.vo.TurnoverReportPageReqVO;
import com.laby.module.wms.controller.admin.report.vo.TurnoverReportRespVO;
import com.laby.module.wms.service.report.TurnoverReportService;
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
 * 库存周转率报表 Controller
 *
 * @author laby
 */
@Tag(name = "管理后台 - 库存周转率报表")
@RestController
@RequestMapping("/wms/report/turnover")
@Validated
public class TurnoverReportController {

    @Resource
    private TurnoverReportService turnoverReportService;

    @GetMapping("/page")
    @Operation(summary = "获得库存周转率报表分页")
    @PreAuthorize("@ss.hasPermission('wms:report:query')")
    public CommonResult<PageResult<TurnoverReportRespVO>> getTurnoverReportPage(@Valid TurnoverReportPageReqVO pageVO) {
        PageResult<TurnoverReportRespVO> pageResult = turnoverReportService.getTurnoverReportPage(pageVO);
        return success(pageResult);
    }

    @GetMapping("/export")
    @Operation(summary = "导出库存周转率报表 Excel")
    @PreAuthorize("@ss.hasPermission('wms:report:export')")
    public void exportTurnoverReport(@Valid TurnoverReportPageReqVO exportReqVO,
                                      HttpServletResponse response) throws IOException {
        List<TurnoverReportRespVO> list = turnoverReportService.getTurnoverReportList(exportReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "库存周转率报表.xls", "数据", TurnoverReportRespVO.class, list);
    }

}
