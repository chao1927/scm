import { bmsSystem } from './systems/bms'
import { iamSystem } from './systems/iam'
import { inventorySystem } from './systems/inventory'
import { mdmSystem } from './systems/mdm'
import { omsSystem } from './systems/oms'
import { purchaseSystem } from './systems/purchase'
import { supplierSystem } from './systems/supplier'
import { tmsSystem } from './systems/tms'
import { wmsSystem } from './systems/wms'

export const systemCatalog = [
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

export const systemsById = new Map(systemCatalog.map((system) => [system.id, system]))

export function findSystem(systemId) {
  return systemsById.get(systemId) || systemCatalog[0]
}

export function findSystemPage(systemId, pageId) {
  const system = findSystem(systemId)
  return system.pages.find((item) => item.id === pageId) || system.pages[0]
}
