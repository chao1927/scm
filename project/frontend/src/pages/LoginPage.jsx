import { LockOutlined, ShopOutlined, UserOutlined } from '@ant-design/icons'
import { Alert, Button, Form, Input, Typography } from 'antd'
import { useEffect, useRef } from 'react'

export default function LoginPage({ loading, error, onLogin }) {
  const errorRef = useRef(null)

  useEffect(() => {
    if (error) errorRef.current?.focus()
  }, [error])

  return (
    <main className="login-page">
      <section className="login-panel" aria-labelledby="login-title">
        <div className="login-brand"><ShopOutlined /> 供应链系统</div>
        <Typography.Title id="login-title" level={1}>登录运营平台</Typography.Title>
        <Typography.Paragraph type="secondary">使用 IAM 账号进入与授权范围匹配的业务工作台。</Typography.Paragraph>
        {error && (
          <div id="login-error" ref={errorRef} className="login-error" tabIndex={-1}>
            <Alert type="error" showIcon title="登录失败" description={error} />
          </div>
        )}
        <Form layout="vertical" requiredMark={false} onFinish={onLogin} aria-describedby={error ? 'login-error' : undefined}>
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input autoComplete="username" prefix={<UserOutlined />} placeholder="请输入 IAM 用户名" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password autoComplete="current-password" prefix={<LockOutlined />} placeholder="请输入密码" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block>登录</Button>
        </Form>
      </section>
    </main>
  )
}
