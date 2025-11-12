<!--
  简化版表格列设置组件 - 完全参考RuoYi实现
  @author laby
  @date 2025-11-12
-->
<template>
  <div class="right-toolbar">
    <el-tooltip content="显示/隐藏" placement="top">
      <el-button 
        circle 
        :icon="Setting"
        @click="showColumn = !showColumn"
      />
    </el-tooltip>
    
    <!-- 列设置面板 -->
    <div 
      v-show="showColumn" 
      class="column-show-panel"
      v-click-outside="hideColumn"
    >
      <div class="panel-header">
        <span>列展示</span>
        <el-button type="text" @click="resetColumns">重置</el-button>
      </div>
      
      <el-divider style="margin: 8px 0" />
      
      <div class="panel-content">
        <div 
          v-for="column in columns" 
          :key="column.prop"
          class="column-item"
        >
          <el-checkbox 
            v-model="column.visible"
            :disabled="column.fixed"
            @change="handleColumnChange"
          >
            {{ column.label }}
          </el-checkbox>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Setting } from '@element-plus/icons-vue'
import { ref } from 'vue'

interface ColumnConfig {
  prop: string
  label: string
  visible: boolean
  fixed?: boolean
}

interface Props {
  columns: ColumnConfig[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  change: [columns: ColumnConfig[]]
}>()

const showColumn = ref(false)

/** 隐藏面板 */
const hideColumn = () => {
  showColumn.value = false
}

/** 列变更处理 */
const handleColumnChange = () => {
  emit('change', [...props.columns])
}

/** 重置列 */
const resetColumns = () => {
  props.columns.forEach(col => {
    if (!col.fixed) {
      col.visible = true
    }
  })
  emit('change', [...props.columns])
}
</script>

<style scoped lang="scss">
.right-toolbar {
  position: relative;
  
  .column-show-panel {
    position: absolute;
    top: 40px;
    right: 0;
    width: 200px;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    padding: 12px;
    
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 500;
      
      .el-button {
        padding: 0;
        color: #409eff;
        font-size: 12px;
      }
    }
    
    .panel-content {
      .column-item {
        padding: 4px 0;
        
        .el-checkbox {
          width: 100%;
        }
      }
    }
  }
}
</style>
