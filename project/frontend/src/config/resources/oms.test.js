import { describe, expect, it } from 'vitest'
import { omsResources } from './oms'

describe('oms resources', () => {
  it('connects fulfillment metrics and recoverable exports to the OMS workbench', () => {
    const definition = omsResources['oms.workbench']

    expect(definition.query).toBeTypeOf('function')
    expect(definition.columns.map((column) => column.key)).toContain('fulfillmentRate')
    expect(definition.actions.map((action) => action.key)).toEqual([
      'create-export',
      'retry-export',
      'download-export',
    ])
  })

  it('binds the six OMS operation gaps to list and detail queries', () => {
    const operationPages = [
      'oms.audit-results',
      'oms.reservations',
      'oms.cancel-requests',
      'oms.after-sales',
      'oms.exceptions',
      'oms.operation-logs',
    ]

    operationPages.forEach((key) => {
      expect(omsResources[key]?.query).toBeTypeOf('function')
      expect(omsResources[key]?.detail).toBeTypeOf('function')
      expect(omsResources[key]?.rowKey.length).toBeGreaterThan(0)
      expect(omsResources[key]?.columns.length).toBeGreaterThan(0)
    })
  })
})
