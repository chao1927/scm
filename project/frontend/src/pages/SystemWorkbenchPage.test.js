import { describe, expect, it } from 'vitest'
import { metricQueryState, summarizeWorkbenchQueries } from './SystemWorkbenchPage'

describe('SystemWorkbenchPage query states', () => {
  it('reports partial failures separately from genuine empty results', () => {
    const state = summarizeWorkbenchQueries([
      { query: { isError: false, isLoading: false }, result: { records: [], total: 0 } },
      { query: { isError: true, isLoading: false }, result: { records: [], total: 0 } },
      { query: { isError: false, isLoading: false }, result: { records: [{ id: 1 }], total: 1 } },
    ])

    expect(state).toEqual({ failedCount: 1, loadingCount: 0, successfulCount: 2, hasSuccessfulData: true })
  })

  it('makes a failed metric card explicitly retryable', () => {
    expect(metricQueryState({ isError: true, isLoading: false }, { total: 0 })).toEqual({
      value: '—',
      hint: '接口加载失败，点击重试',
      action: 'retry',
    })
  })
})
