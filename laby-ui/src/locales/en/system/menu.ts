/**
 * Menu Management Module Internationalization (English)
 */
export default {
  menu: {
    name: 'Menu Name',
    icon: 'Icon',
    sort: 'Sort',
    permission: 'Permission',
    component: 'Component Path',
    componentName: 'Component Name',
    status: 'Menu Status',
    type: 'Menu Type',
    path: 'Route Path',
    visible: 'Visible Status',
    alwaysShow: 'Always Show',
    keepAlive: 'Keep Alive',
    parentMenu: 'Parent Menu',
    namePlaceholder: 'Please enter menu name',
    statusPlaceholder: 'Please select menu status',
    pathPlaceholder: 'Please enter route path',
    componentPlaceholder: 'e.g.: system/user/index',
    componentNamePlaceholder: 'e.g.: SystemUser',
    permissionPlaceholder: 'Please enter permission',
    expandCollapse: 'Expand/Collapse',
    refreshCache: 'Refresh Menu Cache',
    refreshCacheConfirm: 'About to update cache and refresh browser!',
    pathTooltip: 'The route path to access, e.g.: `user`. For external URLs, it should start with `http(s)://`',
    permissionTooltip: 'The permission character on the Controller method, e.g.: @PreAuthorize(`@ss.hasPermission(\'system:user:list\')`)',
    visibleTooltip: 'When hidden is selected, the route will not appear in the sidebar, but can still be accessed',
    visibleShow: 'Show',
    visibleHide: 'Hide',
    alwaysShowTooltip: 'When "No" is selected, if the menu has only one submenu, it will not display itself and will directly display the submenu',
    alwaysShowYes: 'Yes',
    alwaysShowNo: 'No',
    keepAliveTooltip: 'When cache is selected, it will be cached by `keep-alive`, and the "Component Name" field must be filled in',
    keepAliveYes: 'Cache',
    keepAliveNo: 'No Cache',
    mainCategory: 'Main Category',
    nameRequired: 'Menu name cannot be empty',
    typeRequired: 'Menu type cannot be empty',
    sortRequired: 'Menu sort cannot be empty',
    pathRequired: 'Route path cannot be empty',
    statusRequired: 'Status cannot be empty',
    pathMustStartWithSlash: 'Path must start with /',
    pathCannotStartWithSlash: 'Path cannot start with /'
  }
}

