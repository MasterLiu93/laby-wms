<template>
  <div class="wms-home">
    <!-- 顶部数据统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xl="6" :lg="12" :md="12" :sm="24" :xs="24" v-for="(stat, index) in stats" :key="index">
        <div class="stat-card" :style="{ '--card-color': stat.color }">
          <div class="stat-icon-wrapper">
            <div class="stat-icon-bg"></div>
            <Icon :icon="stat.icon" :size="48" class="stat-icon" />
          </div>
          <div class="stat-content">
            <div class="stat-label">{{ stat.label }}</div>
            <CountTo
              class="stat-value"
              :start-val="0"
              :end-val="stat.value"
              :duration="2000"
            />
            <div class="stat-trend" :class="stat.trend > 0 ? 'trend-up' : 'trend-down'">
              <Icon :icon="stat.trend > 0 ? 'ep:top' : 'ep:bottom'" />
              <span>{{ Math.abs(stat.trend) }}%</span>
            </div>
          </div>
          <div class="stat-wave"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 3D仓库可视化区域 -->
    <el-row :gutter="16" class="mt-4">
      <el-col :xl="16" :lg="16" :md="24" :sm="24" :xs="24">
        <el-card shadow="hover" class="warehouse-visual-card">
          <template #header>
            <div class="card-header">
              <div class="flex items-center">
                <Icon icon="mdi:warehouse" class="mr-2 text-2xl" style="color: #667eea" />
                <span class="card-title">{{ t('wms.warehouseMonitor') }}</span>
              </div>
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button value="3d">{{ t('wms.view3D') }}</el-radio-button>
                <el-radio-button value="chart">{{ t('wms.viewChart') }}</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          
          <!-- 3D仓库视图 -->
          <div v-if="viewMode === '3d'" class="warehouse-3d-view">
            <div class="warehouse-container">
              <div class="warehouse-grid">
                <div 
                  v-for="(rack, index) in warehouseRacks" 
                  :key="index" 
                  class="rack-item"
                  :class="{ 
                    'rack-full': rack.rate > 80, 
                    'rack-medium': rack.rate > 40 && rack.rate <= 80,
                    'rack-low': rack.rate <= 40 
                  }"
                  @mouseenter="showRackInfo(rack)"
                  @mouseleave="hideRackInfo"
                >
                  <div class="rack-label">{{ rack.name }}</div>
                  <div class="rack-fill" :style="{ height: rack.rate + '%' }"></div>
                  <div class="rack-percentage">{{ rack.rate }}%</div>
                </div>
              </div>
              
              <!-- 货架信息提示 -->
              <transition name="fade">
                <div v-if="selectedRack" class="rack-tooltip">
                  <h4>{{ selectedRack?.name }}</h4>
                  <p>{{ t('wms.usageRate') }}: {{ selectedRack?.rate }}%</p>
                  <p>{{ t('wms.stockQuantity') }}: {{ selectedRack?.quantity }}</p>
                  <p>{{ t('wms.area') }}: {{ selectedRack?.area }}</p>
                </div>
              </transition>
            </div>
          </div>

          <!-- 图表视图 -->
          <div v-else class="chart-view">
            <Echart :options="warehouseChartOptions" :height="400" />
          </div>
        </el-card>

        <!-- 入库/出库趋势 -->
        <el-row :gutter="16" class="mt-4">
          <el-col :span="12">
            <el-card shadow="hover" class="trend-card inbound-card">
              <template #header>
                <div class="flex items-center justify-between">
                  <div class="flex items-center">
                    <Icon icon="mdi:arrow-down-bold-box" class="mr-2 text-xl text-green-500" />
                    <span class="card-title">{{ t('wms.inboundTrend') }}</span>
                  </div>
                  <el-tag type="success">{{ t('wms.today') }} +{{ todayInbound }}</el-tag>
                </div>
              </template>
              <Echart :options="inboundChartOptions" :height="220" />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="trend-card outbound-card">
              <template #header>
                <div class="flex items-center justify-between">
                  <div class="flex items-center">
                    <Icon icon="mdi:arrow-up-bold-box" class="mr-2 text-xl text-orange-500" />
                    <span class="card-title">{{ t('wms.outboundTrend') }}</span>
                  </div>
                  <el-tag type="warning">{{ t('wms.today') }} +{{ todayOutbound }}</el-tag>
                </div>
              </template>
              <Echart :options="outboundChartOptions" :height="220" />
            </el-card>
          </el-col>
        </el-row>
      </el-col>

      <!-- 右侧信息面板 -->
      <el-col :xl="8" :lg="8" :md="24" :sm="24" :xs="24">
        <!-- 快捷操作 -->
        <el-card shadow="hover" class="quick-actions-card">
          <template #header>
            <div class="flex items-center">
              <Icon icon="mdi:lightning-bolt" class="mr-2 text-xl text-yellow-500" />
              <span class="card-title">{{ t('wms.quickActions') }}</span>
            </div>
          </template>
          <div class="quick-actions-grid">
            <div 
              v-for="action in quickActions" 
              :key="action.name"
              class="action-item"
              :style="{ '--action-color': action.color }"
              @click="handleQuickAction(action.route)"
            >
              <div class="action-icon-wrapper">
                <Icon :icon="action.icon" :size="32" />
              </div>
              <div class="action-name">{{ action.name }}</div>
            </div>
          </div>
        </el-card>

        <!-- 库存预警 -->
        <el-card shadow="hover" class="mt-4 warning-card">
          <template #header>
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <Icon icon="ep:warning" class="mr-2 text-xl text-red-500" />
                <span class="card-title">{{ t('wms.stockWarning') }}</span>
              </div>
              <el-badge :value="warnings.length" class="item" />
            </div>
          </template>
          <div class="warning-list">
            <div 
              v-for="(warning, index) in warnings" 
              :key="index"
              class="warning-item"
              :class="'warning-' + warning.level"
            >
              <div class="warning-icon">
                <Icon 
                  :icon="warning.level === 'high' ? 'ep:warning-filled' : 'ep:info-filled'" 
                  :size="20" 
                />
              </div>
              <div class="warning-content">
                <div class="warning-title">{{ warning.goods }}</div>
                <div class="warning-desc">{{ t('wms.stock') }}: {{ warning.stock }} / {{ t('wms.minStock') }}: {{ warning.min }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 实时动态 -->
        <el-card shadow="hover" class="mt-4 activity-card">
          <template #header>
            <div class="flex items-center">
              <Icon icon="mdi:clock-time-four-outline" class="mr-2 text-xl text-blue-500" />
              <span class="card-title">{{ t('wms.realtimeActivity') }}</span>
            </div>
          </template>
          <el-timeline class="activity-timeline">
            <el-timeline-item
              v-for="(activity, index) in recentActivities"
              :key="index"
              :timestamp="activity.time"
              :color="activity.color"
              placement="top"
            >
              <div class="activity-content">
                <div class="activity-type">{{ activity.type }}</div>
                <div class="activity-desc">{{ activity.desc }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门商品统计 -->
    <el-row :gutter="16" class="mt-4">
      <el-col :span="24">
        <el-card shadow="hover" class="popular-goods-card">
          <template #header>
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <Icon icon="mdi:fire" class="mr-2 text-xl text-red-500" />
                <span class="card-title">{{ t('wms.popularGoods') }}</span>
              </div>
              <el-radio-group v-model="goodsStatType" size="small">
                <el-radio-button value="inbound">{{ t('wms.inboundTop10') }}</el-radio-button>
                <el-radio-button value="outbound">{{ t('wms.outboundTop10') }}</el-radio-button>
                <el-radio-button value="inventory">{{ t('wms.inventoryTop10') }}</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <Echart :options="popularGoodsChartOptions" :height="300" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@/components/Icon'
import { EChartsOption } from 'echarts'
import * as HomeApi from '@/api/wms/home'

defineOptions({ name: 'WmsHome' })

const { t } = useI18n()
const router = useRouter()

// 视图模式
const viewMode = ref('3d')
const goodsStatType = ref('inbound')

// 顶部统计数据
const stats = computed(() => [
  {
    label: t('wms.totalInventory'),
    value: statisticsData.value.totalInventory || 0,
    icon: 'mdi:package-variant-closed',
    color: '#667eea',
    trend: statisticsData.value.totalInventoryTrend || 0
  },
  {
    label: t('wms.todayInbound'),
    value: statisticsData.value.todayInbound || 0,
    icon: 'mdi:arrow-down-bold-circle',
    color: '#10b981',
    trend: statisticsData.value.todayInboundTrend || 0
  },
  {
    label: t('wms.todayOutbound'),
    value: statisticsData.value.todayOutbound || 0,
    icon: 'mdi:arrow-up-bold-circle',
    color: '#f59e0b',
    trend: statisticsData.value.todayOutboundTrend || 0
  },
  {
    label: t('wms.inventoryWarning'),
    value: statisticsData.value.inventoryWarning || 0,
    icon: 'mdi:alert-circle',
    color: '#ef4444',
    trend: statisticsData.value.inventoryWarningTrend || 0
  }
])

const statisticsData = ref<HomeApi.HomeStatisticsVO>({
  totalInventory: 0,
  todayInbound: 0,
  todayOutbound: 0,
  inventoryWarning: 0,
  totalInventoryTrend: 0,
  todayInboundTrend: 0,
  todayOutboundTrend: 0,
  inventoryWarningTrend: 0
})

// 仓库货架数据
const warehouseRacks = ref<HomeApi.RackInfoVO[]>([])

const selectedRack = ref<HomeApi.RackInfoVO | null>(null)

const showRackInfo = (rack: HomeApi.RackInfoVO) => {
  selectedRack.value = rack
}

const hideRackInfo = () => {
  selectedRack.value = null
}

// 仓库图表配置
const warehouseChartOptions = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: warehouseRacks.value.map(r => r.name),
    axisLabel: {
      rotate: 45
    }
  },
  yAxis: {
    type: 'value',
    name: t('wms.usageRate') + '(%)'
  },
  series: [
    {
      name: t('wms.usageRate'),
      type: 'bar',
      data: warehouseRacks.value.map(r => r.rate),
      itemStyle: {
        color: (params) => {
          const rate = params.value as number
          if (rate > 80) return '#ef4444'
          if (rate > 40) return '#f59e0b'
          return '#10b981'
        }
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%'
      }
    }
  ]
}))

