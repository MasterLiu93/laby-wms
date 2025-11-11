<template>
  <div class="modern-dashboard">
    <!-- 顶部欢迎横幅 -->
    <div class="hero-section">
      <div class="hero-bg">
        <div class="hero-pattern"></div>
        <div class="floating-shapes">
          <div class="shape shape-1">📦</div>
          <div class="shape shape-2">🚚</div>
          <div class="shape shape-3">📊</div>
          <div class="shape shape-4">✅</div>
        </div>
      </div>
      
      <div class="hero-content">
        <div class="welcome-area">
          <el-avatar :src="avatar" :size="64" class="hero-avatar">
                  <img src="@/assets/imgs/avatar.gif" alt="" />
                </el-avatar>
          <div class="welcome-text">
            <h1 class="hero-title">
              <span class="wave-emoji">👋</span> 
              {{ getGreeting() }}，{{ username }}！
            </h1>
            <p class="hero-subtitle">{{ currentDate }} · {{ t('wms.niceDay') }}</p>
          </div>
        </div>
        
        <div class="hero-stats">
          <div class="hero-stat-item" v-for="(stat, index) in heroStats" :key="index">
            <div class="stat-icon-wrapper" :style="{ background: stat.gradient }">
              <Icon :icon="stat.icon" :size="24" />
            </div>
            <div class="stat-details">
              <div class="stat-label">{{ stat.label }}</div>
              <CountTo class="stat-number" :start-val="0" :end-val="stat.value" :duration="1800" />
            </div>
          </div>
        </div>
              </div>
                </div>

    <!-- 核心数据看板 -->
    <div class="data-overview">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6" :md="6" :lg="6" v-for="(card, idx) in dataCards" :key="idx">
          <div class="metric-card" :class="`card-theme-${idx + 1}`">
            <div class="metric-icon">
              <Icon :icon="card.icon" :size="28" />
            </div>
            <div class="metric-body">
              <div class="metric-label">{{ card.label }}</div>
              <CountTo class="metric-value" :start-val="0" :end-val="card.value" :duration="2000" />
              <div class="metric-badge" :class="card.trend > 0 ? 'badge-up' : 'badge-down'">
                <Icon :icon="card.trend > 0 ? 'mdi:trending-up' : 'mdi:trending-down'" :size="14" />
                {{ Math.abs(card.trend) }}%
                </div>
              </div>
            </div>
          </el-col>
      </el-row>
    </div>

    <!-- 主要内容区 -->
    <el-row :gutter="16" class="main-content">
      <!-- 左侧列 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <!-- 快捷操作 -->
        <div class="section-card">
          <div class="section-header">
            <h3 class="section-title">
              <Icon icon="mdi:rocket-launch-outline" class="mr-2" />
              {{ t('wms.quickEntry') }}
            </h3>
          </div>
          <div class="quick-grid">
            <div
              v-for="quick in quickAccess"
              :key="quick.name"
              class="quick-card"
              @click="handleNavigate(quick.route)"
            >
              <div class="quick-icon" :style="{ background: quick.gradient, color: quick.color }">
                <Icon :icon="quick.icon" :size="26" />
              </div>
              <div class="quick-info">
                <div class="quick-name">{{ quick.name }}</div>
                <div class="quick-desc">{{ quick.desc }}</div>
              </div>
              <Icon icon="mdi:chevron-right" :size="20" class="quick-arrow" />
            </div>
          </div>
        </div>

        <!-- 图表区 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="section-card chart-card">
              <div class="section-header">
                <h3 class="section-title">📊 {{ t('wms.warehouseDistribution') }}</h3>
              </div>
              <Echart :options="pieChartOptions" :height="280" />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="section-card chart-card">
              <div class="section-header">
                <h3 class="section-title">📈 {{ t('wms.inOutTrend') }}</h3>
              </div>
              <Echart :options="lineChartOptions" :height="280" />
            </div>
          </el-col>
        </el-row>

        <!-- 热销排行 -->
        <div class="section-card">
          <div class="section-header">
            <h3 class="section-title">
              <Icon icon="mdi:fire" class="mr-2" />
              🔥 {{ t('wms.hotProducts') }}
            </h3>
            <el-tag type="success" size="small">{{ t('wms.topFive') }}</el-tag>
          </div>
          <div class="ranking-list">
            <div v-for="(item, index) in topProducts" :key="index" class="ranking-item">
              <div class="rank-badge" :class="`rank-${index + 1}`">
                <span v-if="index < 3">{{ ['🥇', '🥈', '🥉'][index] }}</span>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <div class="product-info">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-cat">{{ item.category }}</div>
              </div>
              <div class="product-stats">
                <div class="product-value">{{ item.sales }}</div>
                <el-progress
                  :percentage="item.progress"
                  :stroke-width="6"
                  :show-text="false"
                  :color="getRankColor(index)"
                />
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧列 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <!-- 库存预警 -->
        <div class="section-card alert-card">
          <div class="section-header">
            <h3 class="section-title">
              <Icon icon="mdi:alert-circle-outline" class="mr-2" />
              ⚠️ {{ t('wms.stockWarning') }}
            </h3>
            <el-badge :value="stockAlerts.length" type="danger" />
                  </div>
          <div class="alert-list">
            <div v-for="(alert, index) in stockAlerts" :key="index" class="alert-item">
              <div class="alert-indicator" :class="`level-${alert.level}`"></div>
              <div class="alert-content">
                <div class="alert-name">{{ alert.goods }}</div>
                <div class="alert-detail">
                  <span class="alert-stock">{{ t('wms.stock') }}: {{ alert.stock }}</span>
                  <span class="alert-min">{{ t('wms.minStock') }}: {{ alert.min }}</span>
                </div>
              </div>
              <el-button size="small" type="primary" link>{{ t('wms.handle') }}</el-button>
                </div>
          </div>
        </div>

        <!-- 待办任务 -->
        <div class="section-card todo-card">
          <div class="section-header">
            <h3 class="section-title">
              <Icon icon="mdi:clipboard-check-outline" class="mr-2" />
              ✅ {{ t('wms.todayTasks') }}
            </h3>
            <div class="todo-progress">
              {{ todoList.filter(t => t.done).length }}/{{ todoList.length }}
            </div>
                </div>
          <div class="todo-list">
            <div v-for="(task, index) in todoList" :key="index" class="todo-item">
              <el-checkbox v-model="task.done" @change="handleTodoChange" />
              <div class="todo-content" :class="{ completed: task.done }">
                <div class="todo-text">{{ task.text }}</div>
                <div class="todo-time">{{ task.time }}</div>
              </div>
            </div>
          </div>
                </div>

        <!-- 实时动态 -->
        <div class="section-card activity-card">
          <div class="section-header">
            <h3 class="section-title">
              <Icon icon="mdi:pulse" class="mr-2" />
              💬 {{ t('wms.realtimeActivity') }}
            </h3>
                </div>
          <div class="activity-timeline">
            <div v-for="(activity, index) in recentActivities" :key="index" class="timeline-item">
              <div class="timeline-dot" :style="{ background: activity.color }"></div>
              <div class="timeline-content">
                <div class="timeline-text">{{ activity.desc }}</div>
                <div class="timeline-time">{{ activity.time }}</div>
              </div>
            </div>
          </div>
        </div>
    </el-col>
  </el-row>
  </div>
