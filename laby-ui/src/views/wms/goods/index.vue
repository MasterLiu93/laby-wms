<template>
  <ContentWrap v-if="showSearch">
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item :label="t('wms.skuCode')" prop="skuCode">
        <el-input
          v-model="queryParams.skuCode"
          :placeholder="t('wms.skuCodePlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.goodsName')" prop="goodsName">
        <el-input
          v-model="queryParams.goodsName"
          :placeholder="t('wms.goodsNamePlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.category')" prop="categoryId">
        <el-tree-select
          v-model="queryParams.categoryId"
          :data="categoryTree"
          :props="{ label: 'categoryName', value: 'id' }"
          :placeholder="t('common.selectText')"
          clearable
          filterable
          check-strictly
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.brand')" prop="brand">
        <el-input
          v-model="queryParams.brand"
          :placeholder="t('wms.brandPlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('common.selectText')" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery" type="primary">
          <Icon icon="ep:search" class="mr-5px" /> {{ t('common.query') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> {{ t('common.reset') }}
        </el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['wms:goods:create']">
          <Icon icon="ep:plus" class="mr-5px" /> {{ t('wms.addGoods') }}
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
      <RightToolbar 
        v-model:showSearch="showSearch"
        :columns="columns"
        :search="true"
        @queryTable="getList"
      />
    </div>
    
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column 
        v-if="columns.skuCode.visible" 
        :label="t('wms.skuCode')" 
        align="center" 
        prop="skuCode" 
        width="150px" 
      />
      <el-table-column 
        v-if="columns.goodsName.visible" 
        :label="t('wms.goodsName')" 
        align="center" 
        prop="goodsName" 
        min-width="200px" 
        show-overflow-tooltip 
      />
      
      <el-table-column 
        v-if="columns.category.visible" 
        :label="t('wms.category')" 
        align="center" 
        width="130px"
      >
        <template #default="scope">
          <el-tag v-if="scope.row.categoryName" type="info" size="small">
            {{ scope.row.categoryName }}
          </el-tag>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      
      <el-table-column 
        v-if="columns.brand.visible" 
        :label="t('wms.brand')" 
        align="center" 
        prop="brand" 
        width="120px"
      >
        <template #default="scope">
          {{ scope.row.brand || '-' }}
        </template>
      </el-table-column>
      
      <el-table-column 
        v-if="columns.model.visible" 
        :label="t('wms.model')" 
        align="center" 
        prop="model" 
        width="120px" 
        show-overflow-tooltip
      >
        <template #default="scope">
          {{ scope.row.model || '-' }}
        </template>
      </el-table-column>
      
      <el-table-column 
        v-if="columns.unit.visible" 
        :label="t('wms.unit')" 
        align="center" 
        width="80px"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_GOODS_UNIT" :value="scope.row.unit" />
        </template>
      </el-table-column>
      
      <el-table-column 
        v-if="columns.spec.visible" 
        :label="t('wms.spec')" 
        align="center" 
        prop="spec" 
        width="120px"
      >
        <template #default="scope">
          {{ scope.row.spec || '-' }}
        </template>
      </el-table-column>
      
      <el-table-column 
        v-if="columns.safetyStock.visible" 
        :label="t('wms.safetyStock')" 
        align="center" 
        prop="safetyStock" 
        width="100px"
      >
        <template #default="scope">
          <el-tag v-if="scope.row.safetyStock > 0" type="warning" size="small">
            {{ scope.row.safetyStock }}
          </el-tag>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      
      <el-table-column 
        v-if="columns.batchSerial.visible" 
        :label="t('wms.batchSerial')" 
        align="center" 
        width="120px"
      >
        <template #default="scope">
          <div class="flex gap-1 justify-center">
            <el-tag v-if="scope.row.needBatch" type="warning" size="small">{{ t('wms.batch') }}</el-tag>
            <el-tag v-if="scope.row.needSerial" type="danger" size="small">{{ t('wms.serial') }}</el-tag>
            <span v-if="!scope.row.needBatch && !scope.row.needSerial" class="text-gray-400">-</span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column 
        v-if="columns.status.visible" 
        :label="t('common.status')" 
        align="center" 
        width="80px"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      
      <el-table-column 
        v-if="columns.createTime.visible" 
        :label="t('common.createTime')" 
        align="center" 
        prop="createTime" 
        width="180px"
      >
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      
      <el-table-column :label="t('table.action')" align="center" fixed="right" width="180px">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['wms:goods:update']">
            {{ t('action.edit') }}
          </el-button>
          <el-button link type="primary" @click="openDetail(scope.row)" v-hasPermi="['wms:goods:query']">
            {{ t('action.detail') }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['wms:goods:delete']">
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
  <GoodsForm ref="formRef" @success="getList" />

  <!-- 详情弹窗 -->
  <Dialog :title="t('wms.goodsDetail')" v-model="detailVisible" width="900px">
    <el-descriptions :column="2" border v-loading="detailLoading">
      <el-descriptions-item :label="t('wms.skuCode')">
        <el-tag type="primary">{{ detailData.skuCode }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.goodsName')">
        <strong>{{ detailData.goodsName }}</strong>
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.category')">
        {{ detailData.categoryName || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.brand')">
        {{ detailData.brand || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.model')">
        {{ detailData.model || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.barcode')">
        {{ detailData.barcode || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.unit')">
        <dict-tag :type="DICT_TYPE.WMS_GOODS_UNIT" :value="detailData.unit" />
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.spec')">
        {{ detailData.spec || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.weight')">
        {{ detailData.weight || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.volume')">
        {{ detailData.volume || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.shelfLife')">
        {{ detailData.shelfLife || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.storageTempMin') + ' ~ ' + t('wms.storageTempMax')">
        <span v-if="detailData.storageTempMin || detailData.storageTempMax">
          {{ detailData.storageTempMin || '-' }} ~ {{ detailData.storageTempMax || '-' }}
        </span>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.needBatch')">
        <el-tag :type="detailData.needBatch ? 'success' : 'info'">
          {{ detailData.needBatch ? t('wms.enabled') : t('wms.disabled') }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.needSerial')">
        <el-tag :type="detailData.needSerial ? 'success' : 'info'">
          {{ detailData.needSerial ? t('wms.enabled') : t('wms.disabled') }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.safetyStock')">
        <el-tag type="warning">{{ detailData.safetyStock || 0 }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.maxStock')">
        {{ detailData.maxStock || '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('common.status')">
        <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="detailData.status" />
      </el-descriptions-item>
      <el-descriptions-item :label="t('common.createTime')">
        {{ formatDate(detailData.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('form.remark')" :span="2">
        {{ detailData.remark || '-' }}
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="detailVisible = false">{{ t('common.close') }}</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'

const { getDictLabel } = useDictI18n() // 字典国际化
import { handleTree } from '@/utils/tree'
import * as GoodsApi from '@/api/wms/goods'
import * as GoodsCategoryApi from '@/api/wms/category'
import GoodsForm from './GoodsForm.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'

/** 商品信息 列表 */
defineOptions({ name: 'WmsGoods' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  skuCode: undefined,
  goodsName: undefined,
  categoryId: undefined,
  brand: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单
const categoryTree = ref([]) // 分类树

// 列设置功能 - RuoYi风格
const columns = reactive({
  skuCode: { visible: true, label: t('wms.skuCode') },
  goodsName: { visible: true, label: t('wms.goodsName') },
  category: { visible: true, label: t('wms.category') },
  brand: { visible: true, label: t('wms.brand') },
  model: { visible: true, label: t('wms.model') },
  unit: { visible: true, label: t('wms.unit') },
  spec: { visible: true, label: t('wms.spec') },
  safetyStock: { visible: true, label: t('wms.safetyStock') },
  batchSerial: { visible: true, label: t('wms.batchSerial') },
  status: { visible: true, label: t('common.status') },
  createTime: { visible: true, label: t('common.createTime') }
})

// 显示搜索状态
const showSearch = ref(true)

// 详情相关
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref({} as GoodsApi.GoodsVO)

/** 格式化日期 */
const formatDate = (date: any) => {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

/** 查询列表 */
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

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 打开详情 */
const openDetail = async (row: GoodsApi.GoodsVO) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailData.value = await GoodsApi.getGoods(row.id!)
  } finally {
    detailLoading.value = false
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await GoodsApi.deleteGoods(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(async () => {
  await getList()
  // 加载分类树
  const categoryList = await GoodsCategoryApi.getGoodsCategorySimpleList()
  categoryTree.value = handleTree(categoryList, 'id', 'parentId')
})
</script>