// 趋势数据
const trendData = ref<HomeApi.TrendDataVO>({
  dates: [],
  inboundData: [],
  outboundData: []
})

// 今日数据
const todayInbound = computed(() => statisticsData.value.todayInbound || 0)
const todayOutbound = computed(() => statisticsData.value.todayOutbound || 0)

// 入库趋势图
const inboundChartOptions = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: trendData.value.dates.map((_, index) => t('wms.' + ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'][index % 7])),
    boundaryGap: false
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: t('wms.inboundQuantity'),
      type: 'line',
      smooth: true,
      data: trendData.value.inboundData,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
          ]
        }
      },
      itemStyle: {
        color: '#10b981'
      },
      lineStyle: {
        width: 3
      }
    }
  ]
}))

// 出库趋势图
const outboundChartOptions = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: trendData.value.dates.map((_, index) => t('wms.' + ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'][index % 7])),
    boundaryGap: false
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: t('wms.outboundQuantity'),
      type: 'line',
      smooth: true,
      data: trendData.value.outboundData,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
            { offset: 1, color: 'rgba(245, 158, 11, 0.05)' }
          ]
        }
      },
      itemStyle: {
        color: '#f59e0b'
      },
      lineStyle: {
        width: 3
      }
    }
  ]
}))

// 快捷操作
const quickActions = computed(() => [
  { name: t('wms.newInbound'), icon: 'mdi:package-down', color: '#10b981', route: '/wms/inbound' },
  { name: t('wms.newOutbound'), icon: 'mdi:package-up', color: '#f59e0b', route: '/wms/outbound' },
  { name: t('wms.inventoryQuery'), icon: 'mdi:magnify', color: '#3b82f6', route: '/wms/inventory' },
  { name: t('wms.stocktakingPlan'), icon: 'mdi:clipboard-list', color: '#8b5cf6', route: '/wms/stocktaking-plan' },
  { name: t('wms.stockMove'), icon: 'mdi:transfer', color: '#ec4899', route: '/wms/stock-move' },
  { name: t('wms.inventoryReport'), icon: 'mdi:chart-line', color: '#06b6d4', route: '/wms/report/inventory' }
])

