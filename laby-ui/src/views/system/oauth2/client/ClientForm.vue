<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" max-height="500px" scroll>
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="160px"
    >
      <el-form-item :label="t('system.oauth2.client.clientId')" prop="clientId">
        <el-input v-model="formData.clientId" :placeholder="t('system.oauth2.client.clientIdPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.secret')" prop="secret">
        <el-input v-model="formData.secret" :placeholder="t('system.oauth2.client.secretPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.name')" prop="name">
        <el-input v-model="formData.name" :placeholder="t('system.oauth2.client.namePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.logo')">
        <UploadImg v-model="formData.logo" :limit="1" />
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.description')">
        <el-input v-model="formData.description" :placeholder="t('system.oauth2.client.descriptionPlaceholder')" type="textarea" />
      </el-form-item>
      <el-form-item :label="t('common.status')" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.accessTokenValiditySeconds')" prop="accessTokenValiditySeconds">
        <el-input-number v-model="formData.accessTokenValiditySeconds" :placeholder="t('system.oauth2.client.accessTokenValidityPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.refreshTokenValiditySeconds')" prop="refreshTokenValiditySeconds">
        <el-input-number v-model="formData.refreshTokenValiditySeconds" :placeholder="t('system.oauth2.client.refreshTokenValidityPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.authorizedGrantTypes')" prop="authorizedGrantTypes">
        <el-select
          v-model="formData.authorizedGrantTypes"
          filterable
          multiple
          :placeholder="t('system.oauth2.client.authorizedGrantTypesPlaceholder')"
          style="width: 500px"
        >
          <el-option
            v-for="dict in getDictOptions(DICT_TYPE.SYSTEM_OAUTH2_GRANT_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.scopes')" prop="scopes">
        <el-select
          v-model="formData.scopes"
          filterable
          multiple
          allow-create
          :placeholder="t('system.oauth2.client.scopesPlaceholder')"
          style="width: 500px"
        >
          <el-option v-for="scope in formData.scopes" :key="scope" :label="scope" :value="scope" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.autoApproveScopes')" prop="autoApproveScopes">
        <el-select
          v-model="formData.autoApproveScopes"
          filterable
          multiple
          :placeholder="t('system.oauth2.client.autoApproveScopesPlaceholder')"
          style="width: 500px"
        >
          <el-option v-for="scope in formData.scopes" :key="scope" :label="scope" :value="scope" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.redirectUris')" prop="redirectUris">
        <el-select
          v-model="formData.redirectUris"
          allow-create
          filterable
          multiple
          :placeholder="t('system.oauth2.client.redirectUrisPlaceholder')"
          style="width: 500px"
        >
          <el-option
            v-for="redirectUri in formData.redirectUris"
            :key="redirectUri"
            :label="redirectUri"
            :value="redirectUri"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.authorities')" prop="authorities">
        <el-select
          v-model="formData.authorities"
          allow-create
          filterable
          multiple
          :placeholder="t('system.oauth2.client.authoritiesPlaceholder')"
          style="width: 500px"
        >
          <el-option
            v-for="authority in formData.authorities"
            :key="authority"
            :label="authority"
            :value="authority"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.resourceIds')" prop="resourceIds">
        <el-select
          v-model="formData.resourceIds"
          allow-create
          filterable
          multiple
          :placeholder="t('system.oauth2.client.resourceIdsPlaceholder')"
          style="width: 500px"
        >
          <el-option
            v-for="resourceId in formData.resourceIds"
            :key="resourceId"
            :label="resourceId"
            :value="resourceId"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('system.oauth2.client.additionalInformation')" prop="additionalInformation">
        <el-input
          v-model="formData.additionalInformation"
          :placeholder="t('system.oauth2.client.additionalInformationPlaceholder')"
          type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">{{ t('common.ok') }}</el-button>
      <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { DICT_TYPE, getDictOptions, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import * as ClientApi from '@/api/system/oauth2/client'

defineOptions({ name: 'SystemOAuth2ClientForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  clientId: undefined,
  secret: undefined,
  name: undefined,
  logo: undefined,
  description: undefined,
  status: CommonStatusEnum.ENABLE,
  accessTokenValiditySeconds: 30 * 60,
  refreshTokenValiditySeconds: 30 * 24 * 60,
  redirectUris: [],
  authorizedGrantTypes: [],
  scopes: [],
  autoApproveScopes: [],
  authorities: [],
  resourceIds: [],
  additionalInformation: undefined
})
const formRules = reactive({
  clientId: [{ required: true, message: t('system.oauth2.client.clientIdRequired'), trigger: 'blur' }],
  secret: [{ required: true, message: t('system.oauth2.client.secretRequired'), trigger: 'blur' }],
  name: [{ required: true, message: t('system.oauth2.client.nameRequired'), trigger: 'blur' }],
  logo: [{ required: true, message: t('system.oauth2.client.logoRequired'), trigger: 'blur' }],
  status: [{ required: true, message: t('system.oauth2.client.statusRequired'), trigger: 'blur' }],
  accessTokenValiditySeconds: [
    { required: true, message: t('system.oauth2.client.accessTokenValidityRequired'), trigger: 'blur' }
  ],
  refreshTokenValiditySeconds: [
    { required: true, message: t('system.oauth2.client.refreshTokenValidityRequired'), trigger: 'blur' }
  ],
  redirectUris: [{ required: true, message: t('system.oauth2.client.redirectUrisRequired'), trigger: 'blur' }],
  authorizedGrantTypes: [{ required: true, message: t('system.oauth2.client.authorizedGrantTypesRequired'), trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ClientApi.getOAuth2Client(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as ClientApi.OAuth2ClientVO
    if (formType.value === 'create') {
      await ClientApi.createOAuth2Client(data)
      message.success(t('common.createSuccess'))
    } else {
      await ClientApi.updateOAuth2Client(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    clientId: undefined,
    secret: undefined,
    name: undefined,
    logo: undefined,
    description: undefined,
    status: CommonStatusEnum.ENABLE,
    accessTokenValiditySeconds: 30 * 60,
    refreshTokenValiditySeconds: 30 * 24 * 60,
    redirectUris: [],
    authorizedGrantTypes: [],
    scopes: [],
    autoApproveScopes: [],
    authorities: [],
    resourceIds: [],
    additionalInformation: undefined
  }
  formRef.value?.resetFields()
}
</script>
