import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Alert,
  Button,
  Col,
  Descriptions,
  Drawer,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Row,
  Select,
  Space,
  Statistic,
  Table,
  Tabs,
  Tag,
  Tooltip,
  Typography,
} from 'antd'
import {
  ApiOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  CloudSyncOutlined,
  PlayCircleOutlined,
  PlusOutlined,
  ReloadOutlined,
  RetweetOutlined,
  StopOutlined,
  SyncOutlined,
} from '@ant-design/icons'
import {
  createEndpoint,
  createRoute,
  disableEndpoint,
  disableRoute,
  dispatchMessage,
  getIntegrationSummary,
  listDeadLetters,
  listDeliveryAttempts,
  listEndpoints,
  listMessages,
  listRoutes,
  replayDeadLetter,
  retryMessage,
  runDispatch,
  verifyEndpoint,
} from '../api/integration'

const routeStatus = {
  1: { text: '启用', color: 'green' },
  2: { text: '停用', color: 'default' },
}

const endpointStatus = {
  1: { text: '启用', color: 'green' },
  2: { text: '停用', color: 'default' },
}

const messageStatus = {
  1: { text: '待投递', color: 'blue' },
  2: { text: '已投递', color: 'green' },
  3: { text: '失败', color: 'gold' },
  4: { text: '死信', color: 'red' },
  5: { text: '已重放', color: 'purple' },
}

const channelOptions = [
  { value: 'HTTP', label: 'HTTP' },
  { value: 'OPENAPI', label: 'OpenAPI' },
  { value: 'ROCKETMQ', label: 'RocketMQ' },
  { value: 'DUBBO', label: 'Dubbo' },
  { value: 'LOCAL_ACK', label: 'Local Ack' },
  { value: 'LOCAL_FAIL', label: 'Local Fail' },
]

const messageStatusOptions = Object.entries(messageStatus).map(([value, item]) => ({
  value: Number(value),
  label: item.text,
}))