</template>

<script lang="ts" setup>
import { reactive, computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@/components/Icon'
import { useUserStore } from '@/store/modules/user'
import type { EChartsOption } from 'echarts'
import * as HomeApi from '@/api/wms/home'

defineOptions({ name: 'Home' })

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const avatar = userStore.getUser.avatar
const username = userStore.getUser.nickname

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' })
})

const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return t('wms.greeting.dawn')
  if (hour < 9) return t('wms.greeting.morning')
  if (hour < 12) return t('wms.greeting.forenoon')
  if (hour < 14) return t('wms.greeting.noon')
  if (hour < 17) return t('wms.greeting.afternoon')
  if (hour < 19) return t('wms.greeting.evening')
  if (hour < 22) return t('wms.greeting.night')
  return t('wms.greeting.lateNight')
}

// 统计数据
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

// Hero统计
const heroStats = computed(() => [
  { label: t('wms.totalInventory'), value: statisticsData.value.totalInventory || 0, icon: 'mdi:cube-outline', gradient: 'linear-gradient(135deg, #409eff 0%, #79bbff 100%)' },
  { label: t('wms.todayInbound'), value: statisticsData.value.todayInbound || 0, icon: 'mdi:arrow-down-circle-outline', gradient: 'linear-gradient(135deg, #67c23a 0%, #95d475 100%)' },
  { label: t('wms.todayOutbound'), value: statisticsData.value.todayOutbound || 0, icon: 'mdi:arrow-up-circle-outline', gradient: 'linear-gradient(135deg, #e6a23c 0%, #eebe77 100%)' }
])

