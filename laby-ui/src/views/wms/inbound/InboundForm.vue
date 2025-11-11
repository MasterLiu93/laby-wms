<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="1400px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item :label="t('wms.inboundType')" prop="inboundType">
            <el-select v-model="formData.inboundType" :placeholder="t('wms.inboundTypePlaceholder')" class="!w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_INBOUND_TYPE)"
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
          <el-form-item :label="t('wms.expectedArrivalTime')" prop="expectedArrivalTime">
            <el-date-picker
              v-model="formData.expectedArrivalTime"
              type="datetime"
              value-format="x"
              :placeholder="t('wms.expectedArrivalTimePlaceholder')"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item :label="t('wms.supplierName')" prop="supplierId">
            <el-select 
              v-model="formData.supplierId" 
              :placeholder="t('wms.supplierNamePlaceholder')"
              class="!w-full"
              filterable
              clearable
              @change="handleSupplierChange"
            >
              <el-option
                v-for="supplier in supplierList"
                :key="supplier.id"
                :label="`${supplier.supplierName} (${supplier.supplierCode})`"
                :value="supplier.id"
              >
                <span>{{ supplier.supplierName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px; margin-left: 10px">
                  {{ supplier.supplierCode }}
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
        <span class="text-gray-600">{{ t('wms.inboundDetailTitle') }}</span>
      </el-divider>
      
      <el-button type="primary" plain size="small" @click="openGoodsSelector" class="mb-10px">
        <Icon icon="ep:search" class="mr-5px" />
        {{ t('wms.selectGoods') }}
      </el-button>
      
      <el-table :data="formData.items" border stripe max-height="400px">
        <el-table-column type="index" label="#" width="50" align="center" />
        
        <!-- 商品信息（只读） -->
        <el-table-column :label="t('wms.skuCode')" prop="skuCode" width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.skuCode }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column :label="t('wms.goodsName')" prop="goodsName" width="180" show-overflow-tooltip />
        
        <el-table-column :label="t('wms.spec')" prop="spec" width="120" show-overflow-tooltip />
        
        <el-table-column :label="t('wms.unit')" prop="unit" width="70" align="center">
          <template #default="{ row }">
            <dict-tag :type="DICT_TYPE.WMS_GOODS_UNIT" :value="row.unit" />
          </template>
        </el-table-column>
        
        <!-- 可编辑项 -->
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
        
        <el-table-column :label="t('wms.batchNo')" prop="batchNo" width="150">
          <template #default="{ row }">
            <el-input v-model="row.batchNo" :placeholder="t('wms.batchNoPlaceholder')" size="small" />
          </template>
        </el-table-column>
        
        <el-table-column :label="t('wms.productionDate')" prop="productionDate" width="140">
          <template #default="{ row }">
            <el-date-picker
              v-model="row.productionDate"
              type="date"
              value-format="x"
              :placeholder="t('wms.productionDatePlaceholder')"
              size="small"
              class="!w-full"
            />
          </template>
        </el-table-column>
        
        <el-table-column :label="t('wms.expireDate')" prop="expireDate" width="140">
          <template #default="{ row }">
            <el-date-picker
              v-model="row.expireDate"
              type="date"
              value-format="x"
              :placeholder="t('wms.expireDatePlaceholder')"
              size="small"
              class="!w-full"
            />
          </template>
        </el-table-column>
        
        <el-table-column :label="t('wms.planQuantity')" prop="planQuantity" width="110">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.planQuantity" 
              :min="0" 
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
        
        <el-table-column :label="t('wms.amount')" prop="amount" width="120" align="right" fixed="right">
          <template #default="{ row }">
            <span class="text-red-500 font-semibold text-base">
              ¥{{ row.amount ? row.amount.toFixed(2) : '0.00' }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column :label="t('form.remark')" prop="remark" width="120">
          <template #default="{ row }">
            <el-input v-model="row.remark" :placeholder="t('form.remark')" size="small" />
          </template>
        </el-table-column>
        
        <el-table-column :label="t('action.action')" width="80" align="center" fixed="right">
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
      <el-button type="primary" @click="submitForm">{{ t('common.ok') }}</el-button>
    </template>
  </Dialog>
  
  <!-- 商品选择器 -->
  <GoodsSelector ref="goodsSelectorRef" @success="handleGoodsSelected" />
</template>


<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { getInbound, createInbound, updateInbound } from '@/api/wms/inbound'
import { getWarehouseSimpleList } from '@/api/wms/warehouse'
import * as SupplierApi from '@/api/wms/supplier'
import * as AreaApi from '@/api/wms/area'
import * as LocationApi from '@/api/wms/location'
import GoodsSelector from './GoodsSelector.vue'
import dayjs from 'dayjs'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  inboundType: undefined,
  warehouseId: undefined,
  supplierId: undefined,
  supplierName: undefined,
  expectedArrivalTime: undefined,
  remark: undefined,
  items: []
})
const formRules = reactive({
  inboundType: [{ required: true, message: t('common.required'), trigger: 'change' }],
  warehouseId: [{ required: true, message: t('common.required'), trigger: 'change' }]
})
const formRef = ref() // 表单 Ref
const warehouseList = ref([]) // 仓库列表
const supplierList = ref([]) // 供应商列表
const areaList = ref([]) // 库区列表
const locationList = ref([]) // 库位列表（所有库位）

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? t('wms.addInbound') : t('wms.editInbound')
  formType.value = type
  resetForm()
  
  // 加载基础数据
  await Promise.all([
    loadWarehouseList(),
    loadSupplierList()
  ])
  
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await getInbound(id)
      
      // 如果有仓库，先加载库区和库位列表
      if (data.warehouseId) {
        await Promise.all([
          loadAreaList(data.warehouseId),
          loadLocationList(data.warehouseId)
        ])
      }
      
      // 处理明细数据
      if (data.items && data.items.length > 0) {
        data.items.forEach((item: any) => {
          // 从库位ID反推库区ID
          if (item.locationId) {
            const location = locationList.value.find((loc: any) => loc.id === item.locationId)
            if (location) {
              item.areaId = location.areaId
            }
          }
          
          // 日期转换：时间戳 -> Date
          if (item.productionDate) {
            item.productionDate = new Date(item.productionDate).getTime()
          }
          if (item.expireDate) {
            item.expireDate = new Date(item.expireDate).getTime()
          }
          
          // 确保数值字段有默认值
          item.planQuantity = item.planQuantity || 0
          item.price = item.price || 0
          item.amount = item.amount || 0
          
          // 前端显示需要的字段（后端已返回）
          // goodsName, skuCode, goodsUnit, locationCode
          // 如果后端没有返回 spec 和 brand，设置为空字符串
          item.spec = item.spec || ''
          item.brand = item.brand || ''
          item.unit = item.goodsUnit // 统一使用 unit 字段
        })
      }
      
      // 设置表单数据
      formData.value = data
      
      // 转换主表日期字段
      if (formData.value.expectedArrivalTime) {
        formData.value.expectedArrivalTime = new Date(formData.value.expectedArrivalTime).getTime()
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
    message.warning(t('wms.selectGoodsFirst'))
    return
  }
  
  // 提交请求
  formLoading.value = true
  try {
    // 构建提交数据（只保留后端需要的字段）
    const submitData = {
      id: formData.value.id,
      inboundType: formData.value.inboundType,
      warehouseId: formData.value.warehouseId,
      supplierId: formData.value.supplierId,
      expectedArrivalTime: formData.value.expectedArrivalTime,
      remark: formData.value.remark,
      items: formData.value.items.map((item: any) => ({
        // 编辑时不传递明细ID，让后端完全重建明细列表
        // id: item.id,  ← 注释掉，避免主键冲突
        goodsId: item.goodsId,
        locationId: item.locationId,
        batchNo: item.batchNo,
        serialNo: item.serialNo,
        // 日期转换：时间戳 -> yyyy-MM-dd 格式
        productionDate: item.productionDate ? dayjs(item.productionDate).format('YYYY-MM-DD') : undefined,
        expireDate: item.expireDate ? dayjs(item.expireDate).format('YYYY-MM-DD') : undefined,
        planQuantity: item.planQuantity,
        price: item.price,
        remark: item.remark
      }))
    }
    
    if (formType.value === 'create') {
      await createInbound(submitData)
      message.success(t('wms.addSuccess'))
    } else {
      await updateInbound(submitData)
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
    inboundType: undefined,
    warehouseId: undefined,
    supplierId: undefined,
    supplierName: undefined,
    expectedArrivalTime: undefined,
    remark: undefined,
    items: []
  }
  formRef.value?.resetFields()
}

