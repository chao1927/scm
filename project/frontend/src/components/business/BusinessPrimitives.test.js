import { createElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import { InlineQueryError } from './BusinessPrimitives'

describe('InlineQueryError', () => {
  it('renders an explicit alert and retry action outside an empty table', () => {
    const html = renderToStaticMarkup(createElement(InlineQueryError, { error: new Error('读模型超时'), onRetry: () => {} }))

    expect(html).toContain('role="alert"')
    expect(html).toContain('数据加载失败')
    expect(html).toContain('读模型超时')
    expect(html).toContain('重新加载')
  })
})