// 数据卡片
const dataCards = computed(() => [
  { label: t('wms.totalOrders'), value: 3856, trend: 12.5, icon: 'mdi:file-document-multiple-outline' },
  { label: t('wms.processing'), value: 842, trend: 8.3, icon: 'mdi:clock-outline' },
  { label: t('wms.completed'), value: 2891, trend: -3.2, icon: 'mdi:check-circle-outline' },
  { label: t('wms.warningCount'), value: statisticsData.value.inventoryWarning || 0, trend: statisticsData.value.inventoryWarningTrend || 0, icon: 'mdi:alert-circle-outline' }
])

// 快捷入口
const quickAccess = computed(() => [
  { name: t('wms.newInbound'), desc: t('wms.createInbound'), icon: 'mdi:package-down', route: '/wms/inbound', gradient: '#ecf5ff', color: '#409eff' },
  { name: t('wms.newOutbound'), desc: t('wms.createOutbound'), icon: 'mdi:package-up', route: '/wms/outbound', gradient: '#fef0f0', color: '#f56c6c' },
  { name: t('wms.stocktakingPlan'), desc: t('wms.startStocktaking'), icon: 'mdi:clipboard-text-outline', route: '/wms/stocktaking-plan', gradient: '#f0f9eb', color: '#67c23a' },
  { name: t('wms.dataReport'), desc: t('wms.viewStatistics'), icon: 'mdi:chart-line', route: '/wms/report/inventory', gradient: '#fdf6ec', color: '#e6a23c' }
])

// 库存预警
const stockAlerts = ref<HomeApi.InventoryWarningVO[]>([])

// 待办任务
const todoList = reactive([
  { text: '审核入库单 WH-IN-001', time: '10分钟前', done: false },
  { text: '处理出库申请 WH-OUT-045', time: '30分钟前', done: false },
  { text: '完成A区盘点任务', time: '1小时前', done: true }
])

// 实时动态
const recentActivities = ref<HomeApi.RecentActivityVO[]>([])

// 热门商品数据
const popularGoodsData = ref<HomeApi.PopularGoodsVO>({
  names: [],
  values: []
})

// 热销商品
const topProducts = computed(() => {
  return popularGoodsData.value.names.slice(0, 5).map((name, index) => ({
    name: name,
    category: t('wms.phone'), // 可以根据商品名称动态确定分类
    sales: popularGoodsData.value.values[index]?.toLocaleString() || '0',
    progress: index === 0 ? 100 : Math.round((popularGoodsData.value.values[index] / popularGoodsData.value.values[0]) * 100)
  }))
})

// 饼图
const pieChartOptions = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '5%', left: 'center' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: {
      borderRadius: 8,
      borderColor: '#fff',
      borderWidth: 2
    },
    label: { show: false },
    emphasis: {
      label: { show: true, fontSize: 16, fontWeight: 'bold' }
    },
    data: [
      { value: 1048, name: t('wms.warehouseA') },
      { value: 735, name: t('wms.warehouseB') },
      { value: 580, name: t('wms.warehouseC') },
      { value: 484, name: t('wms.warehouseD') }
    ],
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c']
  }]
}))

// 趋势数据
const trendData = ref<HomeApi.TrendDataVO>({
  dates: [],
  inboundData: [],
  outboundData: []
})

