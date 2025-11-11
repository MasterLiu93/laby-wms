<!--
  库存信息表单
  
  功能说明：
  1. 新增/编辑库存信息
  2. 表单校验：库存数量、锁定数量合法性
  3. 仓库-库位三级联动
  4. 根据商品配置决定是否显示批次号/序列号
  
  @author laby
  @date 2025-10-28
-->
<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <!-- 仓库选择 -->
        <el-col :span="12">
          <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
            <el-select
              v-model="formData.warehouseId"
              :placeholder="t('common.selectText')"
              clearable
              filterable
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
        
        <!-- 库位选择（联动） -->
        <el-col :span="12">
          <el-form-item :label="t('wms.location')" prop="locationId">
            <el-select
              v-model="formData.locationId"
              :placeholder="t('wms.locationOptional')"
              clearable
              filterable
              class="!w-full"
              :disabled="!formData.warehouseId"
            >
              <el-option
                v-for="location in locationList"
                :key="location.id"
                :label="location.locationCode"
                :value="location.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        
        <!-- 商品选择 -->
        <el-col :span="24">
          <el-form-item :label="t('wms.goods')" prop="goodsId">
            <el-select
              v-model="formData.goodsId"
              :placeholder="t('common.selectText')"
              clearable
              filterable
              class="!w-full"
              @change="handleGoodsChange"
            >
              <el-option
                v-for="goods in goodsList"
                :key="goods.id"
                :label="`${goods.goodsName} (${goods.skuCode})`"
                :value="goods.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        
        <!-- 批次号（根据商品配置显示） -->
        <el-col :span="12" v-if="showBatchNo">
          <el-form-item :label="t('wms.batchNo')" prop="batchNo">
            <el-input
              v-model="formData.batchNo"
              :placeholder="t('wms.batchNoPlaceholder')"
              clearable
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        
        <!-- 序列号（根据商品配置显示） -->
        <el-col :span="12" v-if="showSerialNo">
          <el-form-item :label="t('wms.serialNo')" prop="serialNo">
            <el-input
              v-model="formData.serialNo"
              :placeholder="t('wms.serialNoPlaceholder')"
              clearable
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        
        <!-- 库存数量 -->
        <el-col :span="12">
          <el-form-item :label="t('wms.quantity')" prop="quantity">
            <el-input-number
              v-model="formData.quantity"
              :min="0"
              :precision="2"
              controls-position="right"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        
        <!-- 锁定数量 -->
        <el-col :span="12">
          <el-form-item :label="t('wms.lockQuantity')" prop="lockQuantity">
            <el-input-number
              v-model="formData.lockQuantity"
              :min="0"
              :max="formData.quantity || 0"
              :precision="2"
              controls-position="right"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        
        <!-- 生产日期 -->
        <el-col :span="12">
          <el-form-item :label="t('wms.productionDate')" prop="productionDate">
            <el-date-picker
              v-model="formData.productionDate"
              type="date"
              value-format="YYYY-MM-DD"
              :placeholder="t('wms.productionDatePlaceholder')"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        
        <!-- 过期日期 -->
        <el-col :span="12">
          <el-form-item :label="t('wms.expireDate')" prop="expireDate">
            <el-date-picker
              v-model="formData.expireDate"
              type="date"
              value-format="YYYY-MM-DD"
              :placeholder="t('wms.expireDatePlaceholder')"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        
        <!-- 入库日期 -->
        <el-col :span="12">
          <el-form-item :label="t('wms.inboundDate')" prop="inboundDate">
            <el-date-picker
              v-model="formData.inboundDate"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              :placeholder="t('wms.inboundDatePlaceholder')"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        
        <!-- 库存状态 -->
        <el-col :span="12">
          <el-form-item :label="t('wms.inventoryStatus')" prop="status">
            <el-select
              v-model="formData.status"
              :placeholder="t('common.selectText')"
              class="!w-full"
            >
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_INVENTORY_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        
        <!-- 供应商名称 -->
        <el-col :span="24">
          <el-form-item :label="t('wms.supplier')" prop="supplierName">
            <el-input
              v-model="formData.supplierName"
              :placeholder="t('wms.supplierNamePlaceholder')"
              clearable
              maxlength="100"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">{{ t('common.ok') }}</el-button>
      <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as InventoryApi from '@/api/wms/inventory'
