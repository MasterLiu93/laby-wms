<!--
  供应商管理列表页
  
  功能说明：
  1. 供应商的查询、新增、编辑、删除
  2. 支持多条件搜索（供应商编码、名称、类型、信用等级、联系人、电话、状态）
  3. 支持分页显示
  4. 权限控制：wms:supplier:query、create、update、delete
  
  @author laby
  @date 2025-10-28
-->
<template>
  <ContentWrap v-if="showSearch">
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item :label="t('wms.supplierCode')" prop="supplierCode">
        <el-input
          v-model="queryParams.supplierCode"
          :placeholder="t('wms.supplierCodePlaceholder')"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.supplierName')" prop="supplierName">
        <el-input
          v-model="queryParams.supplierName"
          :placeholder="t('wms.supplierNamePlaceholder')"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.supplierType')" prop="supplierType">
        <el-select
          v-model="queryParams.supplierType"
          :placeholder="t('wms.supplierTypePlaceholder')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_SUPPLIER_TYPE)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.creditLevel')" prop="creditLevel">
        <el-select
          v-model="queryParams.creditLevel"
          :placeholder="t('wms.creditLevelPlaceholder')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_SUPPLIER_CREDIT_LEVEL)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.contactPerson')" prop="contactPerson">
        <el-input
          v-model="queryParams.contactPerson"
          :placeholder="t('wms.contactPersonPlaceholder')"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.contactPhone')" prop="contactPhone">
        <el-input
          v-model="queryParams.contactPhone"
          :placeholder="t('wms.contactPhonePlaceholder')"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.status')" prop="status">
        <el-select v-model="queryParams.status" :placeholder="t('common.selectText')" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.createTime')" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetimerange"
          :start-placeholder="t('common.startTimeText')"
          :end-placeholder="t('common.endTimeText')"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          {{ t('common.query') }}
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          {{ t('common.reset') }}
        </el-button>
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['wms:supplier:create']">
          <Icon icon="ep:plus" class="mr-5px" />
          {{ t('action.add') }}
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
        v-if="columns.supplierCode.visible" 
        :label="t('wms.supplierCode')" 
        align="center" 
        prop="supplierCode" 
        width="150px" 
      />
      <el-table-column 
        v-if="columns.supplierName.visible" 
        :label="t('wms.supplierName')" 
        align="center" 
        prop="supplierName" 
        min-width="200px" 
      />
      <el-table-column 
        v-if="columns.supplierType.visible" 
        :label="t('wms.supplierType')" 
        align="center" 
        width="120px"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_SUPPLIER_TYPE" :value="scope.row.supplierType" />
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.contactPerson.visible" 
        :label="t('wms.contactPerson')" 
        align="center" 
        width="100px"
      />
      <el-table-column 
        v-if="columns.contactPhone.visible" 
        :label="t('wms.contactPhone')" 
        prop="contactPhone" 
        width="120" 
      />
      <el-table-column 
        v-if="columns.provinceCityDistrict.visible" 
        :label="t('wms.provinceCityDistrict')" 
        min-width="150" 
        show-overflow-tooltip
      >
        <template #default="scope">
          {{ [scope.row.province, scope.row.city, scope.row.district].filter(Boolean).join(' ') || '-' }}
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.address.visible" 
        :label="t('wms.address')" 
        prop="address" 
        min-width="180" 
        show-overflow-tooltip 
      />
      <el-table-column 
        v-if="columns.creditLevel.visible" 
        :label="t('wms.creditLevel')" 
        align="center" 
        width="100px"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_SUPPLIER_CREDIT_LEVEL" :value="scope.row.creditLevel" />
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.cooperationStartDate.visible" 
        :label="t('wms.cooperationStartDate')" 
        prop="cooperationStartDate" 
        width="120px"
      >
        <template #default="scope">
          {{ formatCooperationDate(scope.row.cooperationStartDate) }}
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.status.visible" 
        :label="t('common.status')" 
        align="center" 
        prop="status" 
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
        :formatter="dateFormatter" 
      />
      <el-table-column :label="t('action.action')" fixed="right" width="150">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['wms:supplier:update']">
            {{ t('action.edit') }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['wms:supplier:delete']">
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
  <SupplierForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'

const { getDictLabel } = useDictI18n() // 字典国际化
import { formatDate } from '@/utils/formatTime'
import * as SupplierApi from '@/api/wms/supplier'
import SupplierForm from './SupplierForm.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

/**
 * 供应商管理列表页组件定义
 */
defineOptions({ name: 'WmsSupplier' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true) // 列表加载中
const list = ref([]) // 列表数据
const total = ref(0) // 总条数

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  supplierCode: undefined,
  supplierName: undefined,
  supplierType: undefined,
  creditLevel: undefined,
  contactPerson: undefined,
  contactPhone: undefined,
  status: undefined,
  createTime: undefined
})
const queryFormRef = ref()

// 列设置功能 - RuoYi风格
const columns = reactive(createWMSColumns(t).supplier)

// 显示搜索状态
const showSearch = ref(true)

/**
 * 查询列表
 */
const getList = async () => {
  loading.value = true
  try {
    const data = await SupplierApi.getSupplierPage(queryParams)
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
 * 打开表单
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
    await SupplierApi.deleteSupplier(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/**
 * 日期格式化（创建时间）
 */
const dateFormatter = (row, column, cellValue) => {
  if (!cellValue) return '-'
  return formatDate(cellValue)
}

/**
 * 合作日期格式化（处理 LocalDate 数组格式）
 */
const formatCooperationDate = (date) => {
  if (!date) return '-'
  // 如果是数组格式 [2021, 1, 15]
  if (Array.isArray(date) && date.length === 3) {
    const [year, month, day] = date
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  // 如果已经是字符串格式
  if (typeof date === 'string') {
    return date
  }
  return '-'
}

/**
 * 初始化
 */
onMounted(() => {
  getList()
})
</script>
