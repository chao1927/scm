import { createElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import { resourceDefinitions } from '../config/resourceDefinitions'
import { systemCatalog } from '../config/systemCatalog'
import WorkbenchPage, { buildCapabilitySnapshot } from './WorkbenchPage'

describe('WorkbenchPage capability snapshot', () => {
  it('derives registered resources and gaps from the catalog instead of completion estimates', () => {
    const systems = [{
      id: 'purchase',
      name: '采购系统',
      domain: '采购中心',
      pages: [
        { id: 'workbench', label: '工作台' },
        { id: 'orders', label: '采购订单' },
        { id: 'exceptions', label: '异常管理' },
      ],
    }]

    const snapshot = buildCapabilitySnapshot(systems, { 'purchase.orders': { query: () => {} } })

    expect(snapshot.totals).toEqual({ systems: 1, pages: 2, registeredResources: 1, pendingPages: 1 })
    expect(snapshot.rows[0]).toMatchObject({
      key: 'purchase',
      system: '采购系统',
      domain: '采购中心',
      pageCount: 2,
      registeredCount: 1,
      pendingCount: 1,
      registeredLabels: ['采购订单'],
      pendingLabels: ['异常管理'],
      workbenchPath: '/purchase/workbench',
    })
    expect(snapshot.rows[0]).not.toHaveProperty('progress')
    expect(snapshot.rows[0]).not.toHaveProperty('status')
    expect(snapshot.rows[0]).not.toHaveProperty('risk')
  })

  it('keeps the real nine-system catalog fully traceable to registered definitions', () => {
    const snapshot = buildCapabilitySnapshot(systemCatalog, resourceDefinitions)

    expect(snapshot.rows).toHaveLength(9)
    expect(snapshot.totals.pages).toBeGreaterThan(0)
    expect(snapshot.totals.registeredResources).toBeGreaterThan(0)
    expect(snapshot.totals.registeredResources + snapshot.totals.pendingPages).toBe(snapshot.totals.pages)
    expect(snapshot.rows.every((row) => row.registeredCount + row.pendingCount === row.pageCount)).toBe(true)
  })

  it('renders factual capability navigation without historical completion or risk claims', () => {
    const html = renderToStaticMarkup(createElement(WorkbenchPage, { onOpenAsn: () => {} }))

    expect(html).toContain('九系统资源接入情况')
    expect(html).toContain('系统能力导航')
    expect(html).not.toContain('完成度')
    expect(html).not.toContain('首轮完成')
    expect(html).not.toContain('P1 阻塞项')
    expect(html).not.toContain('最近推进')
  })
})
