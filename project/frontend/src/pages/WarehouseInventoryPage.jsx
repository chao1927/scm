import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Button, Descriptions, InputNumber, Modal, Space, Table, Tabs, Tag, Tooltip, Typography, message } from 'antd'
import {
  CheckCircleOutlined,
  CloudSyncOutlined,
  DatabaseOutlined,
  FileDoneOutlined,
  ReloadOutlined,
  RetweetOutlined,
  SyncOutlined,
} from '@ant-design/icons'
import {
  confirmInventoryReconciliation,
  dispatchInventoryOutbox,
  dispatchWmsOutbox,
  generateInventorySnapshot,
  queryInventoryLedgers,
  queryInventoryReconciliations,
  queryInventorySnapshots,
  queryInventoryStocks,
  queryWmsInboxFailedEvents,
  queryWmsOutboxFailedEvents,
  replayWmsInboxFailedEvent,
  retryWmsOutboxFailedEvent,
} from '../api/warehouseInventory'

export default function WarehouseInventoryPage() {
  const [limit, setLimit] = useState(50)
  const queryClient = useQueryClient()

  const wmsOutboxQuery = useQuery({ queryKey: ['wms-outbox-failed', limit], queryFn: () => queryWmsOutboxFailedEvents({ limit }) })
  const wmsInboxQuery = useQuery({ queryKey: ['wms-inbox-failed', limit], queryFn: () => queryWmsInboxFailedEvents({ limit }) })
  const stocksQuery = useQuery({ queryKey: ['inventory-stocks', limit], queryFn: () => queryInventoryStocks({ limit }) })
  const ledgersQuery = useQuery({ queryKey: ['inventory-ledgers', limit], queryFn: () => queryInventoryLedgers({ limit }) })
  const snapshotsQuery = useQuery({ queryKey: ['inventory-snapshots', limit], queryFn: () => queryInventorySnapshots({ limit }) })
  const reconcilesQuery = useQuery({ queryKey: ['inventory-reconciles', limit], queryFn: () => queryInventoryReconciliations({ limit }) })

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['wms-outbox-failed'] })
    queryClient.invalidateQueries({ queryKey: ['wms-inbox-failed'] })
    queryClient.invalidateQueries({ queryKey: ['inventory-stocks'] })
    queryClient.invalidateQueries({ queryKey: ['inventory-ledgers'] })
    queryClient.invalidateQueries({ queryKey: ['inventory-snapshots'] })
    queryClient.invalidateQueries({ queryKey: ['inventory-reconciles'] })
  }

  const actionMutation = useMutation({
    mutationFn: (action) => action(),
    onSuccess: () => {
      message.success('操作已完成')
      refresh()
    },
  })

  const dispatchWmsMutation = useMutation({
    mutationFn: dispatchWmsOutbox,
    onSuccess: (result) => {
      message.success(`WMS 投递完成：成功 ${result.data?.published ?? result.published}，失败 ${result.data?.failed ?? result.failed}`)
      refresh()
    },
  })

  const dispatchInventoryMutation = useMutation({
    mutationFn: dispatchInventoryOutbox,
    onSuccess: (result) => {
      message.success(`库存投递完成：成功 ${result.data?.published ?? result.published}，失败 ${result.data?.failed ?? result.failed}`)
      refresh()
    },
  })

  const wmsOutboxColumns = useMemo(() => [
    { title: '事件 ID', dataIndex: 'id', width: 100, fixed: 'left' },
    { title: '事件编码', dataIndex: 'code', width: 220 },
    { title: '事件类型', dataIndex: 'type', width: 220 },
    { title: '聚合类型', dataIndex: 'aggregateType', width: 160 },
    { title: '聚合 ID', dataIndex: 'aggregateId', width: 160 },
    { title: '版本', dataIndex: 'version', width: 80 },
    { title: '重试次数', dataIndex: 'retryCount', width: 100 },
    {
      title: '操作',
      key: 'actions',
      width: 90,
      fixed: 'right',
      render: (_, row) => <IconAction title="重试 Outbox" icon={<RetweetOutlined />} onClick={() => confirmAction('重试 WMS Outbox 事件', () => retryWmsOutboxFailedEvent(row.id), actionMutation)} />,
    },
  ], [actionMutation])

  const wmsInboxColumns = useMemo(() => [
    { title: 'Inbox ID', dataIndex: 'id', width: 100, fixed: 'left' },
    { title: '来源系统', dataIndex: 'sourceSystem', width: 130 },
    { title: '事件编码', dataIndex: 'eventCode', width: 220 },
    { title: '事件类型', dataIndex: 'eventType', width: 220 },
    { title: '重试次数', dataIndex: 'retryCount', width: 100 },
    { title: '最后错误', dataIndex: 'lastError', width: 320, ellipsis: true },
    {
      title: '操作',
      key: 'actions',
      width: 90,
      fixed: 'right',
      render: (_, row) => <IconAction title="重放 Inbox" icon={<CloudSyncOutlined />} onClick={() => confirmAction('重放 WMS Inbox 事件', () => replayWmsInboxFailedEvent(row.id), actionMutation)} />,
    },
  ], [actionMutation])

  const stockColumns = useMemo(() => [
    { title: '库存账户', dataIndex: 'id', width: 110, fixed: 'left' },
    { title: '货主', dataIndex: 'ownerId', width: 90 },
    { title: '仓库', dataIndex: 'warehouseId', width: 90 },
    { title: 'SKU', dataIndex: 'sku', width: 150 },
    { title: '批次', dataIndex: 'batchNo', width: 120 },
    { title: '在手', dataIndex: 'onHandQty', width: 100 },
    { title: '可用', dataIndex: 'availableQty', width: 100 },
    { title: '预占', dataIndex: 'reservedQty', width: 100 },
    { title: '冻结', dataIndex: 'frozenQty', width: 100 },
    { title: '版本', dataIndex: 'version', width: 80 },
    {
      title: '操作',
      key: 'actions',
      width: 100,
      fixed: 'right',
      render: (_, row) => <IconAction title="生成快照" icon={<DatabaseOutlined />} onClick={() => confirmAction('生成库存快照', () => generateInventorySnapshot(row.id), actionMutation)} />,
    },
  ], [actionMutation])

  const ledgerColumns = useMemo(() => [
    { title: '流水号', dataIndex: 'ledgerNo', width: 190, fixed: 'left' },
    { title: '账户 ID', dataIndex: 'accountId', width: 100 },
    { title: '类型', dataIndex: 'type', width: 140, render: (value) => <Tag color="blue">{value}</Tag> },
    { title: '数量变化', dataIndex: 'qtyDelta', width: 120 },
    { title: '来源系统', dataIndex: 'sourceSystem', width: 120 },
    { title: '来源单号', dataIndex: 'sourceNo', width: 200 },
  ], [])

  const snapshotColumns = useMemo(() => [
    { title: '快照号', dataIndex: 'snapshotNo', width: 190, fixed: 'left' },
    { title: '账户 ID', dataIndex: 'accountId', width: 100 },
    { title: '在手数量', dataIndex: 'onHandQty', width: 120 },
    { title: '可用数量', dataIndex: 'availableQty', width: 120 },
  ], [])

  const reconcileColumns = useMemo(() => [
    { title: '对账单号', dataIndex: 'reconcileNo', width: 190, fixed: 'left' },
    { title: '账户 ID', dataIndex: 'accountId', width: 100 },
    { title: '系统数量', dataIndex: 'systemQty', width: 120 },
    { title: 'WMS 数量', dataIndex: 'wmsQty', width: 120 },
    { title: '差异', dataIndex: 'differenceQty', width: 120 },
    { title: '状态', dataIndex: 'status', width: 100, render: (value) => value === 1 ? <Tag color="gold">待确认</Tag> : <Tag color="green">已确认</Tag> },
    { title: '版本', dataIndex: 'version', width: 80 },
    {
      title: '操作',
      key: 'actions',
      width: 90,
      fixed: 'right',
      render: (_, row) => row.status === 1 && <IconAction title="确认对账" icon={<CheckCircleOutlined />} onClick={() => confirmAction('确认库存对账', () => confirmInventoryReconciliation(row.reconcileNo, row.version), actionMutation)} />,
    },
  ], [actionMutation])

  return (
    <div className="warehouse-inventory-page">
      <div className="page-heading">
        <div>
          <Typography.Title level={3}>仓储与库存</Typography.Title>
          <Typography.Text type="secondary">处理 WMS 事件可靠性、库存余额、流水、快照和对账</Typography.Text>
        </div>
        <Space wrap>
          <InputNumber min={1} max={200} value={limit} onChange={(value) => setLimit(value || 50)} addonBefore="查询上限" />
          <Button icon={<ReloadOutlined />} onClick={refresh}>刷新</Button>
        </Space>
      </div>

      <div className="section-band">
        <div className="ops-toolbar">
          <Space wrap>
            <Button icon={<SyncOutlined />} loading={dispatchWmsMutation.isPending} onClick={() => dispatchWmsMutation.mutate(limit)}>投递 WMS Outbox</Button>
            <Button icon={<SyncOutlined />} loading={dispatchInventoryMutation.isPending} onClick={() => dispatchInventoryMutation.mutate(limit)}>投递库存 Outbox</Button>
          </Space>
          <Descriptions size="small" column={2} className="dispatch-result">
            <Descriptions.Item label="WMS 失败事件">{(wmsOutboxQuery.data?.data || []).length + (wmsInboxQuery.data?.data || []).length}</Descriptions.Item>
            <Descriptions.Item label="库存账户">{(stocksQuery.data?.data || []).length}</Descriptions.Item>
          </Descriptions>
        </div>
      </div>

      <Tabs
        className="ops-tabs"
        items={[
          {
            key: 'wmsOutbox',
            label: 'WMS Outbox',
            children: <OpsTableSection title="WMS Outbox 失败事件"><Table rowKey="id" columns={wmsOutboxColumns} dataSource={wmsOutboxQuery.data?.data || []} loading={wmsOutboxQuery.isLoading} scroll={{ x: 1150 }} pagination={{ pageSize: 8 }} /></OpsTableSection>,
          },
          {
            key: 'wmsInbox',
            label: 'WMS Inbox',
            children: <OpsTableSection title="WMS Inbox 失败事件"><Table rowKey="id" columns={wmsInboxColumns} dataSource={wmsInboxQuery.data?.data || []} loading={wmsInboxQuery.isLoading} scroll={{ x: 1160 }} pagination={{ pageSize: 8 }} /></OpsTableSection>,
          },
          {
            key: 'stocks',
            label: '库存余额',
            children: <OpsTableSection title="库存余额"><Table rowKey="id" columns={stockColumns} dataSource={stocksQuery.data?.data || []} loading={stocksQuery.isLoading} scroll={{ x: 1180 }} pagination={{ pageSize: 8 }} /></OpsTableSection>,
          },
          {
            key: 'ledgers',
            label: '库存流水',
            children: <OpsTableSection title="库存流水"><Table rowKey="ledgerNo" columns={ledgerColumns} dataSource={ledgersQuery.data?.data || []} loading={ledgersQuery.isLoading} scroll={{ x: 900 }} pagination={{ pageSize: 8 }} /></OpsTableSection>,
          },
          {
            key: 'snapshots',
            label: '快照',
            children: <OpsTableSection title="库存快照"><Table rowKey="snapshotNo" columns={snapshotColumns} dataSource={snapshotsQuery.data?.data || []} loading={snapshotsQuery.isLoading} scroll={{ x: 620 }} pagination={{ pageSize: 8 }} /></OpsTableSection>,
          },
          {
            key: 'reconciles',
            label: '对账',
            children: <OpsTableSection title="库存对账"><Table rowKey="reconcileNo" columns={reconcileColumns} dataSource={reconcilesQuery.data?.data || []} loading={reconcilesQuery.isLoading} scroll={{ x: 900 }} pagination={{ pageSize: 8 }} /></OpsTableSection>,
          },
        ]}
      />
    </div>
  )
}

function OpsTableSection({ title, children }) {
  return (
    <div className="section-band">
      <div className="section-title">
        <Typography.Title level={4}>{title}</Typography.Title>
      </div>
      {children}
    </div>
  )
}

function IconAction({ title, icon, onClick }) {
  return (
    <Tooltip title={title}>
      <Button type="text" icon={icon} onClick={onClick} />
    </Tooltip>
  )
}

function confirmAction(title, action, mutation) {
  Modal.confirm({
    title,
    content: '操作会更新后端状态并刷新当前页面数据。',
    onOk: () => mutation.mutate(action),
  })
}
