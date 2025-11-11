<!--
  供应商信息表单组件
  
  功能说明：
  1. 支持新增和编辑供应商
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
          <el-form-item :label="t('wms.supplierName')" prop="supplierName">
            <el-input v-model="formData.supplierName" :placeholder="t('wms.supplierNamePlaceholder')" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.supplierType')" prop="supplierType">
            <el-select v-model="formData.supplierType" :placeholder="t('wms.supplierTypePlaceholder')" class="!w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_SUPPLIER_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.creditLevel')" prop="creditLevel">
            <el-select v-model="formData.creditLevel" :placeholder="t('wms.creditLevelPlaceholder')" class="!w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_SUPPLIER_CREDIT_LEVEL)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.cooperationStartDate')" prop="cooperationStartDate">
            <el-date-picker
              v-model="formData.cooperationStartDate"
              type="date"
              value-format="YYYY-MM-DD"
              :placeholder="t('wms.cooperationStartDatePlaceholder')"
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

      <el-divider content-position="left">{{ t('wms.addressInfo') }}</el-divider>
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
          <el-form-item :label="t('wms.address')" prop="address">
            <el-input v-model="formData.address" :placeholder="t('wms.addressPlaceholder')" />
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
import * as SupplierApi from '@/api/wms/supplier'
import * as AreaApi from '@/api/system/area'

/**
 * 供应商表单组件定义
 */
defineOptions({ name: 'SupplierForm' })

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
  emitPath: true // 返回完整路径 [省ID, 市ID, 区ID]
}

// 表单数据
const formData = ref({
  id: undefined,
  supplierName: undefined,
  supplierType: 1, // 默认值：普通供应商
  contactPerson: undefined,
  contactPhone: undefined,
  contactEmail: undefined,
  areaId: undefined, // 地区ID数组 [省ID, 市ID, 区ID]
  province: undefined, // 省份名称（后端字段）
  city: undefined, // 城市名称（后端字段）
  district: undefined, // 区县名称（后端字段）
  address: undefined, // 详细地址
  creditLevel: 2, // 默认值：良好
  cooperationStartDate: undefined,
  status: 1, // 默认值：启用
  remark: undefined
})

// 表单验证规则
const formRules = reactive({
  supplierName: [{ required: true, message: t('wms.supplierNameRequired'), trigger: 'blur' }],
  supplierType: [{ required: true, message: t('wms.supplierTypeRequired'), trigger: 'change' }],
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
      const data = await SupplierApi.getSupplier(id)
      formData.value = data
      
      // 处理合作日期格式（LocalDate 数组 → 字符串）
      if (data.cooperationStartDate && Array.isArray(data.cooperationStartDate)) {
        const [year, month, day] = data.cooperationStartDate
        formData.value.cooperationStartDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/**
 * 地区变化处理
 * 将选中的地区ID数组转换为地区名称
 */
const handleAreaChange = (value: number[]) => {
  if (!value || value.length === 0) {
    formData.value.province = undefined
    formData.value.city = undefined
    formData.value.district = undefined
    return
  }
  
  // 根据ID路径查找地区名称
  const findAreaName = (list: any[], id: number): string | undefined => {
    for (const item of list) {
      if (item.id === id) {
        return item.name
      }
      if (item.children && item.children.length > 0) {
        const found = findAreaName(item.children, id)
        if (found) return found
      }
    }
    return undefined
  }
  
  // 设置省市区名称
  if (value.length >= 1) {
    formData.value.province = findAreaName(areaList.value, value[0])
  }
  if (value.length >= 2) {
    const province = areaList.value.find(item => item.id === value[0])
    if (province && province.children) {
      formData.value.city = findAreaName(province.children, value[1])
    }
  }
  if (value.length >= 3) {
    const province = areaList.value.find(item => item.id === value[0])
    if (province && province.children) {
      const city = province.children.find(item => item.id === value[1])
      if (city && city.children) {
        formData.value.district = findAreaName(city.children, value[2])
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
    const data = formData.value as unknown as SupplierApi.SupplierVO
    // 移除 areaId 字段（后端不需要）
    delete (data as any).areaId
    
    if (formType.value === 'create') {
      await SupplierApi.createSupplier(data)
      message.success(t('common.createSuccess'))
    } else {
      await SupplierApi.updateSupplier(data)
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
    supplierName: undefined,
    supplierType: 1,
    contactPerson: undefined,
    contactPhone: undefined,
    contactEmail: undefined,
    areaId: undefined,
    province: undefined,
    city: undefined,
    district: undefined,
    address: undefined,
    creditLevel: 2,
    cooperationStartDate: undefined,
    status: 1,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
