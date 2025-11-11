/**
 * 三方登录模块国际化（中文）
 */
export default {
  social: {
    client: {
      id: '编号',
      name: '应用名',
      socialType: '社交平台',
      userType: '用户类型',
      clientId: '客户端编号',
      clientSecret: '客户端密钥',
      agentId: 'agentId',
      status: '状态',
      createTime: '创建时间',
      namePlaceholder: '请输入应用名',
      socialTypePlaceholder: '请选择社交平台',
      userTypePlaceholder: '请选择用户类型',
      clientIdPlaceholder: '请输入客户端编号,对应各平台的appKey',
      clientSecretPlaceholder: '请输入客户端密钥,对应各平台的appSecret',
      agentIdPlaceholder: '授权方的网页应用 ID，有则填',
      nameRequired: '应用名不能为空',
      socialTypeRequired: '社交平台不能为空',
      userTypeRequired: '用户类型不能为空',
      clientIdRequired: '客户端编号不能为空',
      clientSecretRequired: '客户端密钥不能为空',
      statusRequired: '状态不能为空'
    },
    user: {
      type: '社交平台',
      openid: '社交 openid',
      nickname: '用户昵称',
      avatar: '用户头像',
      token: '社交 token',
      rawTokenInfo: '原始 Token 数据',
      rawUserInfo: '原始 User 数据',
      code: '最后一次的认证 code',
      state: '最后一次的认证 state',
      createTime: '创建时间',
      updateTime: '更新时间',
      socialTypePlaceholder: '请选择社交平台',
      nicknamePlaceholder: '请输入用户昵称',
      openidPlaceholder: '请输入社交 openid'
    }
  }
}

