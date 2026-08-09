import { describe, expect, it } from 'vitest'
import {
  findResourceDefinition,
  normalizePage,
  recordIdentity,
  resourceDefinitions,
} from './resourceDefinitions'
import { bmsResources } from './resources/bms'
import { iamResources } from './resources/iam'
import { inventoryResources } from './resources/inventory'
import { mdmResources } from './resources/mdm'
import { omsResources } from './resources/oms'
import { purchaseResources } from './resources/purchase'
import { supplierResources } from './resources/supplier'
import { tmsResources } from './resources/tms'
import { wmsResources } from './resources/wms'
import { systemCatalog } from './systemCatalog'

describe('resource definitions', () => {
  const resourcesBySystem = {
    supplier: supplierResources,
    purchase: purchaseResources,
    wms: wmsResources,
    inventory: inventoryResources,
    oms: omsResources,
    tms: tmsResources,
    bms: bmsResources,
    mdm: mdmResources,
    iam: iamResources,
  }

  it('keeps every resource definition in its owning system module', () => {
    Object.entries(resourcesBySystem).forEach(([systemId, definitions]) => {
      Object.entries(definitions).forEach(([key, definition]) => {
        expect(key.startsWith(`${systemId}.`)).toBe(true)
        expect(resourceDefinitions[key]).toBe(definition)
      })
    })
  })

  it('aggregates exactly the resources exported by the nine systems', () => {
    expect(resourceDefinitions).toEqual(Object.assign({}, ...Object.values(resourcesBySystem)))
    expect(Object.fromEntries(
      Object.entries(resourcesBySystem).map(([systemId, definitions]) => [
        systemId,
        Object.keys(definitions).length,
      ]),
    )).toEqual({
      supplier: 11,
      purchase: 13,
      wms: 15,
      inventory: 10,
      oms: 11,
      tms: 9,
      bms: 11,
      mdm: 11,
      iam: 14,
    })
  })

  it('connects every catalog business page to a real resource definition', () => {
    const missingPages = systemCatalog.flatMap((system) => system.pages
      .filter((page) => page.id !== 'workbench')
      .map((page) => `${system.id}.${page.id}`)
      .filter((key) => !resourceDefinitions[key]))

    expect(missingPages).toEqual([])
  })

  it('gives every catalog business page an executable read-model query', () => {
    const missingQueries = systemCatalog.flatMap((system) => system.pages
      .filter((page) => page.id !== 'workbench')
      .map((page) => `${system.id}.${page.id}`)
      .filter((key) => typeof resourceDefinitions[key]?.query !== 'function'))

    expect(missingQueries).toEqual([])
  })

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
