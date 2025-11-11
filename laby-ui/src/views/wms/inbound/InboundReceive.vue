<template>
  <Dialog :title="t('wms.inboundReceive')" v-model="dialogVisible" width="1000px">
    <el-descriptions :column="2" border class="mb-20px">
      <el-descriptions-item :label="t('wms.inboundNo')">{{ inboundData.inboundNo }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.warehouse')">{{ inboundData.warehouseName }}</el-descriptions-item>
      <el-descriptions-item :label="t('wms.supplierName')">{{ inboundData.supplierName || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('common.status')">
        <dict-tag :type="DICT_TYPE.WMS_INBOUND_STATUS" :value="inboundData.status" />
      </el-descriptions-item>
    </el-descriptions>

    <h3>{{ t('wms.inboundDetailTitle') }}</h3>
    <el-table :data="inboundData.items" border class="mt-10px">
      <el-table-column :label="t('wms.goodsName')" prop="goodsName" width="150px" />
      <el-table-column :label="t('wms.skuCode')" prop="skuCode" width="150px" />
      <el-table-column :label="t('wms.batchNo')" prop="batchNo" width="120px">
        <template #default="scope">{{ scope.row.batchNo || '-' }}</template>
      </el-table-column>
      <el-table-column :label="t('wms.planQuantity')" prop="planQuantity" width="100px" />
      <el-table-column :label="t('wms.receivedQuantity')" prop="receivedQuantity" width="100px" />
      <el-table-column :label="t('wms.currentReceived')" width="120px">
        <template #default="{ row }">
          <!-- 已收货：显示实收数量（绿色加粗） -->
          <span v-if="row.receivedQuantity > 0" class="text-green-600 font-semibold">
            {{ row.receivedQuantity }}
          </span>
          <!-- 未收货：显示输入框 -->
          <el-input-number
            v-else
            v-model="row.currentReceived"
            :min="0"
            :max="row.planQuantity - row.receivedQuantity"
            :precision="2"
            size="small"
            class="!w-full"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('wms.currentQualified')" width="120px">
        <template #default="{ row }">
          <!-- 已收货：显示合格数量（绿色加粗） -->
          <span v-if="row.receivedQuantity > 0" class="text-green-600 font-semibold">
            {{ row.qualifiedQuantity }}
          </span>
          <!-- 未收货：显示输入框 -->
          <el-input-number
            v-else
            v-model="row.currentQualified"
            :min="0"
            :max="row.currentReceived || 0"
            :precision="2"
            size="small"
            class="!w-full"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('wms.unqualifiedQuantity')" width="130px">
        <template #default="{ row }">
          <!-- 已收货：显示不合格数量（红色加粗） -->
          <span v-if="row.receivedQuantity > 0" class="text-red-600 font-semibold">
            {{ row.unqualifiedQuantity }}
          </span>
          <!-- 未收货：计算不合格数量 -->
          <span v-else>
            {{ ((row.currentReceived || 0) - (row.currentQualified || 0)).toFixed(2) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('action.action')" width="120px" fixed="right">
        <template #default="{ row }">
          <!-- 已收货：显示已确认 -->
          <el-tag v-if="row.receivedQuantity > 0" type="success">{{ t('wms.confirmed') }}</el-tag>
          <!-- 未收货：显示确认收货按钮 -->
          <el-button
            v-else
            link
            type="primary"
            size="small"
            @click="handleReceiveItem(row)"
            :disabled="!row.currentReceived || row.currentReceived <= 0"
          >
            {{ t('wms.confirmReceive') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('common.close') }}</el-button>
      <el-button type="success" @click="handleComplete">{{ t('wms.completeInbound') }}</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { getInbound, startReceiving, completeReceiving, completeInbound } from '@/api/wms/inbound'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const inboundData = ref({
  id: undefined,
  inboundNo: '',
  warehouseName: '',
  supplierName: '',
  status: undefined,
  items: []
})

/** 打开弹窗 */
const open = async (id: number) => {
  dialogVisible.value = true
  
  // 加载入库单信息
  const data = await getInbound(id)
  inboundData.value = data
  
  // 为未收货的明细初始化临时字段（用于输入框绑定）
  inboundData.value.items.forEach(item => {
    if (item.receivedQuantity === 0 || item.receivedQuantity === null) {
      // 默认值设置为计划数量，方便用户操作
      item.currentReceived = item.planQuantity || 0
      item.currentQualified = item.planQuantity || 0
    }
  })
  
  // 如果是已审核状态，开始收货
  if (data.status === 2) {
    await startReceiving(id)
    inboundData.value.status = 3 // 更新状态为收货中
  }
}
defineExpose({ open })

/** 确认收货单个明细 */
const handleReceiveItem = async (row: any) => {
  try {
    await completeReceiving(
      inboundData.value.id,
      row.id,
      row.currentReceived,
      row.currentQualified,
      row.currentReceived - row.currentQualified
    )
    message.success(t('wms.receiveSuccess'))
    
    // 刷新数据，显示后端返回的实际收货数量
    const data = await getInbound(inboundData.value.id)
    inboundData.value.items = data.items
    
    // 为未收货的明细初始化临时字段
    inboundData.value.items.forEach(item => {
      if (item.receivedQuantity === 0 || item.receivedQuantity === null) {
        item.currentReceived = item.planQuantity || 0
        item.currentQualified = item.planQuantity || 0
      }
    })
  } catch {}
}

/** 完成入库 */
const emit = defineEmits(['success'])
const handleComplete = async () => {
  try {
    // 检查是否所有明细都已确认收货
    const unreceivedItems = inboundData.value.items.filter(item => 
      !item.receivedQuantity || item.receivedQuantity === 0
    )
    
    if (unreceivedItems.length > 0) {
      await message.error(t('wms.hasUnreceivedItems').replace('{count}', unreceivedItems.length))
      return
    }
    
    // 检查是否有合格数量为0的明细
    const zeroQualifiedItems = inboundData.value.items.filter(item => 
      !item.qualifiedQuantity || item.qualifiedQuantity === 0
    )
    
    if (zeroQualifiedItems.length === inboundData.value.items.length) {
      await message.error(t('wms.allZeroQualified'))
      return
    }
    
    if (zeroQualifiedItems.length > 0) {
      await message.warning(t('wms.hasZeroQualified').replace('{count}', zeroQualifiedItems.length))
    }
    
    await message.confirm(t('wms.completeInboundConfirm'))
    // TODO: 获取当前用户信息
    await completeInbound(inboundData.value.id, 1, 'admin')
    message.success(t('wms.inboundCompleteSuccess'))
    dialogVisible.value = false
    emit('success')
  } catch {}
}
</script>
