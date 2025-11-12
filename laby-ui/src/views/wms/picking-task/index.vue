<template>
  <ContentWrap v-if="showSearch">
    <!-- 搜索工作栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="88px"
    >
      <el-form-item :label="t('wms.taskNo')" prop="taskNo">
        <el-input
          v-model="queryParams.taskNo"
          class="!w-240px"
          clearable
          :placeholder="t('wms.taskNoPlaceholder')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('wms.outboundNo')" prop="outboundNo">
        <el-input
          v-model="queryParams.outboundNo"
          class="!w-240px"
          clearable
          :placeholder="t('wms.outboundNoPlaceholder')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" class="!w-240px" clearable :placeholder="t('common.selectText')">
          <el-option
            v-for="warehouse in warehouseList"
            :key="warehouse.id"
            :label="warehouse.warehouseName"
            :value="warehouse.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.picker')" prop="pickerId">
        <el-select v-model="queryParams.pickerId" class="!w-240px" clearable :placeholder="t('wms.pickerPlaceholder')">
          <el-option
            v-for="user in pickerList"
            :key="user.id"
            :label="user.nickname"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.taskStatus')" prop="status">
        <el-select v-model="queryParams.status" class="!w-240px" clearable :placeholder="t('wms.statusPlaceholder')">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_PICKING_TASK_STATUS)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.createTime')" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
          :end-placeholder="t('common.endTimeText')"
          :start-placeholder="t('common.startTimeText')"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          {{ t('common.query') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          {{ t('common.reset') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <!-- 表格工具栏 -->
    <div class="flex justify-between items-center mb-4">
      <div class="text-sm text-gray-600">
        {{ t('common.total') }}: {{ total }} {{ t('common.items') }}
      </div>
      <RightToolbar 
        v-model:showSearch="showSearch"
        :columns="columns"
        :search="true"
        @queryTable="getList"
      />
    </div>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column v-if="columns.taskNo.visible" align="center" :label="t('wms.taskNo')" min-width="160" prop="taskNo" show-overflow-tooltip />
      <el-table-column align="center" :label="t('wms.outboundNo')" min-width="160" prop="outboundNo" show-overflow-tooltip />
      <el-table-column v-if="columns.goodsName.visible" align="center" :label="t('wms.goodsInfo')" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <div>{{ row.goodsName }}</div>
          <div class="text-xs text-gray-500">SKU: {{ row.skuCode }}</div>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.locationCode.visible" align="center" :label="t('wms.location')" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <div v-if="row.rowNo">
            <div class="font-semibold">
              {{ t('wms.locationFormat').replace('{row}', row.rowNo).replace('{column}', row.columnNo).replace('{layer}', row.layerNo) }}
            </div>
            <div class="text-xs text-gray-400">
              {{ row.locationCode }}
            </div>
          </div>
          <span v-else>{{ row.locationCode || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" :label="t('wms.batchNo')" min-width="140" prop="batchNo" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.batchNo || '-' }}
        </template>
      </el-table-column>
      <el-table-column v-if="columns.quantity.visible" align="center" :label="t('wms.planQuantity')" min-width="100" prop="planQuantity">
        <template #default="{ row }">
          <span class="font-medium">{{ row.planQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.pickedQuantity.visible" align="center" :label="t('wms.actualQuantity')" min-width="100" prop="actualQuantity">
        <template #default="{ row }">
          <span v-if="row.actualQuantity" class="text-primary font-medium">{{ row.actualQuantity }}</span>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.assignedUser.visible" align="center" :label="t('wms.picker')" min-width="100" prop="pickerName">
        <template #default="{ row }">
          {{ row.pickerName || '-' }}
        </template>
      </el-table-column>
      <el-table-column v-if="columns.taskStatus.visible" align="center" :label="t('wms.taskStatus')" min-width="100" prop="status">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.WMS_PICKING_TASK_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        :label="t('wms.pickingTime')"
        min-width="155"
        prop="pickingTime"
      >
        <template #default="{ row }">
          {{ row.pickingTime ? dateFormatter(row.pickingTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column
        v-if="columns.createTime.visible"
        :formatter="dateFormatter"
        align="center"
        :label="t('common.createTime')"
        min-width="155"
        prop="createTime"
      />
      <el-table-column align="center" fixed="right" :label="t('action.action')" min-width="180">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['wms:picking-task:query']"
            link
            type="primary"
            @click="handleDetail(row)"
          >
            {{ t('action.detail') }}
          </el-button>
          <el-button
            v-if="row.status === 1"
            v-hasPermi="['wms:picking-task:execute']"
            link
            type="primary"
            @click="handleAssign(row)"
          >
            {{ t('wms.assignPicker') }}
          </el-button>
          <el-button
            v-if="row.status === 1 || row.status === 2"
            v-hasPermi="['wms:picking-task:execute']"
            link
            type="success"
            @click="handlePick(row)"
          >
            {{ t('wms.executePick') }}
          </el-button>
          <el-button
            v-if="row.status === 4"
            v-hasPermi="['wms:picking-task:exception']"
            link
            type="warning"
            @click="handleException(row)"
          >
            {{ t('wms.viewException') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 任务详情弹窗 -->
  <el-dialog v-model="detailVisible" :title="t('wms.taskDetail')" width="800px">
    <el-descriptions v-if="currentTask" :column="2" border>
      <el-descriptions-item :label="t('wms.taskNo')">{{ currentTask.taskNo }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.outboundNo')">{{ currentTask.outboundNo }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.warehouse')">{{ currentTask.warehouseName }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.location')">{{ currentTask.locationCode }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.goodsName')">{{ currentTask.goodsName }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.skuCode')">{{ currentTask.skuCode }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.batchNo')">{{ currentTask.batchNo || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.sortOrder')">{{ currentTask.sortOrder || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.planQuantity')">
        <span class="font-medium">{{ currentTask.planQuantity }}</span>
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.actualQuantity')">
        <span v-if="currentTask.actualQuantity" class="text-primary font-medium">
          {{ currentTask.actualQuantity }}
        </span>
        <span v-else class="text-gray-400">-</span>
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.picker')">{{ currentTask.pickerName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.pickingTime')">
        {{ currentTask.pickingTime ? dateFormatter(currentTask.pickingTime) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.taskStatus')">
        <dict-tag :type="DICT_TYPE.WMS_PICKING_TASK_STATUS" :value="currentTask.status" />
      </el-descriptions-item>
      <el-descriptions-item v-if="currentTask.exceptionType" :label="t('wms.exceptionType')">
        <dict-tag :type="DICT_TYPE.WMS_PICKING_EXCEPTION_TYPE" :value="currentTask.exceptionType" />
      </el-descriptions-item>
      <el-descriptions-item v-if="currentTask.exceptionRemark" :label="t('wms.exceptionRemark')" :span="2">
        {{ currentTask.exceptionRemark }}
      </el-descriptions-item>
      <el-descriptions-item v-if="currentTask.remark" :label="t('form.remark')" :span="2">
        {{ currentTask.remark }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('common.createTime')">
        {{ dateFormatter(currentTask.createTime) }}
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>

  <!-- 分配拣货员弹窗 -->
  <el-dialog v-model="assignVisible" :title="t('wms.assignPickerTitle')" width="500px">
    <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
      <el-form-item :label="t('wms.picker')" prop="pickerId">
        <el-select v-model="assignForm.pickerId" class="w-full" :placeholder="t('wms.pickerPlaceholder')">
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
      <el-button @click="assignVisible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="submitAssign">{{ t('common.ok') }}</el-button>
    </template>
  </el-dialog>

  <!-- 拣货操作弹窗 -->
  <el-dialog v-model="pickVisible" :title="t('wms.executePickTitle')" width="600px">
    <el-form ref="pickFormRef" :model="pickForm" :rules="pickRules" label-width="120px">
      <el-form-item :label="t('wms.warehouse')">
        {{ currentTask?.warehouseName }}
      </el-form-item>
      <el-form-item :label="t('wms.goodsInfo')">
        <div>{{ currentTask?.goodsName }}</div>
        <div class="text-xs text-gray-500">SKU: {{ currentTask?.skuCode }}</div>
      </el-form-item>
      <el-form-item :label="t('wms.area')">
        {{ currentTask?.areaName || '-' }}
      </el-form-item>
      <el-form-item :label="t('wms.location')">
        <div v-if="currentTask?.rowNo">
          <div class="font-semibold">
            {{ t('wms.locationFormat').replace('{row}', currentTask.rowNo).replace('{column}', currentTask.columnNo).replace('{layer}', currentTask.layerNo) }}
          </div>
          <div class="text-xs text-gray-400">
            {{ currentTask.locationCode }}
          </div>
        </div>
        <span v-else>{{ currentTask?.locationCode }}</span>
      </el-form-item>
      <el-form-item :label="t('wms.batchNo')">
        {{ currentTask?.batchNo || '-' }}
      </el-form-item>
      <el-form-item :label="t('wms.planPickingQuantity')">
        <span class="font-medium text-lg">{{ currentTask?.planQuantity }}</span>
      </el-form-item>
      <el-form-item :label="t('wms.actualPickingQuantity')" prop="actualQuantity">
        <el-input-number
          v-model="pickForm.actualQuantity"
          :max="currentTask?.planQuantity"
          :min="0"
          :precision="2"
          :step="1"
          class="w-full"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item :label="t('wms.hasException')">
        <el-switch v-model="hasException" />
      </el-form-item>
      <template v-if="hasException">
        <el-form-item :label="t('wms.exceptionType')" prop="exceptionType">
          <el-select v-model="pickForm.exceptionType" class="w-full" :placeholder="t('wms.exceptionTypePlaceholder')">
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.WMS_PICKING_EXCEPTION_TYPE)"
              :key="dict.value"
              :label="getDictLabel(dict)"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('wms.exceptionRemark')" prop="exceptionRemark">
          <el-input
            v-model="pickForm.exceptionRemark"
            :rows="3"
            :placeholder="t('wms.exceptionRemarkPlaceholder')"
            type="textarea"
          />
        </el-form-item>
      </template>
      <el-form-item :label="t('form.remark')">
        <el-input
          v-model="pickForm.remark"
          :rows="2"
          :placeholder="t('form.remarkPlaceholder')"
          type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pickVisible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="submitPick">{{ t('wms.confirmPick') }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'

const { getDictLabel } = useDictI18n() // 字典国际化
import * as PickingTaskApi from '@/api/wms/pickingTask'
import * as WarehouseApi from '@/api/wms/warehouse'
import * as UserApi from '@/api/system/user'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

defineOptions({ name: 'WmsPickingTask' })

// ...
const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  taskNo: undefined,
  outboundNo: undefined,
  warehouseId: undefined,
  pickerId: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref()

// 列设置功能
const columns = reactive(createWMSColumns(t).pickingTask)
const showSearch = ref(true)

// 仓库列表
const warehouseList = ref([])
// 拣货员列表
const pickerList = ref([])

// 详情弹窗
const detailVisible = ref(false)
const currentTask = ref(null)

// 分配弹窗
const assignVisible = ref(false)
const assignForm = reactive({
  pickerId: undefined
})
const assignFormRef = ref()
const assignRules = {
  pickerId: [{ required: true, message: t('wms.pickerRequired'), trigger: 'change' }]
}

// 拣货弹窗
const pickVisible = ref(false)
const pickForm = reactive({
  id: undefined,
  actualQuantity: undefined,
  exceptionType: undefined,
  exceptionRemark: undefined,
  remark: undefined
})
const pickFormRef = ref()
const hasException = ref(false)
const pickRules = {
  actualQuantity: [{ required: true, message: t('wms.actualQuantityRequired'), trigger: 'blur' }],
  exceptionType: [
    { required: true, message: t('wms.exceptionTypePlaceholder'), trigger: 'change' },
    { validator: (rule, value, callback) => {
      if (hasException.value && !value) {
        callback(new Error(t('wms.exceptionTypePlaceholder')))
      } else {
        callback()
      }
    }, trigger: 'change' }
  ]
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PickingTaskApi.getPickingTaskPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 查看详情 */
const handleDetail = (row) => {
  currentTask.value = row
  detailVisible.value = true
}

/** 分配拣货员 */
const handleAssign = (row) => {
  currentTask.value = row
  assignForm.pickerId = row.pickerId
  assignVisible.value = true
}

/** 提交分配 */
const submitAssign = async () => {
  await assignFormRef.value.validate()
  const picker = pickerList.value.find(u => u.id === assignForm.pickerId)
  await PickingTaskApi.assignPickingTasks([currentTask.value.id], assignForm.pickerId, picker.nickname)
  message.success(t('wms.assignSuccess'))
  assignVisible.value = false
  getList()
}

/** 执行拣货 */
const handlePick = (row) => {
  currentTask.value = row
  pickForm.id = row.id
  pickForm.actualQuantity = row.planQuantity
  pickForm.exceptionType = undefined
  pickForm.exceptionRemark = undefined
  pickForm.remark = undefined
  hasException.value = false
  pickVisible.value = true
}

/** 提交拣货 */
const submitPick = async () => {
  await pickFormRef.value.validate()
  if (!hasException.value) {
    pickForm.exceptionType = undefined
    pickForm.exceptionRemark = undefined
  }
  await PickingTaskApi.executePicking(pickForm)
  message.success(t('wms.pickSuccess'))
  pickVisible.value = false
  getList()
}

/** 查看异常 */
const handleException = (row) => {
  currentTask.value = row
  detailVisible.value = true
}

/** 初始化 */
onMounted(async () => {
  // 加载仓库列表
  const warehouseData = await WarehouseApi.getWarehouseSimpleList()
  warehouseList.value = warehouseData
  
  // 加载拣货员列表（假设有专门的拣货员角色）
  const userData = await UserApi.getSimpleUserList()
  pickerList.value = userData
  
  // 加载列表
  getList()
})
</script>
