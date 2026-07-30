import {
  ApiOutlined,
  AppstoreOutlined,
  DatabaseOutlined,
  FieldTimeOutlined,
  ShopOutlined,
} from '@ant-design/icons'
import { Button, Col, Descriptions, Empty, Row, Space, Table, Tag, Typography } from 'antd'
import { resourceDefinitions, resourceKey } from '../config/resourceDefinitions'
import { systemCatalog } from '../config/systemCatalog'

export function buildCapabilitySnapshot(systems, definitions) {
  const rows = systems.map((system) => {
    const pages = system.pages.filter((page) => page.id !== 'workbench')
    const registeredPages = pages.filter((page) => Boolean(definitions[resourceKey(system.id, page.id)]))
    const pendingPages = pages.filter((page) => !definitions[resourceKey(system.id, page.id)])
    return {
      key: system.id,
      system: system.name,
      domain: system.domain,
      pageCount: pages.length,
      registeredCount: registeredPages.length,
      pendingCount: pendingPages.length,
      registeredLabels: registeredPages.map((page) => page.label),
      pendingLabels: pendingPages.map((page) => page.label),
      workbenchPath: `/${system.id}/workbench`,
    }
  })
  return {
    rows,
    totals: {
      systems: rows.length,
      pages: rows.reduce((total, row) => total + row.pageCount, 0),
      registeredResources: rows.reduce((total, row) => total + row.registeredCount, 0),
      pendingPages: rows.reduce((total, row) => total + row.pendingCount, 0),
    },
  }
}

const capabilitySnapshot = buildCapabilitySnapshot(systemCatalog, resourceDefinitions)

const capabilityColumns = [
  { title: '系统', dataIndex: 'system', width: 150, fixed: 'left' },
  { title: '领域边界', dataIndex: 'domain', width: 150 },
  { title: '业务页面', dataIndex: 'pageCount', width: 100, render: (value) => `${value} 个` },
  { title: '已注册资源', dataIndex: 'registeredCount', width: 130, render: (value) => <Tag color="green">已注册 {value}</Tag> },
  { title: '待接读模型', dataIndex: 'pendingCount', width: 130, render: (value) => value > 0 ? <Tag color="gold">待接 {value}</Tag> : <Tag color="blue">无缺口</Tag> },
  {
    title: '已注册业务入口',
    dataIndex: 'registeredLabels',
    width: 420,
    render: (labels) => labels.length ? labels.join('、') : '尚无已注册资源',
  },
  {
    title: '导航',
    dataIndex: 'workbenchPath',
    width: 120,
    fixed: 'right',
    render: (path) => <Button type="link" href={path}>进入工作台</Button>,
  },
]

export default function WorkbenchPage({ onOpenAsn }) {
  const { rows, totals } = capabilitySnapshot
  return (
    <div className="workbench-page">
      <div className="page-heading">
        <div>
          <Typography.Title level={3}>供应链运营工作台</Typography.Title>
          <Typography.Text type="secondary">从九个子系统目录和资源注册表实时汇总业务入口，不展示人工估算或演示状态。</Typography.Text>
        </div>
        <Space wrap>
          <Button type="primary" icon={<ShopOutlined />} onClick={onOpenAsn}>进入 ASN</Button>
        </Space>
      </div>

      <Row gutter={[16, 16]} className="metric-row">
        <Col xs={24} sm={12} xl={6}><Metric icon={<AppstoreOutlined />} label="子系统目录" value={totals.systems} hint="来自 systemCatalog" /></Col>
        <Col xs={24} sm={12} xl={6}><Metric icon={<FieldTimeOutlined />} label="业务页面" value={totals.pages} hint="不含子系统工作台" /></Col>
        <Col xs={24} sm={12} xl={6}><Metric icon={<DatabaseOutlined />} label="已注册资源" value={totals.registeredResources} hint="来自 resourceDefinitions" /></Col>
        <Col xs={24} sm={12} xl={6}><Metric icon={<ApiOutlined />} label="待接读模型" value={totals.pendingPages} hint="目录页面尚无资源定义" /></Col>
      </Row>

      <div className="section-band">
        <div className="section-title">
          <Typography.Title level={4}>九系统资源接入情况</Typography.Title>
          <Typography.Text type="secondary">只展示当前代码中可验证的页面和资源注册事实</Typography.Text>
        </div>
        <Table rowKey="key" columns={capabilityColumns} dataSource={rows} pagination={false} scroll={{ x: 1200 }} size="middle" />
      </div>

      <Row gutter={[16, 16]} className="lower-grid">
        <Col xs={24} xl={15}>
          <div className="section-band">
            <div className="section-title">
              <Typography.Title level={4}>系统能力导航</Typography.Title>
              <Typography.Text type="secondary">进入子系统工作台后查看其已授权菜单和真实读模型</Typography.Text>
            </div>
            <div className="quick-links">
              {rows.map((row) => (
                <Button key={row.key} type="text" href={row.workbenchPath}>
                  <span>{row.system}</span>
                  <span>{row.registeredCount} / {row.pageCount} 资源</span>
                </Button>
              ))}
            </div>
          </div>
        </Col>
        <Col xs={24} xl={9}>
          <div className="section-band">
            <div className="section-title">
              <Typography.Title level={4}>统计口径</Typography.Title>
            </div>
            <Descriptions bordered size="small" column={1}>
              <Descriptions.Item label="系统事实源">systemCatalog 九系统目录</Descriptions.Item>
              <Descriptions.Item label="资源事实源">resourceDefinitions 资源注册表</Descriptions.Item>
              <Descriptions.Item label="已注册">目录页面存在对应资源定义</Descriptions.Item>
              <Descriptions.Item label="待接读模型">目录页面尚无对应资源定义</Descriptions.Item>
            </Descriptions>
          </div>
        </Col>
      </Row>

      {rows.length === 0 && <Empty description="当前未登记业务子系统" />}
    </div>
  )
}

function Metric({ icon, label, value, hint }) {
  return (
    <div className="metric-tile">
      <div className="metric-icon" aria-hidden="true">{icon}</div>
      <div>
        <div className="metric-label">{label}</div>
        <div className="metric-value">{value}</div>
        <div className="metric-hint">{hint}</div>
      </div>
    </div>
  )
}
