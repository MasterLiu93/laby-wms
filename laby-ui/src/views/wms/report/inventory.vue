<!--
  库存报表页面
  
  功能说明：
  1. 库存汇总统计（总商品数、总库存、可用库存、锁定库存、低库存预警）
  2. 库存明细列表（支持多条件搜索）
  3. 支持导出Excel
  
  @author laby
  @date 2025-10-28
-->
<template>
  <ContentWrap>
    <!-- 汇总卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.totalGoodsCount') }}</div>
            <div class="statistic-value">{{ summary.totalGoodsCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.totalQuantity') }}</div>
            <div class="statistic-value text-primary">{{ summary.totalQuantity || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.availableQuantity') }}</div>
            <div class="statistic-value text-success">{{ summary.availableQuantity || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.lockedQuantity') }}</div>
            <div class="statistic-value text-warning">{{ summary.lockedQuantity || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.lowStockCount') }}</div>
            <div class="statistic-value text-danger">{{ summary.lowStockCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.zeroStockCount') }}</div>
            <div class="statistic-value text-info">{{ summary.zeroStockCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" :placeholder="t('wms.warehousePlaceholder')" clearable class="!w-240px">
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

      <el-form-item :label="t('wms.skuCode')" prop="skuCode">
        <el-input v-model="queryParams.skuCode" :placeholder="t('wms.skuCodePlaceholder')" clearable class="!w-240px" />
      </el-form-item>

      <el-form-item :label="t('wms.lowStock')" prop="lowStock">
        <el-checkbox v-model="queryParams.lowStock">{{ t('wms.lowStockOnly') }}</el-checkbox>
      </el-form-item>

      <el-form-item>
        <el-button @click="handleQuery">{{ t('common.query') }}</el-button>
        <el-button @click="resetQuery">{{ t('common.reset') }}</el-button>
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
    
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column v-if="columns.warehouseName.visible" :label="t('wms.warehouse')" prop="warehouseName" width="120" align="center" />
      <el-table-column v-if="columns.locationCode.visible" :label="t('wms.location')" prop="locationCode" width="120" align="center" />
      <el-table-column v-if="columns.skuCode.visible" :label="t('wms.skuCode')" prop="skuCode" width="140" align="center" />
      <el-table-column v-if="columns.goodsName.visible" :label="t('wms.goodsName')" prop="goodsName" min-width="180" show-overflow-tooltip />
      <el-table-column v-if="columns.categoryName.visible" :label="t('wms.category')" prop="categoryName" width="120" align="center" />
      <el-table-column v-if="columns.batchNo.visible" :label="t('wms.batchNo')" prop="batchNo" width="120" align="center" />
      <el-table-column v-if="columns.totalQuantity.visible" :label="t('wms.totalQuantityLabel')" prop="totalQuantity" width="100" align="center" />
      <el-table-column v-if="columns.availableQuantity.visible" :label="t('wms.availableQuantityLabel')" prop="availableQuantity" width="100" align="center">
        <template #default="scope">
          <span :class="{ 'text-danger': scope.row.isLowStock }">{{ scope.row.availableQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.lockedQuantity.visible" :label="t('wms.lockedQuantityLabel')" prop="lockedQuantity" width="100" align="center" />
      <el-table-column v-if="columns.safetyStock.visible" :label="t('wms.safetyStock')" prop="safetyStock" width="100" align="center" />
      <el-table-column v-if="columns.stockStatus.visible" :label="t('wms.stockStatus')" prop="stockStatus" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.isLowStock ? 'danger' : 'success'">
            {{ scope.row.isLowStock ? t('wms.lowStockStatus') : t('wms.normal') }}
          </el-tag>
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
import * as InventoryReportApi from '@/api/wms/inventoryReport'
import * as WarehouseApi from '@/api/wms/warehouse'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

defineOptions({ name: 'WmsInventoryReport' })

const { t } = useI18n()

const loading = ref(true)
const list = ref([])
const total = ref(0)
const warehouseList = ref([])
const summary = ref({
  totalGoodsCount: 0,
  totalQuantity: 0,
  availableQuantity: 0,
  lockedQuantity: 0,
  lowStockCount: 0,
  zeroStockCount: 0
})

// 列设置功能
const columns = reactive(createWMSColumns(t).inventoryReport)
const showSearch = ref(true)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  warehouseId: undefined,
  goodsName: undefined,
  skuCode: undefined,
  lowStock: false
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await InventoryReportApi.getInventoryReportPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getSummary = async () => {
  const data = await InventoryReportApi.getInventoryReportSummary(queryParams)
  summary.value = data
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
  getSummary()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

onMounted(async () => {
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
  await getList()
  await getSummary()
})
</script>

<style scoped lang="scss">
.statistic-card {
  text-align: center;
  padding: 10px 0;
}

.statistic-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.statistic-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.text-primary {
  color: #409eff !important;
}

.text-success {
  color: #67c23a !important;
}

.text-warning {
  color: #e6a23c !important;
}

.text-danger {
  color: #f56c6c !important;
}

.text-info {
  color: #909399 !important;
}
</style>
