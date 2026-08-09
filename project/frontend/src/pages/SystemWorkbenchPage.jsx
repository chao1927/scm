import { ArrowRightOutlined, ReloadOutlined, WarningOutlined } from '@ant-design/icons'
import { useQueries, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Card, Col, Empty, Row, Space, Steps, Table, Tag, Typography } from 'antd'
import { useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { BusinessPageHeader, StatusTag } from '../components/business/BusinessPrimitives'
import { findResourceDefinition, normalizePage, recordIdentity } from '../config/resourceDefinitions'
import { findSystem } from '../config/systemCatalog'

export const workbenchQueryParams = { pageNo: 1, pageSize: 10 }

export function workbenchPageGroups(system) {
  const businessPages = system.pages.filter((page) => page.id !== 'workbench')
  const connectedPages = businessPages.filter((page) => (
    typeof findResourceDefinition(system.id, page.id)?.query === 'function'
  ))
  return {
    connectedPages,
    pendingPages: businessPages.filter((page) => !connectedPages.includes(page)),
    summaryPages: connectedPages.slice(0, 6),
  }
}

export function summarizeWorkbenchQueries(summaries) {
  return summaries.reduce((state, { query, result }) => ({
    failedCount: state.failedCount + (query.isError ? 1 : 0),
    loadingCount: state.loadingCount + (query.isLoading ? 1 : 0),
    successfulCount: state.successfulCount + (query.isError ? 0 : 1),
    hasSuccessfulData: state.hasSuccessfulData || (!query.isError && result.records.length > 0),
  }), { failedCount: 0, loadingCount: 0, successfulCount: 0, hasSuccessfulData: false })
}

export function metricQueryState(query, result) {
  if (query.isError) return { value: '—', hint: '接口加载失败，点击重试', action: 'retry' }
  if (query.isLoading) return { value: '…', hint: '正在加载真实业务查询', action: 'none' }
  return { value: result.total, hint: '来自真实业务查询', action: 'navigate' }
}

export default function SystemWorkbenchPage() {
  const { systemId } = useParams()
  const system = findSystem(systemId)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { connectedPages, pendingPages, summaryPages } = useMemo(
    () => workbenchPageGroups(system),
    [system],
  )

  const queries = useQueries({
    queries: summaryPages.map((page) => {
      const definition = findResourceDefinition(system.id, page.id)
      return {
        queryKey: ['workbench-resource', system.id, page.id],
        queryFn: () => definition.query(workbenchQueryParams),
        retry: 0,
      }
    }),
  })

  const summaries = summaryPages.map((page, index) => {
    const definition = findResourceDefinition(system.id, page.id)
    const query = queries[index]
    const result = query.data ? normalizePage(query.data, workbenchQueryParams) : { records: [], total: 0 }
    return { page, definition, query, result }
  })
  const queryState = summarizeWorkbenchQueries(summaries)
  const recentRows = summaries.flatMap(({ page, definition, result }) => result.records.slice(0, 2).map((record) => ({
    ...record,
    __page: page,
    __definition: definition,
    __identity: recordIdentity(definition, record),
  }))).slice(0, 8)

  const refresh = () => queryClient.invalidateQueries({ queryKey: ['workbench-resource', system.id] })
  const retryFailed = () => summaries.filter(({ query }) => query.isError).forEach(({ query }) => query.refetch())

  return (
    <div className="system-workbench-page">
      <BusinessPageHeader
        eyebrow={`${system.domain} · ${system.code}`}
        title={`${system.name}工作台`}
        description={system.goal}
        actions={<Button icon={<ReloadOutlined />} loading={queries.some((query) => query.isFetching)} onClick={refresh}>刷新工作台</Button>}
      />

      <Row gutter={[16, 16]} className="workbench-metrics">
        {summaries.slice(0, 4).map(({ page, query, result }) => {
          const metric = metricQueryState(query, result)
          return (
            <Col xs={24} sm={12} xl={6} key={page.id}>
              <button
                className="workbench-metric"
                aria-label={metric.action === 'retry' ? `${page.label}加载失败，重试` : `进入${page.label}`}
                onClick={() => metric.action === 'retry' ? query.refetch() : metric.action === 'navigate' && navigate(`/${system.id}/${page.id}`)}
              >
                <span>{page.label}</span>
                <strong>{metric.value}</strong>
                <small>{query.isError && query.error?.message ? `${metric.hint}：${query.error.message}` : metric.hint}</small>
              </button>
            </Col>
          )
        })}
        {summaries.length === 0 && <Col span={24}><Empty description="当前子系统尚无可查询的工作台指标接口" /></Col>}
      </Row>

      {queryState.failedCount > 0 && (
        <Alert
          type="error"
          showIcon
          role="alert"
          title={`${queryState.failedCount} 个工作台查询加载失败`}
          description="已成功的读模型仍保留展示；失败卡片不会被当作空数据。"
          action={<Button loading={summaries.some(({ query }) => query.isError && query.isFetching)} onClick={retryFailed}>重试失败查询</Button>}
        />
      )}

      <Row gutter={[16, 16]} align="top">
        <Col xs={24} xl={18}>
          <Card title="关键业务闭环" extra={<Button type="link" onClick={() => navigate(`/${system.id}/${connectedPages[0]?.id || 'workbench'}`)}>进入业务 <ArrowRightOutlined /></Button>} className="workbench-section">
            <Steps responsive={false} items={system.workflow.map((title, index) => ({ title, status: index === 0 ? 'process' : 'wait' }))} />
          </Card>
          <Card title="最近业务对象" className="workbench-section">
            <Table
              rowKey={(row) => `${row.__page.id}-${row.__identity}`}
              dataSource={recentRows}
              loading={queryState.loadingCount > 0}
              pagination={false}
              locale={{ emptyText: queryState.failedCount > 0 && !queryState.hasSuccessfulData ? '工作台查询加载失败，请重试上方接口' : '当前接口未返回业务数据' }}
              scroll={{ x: 850 }}
              columns={[
                { title: '所属功能', dataIndex: ['__page', 'label'], width: 140 },
                { title: '业务编号', dataIndex: '__identity', width: 180, render: (value, row) => <Button type="link" onClick={() => navigate(`/${system.id}/${row.__page.id}/${encodeURIComponent(value)}`)}>{value || '查看详情'}</Button> },
                { title: '业务对象', width: 240, ellipsis: true, render: (_, row) => row.subject || row.name || row.supplierName || row.dataName || row.reason || '—' },
                { title: '状态', width: 130, render: (_, row) => <StatusTag value={row.statusName || row.processedStatusName || row.status} /> },
                { title: '更新时间', width: 180, render: (_, row) => row.updatedAt || row.createdAt || row.occurredAt || '—' },
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} xl={6}>
          <Card title="常用功能" className="workbench-section">
            <div className="quick-links">
              {system.pages.filter((page) => page.id !== 'workbench').slice(0, 8).map((page) => (
                <Button key={page.id} type="text" onClick={() => navigate(`/${system.id}/${page.id}`)}><span>{page.label}</span><ArrowRightOutlined /></Button>
              ))}
            </div>
          </Card>
          <Card title="接口状态" className="workbench-section">
            <Space orientation="vertical" size={10}>
              <Tag color="green">已接真实接口 {connectedPages.length}</Tag>
              <Tag icon={<WarningOutlined />} color={pendingPages.length > 0 ? 'gold' : 'default'}>待补读模型 {pendingPages.length}</Tag>
              <Typography.Text type="secondary">无接口页面不会使用原型演示数据。</Typography.Text>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  )
}
