import { describe, expect, it } from 'vitest'
import { listEmptyText, listPaginationPatch, mergeListSearchParams, readListFilters } from './ResourceListPage'

describe('ResourceListPage URL state', () => {
  it('normalizes invalid paging values without discarding filters', () => {
    const filters = readListFilters(new URLSearchParams('pageNo=0&pageSize=abc&keyword=PO-1&status=2'))

    expect(filters).toEqual({ pageNo: 1, pageSize: 20, keyword: 'PO-1', status: '2' })
  })

  it('merges filter changes and removes empty values while preserving paging state', () => {
    const next = mergeListSearchParams(
      new URLSearchParams('pageNo=3&pageSize=50&keyword=old&status=2'),
      { keyword: '', status: '4', pageNo: 1 },
    )

    expect(next.toString()).toBe('pageNo=1&pageSize=50&status=4')
  })

  it('does not describe a failed request as an empty business list', () => {
    expect(listEmptyText({ isError: true })).toBe('数据加载失败，请重试')
    expect(listEmptyText({ isError: false })).toBe('暂无业务数据')
  })

  it('returns to the first page when the page size changes', () => {
    expect(listPaginationPatch({ pageNo: 4, pageSize: 20 }, 4, 50)).toEqual({ pageNo: 1, pageSize: 50 })
    expect(listPaginationPatch({ pageNo: 4, pageSize: 20 }, 3, 20)).toEqual({ pageNo: 3, pageSize: 20 })
  })
})
