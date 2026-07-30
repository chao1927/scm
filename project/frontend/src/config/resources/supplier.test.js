import { describe, expect, it } from 'vitest'
import { supplierResources } from './supplier'

describe('supplier resources', () => {
  it('keeps the six supplier pages on real query functions', () => {
    expect(Object.keys(supplierResources)).toEqual([
      'supplier.profile',
      'supplier.po-confirms',
      'supplier.asns',
      'supplier.reconciliations',
      'supplier.quality-issues',
      'supplier.rectifications',
    ])

    Object.values(supplierResources).forEach((resource) => {
      expect(resource.query).toBeTypeOf('function')
      expect(resource.rowKey.length).toBeGreaterThan(0)
      expect(resource.columns.length).toBeGreaterThan(0)
    })
  })
})