// 折线图
const lineChartOptions = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: [t('wms.inbound'), t('wms.outbound')], top: '0' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: trendData.value.dates.length > 0 ? trendData.value.dates.map((_, index) => t('wms.' + ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'][index % 7])) : [t('wms.monday'), t('wms.tuesday'), t('wms.wednesday'), t('wms.thursday'), t('wms.friday'), t('wms.saturday'), t('wms.sunday')] },
  yAxis: { type: 'value' },
  series: [
    {
      name: t('wms.inbound'),
      type: 'line',
      smooth: true,
      data: trendData.value.inboundData.length > 0 ? trendData.value.inboundData : [1200, 1320, 1010, 1340, 1290, 1330, 1420],
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0)' }
          ]
        }
      },
      itemStyle: { color: '#409eff' }
    },
    {
      name: t('wms.outbound'),
      type: 'line',
      smooth: true,
      data: trendData.value.outboundData.length > 0 ? trendData.value.outboundData : [1120, 1420, 1010, 1340, 1590, 1430, 1520],
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0)' }
          ]
        }
      },
      itemStyle: { color: '#67c23a' }
    }
  ]
}))

const getRankColor = (index: number) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  return colors[index] || '#94a3b8'
}

const handleNavigate = (route: string) => {
  router.push(route)
}

const handleTodoChange = () => {
  // 处理待办任务变化
}

// 加载数据的函数
const loadData = async () => {
  // TODO: 等后端接口实现后，取消下面注释，启用真实数据加载
  // 目前使用默认数据，避免接口不存在的错误提示
  
  // 使用默认统计数据
  statisticsData.value = {
    totalInventory: 125680,
    todayInbound: 1856,
    todayOutbound: 2143,
    inventoryWarning: 23,
    totalInventoryTrend: 12.5,
    todayInboundTrend: 8.3,
    todayOutboundTrend: -3.2,
    inventoryWarningTrend: -15.6
  }
  
  // 使用默认预警数据
  stockAlerts.value = [
    { goods: 'iPhone 15 Pro Max', stock: 15, min: 50, level: 'critical' },
    { goods: 'MacBook Pro 16寸', stock: 28, min: 30, level: 'warning' },
    { goods: 'iPad Air 5代', stock: 45, min: 80, level: 'critical' }
  ]
  
  // 使用默认实时动态数据
  recentActivities.value = [
    { desc: 'WH-IN-001 入库完成', time: '2分钟前', color: '#10b981', type: '入库单' },
    { desc: 'WH-OUT-045 正在拣货', time: '5分钟前', color: '#f59e0b', type: '出库单' },
    { desc: 'A区盘点完成', time: '10分钟前', color: '#3b82f6', type: '盘点' },
    { desc: '库存预警: iPhone 15', time: '15分钟前', color: '#ef4444', type: '预警' }
  ]
  
  // 使用默认热门商品数据
  popularGoodsData.value = {
    names: ['iPhone 15 Pro Max', 'MacBook Pro 16寸', 'iPad Air 5代', 'AirPods Pro 2代', 'Apple Watch Ultra'],
    values: [2580, 1890, 1520, 1280, 1050]
  }
  
  /* 
  // 等后端接口实现后，使用下面的代码替换上面的默认数据
  try {
    // 加载统计数据
    statisticsData.value = await HomeApi.getHomeStatistics()
    
    // 加载趋势数据
    trendData.value = await HomeApi.getTrendData()
    
    // 加载预警列表
    stockAlerts.value = await HomeApi.getWarningList()
    
    // 加载实时动态
    recentActivities.value = await HomeApi.getRecentActivities()
    
    // 加载热门商品数据
    popularGoodsData.value = await HomeApi.getPopularGoods('outbound')
  } catch (error) {
    console.error('Failed to load home data:', error)
  }
  */
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.modern-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #e3f2fd 0%, #fce4ec 100%);
  padding: 20px;
}