const handleQuickAction = (route: string) => {
  router.push(route)
}

// 库存预警
const warnings = ref<HomeApi.InventoryWarningVO[]>([])

// 实时动态
const recentActivities = ref<HomeApi.RecentActivityVO[]>([])

// 热门商品统计
const popularGoodsData = ref<HomeApi.PopularGoodsVO>({
  names: [],
  values: []
})

const popularGoodsChartOptions = computed<EChartsOption>(() => {
  const seriesName = goodsStatType.value === 'inbound' 
    ? t('wms.inboundQuantity') 
    : goodsStatType.value === 'outbound' 
    ? t('wms.outboundQuantity') 
    : t('wms.inventoryQuantity')

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: popularGoodsData.value.names,
      axisLabel: {
        width: 120,
        overflow: 'truncate'
      }
    },
    series: [
      {
        name: seriesName,
        type: 'bar',
        data: popularGoodsData.value.values,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#667eea' },
              { offset: 1, color: '#764ba2' }
            ]
          },
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}'
        }
      }
    ]
  }
})

// 加载数据的函数
const loadData = async () => {
  try {
    // 加载统计数据
    statisticsData.value = await HomeApi.getHomeStatistics()
    
    // 加载货架信息
    warehouseRacks.value = await HomeApi.getRackList()
    
    // 加载趋势数据
    trendData.value = await HomeApi.getTrendData()
    
    // 加载预警列表
    warnings.value = await HomeApi.getWarningList()
    
    // 加载实时动态
    recentActivities.value = await HomeApi.getRecentActivities()
    
    // 加载热门商品数据
    await loadPopularGoods()
  } catch (error) {
    console.error('Failed to load home data:', error)
  }
}

