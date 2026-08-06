import { LockOutlined, SafetyCertificateOutlined, ShopOutlined, UserOutlined } from '@ant-design/icons'
import { Alert, Button, Form, Input, Select, Typography } from 'antd'
import { useEffect, useRef } from 'react'

export default function LoginPage({ loading, error, mfa, onLogin, onMfa }) {
  const errorRef = useRef(null)

  useEffect(() => {
    if (error) errorRef.current?.focus()
  }, [error])

  return (
    <main className="login-page">
      <section className="login-panel" aria-labelledby="login-title">
        <div className="login-brand"><ShopOutlined /> 供应链系统</div>
        <Typography.Title id="login-title" level={1}>登录运营平台</Typography.Title>
        <Typography.Paragraph type="secondary">{mfa ? '账号密码已验证，请完成与当前会话绑定的二次认证。' : '使用 IAM 账号进入与授权范围匹配的业务工作台。'}</Typography.Paragraph>
        {error && (
          <div id="login-error" ref={errorRef} className="login-error" tabIndex={-1}>
            <Alert type="error" showIcon title="登录失败" description={error} />
          </div>
        )}
        {mfa ? (
          <Form layout="vertical" requiredMark={false} onFinish={onMfa} aria-describedby={error ? 'login-error' : undefined}>
            <Alert type="info" showIcon title="需要 MFA 验证" description={`挑战编号：${mfa.challengeNo}`} />
            <Form.Item name="method" label="验证方式" initialValue="TOTP">
              <Select options={[{ value: 'TOTP', label: 'TOTP 验证码' }, { value: 'RECOVERY_CODE', label: '恢复码' }]} />
            </Form.Item>
            <Form.Item name="code" label="验证码" rules={[{ required: true, message: '请输入验证码或恢复码' }]}>
              <Input autoComplete="one-time-code" prefix={<SafetyCertificateOutlined />} placeholder="请输入 MFA 验证码" />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>完成验证</Button>
          </Form>
        ) : (
          <Form layout="vertical" requiredMark={false} onFinish={onLogin} aria-describedby={error ? 'login-error' : undefined}>
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input autoComplete="username" prefix={<UserOutlined />} placeholder="请输入 IAM 用户名" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password autoComplete="current-password" prefix={<LockOutlined />} placeholder="请输入密码" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block>登录</Button>
          </Form>
        )}
      </section>
    </main>
  )
}
