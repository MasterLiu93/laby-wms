<script lang="ts" setup>
import { useLocaleStore } from '@/store/modules/locale'
import { useLocale } from '@/hooks/web/useLocale'
import { propTypes } from '@/utils/propTypes'
import { useDesign } from '@/hooks/web/useDesign'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'

defineOptions({ name: 'LocaleDropdown' })

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('locale-dropdown')

defineProps({
  color: propTypes.string.def('')
})

const localeStore = useLocaleStore()
const { wsCache } = useCache()

const langMap = computed(() => localeStore.getLocaleMap)

const currentLang = computed(() => localeStore.getCurrentLocale)

const setLang = (lang: LocaleType) => {
  if (lang === unref(currentLang).lang) return
  
  // 先保存语言设置
  localeStore.setCurrentLocale({
    lang
  })
  const { changeLocale } = useLocale()
  changeLocale(lang)
  
  // 清除菜单缓存，确保重新加载时从服务器获取新数据
  wsCache.delete(CACHE_KEY.ROLE_ROUTERS)
  
  // 然后重新加载页面让整个系统重新初始化（包括菜单）
  window.location.reload()
}
</script>

<template>
  <ElDropdown :class="prefixCls" trigger="click" @command="setLang">
    <Icon
      :class="$attrs.class"
      :color="color"
      :size="18"
      class="cursor-pointer !p-0"
      icon="ion:language-sharp"
    />
    <template #dropdown>
      <ElDropdownMenu>
        <ElDropdownItem v-for="item in langMap" :key="item.lang" :command="item.lang">
          {{ item.name }}
        </ElDropdownItem>
      </ElDropdownMenu>
    </template>
  </ElDropdown>
</template>
