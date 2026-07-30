import { bmsResources } from './resources/bms'
import { iamResources } from './resources/iam'
import { inventoryResources } from './resources/inventory'
import { mdmResources } from './resources/mdm'
import { omsResources } from './resources/oms'
import { purchaseResources } from './resources/purchase'
import { supplierResources } from './resources/supplier'
import { tmsResources } from './resources/tms'
import { wmsResources } from './resources/wms'

export { normalizePage, recordIdentity } from './resources/helpers'

export const resourceDefinitions = {
  ...supplierResources,
  ...purchaseResources,
  ...wmsResources,
  ...inventoryResources,
  ...omsResources,
  ...tmsResources,
  ...bmsResources,
  ...mdmResources,
  ...iamResources,
}

export function resourceKey(systemId, pageId) {
  return `${systemId}.${pageId}`
}

export function findResourceDefinition(systemId, pageId) {
  return resourceDefinitions[resourceKey(systemId, pageId)]
}
