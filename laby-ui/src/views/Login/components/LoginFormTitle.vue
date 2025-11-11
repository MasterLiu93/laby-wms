<template>
  <div class="enter-x mb-6 text-center">
    <h2 class="text-3xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent xl:text-4xl">
      {{ getFormTitle }}
    </h2>
    <div class="mt-2 text-sm text-gray-500">
      {{ getFormSubtitle }}
    </div>
  </div>
</template>
<script lang="ts" setup>
import { LoginStateEnum, useLoginState } from './useLogin'

defineOptions({ name: 'LoginFormTitle' })

const { t } = useI18n()

const { getLoginState } = useLoginState()

const getFormTitle = computed(() => {
  const titleObj = {
    [LoginStateEnum.RESET_PASSWORD]: t('sys.login.forgetFormTitle'),
    [LoginStateEnum.LOGIN]: t('sys.login.signInFormTitle'),
    [LoginStateEnum.REGISTER]: t('sys.login.signUpFormTitle'),
    [LoginStateEnum.MOBILE]: t('sys.login.mobileSignInFormTitle'),
    [LoginStateEnum.QR_CODE]: t('sys.login.qrSignInFormTitle'),
    [LoginStateEnum.SSO]: t('sys.login.ssoFormTitle')
  }
  return titleObj[unref(getLoginState)]
})

const getFormSubtitle = computed(() => {
  const subtitleObj = {
    [LoginStateEnum.RESET_PASSWORD]: t('sys.login.resetPasswordSubtitle'),
    [LoginStateEnum.LOGIN]: t('sys.login.loginSubtitle'),
    [LoginStateEnum.REGISTER]: t('sys.login.registerSubtitle'),
    [LoginStateEnum.MOBILE]: t('sys.login.mobileSubtitle'),
    [LoginStateEnum.QR_CODE]: t('sys.login.qrcodeSubtitle'),
    [LoginStateEnum.SSO]: t('sys.login.ssoSubtitle')
  }
  return subtitleObj[unref(getLoginState)] || ''
})
</script>
