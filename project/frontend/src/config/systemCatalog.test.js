import { describe, expect, it } from 'vitest'
import { findSystemPage, systemCatalog, systemsById } from './systemCatalog'

describe('system catalog', () => {
  it('contains the nine independently navigable business systems', () => {
    expect(systemCatalog).toHaveLength(9)
    expect(new Set(systemCatalog.map((system) => system.id)).size).toBe(9)
    expect(systemsById.size).toBe(9)
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
    })
  })
})
