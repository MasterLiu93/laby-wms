<!--
  出库单表单组件
  
  功能说明：
  1. 支持新增和编辑出库单
  2. 商品选择器（可选择多个商品）
  3. 库位选择和库存显示
  4. 表单验证
  5. 成功后通知父组件刷新列表
  
  @author laby
  @date 2025-10-31
-->
<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="1600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item :label="t('wms.outboundType')" prop="outboundType">
            <el-select v-model="formData.outboundType" :placeholder="t('wms.outboundTypePlaceholder')" class="!w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_OUTBOUND_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
            <el-select 
              v-model="formData.warehouseId" 
              :placeholder="t('common.selectText')" 
              class="!w-full"
              @change="handleWarehouseChange"
            >
              <el-option
                v-for="warehouse in warehouseList"
                :key="warehouse.id"
                :label="warehouse.warehouseName"
                :value="warehouse.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="t('wms.expectedShipmentTime')" prop="expectedShipmentTime">
            <el-date-picker
              v-model="formData.expectedShipmentTime"
              type="datetime"
              value-format="x"
              :placeholder="t('wms.expectedShipmentTimePlaceholder')"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item :label="t('wms.customerName')" prop="customerId">
            <el-select 
              v-model="formData.customerId" 
              :placeholder="t('wms.customerNamePlaceholder')"
              class="!w-full"
              filterable
              clearable
              @change="handleCustomerChange"
            >
              <el-option
                v-for="customer in customerList"
                :key="customer.id"
                :label="`${customer.customerName} (${customer.customerCode})`"
                :value="customer.id"
              >
                <span>{{ customer.customerName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px; margin-left: 10px">
                  {{ customer.customerCode }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item :label="t('form.remark')" prop="remark">
            <el-input v-model="formData.remark" :placeholder="t('form.remarkPlaceholder')" clearable />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 明细表格 -->
      <el-divider content-position="left">
        <span class="text-gray-600 font-semibold">{{ t('wms.outboundDetailTitle') }}</span>
      </el-divider>
      <el-button type="primary" plain size="small" @click="handleSelectGoods" class="mb-10px" :disabled="!formData.warehouseId">
        <Icon icon="ep:plus" class="mr-5px" />
        {{ t('wms.selectGoods') }}
      </el-button>
      <el-text type="info" v-if="!formData.warehouseId" class="ml-10px">
        {{ t('wms.selectWarehouseFirst') }}
      </el-text>
      
      <el-table :data="formData.items" border stripe max-height="420px">
        <el-table-column type="index" label="#" width="50" align="center" />
        
        <el-table-column :label="t('wms.skuCode')" prop="skuCode" width="130" show-overflow-tooltip />
        
        <el-table-column :label="t('wms.goodsName')" prop="goodsName" min-width="150" show-overflow-tooltip />
        
        <el-table-column :label="t('wms.spec')" prop="spec" width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-gray-600">{{ row.spec || '-' }}</span>
          </template>
        </el-table-column>
        
        <!-- 库区选择 -->
        <el-table-column :label="t('wms.area')" prop="areaId" width="140">
          <template #default="{ row }">
            <el-select 
              v-model="row.areaId" 
              :placeholder="t('wms.selectArea')" 
              size="small" 
              class="!w-full"
              @change="handleAreaChange(row)"
            >
              <el-option
                v-for="area in areaList"
                :key="area.id"
                :label="area.areaName"
                :value="area.id"
              />
            </el-select>
          </template>
        </el-table-column>
        
        <!-- 库位选择 -->
        <el-table-column :label="t('wms.location')" prop="locationId" width="180">
          <template #default="{ row }">
            <el-select 
              v-model="row.locationId" 
              :placeholder="t('wms.selectLocationFirst')" 
              size="small" 
              class="!w-full"
              filterable
              :disabled="!row.areaId"
              @change="handleLocationChange(row)"
            >
              <el-option
                v-for="location in getLocationsByArea(row.areaId)"
                :key="location.id"
                :label="formatLocationLabel(location)"
                :value="location.id"
              >
                <!-- 主显示：位置信息（加粗） -->
                <span class="font-semibold text-base">
                  {{ t('wms.locationFormat').replace('{row}', location.rowNo).replace('{column}', location.columnNo).replace('{layer}', location.layerNo) }}
                </span>
                <!-- 副显示：编码（灰色小字） -->
                <span class="text-gray-400 text-xs ml-2">
                  {{ location.locationCode }}
                </span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column :label="t('wms.batchNo')" prop="batchNo" width="140">
          <template #default="{ row }">
            <el-select 
              v-model="row.batchNo" 
              :placeholder="t('wms.selectBatch')" 
              size="small" 
              class="!w-full"
              clearable
              filterable
              @change="handleBatchChange(row)"
            >
              <el-option
                v-for="batch in row.batchOptions || []"
                :key="batch.batchNo"
                :label="batch.batchNo || t('wms.noBatch')"
                :value="batch.batchNo"
              >
                <span>{{ batch.batchNo || t('wms.noBatch') }}</span>
                <span class="text-gray-400 ml-10px">{{ t('wms.inventory') }}: {{ batch.quantity }}</span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column :label="t('wms.availableQuantity')" prop="availableQuantity" width="100" align="right">
          <template #default="{ row }">
            <el-tag v-if="row.availableQuantity !== undefined" :type="row.availableQuantity > 0 ? 'success' : 'danger'" size="small">
              {{ row.availableQuantity }}
            </el-tag>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        
        <el-table-column :label="t('wms.planQuantity')" prop="planQuantity" width="110">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.planQuantity" 
              :min="0" 
              :max="row.availableQuantity || 999999"
              :precision="2" 
              size="small" 
              class="!w-full" 
              :controls="false"
              @change="calculateAmount(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column :label="t('wms.price')" prop="price" width="110">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.price" 
              :min="0" 
              :precision="2" 
              size="small" 
              class="!w-full" 
              :controls="false"
              @change="calculateAmount(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column :label="t('wms.amount')" prop="amount" width="110" align="right">
          <template #default="{ row }">
            <span class="text-red-500 font-semibold">¥{{ (row.amount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column :label="t('form.remark')" prop="remark" min-width="120">
          <template #default="{ row }">
            <el-input v-model="row.remark" :placeholder="t('form.remark')" size="small" clearable />
          </template>
        </el-table-column>
        
        <el-table-column :label="t('action.action')" width="70" align="center" fixed="right">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="handleDeleteItem($index)">
              {{ t('action.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 合计行 -->
      <div class="mt-10px text-right" v-if="formData.items.length > 0">
        <el-tag size="large" type="info" class="mr-10px">
          {{ t('common.totalQuantity') }}：<span class="font-semibold">{{ totalQuantity }}</span>
        </el-tag>
        <el-tag size="large" type="danger">
          {{ t('common.totalAmount') }}：<span class="font-semibold text-lg">¥{{ totalAmount.toFixed(2) }}</span>
        </el-tag>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">{{ t('common.ok') }}</el-button>
    </template>
  </Dialog>
  
  <!-- 商品选择器 -->
  <GoodsSelector ref="goodsSelectorRef" @success="handleGoodsSelected" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { getOutbound, createOutbound, updateOutbound } from '@/api/wms/outbound'
import { getWarehouseSimpleList } from '@/api/wms/warehouse'
import * as AreaApi from '@/api/wms/area'
import * as LocationApi from '@/api/wms/location'
import { getInventoryPage } from '@/api/wms/inventory'
import { getCustomerSimpleList } from '@/api/wms/customer'
import GoodsSelector from './GoodsSelector.vue'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  outboundType: undefined,
  warehouseId: undefined,
  customerId: undefined,
  customerName: undefined,
  expectedShipmentTime: undefined,
  remark: undefined,
  items: []
})
const formRules = reactive({
  outboundType: [{ required: true, message: t('common.required'), trigger: 'change' }],
  warehouseId: [{ required: true, message: t('common.required'), trigger: 'change' }]
})
const formRef = ref() // 表单 Ref
const warehouseList = ref([]) // 仓库列表
const customerList = ref([]) // 客户列表
const areaList = ref([]) // 库区列表
const locationList = ref([]) // 库位列表

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? t('wms.addOutbound') : t('wms.editOutbound')
  formType.value = type
  resetForm()
  
  // 加载基础数据
  await Promise.all([
    loadWarehouseList(),
    loadCustomerList()
  ])
  
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await getOutbound(id)
      
      // 如果有仓库，先加载库区和库位列表
      if (data.warehouseId) {
        await Promise.all([
          loadAreaList(data.warehouseId),
          loadLocationList(data.warehouseId)
        ])
      }
      
      // 处理明细数据
      if (data.items && data.items.length > 0) {
        // 为每个明细加载批次和库存信息
        for (const item of data.items) {
          // 从库位ID反推库区ID
          if (item.locationId) {
            const location = locationList.value.find((loc: any) => loc.id === item.locationId)
            if (location) {
              item.areaId = location.areaId
            }
          }
          // 确保数值字段有默认值
          item.planQuantity = item.planQuantity || 0
          item.pickedQuantity = item.pickedQuantity || 0
          item.shippedQuantity = item.shippedQuantity || 0
          item.price = item.price || 0
          item.amount = item.amount || 0
          
          // 前端显示需要的字段
          item.spec = item.spec || ''
          item.batchOptions = []
          item.availableQuantity = undefined
          
          // 如果有库位和商品ID，加载批次信息
          if (item.locationId && item.goodsId && data.warehouseId) {
            try {
              const inventoryData = await getInventoryPage({
                warehouseId: data.warehouseId,
                locationId: item.locationId,
                goodsId: item.goodsId,
                pageNo: 1,
                pageSize: 100
              })
              
              // 批次选项
              item.batchOptions = inventoryData.list.map(inv => ({
                batchNo: inv.batchNo,
                quantity: inv.quantity - inv.lockQuantity // 可用数量
              }))
              
              // 如果有批次号，设置可用库存
              if (item.batchNo) {
                const batch = item.batchOptions.find(b => b.batchNo === item.batchNo)
                if (batch) {
                  item.availableQuantity = batch.quantity
                }
              }
            } catch (error) {
              console.error('加载库存信息失败', error)
            }
          }
        }
      }
      
      // 设置表单数据
      formData.value = data
      
      // 转换主表日期字段
      if (formData.value.expectedShipmentTime) {
        formData.value.expectedShipmentTime = new Date(formData.value.expectedShipmentTime).getTime()
      }
      
      // 重新计算所有明细的金额
      if (formData.value.items && formData.value.items.length > 0) {
        formData.value.items.forEach(item => {
          calculateAmount(item)
        })
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  // 校验明细
  if (!formData.value.items || formData.value.items.length === 0) {
    message.error('请添加至少一个商品明细')
    return
  }
  
  // 校验明细的库位和计划数量
  for (let i = 0; i < formData.value.items.length; i++) {
    const item = formData.value.items[i]
    if (!item.locationId) {
      message.error(`第${i + 1}行商品未选择库位`)
      return
    }
    if (!item.planQuantity || item.planQuantity <= 0) {
      message.error(`第${i + 1}行商品的计划数量必须大于0`)
      return
    }
    if (item.availableQuantity !== undefined && item.planQuantity > item.availableQuantity) {
      message.error(`第${i + 1}行商品的计划数量不能超过可用库存`)
      return
    }
  }
  
  // 提交请求
  formLoading.value = true
  try {
    // 构建提交数据（只保留后端需要的字段）
    const submitData = {
      id: formData.value.id,
      outboundType: formData.value.outboundType,
      warehouseId: formData.value.warehouseId,
      customerId: formData.value.customerId,
      expectedShipmentTime: formData.value.expectedShipmentTime,
      remark: formData.value.remark,
      items: formData.value.items.map((item: any) => ({
        goodsId: item.goodsId,
        locationId: item.locationId,
        batchNo: item.batchNo,
        serialNo: item.serialNo,
        planQuantity: item.planQuantity,
        price: item.price,
        remark: item.remark
      }))
    }
    
    if (formType.value === 'create') {
      await createOutbound(submitData)
      message.success(t('wms.addSuccess'))
    } else {
      await updateOutbound(submitData)
      message.success(t('wms.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    outboundType: undefined,
    warehouseId: undefined,
    customerId: undefined,
    customerName: undefined,
    expectedShipmentTime: undefined,
    remark: undefined,
    items: []
  }
  areaList.value = []
  locationList.value = []
  formRef.value?.resetFields()
}

/** 加载仓库列表 */
const loadWarehouseList = async () => {
  const data = await getWarehouseSimpleList()
  warehouseList.value = data
}

/** 加载客户列表 */
const loadCustomerList = async () => {
  const data = await getCustomerSimpleList()
  customerList.value = data
}

/** 加载库区列表 */
const loadAreaList = async (warehouseId: number) => {
  if (!warehouseId) {
    areaList.value = []
    return
  }
  const data = await AreaApi.getWarehouseAreaListByWarehouseId(warehouseId)
  areaList.value = data
}

/** 加载库位列表（加载该仓库下的所有库位） */
const loadLocationList = async (warehouseId: number) => {
  if (!warehouseId) {
    locationList.value = []
    return
  }
  const data = await LocationApi.getWarehouseLocationListByWarehouseId(warehouseId)
  locationList.value = data
}

/** 根据库区过滤库位 */
const getLocationsByArea = (areaId: number) => {
  if (!areaId) return []
  return locationList.value.filter((location: any) => location.areaId === areaId)
}

/** 格式化库位标签 */
const formatLocationLabel = (location: any) => {
  if (!location) return ''
  // 选中后只显示位置信息，不显示编码
  return t('wms.locationFormat').replace('{row}', location.rowNo).replace('{column}', location.columnNo).replace('{layer}', location.layerNo)
}

/** 库区变化处理 - 清空库位选择 */
const handleAreaChange = (row: any) => {
  row.locationId = undefined
  // 清空批次和可用库存
  row.batchNo = undefined
  row.batchOptions = []
  row.availableQuantity = undefined
}

/** 仓库变化 */
const handleWarehouseChange = async (warehouseId: number) => {
  // 清空明细
  formData.value.items = []
  // 清空库区和库位列表
  areaList.value = []
  locationList.value = []
  
  if (!warehouseId) return
  
  // 加载新的库区和库位列表
  await Promise.all([
    loadAreaList(warehouseId),
    loadLocationList(warehouseId)
  ])
}

/** 客户变化 */
const handleCustomerChange = (customerId: number) => {
  if (!customerId) {
    formData.value.customerName = undefined
    return
  }
  // 自动填充客户名称
  const customer = customerList.value.find(c => c.id === customerId)
  if (customer) {
    formData.value.customerName = customer.customerName
  }
}

/** 打开商品选择器 */
const goodsSelectorRef = ref()
const handleSelectGoods = () => {
  goodsSelectorRef.value.open()
}

/** 商品选择成功 */
const handleGoodsSelected = (goods: any[]) => {
  goods.forEach(g => {
    // 检查是否已存在
    const exists = formData.value.items.some(item => item.goodsId === g.id)
    if (!exists) {
  formData.value.items.push({
        goodsId: g.id,
        skuCode: g.skuCode,
        goodsName: g.goodsName,
        spec: g.spec,
        areaId: undefined,
        locationId: undefined,
    batchNo: undefined,
        batchOptions: [],
        availableQuantity: undefined,
    planQuantity: 0,
    pickedQuantity: 0,
    shippedQuantity: 0,
    price: 0,
    amount: 0,
    remark: undefined
  })
    }
  })
}

/** 库位变化 - 加载批次和库存 */
const handleLocationChange = async (row: any) => {
  if (!row.locationId || !formData.value.warehouseId || !row.goodsId) {
    row.batchOptions = []
    row.batchNo = undefined
    row.availableQuantity = undefined
    return
  }
  
  // 查询该库位的库存（按批次分组）
  try {
    const data = await getInventoryPage({
      warehouseId: formData.value.warehouseId,
      locationId: row.locationId,
      goodsId: row.goodsId,
      pageNo: 1,
      pageSize: 100
    })
    
    // 批次选项
    row.batchOptions = data.list.map(inv => ({
      batchNo: inv.batchNo,
      quantity: inv.quantity - inv.lockQuantity // 可用数量
    }))
    
    // 如果只有一个批次，自动选择
    if (row.batchOptions.length === 1) {
      row.batchNo = row.batchOptions[0].batchNo
      row.availableQuantity = row.batchOptions[0].quantity
    } else {
      row.batchNo = undefined
      row.availableQuantity = undefined
    }
  } catch (error) {
    console.error('查询库存失败', error)
    row.batchOptions = []
  }
}

/** 批次变化 - 更新可用库存 */
const handleBatchChange = (row: any) => {
  if (!row.batchNo) {
    row.availableQuantity = undefined
    return
  }
  
  const batch = row.batchOptions.find(b => b.batchNo === row.batchNo)
  if (batch) {
    row.availableQuantity = batch.quantity
    // 自动设置计划数量为可用数量（可以手动修改）
    if (!row.planQuantity || row.planQuantity === 0) {
      row.planQuantity = batch.quantity
      calculateAmount(row)
    }
  }
}

/** 计算金额 */
const calculateAmount = (row: any) => {
  row.amount = (row.planQuantity || 0) * (row.price || 0)
}

/** 删除商品明细 */
const handleDeleteItem = (index: number) => {
  formData.value.items.splice(index, 1)
}

/** 计算合计数量 */
const totalQuantity = computed(() => {
  return formData.value.items.reduce((sum, item) => sum + (item.planQuantity || 0), 0)
})

/** 计算合计金额 */
const totalAmount = computed(() => {
  return formData.value.items.reduce((sum, item) => sum + (item.amount || 0), 0)
})
</script>
