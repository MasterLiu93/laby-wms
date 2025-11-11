// ==================== iOS 主题初始化插件 ====================
import type { App } from 'vue'
import { nextTick } from 'vue'
import { initIOSTheme } from '@/styles/theme/ios/utils'
import { setCssVar } from '@/utils'
import { useAppStore } from '@/store/modules/app'

/**
 * 初始化 iOS 主题
 */
export const setupIOSTheme = (app: App<Element>) => {
  // iOS 主题色配置
  const iosPrimaryColor = '#007AFF'
  
  // 立即设置 CSS 变量（不依赖 store）
  setCssVar('--el-color-primary', iosPrimaryColor)
  setCssVar('--el-color-success', '#34C759')
  setCssVar('--el-color-warning', '#FF9500')
  setCssVar('--el-color-danger', '#FF3B30')
  setCssVar('--el-color-info', '#5AC8FA')
  
  // 设置 RGB 值
  setCssVar('--el-color-primary-rgb', '0, 122, 255')
  setCssVar('--el-color-success-rgb', '52, 199, 89')
  setCssVar('--el-color-warning-rgb', '255, 149, 0')
  setCssVar('--el-color-danger-rgb', '255, 59, 48')
  setCssVar('--el-color-info-rgb', '90, 200, 250')
  
  // 设置字体为 monospace
  setCssVar('--el-font-family', 'monospace')

  // 在下一个 tick 中初始化（确保 store 已初始化）
  nextTick(() => {
    try {
      const appStore = useAppStore()
      
      // 更新 store 中的主题色
      const currentTheme = appStore.getTheme
      if (currentTheme.elColorPrimary !== iosPrimaryColor) {
        appStore.setTheme({ elColorPrimary: iosPrimaryColor })
      }
      
      // 初始化 iOS 主题系统
      const isDark = document.documentElement.classList.contains('dark')
      initIOSTheme({
        mode: isDark ? 'dark' : 'light',
        primaryColor: iosPrimaryColor,
        enableAnimation: true,
        enableBorderRadius: true
      })

      // 监听暗色模式变化
      appStore.$subscribe((mutation, state) => {
        if (mutation.events?.key === 'isDark') {
          initIOSTheme({
            mode: state.isDark ? 'dark' : 'light',
            primaryColor: iosPrimaryColor,
            enableAnimation: true,
            enableBorderRadius: true
          })
        }
      })

      // 设置 Element Plus 主色调的浅色变体
      appStore.setPrimaryLight()

      console.log('✅ iOS Theme initialized')
    } catch (error) {
      console.warn('iOS Theme initialization delayed:', error)
      // 如果 store 还未初始化，延迟执行
      setTimeout(() => {
        try {
          const appStore = useAppStore()
          appStore.setTheme({ elColorPrimary: iosPrimaryColor })
          appStore.setPrimaryLight()
        } catch (e) {
          console.error('Failed to initialize iOS theme:', e)
        }
      }, 100)
    }
  })
}

