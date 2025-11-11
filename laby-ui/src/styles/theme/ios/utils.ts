// ==================== iOS 主题工具函数 ====================
import type {
  IOSThemeConfig,
  IOSThemeMode,
  IOSThemeUtils,
  IOSThemeVariables
} from './types'

/**
 * iOS 主题工具类
 */
class IOSThemeManager implements IOSThemeUtils {
  private currentMode: IOSThemeMode = 'light'
  private config: Partial<IOSThemeConfig> = {}

  /**
   * 设置主题模式
   */
  setThemeMode(mode: IOSThemeMode): void {
    this.currentMode = mode
    const root = document.documentElement

    if (mode === 'auto') {
      // 自动模式：根据系统偏好设置
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      mode = prefersDark ? 'dark' : 'light'
    }

    if (mode === 'dark') {
      root.classList.add('dark')
      root.classList.remove('light')
    } else {
      root.classList.add('light')
      root.classList.remove('dark')
    }

    // 触发自定义事件
    window.dispatchEvent(
      new CustomEvent('ios-theme-change', {
        detail: { mode }
      })
    )
  }

  /**
   * 获取当前主题模式
   */
  getThemeMode(): IOSThemeMode {
    return this.currentMode
  }

  /**
   * 切换主题模式
   */
  toggleThemeMode(): void {
    const root = document.documentElement
    const isDark = root.classList.contains('dark')
    this.setThemeMode(isDark ? 'light' : 'dark')
  }

  /**
   * 设置主色调
   */
  setPrimaryColor(color: string): void {
    this.setCSSVariable('--ios-color-primary', color)
    this.setCSSVariable('--el-color-primary', color)

    // 更新 RGB 值
    const rgb = this.hexToRgb(color)
    if (rgb) {
      this.setCSSVariable('--ios-color-primary-rgb', `${rgb.r}, ${rgb.g}, ${rgb.b}`)
      this.setCSSVariable('--el-color-primary-rgb', `${rgb.r}, ${rgb.g}, ${rgb.b}`)
    }
  }

  /**
   * 获取 CSS 变量值
   */
  getCSSVariable(name: string): string | null {
    const root = document.documentElement
    const value = getComputedStyle(root).getPropertyValue(name).trim()
    return value || null
  }

  /**
   * 设置 CSS 变量值
   */
  setCSSVariable(name: string, value: string): void {
    const root = document.documentElement
    root.style.setProperty(name, value)
  }

  /**
   * 应用主题配置
   */
  applyTheme(config: Partial<IOSThemeConfig>): void {
    this.config = { ...this.config, ...config }

    if (config.mode) {
      this.setThemeMode(config.mode)
    }

    if (config.primaryColor) {
      this.setPrimaryColor(config.primaryColor)
    }

    if (config.enableAnimation !== undefined) {
      const root = document.documentElement
      if (config.enableAnimation) {
        root.style.setProperty('--ios-transition-fast', '0.15s')
        root.style.setProperty('--ios-transition-base', '0.25s')
        root.style.setProperty('--ios-transition-slow', '0.35s')
      } else {
        root.style.setProperty('--ios-transition-fast', '0s')
        root.style.setProperty('--ios-transition-base', '0s')
        root.style.setProperty('--ios-transition-slow', '0s')
      }
    }

    if (config.enableBorderRadius !== undefined) {
      const root = document.documentElement
      if (config.enableBorderRadius) {
        root.style.setProperty('--ios-border-radius-small', '8px')
        root.style.setProperty('--ios-border-radius-medium', '12px')
        root.style.setProperty('--ios-border-radius-large', '16px')
      } else {
        root.style.setProperty('--ios-border-radius-small', '0px')
        root.style.setProperty('--ios-border-radius-medium', '0px')
        root.style.setProperty('--ios-border-radius-large', '0px')
      }
    }

    if (config.customColors) {
      Object.entries(config.customColors).forEach(([key, value]) => {
        this.setCSSVariable(`--ios-color-${key}`, value)
      })
    }
  }

  /**
   * 监听系统主题变化（用于 auto 模式）
   */
  watchSystemTheme(): void {
    if (this.currentMode === 'auto') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      const handler = (e: MediaQueryListEvent) => {
        this.setThemeMode(e.matches ? 'dark' : 'light')
      }

      // 现代浏览器
      if (mediaQuery.addEventListener) {
        mediaQuery.addEventListener('change', handler)
      } else {
        // 兼容旧浏览器
        mediaQuery.addListener(handler)
      }
    }
  }

  /**
   * 获取主题变量
   */
  getThemeVariables(): Partial<IOSThemeVariables> {
    return {
      colors: {
        primary: this.getCSSVariable('--ios-color-primary') || '#007AFF',
        success: this.getCSSVariable('--ios-color-success') || '#34C759',
        warning: this.getCSSVariable('--ios-color-warning') || '#FF9500',
        danger: this.getCSSVariable('--ios-color-danger') || '#FF3B30',
        info: this.getCSSVariable('--ios-color-info') || '#5AC8FA',
        gray: {
          1: this.getCSSVariable('--ios-color-gray-1') || '#8E8E93',
          2: this.getCSSVariable('--ios-color-gray-2') || '#C7C7CC',
          3: this.getCSSVariable('--ios-color-gray-3') || '#D1D1D6',
          4: this.getCSSVariable('--ios-color-gray-4') || '#E5E5EA',
          5: this.getCSSVariable('--ios-color-gray-5') || '#F2F2F7'
        }
      },
      backgrounds: {
        primary: this.getCSSVariable('--ios-bg-primary') || '#F2F2F7',
        secondary: this.getCSSVariable('--ios-bg-secondary') || '#FFFFFF',
        tertiary: this.getCSSVariable('--ios-bg-tertiary') || '#F9F9F9'
      },
      text: {
        primary: this.getCSSVariable('--ios-text-primary') || '#000000',
        secondary: this.getCSSVariable('--ios-text-secondary') || '#3C3C43',
        tertiary: this.getCSSVariable('--ios-text-tertiary') || '#8E8E93',
        quaternary: this.getCSSVariable('--ios-text-quaternary') || '#C7C7CC'
      }
    }
  }

  /**
   * 十六进制颜色转 RGB
   */
  private hexToRgb(hex: string): { r: number; g: number; b: number } | null {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
    return result
      ? {
          r: parseInt(result[1], 16),
          g: parseInt(result[2], 16),
          b: parseInt(result[3], 16)
        }
      : null
  }
}

// 导出单例实例
export const iosTheme = new IOSThemeManager()

// 导出工具函数
export const useIOSTheme = () => {
  return iosTheme
}

/**
 * 初始化 iOS 主题
 */
export const initIOSTheme = (config?: Partial<IOSThemeConfig>) => {
  if (config) {
    iosTheme.applyTheme(config)
  }

  // 监听系统主题变化
  iosTheme.watchSystemTheme()

  // 监听主题变化事件
  window.addEventListener('ios-theme-change', ((e: CustomEvent) => {
    console.log('iOS Theme changed:', e.detail)
  }) as EventListener)

  return iosTheme
}

