import { describe, expect, it } from 'vitest'
import { findSystemPage, systemCatalog, systemsById } from './systemCatalog'
import { bmsSystem } from './systems/bms'
import { iamSystem } from './systems/iam'
import { inventorySystem } from './systems/inventory'
import { mdmSystem } from './systems/mdm'
import { omsSystem } from './systems/oms'
import { purchaseSystem } from './systems/purchase'
import { supplierSystem } from './systems/supplier'
import { tmsSystem } from './systems/tms'
import { wmsSystem } from './systems/wms'

describe('system catalog', () => {
  const independentSystems = [
    supplierSystem,
    purchaseSystem,
    wmsSystem,
    inventorySystem,
    omsSystem,
    tmsSystem,
    bmsSystem,
    mdmSystem,
    iamSystem,
  ]

  it('contains the nine independently navigable business systems', () => {
    expect(systemCatalog).toHaveLength(9)
    expect(new Set(systemCatalog.map((system) => system.id)).size).toBe(9)
    expect(systemsById.size).toBe(9)
  })

  it('aggregates the independent system modules in the established order', () => {
    expect(systemCatalog).toEqual(independentSystems)
    expect(systemCatalog.map((system) => system.id)).toEqual([
      'supplier',
      'purchase',
      'wms',
      'inventory',
      'oms',
      'tms',
      'bms',
      'mdm',
      'iam',
    ])
  })

  it('keeps the established system permissions in their owning modules', () => {
    expect(independentSystems.map((system) => system.permission)).toEqual([
      'supplier:*',
      'purchase:*',
      'wms:*',
      'inventory:*',
      'oms:*',
      'tms:*',
      'bms:*',
      'mdm:*',
      'iam:*',
    ])
  })

  it('uses a workbench as the first page of every system', () => {
    systemCatalog.forEach((system) => {
      expect(system.pages[0].id).toBe('workbench')
      expect(findSystemPage(system.id, 'workbench').id).toBe('workbench')
    })
  })

  it('keeps page identifiers unique inside each system', () => {
    systemCatalog.forEach((system) => {
      expect(new Set(system.pages.map((page) => page.id)).size).toBe(system.pages.length)
      expect(system.pages.every((page) => !Object.hasOwn(page, 'legacy'))).toBe(true)
    })
  })
})