/** 加载仓库列表 */
const loadWarehouseList = async () => {
  const data = await getWarehouseSimpleList()
  warehouseList.value = data
}

/** 加载供应商列表 */
const loadSupplierList = async () => {
  const data = await SupplierApi.getSupplierSimpleList()
  supplierList.value = data
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

/** 库区变化处理 - 清空库位选择 */
const handleAreaChange = (row: any) => {
  row.locationId = undefined
}

/** 供应商变化处理 - 自动填充供应商名称 */
const handleSupplierChange = (supplierId: number) => {
  if (!supplierId) {
    formData.value.supplierName = undefined
    return
  }
  const supplier = supplierList.value.find((s: any) => s.id === supplierId)
  if (supplier) {
    formData.value.supplierName = supplier.supplierName
  }
}

/** 仓库变化处理 - 重新加载库区和库位列表 */
const handleWarehouseChange = async (warehouseId: number) => {
  // 清空明细中已选的库区和库位
  formData.value.items.forEach((item: any) => {
    item.areaId = undefined
    item.locationId = undefined
  })
  
  if (!warehouseId) {
    areaList.value = []
    locationList.value = []
    return
  }
  
  // 加载该仓库下的库区和所有库位
  await Promise.all([
    loadAreaList(warehouseId),
    loadLocationList(warehouseId)
  ])
}

/** 打开商品选择器 */
const goodsSelectorRef = ref()
const openGoodsSelector = () => {
  if (!formData.value.warehouseId) {
    message.warning(t('wms.selectAreaFirst'))
    return
  }
  goodsSelectorRef.value.open()
}

/** 商品选择完成回调 */
const handleGoodsSelected = (selectedGoods: any[]) => {
  selectedGoods.forEach(goods => {
    // 检查是否已添加（避免重复）
    const exists = formData.value.items.some((item: any) => item.goodsId === goods.id)
    if (!exists) {
      formData.value.items.push({
        goodsId: goods.id,
        skuCode: goods.skuCode,
        goodsName: goods.goodsName,
        spec: goods.spec, // 规格字段
        brand: goods.brand,
        unit: goods.unit,
        areaId: undefined, // 库区
        locationId: undefined, // 库位
        batchNo: generateBatchNo(), // 自动生成批次号
        productionDate: undefined,
        expireDate: undefined,
        planQuantity: 0,
        receivedQuantity: 0,
        qualifiedQuantity: 0,
        unqualifiedQuantity: 0,
        price: 0,
        amount: 0,
        remark: undefined
      })
    }
  })
}

/** 生成批次号 */
const generateBatchNo = () => {
  const date = dayjs().format('YYYYMMDD')
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  return `BATCH${date}${random}`
}

/** 格式化库位标签 */
const formatLocationLabel = (location: any) => {
  if (!location) return ''
  // 选中后只显示位置信息，不显示编码
  return t('wms.locationFormat').replace('{row}', location.rowNo).replace('{column}', location.columnNo).replace('{layer}', location.layerNo)
}

/** 计算金额 */
const calculateAmount = (row: any) => {
  row.amount = (row.planQuantity || 0) * (row.price || 0)
}

/** 计算合计数量 */
const totalQuantity = computed(() => {
  return formData.value.items.reduce((sum: number, item: any) => sum + (item.planQuantity || 0), 0)
})

/** 计算合计金额 */
const totalAmount = computed(() => {
  return formData.value.items.reduce((sum: number, item: any) => sum + (item.amount || 0), 0)
})

/** 删除商品明细 */
const handleDeleteItem = (index: number) => {
  formData.value.items.splice(index, 1)
}
</script>

