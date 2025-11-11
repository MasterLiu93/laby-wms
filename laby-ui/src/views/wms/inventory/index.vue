<!--
  库存管理列表页
  
  功能说明：
  1. 库存的查询、新增、编辑、删除
  2. 支持多条件搜索（仓库、库位、商品、批次、序列号、状态等）
  3. 显示可用数量（库存数量-锁定数量）
  4. 关联显示仓库名、库位编码、商品名等信息
  5. 权限控制：wms:inventory:query、create、update、delete
  
  @author laby
  @date 2025-10-28
-->
<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="88px"
    >
      <!-- 仓库下拉框 -->
      <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
        <el-select
          v-model="queryParams.warehouseId"
          :placeholder="t('common.selectText')"
          clearable
          class="!w-240px"
          @change="handleWarehouseChange"
        >
          <el-option
            v-for="warehouse in warehouseList"
            :key="warehouse.id"
            :label="warehouse.warehouseName"
            :value="warehouse.id"
          />
        </el-select>
      </el-form-item>
      
      <!-- 库位下拉框（联动） -->
      <el-form-item :label="t('wms.location')" prop="locationId">
        <el-select
          v-model="queryParams.locationId"
          :placeholder="t('common.selectText')"
          clearable
          class="!w-240px"
          :disabled="!queryParams.warehouseId"
        >
          <el-option
            v-for="location in locationList"
            :key="location.id"
            :label="location.locationCode"
            :value="location.id"
          />
        </el-select>
      </el-form-item>
      
      <!-- 商品名称搜索 -->
      <el-form-item :label="t('wms.goodsName')" prop="goodsName">
        <el-input
          v-model="queryParams.goodsName"
          :placeholder="t('wms.goodsNamePlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      
      <!-- SKU编码搜索 -->
      <el-form-item :label="t('wms.skuCode')" prop="skuCode">
        <el-input
          v-model="queryParams.skuCode"
          :placeholder="t('wms.skuCodePlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      
      <!-- 批次号搜索 -->
      <el-form-item :label="t('wms.batchNo')" prop="batchNo">
        <el-input
          v-model="queryParams.batchNo"
          :placeholder="t('wms.batchNoPlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      
      <!-- 状态字典下拉框 -->
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="queryParams.status"
          :placeholder="t('common.selectText')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_INVENTORY_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      
      <!-- 操作按钮 -->
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> {{ t('common.query') }}</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> {{ t('common.reset') }}</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['wms:inventory:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> {{ t('action.add') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column :label="t('wms.warehouse')" align="center" prop="warehouseName" width="120px" />
      
      <el-table-column :label="t('wms.area')" align="center" prop="areaName" width="120px">
        <template #default="scope">
          {{ scope.row.areaName || '-' }}
        </template>
      </el-table-column>
      
      <el-table-column :label="t('wms.location')" align="center" prop="locationCode" width="160px">
        <template #default="scope">
          <div v-if="scope.row.locationId">
            <div class="font-semibold">
              {{ t('wms.locationFormat').replace('{row}', scope.row.rowNo).replace('{column}', scope.row.columnNo).replace('{layer}', scope.row.layerNo) }}
            </div>
            <div class="text-gray-400 text-xs">
              {{ scope.row.locationCode }}
            </div>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      
      <el-table-column :label="t('wms.goodsInfo')" align="center" min-width="200px">
        <template #default="scope">
          <div class="text-left">
            <div class="font-bold">{{ scope.row.goodsName }}</div>
            <div class="text-gray-400 text-sm">SKU: {{ scope.row.skuCode }}</div>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column :label="t('wms.batchSerial')" align="center" width="150px">
        <template #default="scope">
          <div v-if="scope.row.batchNo || scope.row.serialNo">
            <div v-if="scope.row.batchNo">{{ t('wms.batchNo') }}: {{ scope.row.batchNo }}</div>
            <div v-if="scope.row.serialNo">{{ t('wms.serialNo') }}: {{ scope.row.serialNo }}</div>
          </div>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      
      <el-table-column :label="t('wms.quantity')" align="center" prop="quantity" width="100px">
        <template #default="scope">
          <el-tag type="primary">{{ scope.row.quantity }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column :label="t('wms.lockQuantity')" align="center" prop="lockQuantity" width="100px">
        <template #default="scope">
          <el-tag v-if="scope.row.lockQuantity > 0" type="warning">{{ scope.row.lockQuantity }}</el-tag>
          <span v-else class="text-gray-400">0</span>
        </template>
      </el-table-column>
      
      <el-table-column :label="t('wms.availableQuantity')" align="center" prop="availableQuantity" width="100px">
        <template #default="scope">
          <el-tag type="success">{{ scope.row.availableQuantity || (scope.row.quantity - scope.row.lockQuantity) }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column :label="t('common.status')" align="center" width="100px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_INVENTORY_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      
      <el-table-column
        :label="t('common.createTime')"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      
      <!-- 操作列 -->
      <el-table-column :label="t('table.action')" align="center" fixed="right" width="180px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['wms:inventory:update']"
          >
            {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['wms:inventory:delete']"
          >
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
  <InventoryForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as InventoryApi from '@/api/wms/inventory'
import * as WarehouseApi from '@/api/wms/warehouse'
import * as WarehouseLocationApi from '@/api/wms/location'
import InventoryForm from './InventoryForm.vue'

defineOptions({ name: 'WmsInventory' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(false) // 列表加载中
const list = ref([]) // 列表数据
const total = ref(0) // 总条数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  warehouseId: undefined,
  locationId: undefined,
  goodsName: undefined,
  skuCode: undefined,
  batchNo: undefined,
  status: undefined
})
const queryFormRef = ref()

// 仓库和库位数据
const warehouseList = ref([])
const locationList = ref([])

/**
 * 查询列表
 */
const getList = async () => {
  loading.value = true
  try {
    const data = await InventoryApi.getInventoryPage(queryParams)
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
 * 仓库变更，加载对应的库位列表
 */
const handleWarehouseChange = async (warehouseId: number) => {
  queryParams.locationId = undefined
  locationList.value = []
  
  if (warehouseId) {
    try {
      const data = await WarehouseLocationApi.getWarehouseLocationSimpleList({ warehouseId })
      locationList.value = data
    } catch (e) {
      console.error('加载库位列表失败', e)
    }
  }
}

/**
 * 打开表单
 * @param type 表单类型 'create' | 'update'
 * @param id 库存ID
 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/**
 * 删除操作
 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm(t('wms.deleteInventoryConfirm'))
    await InventoryApi.deleteInventory(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/**
 * 初始化
 */
onMounted(async () => {
  // 自动加载数据
  await getList()
  
  // 加载仓库列表
  try {
    warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
  } catch (e) {
    console.error('加载仓库列表失败', e)
  }
})
</script>

