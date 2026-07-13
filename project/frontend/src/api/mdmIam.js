import client from './client'

export const queryMdmTypes = () => client.get('/mdm/v1/master-data-types')
export const queryMdmTemplates = () => client.get('/mdm/v1/field-templates')
export const queryMdmCodeRules = () => client.get('/mdm/v1/code-rules')
export const queryMdmRecords = (params) => client.get('/mdm/v1/master-data-records', { params })
export const queryMdmImportTasks = (params) => client.get('/mdm/v1/import-tasks', { params })
export const queryMdmQualityIssues = (params) => client.get('/mdm/v1/data-quality-issues', { params })
export const queryMdmSubscriptions = () => client.get('/mdm/v1/publication-subscriptions')
export const queryMdmPublications = () => client.get('/mdm/v1/publications')

export const queryIamUsers = (params) => client.get('/iam/v1/users', { params })
export const queryIamRoles = (params) => client.get('/iam/v1/roles', { params })
export const queryIamPermissions = (params) => client.get('/iam/v1/permissions', { params })
export const queryIamApprovals = (params) => client.get('/iam/v1/approval-instances', { params })
export const queryIamOperationLogs = (params) => client.get('/iam/v1/operation-logs', { params })
export const queryIamSecurityPolicies = (params) => client.get('/iam/v1/security-policies', { params })
export const queryIamApps = () => client.get('/iam/v1/apps')
export const queryIamSsoClients = () => client.get('/iam/v1/sso-clients')
