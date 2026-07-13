import {
  AlertOutlined,
  ApiOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DatabaseOutlined,
  FieldTimeOutlined,
  SafetyCertificateOutlined,
  ShopOutlined,
  SyncOutlined,
} from '@ant-design/icons'
import { Button, Col, Descriptions, Empty, Progress, Row, Space, Table, Tag, Timeline, Typography } from 'antd'

const systemRows = [
  { key: 'supplier', system: '供应商系统', owner: '供应商协同', status: '首轮完成', progress: 86, focus: '准入、报价、合同、ASN、对账、评分', risk: '真实 MQ/Dubbo 联调' },
  { key: 'purchase', system: '采购系统', owner: '采购中心', status: '首轮完成', progress: 88, focus: '请购、询价、比价、PO、到货、退供', risk: '集成命令分发器' },
  { key: 'wms', system: 'WMS 系统', owner: '仓储执行', status: '首轮完成', progress: 84, focus: '入库、收货、质检、上架、出库、盘点', risk: '行级模型与真实联调' },
  { key: 'inventory', system: '中央库存', owner: '库存事实', status: '首轮完成', progress: 82, focus: '账户、流水、预占、冻结、快照、对账', risk: '事件载荷版本化' },
  { key: 'oms', system: 'OMS 系统', owner: '订单履约', status: '首轮完成', progress: 80, focus: '渠道订单、分仓、预占、WMS 出库、取消售后', risk: '退货入库增强' },
  { key: 'tms', system: 'TMS 系统', owner: '运输协同', status: '首轮完成', progress: 81, focus: '运输任务、运单、面单、轨迹、签收、费用来源', risk: '承运商签名验签' },
  { key: 'bms', system: 'BMS 系统', owner: '费用结算', status: '首轮完成', progress: 83, focus: '计费、费用、对账、账单、发票、财务、退款', risk: '财税支付沙箱' },
  { key: 'integration', system: '集成中心', owner: '平台集成', status: '增强完成', progress: 78, focus: '路由、运行面、HTTP、端点预检、死信重放', risk: '真实 MQ/Dubbo 接入' },
]

const priorityRows = [
  { key: 'p1', priority: 'P1', item: 'RocketMQ Producer、Dubbo 泛化调用、ERP/税务/支付沙箱联调', status: '外部环境待准备', owner: '平台集成' },
  { key: 'p2', priority: 'P2', item: '前端业务页面、权限 Web 测试、生产化集成测试', status: '开发中', owner: '前端与 QA' },
  { key: 'p3', priority: 'P3', item: '统一报表分析、压测、告警、容灾和上线治理', status: '计划中', owner: '运维治理' },
]

const activityItems = [
  { color: 'green', children: 'BMS 计费结算首轮完成，费用来源到财务交接链路具备本地验证。' },
  { color: 'green', children: '集成中心完成 HTTP/OpenAPI 通道、端点熔断、RocketMQ/Dubbo 契约预检。' },
  { color: 'blue', children: '前端开始补齐供应链运营工作台与系统导航。' },
  { color: 'orange', children: 'P1 真实联调等待 nameserver、注册中心、财税支付沙箱资料。' },
]

const systemColumns = [
  { title: '系统', dataIndex: 'system', width: 140, fixed: 'left' },
  { title: '业务域', dataIndex: 'owner', width: 120 },
  { title: '状态', dataIndex: 'status', width: 110, render: (value) => <Tag color={value.includes('完成') ? 'green' : 'blue'}>{value}</Tag> },
  { title: '完成度', dataIndex: 'progress', width: 180, render: (value) => <Progress percent={value} size="small" /> },
  { title: '已覆盖范围', dataIndex: 'focus', width: 360 },
  { title: '剩余风险', dataIndex: 'risk', width: 180, render: (value) => <Tag icon={<AlertOutlined />} color="gold">{value}</Tag> },
]

