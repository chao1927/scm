import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Button, DatePicker, Drawer, Form, Input, InputNumber, message, Modal, Select, Space, Table, Tag, Typography } from 'antd'
import { PlusOutlined, ReloadOutlined, SendOutlined, StopOutlined } from '@ant-design/icons'
import { cancelAsn, createAsn, queryAsns, submitAsn } from '../api/asn'

const statusOptions = [
  { value: 1, label: '草稿', color: 'default' }, { value: 2, label: '已提交', color: 'processing' },
  { value: 3, label: '已预约', color: 'cyan' }, { value: 4, label: '已发货', color: 'blue' },
  { value: 5, label: '已到仓', color: 'gold' }, { value: 6, label: '已收货', color: 'green' },
  { value: 7, label: '已取消', color: 'red' }, { value: 8, label: '已关闭', color: 'default' },
]

export default function AsnPage() {
  const [filters, setFilters] = useState({ pageNo: 1, pageSize: 20 })
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [form] = Form.useForm()
  const queryClient = useQueryClient()
  const query = useQuery({ queryKey: ['asns', filters], queryFn: () => queryAsns(filters) })
  const refresh = () => queryClient.invalidateQueries({ queryKey: ['asns'] })
  const createMutation = useMutation({ mutationFn: createAsn, onSuccess: () => { message.success('ASN 草稿已创建'); setDrawerOpen(false); form.resetFields(); refresh() } })
  const submitMutation = useMutation({ mutationFn: ({ id, version }) => submitAsn(id, version), onSuccess: () => { message.success('ASN 已提交'); refresh() } })
  const cancelMutation = useMutation({ mutationFn: ({ id, reason, version }) => cancelAsn(id, reason, version), onSuccess: () => { message.success('ASN 已取消'); refresh() } })

  const columns = useMemo(() => [
    { title: 'ASN 单号', dataIndex: 'asnNo', width: 190, fixed: 'left' },
    { title: '采购订单 ID', dataIndex: 'purchaseOrderId', width: 140 },
    { title: '供应商 ID', dataIndex: 'supplierId', width: 120 },
    { title: '目的仓 ID', dataIndex: 'warehouseId', width: 120 },
    { title: '预计到仓', dataIndex: 'estimatedArrivalAt', width: 190, render: (value) => new Date(value).toLocaleString() },
    { title: '状态', dataIndex: 'status', width: 100, render: (value, record) => <Tag color={statusOptions.find((item) => item.value === value)?.color}>{record.statusName}</Tag> },
    { title: '操作', key: 'actions', width: 190, fixed: 'right', render: (_, record) => <Space>
      {record.status === 1 && <Button type="link" icon={<SendOutlined />} onClick={() => submitMutation.mutate({ id: record.asnId, version: record.version })}>提交</Button>}
      {[1, 2, 3, 4].includes(record.status) && <Button type="link" danger icon={<StopOutlined />} onClick={() => Modal.confirm({ title: '取消 ASN', content: '取消后不可恢复，已发货单据还会产生运输取消待办。', onOk: () => cancelMutation.mutate({ id: record.asnId, reason: '人工取消', version: record.version }) })}>取消</Button>}
    </Space> },
  ], [submitMutation, cancelMutation])

  const submitCreate = (values) => createMutation.mutate({
    purchaseOrderId: values.purchaseOrderId,
    supplierId: values.supplierId,
    warehouseId: values.warehouseId,
    estimatedArrivalAt: values.estimatedArrivalAt.toISOString(),
    lines: [{ skuCode: values.skuCode, plannedQuantity: values.plannedQuantity, batchNo: values.batchNo || null }],
  })

  return <>
    <div className="page-heading"><div><Typography.Title level={3}>ASN 发货通知</Typography.Title><Typography.Text type="secondary">管理供应商发货计划、预约到仓和发运状态</Typography.Text></div><Button type="primary" icon={<PlusOutlined />} onClick={() => setDrawerOpen(true)}>新建 ASN</Button></div>
    <div className="filter-bar"><Space wrap>
      <Input.Search allowClear placeholder="ASN 单号" style={{ width: 220 }} onSearch={(keyword) => setFilters((value) => ({ ...value, keyword, pageNo: 1 }))} />
      <Select allowClear placeholder="状态" options={statusOptions} style={{ width: 140 }} onChange={(status) => setFilters((value) => ({ ...value, status, pageNo: 1 }))} />
      <Button icon={<ReloadOutlined />} onClick={() => query.refetch()}>刷新</Button>
    </Space></div>
    <Table rowKey="asnId" columns={columns} dataSource={query.data?.data?.records || []} loading={query.isLoading} scroll={{ x: 1050 }} pagination={{ current: filters.pageNo, pageSize: filters.pageSize, total: query.data?.data?.total || 0, showSizeChanger: true, showTotal: (total) => `共 ${total} 条`, onChange: (pageNo, pageSize) => setFilters((value) => ({ ...value, pageNo, pageSize })) }} />
    <Drawer title="新建 ASN 草稿" width={520} open={drawerOpen} onClose={() => setDrawerOpen(false)} extra={<Button type="primary" loading={createMutation.isPending} onClick={() => form.submit()}>保存草稿</Button>}>
      <Form form={form} layout="vertical" onFinish={submitCreate}>
        <Form.Item name="purchaseOrderId" label="采购订单 ID" rules={[{ required: true }]}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="supplierId" label="供应商 ID" rules={[{ required: true }]}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="warehouseId" label="目的仓 ID" rules={[{ required: true }]}><InputNumber min={1} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="estimatedArrivalAt" label="预计到仓时间" rules={[{ required: true }]}><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="skuCode" label="SKU 编码" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="plannedQuantity" label="计划发货数量" rules={[{ required: true }]}><InputNumber min={0.0001} precision={4} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="batchNo" label="批次号"><Input /></Form.Item>
      </Form>
    </Drawer>
  </>
}
