import { describe, expect, it } from 'vitest'
import { mdmResources } from './mdm'

describe('mdm resources', () => {
  it('binds approval and change log pages to real read models', () => {
    ;['mdm.approvals', 'mdm.change-logs'].forEach((key) => {
      expect(mdmResources[key]?.query).toBeTypeOf('function')
      expect(mdmResources[key]?.rowKey.length).toBeGreaterThan(0)
      expect(mdmResources[key]?.columns.length).toBeGreaterThan(0)
    })
  })
})
