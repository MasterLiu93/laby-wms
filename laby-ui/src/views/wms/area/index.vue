<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
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
      <el-form-item :label="t('wms.areaCode')" prop="areaCode">
        <el-input
          v-model="queryParams.areaCode"
          :placeholder="t('wms.areaCodePlaceholder')"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item :label="t('wms.areaName')" prop="areaName">
        <el-input
          v-model="queryParams.areaName"
          :placeholder="t('wms.areaNamePlaceholder')"
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
            :label="dict.label"
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
          v-hasPermi="['wms:area:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> {{ t('action.add') }}
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['wms:area:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') }}
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column :label="t('wms.areaName')" align="center" prop="areaName" />
      <el-table-column :label="t('wms.warehouse')" align="center" prop="warehouseName" />
      <el-table-column :label="t('wms.areaType')" align="center" prop="areaType" width="120px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.WMS_WAREHOUSE_AREA_TYPE" :value="scope.row.areaType" />
        </template>
      </el-table-column>
      <el-table-column :label="t('wms.floor')" align="center" prop="floor" width="80px" />
      <el-table-column :label="t('wms.areaSize')" align="center" prop="areaSize" width="120px" />
      <el-table-column :label="t('common.status')" align="center" prop="status" width="100px">
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
      <el-table-column :label="t('table.action')" align="center" width="200px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['wms:area:update']"
          >
            {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['wms:area:delete']"
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
  <WarehouseAreaForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as WarehouseAreaApi from '@/api/wms/area'
import * as WarehouseApi from '@/api/wms/warehouse'
import WarehouseAreaForm from './WarehouseAreaForm.vue'

/** 库区管理 列表 */
defineOptions({ name: 'WmsWarehouseArea' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  warehouseId: undefined,
  areaCode: undefined,
  areaName: undefined,
  areaType: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const warehouseList = ref([]) // 仓库列表

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await WarehouseAreaApi.getWarehouseAreaPage(queryParams)
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
    await WarehouseAreaApi.deleteWarehouseArea(id)
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
    const data = await WarehouseAreaApi.exportWarehouseArea(queryParams)
    download.excel(data, `${t('wms.area')}.xls`)
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(async () => {
  await getList()
  // 加载仓库列表
  warehouseList.value = await WarehouseApi.getWarehouseSimpleList()
})
</script>

