import { describe, expect, it } from 'vitest'
import { tmsResources } from './tms'

describe('tms resources', () => {
  it('binds five standard TMS pages to real query functions', () => {
    const standardPages = [
      'tms.labels',
      'tms.tracks',
      'tms.signatures',
      'tms.carriers',
      'tms.operation-logs',
    ]

    standardPages.forEach((key) => {
      expect(tmsResources[key]?.query).toBeTypeOf('function')
      expect(tmsResources[key]?.rowKey.length).toBeGreaterThan(0)
      expect(tmsResources[key]?.columns.length).toBeGreaterThan(0)
    })
  })
})
