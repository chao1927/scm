import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Button, Input, message, Space, Table, Tabs } from 'antd'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useOutletContext, useParams, useSearchParams } from 'react-router-dom'
import { hasPermission } from '../auth/menuAccess'
import { BusinessPageHeader, CapabilityPending, formatCellValue, InlineQueryError, StatusTag } from '../components/business/BusinessPrimitives'
import CommandDialog from '../components/business/CommandDialog'
import AsnCreateDrawer from '../components/business/AsnCreateDrawer'
import { findResourceDefinition, normalizePage, recordIdentity } from '../config/resourceDefinitions'
import { findSystem, findSystemPage } from '../config/systemCatalog'

const defaultPageSize = 20

export function readListFilters(searchParams) {
  return {
    pageNo: Math.max(1, Number(searchParams.get('pageNo')) || 1),
    pageSize: Math.max(1, Number(searchParams.get('pageSize')) || defaultPageSize),
    keyword: searchParams.get('keyword') || undefined,
    status: searchParams.get('status') || undefined,
  }
}

export function mergeListSearchParams(searchParams, patch) {
  const next = new URLSearchParams(searchParams)
  Object.entries(patch).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') next.delete(key)
    else next.set(key, String(value))
  })
  return next
}

export function listEmptyText(query) {
  return query.isError ? '数据加载失败，请重试' : '暂无业务数据'
}

export function listPaginationPatch(filters, pageNo, pageSize) {
  return { pageNo: pageSize === filters.pageSize ? pageNo : 1, pageSize }
}

