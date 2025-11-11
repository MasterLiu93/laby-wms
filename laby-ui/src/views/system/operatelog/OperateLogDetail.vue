<template>
  <Dialog v-model="dialogVisible" :max-height="500" :scroll="true" :title="t('system.operateLog.detailTitle')" width="800">
    <el-descriptions :column="1" border>
      <el-descriptions-item :label="t('system.operateLog.logId')" min-width="120">
        {{ detailData.id }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.traceId')" v-if="detailData.traceId">
        {{ detailData.traceId }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.operatorId')">
        {{ detailData.userId }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.operatorName')">
        {{ detailData.userName }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.operatorIp')">
        {{ detailData.userIp }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.operatorUA')">
        {{ detailData.userAgent }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.operationModule')">
        {{ detailData.type }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.operationName')">
        {{ detailData.subType }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.operationContent')">
        {{ detailData.action }}
      </el-descriptions-item>
      <el-descriptions-item v-if="detailData.extra" :label="t('system.operateLog.operationExtra')">
        {{ detailData.extra }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.requestMethodLabel')">
        {{ detailData.requestMethod }} {{ detailData.requestUrl }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.operationTime')">
        {{ formatDate(detailData.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('system.operateLog.businessId')">
        {{ detailData.bizId }}
      </el-descriptions-item>
    </el-descriptions>
  </Dialog>
</template>
<script lang="ts" setup>
import { formatDate } from '@/utils/formatTime'
import * as OperateLogApi from '@/api/system/operatelog'

defineOptions({ name: 'SystemOperateLogDetail' })

const { t } = useI18n() // 国际化

const dialogVisible = ref(false) // 弹窗的是否展示
const detailLoading = ref(false) // 表单的加载中
const detailData = ref({} as OperateLogApi.OperateLogVO) // 详情数据

/** 打开弹窗 */
const open = async (data: OperateLogApi.OperateLogVO) => {
  dialogVisible.value = true
  // 设置数据
  detailLoading.value = true
  try {
    detailData.value = data
  } finally {
    detailLoading.value = false
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗
</script>
