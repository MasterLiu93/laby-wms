<template>
  <Dialog :title="t('wms.outboundPick')" v-model="dialogVisible" width="1000px">
    <el-descriptions :column="2" border class="mb-20px">
      <el-descriptions-item :label="t('wms.outboundNo')">{{ outboundData.outboundNo }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.warehouse')">{{ outboundData.warehouseName }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.customerName')">{{ outboundData.customerName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('common.status')">
        <dict-tag :type="DICT_TYPE.WMS_OUTBOUND_STATUS" :value="outboundData.status" />
      </el-descriptions-item>
    </el-descriptions>

    <h3>{{ t('wms.outboundDetailTitle') }}</h3>
    <el-table :data="outboundData.items" border class="mt-10px">
      <el-table-column :label="t('wms.goodsName')" prop="goodsName" width="150px" />
      <el-table-column :label="t('wms.skuCode')" prop="skuCode" width="150px" />
      <el-table-column :label="t('wms.batchNo')" prop="batchNo" width="120px">
        <template #default="scope">{{ scope.row.batchNo || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('wms.planQuantity')" prop="planQuantity" width="100px" />
      <el-table-column :label="t('wms.pickedQuantity')" prop="pickedQuantity" width="100px" />
      <el-table-column :label="t('wms.currentPicked')" width="150px">
        <template #default="{ row }">
          <el-input-number
            v-model="row.currentPicked"
            :min="0"
            :max="row.planQuantity - row.pickedQuantity"
            :precision="2"
            size="small"
            class="!w-full"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('action.action')" width="120px" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            size="small"
            @click="handlePickItem(row)"
            :disabled="!row.currentPicked || row.currentPicked <= 0"
          >
            {{ t('wms.confirmPick') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('common.close') }}</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { getOutbound, startPicking, completePicking } from '@/api/wms/outbound'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const outboundData = ref({
  id: undefined,
  outboundNo: '',
  warehouseName: '',
  customerName: '',
  status: undefined,
  items: []
})

/** 打开弹窗 */
const open = async (id: number) => {
  dialogVisible.value = true
  
  // 加载出库单信息
  const data = await getOutbound(id)
  outboundData.value = data
  
  // 初始化拣货数据
  outboundData.value.items.forEach(item => {
    item.currentPicked = 0
  })
  
  // 如果是已审核状态，开始拣货
  if (data.status === 2) {
    await startPicking(id)
  }
}
defineExpose({ open })

/** 确认拣货单个明细 */
const emit = defineEmits(['success'])
const handlePickItem = async (row: any) => {
  try {
    await completePicking(outboundData.value.id, row.id, row.currentPicked)
    message.success(t('wms.pickSuccess'))
    
    // 刷新数据
    const data = await getOutbound(outboundData.value.id)
    outboundData.value.items = data.items
    outboundData.value.items.forEach(item => {
      item.currentPicked = 0
    })
    
    // 如果状态变为待发货，关闭弹窗并刷新列表
    if (data.status === 4) {
      message.success(t('wms.allPickedComplete'))
      dialogVisible.value = false
      emit('success')
    }
  } catch {}
}
</script>