// Hero区域 - 柔和的渐变
.hero-section {
  position: relative;
  background: linear-gradient(135deg, #409eff 0%, #5eb3f6 100%);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(30deg, transparent 12%, rgba(255,255,255,0.05) 12%, rgba(255,255,255,0.05) 14%, transparent 14%);
  background-size: 60px 60px;
}

.floating-shapes {
  position: absolute;
  inset: 0;
}

.shape {
  position: absolute;
  font-size: 40px;
  opacity: 0.15;
  animation: floatShape 15s ease-in-out infinite;
  
  &.shape-1 {
    top: 10%;
    left: 10%;
    animation-delay: 0s;
  }
  
  &.shape-2 {
    top: 20%;
    right: 15%;
    animation-delay: 2s;
  }
  
  &.shape-3 {
    bottom: 20%;
    left: 20%;
    animation-delay: 4s;
  }
  
  &.shape-4 {
    bottom: 15%;
    right: 10%;
    animation-delay: 6s;
  }
}

@keyframes floatShape {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(10deg);
  }
}

.hero-content {
    position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  
  @media (max-width: 1024px) {
    flex-direction: column;
    align-items: flex-start;
  }
}

.welcome-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-avatar {
  border: 3px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.welcome-text {
  color: white;
}

.hero-title {
      font-size: 28px;
  font-weight: 800;
  margin: 0 0 6px 0;
  color: white;
  
  .wave-emoji {
    display: inline-block;
    animation: wave 2s ease-in-out infinite;
  }
}

@keyframes wave {
  0%, 100% {
    transform: rotate(0deg);
  }
  10%, 30% {
    transform: rotate(14deg);
  }
  20%, 40% {
    transform: rotate(-8deg);
  }
  50% {
    transform: rotate(14deg);
  }
  60% {
    transform: rotate(0deg);
  }
}

.hero-subtitle {
  font-size: 14px;
  opacity: 0.95;
  margin: 0;
}

.hero-stats {
  display: flex;
  gap: 24px;
  
  @media (max-width: 768px) {
    width: 100%;
    justify-content: space-between;
    gap: 12px;
  }
}

.hero-stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-icon-wrapper {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
  color: white;
}

.stat-details {
  .stat-label {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 4px;
  }
  
  .stat-number {
    font-size: 24px;
    font-weight: 800;
    color: white;
  }
}

// 数据概览
.data-overview {
  margin-bottom: 20px;
}

.metric-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 16px;
  align-items: center;
  transition: all 0.2s ease;
  border-left: 3px solid;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &.card-theme-1 {
    border-color: #409eff;
    
    .metric-icon {
      background: #ecf5ff;
      color: #409eff;
    }
  }
  
  &.card-theme-2 {
    border-color: #67c23a;
    
    .metric-icon {
      background: #f0f9eb;
      color: #67c23a;
    }
  }
  
  &.card-theme-3 {
    border-color: #e6a23c;
    
    .metric-icon {
      background: #fdf6ec;
      color: #e6a23c;
    }
  }
  
  &.card-theme-4 {
    border-color: #f56c6c;
    
    .metric-icon {
      background: #fef0f0;
      color: #f56c6c;
    }
  }
}

.metric-icon {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.metric-body {
  flex: 1;
}

.metric-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.metric-value {
  font-size: 26px;
  font-weight: 800;
  color: #1f2937;
  line-height: 1;
  margin-bottom: 6px;
}

.metric-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  
  &.badge-up {
    background: #d1fae5;
    color: #10b981;
  }
  
  &.badge-down {
    background: #fee2e2;
    color: #ef4444;
  }
}

// 通用卡片
.section-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  display: flex;
  align-items: center;
}

// 快捷网格
.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
    
    &:hover {
    background: #f1f5f9;
      transform: translateX(4px);
  }
}

.quick-icon {
  width: 48px;
  height: 48px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
  flex-shrink: 0;
}

.quick-info {
  flex: 1;
  min-width: 0;
}

.quick-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
}

.quick-desc {
  font-size: 12px;
  color: #94a3b8;
}

.quick-arrow {
  color: #cbd5e1;
}

// 排行榜
.ranking-list {
  .ranking-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px;
    border-radius: 10px;
    margin-bottom: 10px;
    background: #f8fafc;
    transition: all 0.3s ease;
    
    &:hover {
      background: #f1f5f9;
    }
  }
}

.rank-badge {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  background: #e2e8f0;
  color: #64748b;
  flex-shrink: 0;
  
  &.rank-1, &.rank-2, &.rank-3 {
    background: transparent;
  }
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
}

.product-cat {
  font-size: 12px;
  color: #94a3b8;
}

.product-stats {
  text-align: right;
  min-width: 100px;
}

