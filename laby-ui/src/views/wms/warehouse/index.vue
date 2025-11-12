<template>
  <!-- 搜索工作栏 -->
  <ContentWrap v-if="showSearch">
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item :label="t('wms.warehouseCode')" prop="warehouseCode">
        <el-input
          v-model="queryParams.warehouseCode"
          :placeholder="t('wms.warehouseCodePlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.warehouseName')" prop="warehouseName">
        <el-input
          v-model="queryParams.warehouseName"
          :placeholder="t('wms.warehouseNamePlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.warehouseType')" prop="warehouseType">
        <el-select
          v-model="queryParams.warehouseType"
          :placeholder="t('wms.warehouseTypePlaceholder')"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.WMS_WAREHOUSE_TYPE)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
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
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="getDictLabel(dict)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> {{ t('common.query') }}</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> {{ t('common.reset') }}</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['wms:warehouse:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> {{ t('action.add') }}
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['wms:warehouse:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
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
    
    <el-table v-loading="loading" :data="list" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="80" />
      
      <el-table-column 
        v-if="columns.warehouseName.visible" 
        :label="t('wms.warehouseName')" 
        align="center" 
        prop="warehouseName" 
      />
      
      <el-table-column 
        v-if="columns.warehouseType.visible" 
        :label="t('wms.warehouseType')" 
        align="center" 
        prop="warehouseType"
      >
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_WAREHOUSE_TYPE" :value="scope.row.warehouseType" />
        </template>
      </el-table-column>
      
      <el-table-column 
        v-if="columns.address.visible" 
        :label="t('wms.address')" 
        align="center" 
        prop="address" 
        show-overflow-tooltip 
      />
      
      <el-table-column 
        v-if="columns.contactPerson.visible" 
        :label="t('wms.contactPerson')" 
        align="center" 
        prop="contactPerson" 
      />
      
      <el-table-column 
        v-if="columns.contactPhone.visible" 
        :label="t('wms.contactPhone')" 
        align="center" 
        prop="contactPhone" 
      />
      
      <el-table-column 
        v-if="columns.status.visible" 
        :label="t('common.status')" 
        align="center" 
        prop="status"
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
        width="180"
        :formatter="dateFormatter"
        show-overflow-tooltip
      />
      <el-table-column :label="t('table.action')" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['wms:warehouse:update']"
          >
            {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['wms:warehouse:delete']"
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

  <!-- 表单弹窗：添加/修改 -->
  <WarehouseForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { useDictI18n } from '@/hooks/web/useDictI18n'
import download from '@/utils/download'
import * as WarehouseApi from '@/api/wms/warehouse'
import WarehouseForm from './WarehouseForm.vue'
import RightToolbar from '@/components/RightToolbar/index.vue'

defineOptions({ name: 'WmsWarehouse' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const { getDictLabel } = useDictI18n() // 字典国际化

const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  warehouseCode: undefined,
  warehouseName: undefined,
  warehouseType: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const selectedIds = ref<number[]>([]) // 选中的ID数组

// 列设置功能 - RuoYi风格
const columns = reactive({
  warehouseName: { visible: true, label: t('wms.warehouseName') },
  warehouseType: { visible: true, label: t('wms.warehouseType') },
  address: { visible: true, label: t('wms.address') },
  contactPerson: { visible: true, label: t('wms.contactPerson') },
  contactPhone: { visible: true, label: t('wms.contactPhone') },
  status: { visible: true, label: t('common.status') },
  createTime: { visible: true, label: t('common.createTime') }
})

// 显示搜索状态
const showSearch = ref(true)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await WarehouseApi.getWarehousePage(queryParams)
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

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await WarehouseApi.deleteWarehouse(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await WarehouseApi.exportWarehouse(queryParams)
    download.excel(data, `${t('wms.warehouse')}.xls`)
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 批量选择操作 */
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map((item) => item.id)
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