const priorityColumns = [
  { title: '优先级', dataIndex: 'priority', width: 90, render: (value) => <Tag color={value === 'P1' ? 'red' : value === 'P2' ? 'blue' : 'default'}>{value}</Tag> },
  { title: '未完成事项', dataIndex: 'item' },
  { title: '状态', dataIndex: 'status', width: 140 },
  { title: '责任域', dataIndex: 'owner', width: 120 },
]

export default function WorkbenchPage({ onOpenAsn }) {
  return (
    <div className="workbench-page">
      <div className="page-heading">
        <div>
          <Typography.Title level={3}>供应链运营工作台</Typography.Title>
          <Typography.Text type="secondary">查看系统完成情况、集成风险和下一步开发优先级</Typography.Text>
        </div>
        <Space wrap>
          <Button icon={<SyncOutlined />}>刷新状态</Button>
          <Button type="primary" icon={<ShopOutlined />} onClick={onOpenAsn}>进入 ASN</Button>
        </Space>
      </div>

      <Row gutter={[16, 16]} className="metric-row">
        <Col xs={24} sm={12} xl={6}><Metric icon={<CheckCircleOutlined />} label="首轮完成系统" value="10" hint="含 BMS 与集成中心" /></Col>
        <Col xs={24} sm={12} xl={6}><Metric icon={<ApiOutlined />} label="集成通道能力" value="4" hint="HTTP、OpenAPI、MQ 预检、Dubbo 预检" /></Col>
        <Col xs={24} sm={12} xl={6}><Metric icon={<SafetyCertificateOutlined />} label="P1 阻塞项" value="1" hint="依赖外部中间件与沙箱" /></Col>
        <Col xs={24} sm={12} xl={6}><Metric icon={<FieldTimeOutlined />} label="前端切片" value="开发中" hint="工作台与 ASN 页面" /></Col>
      </Row>

      <div className="section-band">
        <div className="section-title">
          <Typography.Title level={4}>系统完成状态</Typography.Title>
          <Typography.Text type="secondary">按开发日志中的首轮完成状态汇总</Typography.Text>
        </div>
        <Table rowKey="key" columns={systemColumns} dataSource={systemRows} pagination={false} scroll={{ x: 1180 }} size="middle" />
      </div>

      <Row gutter={[16, 16]} className="lower-grid">
        <Col xs={24} xl={15}>
          <div className="section-band">
            <div className="section-title">
              <Typography.Title level={4}>未完成优先级</Typography.Title>
              <Typography.Text type="secondary">继续开发时从 P1 开始处理，外部阻塞明确记录</Typography.Text>
            </div>
            <Table rowKey="key" columns={priorityColumns} dataSource={priorityRows} pagination={false} size="middle" />
          </div>
        </Col>
        <Col xs={24} xl={9}>
          <div className="section-band">
            <div className="section-title">
              <Typography.Title level={4}>最近推进</Typography.Title>
              <Typography.Text type="secondary">按当前开发日志摘要展示</Typography.Text>
            </div>
            <Timeline items={activityItems} />
          </div>
        </Col>
      </Row>

      <div className="section-band">
        <div className="section-title">
          <Typography.Title level={4}>当前联调前置条件</Typography.Title>
          <Typography.Text type="secondary">P1 继续推进前必须具备的外部资料</Typography.Text>
        </div>
        <Descriptions bordered size="small" column={{ xs: 1, md: 2, xl: 4 }}>
          <Descriptions.Item label="RocketMQ">nameserver、topic、tag、ACL</Descriptions.Item>
          <Descriptions.Item label="Dubbo/Nacos">注册中心、服务接口、方法、版本</Descriptions.Item>
          <Descriptions.Item label="财税支付">ERP、税控、支付沙箱地址和验签资料</Descriptions.Item>
          <Descriptions.Item label="契约测试">样例载荷、错误码、重放数据</Descriptions.Item>
        </Descriptions>
      </div>

      {systemRows.length === 0 && <Empty description="暂无系统状态" />}
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
