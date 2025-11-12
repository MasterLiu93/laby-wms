<!--
  库存预警列表页
  
  功能说明：
  1. 库存预警的查询功能
  2. 包含低库存预警和即将过期预警
  3. 实时显示预警信息
  4. 权限控制：wms:inventory-warning:query
  
  @author laby
  @date 2025-10-28
-->
<template>
  <ContentWrap>
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-gray-500 text-sm">{{ t('wms.lowStockWarning') }}</div>
              <div class="text-2xl font-bold text-orange-500 mt-2">{{ lowStockCount }}</div>
            </div>
            <Icon icon="ep:warning" class="text-5xl text-orange-300" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-gray-500 text-sm">{{ t('wms.expiringWarning') }}</div>
              <div class="text-2xl font-bold text-red-500 mt-2">{{ expiringCount }}</div>
            </div>
            <Icon icon="ep:clock" class="text-5xl text-red-300" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-gray-500 text-sm">{{ t('wms.totalWarnings') }}</div>
              <div class="text-2xl font-bold text-blue-500 mt-2">{{ totalCount }}</div>
            </div>
            <Icon icon="ep:bell" class="text-5xl text-blue-300" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <el-row class="mb-3">
      <el-radio-group v-model="activeTab" @change="handleTabChange">
        <el-radio-button value="all">{{ t('wms.allWarnings') }}</el-radio-button>
        <el-radio-button value="lowStock">{{ t('wms.lowStockWarning') }}</el-radio-button>
        <el-radio-button value="expiring">{{ t('wms.expiringWarning') }}</el-radio-button>
      </el-radio-group>
      <el-button class="ml-3" @click="handleRefresh">
        <Icon icon="ep:refresh" class="mr-5px" />{{ t('wms.refresh') }}
      </el-button>
    </el-row>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <!-- 表格工具栏 -->
    <div class="flex justify-between items-center mb-4">
      <div class="text-sm text-gray-600">
        {{ t('common.total') }}: {{ totalCount }} {{ t('common.items') }}
      </div>
      <RightToolbar v-model:showSearch="showSearch" :columns="columns" :search="true" @queryTable="getList" />
    </div>
    
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column v-if="columns.warningType.visible" :label="t('wms.warningType')" prop="warningType" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <el-tag v-if="scope.row.warningType === 'LOW_STOCK'" type="warning">{{ t('wms.lowStock') }}</el-tag>
          <el-tag v-else-if="scope.row.warningType === 'EXPIRING'" type="danger">{{ t('wms.expiringSoon') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.warehouseName.visible" :label="t('wms.warehouse')" prop="warehouseName" min-width="120" show-overflow-tooltip />
      <el-table-column v-if="columns.goodsInfo.visible" :label="t('wms.goodsInfo')" min-width="200">
        <template #default="scope">
          <div>{{ scope.row.skuCode }}</div>
          <div class="text-gray-500 text-xs">{{ scope.row.goodsName }}</div>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.batchNo.visible" :label="t('wms.batchNo')" prop="batchNo" min-width="120" show-overflow-tooltip />
      <el-table-column v-if="columns.currentQuantity.visible" :label="t('wms.currentQuantity')" min-width="120" show-overflow-tooltip align="right">
        <template #default="scope">
          <div>{{ t('wms.totalQuantity') }}：{{ scope.row.quantity }}</div>
          <div class="text-gray-500 text-xs" v-if="scope.row.lockQuantity > 0">
            {{ t('wms.lockQuantity') }}：{{ scope.row.lockQuantity }}
          </div>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.availableQuantity.visible" :label="t('wms.availableQuantity')" prop="availableQuantity" min-width="120" show-overflow-tooltip align="right">
        <template #default="scope">
          <span :class="scope.row.availableQuantity < (scope.row.safetyStock || 0) ? 'text-red-500 font-bold' : 'text-green-600'">
            {{ scope.row.availableQuantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.safetyStock.visible" :label="t('wms.safetyStock')" prop="safetyStock" min-width="120" show-overflow-tooltip align="right">
        <template #default="scope">
          <span v-if="scope.row.safetyStock" class="text-blue-500">{{ scope.row.safetyStock }}</span>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.expireInfo.visible" :label="t('wms.expireInfo')" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <div v-if="scope.row.expireDate">
            <div class="text-xs">{{ scope.row.expireDate }}</div>
            <el-tag v-if="scope.row.daysToExpire !== undefined" size="small" :type="scope.row.daysToExpire <= 3 ? 'danger' : 'warning'">
              {{ scope.row.daysToExpire }}{{ t('wms.daysToExpire') }}
            </el-tag>
          </div>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.warningDesc.visible" :label="t('wms.warningDesc')" min-width="200">
        <template #default="scope">
          <div v-if="scope.row.warningType === 'LOW_STOCK'" class="text-orange-600">
            {{ t('wms.lowStockDesc').replace('{available}', scope.row.availableQuantity).replace('{safety}', scope.row.safetyStock) }}
          </div>
          <div v-else-if="scope.row.warningType === 'EXPIRING'" class="text-red-600">
            {{ t('wms.expiringDesc').replace('{batchNo}', scope.row.batchNo).replace('{days}', scope.row.daysToExpire) }}
          </div>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script setup lang="ts">
import * as InventoryWarningApi from '@/api/wms/inventory-warning'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

defineOptions({ name: 'WmsInventoryWarning' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref([])
const activeTab = ref('all')

// 列设置功能
const columns = reactive(createWMSColumns(t).inventoryWarning)
const showSearch = ref(true)

// 统计数据
const lowStockCount = ref(0)
const expiringCount = ref(0)
const totalCount = ref(0)

/**
 * 查询列表
 */
const getList = async () => {
  loading.value = true
  try {
    let data
    if (activeTab.value === 'lowStock') {
      data = await InventoryWarningApi.getLowStockWarnings()
    } else if (activeTab.value === 'expiring') {
      data = await InventoryWarningApi.getExpiringWarnings()
    } else {
      data = await InventoryWarningApi.getAllWarnings()
    }
    list.value = data
    
    // 更新统计数据
    if (activeTab.value === 'all') {
      lowStockCount.value = data.filter(item => item.warningType === 'LOW_STOCK').length
      expiringCount.value = data.filter(item => item.warningType === 'EXPIRING').length
      totalCount.value = data.length
    }
  } finally {
    loading.value = false
  }
}

/**
 * 切换标签
 */
const handleTabChange = () => {
  getList()
}

/**
 * 刷新
 */
const handleRefresh = () => {
  getList()
}

/**
 * 初始化
 */
onMounted(async () => {
  await getList()
})
</script>
