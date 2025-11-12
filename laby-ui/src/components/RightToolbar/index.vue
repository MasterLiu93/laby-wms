<template>
  <div class="top-right-btn" :style="style">
    <el-row>
      <el-tooltip
        class="item"
        effect="dark"
        :content="showSearch ? '隐藏搜索' : '显示搜索'"
        placement="top"
        v-if="search"
      >
        <el-button size="small" circle :icon="Search" @click="toggleSearch()" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" content="刷新" placement="top">
        <el-button size="small" circle :icon="Refresh" @click="refresh()" />
      </el-tooltip>
      <el-tooltip
        class="item"
        effect="dark"
        content="显隐列"
        placement="top"
        v-if="Object.keys(columns).length > 0"
      >
        <el-dropdown
          trigger="click"
          :hide-on-click="false"
          style="padding-left: 12px"
        >
          <el-button size="small" circle :icon="Operation" />
          <template #dropdown>
            <el-dropdown-menu>
              <!-- 全选/反选 按钮 -->
              <el-dropdown-item>
                <el-checkbox
                  :indeterminate="isIndeterminate"
                  v-model="isChecked"
                  @change="toggleCheckAll"
                >
                  {{ t('common.columnDisplay') }}
                </el-checkbox>
              </el-dropdown-item>
              <div class="check-line"></div>
              <template v-for="(item, key) in columns" :key="key">
                <el-dropdown-item>
                  <el-checkbox
                    v-model="(item as ColumnItem).visible"
                    @change="(val: boolean) => checkboxChange(val, key)"
                  >
                    {{ (item as ColumnItem).label }}
                  </el-checkbox>
                </el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-tooltip>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh, Operation } from '@element-plus/icons-vue'
import { computed } from 'vue'

interface ColumnItem {
  visible: boolean
  label: string
  key?: string
}

interface Props {
  /* 是否显示检索条件 */
  showSearch?: boolean
  /* 显隐列信息（数组格式、对象格式） */
  columns?: Record<string, ColumnItem> | ColumnItem[]
  /* 是否显示检索图标 */
  search?: boolean
  /* 右外边距 */
  gutter?: number
}

const props = withDefaults(defineProps<Props>(), {
  showSearch: true,
  columns: () => ({}),
  search: true,
  gutter: 10
})

const emit = defineEmits<{
  'update:showSearch': [value: boolean]
  queryTable: []
}>()

const { t } = useI18n()

const style = computed(() => {
  const ret: Record<string, string> = {}
  if (props.gutter) {
    ret.marginRight = `${props.gutter / 2}px`
  }
  return ret
})

const isChecked = computed({
  get() {
    return Array.isArray(props.columns)
      ? props.columns.every((col) => col.visible)
      : Object.values(props.columns).every((col) => col.visible)
  },
  set() {}
})

const isIndeterminate = computed(() => {
  return Array.isArray(props.columns)
    ? props.columns.some((col) => col.visible) && !isChecked.value
    : Object.values(props.columns).some((col) => col.visible) && !isChecked.value
})

// 搜索
const toggleSearch = () => {
  emit('update:showSearch', !props.showSearch)
}

// 刷新
const refresh = async () => {
  const { ElMessage } = await import('element-plus')
  try {
    // 显示刷新中提示
    ElMessage.info(t('common.refreshing'))
    // 触发刷新事件
    emit('queryTable')
    // 等待一小段时间后显示成功提示
    setTimeout(() => {
      ElMessage.success(t('common.refreshSuccess'))
    }, 500)
  } catch (error) {
    ElMessage.error(t('common.refreshFailed'))
  }
}

// 单勾选
const checkboxChange = (event: boolean, key: string) => {
  if (Array.isArray(props.columns)) {
    const item = props.columns.find(item => item.key == key)
    if (item) {
      item.visible = event
    }
  } else {
    const column = props.columns[key] as ColumnItem
    if (column) {
      column.visible = event
    }
  }
}

// 切换全选/反选
const toggleCheckAll = () => {
  const newValue = !isChecked.value
  if (Array.isArray(props.columns)) {
    props.columns.forEach((col) => (col.visible = newValue))
  } else {
    Object.values(props.columns).forEach((col) => (col.visible = newValue))
  }
}
</script>

<style lang="scss" scoped>
.check-line {
  width: 90%;
  height: 1px;
  background-color: #ccc;
  margin: 3px auto;
}
</style>
