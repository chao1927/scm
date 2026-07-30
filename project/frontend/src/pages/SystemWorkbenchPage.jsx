import { ArrowRightOutlined, ReloadOutlined, WarningOutlined } from '@ant-design/icons'
import { useQueries, useQueryClient } from '@tanstack/react-query'
import { Button, Card, Col, Empty, Row, Space, Steps, Table, Tag, Typography } from 'antd'
import { useMemo } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { BusinessPageHeader, StatusTag } from '../components/business/BusinessPrimitives'
import { findResourceDefinition, normalizePage, recordIdentity } from '../config/resourceDefinitions'
import { findSystem } from '../config/systemCatalog'

export default function SystemWorkbenchPage() {
  const { systemId } = useParams()
  const system = findSystem(systemId)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const connectedPages = useMemo(() => system.pages
    .filter((page) => page.id !== 'workbench' && findResourceDefinition(system.id, page.id))
    .slice(0, 6), [system])

  const queries = useQueries({
    queries: connectedPages.map((page) => {
      const definition = findResourceDefinition(system.id, page.id)
      return {
        queryKey: ['workbench-resource', system.id, page.id],
        queryFn: () => definition.query({ pageNo: 1, pageSize: 5 }),
        retry: 0,
      }
    }),
  })

  const summaries = connectedPages.map((page, index) => {
    const definition = findResourceDefinition(system.id, page.id)
    const query = queries[index]
    const result = query.data ? normalizePage(query.data, { pageNo: 1, pageSize: 5 }) : { records: [], total: 0 }
    return { page, definition, query, result }
  })
  const recentRows = summaries.flatMap(({ page, definition, result }) => result.records.slice(0, 2).map((record) => ({
    ...record,
    __page: page,
    __definition: definition,
    __identity: recordIdentity(definition, record),
  }))).slice(0, 8)

  const refresh = () => queryClient.invalidateQueries({ queryKey: ['workbench-resource', system.id] })

  return (
    <div className="system-workbench-page">
      <BusinessPageHeader
        eyebrow={`${system.domain} · ${system.code}`}
        title={`${system.name}工作台`}
        description={system.goal}
        actions={<Button icon={<ReloadOutlined />} loading={queries.some((query) => query.isFetching)} onClick={refresh}>刷新工作台</Button>}
      />

      <Row gutter={[16, 16]} className="workbench-metrics">
        {summaries.slice(0, 4).map(({ page, query, result }) => (
          <Col xs={24} sm={12} xl={6} key={page.id}>
            <button className="workbench-metric" onClick={() => navigate(`/${system.id}/${page.id}`)}>
              <span>{page.label}</span>
              <strong>{query.isError ? '—' : query.isLoading ? '…' : result.total}</strong>
              <small>{query.isError ? '接口暂不可用' : '来自真实业务查询'}</small>
            </button>
          </Col>
        ))}
        {summaries.length === 0 && <Col span={24}><Empty description="当前子系统尚无可查询的工作台指标接口" /></Col>}
      </Row>

      <Row gutter={[16, 16]} align="top">
        <Col xs={24} xl={18}>
          <Card title="关键业务闭环" extra={<Button type="link" onClick={() => navigate(`/${system.id}/${connectedPages[0]?.id || 'workbench'}`)}>进入业务 <ArrowRightOutlined /></Button>} className="workbench-section">
            <Steps responsive={false} items={system.workflow.map((title, index) => ({ title, status: index === 0 ? 'process' : 'wait' }))} />
          </Card>
          <Card title="最近业务对象" className="workbench-section">
            <Table
              rowKey={(row) => `${row.__page.id}-${row.__identity}`}
              dataSource={recentRows}
              pagination={false}
              locale={{ emptyText: '当前接口未返回业务数据' }}
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
              <Tag icon={<WarningOutlined />} color="gold">待补读模型 {Math.max(0, system.pages.length - 1 - connectedPages.length)}</Tag>
              <Typography.Text type="secondary">无接口页面不会使用原型演示数据。</Typography.Text>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  )
}
