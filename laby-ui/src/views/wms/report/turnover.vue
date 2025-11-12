<!--
  库存周转率报表页面
  
  功能说明：
  1. 库存周转率统计（平均周转率、周转天数）
  2. 商品周转率明细列表
  3. 支持按时间范围查询
  4. 支持导出Excel
  
  @author laby
  @date 2025-11-12
-->
<template>
  <ContentWrap>
    <!-- 汇总卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.avgTurnoverRate') }}</div>
            <div class="statistic-value text-primary">{{ summary.avgTurnoverRate || 0 }}</div>
            <div class="statistic-desc">{{ t('wms.times') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.avgTurnoverDays') }}</div>
            <div class="statistic-value text-success">{{ summary.avgTurnoverDays || 0 }}</div>
            <div class="statistic-desc">{{ t('wms.days') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.fastMovingGoods') }}</div>
            <div class="statistic-value text-warning">{{ summary.fastMovingCount || 0 }}</div>
            <div class="statistic-desc">{{ t('wms.items') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.slowMovingGoods') }}</div>
            <div class="statistic-value text-danger">{{ summary.slowMovingCount || 0 }}</div>
            <div class="statistic-desc">{{ t('wms.items') }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="100px"
    >
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

      <el-form-item :label="t('wms.goodsName')" prop="goodsName">
        <el-input
          v-model="queryParams.goodsName"
          :placeholder="t('wms.goodsNamePlaceholder')"
          clearable
          class="!w-240px"
        />
      </el-form-item>

      <el-form-item :label="t('wms.dateRange')" prop="dateRange">
        <el-date-picker
          v-model="queryParams.dateRange"
          type="daterange"
          :start-placeholder="t('common.startTimeText')"
          :end-placeholder="t('common.endTimeText')"
          value-format="YYYY-MM-DD"
          class="!w-240px"
        />
      </el-form-item>

      <el-form-item>
        <el-button @click="handleQuery" type="primary">
          <Icon icon="ep:search" class="mr-5px" /> {{ t('common.query') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> {{ t('common.reset') }}
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
        >
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
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
      <RightToolbar v-model:showSearch="showSearch" :columns="columns" :search="true" @queryTable="getList" />
    </div>
    
    <el-table v-loading="loading" :data="list">
      <el-table-column v-if="columns.goodsCode.visible" :label="t('wms.goodsCode')" align="center" prop="goodsCode" width="120" />
      <el-table-column v-if="columns.goodsName.visible" :label="t('wms.goodsName')" align="center" prop="goodsName" min-width="150" />
      <el-table-column v-if="columns.warehouseName.visible" :label="t('wms.warehouse')" align="center" prop="warehouseName" width="120" />
      <el-table-column v-if="columns.avgInventory.visible" :label="t('wms.avgInventory')" align="center" prop="avgInventory" width="100">
        <template #default="{ row }">
          {{ row.avgInventory?.toFixed(2) || 0 }}
        </template>
      </el-table-column>
      <el-table-column v-if="columns.outboundQuantity.visible" :label="t('wms.outboundQuantity')" align="center" prop="outboundQuantity" width="120" />
      <el-table-column v-if="columns.turnoverRate.visible" :label="t('wms.turnoverRate')" align="center" prop="turnoverRate" width="100">
        <template #default="{ row }">
          <el-tag :type="getTurnoverRateType(row.turnoverRate)">
            {{ row.turnoverRate?.toFixed(2) || 0 }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.turnoverDays.visible" :label="t('wms.turnoverDays')" align="center" prop="turnoverDays" width="100">
        <template #default="{ row }">
          {{ row.turnoverDays?.toFixed(0) || 0 }} {{ t('wms.days') }}
        </template>
      </el-table-column>
      <el-table-column v-if="columns.turnoverLevel.visible" :label="t('wms.turnoverLevel')" align="center" prop="turnoverLevel" width="100">
        <template #default="{ row }">
          <el-tag :type="getTurnoverLevelType(row.turnoverLevel)">
            {{ getTurnoverLevelText(row.turnoverLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.dateRange.visible" :label="t('wms.dateRange')" align="center" width="200">
        <template #default="{ row }">
          {{ row.startDate }} ~ {{ row.endDate }}
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
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import * as TurnoverReportApi from '@/api/wms/report/turnover'
import * as WarehouseApi from '@/api/wms/warehouse'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

defineOptions({ name: 'WmsTurnoverReport' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const warehouseList = ref([])
const exportLoading = ref(false)

// 列设置功能
const columns = reactive(createWMSColumns(t).turnoverReport)
const showSearch = ref(true)

// 汇总数据
const summary = ref({
  avgTurnoverRate: 0,
  avgTurnoverDays: 0,
  fastMovingCount: 0,
  slowMovingCount: 0
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  warehouseId: undefined,
  goodsName: undefined,
  dateRange: []
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      startDate: queryParams.dateRange?.[0],
      endDate: queryParams.dateRange?.[1]
    }
    delete params.dateRange
    const data = await TurnoverReportApi.getTurnoverReportPage(params)
    list.value = data.list
    total.value = data.total
    summary.value = data.summary || summary.value
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

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const params = {
      ...queryParams,
      startDate: queryParams.dateRange?.[0],
      endDate: queryParams.dateRange?.[1]
    }
    delete params.dateRange
    const data = await TurnoverReportApi.exportTurnoverReport(params)
    download.excel(data, `${t('wms.turnoverReport')}.xls`)
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 获取周转率类型 */
const getTurnoverRateType = (rate: number) => {
  if (rate >= 10) return 'success'
  if (rate >= 5) return 'primary'
  if (rate >= 2) return 'warning'
  return 'danger'
}

/** 获取周转等级类型 */
const getTurnoverLevelType = (level: string) => {
  const typeMap = {
    'FAST': 'success',
    'NORMAL': 'primary',
    'SLOW': 'warning',
    'STAGNANT': 'danger'
  }
  return typeMap[level] || 'info'
}

/** 获取周转等级文本 */
const getTurnoverLevelText = (level: string) => {
  const textMap = {
    'FAST': t('wms.fastMoving'),
    'NORMAL': t('wms.normalMoving'),
    'SLOW': t('wms.slowMoving'),
    'STAGNANT': t('wms.stagnant')
  }
  return textMap[level] || level
}

/** 初始化 */
onMounted(async () => {
  // 加载仓库列表
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
  
  // 设置默认日期范围（最近30天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  queryParams.dateRange = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  await getList()
})
</script>

<style scoped lang="scss">
.statistic-card {
  text-align: center;
  padding: 10px 0;
  
  .statistic-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 10px;
  }
  
  .statistic-value {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 5px;
  }
  
  .statistic-desc {
    font-size: 12px;
    color: #909399;
  }
}
</style>
