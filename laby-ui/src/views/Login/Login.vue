<template>
  <div class="elegant-login-page">
    <!-- 背景装饰 -->
    <div class="bg-decorations">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
      <div class="decoration-circle circle-4"></div>
      <div class="decoration-circle circle-5"></div>
    </div>

    <!-- 顶部工具栏 -->
    <div class="top-toolbar">
      <div class="toolbar-right">
        <ThemeSwitch />
        <LocaleDropdown class="ml-2" />
      </div>
    </div>

    <!-- 主登录卡片 -->
    <div class="login-main-card">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-logo">
            <div class="logo-icon-circle">
              <Icon icon="mdi:warehouse" :size="48" />
            </div>
          </div>
          
          <h1 class="brand-name">{{ i18nText.brandName }}</h1>
          <p class="brand-welcome">{{ i18nText.brandWelcome }}</p>
          
          <div class="dots-indicator">
            <span class="dot active"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
          
          <p class="brand-tagline">{{ i18nText.brandTagline }}</p>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-section">
        <div class="form-container">
          <!-- 登录表单 -->
          <div class="form-body">
            <LoginForm />
            <MobileForm />
            <QrCodeForm />
            <RegisterForm />
            <SSOLoginVue />
            <ForgetPasswordForm />
          </div>

          <!-- 分隔线（仅登录页显示） -->
          <div v-if="getLoginState === LoginStateEnum.LOGIN" class="login-divider">
            <span>{{ i18nText.divider }}</span>
          </div>

          <!-- 立即注册 / 返回登录 -->
          <el-button 
            v-if="getLoginState === LoginStateEnum.LOGIN"
            class="register-btn" 
            size="default" 
            @click="setLoginState(LoginStateEnum.REGISTER)"
          >
            {{ i18nText.registerBtn }}
          </el-button>
          <el-button 
            v-else
            class="register-btn" 
            size="default" 
            @click="setLoginState(LoginStateEnum.LOGIN)"
          >
            {{ isEnglish ? 'Back to login' : '返回登录' }}
          </el-button>

          <!-- 社交登录（仅登录页显示） -->
          <div v-if="getLoginState === LoginStateEnum.LOGIN" class="social-login">
            <button class="social-icon-btn" title="Twitter">
              <Icon icon="mdi:twitter" :size="18" />
            </button>
            <button class="social-icon-btn" title="Facebook">
              <Icon icon="mdi:facebook" :size="18" />
            </button>
            <button class="social-icon-btn" title="GitHub">
              <Icon icon="mdi:github" :size="18" />
            </button>
          </div>

          <!-- 服务条款 -->
          <div class="terms">
            <p>
              {{ i18nText.termsText }} 
              <a href="#">{{ i18nText.termsOfService }}</a> 
              {{ i18nText.and }} 
              <a href="#">{{ i18nText.privacyPolicy }}</a>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useLocaleStore } from '@/store/modules/locale'
import { ThemeSwitch } from '@/layout/components/ThemeSwitch'
import { LocaleDropdown } from '@/layout/components/LocaleDropdown'
import { Icon } from '@/components/Icon'
import { LoginForm, MobileForm, QrCodeForm, RegisterForm, SSOLoginVue, ForgetPasswordForm } from './components'
import { LoginStateEnum, useLoginState } from './components/useLogin'

defineOptions({ name: 'Login' })

const localeStore = useLocaleStore()
const { getLoginState, setLoginState } = useLoginState()

// 国际化文本
const isEnglish = computed(() => localeStore.getCurrentLocale.lang === 'en')

const i18nText = computed(() => ({
  brandName: isEnglish.value ? 'Laby Management System' : 'Laby管理系统',
  brandWelcome: isEnglish.value ? 'Welcome to warehouse management system' : '欢迎使用仓储管理系统',
  brandTagline: 'Intelligent warehouse management system for your business',
  formTitle: isEnglish.value ? 'Laby Management System' : 'Laby管理系统',
  formSubtitle: isEnglish.value ? 'Sign in' : '登录',
  divider: isEnglish.value ? 'or' : '或',
  registerBtn: isEnglish.value ? 'Sign up now' : '立即注册',
  termsText: isEnglish.value 
    ? 'By continuing, you agree to our' 
    : '继续使用即表示您同意我们的',
  termsOfService: isEnglish.value ? 'Terms of Service' : '服务条款',
  and: isEnglish.value ? 'and' : '和',
  privacyPolicy: isEnglish.value ? 'Privacy Policy' : '隐私政策'
}))
</script>