// 加载热门商品数据
const loadPopularGoods = async () => {
  try {
    popularGoodsData.value = await HomeApi.getPopularGoods(goodsStatType.value as 'inbound' | 'outbound' | 'inventory')
  } catch (error) {
    console.error('Failed to load popular goods data:', error)
  }
}

// 监听商品统计类型变化
watch(goodsStatType, () => {
  loadPopularGoods()
})

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.wms-home {
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 80px);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.05) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(118, 75, 162, 0.05) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
  }

  > * {
    position: relative;
    z-index: 1;
  }
}

// 统计卡片样式
.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  position: relative;
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

    .stat-icon-wrapper {
      transform: scale(1.1) rotate(5deg);
    }
  }

  .stat-icon-wrapper {
    position: relative;
    width: 80px;
    height: 80px;
    margin-bottom: 16px;
    transition: all 0.3s ease;

    .stat-icon-bg {
      position: absolute;
      width: 100%;
      height: 100%;
      background: var(--card-color);
      opacity: 0.1;
      border-radius: 20px;
      transform: rotate(10deg);
    }

    .stat-icon {
      position: relative;
      z-index: 1;
      color: var(--card-color);
    }
  }

  .stat-content {
    .stat-label {
      font-size: 14px;
      color: #6b7280;
      margin-bottom: 8px;
      font-weight: 500;
    }

    .stat-value {
      font-size: 32px;
      font-weight: 700;
      color: #1f2937;
      margin-bottom: 8px;
    }

    .stat-trend {
      font-size: 14px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 4px;

      &.trend-up {
        color: #10b981;
      }

      &.trend-down {
        color: #ef4444;
      }
    }
  }

  .stat-wave {
    position: absolute;
    bottom: -20px;
    right: -20px;
    width: 150px;
    height: 150px;
    background: var(--card-color);
    opacity: 0.05;
    border-radius: 50%;
  }
}

