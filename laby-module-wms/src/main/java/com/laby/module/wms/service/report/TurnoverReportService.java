package com.laby.module.wms.service.report;

import com.laby.framework.common.pojo.PageResult;
import com.laby.module.wms.controller.admin.report.vo.TurnoverReportPageReqVO;
import com.laby.module.wms.controller.admin.report.vo.TurnoverReportRespVO;

import java.util.List;

/**
 * 库存周转率报表 Service 接口
 *
 * @author laby
 */
public interface TurnoverReportService {

    /**
     * 获得库存周转率报表分页
     *
     * @param pageVO 分页查询
     * @return 库存周转率报表分页
     */
    PageResult<TurnoverReportRespVO> getTurnoverReportPage(TurnoverReportPageReqVO pageVO);

    /**
     * 获得库存周转率报表列表（用于导出）
     *
     * @param exportReqVO 查询条件
     * @return 库存周转率报表列表
     */
    List<TurnoverReportRespVO> getTurnoverReportList(TurnoverReportPageReqVO exportReqVO);

}
