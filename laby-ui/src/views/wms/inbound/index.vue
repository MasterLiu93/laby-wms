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
      <el-form-item :label="t('wms.inboundNo')" prop="inboundNo">
        <el-input
          v-model="queryParams.inboundNo"
          :placeholder="t('wms.inboundNoPlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.inboundType')" prop="inboundType">
        <el-select
          v-model="queryParams.inboundType"
          :placeholder="t('wms.inboundTypePlaceholder')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_INBOUND_TYPE)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('wms.warehouse')" prop="warehouseId">
        <el-select
          v-model="queryParams.warehouseId"
          :placeholder="t('common.selectText')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="warehouse in warehouseList"
            :key="warehouse.id"
            :label="warehouse.warehouseName"
            :value="warehouse.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-select
          v-model="queryParams.status"
          :placeholder="t('common.selectText')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_INBOUND_STATUS)"
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
          type="daterange"
          :start-placeholder="t('common.startTimeText')"
          :end-placeholder="t('common.endTimeText')"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> {{ t('common.query') }}</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> {{ t('common.reset') }}</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['wms:inbound:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> {{ t('action.create') }}
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
        v-if="columns.inboundNo.visible" 
        :label="t('wms.inboundNo')" 
        align="center" 
        prop="inboundNo" 
        min-width="150" 
      />
      <el-table-column 
        v-if="columns.inboundType.visible" 
        :label="t('common.type')" 
        align="center" 
        min-width="120"
        show-overflow-tooltip
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_INBOUND_TYPE" :value="scope.row.inboundType" />
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.warehouseName.visible" 
        :label="t('wms.warehouse')" 
        align="center" 
        prop="warehouseName" 
        min-width="120" 
        show-overflow-tooltip 
      />
      <el-table-column 
        v-if="columns.supplierName.visible" 
        :label="t('wms.supplier')" 
        align="center" 
        prop="supplierName" 
        min-width="160" 
        show-overflow-tooltip 
      >
        <template #default="scope">
          <span class="text-gray-600">{{ scope.row.supplierName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.totalQuantity.visible" 
        :label="t('wms.totalQuantity')" 
        align="center" 
        prop="totalQuantity" 
        min-width="120" 
        show-overflow-tooltip
      />
      <el-table-column 
        v-if="columns.receivedQuantity.visible" 
        :label="t('wms.receivedQuantity')" 
        align="center" 
        prop="receivedQuantity" 
        min-width="120" 
        show-overflow-tooltip
      />
      <el-table-column 
        v-if="columns.totalAmount.visible" 
        :label="t('wms.totalAmount')" 
        align="center" 
        prop="totalAmount" 
        min-width="120"
        show-overflow-tooltip
      >
        <template #default="scope">
          <span v-if="scope.row.totalAmount" class="text-red-500 font-semibold">
            {{ formatToFraction(scope.row.totalAmount) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.status.visible" 
        :label="t('common.status')" 
        align="center" 
        min-width="120"
        show-overflow-tooltip
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_INBOUND_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.createTime.visible" 
        :label="t('common.createTime')" 
        align="center" 
        prop="createTime" 
        :formatter="dateFormatter" 
        min-width="120" 
        show-overflow-tooltip
      />
      <el-table-column :label="t('action.action')" align="center" fixed="right" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)" v-hasPermi="['wms:inbound:query']">
            {{ t('action.detail') }}
          </el-button>
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['wms:inbound:update']" v-if="scope.row.status === 1">
            {{ t('action.edit') }}
          </el-button>
          <el-button link type="success" @click="handleAudit(scope.row)" v-hasPermi="['wms:inbound:audit']" v-if="scope.row.status === 1">
            {{ t('wms.audit') }}
          </el-button>
          <el-button link type="primary" @click="handleReceive(scope.row)" v-hasPermi="['wms:inbound:receive']" v-if="scope.row.status === 2 || scope.row.status === 3">
            {{ t('wms.receive') }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['wms:inbound:delete']" v-if="scope.row.status === 1">
            {{ t('action.delete') }}
          </el-button>
          <el-button link type="warning" @click="handleCancel(scope.row.id)" v-hasPermi="['wms:inbound:cancel']" v-if="scope.row.status !== 4 && scope.row.status !== 5">
            {{ t('action.cancel') }}
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

  <!-- 表单弹窗：添加/修改 -->
  <InboundForm ref="formRef" @success="getList" />

  <!-- 详情弹窗 -->
  <InboundDetail ref="detailRef" />

  <!-- 收货弹窗 -->
  <InboundReceive ref="receiveRef" @success="getList" />
</template>

<script setup lang="ts" name="WmsInbound">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'
import { formatToFraction } from '@/utils'

const { getDictLabel } = useDictI18n() // 字典国际化
import { getInboundPage, deleteInbound, auditInbound, cancelInbound } from '@/api/wms/inbound'
import { getWarehouseSimpleList } from '@/api/wms/warehouse'
import InboundForm from './InboundForm.vue'
import InboundDetail from './InboundDetail.vue'
import InboundReceive from './InboundReceive.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'
import { createWMSColumns } from '@/utils/wms-columns-config'

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  inboundNo: undefined,
  inboundType: undefined,
  warehouseId: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单

// 列设置功能 - RuoYi风格
const columns = reactive(createWMSColumns(t).inbound)

// 显示搜索状态
const showSearch = ref(true)
const warehouseList = ref([]) // 仓库列表

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getInboundPage(queryParams)
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

/** 详情操作 */
const detailRef = ref()
const openDetail = (row: any) => {
  detailRef.value.open(row.id)
}

/** 收货操作 */
const receiveRef = ref()
const handleReceive = (row: any) => {
  receiveRef.value.open(row.id)
}

/** 审核操作 */
const handleAudit = async (row: any) => {
  try {
    await message.confirm(t('wms.auditConfirm'))
    // TODO: 获取当前用户信息
    await auditInbound(row.id, 1, 'admin')
    message.success(t('wms.auditSuccess'))
    await getList()
  } catch {}
}

/** 取消操作 */
const handleCancel = async (id: number) => {
  try {
    await message.confirm(t('wms.cancelConfirm'))
    await cancelInbound(id)
    message.success(t('wms.cancelSuccess'))
    await getList()
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await deleteInbound(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 初始化仓库列表 */
const initWarehouseList = async () => {
  const data = await getWarehouseSimpleList()
  warehouseList.value = data
}

/** 初始化 **/
onMounted(async () => {
  await getList()
  await initWarehouseList()
})
</script>
