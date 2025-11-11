/**
 * 菜单管理模块国际化（中文）
 */
export default {
  menu: {
    name: '菜单名称',
    icon: '图标',
    sort: '排序',
    permission: '权限标识',
    component: '组件路径',
    componentName: '组件名称',
    status: '菜单状态',
    type: '菜单类型',
    path: '路由地址',
    visible: '显示状态',
    alwaysShow: '总是显示',
    keepAlive: '缓存状态',
    parentMenu: '上级菜单',
    namePlaceholder: '请输入菜单名称',
    statusPlaceholder: '请选择菜单状态',
    pathPlaceholder: '请输入路由地址',
    componentPlaceholder: '例如说：system/user/index',
    componentNamePlaceholder: '例如说：SystemUser',
    permissionPlaceholder: '请输入权限标识',
    expandCollapse: '展开/折叠',
    refreshCache: '刷新菜单缓存',
    refreshCacheConfirm: '即将更新缓存刷新浏览器！',
    pathTooltip: '访问的路由地址，如：`user`。如需外网地址时，则以 `http(s)://` 开头',
    permissionTooltip: 'Controller 方法上的权限字符，如：@PreAuthorize(`@ss.hasPermission(\'system:user:list\')`)',
    visibleTooltip: '选择隐藏时，路由将不会出现在侧边栏，但仍然可以访问',
    visibleShow: '显示',
    visibleHide: '隐藏',
    alwaysShowTooltip: '选择不是时，当该菜单只有一个子菜单时，不展示自己，直接展示子菜单',
    alwaysShowYes: '总是',
    alwaysShowNo: '不是',
    keepAliveTooltip: '选择缓存时，则会被 `keep-alive` 缓存，必须填写「组件名称」字段',
    keepAliveYes: '缓存',
    keepAliveNo: '不缓存',
    mainCategory: '主类目',
    nameRequired: '菜单名称不能为空',
    typeRequired: '菜单类型不能为空',
    sortRequired: '菜单顺序不能为空',
    pathRequired: '路由地址不能为空',
    statusRequired: '状态不能为空',
    pathMustStartWithSlash: '路径必须以 / 开头',
    pathCannotStartWithSlash: '路径不能以 / 开头'
  }
}

