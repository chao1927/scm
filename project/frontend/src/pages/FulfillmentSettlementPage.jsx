import { useMemo } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Button, Col, Row, Statistic, Table, Tabs, Tag, Typography } from 'antd'
import { CarOutlined, DatabaseOutlined, DeploymentUnitOutlined, ReloadOutlined, WalletOutlined } from '@ant-design/icons'
import {
  queryBmsBillingObjects,
  queryBmsChargeSources,
  queryBmsSettlementSummary,
  queryOmsChannelOrders,
  queryOmsFulfillments,
  queryOmsOutbounds,
  queryOmsSalesOrders,
  queryTmsExceptions,
  queryTmsFeeSources,
  queryTmsTransportTasks,
  queryTmsWaybills,
} from '../api/fulfillmentSettlement'

const currentYear = new Date().getFullYear()
const settlementRange = {
  from: `${currentYear}-01-01T00:00:00`,
  to: `${currentYear}-12-31T23:59:59`,
}

const omsOrderStatus = {
  1: ['已接收', 'blue'],
  2: ['已审单', 'green'],
  3: ['已取消', 'red'],
}

const fulfillmentStatus = {
  1: ['待分仓', 'default'],
  2: ['已分仓', 'blue'],
  3: ['已预占', 'cyan'],
  4: ['已下发 WMS', 'processing'],
  5: ['已出库', 'green'],
  6: ['已取消', 'red'],
  7: ['异常', 'orange'],
}

const outboundStatus = {
  1: ['待下发', 'default'],
  2: ['已下发', 'blue'],
  3: ['失败', 'red'],
  4: ['已取消', 'default'],
}

const tmsStatus = {
  1: ['待接单', 'default'],
  2: ['已接单', 'blue'],
  3: ['运输中', 'processing'],
  4: ['已签收', 'green'],
  5: ['已取消', 'default'],
  6: ['异常', 'red'],
}

const waybillStatus = {
  1: ['已创建', 'blue'],
  2: ['已作废', 'default'],
  3: ['已签收', 'green'],
}

const bmsStatus = {
  1: ['启用/待处理', 'blue'],
  2: ['停用/成功', 'green'],
  3: ['失败', 'red'],
}

