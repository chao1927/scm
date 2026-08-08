import { describe, expect, it } from 'vitest'
import { supplierResources } from './supplier'

describe('supplier resources', () => {
  it('keeps every supplier business page on a real query function', () => {
    expect(Object.keys(supplierResources)).toEqual([
      'supplier.profile',
      'supplier.user-bindings',
      'supplier.skus',
      'supplier.po-confirms',
      'supplier.asns',
      'supplier.returns',
      'supplier.reconciliations',
      'supplier.quality-issues',
      'supplier.rectifications',
      'supplier.scores',
      'supplier.operation-logs',
    ])

    Object.values(supplierResources).forEach((resource) => {
      expect(resource.query).toBeTypeOf('function')
      expect(resource.rowKey.length).toBeGreaterThan(0)
      expect(resource.columns.length).toBeGreaterThan(0)
    })
  })
})
