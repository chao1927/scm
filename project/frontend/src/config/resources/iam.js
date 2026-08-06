import {
  queryIamApps,
  queryIamApprovals,
  queryIamOperationLogs,
  queryIamPermissions,
  queryIamRoles,
  queryIamSecurityPolicies,
  queryIamSsoClients,
  queryIamUsers,
  queryIamMenus,
  queryIamRoleGrants,
  queryIamUserRoles,
  queryIamSessions,
  queryIamMfaConfigurations,
  queryIamMfaChallenges,
} from '../../api/mdmIam'
import { simpleResource } from './helpers'

export const iamResources = {
  'iam.users': simpleResource(queryIamUsers, ['userNo', 'id'], [['username', '用户名'], ['displayName', '姓名'], ['organizationName', '组织'], ['mobile', '手机号'], ['statusName', '状态', 'status'], ['lastLoginAt', '最近登录', 'date']]),
  'iam.roles': simpleResource(queryIamRoles, ['roleCode', 'id'], [['roleCode', '角色编码'], ['roleName', '角色名称'], ['appCode', '应用'], ['userCount', '用户数量'], ['statusName', '状态', 'status']]),
  'iam.permissions': simpleResource(queryIamPermissions, ['permissionCode', 'id'], [['permissionCode', '权限编码'], ['permissionName', '权限名称'], ['resourceType', '资源类型'], ['appCode', '应用'], ['statusName', '状态', 'status']]),
  'iam.approvals': simpleResource(queryIamApprovals, ['approvalNo', 'id'], [['approvalNo', '审批编号'], ['businessType', '业务类型'], ['applicantName', '申请人'], ['currentNodeName', '当前节点'], ['statusName', '状态', 'status'], ['createdAt', '申请时间', 'date']]),
  'iam.operation-logs': simpleResource(queryIamOperationLogs, ['logNo', 'id'], [['operatorName', '操作人'], ['operationType', '操作类型'], ['resourceName', '资源'], ['resultName', '结果'], ['ipAddress', 'IP 地址'], ['occurredAt', '操作时间', 'date']]),
  'iam.security-policies': simpleResource(queryIamSecurityPolicies, ['policyCode', 'id'], [['policyCode', '策略编码'], ['policyName', '策略名称'], ['policyType', '策略类型'], ['statusName', '状态', 'status'], ['updatedAt', '更新时间', 'date']]),
  'iam.apps': simpleResource(queryIamApps, ['appCode', 'id'], [['appCode', '应用编码'], ['appName', '应用名称'], ['clientType', '客户端类型'], ['statusName', '状态', 'status']]),
  'iam.sso-clients': simpleResource(queryIamSsoClients, ['clientId', 'id'], [['clientId', '客户端 ID'], ['clientName', '客户端名称'], ['grantTypes', '授权类型'], ['redirectUris', '回调地址'], ['statusName', '状态', 'status']]),
  'iam.menus': simpleResource(queryIamMenus, ['menuCode', 'id'], [['menuCode', '菜单编码'], ['menuName', '菜单名称'], ['appCode', '应用'], ['parentCode', '上级菜单'], ['path', '路由'], ['status', '状态', 'status']]),
  'iam.role-grants': simpleResource(queryIamRoleGrants, ['roleId', 'permissionCode'], [['roleCode', '角色编码'], ['roleName', '角色名称'], ['permissionCode', '权限编码'], ['permissionName', '权限名称']]),
  'iam.user-roles': simpleResource(queryIamUserRoles, ['userId', 'roleId'], [['username', '用户名'], ['roleCode', '角色编码'], ['roleName', '角色名称']]),
  'iam.sessions': simpleResource(queryIamSessions, ['sessionId'], [['sessionId', '会话 ID'], ['userId', '用户 ID'], ['generation', '刷新代次', 'number'], ['status', '状态', 'status'], ['accessExpiresAt', '访问令牌到期', 'date'], ['refreshExpiresAt', '刷新令牌到期', 'date'], ['revocationReason', '撤销原因']]),
  'iam.mfa-configurations': simpleResource(queryIamMfaConfigurations, ['configId', 'userId'], [['userId', '用户 ID'], ['status', 'MFA 状态', 'status'], ['version', '版本', 'number']]),
  'iam.mfa-challenges': simpleResource(queryIamMfaChallenges, ['challengeNo'], [['challengeNo', '挑战编号'], ['userId', '用户 ID'], ['appCode', '应用'], ['purpose', '用途'], ['status', '状态', 'status'], ['failedAttempts', '失败次数', 'number'], ['expiresAt', '到期时间', 'date']]),
}