export default function FulfillmentSettlementPage() {
  const queryClient = useQueryClient()
  const channelOrdersQuery = useQuery({ queryKey: ['oms-channel-orders'], queryFn: queryOmsChannelOrders })
  const salesOrdersQuery = useQuery({ queryKey: ['oms-sales-orders'], queryFn: queryOmsSalesOrders })
  const fulfillmentsQuery = useQuery({ queryKey: ['oms-fulfillments'], queryFn: queryOmsFulfillments })
  const outboundsQuery = useQuery({ queryKey: ['oms-outbounds'], queryFn: queryOmsOutbounds })
  const tasksQuery = useQuery({ queryKey: ['tms-transport-tasks'], queryFn: () => queryTmsTransportTasks({ pageNo: 1, pageSize: 50 }) })
  const waybillsQuery = useQuery({ queryKey: ['tms-waybills'], queryFn: queryTmsWaybills })
  const exceptionsQuery = useQuery({ queryKey: ['tms-exceptions'], queryFn: queryTmsExceptions })
  const feeSourcesQuery = useQuery({ queryKey: ['tms-fee-sources'], queryFn: queryTmsFeeSources })
  const billingObjectsQuery = useQuery({ queryKey: ['bms-billing-objects'], queryFn: () => queryBmsBillingObjects() })
  const chargeSourcesQuery = useQuery({ queryKey: ['bms-charge-sources'], queryFn: () => queryBmsChargeSources() })
  const settlementSummaryQuery = useQuery({ queryKey: ['bms-settlement-summary'], queryFn: () => queryBmsSettlementSummary(settlementRange) })

  const refresh = () => queryClient.invalidateQueries()

  const channelOrderColumns = useMemo(() => [
    { title: '渠道', dataIndex: 'channelCode', width: 120 },
    { title: '渠道订单', dataIndex: 'channelOrderNo', width: 180 },
    { title: '销售订单', dataIndex: 'orderNo', width: 180 },
    { title: '创建时间', dataIndex: 'createdAt', width: 180, render: formatDateTime },
  ], [])

  const salesOrderColumns = useMemo(() => [
    { title: '销售订单', dataIndex: 'orderNo', width: 180, fixed: 'left' },
    { title: '渠道', dataIndex: 'channelCode', width: 120 },
    { title: '渠道订单', dataIndex: 'channelOrderNo', width: 180 },
    { title: '客户', dataIndex: 'customerId', width: 100 },
    { title: '金额', dataIndex: 'totalAmount', width: 110 },
    { title: '状态', dataIndex: 'status', width: 110, render: (value) => <StatusTag value={value} mapping={omsOrderStatus} /> },
    { title: '审单备注', dataIndex: 'reviewRemark', width: 220, ellipsis: true },
  ], [])

  const fulfillmentColumns = useMemo(() => [
    { title: '履约单', dataIndex: 'fulfillmentNo', width: 190, fixed: 'left' },
    { title: '销售订单', dataIndex: 'salesOrderNo', width: 180 },
    { title: '渠道', dataIndex: 'channelCode', width: 100 },
    { title: '仓库', dataIndex: 'warehouseCode', width: 110 },
    { title: '物流产品', dataIndex: 'logisticsProductCode', width: 140 },
    { title: '预占号', dataIndex: 'reservationNo', width: 180 },
    { title: '出库单', dataIndex: 'outboundNo', width: 180 },
    { title: '状态', dataIndex: 'status', width: 130, render: (value) => <StatusTag value={value} mapping={fulfillmentStatus} /> },
    { title: '失败原因', dataIndex: 'failureReason', width: 240, ellipsis: true },
  ], [])

  const outboundColumns = useMemo(() => [
    { title: '出库单', dataIndex: 'outboundNo', width: 190, fixed: 'left' },
    { title: '履约单', dataIndex: 'fulfillmentNo', width: 190 },
    { title: '销售订单', dataIndex: 'salesOrderNo', width: 180 },
    { title: '仓库', dataIndex: 'warehouseCode', width: 110 },
    { title: 'WMS 单号', dataIndex: 'wmsOrderNo', width: 180 },
    { title: '状态', dataIndex: 'status', width: 110, render: (value) => <StatusTag value={value} mapping={outboundStatus} /> },
    { title: '重试', dataIndex: 'retryCount', width: 80 },
    { title: '取消原因', dataIndex: 'cancelReason', width: 220, ellipsis: true },
  ], [])

  const taskColumns = useMemo(() => [
    { title: '运输任务', dataIndex: 'taskNo', width: 190, fixed: 'left' },
    { title: '来源系统', dataIndex: 'sourceSystem', width: 120 },
    { title: '来源单', dataIndex: 'sourceOrderNo', width: 180 },
    { title: '场景', dataIndex: 'scenario', width: 110 },
    { title: '仓库', dataIndex: 'warehouseId', width: 100 },
    { title: '承运商', dataIndex: 'carrierName', width: 160, ellipsis: true },
    { title: '物流产品', dataIndex: 'logisticsProductCode', width: 140 },
    { title: '费用责任', dataIndex: 'feeResponsibility', width: 120 },
    { title: '状态', dataIndex: 'status', width: 110, render: (value) => <StatusTag value={value} mapping={tmsStatus} /> },
  ], [])

  const waybillColumns = useMemo(() => [
    { title: '运单', dataIndex: 'waybillNo', width: 190, fixed: 'left' },
    { title: '运输任务', dataIndex: 'taskNo', width: 190 },
    { title: '承运商', dataIndex: 'carrierName', width: 160 },
    { title: '承运商单号', dataIndex: 'carrierWaybillNo', width: 180 },
    { title: '物流产品', dataIndex: 'logisticsProductCode', width: 140 },
    { title: '状态', dataIndex: 'status', width: 110, render: (value) => <StatusTag value={value} mapping={waybillStatus} /> },
    { title: '作废原因', dataIndex: 'voidReason', width: 220, ellipsis: true },
  ], [])

  const exceptionColumns = useMemo(() => [
    { title: '异常单', dataIndex: 'exceptionNo', width: 190, fixed: 'left' },
    { title: '运单', dataIndex: 'waybillNo', width: 190 },
    { title: '类型', dataIndex: 'exceptionType', width: 130 },
    { title: '等级', dataIndex: 'level', width: 90 },
    { title: '责任方', dataIndex: 'responsibleParty', width: 120 },
    { title: '状态', dataIndex: 'status', width: 100, render: (value) => value === 1 ? <Tag color="red">打开</Tag> : <Tag color="green">已关闭</Tag> },
    { title: '描述', dataIndex: 'description', width: 260, ellipsis: true },
    { title: '关闭结果', dataIndex: 'closeResult', width: 220, ellipsis: true },
  ], [])

  const tmsFeeColumns = useMemo(() => [
    { title: '费用来源', dataIndex: 'feeSourceNo', width: 190, fixed: 'left' },
    { title: '运单', dataIndex: 'waybillNo', width: 180 },
    { title: '承运商', dataIndex: 'carrierCode', width: 120 },
    { title: '费用项', dataIndex: 'feeItemCode', width: 120 },
    { title: '金额', dataIndex: 'amount', width: 100 },
    { title: '币种', dataIndex: 'currency', width: 80 },
    { title: '账期', dataIndex: 'billingPeriod', width: 110 },
    { title: '推送状态', dataIndex: 'pushStatus', width: 110, render: (value) => <StatusTag value={value} mapping={bmsStatus} /> },
    { title: 'BMS 接收号', dataIndex: 'bmsReceiveNo', width: 180 },
    { title: '失败原因', dataIndex: 'failureReason', width: 240, ellipsis: true },
  ], [])

  const billingObjectColumns = useMemo(() => [
    { title: '计费对象', dataIndex: 'objectCode', width: 170, fixed: 'left' },
    { title: '名称', dataIndex: 'objectName', width: 180 },
    { title: '类型', dataIndex: 'objectType', width: 120 },
    { title: '方向', dataIndex: 'direction', width: 100 },
    { title: '币种', dataIndex: 'currency', width: 80 },
    { title: '状态', dataIndex: 'status', width: 100, render: (value) => <StatusTag value={value} mapping={bmsStatus} /> },
  ], [])

  const chargeSourceColumns = useMemo(() => [
    { title: '来源号', dataIndex: 'sourceNo', width: 190, fixed: 'left' },
    { title: '来源系统', dataIndex: 'sourceSystem', width: 120 },
    { title: '来源事件', dataIndex: 'sourceEventId', width: 180 },
    { title: '计费对象', dataIndex: 'billingObjectCode', width: 150 },
    { title: '费用项', dataIndex: 'feeType', width: 120 },
    { title: '数量', dataIndex: 'quantity', width: 100 },
    { title: '账期', dataIndex: 'billingPeriod', width: 110 },
    { title: '状态', dataIndex: 'status', width: 110, render: (value) => <StatusTag value={value} mapping={bmsStatus} /> },
    { title: '失败原因', dataIndex: 'failureReason', width: 240, ellipsis: true },
  ], [])

  const summary = settlementSummaryQuery.data || {}

  return (
    <div className="fulfillment-settlement-page">
      <div className="page-heading">
        <div>
          <Typography.Title level={3}>履约、物流与结算</Typography.Title>
          <Typography.Text type="secondary">查看 OMS 履约、TMS 运输和 BMS 结算主链路</Typography.Text>
        </div>
        <Button icon={<ReloadOutlined />} onClick={refresh}>刷新</Button>
      </div>

      <Row gutter={[16, 16]} className="metric-row">
        <Col xs={12} xl={6}><Metric icon={<DeploymentUnitOutlined />} title="履约单" value={(fulfillmentsQuery.data || []).length} /></Col>
        <Col xs={12} xl={6}><Metric icon={<CarOutlined />} title="运输任务" value={(tasksQuery.data || []).length} /></Col>
        <Col xs={12} xl={6}><Metric icon={<WalletOutlined />} title="费用来源" value={(chargeSourcesQuery.data || []).length} /></Col>
        <Col xs={12} xl={6}><Metric icon={<DatabaseOutlined />} title="账单金额" value={summary.billAmount || 0} /></Col>
      </Row>

      <Tabs
        className="ops-tabs"
        items={[
          { key: 'omsChannelOrders', label: '渠道订单', children: <OpsTable title="渠道订单" rowKey="channelOrderNo" columns={channelOrderColumns} query={channelOrdersQuery} scroll={{ x: 720 }} /> },
          { key: 'omsSalesOrders', label: '销售订单', children: <OpsTable title="销售订单" rowKey="orderNo" columns={salesOrderColumns} query={salesOrdersQuery} scroll={{ x: 1060 }} /> },
          { key: 'omsFulfillments', label: '履约单', children: <OpsTable title="履约单" rowKey="fulfillmentNo" columns={fulfillmentColumns} query={fulfillmentsQuery} scroll={{ x: 1420 }} /> },
          { key: 'omsOutbounds', label: '出库单', children: <OpsTable title="出库单" rowKey="outboundNo" columns={outboundColumns} query={outboundsQuery} scroll={{ x: 1260 }} /> },
          { key: 'tmsTasks', label: '运输任务', children: <OpsTable title="运输任务" rowKey="taskNo" columns={taskColumns} query={tasksQuery} scroll={{ x: 1360 }} /> },
          { key: 'tmsWaybills', label: '运单', children: <OpsTable title="运单" rowKey="waybillNo" columns={waybillColumns} query={waybillsQuery} scroll={{ x: 1180 }} /> },
          { key: 'tmsExceptions', label: '物流异常', children: <OpsTable title="物流异常" rowKey="exceptionNo" columns={exceptionColumns} query={exceptionsQuery} scroll={{ x: 1320 }} /> },
          { key: 'tmsFees', label: '物流费用', children: <OpsTable title="物流费用来源" rowKey="feeSourceNo" columns={tmsFeeColumns} query={feeSourcesQuery} scroll={{ x: 1480 }} /> },
          { key: 'bmsObjects', label: '计费对象', children: <OpsTable title="计费对象" rowKey="objectCode" columns={billingObjectColumns} query={billingObjectsQuery} scroll={{ x: 760 }} /> },
          { key: 'bmsSources', label: 'BMS 费用来源', children: <OpsTable title="BMS 费用来源" rowKey="sourceNo" columns={chargeSourceColumns} query={chargeSourcesQuery} scroll={{ x: 1320 }} /> },
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
      <Table rowKey={rowKey} columns={columns} dataSource={query.data || []} loading={query.isLoading} scroll={scroll} pagination={{ pageSize: 8 }} />
    </div>
  )
}

function StatusTag({ value, mapping }) {
  const item = mapping[value] || [value, 'default']
  return <Tag color={item[1]}>{item[0]}</Tag>
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString()
}