// 3D仓库视图样式
.warehouse-visual-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.warehouse-3d-view {
  padding: 20px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border-radius: 12px;
  min-height: 400px;
}

.warehouse-container {
  position: relative;
}

.warehouse-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 16px;
  perspective: 1000px;
}

.rack-item {
  position: relative;
  height: 200px;
  background: linear-gradient(to top, #e5e7eb, #f3f4f6);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;

  &:hover {
    transform: translateY(-8px) scale(1.05);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
    z-index: 10;
  }

  .rack-label {
    position: absolute;
    top: 8px;
    left: 0;
    right: 0;
    text-align: center;
    font-size: 12px;
    font-weight: 600;
    color: #374151;
    z-index: 2;
  }

  .rack-fill {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    transition: height 0.6s ease;
    border-radius: 0 0 8px 8px;
  }

  .rack-percentage {
    position: absolute;
    bottom: 8px;
    left: 0;
    right: 0;
    text-align: center;
    font-size: 14px;
    font-weight: 700;
    color: white;
    z-index: 2;
  }

  &.rack-full .rack-fill {
    background: linear-gradient(to top, #ef4444, #f87171);
    box-shadow: 0 0 20px rgba(239, 68, 68, 0.5);
  }

  &.rack-medium .rack-fill {
    background: linear-gradient(to top, #f59e0b, #fbbf24);
    box-shadow: 0 0 20px rgba(245, 158, 11, 0.5);
  }

  &.rack-low .rack-fill {
    background: linear-gradient(to top, #10b981, #34d399);
    box-shadow: 0 0 20px rgba(16, 185, 129, 0.5);
  }
}

.rack-tooltip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  z-index: 100;
  min-width: 200px;

  h4 {
    margin: 0 0 12px 0;
    font-size: 18px;
    font-weight: 700;
    border-bottom: 2px solid #667eea;
    padding-bottom: 8px;
  }

  p {
    margin: 8px 0;
    font-size: 14px;
  }
}

// 卡片头部样式
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

// 趋势卡片
.trend-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

// 快捷操作
.quick-actions-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.6));
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;

  &:hover {
    transform: translateY(-4px);
    border-color: var(--action-color);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);

    .action-icon-wrapper {
      transform: scale(1.1) rotate(5deg);
      background: var(--action-color);

      :deep(.iconify) {
        color: white !important;
      }
    }
  }

  .action-icon-wrapper {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, var(--action-color), var(--action-color));
    opacity: 0.1;
    border-radius: 16px;
    margin-bottom: 12px;
    transition: all 0.3s ease;

    :deep(.iconify) {
      color: var(--action-color);
      transition: all 0.3s ease;
    }
  }

  .action-name {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    text-align: center;
  }
}

// 预警卡片
.warning-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.warning-list {
  max-height: 280px;
  overflow-y: auto;
}

.warning-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  background: #f9fafb;
  transition: all 0.3s ease;

  &:hover {
    background: #f3f4f6;
    transform: translateX(4px);
  }

  &.warning-high {
    border-left: 4px solid #ef4444;

    .warning-icon {
      color: #ef4444;
    }
  }

  &.warning-medium {
    border-left: 4px solid #f59e0b;

    .warning-icon {
      color: #f59e0b;
    }
  }

  .warning-icon {
    margin-right: 12px;
  }

  .warning-content {
    flex: 1;

    .warning-title {
      font-size: 14px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 4px;
    }

    .warning-desc {
      font-size: 12px;
      color: #6b7280;
    }
  }
}

// 动态卡片
.activity-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.activity-timeline {
  max-height: 300px;
  overflow-y: auto;
  padding: 12px 0;
}

.activity-content {
  .activity-type {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
  }

  .activity-desc {
    font-size: 13px;
    color: #6b7280;
  }
}

// 热门商品卡片
.popular-goods-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

// 响应式
@media (max-width: 768px) {
  .wms-home {
    padding: 8px;
  }

  .stat-card {
    padding: 16px;
  }

  .warehouse-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .rack-item {
    height: 120px;
  }

  .quick-actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>

