<template>
  <Dialog :title="t('wms.outboundDetail')" v-model="dialogVisible" width="1200px">
    <el-descriptions :column="2" border v-loading="loading">
      <el-descriptions-item :label="t('wms.outboundNo')">{{ detailData.outboundNo }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.outboundType')">
        <dict-tag :type="DICT_TYPE.WMS_OUTBOUND_TYPE" :value="detailData.outboundType" />
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.warehouse')">{{ detailData.warehouseName }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.customerName')">{{ detailData.customerName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('common.status')">
        <dict-tag :type="DICT_TYPE.WMS_OUTBOUND_STATUS" :value="detailData.status" />
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.expectedShipmentTime')">
        {{ formatDate(detailData.expectedShipmentTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.actualShipmentTime')">
        {{ formatDate(detailData.actualShipmentTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.totalQuantity')">{{ detailData.totalQuantity }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.pickedQuantity')">{{ detailData.pickedQuantity }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.totalAmount')">
        {{ detailData.totalAmount ? '¥' + detailData.totalAmount.toFixed(2) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('common.auditor')">{{ detailData.auditByName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('common.auditTime')">
        {{ formatDate(detailData.auditTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('common.completer')">{{ detailData.completeByName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('common.completeTime')">
        {{ formatDate(detailData.completeTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('common.createTime')">
        {{ formatDate(detailData.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('form.remark')" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
    </el-descriptions>

    <!-- 明细列表 -->
    <div class="mt-20px">
      <h3>{{ t('wms.outboundDetailTitle') }}</h3>
      <el-table :data="detailData.items" border class="mt-10px">
        <el-table-column :label="t('wms.goodsName')" prop="goodsName" min-width="150px" />
        <el-table-column :label="t('wms.skuCode')" prop="skuCode" width="150px" />
        <el-table-column :label="t('wms.unit')" prop="goodsUnit" width="80px">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.WMS_GOODS_UNIT" :value="scope.row.goodsUnit" />
          </template>
        </el-table-column>
        <el-table-column :label="t('wms.batchNo')" prop="batchNo" width="150px">
          <template #default="scope">{{ scope.row.batchNo || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('wms.area')" prop="areaName" width="120px">
          <template #default="scope">{{ scope.row.areaName || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('wms.location')" prop="locationCode" width="150px">
          <template #default="scope">
            <div v-if="scope.row.locationId">
              <!-- 主显示：位置信息（加粗） -->
              <div class="font-semibold">
                {{ t('wms.locationFormat').replace('{row}', scope.row.rowNo).replace('{column}', scope.row.columnNo).replace('{layer}', scope.row.layerNo) }}
              </div>
              <!-- 副显示：编码（灰色小字） -->
              <div class="text-gray-400 text-xs">
                {{ scope.row.locationCode }}
              </div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('wms.planQuantity')" prop="planQuantity" width="100px" />
        <el-table-column :label="t('wms.pickedQty')" prop="pickedQuantity" width="100px" />
        <el-table-column :label="t('wms.shippedQty')" prop="shippedQuantity" width="100px" />
        <el-table-column :label="t('wms.price')" prop="price" width="100px">
          <template #default="scope">
            {{ scope.row.price ? '¥' + scope.row.price.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="t('wms.amount')" prop="amount" width="120px">
          <template #default="scope">
            {{ scope.row.amount ? '¥' + scope.row.amount.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="t('form.remark')" prop="remark" min-width="150px">
          <template #default="scope">{{ scope.row.remark || '-' }}</template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('common.close') }}</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'
import { getOutbound } from '@/api/wms/outbound'

const { t } = useI18n()

const dialogVisible = ref(false)
const loading = ref(false)
const detailData = ref({
  outboundNo: '',
  outboundType: undefined,
  warehouseName: '',
  customerName: '',
  status: undefined,
  expectedShipmentTime: undefined,
  actualShipmentTime: undefined,
  totalQuantity: 0,
  pickedQuantity: 0,
  totalAmount: undefined,
  auditByName: '',
  auditTime: undefined,
  completeByName: '',
  completeTime: undefined,
  createTime: undefined,
  remark: '',
  items: []
})

/** 打开弹窗 */
const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    const data = await getOutbound(id)
    detailData.value = data
  } finally {
    loading.value = false
  }
}
defineExpose({ open })
</script>
