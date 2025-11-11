<!--
  客户信息表单组件
  
  功能说明：
  1. 支持新增和编辑客户
  2. 表单验证
  3. 成功后通知父组件刷新列表
  
  @author laby
  @date 2025-10-28
-->
<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="1200px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-divider content-position="left">{{ t('wms.basicInfo') }}</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item :label="t('wms.customerName')" prop="customerName">
            <el-input v-model="formData.customerName" :placeholder="t('wms.customerNamePlaceholder')" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.customerType')" prop="customerType">
            <el-select v-model="formData.customerType" :placeholder="t('wms.customerTypePlaceholder')" class="!w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_CUSTOMER_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.customerLevel')" prop="customerLevel">
            <el-select v-model="formData.customerLevel" :placeholder="t('wms.customerLevelPlaceholder')" class="!w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_CUSTOMER_LEVEL)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.creditLimit')" prop="creditLimit">
            <el-input-number
              v-model="formData.creditLimit"
              :precision="2"
              :min="0"
              :step="1000"
              :placeholder="t('wms.creditLimitPlaceholder')"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.status')" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio
                v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                :key="dict.value"
                :label="dict.value"
              >
                {{ dict.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">{{ t('wms.contactInfo') }}</el-divider>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item :label="t('wms.contactPerson')" prop="contactPerson">
            <el-input v-model="formData.contactPerson" :placeholder="t('wms.contactPersonPlaceholder')" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="t('wms.contactPhone')" prop="contactPhone">
            <el-input v-model="formData.contactPhone" :placeholder="t('wms.contactPhonePlaceholder')" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="t('wms.contactEmail')" prop="contactEmail">
            <el-input v-model="formData.contactEmail" :placeholder="t('wms.contactEmailPlaceholder')" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">{{ t('wms.deliveryAddressInfo') }}</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item :label="t('wms.provinceCityDistrict')" prop="areaId">
            <el-cascader
              v-model="formData.areaId"
              :options="areaList"
              :props="areaProps"
              clearable
              filterable
              :placeholder="t('wms.areaPlaceholder')"
              class="!w-full"
              @change="handleAreaChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.deliveryAddress')" prop="deliveryAddress">
            <el-input v-model="formData.deliveryAddress" :placeholder="t('wms.deliveryAddressPlaceholder')" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">{{ t('wms.otherInfo') }}</el-divider>
      <el-row>
        <el-col :span="24">
          <el-form-item :label="t('form.remark')" prop="remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              :placeholder="t('form.remarkPlaceholder')"
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
import * as CustomerApi from '@/api/wms/customer'
import * as AreaApi from '@/api/system/area'

/**
 * 客户表单组件定义
 */
defineOptions({ name: 'CustomerForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const areaList = ref<any[]>([]) // 地区列表

// 级联选择器配置
const areaProps = {
  children: 'children',
  label: 'name',
  value: 'id',
  emitPath: true // 返回完整路径
}

// 表单数据
const formData = ref({
  id: undefined,
  customerName: undefined,
  customerType: 1, // 默认值：零售客户
  customerLevel: 4, // 默认值：普通客户
  contactPerson: undefined,
  contactPhone: undefined,
  contactEmail: undefined,
  areaId: undefined, // 地区ID数组
  deliveryProvince: undefined, // 省份（后端字段）
  deliveryCity: undefined, // 城市（后端字段）
  deliveryDistrict: undefined, // 区县（后端字段）
  deliveryAddress: undefined,
  creditLimit: undefined,
  status: 1, // 默认值：启用
  remark: undefined
})

// 表单验证规则
const formRules = reactive({
  customerName: [{ required: true, message: t('wms.customerNameRequired'), trigger: 'blur' }],
  customerType: [{ required: true, message: t('wms.customerTypeRequired'), trigger: 'change' }],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: t('wms.phoneFormatError'), trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: t('wms.emailFormatError'), trigger: 'blur' }
  ]
})
const formRef = ref()

/**
 * 打开弹窗
 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? t('action.add') : t('action.edit')
  formType.value = type
  resetForm()
  
  // 加载地区列表
  areaList.value = await AreaApi.getAreaTree()
  
  // 修改时，加载数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await CustomerApi.getCustomer(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/**
 * 地区变化处理
 */
const handleAreaChange = (value: number[]) => {
  if (!value || value.length === 0) {
    formData.value.deliveryProvince = undefined
    formData.value.deliveryCity = undefined
    formData.value.deliveryDistrict = undefined
    return
  }
  
  const findAreaName = (list: any[], id: number): string | undefined => {
    for (const item of list) {
      if (item.id === id) return item.name
      if (item.children && item.children.length > 0) {
        const found = findAreaName(item.children, id)
        if (found) return found
      }
    }
    return undefined
  }
  
  if (value.length >= 1) {
    formData.value.deliveryProvince = findAreaName(areaList.value, value[0])
  }
  if (value.length >= 2) {
    const province = areaList.value.find(item => item.id === value[0])
    if (province && province.children) {
      formData.value.deliveryCity = findAreaName(province.children, value[1])
    }
  }
  if (value.length >= 3) {
    const province = areaList.value.find(item => item.id === value[0])
    if (province && province.children) {
      const city = province.children.find(item => item.id === value[1])
      if (city && city.children) {
        formData.value.deliveryDistrict = findAreaName(city.children, value[2])
      }
    }
  }
}

/**
 * 提交表单
 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as CustomerApi.CustomerVO
    delete (data as any).areaId // 移除 areaId
    
    if (formType.value === 'create') {
      await CustomerApi.createCustomer(data)
      message.success(t('common.createSuccess'))
    } else {
      await CustomerApi.updateCustomer(data)
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
    customerName: undefined,
    customerType: 1,
    customerLevel: 4,
    contactPerson: undefined,
    contactPhone: undefined,
    contactEmail: undefined,
    areaId: undefined,
    deliveryProvince: undefined,
    deliveryCity: undefined,
    deliveryDistrict: undefined,
    deliveryAddress: undefined,
    creditLimit: undefined,
    status: 1,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
