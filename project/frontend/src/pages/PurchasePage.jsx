import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Button, Input, Modal, Select, Space, Table, Tabs, Tag, Tooltip, Typography, message } from 'antd'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  FileDoneOutlined,
  PlayCircleOutlined,
  ReloadOutlined,
  RetweetOutlined,
  SendOutlined,
  StopOutlined,
  SyncOutlined,
} from '@ant-design/icons'
import {
  acceptSupplierConfirmDiff,
  approvePurchaseOrder,
  approveRequisition,
  cancelPurchaseOrder,
  cancelSupplierConfirmOrder,
  closePurchaseOrder,
  closeRfq,
  publishPurchaseOrder,
  publishRfq,
  queryInbounds,
  queryPurchaseFailedEvents,
  queryPurchaseOrders,
  queryRequisitions,
  queryRfqs,
  querySupplierConfirms,
  rejectRequisition,
  renegotiateSupplierConfirm,
  replayPurchaseFailedEvent,
  submitPurchaseOrder,
  submitRequisition,
} from '../api/purchase'

const reqStatus = {
  1: ['草稿', 'default'],
  2: ['审批中', 'blue'],
  3: ['已批准', 'green'],
  4: ['已驳回', 'red'],
  5: ['部分转采购', 'gold'],
  6: ['已转采购', 'green'],
  7: ['已关闭', 'default'],
}

const rfqStatus = {
  1: ['草稿', 'default'],
  2: ['已发布', 'blue'],
  3: ['报价中', 'processing'],
  4: ['已截标', 'gold'],
  5: ['已定标', 'green'],
  6: ['已取消', 'red'],
  7: ['已关闭', 'default'],
}

const poStatus = {
  1: ['草稿', 'default'],
  2: ['待审批', 'blue'],
  3: ['已审批', 'cyan'],
  4: ['待供应商确认', 'gold'],
  5: ['供应商已确认', 'green'],
  6: ['供应商差异', 'orange'],
  7: ['部分入库', 'processing'],
  8: ['已完成', 'green'],
  9: ['已取消', 'red'],
  10: ['已关闭', 'default'],
  11: ['已驳回', 'red'],
  12: ['供应商已拒绝', 'red'],
}

const inboundStatus = {
  1: ['ASN已记录', 'blue'],
  2: ['在途', 'processing'],
  3: ['已到仓', 'cyan'],
  4: ['已收货', 'gold'],
  5: ['已质检', 'purple'],
  6: ['已上架', 'green'],
  7: ['异常', 'red'],
}

const processedStatus = {
  1: ['待处理', 'gold'],
  2: ['已接受差异', 'green'],
  3: ['协商中', 'blue'],
  4: ['已取消订单', 'red'],
}

const pageDefaults = { pageNo: 1, pageSize: 20 }
const defaultReason = '采购运营页面处理'

