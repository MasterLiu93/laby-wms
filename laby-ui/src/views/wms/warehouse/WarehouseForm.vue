<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-form-item :label="t('wms.warehouseName')" prop="warehouseName">
        <el-input v-model="formData.warehouseName" :placeholder="t('wms.warehouseNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('wms.warehouseType')" prop="warehouseType">
        <el-select v-model="formData.warehouseType" :placeholder="t('wms.warehouseTypePlaceholder')">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_WAREHOUSE_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.address')" prop="address">
        <el-input v-model="formData.address" :placeholder="t('wms.addressPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('wms.contactPerson')" prop="contactPerson">
        <el-input v-model="formData.contactPerson" :placeholder="t('wms.contactPersonPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('wms.contactPhone')" prop="contactPhone">
        <el-input v-model="formData.contactPhone" :placeholder="t('wms.contactPhonePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
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
      <el-form-item :label="t('form.remark')" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :placeholder="t('common.inputText')" />
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
import * as WarehouseApi from '@/api/wms/warehouse'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  warehouseName: '',
  warehouseType: undefined,
  address: '',
  contactPerson: '',
  contactPhone: '',
  status: 0,
  remark: ''
})
const formRules = reactive({
  warehouseName: [{ required: true, message: t('wms.warehouseNamePlaceholder'), trigger: 'blur' }],
  warehouseType: [{ required: true, message: t('wms.warehouseTypePlaceholder'), trigger: 'change' }],
  status: [{ required: true, message: t('common.required'), trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await WarehouseApi.getWarehouse(id)
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
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as WarehouseApi.WarehouseVO
    if (formType.value === 'create') {
      await WarehouseApi.createWarehouse(data)
      message.success(t('common.createSuccess'))
    } else {
      await WarehouseApi.updateWarehouse(data)
      message.success(t('common.updateSuccess'))
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
    warehouseName: '',
    warehouseType: undefined,
    address: '',
    contactPerson: '',
    contactPhone: '',
    status: 0,
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>

