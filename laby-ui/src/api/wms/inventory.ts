/**
 * 库存管理 API
 * 
 * @author laby
 * @description 提供库存信息的增删改查功能
 */
import request from '@/config/axios'

/**
 * 库存信息 VO
 * 用于库存信息的数据传输
 */
export interface InventoryVO {
  id?: number // 库存ID（编辑时必传）
  warehouseId: number // 仓库ID
  warehouseName?: string // 仓库名称（关联查询字段）
  areaId?: number // 库区ID
  areaName?: string // 库区名称（关联查询字段）
  locationId: number // 库位ID
  locationCode?: string // 库位编码（关联查询字段）
  rowNo?: number // 排号（关联查询字段）
  columnNo?: number // 列号（关联查询字段）
  layerNo?: number // 层号（关联查询字段）
  goodsId: number // 商品ID
  goodsName?: string // 商品名称（关联查询字段）
  skuCode?: string // SKU编码（关联查询字段）
  batchNo?: string // 批次号
  serialNo?: string // 序列号
  quantity: number // 库存数量
  lockQuantity?: number // 锁定数量
  availableQuantity?: number // 可用数量（库存数量-锁定数量，计算字段）
  status: number // 状态：0-已下架，1-正常，2-已锁定，3-待检验等
  remark?: string // 备注说明
  createTime?: Date // 创建时间
}

/**
 * 库存分页查询 VO
 * 用于库存列表的搜索和分页
 */
export interface InventoryPageReqVO extends PageParam {
  warehouseId?: number // 仓库ID
  locationId?: number // 库位ID
  goodsName?: string // 商品名称（模糊搜索）
  skuCode?: string // SKU编码（模糊搜索）
  batchNo?: string // 批次号（模糊搜索）
  serialNo?: string // 序列号（精确搜索）
  status?: number // 状态
  createTime?: Date[] // 创建时间范围
}

/**
 * 创建库存
 * 
 * @param data 库存信息
 * @returns 返回创建的库存ID
 */
export const createInventory = (data: InventoryVO) => {
  return request.post({ url: '/wms/inventory/create', data })
}

/**
 * 更新库存
 * 
 * @param data 库存信息（必须包含id）
 * @returns 返回是否成功
 */
export const updateInventory = (data: InventoryVO) => {
  return request.put({ url: '/wms/inventory/update', data })
}

/**
 * 删除库存
 * 
 * @param id 库存ID
 * @returns 返回是否成功
 */
export const deleteInventory = (id: number) => {
  return request.delete({ url: '/wms/inventory/delete?id=' + id })
}

/**
 * 获取库存详情
 * 后端会自动关联仓库、库位、商品等信息
 * 
 * @param id 库存ID
 * @returns 返回库存详细信息
 */
export const getInventory = (id: number) => {
  return request.get({ url: '/wms/inventory/get?id=' + id })
}

/**
 * 获取库存分页列表
 * 后端会自动关联仓库、库位、商品等信息
 * 
 * @param params 分页查询参数
 * @returns 返回分页数据
 */
export const getInventoryPage = (params: InventoryPageReqVO) => {
  return request.get({ url: '/wms/inventory/page', params })
}

/**
 * 导出库存 Excel
 * 
 * @param params 查询参数
 * @returns 返回Excel文件
 */
export const exportInventory = (params: InventoryPageReqVO) => {
  return request.download({ url: '/wms/inventory/export-excel', params })
}

