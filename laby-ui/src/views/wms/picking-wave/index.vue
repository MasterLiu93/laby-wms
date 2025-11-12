<template>
  <ContentWrap v-if="showSearch">
    <!-- 搜索区域 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="auto"
    >
      <el-form-item :label="t('wms.waveNo')" prop="waveNo">
        <el-input
          v-model="queryParams.waveNo"
          :placeholder="t('wms.waveNoPlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
        <el-select
          v-model="queryParams.warehouseId"
          :placeholder="t('common.selectText')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="warehouse in warehouseList"
            :key="warehouse.id"
            :label="warehouse.warehouseName"
            :value="warehouse.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.waveType')" prop="waveType">
        <el-select
          v-model="queryParams.waveType"
          :placeholder="t('wms.waveTypePlaceholder')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_WAVE_TYPE)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.waveStatus')" prop="status">
        <el-select
          v-model="queryParams.status"
          :placeholder="t('wms.waveStatusPlaceholder')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_WAVE_STATUS)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.createTime')" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          :start-placeholder="t('common.startTimeText')"
          :end-placeholder="t('common.endTimeText')"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery" type="primary">
          <Icon icon="ep:search" class="mr-5px" />
          {{ t('common.query') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          {{ t('common.reset') }}
        </el-button>
        <el-button @click="openForm('create')" type="success" v-hasPermi="['wms:picking-wave:create']">
          <Icon icon="ep:plus" class="mr-5px" />
          {{ t('wms.addWave') }}
        </el-button>
        <el-button @click="handleGenerate" type="warning" v-hasPermi="['wms:picking-wave:create']">
          <Icon icon="ep:magic-stick" class="mr-5px" />
          {{ t('wms.autoGenerate') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  
  <!-- 列表区域 -->
  <ContentWrap>
    <!-- 表格工具栏 -->
    <div class="flex justify-between items-center mb-4">
      <div class="text-sm text-gray-600">
        {{ t('common.total') }}: {{ total }} {{ t('common.items') }}
      </div>
      <RightToolbar v-model:showSearch="showSearch" :columns="columns" :search="true" @queryTable="getList" />
    </div>
    
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column v-if="columns.waveNo.visible" :label="t('wms.waveNo')" prop="waveNo" min-width="180" show-overflow-tooltip fixed />
      <el-table-column v-if="columns.warehouseName.visible" :label="t('wms.warehouse')" prop="warehouseName" min-width="120" show-overflow-tooltip />
      <el-table-column v-if="columns.waveType.visible" :label="t('wms.waveType')" prop="waveType" min-width="100" align="center">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.WMS_WAVE_TYPE" :value="row.waveType" />
        </template>
      </el-table-column>
      <el-table-column v-if="columns.outboundNos.visible" :label="t('wms.relatedOutbounds')" prop="outboundNos" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.outboundNos && row.outboundNos.length > 0">
            {{ row.outboundNos.join(', ') }}
          </span>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.orderCount.visible" :label="t('wms.orderCount')" prop="orderCount" min-width="90" align="center" />
      <el-table-column v-if="columns.totalQuantity.visible" :label="t('wms.totalQuantity')" prop="totalQuantity" min-width="90" align="center" />
      <el-table-column v-if="columns.priority.visible" :label="t('wms.priority')" prop="priority" min-width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.priority === 3" type="danger">{{ t('wms.urgent') }}</el-tag>
          <el-tag v-else-if="row.priority === 2" type="warning">{{ t('wms.important') }}</el-tag>
          <el-tag v-else type="info">{{ t('wms.normal') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.pickerName.visible" :label="t('wms.picker')" prop="pickerName" min-width="100" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.pickerName">{{ row.pickerName }}</span>
          <el-tag v-else type="info">{{ t('wms.unassigned') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.status.visible" :label="t('wms.waveStatus')" prop="status" min-width="100" align="center">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.WMS_WAVE_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column
        v-if="columns.startTime.visible"
        :label="t('wms.startTime')"
        prop="startTime"
        min-width="160"
        :formatter="dateFormatter"
      />
      <el-table-column v-if="columns.endTime.visible" :label="t('wms.endTime')" prop="endTime" min-width="160" :formatter="dateFormatter" />
      <el-table-column v-if="columns.remark.visible" :label="t('form.remark')" prop="remark" min-width="150" show-overflow-tooltip />
      <el-table-column
        v-if="columns.createTime.visible"
        :label="t('common.createTime')"
        prop="createTime"
        min-width="160"
        :formatter="dateFormatter"
      />
      <el-table-column :label="t('action.action')" align="center" min-width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="openDetail(row.id)"
            v-hasPermi="['wms:picking-wave:query']"
          >
            {{ t('action.detail') }}
          </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('update', row.id)"
            v-hasPermi="['wms:picking-wave:update']"
            v-if="row.status === 1"
          >
            {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="success"
            @click="handleGenerateTasks(row)"
            v-hasPermi="['wms:picking-wave:create']"
            v-if="row.status === 1"
          >
            {{ t('wms.generateTasks') }}
          </el-button>
          <el-button
            link
            type="primary"
            @click="handleAssign(row)"
            v-hasPermi="['wms:picking-wave:update']"
            v-if="row.status === 1"
          >
            {{ t('wms.assignPicker') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleCancel(row.id)"
            v-hasPermi="['wms:picking-wave:update']"
            v-if="row.status !== 4 && row.status !== 5"
          >
            {{ t('action.cancel') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(row.id)"
            v-hasPermi="['wms:picking-wave:delete']"
            v-if="row.status === 1"
          >
            {{ t('action.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <!-- 表单弹窗 -->
    <PickingWaveForm ref="formRef" @success="getList" />

    <!-- 详情弹窗 -->
    <PickingWaveDetail ref="detailRef" />

    <!-- 分配拣货员弹窗 -->
    <el-dialog v-model="assignDialogVisible" :title="t('wms.assignPickerTitle')" width="600px" append-to-body>
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item :label="t('wms.waveNo')" prop="waveNo">
          <el-input v-model="assignForm.waveNo" disabled />
        </el-form-item>
        <el-form-item :label="t('wms.picker')" prop="pickerId">
          <el-select v-model="assignForm.pickerId" :placeholder="t('wms.pickerPlaceholder')" class="!w-full">
            <el-option
              v-for="user in pickerList"
              :key="user.id"
              :label="user.nickname"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitAssign" :loading="assignLoading">{{ t('common.ok') }}</el-button>
      </template>
    </el-dialog>

    <!-- 自动生成弹窗 -->
    <el-dialog v-model="generateDialogVisible" :title="t('wms.autoGenerateTitle')" width="600px" append-to-body>
      <el-form
        ref="generateFormRef"
        :model="generateForm"
        :rules="generateRules"
        label-width="100px"
      >
        <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
          <el-select v-model="generateForm.warehouseId" :placeholder="t('common.selectText')" class="!w-full">
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('wms.waveType')" prop="waveType">
          <el-select v-model="generateForm.waveType" :placeholder="t('wms.waveTypePlaceholder')" class="!w-full">
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.WMS_WAVE_TYPE)"
              :key="dict.value"
              :label="getDictLabel(dict)"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-alert
          :title="t('common.reminder')"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #default>
            <div>{{ t('wms.autoGenerateDesc') }}</div>
            <ul style="margin-top: 10px; padding-left: 20px">
              <li>{{ t('wms.batchPicking') }}</li>
              <li>{{ t('wms.zonePicking') }}</li>
              <li>{{ t('wms.singlePicking') }}</li>
            </ul>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitGenerate" :loading="generateLoading">{{ t('wms.confirmGenerate') }}</el-button>
      </template>
    </el-dialog>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'

const { getDictLabel } = useDictI18n() // 字典国际化
import { dateFormatter } from '@/utils/formatTime'
import { generatePickingTasks, generatePickingWaves } from '@/api/wms/pickingWave'
import * as PickingWaveApi from '@/api/wms/pickingWave'
import * as WarehouseApi from '@/api/wms/warehouse'
import * as UserApi from '@/api/system/user'
import PickingWaveForm from './PickingWaveForm.vue'
import PickingWaveDetail from './PickingWaveDetail.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

const { t } = useI18n()
const message = useMessage()

/** 列表数据 */
const loading = ref(true)
const list = ref([])
const total = ref(0)
const queryFormRef = ref()

// 列设置功能
const columns = reactive(createWMSColumns(t).pickingWave)
const showSearch = ref(true)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  waveNo: undefined,
  warehouseId: undefined,
  waveType: undefined,
  status: undefined,
  pickerId: undefined,
  createTime: []
})

/** 仓库列表 */
const warehouseList = ref([])

/** 拣货员列表 */
const pickerList = ref([])

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** 获取列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PickingWaveApi.getPickingWavePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 获取仓库列表 */
const getWarehouseList = async () => {
  const data = await WarehouseApi.getWarehouseSimpleList()
  warehouseList.value = data
}

/** 获取拣货员列表（查询所有用户，实际应根据角色筛选） */
const getPickerList = async () => {
  const data = await UserApi.getSimpleUserList()
  pickerList.value = data
}

/** 打开表单 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 打开详情 */
const detailRef = ref()
const openDetail = (id: number) => {
  detailRef.value.open(id)
}

/** 删除 */
const handleDelete = async (id: number) => {
  try {
    await message.confirm(t('wms.deleteConfirm'))
    await PickingWaveApi.deletePickingWave(id)
    message.success(t('wms.deleteSuccess'))
    await getList()
  } catch {}
}

/** 取消 */
const handleCancel = async (id: number) => {
  try {
    await message.confirm(t('wms.cancelConfirm'))
    await PickingWaveApi.cancelPickingWave(id)
    message.success(t('wms.cancelSuccess'))
    await getList()
  } catch {}
}

/** 分配拣货员 */
const assignDialogVisible = ref(false)
const assignFormRef = ref()
const assignLoading = ref(false)
const assignForm = reactive({
  id: undefined,
  waveNo: '',
  pickerId: undefined
})
const assignRules = {
  pickerId: [{ required: true, message: t('wms.pickerRequired'), trigger: 'change' }]
}

const handleAssign = (row: any) => {
  assignForm.id = row.id
  assignForm.waveNo = row.waveNo
  assignForm.pickerId = row.pickerId
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  await assignFormRef.value?.validate()
  assignLoading.value = true
  try {
    await PickingWaveApi.assignPickingWave(assignForm.id, assignForm.pickerId)
    message.success(t('wms.assignSuccess'))
    assignDialogVisible.value = false
    await getList()
  } finally {
    assignLoading.value = false
  }
}

/** 自动生成拣货波次 */
const generateDialogVisible = ref(false)
const generateFormRef = ref()
const generateLoading = ref(false)
const generateForm = reactive({
  warehouseId: undefined,
  waveType: 1
})
const generateRules = {
  warehouseId: [{ required: true, message: t('common.selectText'), trigger: 'change' }],
  waveType: [{ required: true, message: t('common.selectText'), trigger: 'change' }]
}

const handleGenerate = () => {
  generateForm.warehouseId = queryParams.warehouseId
  generateDialogVisible.value = true
}

const submitGenerate = async () => {
  await generateFormRef.value?.validate()
  generateLoading.value = true
  try {
    // 波次类型转换：1->BATCH, 2->ZONE, 3->SINGLE
    const waveTypeMap = { 1: 'BATCH', 2: 'ZONE', 3: 'SINGLE' }
    const waveType = waveTypeMap[generateForm.waveType] || 'BATCH'
    
    const waveIds = await generatePickingWaves(
      generateForm.warehouseId,
      waveType
    )
    message.success(t('wms.generateWavesSuccess').replace('{count}', waveIds.length))
    generateDialogVisible.value = false
    await getList()
  } finally {
    generateLoading.value = false
  }
}

/** 生成拣货任务 */
const handleGenerateTasks = async (row: any) => {
  try {
    await message.confirm(t('wms.generateTasksConfirm').replace('{waveNo}', row.waveNo), t('common.reminder'), {
      confirmButtonText: t('common.ok'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    
    const taskCount = await generatePickingTasks(row.id)
    message.success(t('wms.generateTasksSuccess').replace('{count}', taskCount))
    await getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('生成拣货任务失败', error)
    }
  }
}

/** 初始化 */
onMounted(() => {
  getList()
  getWarehouseList()
  getPickerList()
})
</script>
