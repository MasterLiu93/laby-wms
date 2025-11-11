<script lang="ts" setup>
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'
import { useLocaleStore } from '@/store/modules/locale'

// eslint-disable-next-line vue/no-reserved-component-names
defineOptions({ name: 'Footer' })

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('footer')

const appStore = useAppStore()
const localeStore = useLocaleStore()

const title = computed(() => {
  const currentLang = localeStore.getCurrentLocale.lang
  return currentLang === 'en' ? 'Laby Management System' : 'Laby管理系统'
})

// 添加当前年份计算属性
const currentYear = computed(() => new Date().getFullYear())
</script>

<template>
  <div
    :class="prefixCls"
    class="h-[var(--app-footer-height)] bg-[var(--app-content-bg-color)] text-center leading-[var(--app-footer-height)] text-[var(--el-text-color-placeholder)] dark:bg-[var(--el-bg-color)] overflow-hidden"
  >
    <span class="text-14px">Copyright ©{{ currentYear }} {{ title }}</span>
  </div>
</template>
