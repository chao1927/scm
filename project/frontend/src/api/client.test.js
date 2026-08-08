import { describe, expect, it } from 'vitest'
import { normalizeApiError } from './client'

describe('normalizeApiError', () => {
  it('distinguishes forbidden responses from network failures', () => {
    expect(normalizeApiError({ response: { status: 403 } })).toEqual({
      message: '无权访问当前功能',
      status: 403,
    })
  })

  it('preserves a business error returned by the backend', () => {
    expect(normalizeApiError({ response: { status: 400, data: { message: '业务校验失败' } } }))
      .toEqual({ message: '业务校验失败' })
  })

  it('reports a real connection failure clearly', () => {
    expect(normalizeApiError({})).toEqual({
      message: '网络请求失败，请检查服务是否已启动',
      status: null,
    })
  })
})
