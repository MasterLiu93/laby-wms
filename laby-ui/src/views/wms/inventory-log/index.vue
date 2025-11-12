<!--
  库存流水列表页
  
  功能说明：
  1. 库存流水的查询功能
  2. 支持多条件搜索（仓库、商品、批次、操作类型等）
  3. 支持分页显示
  4. 权限控制：wms:inventory-log:query
  
  @author laby
  @date 2025-10-28
-->
<template>
  <ContentWrap v-if="showSearch">
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
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
      
      <el-form-item :label="t('wms.goods')" prop="goodsId">
        <el-select v-model="queryParams.goodsId" :placeholder="t('wms.goodsPlaceholder')" clearable class="!w-240px" filterable>
          <el-option
            v-for="goods in goodsList"
            :key="goods.id"
            :label="`${goods.skuCode} - ${goods.goodsName}`"
            :value="goods.id"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item :label="t('wms.operationType')" prop="operationType">
        <el-select v-model="queryParams.operationType" :placeholder="t('wms.operationTypePlaceholder')" clearable class="!w-240px">
          <el-option :label="t('wms.inbound')" value="INBOUND" />
          <el-option :label="t('wms.outbound')" value="OUTBOUND" />
          <el-option :label="t('wms.move')" value="MOVE" />
          <el-option :label="t('wms.lock')" value="LOCK" />
          <el-option :label="t('wms.unlock')" value="UNLOCK" />
        </el-select>
      </el-form-item>
      
      <el-form-item :label="t('common.createTime')" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          :start-placeholder="t('common.startTimeText')"
          :end-placeholder="t('common.endTimeText')"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />{{ t('common.query') }}</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />{{ t('common.reset') }}</el-button>
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
      <el-table-column v-if="columns.warehouseName.visible" :label="t('wms.warehouse')" prop="warehouseName" width="120" />
      <el-table-column v-if="columns.goodsName.visible" :label="t('wms.goodsInfo')" min-width="200">
        <template #default="scope">
          <div>{{ scope.row.skuCode }}</div>
          <div class="text-gray-500 text-xs">{{ scope.row.goodsName }}</div>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.locationCode.visible" :label="t('wms.location')" prop="locationCode" width="120" />
      <el-table-column v-if="columns.batchNo.visible" :label="t('wms.batchNo')" prop="batchNo" width="140" />
      <el-table-column v-if="columns.operationType.visible" :label="t('wms.operationType')" prop="operationType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.operationType === 'INBOUND'" type="success">{{ t('wms.inbound') }}</el-tag>
          <el-tag v-else-if="scope.row.operationType === 'OUTBOUND'" type="danger">{{ t('wms.outbound') }}</el-tag>
          <el-tag v-else-if="scope.row.operationType === 'MOVE'" type="warning">{{ t('wms.move') }}</el-tag>
          <el-tag v-else-if="scope.row.operationType === 'LOCK'" type="info">{{ t('wms.lock') }}</el-tag>
          <el-tag v-else-if="scope.row.operationType === 'UNLOCK'">{{ t('wms.unlock') }}</el-tag>
          <el-tag v-else>{{ scope.row.operationType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.changeQuantity.visible" :label="t('wms.changeQuantity')" prop="quantityChange" width="100" align="right">
        <template #default="scope">
          <span :class="scope.row.quantityChange > 0 ? 'text-green-500' : 'text-red-500'">
            {{ scope.row.quantityChange > 0 ? '+' : '' }}{{ scope.row.quantityChange }}
          </span>
        </template>
      </el-table-column>
      <el-table-column v-if="columns.beforeQuantity.visible" :label="t('wms.beforeQuantity')" prop="quantityBefore" width="100" align="right" />
      <el-table-column v-if="columns.afterQuantity.visible" :label="t('wms.afterQuantity')" prop="quantityAfter" width="100" align="right" />
      <el-table-column v-if="columns.businessType.visible" :label="t('wms.businessType')" prop="businessType" width="100" />
      <el-table-column v-if="columns.businessNo.visible" :label="t('wms.businessNo')" prop="businessNo" width="150" show-overflow-tooltip />
      <el-table-column v-if="columns.operator.visible" :label="t('wms.operator')" prop="operator" width="100" />
      <el-table-column v-if="columns.createTime.visible" :label="t('wms.operateTime')" prop="createTime" width="160" :formatter="dateFormatter" />
      <el-table-column v-if="columns.remark.visible" :label="t('form.remark')" prop="remark" min-width="150" show-overflow-tooltip />
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
import * as InventoryLogApi from '@/api/wms/inventory-log'
import * as WarehouseApi from '@/api/wms/warehouse'
import * as GoodsApi from '@/api/wms/goods'
import { dateFormatter } from '@/utils/formatTime'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

defineOptions({ name: 'WmsInventoryLog' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref([])
const total = ref(0)
const warehouseList = ref([])
const goodsList = ref([])

// 列设置功能
const columns = reactive(createWMSColumns(t).inventoryLog)
const showSearch = ref(true)

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  warehouseId: undefined,
  goodsId: undefined,
  locationId: undefined,
  batchNo: undefined,
  operationType: undefined,
  businessType: undefined,
  businessNo: undefined,
  createTime: undefined
})
const queryFormRef = ref()

/**
 * 查询列表
 */
const getList = async () => {
  loading.value = true
  try {
    const data = await InventoryLogApi.getInventoryLogPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/**
 * 搜索按钮操作
 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/**
 * 重置按钮操作
 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/**
 * 初始化
 */
onMounted(async () => {
  await getList()
  // 加载下拉数据
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
  goodsList.value = await GoodsApi.getGoodsSimpleList()
})
</script>

