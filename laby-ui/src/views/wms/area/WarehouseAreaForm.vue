<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
        <el-select v-model="formData.warehouseId" :placeholder="t('common.selectText')" clearable class="!w-full">
          <el-option
            v-for="warehouse in warehouseList"
            :key="warehouse.id"
            :label="warehouse.warehouseName"
            :value="warehouse.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.areaName')" prop="areaName">
        <el-input v-model="formData.areaName" :placeholder="t('wms.areaNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('wms.areaType')" prop="areaType">
        <el-select v-model="formData.areaType" :placeholder="t('wms.areaTypePlaceholder')" clearable class="!w-full">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_WAREHOUSE_AREA_TYPE)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.floor')" prop="floor">
        <el-input-number v-model="formData.floor" :min="1" :placeholder="t('wms.floorPlaceholder')" class="!w-full" />
      </el-form-item>
      <el-form-item :label="t('wms.areaSize')" prop="areaSize">
        <el-input-number v-model="formData.areaSize" :min="0" :precision="2" :placeholder="t('wms.areaSizePlaceholder')" class="!w-full" />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
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
import { useDictI18n } from '@/hooks/web/useDictI18n'
import * as WarehouseAreaApi from '@/api/wms/area'
import * as WarehouseApi from '@/api/wms/warehouse'

/** 库区管理 表单 */
defineOptions({ name: 'WarehouseAreaForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const { getDictLabel } = useDictI18n() // 字典国际化

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  warehouseId: undefined,
  areaName: undefined,
  areaType: undefined,
  floor: 1,
  areaSize: undefined,
  status: 1,
  remark: undefined
})
const formRules = reactive({
  warehouseId: [{ required: true, message: t('common.selectText'), trigger: 'change' }],
  areaName: [{ required: true, message: t('wms.areaNamePlaceholder'), trigger: 'blur' }],
  areaType: [{ required: true, message: t('wms.areaTypePlaceholder'), trigger: 'change' }],
  areaSize: [{ required: true, message: t('wms.areaSizePlaceholder'), trigger: 'blur' }],
  status: [{ required: true, message: t('common.required'), trigger: 'change' }]
})
const formRef = ref() // 表单 Ref
const warehouseList = ref([]) // 仓库列表

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
      formData.value = await WarehouseAreaApi.getWarehouseArea(id)
    } finally {
      formLoading.value = false
    }
  }
  // 加载仓库列表
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as WarehouseAreaApi.WarehouseAreaVO
    if (formType.value === 'create') {
      await WarehouseAreaApi.createWarehouseArea(data)
      message.success(t('common.createSuccess'))
    } else {
      await WarehouseAreaApi.updateWarehouseArea(data)
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
    warehouseId: undefined,
    areaName: undefined,
    areaType: undefined,
    floor: 1,
    areaSize: undefined,
    status: 1,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

