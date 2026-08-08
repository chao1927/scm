import { Alert, Button, Result, Skeleton, Space, Tag, Typography } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'

const statusColors = {
  草稿: 'default',
  待处理: 'gold',
  待审批: 'gold',
  审批中: 'processing',
  处理中: 'processing',
  已完成: 'green',
  已确认: 'green',
  已启用: 'green',
  已发布: 'blue',
  已取消: 'default',
  已关闭: 'default',
  失败: 'red',
  异常: 'red',
  驳回: 'red',
  拒绝: 'red',
}

export function BusinessPageHeader({ eyebrow, title, description, actions }) {
  return (
    <header className="business-page-header">
      <div>
        {eyebrow && <Typography.Text className="page-eyebrow">{eyebrow}</Typography.Text>}
        <Typography.Title level={2}>{title}</Typography.Title>
        {description && <Typography.Paragraph type="secondary">{description}</Typography.Paragraph>}
      </div>
      {actions && <Space wrap className="business-page-actions">{actions}</Space>}
    </header>
  )
}

export function StatusTag({ value }) {
  const text = value === undefined || value === null || value === '' ? '未知' : String(value)
  const matched = Object.entries(statusColors).find(([keyword]) => text.includes(keyword))
  return <Tag color={matched?.[1] || (Number.isFinite(Number(value)) ? 'blue' : 'default')}>{text}</Tag>
}

export function LoadingBlock({ rows = 6 }) {
  return <div className="content-state" aria-busy="true" aria-label="页面加载中"><Skeleton active paragraph={{ rows }} /></div>
}

export function QueryError({ error, onRetry }) {
  const message = error?.message || error?.detail || '接口请求失败'
  return (
    <Result
      status="error"
      title="数据加载失败"
      subTitle={message}
      extra={<Button icon={<ReloadOutlined />} onClick={onRetry}>重新加载</Button>}
    />
  )
}

export function InlineQueryError({ error, onRetry, retrying = false }) {
  const detail = error?.message || error?.detail || '接口请求失败'
  return (
    <Alert
      type="error"
      showIcon
      role="alert"
      title="数据加载失败"
      description={detail}
      action={<Button icon={<ReloadOutlined />} loading={retrying} onClick={onRetry}>重新加载</Button>}
    />
  )
}

export function CapabilityPending({ system, page }) {
  return (
    <>
      <BusinessPageHeader eyebrow={system.domain} title={page.label} description={system.goal} />
      <Alert
        type="info"
        showIcon
        message="页面结构已就绪，后端读模型尚未接入"
        description={`当前功能设计已纳入 ${system.name} 菜单，但现有前端 API 或后端列表接口尚不能提供 ${page.label} 的真实业务数据。本页面不会使用原型假数据。`}
      />
    </>
  )
}

export function formatCellValue(value, definition = {}) {
  if (value === undefined || value === null || value === '') return '—'
  if (definition.date) {
    const date = new Date(value)
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString('zh-CN', { hour12: false })
  }
  if (definition.number) {
    const number = Number(value)
    return Number.isFinite(number) ? number.toLocaleString('zh-CN', { maximumFractionDigits: 4 }) : String(value)
  }
  if (Array.isArray(value)) return value.join('、')
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}
