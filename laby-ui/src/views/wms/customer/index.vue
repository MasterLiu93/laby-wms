<!--
  客户管理列表页
  
  功能说明：
  1. 客户的查询、新增、编辑、删除
  2. 支持多条件搜索（客户编码、名称、类型、等级、联系人、电话、状态）
  3. 支持分页显示
  4. 显示累计订单数和累计金额
  5. 权限控制：wms:customer:query、create、update、delete
  
  @author laby
  @date 2025-10-28
-->
<template>
  <ContentWrap v-show="showSearch">
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item :label="t('wms.customerCode')" prop="customerCode">
        <el-input
          v-model="queryParams.customerCode"
          :placeholder="t('wms.customerCodePlaceholder')"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.customerName')" prop="customerName">
        <el-input
          v-model="queryParams.customerName"
          :placeholder="t('wms.customerNamePlaceholder')"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.customerType')" prop="customerType">
        <el-select
          v-model="queryParams.customerType"
          :placeholder="t('wms.customerTypePlaceholder')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_CUSTOMER_TYPE)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.customerLevel')" prop="customerLevel">
        <el-select
          v-model="queryParams.customerLevel"
          :placeholder="t('wms.customerLevelPlaceholder')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_CUSTOMER_LEVEL)"
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
        <el-button type="primary" @click="openForm('create')" v-hasPermi="['wms:customer:create']">
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
        v-if="columns.customerName.visible" 
        :label="t('wms.customerName')" 
        prop="customerName" 
        min-width="220" 
        show-overflow-tooltip 
      />
      <el-table-column 
        v-if="columns.customerType.visible" 
        :label="t('wms.customerType')" 
        prop="customerType" 
        width="150"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_CUSTOMER_TYPE" :value="scope.row.customerType" />
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.customerLevel.visible" 
        :label="t('wms.customerLevel')" 
        prop="customerLevel" 
        width="150"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_CUSTOMER_LEVEL" :value="scope.row.customerLevel" />
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.contactPerson.visible" 
        :label="t('wms.contactPerson')" 
        prop="contactPerson" 
        width="100" 
        show-overflow-tooltip 
      />
      <el-table-column 
        v-if="columns.contactPhone.visible" 
        :label="t('wms.contactPhone')" 
        prop="contactPhone" 
        width="130" 
        show-overflow-tooltip 
      />
      <el-table-column 
        v-if="columns.deliveryAddress.visible" 
        :label="t('wms.deliveryAddress')" 
        min-width="200" 
        show-overflow-tooltip
      >
        <template #default="scope">
          {{ [scope.row.deliveryProvince, scope.row.deliveryCity, scope.row.deliveryDistrict, scope.row.deliveryAddress].filter(Boolean).join(' ') || '-' }}
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.creditLimit.visible" 
        :label="t('wms.creditLimit')" 
        prop="creditLimit" 
        width="110" 
        align="right"
      >
        <template #default="scope">
          <span style="color: #409eff; font-weight: 500">{{ formatToFraction(scope.row.creditLimit) }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.totalOrders.visible" 
        :label="t('wms.totalOrders')" 
        prop="totalOrders" 
        width="90" 
        align="right" 
      />
      <el-table-column 
        v-if="columns.totalAmount.visible" 
        :label="t('wms.totalAmount')" 
        prop="totalAmount" 
        width="180" 
        align="right"
      >
        <template #default="scope">
          <span style="color: #67c23a; font-weight: 500">{{ formatToFraction(scope.row.totalAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.status.visible" 
        :label="t('common.status')" 
        prop="status" 
        width="80"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.createTime.visible" 
        :label="t('common.createTime')" 
        prop="createTime" 
        width="160" 
        :formatter="dateFormatter" 
      />
      <el-table-column :label="t('action.action')" fixed="right" width="150">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['wms:customer:update']">
            {{ t('action.edit') }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['wms:customer:delete']">
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
  <CustomerForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'

const { getDictLabel } = useDictI18n() // 字典国际化
import { dateFormatter } from '@/utils/formatTime'
import { formatToFraction } from '@/utils'
import * as CustomerApi from '@/api/wms/customer'
import CustomerForm from './CustomerForm.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

/**
 * 客户管理列表页组件定义
 */
defineOptions({ name: 'WmsCustomer' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true) // 列表加载中
const list = ref([]) // 列表数据
const total = ref(0) // 总条数

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  customerCode: undefined,
  customerName: undefined,
  customerType: undefined,
  customerLevel: undefined,
  contactPerson: undefined,
  contactPhone: undefined,
  status: undefined,
  createTime: undefined
})
const queryFormRef = ref() // 搜索表单

// 列设置功能 - RuoYi风格
const columns = reactive(createWMSColumns(t).customer)

// 显示搜索状态
const showSearch = ref(true)

/**
 * 查询列表
 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CustomerApi.getCustomerPage(queryParams)
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
    await CustomerApi.deleteCustomer(id)
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
