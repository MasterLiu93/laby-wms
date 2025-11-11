<!--
  出入库统计页面
  
  功能说明：
  1. 出入库汇总统计
  2. 出入库趋势图（折线图）
  3. 支持按时间范围、仓库等筛选
  
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
            <div class="statistic-title">{{ t('wms.totalInboundQuantity') }}</div>
            <div class="statistic-value text-success">{{ summary.totalInboundQuantity || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.totalInboundOrderCount') }}</div>
            <div class="statistic-value">{{ summary.totalInboundOrderCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.totalOutboundQuantity') }}</div>
            <div class="statistic-value text-warning">{{ summary.totalOutboundQuantity || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.totalOutboundOrderCount') }}</div>
            <div class="statistic-value">{{ summary.totalOutboundOrderCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.netChange') }}</div>
            <div class="statistic-value" :class="summary.netChange >= 0 ? 'text-primary' : 'text-danger'">
              {{ summary.netChange || 0 }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="statistic-card">
            <div class="statistic-title">{{ t('wms.turnoverRate') }}</div>
            <div class="statistic-value text-info">{{ summary.turnoverRate || 0 }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item :label="t('wms.timeRange')" prop="startTime">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          :range-separator="t('wms.dateRangeSeparator')"
          :start-placeholder="t('wms.startDatePlaceholder')"
          :end-placeholder="t('wms.endDatePlaceholder')"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="!w-240px"
        />
      </el-form-item>

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

      <el-form-item>
        <el-button @click="handleQuery">{{ t('common.query') }}</el-button>
        <el-button @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 图表 -->
  <ContentWrap class="mb-4">
    <el-card shadow="never" :body-style="{ padding: '20px' }">
      <div ref="chartRef" style="height: 400px; width: 100%;"></div>
    </el-card>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe max-height="400">
      <el-table-column :label="t('wms.statisticDate')" prop="statisticDate" width="120" align="center" :formatter="dateFormatter" fixed="left" />
      <el-table-column :label="t('wms.inboundQuantity')" prop="inboundQuantity" width="140" align="right">
        <template #default="scope">
          <span class="text-success">{{ scope.row.inboundQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('wms.inboundOrderCount')" prop="inboundOrderCount" width="120" align="center" />
      <el-table-column :label="t('wms.outboundQuantity')" prop="outboundQuantity" width="140" align="right">
        <template #default="scope">
          <span class="text-warning">{{ scope.row.outboundQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('wms.outboundOrderCount')" prop="outboundOrderCount" width="120" align="center" />
      <el-table-column :label="t('wms.netChange')" prop="netChange" width="140" align="right">
        <template #default="scope">
          <span :class="scope.row.netChange >= 0 ? 'text-primary' : 'text-danger'">
            {{ scope.row.netChange }}
          </span>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { formatDate } from '@/utils/formatTime'
import * as InOutReportApi from '@/api/wms/inOutReport'
import * as WarehouseApi from '@/api/wms/warehouse'

defineOptions({ name: 'WmsInOutReport' })

const { t } = useI18n()

const loading = ref(true)
const list = ref([])
const warehouseList = ref([])
const chartRef = ref()
const dateRange = ref([])
const summary = ref({
  totalInboundQuantity: 0,
  totalInboundOrderCount: 0,
  totalOutboundQuantity: 0,
  totalOutboundOrderCount: 0,
  netChange: 0,
  turnoverRate: 0
})

const queryParams = reactive({
  startTime: undefined,
  endTime: undefined,
  warehouseId: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await InOutReportApi.getInOutReportList(queryParams)
    list.value = data
    // 使用 nextTick 确保 DOM 更新后再渲染图表
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

const getSummary = async () => {
  const data = await InOutReportApi.getInOutReportSummary(queryParams)
  summary.value = data
}

const chartInstance = ref<any>(null)

const handleResize = () => {
  chartInstance.value?.resize()
}

const renderChart = () => {
  // 如果没有数据或容器不存在，不渲染
  if (!list.value || list.value.length === 0 || !chartRef.value) {
    return
  }

  // 如果图表已存在，先销毁
  if (chartInstance.value) {
    chartInstance.value.dispose()
    window.removeEventListener('resize', handleResize)
    chartInstance.value = null
  }

  // 确保容器有尺寸
  const container = chartRef.value
  if (container.offsetWidth === 0 || container.offsetHeight === 0) {
    console.warn('Chart container has no size')
    return
  }

  try {
    chartInstance.value = echarts.init(container)
    const dates = list.value.map(item => formatDate(item.statisticDate, 'MM-DD'))
    const inboundData = list.value.map(item => Number(item.inboundQuantity) || 0)
    const outboundData = list.value.map(item => Number(item.outboundQuantity) || 0)
    const netChangeData = list.value.map(item => Number(item.netChange) || 0)

    const option = {
      title: {
        text: t('wms.chartTitle'),
        left: 'center',
        top: 10,
        textStyle: {
          fontSize: 16
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        formatter: function(params: any) {
          let result = params[0].name + '<br/>'
          params.forEach((item: any) => {
            result += item.marker + ' ' + item.seriesName + ': ' + item.value + '<br/>'
          })
          return result
        }
      },
      legend: {
        data: [t('wms.chartLegendInbound'), t('wms.chartLegendOutbound'), t('wms.chartLegendNetChange')],
        top: 35,
        itemGap: 20
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLabel: {
          rotate: dates.length > 10 ? 45 : 0
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (value: number) => {
            return value.toLocaleString()
          }
        }
      },
      series: [
        {
          name: t('wms.chartLegendInbound'),
          type: 'line',
          smooth: true,
          data: inboundData,
          itemStyle: { color: '#67c23a' },
          areaStyle: { opacity: 0.3, color: '#67c23a' }
        },
        {
          name: t('wms.chartLegendOutbound'),
          type: 'line',
          smooth: true,
          data: outboundData,
          itemStyle: { color: '#e6a23c' },
          areaStyle: { opacity: 0.3, color: '#e6a23c' }
        },
        {
          name: t('wms.chartLegendNetChange'),
          type: 'line',
          smooth: true,
          data: netChangeData,
          itemStyle: { color: '#409eff' }
        }
      ]
    }

    chartInstance.value.setOption(option, true)
    
    // 响应窗口大小变化
    window.addEventListener('resize', handleResize)
  } catch (error) {
    console.error('Chart render error:', error)
  }
}

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    window.removeEventListener('resize', handleResize)
  }
})

const handleQuery = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startTime = dateRange.value[0]
    queryParams.endTime = dateRange.value[1]
  } else {
    queryParams.startTime = undefined
    queryParams.endTime = undefined
  }
  getList()
  getSummary()
}

const resetQuery = () => {
  dateRange.value = []
  queryFormRef.value.resetFields()
  queryParams.startTime = undefined
  queryParams.endTime = undefined
  handleQuery()
}

const dateFormatter = (row, column, cellValue) => {
  if (!cellValue) return '-'
  return formatDate(cellValue, 'YYYY-MM-DD')
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
