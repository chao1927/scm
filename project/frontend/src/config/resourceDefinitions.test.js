import { describe, expect, it } from 'vitest'
import { findResourceDefinition, normalizePage, recordIdentity } from './resourceDefinitions'

describe('resource definitions', () => {
  it('normalizes the supported backend page shapes', () => {
    expect(normalizePage({ data: { records: [{ id: 1 }], total: 7, pageNo: 2, pageSize: 10 } })).toEqual({
      records: [{ id: 1 }],
      total: 7,
      pageNo: 2,
      pageSize: 10,
    })
    expect(normalizePage({ content: [{ id: 2 }], totalElements: 3, number: 0, size: 25 })).toEqual({
      records: [{ id: 2 }],
      total: 3,
      pageNo: 1,
      pageSize: 25,
    })
  })

  it('falls back without producing NaN for non-page responses', () => {
    const result = normalizePage([{ id: 1 }], { pageNo: 3, pageSize: 50 })
    expect(result.pageNo).toBe(3)
    expect(result.pageSize).toBe(50)
    expect(result.total).toBe(1)
  })

  it('uses technical id for purchase requisition detail navigation', () => {
    const definition = findResourceDefinition('purchase', 'requisitions')
    expect(recordIdentity(definition, { id: 18, requisitionNo: 'PR-0018' })).toBe('18')
  })
})
