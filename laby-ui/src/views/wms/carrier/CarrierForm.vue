<!--
  承运商信息表单组件
  
  功能说明：
  1. 支持新增和编辑承运商
  2. 表单验证
  3. 成功后通知父组件刷新列表
  
  @author laby
  @date 2025-10-28
-->
<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="1000px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <!-- 基本信息 -->
        <el-col :span="24">
          <el-divider content-position="left">{{ t('wms.basicInfo') }}</el-divider>
        </el-col>
        
        <el-col :span="12">
          <el-form-item :label="t('wms.carrierName')" prop="carrierName">
            <el-input
              v-model="formData.carrierName"
              :placeholder="t('wms.carrierNamePlaceholder')"
              maxlength="100"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item :label="t('wms.carrierType')" prop="carrierType">
            <el-select
              v-model="formData.carrierType"
              :placeholder="t('wms.carrierTypePlaceholder')"
              class="!w-full"
            >
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_CARRIER_TYPE)"
                :key="dict.value"
                :label="getDictLabel(dict)"
                :value="dict.value"
              />
            </el-select>
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
                {{ getDictLabel(dict) }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        
        <!-- 联系信息 -->
        <el-col :span="24">
          <el-divider content-position="left">{{ t('wms.contactInfo') }}</el-divider>
        </el-col>
        
        <el-col :span="12">
          <el-form-item :label="t('wms.contactPerson')" prop="contactPerson">
            <el-input
              v-model="formData.contactPerson"
              :placeholder="t('wms.contactPersonPlaceholder')"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item :label="t('wms.contactPhone')" prop="contactPhone">
            <el-input
              v-model="formData.contactPhone"
              :placeholder="t('wms.contactPhonePlaceholder')"
              maxlength="20"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item :label="t('wms.contactEmail')" prop="contactEmail">
            <el-input
              v-model="formData.contactEmail"
              :placeholder="t('wms.contactEmailPlaceholder')"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        
        <!-- 服务信息 -->
        <el-col :span="24">
          <el-divider content-position="left">{{ t('wms.serviceInfo') }}</el-divider>
        </el-col>
        
        <el-col :span="24">
          <el-form-item :label="t('wms.serviceArea')" prop="serviceAreaIds">
            <el-cascader
              v-model="formData.serviceAreaIds"
              :options="areaList"
              :props="areaMultiProps"
              clearable
              filterable
              :placeholder="t('wms.serviceAreaPlaceholder')"
              class="!w-full"
              collapse-tags
              collapse-tags-tooltip
              @change="handleServiceAreaChange"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item :label="t('wms.timeLimit')" prop="timeLimit">
            <el-input
              v-model="formData.timeLimit"
              :placeholder="t('wms.timeLimitPlaceholder')"
              maxlength="100"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="24">
          <el-form-item :label="t('wms.priceStandard')" prop="priceStandard">
            <el-input
              type="textarea"
              v-model="formData.priceStandard"
              :placeholder="t('wms.priceStandardPlaceholder')"
              :rows="2"
              maxlength="500"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item :label="t('wms.rating')" prop="rating">
            <el-rate
              v-model="formData.rating"
              show-score
              text-color="#ff9900"
              :score-template="`{value} ${t('wms.score')}`"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item :label="t('wms.cooperationStartDate')" prop="cooperationStartDate">
            <el-date-picker
              v-model="formData.cooperationStartDate"
              type="date"
              :placeholder="t('wms.cooperationStartDatePlaceholder')"
              value-format="YYYY-MM-DD"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="24">
          <el-form-item :label="t('form.remark')" prop="remark">
            <el-input
              type="textarea"
              v-model="formData.remark"
              :placeholder="t('form.remarkPlaceholder')"
              :rows="3"
              maxlength="500"
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
import { useDictI18n } from '@/hooks/web/useDictI18n'
import * as CarrierApi from '@/api/wms/carrier'
import * as AreaApi from '@/api/system/area'

const { getDictLabel } = useDictI18n() // 字典国际化

defineOptions({ name: 'CarrierForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const areaList = ref<any[]>([]) // 地区列表

// 多选级联选择器配置（服务区域）
const areaMultiProps = {
  children: 'children',
  label: 'name',
  value: 'id',
  multiple: true, // 支持多选
  emitPath: true, // 返回完整路径
  checkStrictly: true // 可以选择任意级别
}

// 表单数据
const formData = ref({
  id: undefined,
  carrierName: undefined,
  carrierType: 1, // 默认值：快递
  contactPerson: undefined,
  contactPhone: undefined,
  contactEmail: undefined,
  serviceAreaIds: [], // 服务区域ID数组（多选）
  serviceArea: undefined, // 服务区域名称（后端字段）
  priceStandard: undefined,
  timeLimit: undefined,
  rating: 5.0, // 默认值：5.0
  cooperationStartDate: undefined,
  status: 1, // 默认值：启用
  remark: undefined
})

// 表单验证规则
const formRules = reactive({
  carrierName: [{ required: true, message: t('wms.carrierNameRequired'), trigger: 'blur' }],
  carrierType: [{ required: true, message: t('wms.carrierTypeRequired'), trigger: 'change' }],
  contactPhone: [
    { required: true, message: t('wms.contactPhoneRequired'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$|^\d{3,4}-\d{7,8}$/, message: t('wms.phoneFormatError'), trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: t('wms.emailFormatError'), trigger: 'blur' }
  ],
  status: [{ required: true, message: t('wms.statusRequired'), trigger: 'change' }]
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
      const data = await CarrierApi.getCarrier(id)
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
 * 服务区域变化处理（多选）
 * 将选中的地区ID数组转换为地区名称字符串
 */
const handleServiceAreaChange = (value: any) => {
  if (!value || value.length === 0) {
    formData.value.serviceArea = undefined
    return
  }
  
  // value 是二维数组，每个元素是一个路径 [[省ID, 市ID, 区ID], [省ID2], ...]
  const areaNames: string[] = []
  
  const findAreaByPath = (path: number[]): string => {
    let current: any = areaList.value
    const names: string[] = []
    
    for (const id of path) {
      const found = current.find((item: any) => item.id === id)
      if (found) {
        names.push(found.name)
        current = found.children || []
      }
    }
    
    return names.join('-')
  }
  
  for (const path of value) {
    areaNames.push(findAreaByPath(path))
  }
  
  // 将多个地区用逗号分隔存储
  formData.value.serviceArea = areaNames.join('、')
}

/**
 * 提交表单
 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as CarrierApi.CarrierVO
    delete (data as any).serviceAreaIds // 移除 serviceAreaIds
    
    if (formType.value === 'create') {
      await CarrierApi.createCarrier(data)
      message.success(t('common.createSuccess'))
    } else {
      await CarrierApi.updateCarrier(data)
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
    carrierName: undefined,
    carrierType: 1,
    contactPerson: undefined,
    contactPhone: undefined,
    contactEmail: undefined,
    serviceAreaIds: [],
    serviceArea: undefined,
    priceStandard: undefined,
    timeLimit: undefined,
    rating: 5.0,
    cooperationStartDate: undefined,
    status: 1,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
