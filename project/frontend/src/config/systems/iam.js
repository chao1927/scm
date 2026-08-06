const page = (id, label, options = {}) => ({ id, label, ...options })

export const iamSystem = {
  id: 'iam',
  code: 'IAM',
  shortName: '权限',
  name: '权限中心',
  domain: '身份与访问上下文',
  permission: 'iam:*',
  goal: '统一身份、用户、角色、菜单、权限点、数据范围、会话和安全审计。',
  workflow: ['用户已创建', '用户已激活', '角色已分配', '权限缓存已刷新', '审计日志已记录'],
  pages: [
    page('workbench', '权限工作台'),
    page('users', '用户管理', { legacy: 'users' }),
    page('roles', '角色管理', { legacy: 'roles' }),
    page('menus', '菜单页面'),
    page('permissions', '权限点', { legacy: 'permissions' }),
    page('role-grants', '角色授权'),
    page('user-roles', '用户角色'),
    page('sessions', '会话管理'),
    page('mfa-configurations', 'MFA 配置'),
    page('mfa-challenges', 'MFA 挑战'),
    page('approvals', '审批实例', { legacy: 'approvals' }),
    page('security-policies', '安全策略', { legacy: 'securityPolicies' }),
    page('apps', '应用管理', { legacy: 'apps' }),
    page('sso-clients', 'SSO 客户端', { legacy: 'ssoClients' }),
    page('operation-logs', '操作日志', { legacy: 'operationLogs' }),
  ],
}
