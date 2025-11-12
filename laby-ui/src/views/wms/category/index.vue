<!--
  商品分类列表页（树形表格）
  
  功能说明：
  1. 商品分类的查询、新增、编辑、删除
  2. 支持树形结构展示（最多3级分类）
  3. 支持分类编码、分类名称的模糊搜索
  4. 支持展开/折叠所有节点
  5. 权限控制：wms:category:query、create、update、delete
  
  @author laby
  @date 2025-10-28
-->
<template>
  <ContentWrap v-if="showSearch">
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <!-- 分类编码搜索 -->
      <el-form-item :label="t('wms.categoryCode')" prop="categoryCode">
        <el-input
          v-model="queryParams.categoryCode"
          :placeholder="t('wms.categoryCodePlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <!-- 分类名称搜索 -->
      <el-form-item :label="t('wms.categoryName')" prop="categoryName">
        <el-input
          v-model="queryParams.categoryName"
          :placeholder="t('wms.categoryNamePlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <!-- 状态搜索 -->
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="queryParams.status"
          :placeholder="t('common.selectText')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="getDictLabel(dict)"
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
          v-hasPermi="['wms:category:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> {{ t('action.add') }}
        </el-button>
        <el-button plain @click="toggleExpandAll">
          <Icon icon="ep:sort" class="mr-5px" /> {{ expandAll ? t('wms.collapseAll') : t('wms.expandAll') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表（树形表格） -->
  <ContentWrap>
    <!-- 表格工具栏 -->
    <div class="flex justify-between items-center mb-4">
      <div class="text-sm text-gray-600">
        {{ t('common.total') }}: {{ list.length }} {{ t('common.items') }}
      </div>
      <RightToolbar v-model:showSearch="showSearch" :search="true" @queryTable="getList" />
    </div>
    <el-table
      v-loading="loading"
      :data="list"
      row-key="id"
      :default-expand-all="expandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="categoryName" :label="t('wms.categoryName')" min-width="200px" show-overflow-tooltip />
      <el-table-column prop="categoryCode" :label="t('wms.categoryCode')" width="150px" />
      <el-table-column prop="level" :label="t('wms.level')" width="80px" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.level === 1" type="success">{{ t('wms.levelOne') }}</el-tag>
          <el-tag v-else-if="scope.row.level === 2" type="warning">{{ t('wms.levelTwo') }}</el-tag>
          <el-tag v-else-if="scope.row.level === 3" type="info">{{ t('wms.levelThree') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" :label="t('wms.categorySort')" width="80px" align="center" />
      <el-table-column :label="t('common.status')" width="80px" align="center">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
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
      <el-table-column :label="t('table.action')" align="center" fixed="right" width="230px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['wms:category:update']"
          >
            {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('create', scope.row.id)"
            v-hasPermi="['wms:category:create']"
            v-if="scope.row.level < 3"
          >
            {{ t('wms.addSubCategory') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['wms:category:delete']"
          >
            {{ t('action.delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <!-- 表单弹窗 -->
  <CategoryForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'
import { dateFormatter } from '@/utils/formatTime'
import * as GoodsCategoryApi from '@/api/wms/category'
import CategoryForm from './CategoryForm.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'

const { getDictLabel } = useDictI18n() // 字典国际化

defineOptions({ name: 'WmsGoodsCategory' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true) // 列表加载中
const list = ref<any[]>([]) // 树形列表数据
const expandAll = ref(false) // 是否展开所有节点
const showSearch = ref(true) // 显示搜索

// 查询参数
const queryParams = reactive({
  categoryCode: undefined,
  categoryName: undefined,
  status: undefined
})
const queryFormRef = ref()

/**
 * 查询列表
 */
const getList = async () => {
  loading.value = true
  try {
    const data = await GoodsCategoryApi.getGoodsCategoryList(queryParams)
    // 构建树形结构
    list.value = handleTree(data, 'id', 'parentId')
  } finally {
    loading.value = false
  }
}

/**
 * 将扁平数组转换为树形结构
 * 
 * @param data 扁平数组
 * @param id ID字段名
 * @param parentId 父ID字段名
 * @param children 子节点字段名
 * @returns 树形结构
 */
const handleTree = (data: any[], id: string = 'id', parentId: string = 'parentId', children: string = 'children'): any[] => {
  if (!data || data.length === 0) return []
  
  const config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    childrenList: children || 'children'
  }

  const childrenListMap: Record<any, any[]> = {}
  const nodeIds: Record<any, any> = {}
  const tree: any[] = []

  for (const d of data) {
    const pId = d[config.parentId]
    if (childrenListMap[pId] == null) {
      childrenListMap[pId] = []
    }
    nodeIds[d[config.id]] = d
    childrenListMap[pId].push(d)
  }

  for (const d of data) {
    const pId = d[config.parentId]
    if (nodeIds[pId] == null) {
      tree.push(d)
    }
  }

  for (const t of tree) {
    adaptToChildrenList(t)
  }

  function adaptToChildrenList(o: any) {
    if (childrenListMap[o[config.id]] !== null) {
      o[config.childrenList] = childrenListMap[o[config.id]]
    }
    if (o[config.childrenList]) {
      for (const c of o[config.childrenList]) {
        adaptToChildrenList(c)
      }
    }
  }

  return tree
}

/**
 * 搜索按钮操作
 */
const handleQuery = () => {
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
 * 展开/折叠所有节点
 */
const toggleExpandAll = () => {
  expandAll.value = !expandAll.value
}

/**
 * 打开表单
 * @param type 操作类型：create-新增，update-编辑
 * @param id 分类ID（编辑时传入）或父分类ID（新增子分类时传入）
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
    await message.delConfirm()
    await GoodsCategoryApi.deleteGoodsCategory(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/**
 * 初始化
 */
onMounted(() => {
  getList()
})
</script>
