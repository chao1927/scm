import { DatePicker, Drawer, Form, Input, InputNumber, Button, message } from 'antd'
import { useMutation } from '@tanstack/react-query'
import { createAsn } from '../../api/asn'

export default function AsnCreateDrawer({ open, onClose, onCreated }) {
  const [form] = Form.useForm()
  const mutation = useMutation({
    mutationFn: createAsn,
    onSuccess: () => {
      message.success('ASN 草稿已创建')
      form.resetFields()
      onCreated()
    },
    onError: (error) => message.error(error?.message || 'ASN 创建失败'),
  })

  const submit = (values) => mutation.mutate({
    purchaseOrderId: values.purchaseOrderId,
    supplierId: values.supplierId,
    warehouseId: values.warehouseId,
    estimatedArrivalAt: values.estimatedArrivalAt.toISOString(),
    lines: [{
      skuCode: values.skuCode,
      plannedQuantity: values.plannedQuantity,
      batchNo: values.batchNo || null,
    }],
  })

  return (
    <Drawer
      title="新建 ASN 草稿"
      size={520}
      open={open}
      onClose={onClose}
      extra={<Button type="primary" loading={mutation.isPending} onClick={() => form.submit()}>保存草稿</Button>}
    >
      <Form form={form} layout="vertical" onFinish={submit}>
        <Form.Item name="purchaseOrderId" label="采购订单 ID" rules={[{ required: true, message: '请输入采购订单 ID' }]}>
          <InputNumber min={1} className="full-width-control" />
        </Form.Item>
        <Form.Item name="supplierId" label="供应商 ID" rules={[{ required: true, message: '请输入供应商 ID' }]}>
          <InputNumber min={1} className="full-width-control" />
        </Form.Item>
        <Form.Item name="warehouseId" label="目的仓 ID" rules={[{ required: true, message: '请输入目的仓 ID' }]}>
          <InputNumber min={1} className="full-width-control" />
        </Form.Item>
        <Form.Item name="estimatedArrivalAt" label="预计到仓时间" rules={[{ required: true, message: '请选择预计到仓时间' }]}>
          <DatePicker showTime className="full-width-control" />
        </Form.Item>
        <Form.Item name="skuCode" label="SKU 编码" rules={[{ required: true, whitespace: true, message: '请输入 SKU 编码' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="plannedQuantity" label="计划发货数量" rules={[{ required: true, message: '请输入计划发货数量' }]}>
          <InputNumber min={0.0001} precision={4} className="full-width-control" />
        </Form.Item>
        <Form.Item name="batchNo" label="批次号"><Input /></Form.Item>
      </Form>
    </Drawer>
  )
}
