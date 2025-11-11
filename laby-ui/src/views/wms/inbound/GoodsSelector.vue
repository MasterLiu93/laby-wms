<!--
  商品选择器弹窗
  
  功能说明：
  1. 用于入库单/出库单选择商品
  2. 支持商品搜索、筛选
  3. 支持多选商品
  4. 选中后回调父组件
  
  @author laby
  @date 2025-10-30
-->
<template>
  <Dialog :title="t('wms.selectGoodsTitle')" v-model="dialogVisible" width="1400px">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" inline class="mb-10px">
      <el-form-item :label="t('wms.goodsNameLabel')">
        <el-input 
          v-model="queryParams.goodsName" 
          :placeholder="t('wms.goodsNamePlaceholder')" 
          clearable 
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('wms.skuCodeLabel')">
        <el-input 
          v-model="queryParams.skuCode" 
          :placeholder="t('wms.skuCodePlaceholder')" 
          clearable 
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="t('wms.categoryLabel')">
        <el-tree-select
          v-model="queryParams.categoryId"
          :data="categoryTree"
          :props="{ label: 'categoryName', value: 'id' }"
          :placeholder="t('wms.categoryPlaceholder')"
          clearable
          filterable
          check-strictly
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          {{ t('common.query') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          {{ t('common.reset') }}
        </el-button>
      </el-form-item>
    </el-form>
    
    <!-- 商品列表 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="list"
      border
      stripe
      max-height="450px"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column :label="t('wms.skuCode')" prop="skuCode" width="140" show-overflow-tooltip />
      <el-table-column :label="t('wms.goodsName')" prop="goodsName" min-width="200" show-overflow-tooltip />
      <el-table-column :label="t('wms.categoryLabel')" prop="categoryName" width="120" show-overflow-tooltip />
      <el-table-column :label="t('wms.specLabel')" prop="spec" width="150" show-overflow-tooltip />
      <el-table-column :label="t('wms.brandLabel')" prop="brand" width="100" show-overflow-tooltip />
      <el-table-column :label="t('wms.unitLabel')" prop="unit" width="80" align="center">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.WMS_GOODS_UNIT" :value="row.unit" />
        </template>
      </el-table-column>
      <el-table-column :label="t('wms.safetyStockLabel')" prop="safetyStock" width="100" align="right" />
      <el-table-column :label="t('wms.maxStockLabel')" prop="maxStock" width="100" align="right" />
      <el-table-column :label="t('wms.statusLabel')" prop="status" width="80" align="center">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
      class="mt-10px"
    />
    
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('wms.cancelSelect') }}</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="selectedGoods.length === 0">
        {{ t('wms.confirmSelect') }}（{{ t('wms.selectedCount').replace('{count}', selectedGoods.length) }}）
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import * as GoodsApi from '@/api/wms/goods'
import * as CategoryApi from '@/api/wms/category'

defineOptions({ name: 'GoodsSelector' })

const { t } = useI18n() // 国际化

const dialogVisible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selectedGoods = ref([])
const categoryTree = ref([])
const tableRef = ref()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  goodsName: undefined,
  skuCode: undefined,
  categoryId: undefined,
  status: 1 // 只显示启用的商品
})

/**
 * 打开弹窗
 */
const open = async () => {
  dialogVisible.value = true
  selectedGoods.value = []
  queryParams.pageNo = 1
  await Promise.all([
    getList(),
    loadCategoryTree()
  ])
}

/**
 * 查询商品列表
 */
const getList = async () => {
  loading.value = true
  try {
    const data = await GoodsApi.getGoodsPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/**
 * 加载分类树
 */
const loadCategoryTree = async () => {
  const data = await CategoryApi.getGoodsCategoryTree()
  categoryTree.value = data
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
  queryParams.goodsName = undefined
  queryParams.skuCode = undefined
  queryParams.categoryId = undefined
  handleQuery()
}

/**
 * 选择变化
 */
const handleSelectionChange = (selection: any[]) => {
  selectedGoods.value = selection
}

/**
 * 确定选择
 */
const emit = defineEmits(['success'])
const handleConfirm = () => {
  if (selectedGoods.value.length === 0) {
    ElMessage.warning(t('wms.selectAtLeastOne'))
    return
  }
  emit('success', selectedGoods.value)
  dialogVisible.value = false
}

defineExpose({ open })
</script>

