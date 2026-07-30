import { renderToStaticMarkup } from 'react-dom/server'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import ApplicationLayout from './ApplicationLayout'

describe('ApplicationLayout accessibility', () => {
  it('names navigation landmarks and exposes the current page context', () => {
    const html = renderToStaticMarkup(
      <MemoryRouter initialEntries={['/supplier/workbench']}>
        <Routes>
          <Route
            path="/:systemId/:pageId"
            element={<ApplicationLayout session={{ menus: [], permissions: ['*'], user: { username: 'admin' } }} onLogout={() => {}} />}
          >
            <Route index element={<p>工作台内容</p>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    )

    expect(html).toContain('aria-label="供应链子系统导航"')
    expect(html).toContain('aria-label="供应商协同功能导航"')
    expect(html).toContain('aria-label="面包屑"')
    expect(html).toContain('aria-labelledby="current-page-title"')
    expect(html).toContain('id="current-page-title"')
    expect(html).toContain('供应商协同 · 供应商工作台')
  })
})
