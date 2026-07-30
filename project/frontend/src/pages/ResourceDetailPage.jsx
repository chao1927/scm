import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Descriptions, Empty, message, Space, Table, Tabs, Timeline, Typography } from 'antd'
import { useMemo, useState } from 'react'
import { useNavigate, useOutletContext, useParams, useSearchParams } from 'react-router-dom'
import { hasPermission } from '../auth/menuAccess'
import { BusinessPageHeader, CapabilityPending, formatCellValue, LoadingBlock, QueryError, StatusTag } from '../components/business/BusinessPrimitives'
import CommandDialog from '../components/business/CommandDialog'
import { findResourceDefinition, normalizePage, recordIdentity } from '../config/resourceDefinitions'
import { findSystem, findSystemPage } from '../config/systemCatalog'

const technicalFields = new Set(['password', 'secret', 'token', 'rawPayload'])

export default function ResourceDetailPage() {
  const { systemId, pageId, recordId } = useParams()
  const system = findSystem(systemId)
  const page = findSystemPage(systemId, pageId)
  const definition = findResourceDefinition(systemId, pageId)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [searchParams, setSearchParams] = useSearchParams()
  const [commandState, setCommandState] = useState({ action: null, record: null })
  const { session } = useOutletContext()
  const tab = searchParams.get('tab') || 'basic'

  const query = useQuery({
    queryKey: ['business-detail', systemId, pageId, recordId],
    queryFn: async () => {
      if (definition.detail) return definition.detail(recordId)
      const result = await definition.query({ keyword: recordId, pageNo: 1, pageSize: 100 })
      const records = normalizePage(result).records
      return { data: records.find((item) => recordIdentity(definition, item) === recordId) || null }
    },
    enabled: Boolean(definition?.query),
  })

  const record = query.data?.data ?? query.data
  const actionMutation = useMutation({
    mutationFn: ({ action, reason }) => action.run(record, reason),
    onSuccess: (_, variables) => {
      message.success(`${variables.action.label}已完成`)
      setCommandState({ action: null, record: null })
      queryClient.invalidateQueries({ queryKey: ['business-detail', systemId, pageId, recordId] })
      queryClient.invalidateQueries({ queryKey: ['business-resource', systemId, pageId] })
    },
    onError: (error) => message.error(error?.message || '操作失败'),
  })

  const detailItems = useMemo(() => record ? Object.entries(record)
    .filter(([key, value]) => !technicalFields.has(key) && !Array.isArray(value) && typeof value !== 'object')
    .map(([key, value]) => ({ key, label: fieldLabel(key, definition), children: key.toLowerCase().includes('status') ? <StatusTag value={value} /> : formatCellValue(value, { date: /At$|Date$/.test(key), number: typeof value === 'number' }) })) : [], [definition, record])

  if (!definition) return <CapabilityPending system={system} page={page} />
  if (query.isLoading) return <LoadingBlock rows={10} />
  if (query.isError) return <QueryError error={query.error} onRetry={() => query.refetch()} />
  if (!record) return <Empty description="未找到该业务对象" />

  const lineCollections = Object.entries(record).filter(([, value]) => Array.isArray(value) && value.length > 0)
  const relations = Object.entries(record).filter(([key, value]) => value && /(No|Id)$/.test(key) && !['id', 'version'].includes(key))
  const visibleActions = (definition.actions || [])
    .filter((action) => hasPermission(session.permissions, action.permission))
    .filter((action) => !action.visible || action.visible(record))

  const tabItems = [
    {
      key: 'basic',
      label: '基本信息',
      children: <section className="detail-section"><Typography.Title level={4}>基本信息</Typography.Title><Descriptions bordered column={{ xs: 1, md: 2 }} items={detailItems} /></section>,
    },
    {
      key: 'lines',
      label: '明细信息',
      children: <section className="detail-section"><Typography.Title level={4}>明细信息</Typography.Title><LineCollections collections={lineCollections} /></section>,
    },
    {
      key: 'fulfillment',
      label: '履约跟踪',
      children: <section className="detail-section"><Typography.Title level={4}>履约跟踪</Typography.Title><Lifecycle system={system} record={record} /></section>,
    },
    {
      key: 'relations',
      label: '关联单据',
      children: <section className="detail-section"><Typography.Title level={4}>关联单据</Typography.Title>{relations.length ? <Descriptions bordered column={1} items={relations.map(([key, value]) => ({ key, label: fieldLabel(key, definition), children: formatCellValue(value) }))} /> : <Empty description="后端详情暂未返回关联单据" />}</section>,
    },
    {
      key: 'process',
      label: '流程记录',
      children: <section className="detail-section"><Typography.Title level={4}>生命周期与流程</Typography.Title><Lifecycle system={system} record={record} /></section>,
    },
    {
      key: 'logs',
      label: '操作日志',
      children: <section className="detail-section"><Typography.Title level={4}>操作日志</Typography.Title><Empty description="当前详情接口未返回操作日志；需接入对应审计查询接口" /></section>,
    },
    {
      key: 'events',
      label: '领域事件',
      children: <section className="detail-section"><Typography.Title level={4}>领域事件</Typography.Title><Alert showIcon type="info" message="领域事件为技术支持视图" description="当前业务详情接口不伪造事件记录。接入 Outbox/Inbox 查询接口后，将展示 eventId、事件类型、发生时间、投递及幂等消费状态。" /></section>,
    },
  ]

  return (
    <div className="business-detail-page">
      <Button type="link" className="back-to-list" icon={<ArrowLeftOutlined />} onClick={() => navigate(`/${systemId}/${pageId}`)}>返回{page.label}列表</Button>
      <div className="detail-header-panel">
        <BusinessPageHeader
          eyebrow={`${system.domain} · ${page.label}`}
          title={recordTitle(record, definition, recordId)}
          description={`业务编号：${recordIdentity(definition, record) || recordId}`}
          actions={<>
            {visibleActions.slice(0, 4).map((action) => <Button key={action.key} type={action.tone === 'primary' ? 'primary' : 'default'} danger={action.danger} onClick={() => setCommandState({ action, record: { ...record, __identity: recordIdentity(definition, record) } })}>{action.label}</Button>)}
            <Button icon={<ReloadOutlined />} onClick={() => query.refetch()}>刷新</Button>
          </>}
        />
      </div>
      <Tabs className="business-detail-tabs" activeKey={tab} items={tabItems} onChange={(nextTab) => setSearchParams({ tab: nextTab })} />
      <CommandDialog
        open={Boolean(commandState.action)}
        action={commandState.action}
        record={commandState.record}
        loading={actionMutation.isPending}
        onCancel={() => setCommandState({ action: null, record: null })}
        onConfirm={(reason) => actionMutation.mutate({ ...commandState, reason })}
      />
    </div>
  )
}

