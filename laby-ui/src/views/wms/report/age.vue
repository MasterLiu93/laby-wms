<!--
  库存库龄报表页面
  
  功能说明：
  1. 库存库龄统计（各库龄段数量、占比）
  2. 商品库龄明细列表
  3. 支持按仓库、商品查询
  4. 支持导出Excel
  
  @author laby
  @date 2025-11-12
-->
<template>
  <ContentWrap>
    <!-- 汇总卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.age0to30Days') }}</div>
            <div class="statistic-value text-success">{{ summary.age0to30 || 0 }}</div>
            <div class="statistic-desc">{{ summary.age0to30Percent || 0 }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.age31to60Days') }}</div>
            <div class="statistic-value text-primary">{{ summary.age31to60 || 0 }}</div>
            <div class="statistic-desc">{{ summary.age31to60Percent || 0 }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.age61to90Days') }}</div>
            <div class="statistic-value text-warning">{{ summary.age61to90 || 0 }}</div>
            <div class="statistic-desc">{{ summary.age61to90Percent || 0 }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.age91to180Days') }}</div>
            <div class="statistic-value text-danger">{{ summary.age91to180 || 0 }}</div>
            <div class="statistic-desc">{{ summary.age91to180Percent || 0 }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.age180PlusDays') }}</div>
            <div class="statistic-value text-danger">{{ summary.age180Plus || 0 }}</div>
            <div class="statistic-desc">{{ summary.age180PlusPercent || 0 }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.avgAge') }}</div>
            <div class="statistic-value">{{ summary.avgAge || 0 }}</div>
            <div class="statistic-desc">{{ t('wms.days') }}</div>
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

      <el-form-item :label="t('wms.ageRange')" prop="ageRange">
        <el-select
          v-model="queryParams.ageRange"
          :placeholder="t('common.selectText')"
          clearable
          class="!w-240px"
        >
          <el-option :label="t('wms.ageRange0to30')" value="0-30" />
          <el-option :label="t('wms.ageRange31to60')" value="31-60" />
          <el-option :label="t('wms.ageRange61to90')" value="61-90" />
          <el-option :label="t('wms.ageRange91to180')" value="91-180" />
          <el-option :label="t('wms.ageRange180Plus')" value="180+" />
        </el-select>
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
      <el-table-column v-if="columns.locationCode.visible" :label="t('wms.location')" align="center" prop="locationCode" width="100" />
      <el-table-column v-if="columns.batchNo.visible" :label="t('wms.batchNo')" align="center" prop="batchNo" width="120" />
      <el-table-column v-if="columns.quantity.visible" :label="t('wms.quantity')" align="center" prop="quantity" width="100" />
      <el-table-column v-if="columns.inboundDate.visible" :label="t('wms.inboundDate')" align="center" prop="inboundDate" width="120" />
      <el-table-column v-if="columns.age.visible" :label="t('wms.age')" align="center" prop="age" width="100">
        <template #default="{ row }">
          <el-tag :type="getAgeType(row.age)">
            {{ row.age }} {{ t('wms.days') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.ageLevel.visible" :label="t('wms.ageLevel')" align="center" prop="ageLevel" width="100">
        <template #default="{ row }">
          <el-tag :type="getAgeLevelType(row.ageLevel)">
            {{ getAgeLevelText(row.ageLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.remark.visible" :label="t('wms.remark')" align="center" prop="remark" min-width="150" show-overflow-tooltip />
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
import download from '@/utils/download'
import * as AgeReportApi from '@/api/wms/report/age'
import * as WarehouseApi from '@/api/wms/warehouse'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

defineOptions({ name: 'WmsAgeReport' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const warehouseList = ref([])
const exportLoading = ref(false)

// 列设置功能
const columns = reactive(createWMSColumns(t).ageReport)
const showSearch = ref(true)

// 汇总数据
const summary = ref({
  age0to30: 0,
  age0to30Percent: 0,
  age31to60: 0,
  age31to60Percent: 0,
  age61to90: 0,
  age61to90Percent: 0,
  age91to180: 0,
  age91to180Percent: 0,
  age180Plus: 0,
  age180PlusPercent: 0,
  avgAge: 0
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  warehouseId: undefined,
  goodsName: undefined,
  ageRange: undefined
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await AgeReportApi.getAgeReportPage(queryParams)
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
    const data = await AgeReportApi.exportAgeReport(queryParams)
    download.excel(data, `${t('wms.ageReport')}.xls`)
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 获取库龄类型 */
const getAgeType = (age: number) => {
  if (age <= 30) return 'success'
  if (age <= 60) return 'primary'
  if (age <= 90) return 'warning'
  return 'danger'
}

/** 获取库龄等级类型 */
const getAgeLevelType = (level: string) => {
  const typeMap = {
    'FRESH': 'success',
    'NORMAL': 'primary',
    'AGING': 'warning',
    'OLD': 'danger'
  }
  return typeMap[level] || 'info'
}

/** 获取库龄等级文本 */
const getAgeLevelText = (level: string) => {
  const textMap = {
    'FRESH': t('wms.fresh'),
    'NORMAL': t('wms.normal'),
    'AGING': t('wms.aging'),
    'OLD': t('wms.old')
  }
  return textMap[level] || level
}

/** 初始化 */
onMounted(async () => {
  // 加载仓库列表
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
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
