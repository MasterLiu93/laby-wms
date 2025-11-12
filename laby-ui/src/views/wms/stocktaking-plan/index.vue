<!--
  盘点计划列表页
  
  功能说明：
  1. 盘点计划的查询、新增、编辑、删除
  2. 盘点计划的审核、取消操作
  3. 支持多条件搜索和分页显示
  
  @author laby
  @date 2025-10-28
-->
<template>
  <ContentWrap v-if="showSearch">
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item :label="t('wms.planNo')" prop="planNo">
        <el-input v-model="queryParams.planNo" :placeholder="t('wms.planNoPlaceholder')" clearable class="!w-240px" />
      </el-form-item>

      <el-form-item :label="t('wms.planName')" prop="planName">
        <el-input v-model="queryParams.planName" :placeholder="t('wms.planNamePlaceholder')" clearable class="!w-240px" />
      </el-form-item>

      <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" :placeholder="t('common.selectText')" clearable class="!w-240px">
          <el-option
            v-for="warehouse in warehouseList"
            :key="warehouse.id"
            :label="warehouse.warehouseName"
            :value="warehouse.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('wms.takingType')" prop="takingType">
        <el-select v-model="queryParams.takingType" :placeholder="t('wms.takingTypePlaceholder')" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_STOCK_TAKING_TYPE)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('wms.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('common.selectText')" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_STOCK_TAKING_PLAN_STATUS)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button @click="handleQuery">{{ t('common.query') }}</el-button>
        <el-button @click="resetQuery">{{ t('common.reset') }}</el-button>
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['wms:stocktaking-plan:create']">
          {{ t('action.add') }}
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
    
    <el-table v-loading="loading" :data="list">
      <el-table-column v-if="columns.planNo.visible" :label="t('wms.planNo')" prop="planNo" width="160" align="center" />
      <el-table-column v-if="columns.planName.visible" :label="t('wms.planName')" prop="planName" min-width="180" show-overflow-tooltip />
      <el-table-column v-if="columns.warehouseName.visible" :label="t('wms.warehouse')" prop="warehouseName" width="120" align="center" />
      <el-table-column v-if="columns.takingType.visible" :label="t('wms.takingType')" prop="takingType" width="100" align="center">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_STOCK_TAKING_TYPE" :value="scope.row.takingType" />
        </template>
      </el-table-column>
      <el-table-column v-if="columns.scopeType.visible" :label="t('wms.scopeType')" prop="scopeType" width="100" align="center">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_STOCK_TAKING_SCOPE_TYPE" :value="scope.row.scopeType" />
        </template>
      </el-table-column>
      <el-table-column v-if="columns.planTime.visible" :label="t('wms.planTime')" width="180" align="center">
        <template #default="scope">
          <div>{{ formatDate(scope.row.planStartTime) }}</div>
          <div>{{ t('wms.planTimeTo') }} {{ formatDate(scope.row.planEndTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.progress.visible" :label="t('wms.progress')" width="120" align="center">
        <template #default="scope">
          {{ scope.row.completedCount || 0 }} / {{ scope.row.totalCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column v-if="columns.diffCount.visible" :label="t('wms.diffCount')" prop="diffCount" width="80" align="center" />
      <el-table-column v-if="columns.status.visible" :label="t('wms.status')" prop="status" width="100" align="center" show-overflow-tooltip>
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_STOCK_TAKING_PLAN_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column v-if="columns.createTime.visible" :label="t('common.createTime')" prop="createTime" width="160" align="center" :formatter="dateFormatter" />
      <el-table-column :label="t('action.action')" fixed="right" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['wms:stocktaking-plan:update']" v-if="scope.row.status === 1">
            {{ t('action.edit') }}
          </el-button>
          <el-button link type="success" @click="handleAudit(scope.row.id)" v-hasPermi="['wms:stocktaking-plan:audit']" v-if="scope.row.status === 1">
            {{ t('action.audit') }}
          </el-button>
          <el-button link type="warning" @click="handleCancel(scope.row.id)" v-hasPermi="['wms:stocktaking-plan:cancel']" v-if="scope.row.status === 1 || scope.row.status === 2 || scope.row.status === 3">
            {{ t('action.cancel') }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['wms:stocktaking-plan:delete']" v-if="scope.row.status === 1">
            {{ t('action.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <StockTakingPlanForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'

const { getDictLabel } = useDictI18n() // 字典国际化
import { formatDate } from '@/utils/formatTime'
import * as StockTakingPlanApi from '@/api/wms/stockTakingPlan'
import * as WarehouseApi from '@/api/wms/warehouse'
import StockTakingPlanForm from './StockTakingPlanForm.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

defineOptions({ name: 'WmsStocktakingPlan' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref([])
const total = ref(0)
const warehouseList = ref([])

// 列设置功能
const columns = reactive(createWMSColumns(t).stocktakingPlan)
const showSearch = ref(true)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  planNo: undefined,
  planName: undefined,
  warehouseId: undefined,
  takingType: undefined,
  status: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await StockTakingPlanApi.getStockTakingPlanPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleAudit = async (id: number) => {
  try {
    await message.confirm(t('wms.auditConfirm'))
    await StockTakingPlanApi.auditStockTakingPlan(id)
    message.success(t('wms.auditSuccess'))
    await getList()
  } catch {}
}

const handleCancel = async (id: number) => {
  try {
    await message.confirm(t('wms.cancelConfirm'))
    await StockTakingPlanApi.cancelStockTakingPlan(id)
    message.success(t('wms.cancelSuccess'))
    await getList()
  } catch {}
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await StockTakingPlanApi.deleteStockTakingPlan(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const dateFormatter = (row, column, cellValue) => {
  if (!cellValue) return '-'
  return formatDate(cellValue)
}

onMounted(async () => {
  await getList()
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
})
</script>
