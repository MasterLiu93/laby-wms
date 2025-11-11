// ==================== iOS 主题 TypeScript 类型定义 ====================

/**
 * iOS 主题颜色类型
 */
export type IOSColorType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

/**
 * iOS 主题模式
 */
export type IOSThemeMode = 'light' | 'dark' | 'auto'

/**
 * iOS 字体大小类型
 */
export type IOSFontSize =
  | 'large-title'
  | 'title-1'
  | 'title-2'
  | 'title-3'
  | 'headline'
  | 'body'
  | 'callout'
  | 'subheadline'
  | 'footnote'
  | 'caption-1'
  | 'caption-2'

/**
 * iOS 字体粗细类型
 */
export type IOSFontWeight = 'regular' | 'medium' | 'semibold' | 'bold'

/**
 * iOS 圆角大小类型
 */
export type IOSBorderRadius = 'small' | 'medium' | 'large' | 'xlarge'

/**
 * iOS 间距大小类型
 */
export type IOSSpacing = 'xs' | 'sm' | 'md' | 'lg' | 'xl' | 'xxl' | 'xxxl'

/**
 * iOS 阴影大小类型
 */
export type IOSShadow = 'sm' | 'md' | 'lg' | 'xl'

/**
 * iOS 动画时长类型
 */
export type IOSTransition = 'fast' | 'base' | 'slow' | 'slower'

/**
 * iOS 主题配置接口
 */
export interface IOSThemeConfig {
  /** 主题模式 */
  mode: IOSThemeMode
  /** 主色调 */
  primaryColor: string
  /** 是否启用动画 */
  enableAnimation: boolean
  /** 是否启用圆角 */
  enableBorderRadius: boolean
  /** 自定义颜色映射 */
  customColors?: Record<string, string>
}

/**
 * iOS 主题变量接口
 */
export interface IOSThemeVariables {
  // 颜色
  colors: {
    primary: string
    success: string
    warning: string
    danger: string
    info: string
    gray: {
      1: string
      2: string
      3: string
      4: string
      5: string
    }
  }
  // 背景色
  backgrounds: {
    primary: string
    secondary: string
    tertiary: string
  }
  // 文字颜色
  text: {
    primary: string
    secondary: string
    tertiary: string
    quaternary: string
  }
  // 字体
  fonts: {
    family: string
    sizes: Record<IOSFontSize, string>
    weights: Record<IOSFontWeight, number>
  }
  // 圆角
  borderRadius: Record<IOSBorderRadius, string>
  // 间距
  spacing: Record<IOSSpacing, string>
  // 阴影
  shadows: Record<IOSShadow, string>
  // 动画
  transitions: {
    duration: Record<IOSTransition, string>
    easing: {
      'ease-in-out': string
      'ease-out': string
      'ease-in': string
      spring: string
    }
  }
}

/**
 * iOS 主题工具函数类型
 */
export interface IOSThemeUtils {
  /** 设置主题模式 */
  setThemeMode: (mode: IOSThemeMode) => void
  /** 获取当前主题模式 */
  getThemeMode: () => IOSThemeMode
  /** 切换主题模式 */
  toggleThemeMode: () => void
  /** 设置主色调 */
  setPrimaryColor: (color: string) => void
  /** 获取CSS变量值 */
  getCSSVariable: (name: string) => string | null
  /** 设置CSS变量值 */
  setCSSVariable: (name: string, value: string) => void
  /** 应用主题配置 */
  applyTheme: (config: Partial<IOSThemeConfig>) => void
}

/**
 * Element Plus 组件尺寸类型（iOS 适配）
 */
export type IOSComponentSize = 'small' | 'default' | 'large'

/**
 * iOS 主题预设配置
 */
export const IOS_THEME_PRESETS: Record<string, Partial<IOSThemeConfig>> = {
  default: {
    mode: 'light',
    primaryColor: '#007AFF',
    enableAnimation: true,
    enableBorderRadius: true
  },
  dark: {
    mode: 'dark',
    primaryColor: '#007AFF',
    enableAnimation: true,
    enableBorderRadius: true
  },
  highContrast: {
    mode: 'light',
    primaryColor: '#007AFF',
    enableAnimation: false,
    enableBorderRadius: true
  }
}

