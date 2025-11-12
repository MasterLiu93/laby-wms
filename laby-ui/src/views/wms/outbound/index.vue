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
      <el-form-item :label="t('wms.outboundNo')" prop="outboundNo">
        <el-input
          v-model="queryParams.outboundNo"
          :placeholder="t('wms.outboundNoPlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.outboundType')" prop="outboundType">
        <el-select
          v-model="queryParams.outboundType"
          :placeholder="t('wms.outboundTypePlaceholder')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_OUTBOUND_TYPE)"
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
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_OUTBOUND_STATUS)"
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
          v-hasPermi="['wms:outbound:create']"
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
        v-if="columns.outboundNo.visible" 
        :label="t('wms.outboundNo')" 
        align="center" 
        prop="outboundNo" 
        min-width="150" 
      />
      <el-table-column 
        v-if="columns.outboundType.visible" 
        :label="t('common.type')" 
        align="center" 
        min-width="120" show-overflow-tooltip
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_OUTBOUND_TYPE" :value="scope.row.outboundType" />
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
        v-if="columns.customerName.visible" 
        :label="t('wms.customer')" 
        align="center" 
        prop="customerName" 
        min-width="160" 
        show-overflow-tooltip 
      >
        <template #default="scope">
          <span class="text-gray-600">{{ scope.row.customerName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.totalQuantity.visible" 
        :label="t('wms.totalQuantity')" 
        align="center" 
        prop="totalQuantity" 
        min-width="120" show-overflow-tooltip 
      />
      <el-table-column 
        v-if="columns.pickedQuantity.visible" 
        :label="t('wms.pickedQuantity')" 
        align="center" 
        prop="pickedQuantity" 
        min-width="120" show-overflow-tooltip 
      />
      <el-table-column 
        v-if="columns.totalAmount.visible" 
        :label="t('wms.totalAmount')" 
        align="center" 
        prop="totalAmount" 
        min-width="120" show-overflow-tooltip
      >
        <template #default="scope">
          <span v-if="scope.row.totalAmount" class="text-red-500 font-semibold">
            ¥{{ scope.row.totalAmount.toFixed(2) }}
          </span>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.status.visible" 
        :label="t('common.status')" 
        align="center" 
        min-width="120" show-overflow-tooltip
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_OUTBOUND_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column 
        v-if="columns.createTime.visible" 
        :label="t('common.createTime')" 
        align="center" 
        prop="createTime" 
        :formatter="dateFormatter" 
        min-width="120" show-overflow-tooltip 
      />
      <el-table-column :label="t('action.action')" align="center" fixed="right" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)" v-hasPermi="['wms:outbound:query']">
            {{ t('action.detail') }}
          </el-button>
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['wms:outbound:update']" v-if="scope.row.status === 1">
            {{ t('action.edit') }}
          </el-button>
          <el-button link type="success" @click="handleAudit(scope.row)" v-hasPermi="['wms:outbound:audit']" v-if="scope.row.status === 1">
            {{ t('wms.audit') }}
          </el-button>
          <el-button link type="primary" @click="handlePick(scope.row)" v-hasPermi="['wms:outbound:pick']" v-if="scope.row.status === 2 || scope.row.status === 3">
            {{ t('wms.pick') }}
          </el-button>
          <el-button link type="success" @click="handleShip(scope.row)" v-hasPermi="['wms:outbound:ship']" v-if="scope.row.status === 4">
            {{ t('wms.ship') }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['wms:outbound:delete']" v-if="scope.row.status === 1">
            {{ t('action.delete') }}
          </el-button>
          <el-button link type="warning" @click="handleCancel(scope.row.id)" v-hasPermi="['wms:outbound:cancel']" v-if="scope.row.status !== 5 && scope.row.status !== 6">
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
  <OutboundForm ref="formRef" @success="getList" />

  <!-- 详情弹窗 -->
  <OutboundDetail ref="detailRef" />

  <!-- 拣货弹窗 -->
  <OutboundPick ref="pickRef" @success="getList" />
</template>

<script setup lang="ts" name="WmsOutbound">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'

const { getDictLabel } = useDictI18n() // 字典国际化
import { getOutboundPage, deleteOutbound, auditOutbound, shipOutbound, cancelOutbound } from '@/api/wms/outbound'
import { getWarehouseSimpleList } from '@/api/wms/warehouse'
import OutboundForm from './OutboundForm.vue'
import OutboundDetail from './OutboundDetail.vue'
import OutboundPick from './OutboundPick.vue'
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
  outboundNo: undefined,
  outboundType: undefined,
  warehouseId: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单

// 列设置功能 - RuoYi风格
const columns = reactive(createWMSColumns(t).outbound)

// 显示搜索状态
const showSearch = ref(true)
const warehouseList = ref([]) // 仓库列表

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getOutboundPage(queryParams)
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

/** 拣货操作 */
const pickRef = ref()
const handlePick = (row: any) => {
  pickRef.value.open(row.id)
}

/** 审核操作 */
const handleAudit = async (row: any) => {
  try {
    await message.confirm(t('wms.auditConfirm'))
    // TODO: 获取当前用户信息
    await auditOutbound(row.id, 1, 'admin')
    message.success(t('wms.auditSuccess'))
    await getList()
  } catch {}
}

/** 发货操作 */
const handleShip = async (row: any) => {
  try {
    await message.confirm(t('wms.shipConfirm'))
    // TODO: 获取当前用户信息
    await shipOutbound(row.id, 1, 'admin')
    message.success(t('wms.shipSuccess'))
    await getList()
  } catch {}
}

/** 取消操作 */
const handleCancel = async (id: number) => {
  try {
    await message.confirm(t('wms.cancelConfirm'))
    await cancelOutbound(id)
    message.success(t('wms.cancelSuccess'))
    await getList()
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await deleteOutbound(id)
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
