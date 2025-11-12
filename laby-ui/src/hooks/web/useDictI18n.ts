import { computed } from 'vue'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import type { DictDataType } from '@/utils/dict'

/**
 * 字典国际化 Hook
 * 用于在模板中显示字典的国际化标签
 */
export const useDictI18n = () => {
  const localeStore = useLocaleStoreWithOut()
  
  /**
   * 获取字典的国际化标签
   * @param dict 字典数据对象
   * @returns 根据当前语言返回对应的标签
   */
  const getDictLabel = (dict: DictDataType): string => {
    const currentLang = localeStore.getCurrentLocale.lang
    // 如果是英文且有英文标签，返回英文标签；否则返回中文标签
    if (currentLang === 'en' && dict.labelEn) {
      return dict.labelEn
    }
    return dict.label
  }

  /**
   * 判断当前是否为英文环境
   */
  const isEnglish = computed(() => localeStore.getCurrentLocale.lang === 'en')

  return {
    getDictLabel,
    isEnglish
  }
}
