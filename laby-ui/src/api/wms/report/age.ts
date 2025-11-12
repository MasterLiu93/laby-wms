import request from '@/config/axios'

export interface AgeReportVO {
  goodsCode: string
  goodsName: string
  warehouseId: number
  warehouseName: string
  locationCode: string
  batchNo: string
  quantity: number
  inboundDate: string
  age: number
  ageLevel: string
  remark: string
}

// 查询库龄报表分页
export const getAgeReportPage = (params: any) => {
  return request.get({ url: '/wms/report/age/page', params })
}

// 导出库龄报表
export const exportAgeReport = (params: any) => {
  return request.download({ url: '/wms/report/age/export', params })
}
