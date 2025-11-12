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
        <el-select
          v-model="formData.warehouseId"
          :placeholder="t('common.selectText')"
          clearable
          @change="handleWarehouseChange"
          class="!w-full"
        >
          <el-option
            v-for="warehouse in warehouseList"
            :key="warehouse.id"
            :label="warehouse.warehouseName"
            :value="warehouse.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.area')" prop="areaId">
        <el-select v-model="formData.areaId" :placeholder="t('common.selectText')" clearable class="!w-full">
          <el-option
            v-for="area in areaList"
            :key="area.id"
            :label="area.areaName"
            :value="area.id"
          />
        </el-select>
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item :label="t('wms.rowNo')" prop="rowNo">
            <el-input-number v-model="formData.rowNo" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="t('wms.columnNo')" prop="columnNo">
            <el-input-number v-model="formData.columnNo" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="t('wms.layerNo')" prop="layerNo">
            <el-input-number v-model="formData.layerNo" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="t('wms.locationType')" prop="locationType">
        <el-select
          v-model="formData.locationType"
          :placeholder="t('wms.locationTypePlaceholder')"
          clearable
          class="!w-full"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_WAREHOUSE_LOCATION_TYPE)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item :label="t('wms.capacity')" prop="capacity">
            <el-input-number v-model="formData.capacity" :min="0" :precision="2" :placeholder="t('wms.capacityPlaceholder')" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('wms.maxWeight')" prop="maxWeight">
            <el-input-number
              v-model="formData.maxWeight"
              :min="0"
              :precision="2"
              :placeholder="t('wms.maxWeightPlaceholder')"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="formData.status" :placeholder="t('common.selectText')" class="!w-full">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_LOCATION_STATUS)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
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
import * as WarehouseLocationApi from '@/api/wms/location'
import * as WarehouseApi from '@/api/wms/warehouse'
import * as WarehouseAreaApi from '@/api/wms/area'

/** 库位管理 表单 */
defineOptions({ name: 'WarehouseLocationForm' })

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
  areaId: undefined,
  rowNo: undefined,
  columnNo: undefined,
  layerNo: undefined,
  locationType: 1,
  capacity: undefined,
  maxWeight: undefined,
  status: 1,
  remark: undefined
})
const formRules = reactive({
  warehouseId: [{ required: true, message: t('common.selectText'), trigger: 'change' }],
  areaId: [{ required: true, message: t('common.selectText'), trigger: 'change' }],
  rowNo: [{ required: true, message: t('wms.rowNoPlaceholder'), trigger: 'blur' }],
  columnNo: [{ required: true, message: t('wms.columnNoPlaceholder'), trigger: 'blur' }],
  layerNo: [{ required: true, message: t('wms.layerNoPlaceholder'), trigger: 'blur' }],
  locationType: [{ required: true, message: t('wms.locationTypePlaceholder'), trigger: 'change' }],
  capacity: [{ required: true, message: t('wms.capacityPlaceholder'), trigger: 'blur' }],
  maxWeight: [{ required: true, message: t('wms.maxWeightPlaceholder'), trigger: 'blur' }],
  status: [{ required: true, message: t('common.required'), trigger: 'change' }]
})
const formRef = ref() // 表单 Ref
const warehouseList = ref([]) // 仓库列表
const areaList = ref([]) // 库区列表

/** 仓库切换 */
const handleWarehouseChange = async (warehouseId: number) => {
  formData.value.areaId = undefined
  if (warehouseId) {
    // 加载库区列表
    areaList.value = await WarehouseAreaApi.getWarehouseAreaListByWarehouseId(warehouseId)
  } else {
    areaList.value = []
  }
}

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
      formData.value = await WarehouseLocationApi.getWarehouseLocation(id)
      // 加载库区列表
      if (formData.value.warehouseId) {
        areaList.value = await WarehouseAreaApi.getWarehouseAreaListByWarehouseId(formData.value.warehouseId)
      }
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
    const data = formData.value as unknown as WarehouseLocationApi.WarehouseLocationVO
    if (formType.value === 'create') {
      await WarehouseLocationApi.createWarehouseLocation(data)
      message.success(t('common.createSuccess'))
    } else {
      await WarehouseLocationApi.updateWarehouseLocation(data)
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
    areaId: undefined,
    rowNo: undefined,
    columnNo: undefined,
    layerNo: undefined,
    locationType: 1,
    capacity: undefined,
    maxWeight: undefined,
    status: 1,
    remark: undefined
  }
  areaList.value = []
  formRef.value?.resetFields()
}
</script>

