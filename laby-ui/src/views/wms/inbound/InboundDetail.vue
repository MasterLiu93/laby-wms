<template>
  <Dialog :title="t('wms.inboundDetail')" v-model="dialogVisible" width="1200px">
    <el-descriptions :column="2" border v-loading="loading">
      <el-descriptions-item :label="t('wms.inboundNo')">{{ detailData.inboundNo }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.inboundType')">
        <dict-tag :type="DICT_TYPE.WMS_INBOUND_TYPE" :value="detailData.inboundType" />
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.warehouse')">{{ detailData.warehouseName }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.supplierName')">{{ detailData.supplierName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('common.status')">
        <dict-tag :type="DICT_TYPE.WMS_INBOUND_STATUS" :value="detailData.status" />
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.expectedArrivalTime')">
        {{ formatDate(detailData.expectedArrivalTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.actualArrivalTime')">
        {{ formatDate(detailData.actualArrivalTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.totalQuantity')">{{ detailData.totalQuantity }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.receivedQuantity')">{{ detailData.receivedQuantity }}</el-descriptions-item>
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
      <h3>{{ t('wms.inboundDetailTitle') }}</h3>
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
        <el-table-column :label="t('wms.receivedQty')" prop="receivedQuantity" width="100px" />
        <el-table-column :label="t('wms.qualifiedQty')" prop="qualifiedQuantity" width="100px" />
        <el-table-column :label="t('wms.unqualifiedQty')" prop="unqualifiedQuantity" width="110px" />
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
import { getInbound } from '@/api/wms/inbound'

const { t } = useI18n()

const dialogVisible = ref(false)
const loading = ref(false)
const detailData = ref({
  inboundNo: '',
  inboundType: undefined,
  warehouseName: '',
  supplierName: '',
  status: undefined,
  expectedArrivalTime: undefined,
  actualArrivalTime: undefined,
  totalQuantity: 0,
  receivedQuantity: 0,
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
    const data = await getInbound(id)
    detailData.value = data
  } finally {
    loading.value = false
  }
}
defineExpose({ open })
</script>