export default function PurchasePage() {
  const [requisitionFilters, setRequisitionFilters] = useState(pageDefaults)
  const [rfqFilters, setRfqFilters] = useState(pageDefaults)
  const [orderFilters, setOrderFilters] = useState(pageDefaults)
  const [inboundFilters, setInboundFilters] = useState(pageDefaults)
  const [confirmFilters, setConfirmFilters] = useState(pageDefaults)
  const queryClient = useQueryClient()

  const requisitionsQuery = useQuery({ queryKey: ['purchase-requisitions', requisitionFilters], queryFn: () => queryRequisitions(requisitionFilters) })
  const rfqsQuery = useQuery({ queryKey: ['purchase-rfqs', rfqFilters], queryFn: () => queryRfqs(rfqFilters) })
  const ordersQuery = useQuery({ queryKey: ['purchase-orders', orderFilters], queryFn: () => queryPurchaseOrders(orderFilters) })
  const inboundsQuery = useQuery({ queryKey: ['purchase-inbounds', inboundFilters], queryFn: () => queryInbounds(inboundFilters) })
  const confirmsQuery = useQuery({ queryKey: ['purchase-confirms', confirmFilters], queryFn: () => querySupplierConfirms(confirmFilters) })
  const failedEventsQuery = useQuery({ queryKey: ['purchase-failed-events'], queryFn: queryPurchaseFailedEvents })

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['purchase-requisitions'] })
    queryClient.invalidateQueries({ queryKey: ['purchase-rfqs'] })
    queryClient.invalidateQueries({ queryKey: ['purchase-orders'] })
    queryClient.invalidateQueries({ queryKey: ['purchase-inbounds'] })
    queryClient.invalidateQueries({ queryKey: ['purchase-confirms'] })
    queryClient.invalidateQueries({ queryKey: ['purchase-failed-events'] })
  }

  const actionMutation = useMutation({
    mutationFn: (action) => action(),
    onSuccess: () => {
      message.success('操作已完成')
      refresh()
    },
  })

  const requisitionColumns = useMemo(() => [
    { title: '请购单号', dataIndex: 'requisitionNo', width: 190, fixed: 'left' },
    { title: '申请人', dataIndex: 'applicantId', width: 100 },
    { title: '采购组织', dataIndex: 'purchaseOrgId', width: 110 },
    { title: '需求部门', dataIndex: 'demandDepartmentId', width: 110 },
    { title: '状态', dataIndex: 'status', width: 100, render: (value, row) => <StatusTag value={value} label={row.statusName} mapping={reqStatus} /> },
    { title: '行数', dataIndex: 'lines', width: 80, render: (lines = []) => lines.length },
    { title: '原因', dataIndex: 'reason', width: 220, ellipsis: true },
    { title: '更新时间', dataIndex: 'updatedAt', width: 180, render: formatDateTime },
    {
      title: '操作',
      key: 'actions',
      width: 150,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          {row.status === 1 && <IconAction title="提交" icon={<SendOutlined />} onClick={() => confirmAction('提交请购单', () => submitRequisition(row.id, row.version), actionMutation)} />}
          {row.status === 2 && <IconAction title="批准" icon={<CheckCircleOutlined />} onClick={() => approveRequisitionRow(row, actionMutation)} />}
          {row.status === 2 && <IconAction danger title="驳回" icon={<CloseCircleOutlined />} onClick={() => confirmAction('驳回请购单', () => rejectRequisition(row.id, row.version, defaultReason), actionMutation)} />}
        </Space>
      ),
    },
  ], [actionMutation])

  const rfqColumns = useMemo(() => [
    { title: 'RFQ 单号', dataIndex: 'rfqNo', width: 190, fixed: 'left' },
    { title: '类型', dataIndex: 'rfqType', width: 80 },
    { title: '采购组织', dataIndex: 'purchaseOrgId', width: 110 },
    { title: '品类', dataIndex: 'categoryCode', width: 120 },
    { title: '来源请购', dataIndex: 'sourceRequisitionNo', width: 160 },
    { title: '报价截止', dataIndex: 'quoteDeadline', width: 180, render: formatDateTime },
    { title: '邀请供应商', dataIndex: 'invitedSupplierCount', width: 110 },
    { title: '状态', dataIndex: 'status', width: 100, render: (value, row) => <StatusTag value={value} label={row.statusName} mapping={rfqStatus} /> },
    {
      title: '操作',
      key: 'actions',
      width: 130,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          {row.status === 1 && <IconAction title="发布" icon={<SendOutlined />} onClick={() => confirmAction('发布 RFQ', () => publishRfq(row.rfqNo, row.version), actionMutation)} />}
          {[2, 3].includes(row.status) && <IconAction title="截标" icon={<FileDoneOutlined />} onClick={() => confirmAction('RFQ 截标', () => closeRfq(row.rfqNo, row.version, defaultReason), actionMutation)} />}
        </Space>
      ),
    },
  ], [actionMutation])

  const orderColumns = useMemo(() => [
    { title: '采购订单', dataIndex: 'orderNo', width: 190, fixed: 'left' },
    { title: '供应商', dataIndex: 'supplierName', width: 180, ellipsis: true },
    { title: '采购组织', dataIndex: 'purchaseOrgId', width: 110 },
    { title: '仓库', dataIndex: 'warehouseCode', width: 110 },
    { title: '币种', dataIndex: 'currency', width: 80 },
    { title: '含税金额', dataIndex: 'taxIncludedAmount', width: 120 },
    { title: '状态', dataIndex: 'status', width: 130, render: (value, row) => <StatusTag value={value} label={row.statusName} mapping={poStatus} /> },
    {
      title: '操作',
      key: 'actions',
      width: 190,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          {row.status === 1 && <IconAction title="提交" icon={<SendOutlined />} onClick={() => confirmAction('提交采购订单', () => submitPurchaseOrder(row.orderNo, row.version), actionMutation)} />}
          {row.status === 2 && <IconAction title="审批通过" icon={<CheckCircleOutlined />} onClick={() => confirmAction('审批采购订单', () => approvePurchaseOrder(row.orderNo, row.version), actionMutation)} />}
          {row.status === 3 && <IconAction title="发布" icon={<PlayCircleOutlined />} onClick={() => confirmAction('发布采购订单', () => publishPurchaseOrder(row.orderNo, row.version), actionMutation)} />}
          {[1, 2, 3, 4, 6, 12].includes(row.status) && <IconAction danger title="取消" icon={<StopOutlined />} onClick={() => confirmAction('取消采购订单', () => cancelPurchaseOrder(row.orderNo, row.version, defaultReason), actionMutation)} />}
          {[5, 6, 7, 12].includes(row.status) && <IconAction title="关闭剩余" icon={<FileDoneOutlined />} onClick={() => confirmAction('关闭采购订单剩余数量', () => closePurchaseOrder(row.orderNo, row.version, defaultReason), actionMutation)} />}
        </Space>
      ),
    },
  ], [actionMutation])

  const inboundColumns = useMemo(() => [
    { title: '到货单', dataIndex: 'inboundNo', width: 180, fixed: 'left' },
    { title: '采购订单', dataIndex: 'orderNo', width: 180 },
    { title: 'ASN', dataIndex: 'asnNo', width: 180 },
    { title: '供应商', dataIndex: 'supplierId', width: 100 },
    { title: '仓库', dataIndex: 'warehouseCode', width: 110 },
    { title: 'SKU', dataIndex: 'skuCode', width: 130 },
    { title: '通知', dataIndex: 'notifiedQty', width: 90 },
    { title: '收货', dataIndex: 'receivedQty', width: 90 },
    { title: '合格', dataIndex: 'qualifiedQty', width: 90 },
    { title: '上架', dataIndex: 'putawayQty', width: 90 },
    { title: '状态', dataIndex: 'status', width: 120, render: (value, row) => <StatusTag value={value} label={row.statusName} mapping={inboundStatus} /> },
    { title: '异常原因', dataIndex: 'exceptionReason', width: 220, ellipsis: true },
  ], [])

  const confirmColumns = useMemo(() => [
    { title: '确认 ID', dataIndex: 'confirmId', width: 110, fixed: 'left' },
    { title: '订单号', dataIndex: 'orderNo', width: 180 },
    { title: '供应商', dataIndex: 'supplierId', width: 100 },
    { title: '事件', dataIndex: 'eventCode', width: 220 },
    { title: '确认状态', dataIndex: 'confirmStatus', width: 130 },
    { title: '处理状态', dataIndex: 'processedStatus', width: 130, render: (value, row) => <StatusTag value={value} label={row.processedStatusName} mapping={processedStatus} /> },
    { title: '原因', dataIndex: 'reason', width: 220, ellipsis: true },
    { title: '发生时间', dataIndex: 'occurredAt', width: 180, render: formatDateTime },
    {
      title: '操作',
      key: 'actions',
      width: 160,
      fixed: 'right',
      render: (_, row) => row.processedStatus === 1 && (
        <Space>
          <IconAction title="接受差异" icon={<CheckCircleOutlined />} onClick={() => confirmAction('接受供应商差异', () => acceptSupplierConfirmDiff(row.confirmId, row.version, defaultReason), actionMutation)} />
          <IconAction title="重新协商" icon={<SyncOutlined />} onClick={() => confirmAction('发起重新协商', () => renegotiateSupplierConfirm(row.confirmId, row.version, '请供应商重新确认数量、价格或交期', defaultReason), actionMutation)} />
          <IconAction danger title="取消订单" icon={<StopOutlined />} onClick={() => confirmAction('按供应商差异取消订单', () => cancelSupplierConfirmOrder(row.confirmId, row.version, defaultReason), actionMutation)} />
        </Space>
      ),
    },
  ], [actionMutation])

  const failedEventColumns = useMemo(() => [
    { title: '事件 ID', dataIndex: 'id', width: 100, fixed: 'left' },
    { title: '来源系统', dataIndex: 'sourceSystem', width: 120 },
    { title: '事件编码', dataIndex: 'eventCode', width: 220 },
    { title: '事件类型', dataIndex: 'eventType', width: 220 },
    { title: '消费者', dataIndex: 'consumerName', width: 180 },
    { title: '重试次数', dataIndex: 'retryCount', width: 100 },
    { title: '失败原因', dataIndex: 'reason', width: 260, ellipsis: true },
    { title: '更新时间', dataIndex: 'updatedAt', width: 180, render: formatDateTime },
    {
      title: '操作',
      key: 'actions',
      width: 100,
      fixed: 'right',
      render: (_, row) => <IconAction title="重放" icon={<RetweetOutlined />} onClick={() => confirmAction('重放失败事件', () => replayPurchaseFailedEvent(row.id, defaultReason), actionMutation)} />,
    },
  ], [actionMutation])

  return (
    <div className="purchase-page">
      <div className="page-heading">
        <div>
          <Typography.Title level={3}>采购中心</Typography.Title>
          <Typography.Text type="secondary">跟踪请购、询价、采购订单、到货和供应商确认差异</Typography.Text>
        </div>
        <Button icon={<ReloadOutlined />} onClick={refresh}>刷新</Button>
      </div>

      <Tabs
        className="ops-tabs"
        items={[
          {
            key: 'requisitions',
            label: '请购',
            children: (
              <OpsTableSection title="请购单" action={<PurchaseFilters statusMap={reqStatus} filters={requisitionFilters} onChange={setRequisitionFilters} keyword />}>
                <PagedTable rowKey="id" query={requisitionsQuery} columns={requisitionColumns} filters={requisitionFilters} onChange={setRequisitionFilters} scroll={{ x: 1260 }} />
              </OpsTableSection>
            ),
          },
          {
            key: 'rfqs',
            label: 'RFQ',
            children: (
              <OpsTableSection title="询价单" action={<PurchaseFilters statusMap={rfqStatus} filters={rfqFilters} onChange={setRfqFilters} />}>
                <PagedTable rowKey="rfqNo" query={rfqsQuery} columns={rfqColumns} filters={rfqFilters} onChange={setRfqFilters} scroll={{ x: 1260 }} />
              </OpsTableSection>
            ),
          },
          {
            key: 'orders',
            label: '采购订单',
            children: (
              <OpsTableSection title="采购订单" action={<PurchaseFilters statusMap={poStatus} filters={orderFilters} onChange={setOrderFilters} />}>
                <PagedTable rowKey="orderNo" query={ordersQuery} columns={orderColumns} filters={orderFilters} onChange={setOrderFilters} scroll={{ x: 1380 }} />
              </OpsTableSection>
            ),
          },
          {
            key: 'inbounds',
            label: '到货',
            children: (
              <OpsTableSection title="到货跟踪" action={<PurchaseFilters statusMap={inboundStatus} filters={inboundFilters} onChange={setInboundFilters} orderNo asnNo />}>
                <PagedTable rowKey="inboundNo" query={inboundsQuery} columns={inboundColumns} filters={inboundFilters} onChange={setInboundFilters} scroll={{ x: 1440 }} />
              </OpsTableSection>
            ),
          },
          {
            key: 'confirms',
            label: '供应商确认',
            children: (
              <OpsTableSection title="供应商确认差异" action={<PurchaseFilters statusMap={processedStatus} statusKey="processedStatus" filters={confirmFilters} onChange={setConfirmFilters} orderNo />}>
                <PagedTable rowKey="confirmId" query={confirmsQuery} columns={confirmColumns} filters={confirmFilters} onChange={setConfirmFilters} scroll={{ x: 1460 }} />
              </OpsTableSection>
            ),
          },
          {
            key: 'failedEvents',
            label: '失败事件',
            children: (
              <OpsTableSection title="采购 Inbox 失败事件">
                <Table rowKey="id" columns={failedEventColumns} dataSource={failedEventsQuery.data?.data || []} loading={failedEventsQuery.isLoading} scroll={{ x: 1360 }} pagination={{ pageSize: 8 }} />
              </OpsTableSection>
            ),
          },
        ]}
      />
    </div>
  )
}

