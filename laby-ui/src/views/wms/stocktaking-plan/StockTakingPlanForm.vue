<!--
  盘点计划表单组件
  
  功能说明：
  1. 支持新增和编辑盘点计划
  2. 表单验证
  3. 成功后通知父组件刷新列表
  
  @author laby
  @date 2025-10-28
-->
<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px" v-loading="formLoading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item :label="t('wms.planName')" prop="planName">
            <el-input v-model="formData.planName" :placeholder="t('wms.planNamePlaceholder')" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
            <el-select v-model="formData.warehouseId" :placeholder="t('common.selectText')" class="!w-full">
              <el-option
                v-for="warehouse in warehouseList"
                :key="warehouse.id"
                :label="warehouse.warehouseName"
                :value="warehouse.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item :label="t('wms.takingType')" prop="takingType">
            <el-select v-model="formData.stockTakingType" :placeholder="t('wms.stockTakingTypePlaceholder')" class="!w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_STOCK_TAKING_TYPE)"
                :key="dict.value"
                :label="getDictLabel(dict)"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.scopeType')" prop="scopeType">
            <el-select v-model="formData.scopeType" :placeholder="t('wms.scopeTypePlaceholder')" class="!w-full">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.WMS_STOCK_TAKING_SCOPE_TYPE)"
                :key="dict.value"
                :label="getDictLabel(dict)"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item :label="t('wms.planStartTime')" prop="planStartTime">
            <el-date-picker
              v-model="formData.planStartTime"
              type="datetime"
              :placeholder="t('common.selectDateTimeText')"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.planEndTime')" prop="planEndTime">
            <el-date-picker
              v-model="formData.planEndTime"
              type="datetime"
              :placeholder="t('common.selectDateTimeText')"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item :label="t('form.remark')" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" :placeholder="t('form.remarkPlaceholder')" />
      </el-form-item>
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
import { formatDate } from '@/utils/formatTime'
import * as StockTakingPlanApi from '@/api/wms/stockTakingPlan'
import * as WarehouseApi from '@/api/wms/warehouse'

const { getDictLabel } = useDictI18n() // 字典国际化

defineOptions({ name: 'StockTakingPlanForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const warehouseList = ref([])

const formData = ref({
  id: undefined,
  planName: '',
  warehouseId: undefined,
  takingType: 1,
  scopeType: 1,
  planStartTime: '',
  planEndTime: '',
  remark: ''
})

const formRules = reactive({
  planName: [{ required: true, message: t('wms.planNameRequired'), trigger: 'blur' }],
  warehouseId: [{ required: true, message: t('wms.warehouseRequired'), trigger: 'change' }],
  takingType: [{ required: true, message: t('wms.takingTypeRequired'), trigger: 'change' }],
  scopeType: [{ required: true, message: t('wms.scopeTypeRequired'), trigger: 'change' }],
  planStartTime: [{ required: true, message: t('wms.planStartTimeRequired'), trigger: 'change' }],
  planEndTime: [{ required: true, message: t('wms.planEndTimeRequired'), trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? t('action.add') : t('action.edit')
  formType.value = type
  resetForm()

  if (id) {
    formLoading.value = true
    try {
      const data = await StockTakingPlanApi.getStockTakingPlan(id)
      // 格式化时间字段
      formData.value = {
        ...data,
        planStartTime: data.planStartTime ? formatDate(data.planStartTime, 'YYYY-MM-DD HH:mm:ss') : '',
        planEndTime: data.planEndTime ? formatDate(data.planEndTime, 'YYYY-MM-DD HH:mm:ss') : ''
      }
    } finally {
      formLoading.value = false
    }
  }

  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as StockTakingPlanApi.StockTakingPlanVO
    if (formType.value === 'create') {
      await StockTakingPlanApi.createStockTakingPlan(data)
      message.success(t('common.createSuccess'))
    } else {
      await StockTakingPlanApi.updateStockTakingPlan(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    planName: '',
    warehouseId: undefined,
    takingType: 1,
    scopeType: 1,
    planStartTime: '',
    planEndTime: '',
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>