function LineCollections({ collections }) {
  if (!collections.length) return <Empty description="当前详情接口没有返回业务明细" />
  return <Space orientation="vertical" size={20} className="detail-collections">{collections.map(([name, rows]) => {
    const sample = rows[0] || {}
    const columns = Object.keys(sample).filter((key) => !technicalFields.has(key)).slice(0, 10).map((key) => ({
      title: fieldLabel(key),
      dataIndex: key,
      key,
      render: (value) => formatCellValue(value, { date: /At$|Date$/.test(key), number: typeof value === 'number' }),
    }))
    return <div key={name}><Typography.Title level={5}>{fieldLabel(name)}</Typography.Title><Table size="small" rowKey={(row, index) => row.id || row.lineId || index} columns={columns} dataSource={rows} pagination={false} scroll={{ x: 700 }} /></div>
  })}</Space>
}

function Lifecycle({ system, record }) {
  const status = record.statusName || record.processedStatusName || record.status || '当前状态未知'
  return <Timeline items={system.workflow.map((step, index) => ({
    color: index === 0 ? 'green' : index === 1 ? 'blue' : 'gray',
    content: <div><Typography.Text strong>{step}</Typography.Text><br /><Typography.Text type="secondary">{index === 0 ? `后端当前状态：${status}` : '等待后端流程记录确认节点时间'}</Typography.Text></div>,
  }))} />
}

function fieldLabel(key, definition) {
  const configured = definition?.columns?.find((item) => item.key === key)
  if (configured) return configured.title
  const known = {
    id: '内部 ID', version: '版本', createdAt: '创建时间', updatedAt: '更新时间',
    lines: '业务明细', items: '业务明细', status: '状态', statusName: '状态名称',
  }
  return known[key] || key.replace(/([A-Z])/g, ' $1').replace(/^./, (value) => value.toUpperCase())
}

function recordTitle(record, definition, fallback) {
  const primaryBusinessField = definition?.columns?.[0]?.key
  const candidateKeys = ['name', 'subject', 'supplierName', 'dataName', 'displayName', primaryBusinessField, ...(definition?.rowKey || [])].filter(Boolean)
  for (const key of candidateKeys) {
    if (record?.[key]) return String(record[key])
  }
  return fallback
}