function PurchaseFilters({ statusMap, statusKey = 'status', filters, onChange, keyword, orderNo, asnNo }) {
  const options = Object.entries(statusMap).map(([value, item]) => ({ value: Number(value), label: item[0] }))
  const update = (patch) => onChange((current) => ({ ...current, ...patch, pageNo: 1 }))
  return (
    <Space wrap>
      {keyword && <Input.Search allowClear placeholder="关键字" style={{ width: 180 }} onSearch={(value) => update({ keyword: value || undefined })} />}
      {orderNo && <Input.Search allowClear placeholder="采购订单号" style={{ width: 180 }} onSearch={(value) => update({ orderNo: value || undefined })} />}
      {asnNo && <Input.Search allowClear placeholder="ASN 单号" style={{ width: 180 }} onSearch={(value) => update({ asnNo: value || undefined })} />}
      <Select allowClear placeholder="状态" options={options} value={filters[statusKey]} style={{ width: 150 }} onChange={(value) => update({ [statusKey]: value })} />
    </Space>
  )
}

function PagedTable({ rowKey, query, columns, filters, onChange, scroll }) {
  const page = query.data?.data || {}
  return (
    <Table
      rowKey={rowKey}
      columns={columns}
      dataSource={page.records || []}
      loading={query.isLoading}
      scroll={scroll}
      pagination={{
        current: filters.pageNo,
        pageSize: filters.pageSize,
        total: page.total || 0,
        showSizeChanger: true,
        showTotal: (total) => `共 ${total} 条`,
        onChange: (pageNo, pageSize) => onChange((current) => ({ ...current, pageNo, pageSize })),
      }}
    />
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

function StatusTag({ value, label, mapping }) {
  const item = mapping[value] || [label || value, 'default']
  return <Tag color={item[1]}>{label || item[0]}</Tag>
}

function IconAction({ title, icon, danger, onClick }) {
  return (
    <Tooltip title={title}>
      <Button type="text" danger={danger} icon={icon} onClick={onClick} />
    </Tooltip>
  )
}

function confirmAction(title, action, mutation) {
  Modal.confirm({
    title,
    content: '操作会推进业务状态并写入后端审计与事件记录。',
    onOk: () => mutation.mutate(action),
  })
}

function approveRequisitionRow(row, mutation) {
  const approvedQuantities = Object.fromEntries((row.lines || []).map((line) => [line.lineId, line.requestedQty]))
  confirmAction('批准请购单', () => approveRequisition(row.id, row.version, approvedQuantities), mutation)
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString()
}