<style lang="scss" scoped>
.elegant-login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #e3f2fd 0%, #fce4ec 100%);
  overflow: hidden;
  
  .dark & {
    background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%);
  }
}

// 背景装饰圆圈
.bg-decorations {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0.1));
  
  .dark & {
    background: radial-gradient(circle, rgba(139, 92, 246, 0.3), rgba(139, 92, 246, 0.05));
  }
  
  &.circle-1 {
    width: 300px;
    height: 300px;
    top: -100px;
    left: -100px;
    animation: float 20s ease-in-out infinite;
  }
  
  &.circle-2 {
    width: 200px;
    height: 200px;
    top: 20%;
    right: 10%;
    animation: float 15s ease-in-out infinite 2s;
  }
  
  &.circle-3 {
    width: 150px;
    height: 150px;
    bottom: 10%;
    left: 15%;
    animation: float 18s ease-in-out infinite 4s;
  }
  
  &.circle-4 {
    width: 250px;
    height: 250px;
    bottom: -80px;
    right: -80px;
    animation: float 22s ease-in-out infinite 1s;
  }
  
  &.circle-5 {
    width: 100px;
    height: 100px;
    top: 60%;
    left: 5%;
    animation: float 16s ease-in-out infinite 3s;
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(20px, -20px) scale(1.05);
  }
  50% {
    transform: translate(-15px, 15px) scale(0.95);
  }
  75% {
    transform: translate(25px, 10px) scale(1.02);
  }
}

// 顶部工具栏
.top-toolbar {
  position: absolute;
  top: 16px;
  right: 24px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  z-index: 100;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 主登录卡片
.login-main-card {
  position: relative;
  z-index: 10;
  display: flex;
  width: 100%;
  max-width: 900px;
  height: auto;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  animation: cardSlideUp 0.6s ease-out;
  
  .dark & {
    background: #1e293b;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.7);
    border: 1px solid rgba(148, 163, 184, 0.1);
  }
  
  @media (max-width: 968px) {
    flex-direction: column;
    max-width: 460px;
  }
}

@keyframes cardSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 左侧品牌区
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #5eb3f6 0%, #b39ddb 50%, #f48fb1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 30px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: 
      radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  }
  
  @media (max-width: 968px) {
    min-height: 240px;
    padding: 30px 20px;
  }
}

.brand-content {
  position: relative;
  text-align: center;
  color: white;
}

.brand-logo {
  margin-bottom: 20px;
}

.logo-icon-circle {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255, 255, 255, 0.3);
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

.brand-name {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  letter-spacing: -0.5px;
}

.brand-welcome {
  font-size: 14px;
  margin: 0 0 24px 0;
  opacity: 0.95;
}

.dots-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 20px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;
  
  &.active {
    background: white;
    width: 20px;
    border-radius: 3px;
  }
}

.brand-tagline {
  font-size: 13px;
  opacity: 0.85;
  margin: 0;
  max-width: 280px;
  line-height: 1.5;
}

// 右侧表单区
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px 30px 20px;
  background: white;
  
  .dark & {
    background: #1e293b;
  }
  
  @media (max-width: 768px) {
    padding: 24px 20px;
  }
}

.form-container {
  width: 100%;
  max-width: 340px;
}

.form-brand {
  margin-bottom: 16px;
}

.form-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
  
  .title-primary {
    color: #5eb3f6;
  }
  
  .title-secondary {
    color: #1f2937;
    
    .dark & {
      color: #f1f5f9;
    }
  }
}

.form-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  font-weight: 500;
  
  .dark & {
    color: #cbd5e1;
  }
}

