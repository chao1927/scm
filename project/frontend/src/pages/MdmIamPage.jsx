import { useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Button, Col, InputNumber, Row, Statistic, Table, Tabs, Tag, Typography } from 'antd'
import { DatabaseOutlined, ReloadOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons'
import {
  queryIamApps,
  queryIamApprovals,
  queryIamOperationLogs,
  queryIamPermissions,
  queryIamRoles,
  queryIamSecurityPolicies,
  queryIamSsoClients,
  queryIamUsers,
  queryMdmCodeRules,
  queryMdmImportTasks,
  queryMdmPublications,
  queryMdmQualityIssues,
  queryMdmRecords,
  queryMdmSubscriptions,
  queryMdmTemplates,
  queryMdmTypes,
} from '../api/mdmIam'

const statusMap = {
  1: ['启用/草稿/待处理', 'blue'],
  2: ['停用/已发布/已完成', 'green'],
  3: ['失败/已驳回', 'red'],
  4: ['处理中', 'processing'],
}

export default function MdmIamPage() {
  const [limit, setLimit] = useState(50)
  const queryClient = useQueryClient()

  const mdmTypesQuery = useQuery({ queryKey: ['mdm-types'], queryFn: queryMdmTypes })
  const mdmTemplatesQuery = useQuery({ queryKey: ['mdm-templates'], queryFn: queryMdmTemplates })
  const mdmCodeRulesQuery = useQuery({ queryKey: ['mdm-code-rules'], queryFn: queryMdmCodeRules })
  const mdmRecordsQuery = useQuery({ queryKey: ['mdm-records'], queryFn: () => queryMdmRecords({ pageNo: 1, pageSize: limit }) })
  const mdmImportTasksQuery = useQuery({ queryKey: ['mdm-import-tasks'], queryFn: () => queryMdmImportTasks() })
  const mdmIssuesQuery = useQuery({ queryKey: ['mdm-quality-issues'], queryFn: () => queryMdmQualityIssues() })
  const mdmSubscriptionsQuery = useQuery({ queryKey: ['mdm-subscriptions'], queryFn: queryMdmSubscriptions })
  const mdmPublicationsQuery = useQuery({ queryKey: ['mdm-publications'], queryFn: queryMdmPublications })

  const iamUsersQuery = useQuery({ queryKey: ['iam-users', limit], queryFn: () => queryIamUsers({ limit }) })
  const iamRolesQuery = useQuery({ queryKey: ['iam-roles', limit], queryFn: () => queryIamRoles({ limit }) })
  const iamPermissionsQuery = useQuery({ queryKey: ['iam-permissions', limit], queryFn: () => queryIamPermissions({ limit }) })
  const iamApprovalsQuery = useQuery({ queryKey: ['iam-approvals', limit], queryFn: () => queryIamApprovals({ limit }) })
  const iamOperationLogsQuery = useQuery({ queryKey: ['iam-operation-logs', limit], queryFn: () => queryIamOperationLogs({ limit }) })
  const iamPoliciesQuery = useQuery({ queryKey: ['iam-policies', limit], queryFn: () => queryIamSecurityPolicies({ limit }) })
  const iamAppsQuery = useQuery({ queryKey: ['iam-apps'], queryFn: queryIamApps })
  const iamSsoQuery = useQuery({ queryKey: ['iam-sso'], queryFn: queryIamSsoClients })

  const refresh = () => queryClient.invalidateQueries()

  const mdmTypeColumns = useMemo(() => [
    { title: '类型编码', dataIndex: 'typeCode', width: 160, fixed: 'left' },
    { title: '类型名称', dataIndex: 'typeName', width: 180 },
    { title: '领域', dataIndex: 'domainCode', width: 120 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
    { title: '版本', dataIndex: 'version', width: 80 },
  ], [])

  const templateColumns = useMemo(() => [
    { title: '模板编码', dataIndex: 'templateCode', width: 180, fixed: 'left' },
    { title: '类型编码', dataIndex: 'typeCode', width: 160 },
    { title: '字段定义', dataIndex: 'fieldPayload', width: 360, ellipsis: true },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
    { title: '版本', dataIndex: 'version', width: 80 },
  ], [])

  const codeRuleColumns = useMemo(() => [
    { title: '规则编码', dataIndex: 'ruleCode', width: 180, fixed: 'left' },
    { title: '类型编码', dataIndex: 'typeCode', width: 160 },
    { title: '前缀', dataIndex: 'prefix', width: 100 },
    { title: '流水长度', dataIndex: 'serialLength', width: 100 },
    { title: '当前流水', dataIndex: 'currentSerial', width: 110 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
  ], [])

  const recordColumns = useMemo(() => [
    { title: '记录号', dataIndex: 'recordNo', width: 190, fixed: 'left' },
    { title: '类型', dataIndex: 'typeCode', width: 130 },
    { title: '数据编码', dataIndex: 'dataCode', width: 160 },
    { title: '名称', dataIndex: 'dataName', width: 180 },
    { title: '版本号', dataIndex: 'versionNo', width: 120 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
    { title: '载荷', dataIndex: 'dataPayload', width: 320, ellipsis: true },
  ], [])

  const importColumns = useMemo(() => [
    { title: '导入任务', dataIndex: 'importTaskNo', width: 190, fixed: 'left' },
    { title: '类型', dataIndex: 'typeCode', width: 130 },
    { title: '文件名', dataIndex: 'fileName', width: 220, ellipsis: true },
    { title: '总数', dataIndex: 'totalCount', width: 90 },
    { title: '成功', dataIndex: 'successCount', width: 90 },
    { title: '失败', dataIndex: 'errorCount', width: 90 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
  ], [])

  const issueColumns = useMemo(() => [
    { title: '问题号', dataIndex: 'issueNo', width: 190, fixed: 'left' },
    { title: '类型', dataIndex: 'typeCode', width: 130 },
    { title: '数据编码', dataIndex: 'dataCode', width: 160 },
    { title: '问题类型', dataIndex: 'issueType', width: 140 },
    { title: '严重级别', dataIndex: 'severity', width: 100 },
    { title: '处理人', dataIndex: 'assigneeId', width: 100 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
    { title: '描述', dataIndex: 'description', width: 280, ellipsis: true },
  ], [])

  const subscriptionColumns = useMemo(() => [
    { title: '订阅号', dataIndex: 'subscriptionNo', width: 190, fixed: 'left' },
    { title: '类型', dataIndex: 'typeCode', width: 130 },
    { title: '目标系统', dataIndex: 'targetSystem', width: 140 },
    { title: 'Topic', dataIndex: 'eventTopic', width: 180 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
  ], [])

  const publicationColumns = useMemo(() => [
    { title: '发布号', dataIndex: 'publicationNo', width: 190, fixed: 'left' },
    { title: '版本号', dataIndex: 'versionNo', width: 160 },
    { title: '类型', dataIndex: 'typeCode', width: 130 },
    { title: '数据编码', dataIndex: 'dataCode', width: 160 },
    { title: '目标系统', dataIndex: 'targetSystem', width: 140 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
    { title: '失败原因', dataIndex: 'failureReason', width: 260, ellipsis: true },
  ], [])

  const userColumns = useMemo(() => [
    { title: '用户 ID', dataIndex: 'id', width: 100, fixed: 'left' },
    { title: '用户名', dataIndex: 'username', width: 180 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
    { title: '失败次数', dataIndex: 'failedAttempts', width: 100 },
    { title: '版本', dataIndex: 'version', width: 80 },
  ], [])

  const roleColumns = useMemo(() => [
    { title: '角色 ID', dataIndex: 'id', width: 100, fixed: 'left' },
    { title: '角色编码', dataIndex: 'roleCode', width: 180 },
    { title: '角色名称', dataIndex: 'roleName', width: 180 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
    { title: '版本', dataIndex: 'version', width: 80 },
  ], [])

  const permissionColumns = useMemo(() => [
    { title: '权限 ID', dataIndex: 'id', width: 100, fixed: 'left' },
    { title: '应用', dataIndex: 'appCode', width: 120 },
    { title: '权限编码', dataIndex: 'permissionCode', width: 220 },
    { title: '权限名称', dataIndex: 'permissionName', width: 220 },
  ], [])

  const approvalColumns = useMemo(() => [
    { title: '审批号', dataIndex: 'approvalNo', width: 190, fixed: 'left' },
    { title: '业务类型', dataIndex: 'businessType', width: 160 },
    { title: '业务单号', dataIndex: 'businessNo', width: 180 },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
    { title: '版本', dataIndex: 'version', width: 80 },
  ], [])

  const operationLogColumns = useMemo(() => [
    { title: '日志 ID', dataIndex: 'id', width: 100, fixed: 'left' },
    { title: '操作', dataIndex: 'operation', width: 220 },
    { title: '目标', dataIndex: 'targetNo', width: 220 },
  ], [])

  const policyColumns = useMemo(() => [
    { title: '策略 ID', dataIndex: 'id', width: 100, fixed: 'left' },
    { title: '策略编码', dataIndex: 'policyCode', width: 220 },
    { title: '策略值', dataIndex: 'policyValue', width: 260 },
    { title: '版本', dataIndex: 'version', width: 80 },
  ], [])

  const appColumns = useMemo(() => [
    { title: '应用编码', dataIndex: 'appCode', width: 180, fixed: 'left' },
    { title: '应用名称', dataIndex: 'appName', width: 180 },
    { title: '首页', dataIndex: 'homeUrl', width: 260, ellipsis: true },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
  ], [])

  const ssoColumns = useMemo(() => [
    { title: 'SSO 编码', dataIndex: 'ssoCode', width: 180, fixed: 'left' },
    { title: '应用', dataIndex: 'appCode', width: 120 },
    { title: '回调地址', dataIndex: 'redirectUrl', width: 300, ellipsis: true },
    { title: '状态', dataIndex: 'status', width: 120, render: statusTag },
  ], [])

  return (
    <div className="mdm-iam-page">
      <div className="page-heading">
        <div>
          <Typography.Title level={3}>主数据与权限</Typography.Title>
          <Typography.Text type="secondary">查看主数据治理、发布订阅、用户角色权限和安全策略</Typography.Text>
        </div>
        <div className="heading-actions">
          <InputNumber min={1} max={200} value={limit} onChange={(value) => setLimit(value || 50)} addonBefore="查询上限" />
          <Button icon={<ReloadOutlined />} onClick={refresh}>刷新</Button>
        </div>
      </div>

      <Row gutter={[16, 16]} className="metric-row">
        <Col xs={12} xl={6}><Metric icon={<DatabaseOutlined />} title="主数据类型" value={listOf(mdmTypesQuery.data).length} /></Col>
        <Col xs={12} xl={6}><Metric icon={<DatabaseOutlined />} title="主数据记录" value={listOf(mdmRecordsQuery.data).length} /></Col>
        <Col xs={12} xl={6}><Metric icon={<UserOutlined />} title="用户" value={listOf(iamUsersQuery.data).length} /></Col>
        <Col xs={12} xl={6}><Metric icon={<SafetyCertificateOutlined />} title="权限点" value={listOf(iamPermissionsQuery.data).length} /></Col>
      </Row>

      <Tabs
        className="ops-tabs"
        items={[
          { key: 'mdmTypes', label: '类型', children: <OpsTable title="主数据类型" rowKey="typeCode" columns={mdmTypeColumns} query={mdmTypesQuery} scroll={{ x: 700 }} /> },
          { key: 'mdmTemplates', label: '模板', children: <OpsTable title="字段模板" rowKey="templateCode" columns={templateColumns} query={mdmTemplatesQuery} scroll={{ x: 980 }} /> },
          { key: 'mdmCodeRules', label: '编码规则', children: <OpsTable title="编码规则" rowKey="ruleCode" columns={codeRuleColumns} query={mdmCodeRulesQuery} scroll={{ x: 920 }} /> },
          { key: 'mdmRecords', label: '主数据记录', children: <OpsTable title="主数据记录" rowKey="recordNo" columns={recordColumns} query={mdmRecordsQuery} scroll={{ x: 1280 }} /> },
          { key: 'mdmImports', label: '导入任务', children: <OpsTable title="导入任务" rowKey="importTaskNo" columns={importColumns} query={mdmImportTasksQuery} scroll={{ x: 960 }} /> },
          { key: 'mdmIssues', label: '质量问题', children: <OpsTable title="数据质量问题" rowKey="issueNo" columns={issueColumns} query={mdmIssuesQuery} scroll={{ x: 1260 }} /> },
          { key: 'mdmSubscriptions', label: '发布订阅', children: <OpsTable title="发布订阅" rowKey="subscriptionNo" columns={subscriptionColumns} query={mdmSubscriptionsQuery} scroll={{ x: 820 }} /> },
          { key: 'mdmPublications', label: '发布记录', children: <OpsTable title="发布记录" rowKey="publicationNo" columns={publicationColumns} query={mdmPublicationsQuery} scroll={{ x: 1260 }} /> },
          { key: 'iamUsers', label: '用户', children: <OpsTable title="用户" rowKey="id" columns={userColumns} query={iamUsersQuery} scroll={{ x: 640 }} /> },
          { key: 'iamRoles', label: '角色', children: <OpsTable title="角色" rowKey="id" columns={roleColumns} query={iamRolesQuery} scroll={{ x: 720 }} /> },
          { key: 'iamPermissions', label: '权限点', children: <OpsTable title="权限点" rowKey="id" columns={permissionColumns} query={iamPermissionsQuery} scroll={{ x: 760 }} /> },
          { key: 'iamApprovals', label: '审批', children: <OpsTable title="审批实例" rowKey="approvalNo" columns={approvalColumns} query={iamApprovalsQuery} scroll={{ x: 760 }} /> },
          { key: 'iamPolicies', label: '安全策略', children: <OpsTable title="安全策略" rowKey="id" columns={policyColumns} query={iamPoliciesQuery} scroll={{ x: 700 }} /> },
          { key: 'iamApps', label: '应用', children: <OpsTable title="应用" rowKey="appCode" columns={appColumns} query={iamAppsQuery} scroll={{ x: 760 }} /> },
          { key: 'iamSso', label: 'SSO', children: <OpsTable title="SSO 客户端" rowKey="ssoCode" columns={ssoColumns} query={iamSsoQuery} scroll={{ x: 760 }} /> },
          { key: 'iamLogs', label: '操作日志', children: <OpsTable title="操作日志" rowKey="id" columns={operationLogColumns} query={iamOperationLogsQuery} scroll={{ x: 620 }} /> },
        ]}
      />
    </div>
  )
}

function Metric({ icon, title, value }) {
  return (
    <div className="metric-tile">
      <div className="metric-icon" aria-hidden="true">{icon}</div>
      <Statistic title={title} value={value} />
    </div>
  )
}

function OpsTable({ title, rowKey, columns, query, scroll }) {
  return (
    <div className="section-band">
      <div className="section-title">
        <Typography.Title level={4}>{title}</Typography.Title>
      </div>
      <Table rowKey={rowKey} columns={columns} dataSource={listOf(query.data)} loading={query.isLoading} scroll={scroll} pagination={{ pageSize: 8 }} />
    </div>
  )
}

function listOf(response) {
  if (Array.isArray(response)) {
    return response
  }
  if (Array.isArray(response?.data)) {
    return response.data
  }
  return []
}

function statusTag(value) {
  const item = statusMap[value] || [value, 'default']
  return <Tag color={item[1]}>{item[0]}</Tag>
}
