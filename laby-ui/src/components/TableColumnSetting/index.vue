<!--
  表格列设置组件 - RuoYi风格
  功能：点击按钮显示列设置菜单
  @author laby
  @date 2025-11-12
-->
<template>
  <div class="table-column-setting">
    <el-popover
      placement="bottom-end"
      :width="200"
      trigger="click"
      popper-class="table-column-popover"
    >
      <template #reference>
        <el-tooltip :content="t('common.columnSetting')" placement="top">
          <el-button :icon="Setting" circle class="column-setting-btn" />
        </el-tooltip>
      </template>
      
      <div class="column-setting-content">
        <div class="column-header">
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
          >
            {{ t('common.selectAll') }}
          </el-checkbox>
          <el-button 
            type="text" 
            @click="resetColumns"
            class="reset-btn"
            size="small"
          >
            {{ t('common.reset') }}
          </el-button>
        </div>
        
        <el-divider style="margin: 8px 0" />
        
        <div class="column-items">
          <div 
            v-for="column in localColumns" 
            :key="column.prop"
            class="column-item"
          >
            <el-checkbox 
              v-model="column.visible"
              @change="handleColumnToggle"
              :disabled="column.fixed"
            >
              {{ column.label }}
            </el-checkbox>
          </div>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { Setting } from '@element-plus/icons-vue'
import { ref, watch, onMounted } from 'vue'

interface ColumnConfig {
  prop: string
  label: string
  visible: boolean
  fixed?: boolean // 固定列不可隐藏和拖拽
  width?: number | string
  minWidth?: number | string
}

interface Props {
  columns: ColumnConfig[]
  storageKey?: string // 本地存储key
}

const props = withDefaults(defineProps<Props>(), {
  storageKey: 'table-columns'
})

const emit = defineEmits<{
  change: [columns: ColumnConfig[]]
}>()

const { t } = useI18n()

const localColumns = ref<ColumnConfig[]>([])

// 全选相关
const checkAll = ref(false)
const isIndeterminate = ref(false)

/** 初始化列配置 */
const initColumns = () => {
  // 尝试从本地存储获取配置
  const stored = localStorage.getItem(props.storageKey)
  if (stored) {
    try {
      const storedColumns = JSON.parse(stored) as ColumnConfig[]
      // 合并默认配置和存储配置
      localColumns.value = props.columns.map(col => {
        const stored = storedColumns.find(s => s.prop === col.prop)
        return stored ? { ...col, ...stored } : col
      })
    } catch {
      localColumns.value = [...props.columns]
    }
  } else {
    localColumns.value = [...props.columns]
  }
  
  updateCheckStatus()
}

/** 更新全选状态 */
const updateCheckStatus = () => {
  const visibleCount = localColumns.value.filter(col => col.visible).length
  const totalCount = localColumns.value.length
  
  checkAll.value = visibleCount === totalCount
  isIndeterminate.value = visibleCount > 0 && visibleCount < totalCount
}

/** 全选/取消全选 */
const handleCheckAllChange = (checked: boolean) => {
  localColumns.value.forEach(col => {
    if (!col.fixed) {
      col.visible = checked
    }
  })
  updateCheckStatus()
  // 实时更新
  emit('change', [...localColumns.value])
}

/** 列切换处理 */
const handleColumnToggle = () => {
  updateCheckStatus()
  // 实时更新
  emit('change', [...localColumns.value])
}

/** 重置列配置 */
const resetColumns = () => {
  localColumns.value = [...props.columns]
  updateCheckStatus()
  // 实时更新
  emit('change', [...localColumns.value])
}

/** 保存配置到本地存储 */
const saveToStorage = () => {
  localStorage.setItem(props.storageKey, JSON.stringify(localColumns.value))
}

// 初始化
onMounted(() => {
  initColumns()
  emit('change', [...localColumns.value])
})

// 监听props变化
watch(() => props.columns, () => {
  initColumns()
}, { deep: true })

// 监听列变化，实时保存
watch(localColumns, () => {
  saveToStorage()
}, { deep: true })
</script>

<style scoped lang="scss">
.table-column-setting {
  .column-setting-btn {
    margin-left: 8px;
  }
}

.column-setting-content {
  .column-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .reset-btn {
      color: #409eff;
      padding: 0;
      font-size: 12px;
    }
  }
  
  .column-items {
    max-height: 300px;
    overflow-y: auto;
    
    .column-item {
      padding: 4px 0;
      
      .el-checkbox {
        width: 100%;
        
        :deep(.el-checkbox__label) {
          color: #606266;
          font-size: 14px;
        }
      }
      
      &:hover {
        background-color: #f5f7fa;
      }
    }
  }
}

:deep(.table-column-popover) {
  padding: 12px;
}
</style>
