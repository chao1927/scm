import { Alert, Form, Input, Modal } from 'antd'
import { useEffect, useRef, useState } from 'react'

const DANGER_DESCRIPTION = '该操作可能不可逆，并会影响后续业务流程。请确认目标对象和操作原因后再执行。'

export function commandDialogAccessibility(action) {
  return {
    title: action ? `确认${action.label}` : '确认业务操作',
    titleId: 'command-dialog-title',
    descriptionId: action?.danger ? 'command-dialog-danger-description' : undefined,
    dangerDescription: action?.danger ? DANGER_DESCRIPTION : undefined,
  }
}

export default function CommandDialog({ action, record, open, loading, onCancel, onConfirm }) {
  const [form] = Form.useForm()
  const [submitError, setSubmitError] = useState(null)
  const submitErrorRef = useRef(null)
  const accessibility = commandDialogAccessibility(action)

  useEffect(() => {
    if (open) {
      form.setFieldsValue({ reason: '业务页面人工操作' })
      setSubmitError(null)
    }
  }, [form, open])

  const focusValidationError = (error) => {
    const fieldName = error?.errorFields?.[0]?.name
    if (fieldName) {
      form.scrollToField(fieldName, { focus: true })
    }
  }

  const handleConfirm = async () => {
    try {
      const { reason } = await form.validateFields()
      setSubmitError(null)
      await onConfirm(reason)
    } catch (error) {
      if (error?.errorFields) {
        focusValidationError(error)
        return
      }
      setSubmitError(error?.message || '操作提交失败，请检查后重试')
      queueMicrotask(() => submitErrorRef.current?.focus())
    }
  }

  return (
    <Modal
      title={<span id={accessibility.titleId}>{accessibility.title}</span>}
      aria-labelledby={accessibility.titleId}
      aria-describedby={accessibility.descriptionId}
      open={open}
      okText="确认执行"
      cancelText="取消"
      confirmLoading={loading}
      okButtonProps={{ danger: action?.danger }}
      onCancel={() => {
        setSubmitError(null)
        onCancel()
      }}
      onOk={handleConfirm}
      destroyOnHidden
    >
      {action?.danger && (
        <Alert
          id={accessibility.descriptionId}
          className="command-danger-description"
          type="warning"
          showIcon
          title="高风险业务命令"
          description={accessibility.dangerDescription}
        />
      )}
      {submitError && (
        <div ref={submitErrorRef} className="command-submit-error" tabIndex={-1}>
          <Alert type="error" showIcon title="操作未提交" description={submitError} />
        </div>
      )}
      <div className="command-impact">
        <div><span>目标对象</span><strong>{record?.__identity || '当前业务对象'}</strong></div>
        <div><span>业务命令</span><strong>{action?.label}</strong></div>
        <div><span>执行影响</span><strong>后端校验状态、版本、权限和业务不变量</strong></div>
      </div>
      <Form form={form} layout="vertical">
        <Form.Item name="reason" label="操作原因" rules={[{ required: true, whitespace: true, message: '请输入操作原因' }]}>
          <Input.TextArea rows={3} maxLength={200} showCount autoFocus />
        </Form.Item>
      </Form>
    </Modal>
  )
}