import * as WarehouseApi from '@/api/wms/warehouse'
import * as WarehouseLocationApi from '@/api/wms/location'
import * as GoodsApi from '@/api/wms/goods'

defineOptions({ name: 'InventoryForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  warehouseId: undefined,
  locationId: undefined,
  goodsId: undefined,
  batchNo: undefined,
  serialNo: undefined,
  quantity: 0,
  lockQuantity: 0,
  productionDate: undefined,
  expireDate: undefined,
  inboundDate: undefined,
  supplierId: undefined,
  supplierName: undefined,
  status: 1 // 默认正常状态
})

const formRules = reactive({
  warehouseId: [{ required: true, message: t('common.selectText'), trigger: 'change' }],
  goodsId: [{ required: true, message: t('common.selectText'), trigger: 'change' }],
  quantity: [{ required: true, message: t('common.required'), trigger: 'blur' }],
  status: [{ required: true, message: t('common.required'), trigger: 'change' }]
})

const formRef = ref()

// 仓库、库位、商品数据
const warehouseList = ref([])
const locationList = ref([])
const goodsList = ref([])

// 根据商品配置决定是否显示批次号/序列号
const showBatchNo = ref(false)
const showSerialNo = ref(false)

/**
 * 打开弹窗
 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? t('wms.addInventory') : t('wms.editInventory')
  formType.value = type
  resetForm()
  
  // 加载仓库和商品列表
  await loadBaseData()
  
  // 编辑时加载详情
  if (id) {
    formLoading.value = true
    try {
      const data = await InventoryApi.getInventory(id)
      formData.value = data
      
      // 加载对应仓库的库位列表
      if (data.warehouseId) {
        await handleWarehouseChange(data.warehouseId)
      }
      
      // 加载商品配置
      if (data.goodsId) {
        await handleGoodsChange(data.goodsId)
      }
    } finally {
      formLoading.value = false
    }
  }
}

/**
 * 提交表单
 */
const submitForm = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  
  formLoading.value = true
  try {
    const data = formData.value
    if (formType.value === 'create') {
      await InventoryApi.createInventory(data)
      message.success(t('common.createSuccess'))
    } else {
      await InventoryApi.updateInventory(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    warehouseId: undefined,
    locationId: undefined,
    goodsId: undefined,
    batchNo: undefined,
    serialNo: undefined,
    quantity: 0,
    lockQuantity: 0,
    productionDate: undefined,
    expireDate: undefined,
    inboundDate: undefined,
    supplierId: undefined,
    supplierName: undefined,
    status: 1
  }
  formRef.value?.resetFields()
}

/**
 * 加载基础数据
 */
const loadBaseData = async () => {
  try {
    // 加载仓库列表
    warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
    
    // 加载商品列表
    goodsList.value = await GoodsApi.getGoodsSimpleList()
  } catch (e) {
    console.error('加载基础数据失败', e)
  }
}

/**
 * 仓库变更，加载对应的库位列表
 */
const handleWarehouseChange = async (warehouseId: number) => {
  formData.value.locationId = undefined
  locationList.value = []
  
  if (warehouseId) {
    try {
      const data = await WarehouseLocationApi.getWarehouseLocationSimpleList({ warehouseId })
      locationList.value = data
    } catch (e) {
      console.error('加载库位列表失败', e)
    }
  }
}

/**
 * 商品变更，加载商品配置
 */
const handleGoodsChange = async (goodsId: number) => {
  if (goodsId) {
    try {
      const goods = goodsList.value.find(item => item.id === goodsId)
      if (goods) {
        // 根据商品配置决定是否显示批次号/序列号
        showBatchNo.value = goods.needBatch === 1 || goods.needBatch === true
        showSerialNo.value = goods.needSerial === 1 || goods.needSerial === true
      }
    } catch (e) {
      console.error('加载商品配置失败', e)
    }
  }
}

// 暴露给父组件
defineExpose({ open })

// 定义 emit
const emit = defineEmits(['success'])
</script>

