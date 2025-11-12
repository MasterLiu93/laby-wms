/**
 * 表格列设置Hook
 * 用于统一管理表格列的显示/隐藏功能
 * @author laby
 * @date 2025-11-12
 */
import { ref, reactive } from 'vue'

export interface ColumnConfig {
  prop: string
  label: string
  visible: boolean
  fixed?: boolean // 固定列不可隐藏和拖拽
  width?: number | string
  minWidth?: number | string
}

export function useTableColumns(initialColumns: ColumnConfig[], storageKey: string) {
  // 表格列配置
  const tableColumns = ref<ColumnConfig[]>(initialColumns)
  
  // 可见列状态（响应式对象，用于v-if控制）
  const visibleColumns = reactive<Record<string, boolean>>({})
  
  // 初始化可见列状态
  const initVisibleColumns = () => {
    initialColumns.forEach(col => {
      visibleColumns[col.prop] = col.visible
    })
  }
  
  // 列设置变更处理
  const handleColumnsChange = (columns: ColumnConfig[]) => {
    // 更新tableColumns
    tableColumns.value = [...columns]
    
    // 更新可见列状态
    columns.forEach(col => {
      if (visibleColumns.hasOwnProperty(col.prop)) {
        visibleColumns[col.prop] = col.visible
      }
    })
  }
  
  // 初始化
  initVisibleColumns()
  
  return {
    tableColumns,
    visibleColumns,
    handleColumnsChange
  }
}
