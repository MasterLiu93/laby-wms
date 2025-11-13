/**
 * 商品分类 API
 * 
 * @author laby
 * @description 提供商品分类的增删改查功能，支持树形结构
 */
import request from '@/config/axios'

// 商品分类状态常量
export const CATEGORY_STATUS = {
  ENABLE: 1,  // 启用
  DISABLE: 0  // 禁用
}

/**
 * 商品分类 VO
 * 用于商品分类的数据传输
 * 
 * 说明：
 * - categoryCode 由后端自动生成，新增时不需要传
 * - level 由后端根据父分类自动计算，不需要传
 */
export interface GoodsCategoryVO {
  id?: number // 分类ID（编辑时必传）
  categoryCode?: string // 分类编码（后端自动生成，前端不需要传）
  categoryName: string // 分类名称，如：电子产品
  parentId?: number // 父分类ID，0表示顶级分类
  level?: number // 分类层级（后端自动计算，前端不需要传）
  sort?: number // 排序号，数字越小越靠前
  status?: number // 状态：1-启用，0-禁用
  createTime?: Date // 创建时间
}

/**
 * 商品分类查询条件 VO
 * 用于分类列表的搜索和过滤
 */
export interface GoodsCategoryListReqVO {
  categoryCode?: string // 分类编码（模糊搜索）
  categoryName?: string // 分类名称（模糊搜索）
  status?: number // 状态：0-禁用，1-启用
  createTime?: Date[] // 创建时间范围
}

/**
 * 创建商品分类
 * 
 * @param data 分类信息
 * @returns 返回创建的分类ID
 */
export const createGoodsCategory = (data: GoodsCategoryVO) => {
  return request.post({ url: '/wms/goods-category/create', data })
}

/**
 * 更新商品分类
 * 
 * @param data 分类信息（必须包含id）
 * @returns 返回是否成功
 */
export const updateGoodsCategory = (data: GoodsCategoryVO) => {
  return request.put({ url: '/wms/goods-category/update', data })
}

/**
 * 删除商品分类
 * 
 * @param id 分类ID
 * @returns 返回是否成功
 */
export const deleteGoodsCategory = (id: number) => {
  return request.delete({ url: '/wms/goods-category/delete?id=' + id })
}

/**
 * 获取商品分类详情
 * 
 * @param id 分类ID
 * @returns 返回分类详细信息
 */
export const getGoodsCategory = (id: number) => {
  return request.get({ url: '/wms/goods-category/get?id=' + id })
}

/**
 * 获取商品分类列表（树形表格）
 * 不分页，返回所有符合条件的分类（扁平结构）
 * 
 * @param params 查询参数
 * @returns 返回分类列表（扁平结构，前端构建树）
 */
export const getGoodsCategoryList = (params?: GoodsCategoryListReqVO) => {
  return request.get({ url: '/wms/goods-category/list', params })
}

/**
 * 获取商品分类简单列表（用于下拉框）
 * 返回所有启用的分类，不分页
 * 
 * @returns 返回分类列表
 */
export const getGoodsCategorySimpleList = () => {
  return request.get({ url: '/wms/goods-category/simple-list' })
}

/**
 * 获取商品分类树（用于级联选择器）
 * 返回树形结构的分类数据
 * 
 * @returns 返回分类树
 */
export const getGoodsCategoryTree = async () => {
  const list = await request.get({ url: '/wms/goods-category/list' })
  return buildTree(list)
}

/**
 * 将扁平列表转换为树形结构
 * 
 * @param list 扁平列表
 * @param parentId 父ID（默认为0，表示顶级节点）
 * @returns 树形结构
 */
function buildTree(list: any[], parentId: number = 0): any[] {
  const tree: any[] = []
  
  list.forEach(item => {
    if (item.parentId === parentId) {
      const children = buildTree(list, item.id)
      const node = {
        id: item.id,
        categoryName: item.categoryName,
        categoryCode: item.categoryCode,
        parentId: item.parentId,
        level: item.level,
        sort: item.sort,
        status: item.status ?? CATEGORY_STATUS.ENABLE, // 默认启用状态
        createTime: item.createTime,
        label: item.categoryName,
        value: item.id,
        children: children.length > 0 ? children : undefined
      }
      tree.push(node)
    }
  })
  
  // 按sort排序
  tree.sort((a, b) => (a.sort || 0) - (b.sort || 0))
  
  return tree
}

// 更新商品分类状态
export const updateGoodsCategoryStatus = (id: number, status: number) => {
  return request.put({
    url: `/wms/goods-category/update-status`,
    params: {
      id,
      status
    }
  })
}

