<!--
  商品分类表单组件
  
  功能说明：
  1. 支持新增和编辑商品分类
  2. 使用树形选择器选择父分类
  3. 分类编码由后端自动生成
  4. 层级由系统根据父分类自动计算
  5. 支持三级分类结构
  
  使用方法：
  <CategoryForm ref="formRef" @success="getList" />
  formRef.value.open('create') // 新增
  formRef.value.open('update', id) // 编辑
  
  @author laby
  @date 2025-10-30
-->
<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <!-- 父分类（树形选择器） -->
      <el-form-item :label="t('wms.parentCategory')" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="categoryTree"
          :props="treeProps"
          check-strictly
          default-expand-all
          :placeholder="t('wms.parentCategoryPlaceholder')"
          class="!w-full"
        />
      </el-form-item>
      
      <!-- 分类名称 -->
      <el-form-item :label="t('wms.categoryName')" prop="categoryName">
        <el-input 
          v-model="formData.categoryName" 
          :placeholder="t('wms.categoryNamePlaceholder')"
          maxlength="100"
        />
      </el-form-item>
      
      <!-- 排序 -->
      <el-form-item :label="t('wms.categorySort')" prop="sort">
        <el-input-number 
          v-model="formData.sort" 
          :min="0" 
          controls-position="right"
          :placeholder="t('wms.sortPlaceholder')"
          class="!w-full" 
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">{{ t('common.ok') }}</el-button>
      <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { handleTree } from '@/utils/tree'
import * as GoodsCategoryApi from '@/api/wms/category'

/** 商品分类表单 */
defineOptions({ name: 'CategoryForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const categoryTree = ref([]) // 分类树（用于父分类选择）

// 树形选择器配置（指定显示字段为 categoryName）
const treeProps = {
  children: 'children',
  label: 'categoryName',
  value: 'id'
}

// 表单数据
const formData = ref({
  id: undefined, // 分类ID（编辑时必传）
  parentId: 0, // 父分类ID，默认0表示顶级分类
  categoryName: undefined, // 分类名称
  sort: 0 // 排序，默认0
})

// 表单验证规则
const formRules = reactive({
  categoryName: [{ required: true, message: t('wms.categoryNamePlaceholder'), trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/**
 * 打开弹窗
 * @param type 操作类型：create-新增，update-修改
 * @param id 分类ID（修改时必传）
 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  
  // 加载分类树（用于父分类选择）
  await loadCategoryTree()
  
  // 修改时，加载数据
  if (id) {
    formLoading.value = true
    try {
      const data = await GoodsCategoryApi.getGoodsCategory(id)
      formData.value = {
        id: data.id,
        parentId: data.parentId || 0,
        categoryName: data.categoryName,
        sort: data.sort || 0
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/**
 * 加载分类树
 * 用于父分类选择器
 */
const loadCategoryTree = async () => {
  try {
    const list = await GoodsCategoryApi.getGoodsCategoryList()
    // 构建树形结构
    categoryTree.value = handleTree(list, 'id', 'parentId')
    // 添加顶级分类选项
    categoryTree.value.unshift({
      id: 0,
      categoryName: t('wms.levelOne'),
      parentId: null,
      children: []
    })
  } catch (error) {
    console.error('加载分类树失败', error)
  }
}

/**
 * 提交表单
 * 根据formType执行新增或更新操作
 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 1. 校验表单
  await formRef.value.validate()
  
  // 2. 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as GoodsCategoryApi.GoodsCategoryVO
    if (formType.value === 'create') {
      // 新增
      await GoodsCategoryApi.createGoodsCategory(data)
      message.success(t('common.createSuccess'))
    } else {
      // 更新
      await GoodsCategoryApi.updateGoodsCategory(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件，通知父组件刷新列表
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/**
 * 重置表单
 * 将表单数据恢复到初始状态
 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    parentId: 0,
    categoryName: undefined,
    sort: 0
  }
  formRef.value?.resetFields()
}
</script>