export default function ResourceListPage() {
  const { systemId, pageId } = useParams()
  const system = findSystem(systemId)
  const page = findSystemPage(systemId, pageId)
  const definition = findResourceDefinition(systemId, pageId)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [searchParams, setSearchParams] = useSearchParams()
  const [commandState, setCommandState] = useState({ action: null, record: null })
  const [createOpen, setCreateOpen] = useState(false)
  const { session } = useOutletContext()
  const filters = readListFilters(searchParams)
  const [keywordDraft, setKeywordDraft] = useState(filters.keyword || '')

  useEffect(() => {
    setKeywordDraft(filters.keyword || '')
  }, [filters.keyword])

  const query = useQuery({
    queryKey: ['business-resource', systemId, pageId, filters],
    queryFn: () => definition.query(filters),
    enabled: Boolean(definition?.query),
  })

  const pageResult = definition ? normalizePage(query.data, filters) : { records: [], total: 0, pageNo: 1, pageSize: defaultPageSize }
  const records = pageResult.records
  const statusItems = useMemo(() => {
    const unique = new Map()
    records.forEach((record) => {
      const value = record.status
      const label = record.statusName || record.processedStatusName || record.status
      if (value !== undefined && value !== null) unique.set(String(value), String(label))
    })
    return [
      { key: '', label: `全部 ${pageResult.total}` },
      ...[...unique].map(([key, label]) => ({ key, label: `${label} ${records.filter((record) => String(record.status) === key).length}` })),
    ]
  }, [pageResult.total, records])

  const updateSearch = (patch) => {
    setSearchParams(mergeListSearchParams(searchParams, patch))
  }

  const actionMutation = useMutation({
    mutationFn: ({ action, record, reason }) => action.run(record, reason),
    onSuccess: (_, variables) => {
      message.success(`${variables.action.label}已完成`)
      setCommandState({ action: null, record: null })
      queryClient.invalidateQueries({ queryKey: ['business-resource', systemId, pageId] })
    },
    onError: (error) => message.error(error?.message || '操作失败'),
  })
  const pageActions = (definition?.pageActions || [])
    .filter((action) => hasPermission(session.permissions, action.permission))
  const canCreate = definition?.create && hasPermission(session.permissions, definition.create.permission)

  if (!definition) return <CapabilityPending system={system} page={page} />

  const columns = definition.columns.map((item, index) => ({
    title: item.title,
    dataIndex: item.key,
    key: item.key,
    width: item.width || (index === 0 ? 190 : 140),
    ellipsis: !item.status,
    fixed: index === 0 ? 'left' : undefined,
    render: (value, record) => {
      if (index === 0) {
        const identity = recordIdentity(definition, record)
        return <Button type="link" className="record-link" onClick={() => navigate(`/${systemId}/${pageId}/${encodeURIComponent(identity)}`)}>{formatCellValue(value, item)}</Button>
      }
      return item.status ? <StatusTag value={value ?? record.statusName ?? record.status} /> : formatCellValue(value, item)
    },
  }))

  if (definition.actions?.length) {
    columns.push({
      title: '操作',
      key: 'actions',
      width: 180,
      fixed: 'right',
      render: (_, record) => (
        <Space size={4}>
          {definition.actions
            .filter((action) => hasPermission(session.permissions, action.permission))
            .filter((action) => !action.visible || action.visible(record))
            .slice(0, 3)
            .map((action) => (
              <Button key={action.key} type="link" danger={action.danger} onClick={() => setCommandState({ action, record: { ...record, __identity: recordIdentity(definition, record) } })}>
                {action.label}
              </Button>
            ))}
        </Space>
      ),
    })
  }

  return (
    <div className="business-list-page">
      <BusinessPageHeader
        eyebrow={system.domain}
        title={page.label}
        description={`${system.goal} 当前页面使用真实后端读模型。`}
        actions={<>
          {canCreate && <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>{definition.create.label}</Button>}
          {pageActions.map((action) => <Button key={action.key} onClick={() => setCommandState({ action, record: null })}>{action.label}</Button>)}
          <Button icon={<ReloadOutlined />} loading={query.isFetching} onClick={() => query.refetch()}>刷新</Button>
        </>}
      />
      <section className="business-list-panel">
        <Tabs
          className="status-tabs"
          activeKey={filters.status || ''}
          items={statusItems}
          onChange={(status) => updateSearch({ status, pageNo: 1 })}
        />
        <div className="business-filter-bar">
          <Input.Search
            allowClear
            prefix={<SearchOutlined />}
            value={keywordDraft}
            placeholder="输入编号、对象或负责人"
            onChange={(event) => setKeywordDraft(event.target.value)}
            onSearch={(keyword) => updateSearch({ keyword, pageNo: 1 })}
          />
          <Button onClick={() => setSearchParams(new URLSearchParams())}>重置</Button>
        </div>
        {query.isError && <InlineQueryError error={query.error} retrying={query.isFetching} onRetry={() => query.refetch()} />}
        <Table
          rowKey={(record) => recordIdentity(definition, record)}
          columns={columns}
          dataSource={records}
          loading={query.isLoading}
          locale={{ emptyText: listEmptyText(query) }}
          scroll={{ x: Math.max(900, columns.reduce((total, item) => total + Number(item.width || 140), 0)) }}
          pagination={{
            current: pageResult.pageNo,
            pageSize: pageResult.pageSize,
            total: pageResult.total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (pageNo, pageSize) => updateSearch(listPaginationPatch(filters, pageNo, pageSize)),
          }}
        />
      </section>
      <CommandDialog
        open={Boolean(commandState.action)}
        action={commandState.action}
        record={commandState.record}
        loading={actionMutation.isPending}
        onCancel={() => setCommandState({ action: null, record: null })}
        onConfirm={(reason) => actionMutation.mutate({ ...commandState, reason })}
      />
      {definition.create?.kind === 'asn' && (
        <AsnCreateDrawer
          open={createOpen}
          onClose={() => setCreateOpen(false)}
          onCreated={() => {
            setCreateOpen(false)
            queryClient.invalidateQueries({ queryKey: ['business-resource', systemId, pageId] })
          }}
        />
      )}
    </div>
  )
}
