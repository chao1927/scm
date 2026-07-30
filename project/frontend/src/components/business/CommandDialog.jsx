import { Form, Input, Modal } from 'antd'
import { useEffect } from 'react'

export default function CommandDialog({ action, record, open, loading, onCancel, onConfirm }) {
  const [form] = Form.useForm()

  useEffect(() => {
    if (open) form.setFieldsValue({ reason: '业务页面人工操作' })
  }, [form, open])

  return (
    <Modal
      title={action ? `确认${action.label}` : '确认业务操作'}
      open={open}
      okText="确认执行"
      cancelText="取消"
      confirmLoading={loading}
      okButtonProps={{ danger: action?.danger }}
      onCancel={onCancel}
      onOk={() => form.validateFields().then(({ reason }) => onConfirm(reason))}
      destroyOnHidden
    >
      <div className="command-impact">
        <div><span>目标对象</span><strong>{record?.__identity || '当前业务对象'}</strong></div>
        <div><span>业务命令</span><strong>{action?.label}</strong></div>
        <div><span>执行影响</span><strong>后端校验状态、版本、权限和业务不变量</strong></div>
      </div>
      <Form form={form} layout="vertical">
        <Form.Item name="reason" label="操作原因" rules={[{ required: true, whitespace: true, message: '请输入操作原因' }]}>
          <Input.TextArea rows={3} maxLength={200} showCount />
        </Form.Item>
      </Form>
    </Modal>
  )
}