.form-body {
  margin-bottom: 12px;
  
  // 自定义表单样式 - 增加间距避免验证错误提示被遮挡
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-input__wrapper) {
    padding: 10px 14px;
    border-radius: 8px;
    box-shadow: none;
    border: 1px solid #e2e8f0;
    transition: all 0.3s ease;
    background: white;
    
    .dark & {
      background: #0f172a;
      border-color: #334155;
      
      .el-input__inner {
        color: #f1f5f9;
      }
    }
    
    &:hover {
      border-color: #cbd5e1;
      
      .dark & {
        border-color: #475569;
      }
    }
    
    &.is-focus {
      border-color: #5eb3f6;
      box-shadow: 0 0 0 3px rgba(94, 179, 246, 0.1);
      
      .dark & {
        box-shadow: 0 0 0 3px rgba(94, 179, 246, 0.2);
      }
    }
  }
  
  :deep(.el-button--primary) {
    width: 100%;
    padding: 11px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 8px;
    background: #5eb3f6;
    border: none;
    transition: all 0.3s ease;
    
    &:hover {
      background: #4ea5e6;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(94, 179, 246, 0.4);
    }
  }
  
  :deep(.el-checkbox) {
    .el-checkbox__label {
      color: #64748b;
      font-size: 13px;
      
      .dark & {
        color: #cbd5e1;
      }
    }
  }
}

.login-divider {
  position: relative;
  text-align: center;
  margin: 12px 0;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    top: 50%;
    height: 1px;
    background: #e2e8f0;
    
    .dark & {
      background: #334155;
    }
  }
  
  span {
    position: relative;
    display: inline-block;
    padding: 0 12px;
    background: white;
    color: #94a3b8;
    font-size: 12px;
    
    .dark & {
      background: #1e293b;
      color: #64748b;
    }
  }
}

.register-btn {
  width: 100%;
  padding: 11px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
  background: white;
  border: 2px solid #5eb3f6;
  color: #5eb3f6;
  transition: all 0.3s ease;
  
  .dark & {
    background: #1e293b;
    border-color: #5eb3f6;
    color: #5eb3f6;
  }
  
  &:hover {
    background: #5eb3f6;
    color: white;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(94, 179, 246, 0.3);
  }
}

.social-login {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 12px;
}

.social-icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1px solid #e2e8f0;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s ease;
  
  .dark & {
    background: #0f172a;
    border-color: #475569;
    color: #cbd5e1;
  }
  
  &:hover {
    border-color: #5eb3f6;
    color: #5eb3f6;
    background: rgba(94, 179, 246, 0.05);
    transform: translateY(-2px);
    
    .dark & {
      background: rgba(94, 179, 246, 0.2);
      border-color: #5eb3f6;
    }
  }
}

.terms {
  margin-top: 12px;
  text-align: center;
  
  p {
    font-size: 11px;
    color: #94a3b8;
    margin: 0;
    line-height: 1.5;
    
    .dark & {
      color: #94a3b8;
    }
    
    a {
      color: #5eb3f6;
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 统一所有表单的输入框样式（与登录表单保持一致）
:deep(.modern-login-form) {
  // 增加表单项间距，避免验证错误提示被遮挡
  .el-form-item {
    margin-bottom: 24px;
  }
  
  .el-input__wrapper {
    border-radius: 12px;
    padding: 8px 12px;
    min-height: 40px;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    }
    
    &.is-focus {
      box-shadow: 0 0 0 2px rgba(var(--el-color-primary-rgb), 0.2);
    }
  }
  
  .el-input__inner {
    height: auto;
    line-height: 1.5;
  }
  
  .el-button {
    border-radius: 12px;
    font-weight: 600;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.4);
    }
  }
  
  .el-divider {
    margin: 24px 0;
  }
  
  .el-link {
    transition: all 0.3s ease;
    &:hover {
      transform: translateX(2px);
    }
  }
  
  // 确保错误提示文本可见
  .el-form-item__error {
    position: relative;
    top: 2px;
    padding-top: 2px;
    line-height: 1.4;
  }
}
</style>
