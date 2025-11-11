import request from '@/config/axios'

/**
 * 首页统计数据
 */
export interface HomeStatisticsVO {
  totalInventory: number // 总库存量
  todayInbound: number // 今日入库
  todayOutbound: number // 今日出库
  inventoryWarning: number // 库存预警数
  totalInventoryTrend: number // 总库存趋势百分比
  todayInboundTrend: number // 今日入库趋势百分比
  todayOutboundTrend: number // 今日出库趋势百分比
  inventoryWarningTrend: number // 库存预警趋势百分比
}

/**
 * 货架信息
 */
export interface RackInfoVO {
  name: string // 货架名称
  rate: number // 使用率
  quantity: number // 库存数量
  area: string // 所属区域
}

/**
 * 趋势数据
 */
export interface TrendDataVO {
  dates: string[] // 日期数组
  inboundData: number[] // 入库数据
  outboundData: number[] // 出库数据
}

/**
 * 库存预警信息
 */
export interface InventoryWarningVO {
  goods: string // 商品名称
  stock: number // 当前库存
  min: number // 最低库存
  level: 'high' | 'medium' // 预警级别
}

/**
 * 实时动态
 */
export interface RecentActivityVO {
  type: string // 活动类型
  desc: string // 活动描述
  time: string // 活动时间
  color: string // 颜色标识
}

/**
 * 热门商品数据
 */
export interface PopularGoodsVO {
  names: string[] // 商品名称数组
  values: number[] // 数量数组
}

/**
 * 获取首页统计数据
 */
export const getHomeStatistics = () => {
  return request.get<HomeStatisticsVO>({ url: '/wms/home/statistics' })
}

/**
 * 获取货架信息
 */
export const getRackList = () => {
  return request.get<RackInfoVO[]>({ url: '/wms/home/racks' })
}

/**
 * 获取趋势数据（最近7天）
 */
export const getTrendData = () => {
  return request.get<TrendDataVO>({ url: '/wms/home/trend' })
}

/**
 * 获取库存预警列表
 */
export const getWarningList = () => {
  return request.get<InventoryWarningVO[]>({ url: '/wms/home/warnings' })
}

/**
 * 获取实时动态
 */
export const getRecentActivities = () => {
  return request.get<RecentActivityVO[]>({ url: '/wms/home/activities' })
}

/**
 * 获取热门商品数据
 * @param type 类型：inbound-入库TOP10, outbound-出库TOP10, inventory-库存TOP10
 */
export const getPopularGoods = (type: 'inbound' | 'outbound' | 'inventory') => {
  return request.get<PopularGoodsVO>({ url: '/wms/home/popular-goods', params: { type } })
}

