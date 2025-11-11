<!--
  商品选择器弹窗（出库专用）
  
  功能说明：
  1. 用于出库单选择商品
  2. 支持商品搜索、筛选
  3. 支持多选商品
  4. 选中后回调父组件
  
  @author laby
  @date 2025-10-31
-->
<template>
  <Dialog title="选择商品" v-model="dialogVisible" width="1400px">
    <!-- 搜索条件 -->
    <el-form :model="queryParams" inline class="mb-10px">
      <el-form-item label="商品名称">
        <el-input 
          v-model="queryParams.goodsName" 
          placeholder="请输入商品名称" 
          clearable 
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="SKU编码">
        <el-input 
          v-model="queryParams.skuCode" 
          placeholder="请输入SKU编码" 
          clearable 
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品分类">
        <el-tree-select
          v-model="queryParams.categoryId"
          :data="categoryTree"
          :props="{ label: 'categoryName', value: 'id' }"
          placeholder="请选择分类"
          clearable
          filterable
          check-strictly
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
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
      <el-table-column label="SKU编码" prop="skuCode" width="140" show-overflow-tooltip />
      <el-table-column label="商品名称" prop="goodsName" min-width="200" show-overflow-tooltip />
      <el-table-column label="分类" prop="categoryName" width="120" show-overflow-tooltip />
      <el-table-column label="规格型号" prop="spec" width="150" show-overflow-tooltip />
      <el-table-column label="品牌" prop="brand" width="100" show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" width="80" align="center">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.WMS_GOODS_UNIT" :value="row.unit" />
        </template>
      </el-table-column>
      <el-table-column label="安全库存" prop="safetyStock" width="100" align="right" />
      <el-table-column label="最大库存" prop="maxStock" width="100" align="right" />
      <el-table-column label="状态" prop="status" width="80" align="center">
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
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="selectedGoods.length === 0">
        确定选择（已选 {{ selectedGoods.length }} 个）
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import * as GoodsApi from '@/api/wms/goods'
import * as CategoryApi from '@/api/wms/category'

defineOptions({ name: 'GoodsSelector' })

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
    ElMessage.warning('请至少选择一个商品')
    return
  }
  emit('success', selectedGoods.value)
  dialogVisible.value = false
}

defineExpose({ open })
</script>

