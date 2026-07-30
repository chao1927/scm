import { describe, expect, it } from 'vitest'
import { bmsResources } from './bms'

describe('bms resources', () => {
  it('binds all eight finance and settlement pages to real read models', () => {
    const pages = [
      'bms.charge-details',
      'bms.billing-rules',
      'bms.reconciliations',
      'bms.bills',
      'bms.invoices',
      'bms.finance-handoffs',
      'bms.refunds',
      'bms.settlement-reports',
    ]

    pages.forEach((key) => {
      expect(bmsResources[key]?.query).toBeTypeOf('function')
      expect(bmsResources[key]?.rowKey.length).toBeGreaterThan(0)
      expect(bmsResources[key]?.columns.length).toBeGreaterThan(0)
    })
  })

  it('exposes failed external and report tasks as recoverable actions', () => {
    expect(bmsResources['bms.finance-handoffs'].actions[0].visible({
      externalTaskNo: 'BEXT-1',
      externalTaskStatus: 4,
    })).toBe(true)
    expect(bmsResources['bms.refunds'].actions[0].visible({
      externalTaskNo: 'BEXT-2',
      externalTaskStatus: 4,
    })).toBe(true)
    expect(bmsResources['bms.settlement-reports'].actions[1].visible({
      __rowKind: 'export',
      status: 5,
    })).toBe(true)
  })
})