export default function IntegrationPage() {
  const [routeDrawerOpen, setRouteDrawerOpen] = useState(false)
  const [endpointDrawerOpen, setEndpointDrawerOpen] = useState(false)
  const [activeMessageStatus, setActiveMessageStatus] = useState()
  const [attemptMessageNo, setAttemptMessageNo] = useState('')
  const [dispatchLimit, setDispatchLimit] = useState(20)
  const [routeForm] = Form.useForm()
  const [endpointForm] = Form.useForm()
  const queryClient = useQueryClient()

  const summaryQuery = useQuery({ queryKey: ['integration-summary'], queryFn: getIntegrationSummary })
  const routesQuery = useQuery({ queryKey: ['integration-routes'], queryFn: listRoutes })
  const endpointsQuery = useQuery({ queryKey: ['integration-endpoints'], queryFn: listEndpoints })
  const messagesQuery = useQuery({
    queryKey: ['integration-messages', activeMessageStatus],
    queryFn: () => listMessages({ status: activeMessageStatus }),
  })
  const deadLettersQuery = useQuery({ queryKey: ['integration-dead-letters'], queryFn: listDeadLetters })
  const attemptsQuery = useQuery({
    queryKey: ['integration-delivery-attempts', attemptMessageNo],
    queryFn: () => listDeliveryAttempts({ messageNo: attemptMessageNo || undefined }),
  })

  const refreshAll = () => {
    queryClient.invalidateQueries({ queryKey: ['integration-summary'] })
    queryClient.invalidateQueries({ queryKey: ['integration-routes'] })
    queryClient.invalidateQueries({ queryKey: ['integration-endpoints'] })
    queryClient.invalidateQueries({ queryKey: ['integration-messages'] })
    queryClient.invalidateQueries({ queryKey: ['integration-dead-letters'] })
    queryClient.invalidateQueries({ queryKey: ['integration-delivery-attempts'] })
  }

  const createRouteMutation = useMutation({
    mutationFn: createRoute,
    onSuccess: () => {
      message.success('路由已创建')
      setRouteDrawerOpen(false)
      routeForm.resetFields()
      refreshAll()
    },
  })
  const disableRouteMutation = useMutation({
    mutationFn: ({ routeNo, version }) => disableRoute(routeNo, version),
    onSuccess: () => {
      message.success('路由已停用')
      refreshAll()
    },
  })
  const createEndpointMutation = useMutation({
    mutationFn: createEndpoint,
    onSuccess: () => {
      message.success('端点已创建')
      setEndpointDrawerOpen(false)
      endpointForm.resetFields()
      refreshAll()
    },
  })
  const disableEndpointMutation = useMutation({
    mutationFn: ({ endpointNo, version }) => disableEndpoint(endpointNo, version),
    onSuccess: () => {
      message.success('端点已停用')
      refreshAll()
    },
  })
  const verifyEndpointMutation = useMutation({
    mutationFn: verifyEndpoint,
    onSuccess: (result) => {
      const status = result.valid ? 'success' : 'warning'
      message.open({ type: status, content: result.message || '端点预检完成' })
    },
  })
  const dispatchMutation = useMutation({
    mutationFn: ({ messageNo, version, success, failureReason }) =>
      dispatchMessage(messageNo, { expectedVersion: version, success, failureReason }),
    onSuccess: () => {
      message.success('消息派发状态已更新')
      refreshAll()
    },
  })
  const retryMutation = useMutation({
    mutationFn: ({ messageNo, version }) => retryMessage(messageNo, version),
    onSuccess: () => {
      message.success('消息已重新进入待投递')
      refreshAll()
    },
  })
  const replayMutation = useMutation({
    mutationFn: replayDeadLetter,
    onSuccess: () => {
      message.success('死信已重放')
      refreshAll()
    },
  })
  const runDispatchMutation = useMutation({
    mutationFn: runDispatch,
    onSuccess: (result) => {
      message.success(`批量投递完成：成功 ${result.successCount}，失败 ${result.failedCount}`)
      refreshAll()
    },
  })

  const routeColumns = useMemo(() => [
    { title: '路由号', dataIndex: 'routeNo', width: 180, fixed: 'left' },
    { title: '消息类型', dataIndex: 'messageType', width: 220 },
    { title: '来源系统', dataIndex: 'sourceSystem', width: 130 },
    { title: '目标系统', dataIndex: 'targetSystem', width: 130 },
    { title: '通道', dataIndex: 'channelType', width: 120, render: (value) => <Tag color="blue">{value}</Tag> },
    { title: '状态', dataIndex: 'status', width: 100, render: (value) => <StatusTag mapping={routeStatus} value={value} /> },
    { title: '版本', dataIndex: 'version', width: 90 },
    {
      title: '操作',
      key: 'actions',
      width: 110,
      fixed: 'right',
      render: (_, record) => record.status === 1 && (
        <Tooltip title="停用路由">
          <Button
            danger
            type="text"
            icon={<StopOutlined />}
            onClick={() => confirmDisableRoute(record, disableRouteMutation)}
          />
        </Tooltip>
      ),
    },
  ], [disableRouteMutation])

  const endpointColumns = useMemo(() => [
    { title: '端点号', dataIndex: 'endpointNo', width: 180, fixed: 'left' },
    { title: '目标系统', dataIndex: 'targetSystem', width: 130 },
    { title: '通道', dataIndex: 'channelType', width: 120, render: (value) => <Tag color="blue">{value}</Tag> },
    { title: '端点地址', dataIndex: 'endpointUrl', width: 300, ellipsis: true },
    { title: '超时', dataIndex: 'timeoutMillis', width: 100, render: (value) => `${value}ms` },
    { title: '失败阈值', dataIndex: 'failureThreshold', width: 100 },
    { title: '连续失败', dataIndex: 'consecutiveFailures', width: 100 },
    { title: '状态', dataIndex: 'status', width: 100, render: (value) => <StatusTag mapping={endpointStatus} value={value} /> },
    {
      title: '操作',
      key: 'actions',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Tooltip title="契约预检">
            <Button type="text" icon={<CheckCircleOutlined />} onClick={() => verifyEndpointMutation.mutate(record.endpointNo)} />
          </Tooltip>
          {record.status === 1 && (
            <Tooltip title="停用端点">
              <Button
                danger
                type="text"
                icon={<StopOutlined />}
                onClick={() => confirmDisableEndpoint(record, disableEndpointMutation)}
              />
            </Tooltip>
          )}
        </Space>
      ),
    },
  ], [disableEndpointMutation, verifyEndpointMutation])

  const messageColumns = useMemo(() => [
    { title: '消息号', dataIndex: 'messageNo', width: 190, fixed: 'left' },
    { title: '消息类型', dataIndex: 'messageType', width: 220 },
    { title: '来源', dataIndex: 'sourceSystem', width: 110 },
    { title: '目标', dataIndex: 'targetSystem', width: 110 },
    { title: '业务单号', dataIndex: 'businessNo', width: 160 },
    { title: '状态', dataIndex: 'status', width: 100, render: (value) => <StatusTag mapping={messageStatus} value={value} /> },
    { title: '重试', dataIndex: 'retryCount', width: 80 },
    { title: '失败原因', dataIndex: 'failureReason', width: 240, ellipsis: true },
    {
      title: '操作',
      key: 'actions',
      width: 170,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          {[1, 3].includes(record.status) && (
            <Tooltip title="标记派发成功">
              <Button type="text" icon={<PlayCircleOutlined />} onClick={() => confirmDispatch(record, dispatchMutation)} />
            </Tooltip>
          )}
          {record.status === 3 && (
            <Tooltip title="重新待投递">
              <Button type="text" icon={<RetweetOutlined />} onClick={() => retryMutation.mutate({ messageNo: record.messageNo, version: record.version })} />
            </Tooltip>
          )}
        </Space>
      ),
    },
  ], [dispatchMutation, retryMutation])

  const deadLetterColumns = useMemo(() => [
    { title: '死信号', dataIndex: 'deadLetterNo', width: 190, fixed: 'left' },
    { title: '消息号', dataIndex: 'messageNo', width: 190 },
    { title: '消息类型', dataIndex: 'messageType', width: 220 },
    { title: '来源', dataIndex: 'sourceSystem', width: 110 },
    { title: '目标', dataIndex: 'targetSystem', width: 110 },
    { title: '业务单号', dataIndex: 'businessNo', width: 160 },
    { title: '失败原因', dataIndex: 'failureReason', width: 260, ellipsis: true },
    { title: '已重放', dataIndex: 'replayed', width: 100, render: (value) => value ? <Tag color="purple">是</Tag> : <Tag color="red">否</Tag> },
    {
      title: '操作',
      key: 'actions',
      width: 100,
      fixed: 'right',
      render: (_, record) => !record.replayed && (
        <Tooltip title="重放死信">
          <Button type="text" icon={<CloudSyncOutlined />} onClick={() => replayMutation.mutate(record.deadLetterNo)} />
        </Tooltip>
      ),
    },
  ], [replayMutation])

  const attemptColumns = useMemo(() => [
    { title: '尝试号', dataIndex: 'attemptNo', width: 190, fixed: 'left' },
    { title: '消息号', dataIndex: 'messageNo', width: 190 },
    { title: '消息类型', dataIndex: 'messageType', width: 220 },
    { title: '来源', dataIndex: 'sourceSystem', width: 110 },
    { title: '目标', dataIndex: 'targetSystem', width: 110 },
    { title: '通道', dataIndex: 'channelType', width: 120, render: (value) => <Tag color="blue">{value}</Tag> },
    { title: '结果', dataIndex: 'success', width: 100, render: (value) => value ? <Tag icon={<CheckCircleOutlined />} color="green">成功</Tag> : <Tag icon={<CloseCircleOutlined />} color="red">失败</Tag> },
    { title: '耗时', dataIndex: 'durationMillis', width: 100, render: (value) => `${value}ms` },
    { title: '失败原因', dataIndex: 'failureReason', width: 260, ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 180, render: formatDateTime },
  ], [])

  const summary = summaryQuery.data || {}

  return (
    <div className="integration-page">
      <div className="page-heading">
        <div>
          <Typography.Title level={3}>集成中心运维</Typography.Title>
          <Typography.Text type="secondary">管理跨系统路由、端点预检、消息派发、失败补偿和死信重放</Typography.Text>
        </div>
        <Space wrap>
          <Button icon={<ReloadOutlined />} onClick={refreshAll}>刷新</Button>
          <Button type="primary" icon={<PlayCircleOutlined />} loading={runDispatchMutation.isPending} onClick={() => runDispatchMutation.mutate(dispatchLimit)}>执行投递</Button>
        </Space>
      </div>

      <Alert
        className="ops-alert"
        type="warning"
        showIcon
        message="真实 RocketMQ、Dubbo、ERP、税务、支付联调仍依赖外部环境，本页先完成已具备后端能力的运维闭环。"
      />

      <Row gutter={[16, 16]} className="metric-row">
        <Col xs={12} lg={6}><MetricStat title="待投递" value={summary.pendingCount || 0} icon={<SyncOutlined />} /></Col>
        <Col xs={12} lg={6}><MetricStat title="已投递" value={summary.dispatchedCount || 0} icon={<CheckCircleOutlined />} /></Col>
        <Col xs={12} lg={6}><MetricStat title="失败" value={summary.failedCount || 0} icon={<CloseCircleOutlined />} /></Col>
        <Col xs={12} lg={6}><MetricStat title="死信/重放" value={`${summary.deadLetterCount || 0}/${summary.replayedCount || 0}`} icon={<CloudSyncOutlined />} /></Col>
      </Row>

      <div className="section-band">
        <div className="ops-toolbar">
          <Space wrap>
            <InputNumber min={1} max={200} value={dispatchLimit} onChange={(value) => setDispatchLimit(value || 20)} addonBefore="批量上限" />
            <Button icon={<PlayCircleOutlined />} loading={runDispatchMutation.isPending} onClick={() => runDispatchMutation.mutate(dispatchLimit)}>运行投递扫描</Button>
          </Space>
          {runDispatchMutation.data && (
            <Descriptions size="small" column={4} className="dispatch-result">
              <Descriptions.Item label="扫描">{runDispatchMutation.data.scannedCount}</Descriptions.Item>
              <Descriptions.Item label="成功">{runDispatchMutation.data.successCount}</Descriptions.Item>
              <Descriptions.Item label="失败">{runDispatchMutation.data.failedCount}</Descriptions.Item>
              <Descriptions.Item label="死信">{runDispatchMutation.data.deadLetterCount}</Descriptions.Item>
            </Descriptions>
          )}
        </div>
      </div>

      <Tabs
        className="ops-tabs"
        items={[
          {
            key: 'routes',
            label: '路由',
            children: (
              <OpsTableSection title="路由管理" action={<Button type="primary" icon={<PlusOutlined />} onClick={() => setRouteDrawerOpen(true)}>新建路由</Button>}>
                <Table rowKey="routeNo" columns={routeColumns} dataSource={routesQuery.data || []} loading={routesQuery.isLoading} scroll={{ x: 1100 }} pagination={{ pageSize: 8 }} />
              </OpsTableSection>
            ),
          },
          {
            key: 'endpoints',
            label: '端点',
            children: (
              <OpsTableSection title="端点管理" action={<Button type="primary" icon={<PlusOutlined />} onClick={() => setEndpointDrawerOpen(true)}>新建端点</Button>}>
                <Table rowKey="endpointNo" columns={endpointColumns} dataSource={endpointsQuery.data || []} loading={endpointsQuery.isLoading} scroll={{ x: 1280 }} pagination={{ pageSize: 8 }} />
              </OpsTableSection>
            ),
          },
          {
            key: 'messages',
            label: '消息',
            children: (
              <OpsTableSection
                title="消息运维"
                action={<Select allowClear placeholder="消息状态" options={messageStatusOptions} value={activeMessageStatus} style={{ width: 150 }} onChange={setActiveMessageStatus} />}
              >
                <Table rowKey="messageNo" columns={messageColumns} dataSource={messagesQuery.data || []} loading={messagesQuery.isLoading} scroll={{ x: 1460 }} pagination={{ pageSize: 8 }} />
              </OpsTableSection>
            ),
          },
          {
            key: 'deadLetters',
            label: '死信',
            children: (
              <OpsTableSection title="死信处理">
                <Table rowKey="deadLetterNo" columns={deadLetterColumns} dataSource={deadLettersQuery.data || []} loading={deadLettersQuery.isLoading} scroll={{ x: 1420 }} pagination={{ pageSize: 8 }} />
              </OpsTableSection>
            ),
          },
          {
            key: 'attempts',
            label: '尝试日志',
            children: (
              <OpsTableSection
                title="投递尝试"
                action={<Input.Search allowClear placeholder="消息号" value={attemptMessageNo} style={{ width: 240 }} onChange={(event) => setAttemptMessageNo(event.target.value)} onSearch={setAttemptMessageNo} />}
              >
                <Table rowKey="attemptNo" columns={attemptColumns} dataSource={attemptsQuery.data || []} loading={attemptsQuery.isLoading} scroll={{ x: 1500 }} pagination={{ pageSize: 8 }} />
              </OpsTableSection>
            ),
          },
        ]}
      />

      <Drawer title="新建路由" width={520} open={routeDrawerOpen} onClose={() => setRouteDrawerOpen(false)} extra={<Button type="primary" loading={createRouteMutation.isPending} onClick={() => routeForm.submit()}>保存</Button>}>
        <Form form={routeForm} layout="vertical" onFinish={createRouteMutation.mutate} initialValues={{ channelType: 'HTTP' }}>
          <Form.Item name="messageType" label="消息类型" rules={[{ required: true }]}><Input placeholder="PurchaseOrderPublished" /></Form.Item>
          <Form.Item name="sourceSystem" label="来源系统" rules={[{ required: true }]}><Input placeholder="PURCHASE" /></Form.Item>
          <Form.Item name="targetSystem" label="目标系统" rules={[{ required: true }]}><Input placeholder="WMS" /></Form.Item>
          <Form.Item name="channelType" label="通道类型" rules={[{ required: true }]}><Select options={channelOptions} /></Form.Item>
        </Form>
      </Drawer>

      <Drawer title="新建端点" width={560} open={endpointDrawerOpen} onClose={() => setEndpointDrawerOpen(false)} extra={<Button type="primary" loading={createEndpointMutation.isPending} onClick={() => endpointForm.submit()}>保存</Button>}>
        <Form form={endpointForm} layout="vertical" onFinish={createEndpointMutation.mutate} initialValues={{ channelType: 'HTTP', timeoutMillis: 3000, failureThreshold: 3 }}>
          <Form.Item name="targetSystem" label="目标系统" rules={[{ required: true }]}><Input placeholder="WMS" /></Form.Item>
          <Form.Item name="channelType" label="通道类型" rules={[{ required: true }]}><Select options={channelOptions} /></Form.Item>
          <Form.Item name="endpointUrl" label="端点地址" rules={[{ required: true }]}><Input placeholder="https://example.com/openapi/events" /></Form.Item>
          <Form.Item name="timeoutMillis" label="超时时间 ms" rules={[{ required: true }]}><InputNumber min={100} max={30000} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="failureThreshold" label="熔断失败阈值" rules={[{ required: true }]}><InputNumber min={1} max={20} style={{ width: '100%' }} /></Form.Item>
        </Form>
      </Drawer>
    </div>
  )
}

