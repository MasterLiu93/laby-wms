package com.laby.module.wms.service.report;

import com.laby.framework.common.pojo.PageResult;
import com.laby.module.wms.controller.admin.report.vo.AgeReportPageReqVO;
import com.laby.module.wms.controller.admin.report.vo.AgeReportRespVO;

import java.util.List;

/**
 * 库存库龄报表 Service 接口
 *
 * @author laby
 */
public interface AgeReportService {

    /**
     * 获得库存库龄报表分页
     *
     * @param pageVO 分页查询
     * @return 库存库龄报表分页
     */
    PageResult<AgeReportRespVO> getAgeReportPage(AgeReportPageReqVO pageVO);

    /**
     * 获得库存库龄报表列表（用于导出）
     *
     * @param exportReqVO 查询条件
     * @return 库存库龄报表列表
     */
    List<AgeReportRespVO> getAgeReportList(AgeReportPageReqVO exportReqVO);

}
