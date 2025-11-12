import request from '@/config/axios'

export interface TurnoverReportVO {
  goodsCode: string
  goodsName: string
  warehouseId: number
  warehouseName: string
  avgInventory: number
  outboundQuantity: number
  turnoverRate: number
  turnoverDays: number
  turnoverLevel: string
  startDate: string
  endDate: string
}

// 查询周转率报表分页
export const getTurnoverReportPage = (params: any) => {
  return request.get({ url: '/wms/report/turnover/page', params })
}

// 导出周转率报表
export const exportTurnoverReport = (params: any) => {
  return request.download({ url: '/wms/report/turnover/export', params })
}