function MetricStat({ title, value, icon }) {
  return (
    <div className="metric-tile">
      <div className="metric-icon" aria-hidden="true">{icon}</div>
      <Statistic title={title} value={value} />
    </div>
  )
}

function OpsTableSection({ title, action, children }) {
  return (
    <div className="section-band">
      <div className="section-title">
        <Typography.Title level={4}>{title}</Typography.Title>
        {action}
      </div>
      {children}
    </div>
  )
}

function StatusTag({ mapping, value }) {
  const item = mapping[value] || { text: value, color: 'default' }
  return <Tag color={item.color}>{item.text}</Tag>
}

function confirmDisableRoute(record, mutation) {
  Modal.confirm({
    title: '停用路由',
    content: `${record.routeNo} 停用后新消息不会再按该路由投递。`,
    onOk: () => mutation.mutate({ routeNo: record.routeNo, version: record.version }),
  })
}

function confirmDisableEndpoint(record, mutation) {
  Modal.confirm({
    title: '停用端点',
    content: `${record.endpointNo} 停用后对应目标系统不会再通过该端点投递。`,
    onOk: () => mutation.mutate({ endpointNo: record.endpointNo, version: record.version }),
  })
}

function confirmDispatch(record, mutation) {
  Modal.confirm({
    title: '标记派发成功',
    content: `确认将 ${record.messageNo} 标记为已派发。`,
    onOk: () => mutation.mutate({ messageNo: record.messageNo, version: record.version, success: true }),
  })
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString()
}
