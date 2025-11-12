/**
 * WMS完整系统表格列配置 - 统一管理
 * 包含全部21个WMS模块的完整列定义，支持RightToolbar列选择功能
 * 
 * 模块清单：
 * 基础管理: Warehouse Info, Area, Location, Product Category
 * 商品管理: Goods Info
 * 库存管理: Inventory Query, Inventory Log, Inventory Warning  
 * 入出库: Inbound, Outbound
 * 拣货: Picking Wave, Picking Task
 * 盘点: Stocktaking Plan, Stocktaking
 * 调拨: Stock Move
 * 合作伙伴: Supplier, Customer, Carrier
 * 报表: Inventory Report, Turnover Report, Age Report
 * 
 * @author laby
 * @date 2025-11-12
 */

interface ColumnConfig {
  visible: boolean
  label: string
}

/**
 * 创建WMS全部21个模块的列配置
 * @param t 国际化函数
 * @returns 包含所有模块完整列配置的对象
 */
export function createWMSColumns(t: Function) {
  return {
    // ==================== 基础管理模块 (4个) ====================
    
    /** 1. Warehouse Info - 仓库信息管理 */
    warehouse: {
      warehouseName: { visible: true, label: t('wms.warehouseName') },
      warehouseType: { visible: true, label: t('wms.warehouseType') },
      address: { visible: true, label: t('wms.address') },
      contactPerson: { visible: true, label: t('wms.contactPerson') },
      contactPhone: { visible: true, label: t('wms.contactPhone') },
      status: { visible: true, label: t('common.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    /** 2. Area - 区域管理 */
    area: {
      areaName: { visible: true, label: t('wms.areaName') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      areaType: { visible: true, label: t('wms.areaType') },
      floor: { visible: true, label: t('wms.floor') },
      areaSize: { visible: true, label: t('wms.areaSize') },
      status: { visible: true, label: t('common.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    /** 3. Location - 库位管理 */
    location: {
      warehouseName: { visible: true, label: t('wms.warehouse') },
      areaName: { visible: true, label: t('wms.area') },
      rowNo: { visible: true, label: t('wms.rowNo') },
      columnNo: { visible: true, label: t('wms.columnNo') },
      layerNo: { visible: true, label: t('wms.layerNo') },
      locationType: { visible: true, label: t('wms.locationType') },
      capacity: { visible: true, label: t('wms.capacity') },
      maxWeight: { visible: true, label: t('wms.maxWeight') },
      status: { visible: true, label: t('common.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    /** 4. Product Category - 商品分类管理 */
    category: {
      categoryName: { visible: true, label: t('wms.categoryName') },
      categoryCode: { visible: true, label: t('wms.categoryCode') },
      parentName: { visible: true, label: t('wms.parentCategory') },
      sort: { visible: true, label: t('common.sort') },
      status: { visible: true, label: t('common.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    // ==================== 商品管理模块 (1个) ====================
    
    /** 5. Goods Info - 商品信息管理 */
    goods: {
      skuCode: { visible: true, label: t('wms.skuCode') },
      goodsName: { visible: true, label: t('wms.goodsName') },
      category: { visible: true, label: t('wms.category') },
      brand: { visible: true, label: t('wms.brand') },
      model: { visible: true, label: t('wms.model') },
      unit: { visible: true, label: t('wms.unit') },
      spec: { visible: true, label: t('wms.spec') },
      safetyStock: { visible: true, label: t('wms.safetyStock') },
      batchSerial: { visible: true, label: t('wms.batchSerial') },
      status: { visible: true, label: t('common.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    // ==================== 库存管理模块 (3个) ====================
    
    /** 6. Inventory Query - 库存查询 */
    inventory: {
      warehouseName: { visible: true, label: t('wms.warehouse') },
      areaName: { visible: true, label: t('wms.area') },
      locationCode: { visible: true, label: t('wms.location') },
      goodsName: { visible: true, label: t('wms.goodsInfo') },
      batchNo: { visible: true, label: t('wms.batchSerial') },
      quantity: { visible: true, label: t('wms.quantity') },
      lockedQuantity: { visible: true, label: t('wms.lockQuantity') },
      availableQuantity: { visible: true, label: t('wms.availableQuantity') },
      status: { visible: true, label: t('common.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    /** 7. Inventory Log - 库存日志 */
    inventoryLog: {
      warehouseName: { visible: true, label: t('wms.warehouse') },
      goodsName: { visible: true, label: t('wms.goodsInfo') },
      locationCode: { visible: true, label: t('wms.location') },
      batchNo: { visible: true, label: t('wms.batchNo') },
      operationType: { visible: true, label: t('wms.operationType') },
      changeQuantity: { visible: true, label: t('wms.changeQuantity') },
      beforeQuantity: { visible: true, label: t('wms.beforeQuantity') },
      afterQuantity: { visible: true, label: t('wms.afterQuantity') },
      businessType: { visible: true, label: t('wms.businessType') },
      businessNo: { visible: true, label: t('wms.businessNo') },
      operator: { visible: true, label: t('wms.operator') },
      createTime: { visible: true, label: t('wms.operateTime') },
      remark: { visible: true, label: t('form.remark') }
    },

    /** 8. Inventory Warning - 库存预警 */
    inventoryWarning: {
      warningType: { visible: true, label: t('wms.warningType') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      goodsInfo: { visible: true, label: t('wms.goodsInfo') },
      batchNo: { visible: true, label: t('wms.batchNo') },
      currentQuantity: { visible: true, label: t('wms.currentQuantity') },
      availableQuantity: { visible: true, label: t('wms.availableQuantity') },
      safetyStock: { visible: true, label: t('wms.safetyStock') },
      expireInfo: { visible: true, label: t('wms.expireInfo') },
      warningDesc: { visible: true, label: t('wms.warningDesc') }
    },

    // ==================== 入出库管理模块 (2个) ====================
    
    /** 9. Inbound - 入库管理 */
    inbound: {
      inboundNo: { visible: true, label: t('wms.inboundNo') },
      inboundType: { visible: true, label: t('wms.inboundType') },
      supplierName: { visible: true, label: t('wms.supplierName') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      totalQuantity: { visible: true, label: t('wms.totalQuantity') },
      receivedQuantity: { visible: true, label: t('wms.receivedQuantity') },
      totalAmount: { visible: true, label: t('wms.totalAmount') },
      status: { visible: true, label: t('common.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    /** 10. Outbound - 出库管理 */
    outbound: {
      outboundNo: { visible: true, label: t('wms.outboundNo') },
      outboundType: { visible: true, label: t('common.type') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      customerName: { visible: true, label: t('wms.customerName') },
      totalQuantity: { visible: true, label: t('wms.totalQuantity') },
      pickedQuantity: { visible: true, label: t('wms.pickedQuantity') },
      totalAmount: { visible: true, label: t('wms.totalAmount') },
      status: { visible: true, label: t('common.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    // ==================== 拣货管理模块 (2个) ====================
    
    /** 11. Picking Wave - 拣货波次 */
    pickingWave: {
      waveNo: { visible: true, label: t('wms.waveNo') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      waveType: { visible: true, label: t('wms.waveType') },
      outboundNos: { visible: true, label: t('wms.relatedOutbounds') },
      orderCount: { visible: true, label: t('wms.orderCount') },
      totalQuantity: { visible: true, label: t('wms.totalQuantity') },
      priority: { visible: true, label: t('wms.priority') },
      pickerName: { visible: true, label: t('wms.picker') },
      status: { visible: true, label: t('wms.waveStatus') },
      startTime: { visible: true, label: t('wms.startTime') },
      endTime: { visible: true, label: t('wms.endTime') },
      remark: { visible: true, label: t('form.remark') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    /** 12. Picking Task - 拣货任务 */
    pickingTask: {
      taskNo: { visible: true, label: t('wms.taskNo') },
      waveNo: { visible: true, label: t('wms.waveNo') },
      goodsName: { visible: true, label: t('wms.goodsName') },
      locationCode: { visible: true, label: t('wms.location') },
      quantity: { visible: true, label: t('wms.quantity') },
      pickedQuantity: { visible: true, label: t('wms.pickedQuantity') },
      taskStatus: { visible: true, label: t('wms.taskStatus') },
      assignedUser: { visible: true, label: t('wms.assignedUser') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    // ==================== 盘点管理模块 (2个) ====================
    
    /** 13. Stocktaking Plan - 盘点计划 */
    stocktakingPlan: {
      planNo: { visible: true, label: t('wms.planNo') },
      planName: { visible: true, label: t('wms.planName') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      takingType: { visible: true, label: t('wms.takingType') },
      scopeType: { visible: true, label: t('wms.scopeType') },
      planTime: { visible: true, label: t('wms.planTime') },
      progress: { visible: true, label: t('wms.progress') },
      diffCount: { visible: true, label: t('wms.diffCount') },
      status: { visible: true, label: t('wms.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    /** 14. Stocktaking - 盘点执行 */
    stocktaking: {
      takingNo: { visible: true, label: t('wms.takingNo') },
      planNo: { visible: true, label: t('wms.planNo') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      locationCode: { visible: true, label: t('wms.locationCode') },
      goodsName: { visible: true, label: t('wms.goodsName') },
      skuCode: { visible: true, label: t('wms.skuCode') },
      batchNo: { visible: true, label: t('wms.batchNo') },
      bookQuantity: { visible: true, label: t('wms.bookQuantity') },
      actualQuantity: { visible: true, label: t('wms.actualQuantity') },
      diffQuantity: { visible: true, label: t('wms.diffQuantity') },
      status: { visible: true, label: t('wms.status') },
      operator: { visible: true, label: t('wms.operator') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    // ==================== 调拨管理模块 (1个) ====================
    
    /** 15. Stock Move - 库存调拨 */
    stockMove: {
      moveNo: { visible: true, label: t('wms.moveNo') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      moveType: { visible: true, label: t('wms.moveType') },
      goodsName: { visible: true, label: t('wms.goodsName') },
      skuCode: { visible: true, label: t('wms.skuCode') },
      batchNo: { visible: true, label: t('wms.batchNo') },
      fromLocationCode: { visible: true, label: t('wms.fromLocationCode') },
      toLocationCode: { visible: true, label: t('wms.toLocationCode') },
      quantity: { visible: true, label: t('wms.quantity') },
      moveReason: { visible: true, label: t('wms.moveReason') },
      status: { visible: true, label: t('wms.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    // ==================== 合作伙伴管理模块 (3个) ====================
    
    /** 16. Supplier - 供应商管理 */
    supplier: {
      supplierCode: { visible: true, label: t('wms.supplierCode') },
      supplierName: { visible: true, label: t('wms.supplierName') },
      supplierType: { visible: true, label: t('wms.supplierType') },
      contactPerson: { visible: true, label: t('wms.contactPerson') },
      contactPhone: { visible: true, label: t('wms.contactPhone') },
      provinceCityDistrict: { visible: true, label: t('wms.provinceCityDistrict') },
      address: { visible: true, label: t('wms.address') },
      creditLevel: { visible: true, label: t('wms.creditLevel') },
      cooperationStartDate: { visible: true, label: t('wms.cooperationStartDate') },
      status: { visible: true, label: t('wms.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    /** 17. Customer - 客户管理 */
    customer: {
      customerName: { visible: true, label: t('wms.customerName') },
      customerType: { visible: true, label: t('wms.customerType') },
      customerLevel: { visible: true, label: t('wms.customerLevel') },
      contactPerson: { visible: true, label: t('wms.contactPerson') },
      contactPhone: { visible: true, label: t('wms.contactPhone') },
      deliveryAddress: { visible: true, label: t('wms.deliveryAddress') },
      creditLimit: { visible: true, label: t('wms.creditLimit') },
      totalOrders: { visible: true, label: t('wms.totalOrders') },
      totalAmount: { visible: true, label: t('wms.totalAmount') },
      status: { visible: true, label: t('wms.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    /** 18. Carrier - 承运商管理 */
    carrier: {
      carrierCode: { visible: true, label: t('wms.carrierCode') },
      carrierName: { visible: true, label: t('wms.carrierName') },
      carrierType: { visible: true, label: t('wms.carrierType') },
      contactPerson: { visible: true, label: t('wms.contactPerson') },
      contactPhone: { visible: true, label: t('wms.contactPhone') },
      serviceArea: { visible: true, label: t('wms.serviceArea') },
      timeLimit: { visible: true, label: t('wms.timeLimit') },
      rating: { visible: true, label: t('wms.rating') },
      cooperationStartDate: { visible: true, label: t('wms.cooperationDate') },
      status: { visible: true, label: t('wms.status') },
      createTime: { visible: true, label: t('common.createTime') }
    },

    // ==================== 报表管理模块 (3个) ====================
    
    /** 19. Inventory Report - 库存报表 */
    inventoryReport: {
      warehouseName: { visible: true, label: t('wms.warehouse') },
      locationCode: { visible: true, label: t('wms.location') },
      skuCode: { visible: true, label: t('wms.skuCode') },
      goodsName: { visible: true, label: t('wms.goodsName') },
      categoryName: { visible: true, label: t('wms.category') },
      batchNo: { visible: true, label: t('wms.batchNo') },
      totalQuantity: { visible: true, label: t('wms.totalQuantityLabel') },
      availableQuantity: { visible: true, label: t('wms.availableQuantityLabel') },
      lockedQuantity: { visible: true, label: t('wms.lockedQuantityLabel') },
      safetyStock: { visible: true, label: t('wms.safetyStock') },
      stockStatus: { visible: true, label: t('wms.stockStatus') }
    },

    /** 20. Turnover Report - 周转率报表 */
    turnoverReport: {
      goodsCode: { visible: true, label: t('wms.goodsCode') },
      goodsName: { visible: true, label: t('wms.goodsName') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      avgInventory: { visible: true, label: t('wms.avgInventory') },
      outboundQuantity: { visible: true, label: t('wms.outboundQuantity') },
      turnoverRate: { visible: true, label: t('wms.turnoverRate') },
      turnoverDays: { visible: true, label: t('wms.turnoverDays') },
      turnoverLevel: { visible: true, label: t('wms.turnoverLevel') },
      dateRange: { visible: true, label: t('wms.dateRange') }
    },

    /** 21. Age Report - 库龄报表 */
    ageReport: {
      goodsCode: { visible: true, label: t('wms.goodsCode') },
      goodsName: { visible: true, label: t('wms.goodsName') },
      warehouseName: { visible: true, label: t('wms.warehouse') },
      locationCode: { visible: true, label: t('wms.location') },
      batchNo: { visible: true, label: t('wms.batchNo') },
      quantity: { visible: true, label: t('wms.quantity') },
      inboundDate: { visible: true, label: t('wms.inboundDate') },
      age: { visible: true, label: t('wms.age') },
      ageLevel: { visible: true, label: t('wms.ageLevel') },
      remark: { visible: true, label: t('wms.remark') }
    }
  }
}

/**
 * 生成RightToolbar模板代码
 */
export function generateRightToolbarTemplate(moduleName: string): string {
  return `
<!-- 表格工具栏 -->
<div class="flex justify-between items-center mb-4">
  <div class="text-sm text-gray-600">
    {{ t('common.total') }}: {{ total }} {{ t('common.items') }}
  </div>
  <RightToolbar 
    v-model:showSearch="showSearch"
    :columns="columns"
    @queryTable="getList"
  />
</div>
`
}

/**
 * 生成JavaScript代码片段
 */
export function generateJavaScriptCode(moduleName: string): string {
  return `
// 导入RightToolbar组件
import RightToolbar from '@/components/RightToolbar/index.vue'

// 列设置功能 - RuoYi风格  
const columns = reactive(createWMSColumns(t).${moduleName})

// 显示搜索状态
const showSearch = ref(true)
`
}
