<template>
  <Dialog v-model="dialogVisible" :title="t('wms.waveDetail')" width="1400px">
    <el-descriptions :column="3" border v-loading="loading">
      <el-descriptions-item :label="t('wms.waveNo')">
        {{ detailData.waveNo }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.warehouse')">
        {{ detailData.warehouseName }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.waveType')">
        <dict-tag :type="DICT_TYPE.WMS_WAVE_TYPE" :value="detailData.waveType" />
      </el-descriptions-item>

      <el-descriptions-item :label="t('wms.orderCount')">
        {{ detailData.orderCount }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.itemCount')">
        {{ detailData.itemCount }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.totalQuantity')">
        {{ detailData.totalQuantity }}
      </el-descriptions-item>

      <el-descriptions-item :label="t('wms.priority')">
        <el-tag v-if="detailData.priority === 3" type="danger">{{ t('wms.urgent') }}</el-tag>
        <el-tag v-else-if="detailData.priority === 2" type="warning">{{ t('wms.important') }}</el-tag>
        <el-tag v-else type="info">{{ t('wms.normal') }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.picker')">
        {{ detailData.pickerName || t('wms.unassigned') }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.waveStatus')">
        <dict-tag :type="DICT_TYPE.WMS_WAVE_STATUS" :value="detailData.status" />
      </el-descriptions-item>

      <el-descriptions-item :label="t('wms.estimatedTime')">
        {{ detailData.estimatedTime ? `${detailData.estimatedTime} ${t('wms.minutes')}` : '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.actualTime')">
        {{ detailData.actualTime ? `${detailData.actualTime} ${t('wms.minutes')}` : '-' }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('wms.startTime')">
        {{ formatDate(detailData.startTime) }}
      </el-descriptions-item>

      <el-descriptions-item :label="t('wms.endTime')">
        {{ formatDate(detailData.endTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('common.createTime')">
        {{ formatDate(detailData.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('form.remark')">
        {{ detailData.remark || '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <el-divider content-position="left">
      <span style="font-weight: bold">{{ t('wms.outboundList') }}</span>
    </el-divider>

    <el-table :data="outboundList" border max-height="400px" v-loading="loading">
      <el-table-column :label="t('wms.outboundNo')" prop="outboundNo" min-width="180" show-overflow-tooltip />
      <el-table-column :label="t('wms.outboundType')" prop="outboundType" min-width="100" align="center">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.WMS_OUTBOUND_TYPE" :value="row.outboundType" />
        </template>
      </el-table-column>
      <el-table-column :label="t('wms.customerName')" prop="customerName" min-width="120" show-overflow-tooltip />
      <el-table-column :label="t('wms.totalQuantity')" prop="totalQuantity" min-width="90" align="center" />
      <el-table-column :label="t('wms.pickedQuantity')" prop="pickedQuantity" min-width="110" align="center">
        <template #default="{ row }">
          <span :style="{ color: row.pickedQuantity >= row.totalQuantity ? '#67c23a' : '#409eff' }">
            {{ row.pickedQuantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('wms.totalAmount')" prop="totalAmount" min-width="120" align="right">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: bold">
            {{ row.totalAmount ? `¥${row.totalAmount.toFixed(2)}` : '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('wms.outboundStatus')" prop="status" min-width="100" align="center">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.WMS_OUTBOUND_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :label="t('wms.expectedShipmentTime')"
        prop="expectedShipmentTime"
        min-width="160"
        :formatter="dateFormatter"
      />
    </el-table>

    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('common.close') }}</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as PickingWaveApi from '@/api/wms/pickingWave'
import * as OutboundApi from '@/api/wms/outbound'

const { t } = useI18n()

/** 对话框 */
const dialogVisible = ref(false)
const loading = ref(false)

/** 详情数据 */
const detailData = ref({
  id: undefined,
  waveNo: '',
  warehouseName: '',
  waveType: '',
  orderCount: 0,
  itemCount: 0,
  totalQuantity: 0,
  priority: 1,
  pickerName: '',
  estimatedTime: 0,
  actualTime: 0,
  startTime: undefined,
  endTime: undefined,
  status: 1,
  remark: '',
  createTime: undefined,
  outboundIds: []
})

/** 出库单列表 */
const outboundList = ref([])

/** 格式化日期 */
const formatDate = (date: any) => {
  if (!date) return '-'
  return dateFormatter(null, null, date)
}

/** 打开弹窗 */
const open = async (id: number) => {
  dialogVisible.value = true
  loading.value = true
  try {
    // 加载波次详情
    detailData.value = await PickingWaveApi.getPickingWave(id)

    // 加载出库单详情
    if (detailData.value.outboundIds && detailData.value.outboundIds.length > 0) {
      outboundList.value = []
      for (const outboundId of detailData.value.outboundIds) {
        const outbound = await OutboundApi.getOutbound(outboundId)
        outboundList.value.push(outbound)
      }
    }
  } finally {
    loading.value = false
  }
}

/** 暴露方法 */
defineExpose({ open })
</script>