.product-value {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
}

// 预警列表
.alert-list {
  .alert-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px;
    border-radius: 10px;
    margin-bottom: 10px;
    background: #fef2f2;
  }
}

.alert-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  
  &.level-critical {
    background: #ef4444;
    box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.2);
  }
  
  &.level-warning {
    background: #f59e0b;
    box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.2);
  }
}

.alert-content {
  flex: 1;
  min-width: 0;
}

.alert-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.alert-detail {
  font-size: 12px;
  color: #64748b;
  display: flex;
  gap: 12px;
}

// 待办列表
.todo-list {
  .todo-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      background: #f8fafc;
    }
  }
}

.todo-content {
  flex: 1;
  
  &.completed {
    .todo-text {
      text-decoration: line-through;
      opacity: 0.5;
    }
  }
}

.todo-text {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
}

.todo-time {
  font-size: 12px;
  color: #94a3b8;
}

.todo-progress {
  font-size: 13px;
  font-weight: 600;
  color: #667eea;
  padding: 4px 12px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 12px;
}

// 时间轴
.activity-timeline {
  .timeline-item {
    display: flex;
    gap: 12px;
    padding-bottom: 16px;
    position: relative;
    
    &:not(:last-child)::after {
      content: '';
      position: absolute;
      left: 3px;
      top: 24px;
      bottom: 0;
      width: 2px;
      background: #e2e8f0;
    }
  }
}

.timeline-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.5);
}

.timeline-content {
  flex: 1;
}

.timeline-text {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
}

.timeline-time {
  font-size: 12px;
  color: #94a3b8;
}

// ==================== 暗色主题适配 ====================
.dark {
  .modern-dashboard {
    background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  }

  .hero-section {
    background: var(--home-greeting-bg);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }

  .metric-card {
    background: var(--home-stat-card-bg);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    }
    
    &.card-theme-1 .metric-icon {
      background: rgba(64, 158, 255, 0.15);
    }
    
    &.card-theme-2 .metric-icon {
      background: rgba(103, 194, 58, 0.15);
    }
    
    &.card-theme-3 .metric-icon {
      background: rgba(230, 162, 60, 0.15);
    }
    
    &.card-theme-4 .metric-icon {
      background: rgba(245, 108, 108, 0.15);
    }
  }

  .metric-label {
    color: #cbd5e1;
  }

  .metric-value {
    color: #f8fafc;
  }

  .section-card {
    background: var(--home-chart-bg);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }

  .section-title {
    color: #f8fafc;
  }

  .quick-card {
    background: var(--home-quick-card-bg);
    
    &:hover {
      background: #38383a;
    }
  }

  .quick-name {
    color: #f8fafc;
  }

  .quick-desc {
    color: #cbd5e1;
  }

  .quick-arrow {
    color: #64748b;
  }

  // 排行榜暗色适配
  .ranking-item {
    background: #2c2c2e !important;
    
    &:hover {
      background: #38383a !important;
    }
  }

  .rank-badge {
    background: #38383a;
    color: #94a3b8;
  }

  .product-name {
    color: #f8fafc;
  }

  .product-cat {
    color: #cbd5e1;
  }

  .product-value {
    color: #f8fafc;
  }

  // 预警列表暗色适配
  .alert-item {
    background: rgba(239, 68, 68, 0.1) !important;
    
    &:hover {
      background: rgba(239, 68, 68, 0.15) !important;
    }
  }

  .alert-name {
    color: #f8fafc;
  }

  .alert-detail {
    color: #cbd5e1;
  }

  // 待办列表暗色适配
  .todo-item {
    &:hover {
      background: #2c2c2e;
    }
  }

  .todo-text {
    color: #f8fafc;
  }

  .todo-time {
    color: #cbd5e1;
  }

  .todo-progress {
    color: #818cf8;
    background: rgba(129, 140, 248, 0.15);
  }

  // 时间轴暗色适配
  .timeline-text {
    color: #f8fafc;
  }

  .timeline-time {
    color: #cbd5e1;
  }

  .timeline-item:not(:last-child)::after {
    background: #38383a;
  }

  .timeline-dot {
    box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.3);
  }
}
</style>
