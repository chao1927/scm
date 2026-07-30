import { describe, expect, it } from 'vitest'
import { inventoryResources } from './inventory'

describe('inventory operational resources', () => {
  it('connects all five operational gap pages to real query definitions', () => {
    const keys = [
      'inventory.reservations',
      'inventory.freezes',
      'inventory.adjustments',
      'inventory.event-logs',
      'inventory.operation-logs',
    ]

    keys.forEach((key) => {
      expect(inventoryResources[key]).toBeDefined()
      expect(inventoryResources[key].query).toBeTypeOf('function')
      expect(inventoryResources[key].rowKey.length).toBeGreaterThan(0)
      expect(inventoryResources[key].columns.length).toBeGreaterThan(0)
      expect(inventoryResources[key].pageActions?.[0]?.key).toBe('export')
    })
  })

  it('offers asynchronous inventory metric exports from the stock page', () => {
    const actions = inventoryResources['inventory.balances'].pageActions

    expect(actions.map((action) => action.key)).toEqual([
      'dispatch',
      'export-book-physical',
      'export-stock-age',
      'export-slow-moving',
      'export-expiry',
    ])
  })
})
