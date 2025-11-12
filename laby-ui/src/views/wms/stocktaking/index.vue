<!--
  盘点单列表页
  
  功能说明：
  1. 盘点单的查询、新增、编辑、删除
  2. 盘点单的提交盘点、复核、调整库存操作
  3. 支持多条件搜索和分页显示
  
  @author laby
  @date 2025-10-28
-->
<template>
  <ContentWrap v-if="showSearch">
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item :label="t('wms.takingNo')" prop="takingNo">
        <el-input v-model="queryParams.takingNo" :placeholder="t('wms.takingNoPlaceholder')" clearable class="!w-240px" />
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

      <el-form-item :label="t('wms.goodsName')" prop="goodsName">
        <el-input v-model="queryParams.goodsName" :placeholder="t('wms.goodsNamePlaceholder')" clearable class="!w-240px" />
      </el-form-item>

      <el-form-item :label="t('wms.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('common.selectText')" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_STOCK_TAKING_STATUS)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button @click="handleQuery">{{ t('common.query') }}</el-button>
        <el-button @click="resetQuery">{{ t('common.reset') }}</el-button>
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['wms:stocktaking:create']">
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
      <el-table-column v-if="columns.takingNo.visible" :label="t('wms.takingNo')" prop="takingNo" width="160" align="center" />
      <el-table-column v-if="columns.planNo.visible" :label="t('wms.planNo')" prop="planNo" width="160" align="center" />
      <el-table-column v-if="columns.warehouseName.visible" :label="t('wms.warehouse')" prop="warehouseName" width="120" align="center" />
      <el-table-column v-if="columns.locationCode.visible" :label="t('wms.locationCode')" prop="locationCode" width="120" align="center" />
      <el-table-column v-if="columns.goodsName.visible" :label="t('wms.goodsName')" prop="goodsName" min-width="180" show-overflow-tooltip />
      <el-table-column v-if="columns.skuCode.visible" :label="t('wms.skuCode')" prop="skuCode" width="140" align="center" />
      <el-table-column v-if="columns.batchNo.visible" :label="t('wms.batchNo')" prop="batchNo" width="120" align="center" />
      <el-table-column v-if="columns.bookQuantity.visible" :label="t('wms.bookQuantity')" prop="bookQuantity" width="100" align="center" />
      <el-table-column v-if="columns.actualQuantity.visible" :label="t('wms.actualQuantity')" prop="actualQuantity" width="100" align="center" />
      <el-table-column v-if="columns.diffQuantity.visible" :label="t('wms.diffQuantity')" prop="diffQuantity" width="100" align="center">
        <template #default="scope">
          <span :class="{ 'text-red-500': scope.row.diffQuantity && scope.row.diffQuantity < 0, 'text-green-500': scope.row.diffQuantity && scope.row.diffQuantity > 0 }">
            {{ scope.row.diffQuantity || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.status.visible" :label="t('wms.status')" prop="status" width="100" align="center" show-overflow-tooltip>
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_STOCK_TAKING_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column v-if="columns.operator.visible" :label="t('wms.operator')" prop="operator" width="100" align="center" />
      <el-table-column v-if="columns.createTime.visible" :label="t('common.createTime')" prop="createTime" width="160" align="center" :formatter="dateFormatter" />
      <el-table-column :label="t('action.action')" fixed="right" width="220" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-if="scope.row.status === 1">
            {{ t('action.edit') }}
          </el-button>
          <el-button link type="success" @click="handleSubmit(scope.row)" v-if="scope.row.status === 1">
            {{ t('wms.submitStocktaking') }}
          </el-button>
          <el-button link type="warning" @click="handleReview(scope.row.id)" v-if="scope.row.status === 2">
            {{ t('wms.reviewStocktaking') }}
          </el-button>
          <el-button link type="primary" @click="handleAdjust(scope.row.id)" v-if="scope.row.status === 3">
            {{ t('wms.adjustInventory') }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-if="scope.row.status === 1">
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
  <StockTakingForm ref="formRef" @success="getList" />

  <!-- 提交盘点弹窗 -->
  <Dialog :title="t('wms.submitStocktakingTitle')" v-model="submitDialogVisible" width="500px">
    <el-form :model="submitForm" label-width="100px">
      <el-form-item :label="t('wms.bookQuantity')">
        <el-input v-model="submitForm.bookQuantity" disabled />
      </el-form-item>
      <el-form-item :label="t('wms.actualQuantity')">
        <el-input-number v-model="submitForm.actualQuantity" :min="0" :precision="2" class="!w-full" />
      </el-form-item>
      <el-form-item :label="t('wms.diffReason')">
        <el-input v-model="submitForm.diffReason" type="textarea" :rows="3" :placeholder="t('wms.diffReasonPlaceholder')" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="confirmSubmit" type="primary">{{ t('common.ok') }}</el-button>
      <el-button @click="submitDialogVisible = false">{{ t('common.cancel') }}</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'

const { getDictLabel } = useDictI18n() // 字典国际化
import { formatDate } from '@/utils/formatTime'
import * as StockTakingApi from '@/api/wms/stockTaking'
import * as WarehouseApi from '@/api/wms/warehouse'
import StockTakingForm from './StockTakingForm.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

defineOptions({ name: 'WmsStocktaking' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref([])
const total = ref(0)

// 列设置功能
const columns = reactive(createWMSColumns(t).stocktaking)
const showSearch = ref(true)
const warehouseList = ref([])
const submitDialogVisible = ref(false)
const submitForm = ref({
  id: undefined,
  bookQuantity: 0,
  actualQuantity: 0,
  diffReason: ''
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  takingNo: undefined,
  warehouseId: undefined,
  goodsName: undefined,
  status: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await StockTakingApi.getStockTakingPage(queryParams)
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

const handleSubmit = (row) => {
  submitForm.value = {
    id: row.id,
    bookQuantity: row.bookQuantity,
    actualQuantity: row.bookQuantity,
    diffReason: ''
  }
  submitDialogVisible.value = true
}

const confirmSubmit = async () => {
  try {
    await StockTakingApi.submitStockTaking(submitForm.value.id, submitForm.value.actualQuantity, submitForm.value.diffReason)
    message.success(t('wms.submitSuccess'))
    submitDialogVisible.value = false
    await getList()
  } catch {}
}

const handleReview = async (id: number) => {
  try {
    await message.confirm(t('wms.reviewConfirm'))
    await StockTakingApi.reviewStockTaking(id)
    message.success(t('wms.reviewSuccess'))
    await getList()
  } catch {}
}

const handleAdjust = async (id: number) => {
  try {
    await message.confirm(t('wms.adjustConfirm'))
    await StockTakingApi.adjustStockTaking(id)
    message.success(t('wms.adjustSuccess'))
    await getList()
  } catch {}
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await StockTakingApi.deleteStockTaking(id)
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
