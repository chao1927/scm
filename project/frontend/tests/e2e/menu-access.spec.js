import { expect, test } from '@playwright/test'

const browserErrors = new WeakMap()

test.beforeEach(async ({ page }) => {
  const errors = []
  browserErrors.set(page, errors)
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text())
  })
  page.on('pageerror', (error) => errors.push(error.message))
})

test.afterEach(async ({ page }) => {
  expect(browserErrors.get(page)).toEqual([])
})

const menus = [
  { menuCode: 'purchase-requisitions', menuName: '业务请购单', status: 1, sortNo: 10 },
  { menuCode: 'purchase-orders', menuName: '采购订单', status: 1, sortNo: 20 },
]

async function mockIam(page, permissionPayload) {
  await page.route('**/api/iam/v1/auth/login', (route) => route.fulfill({ json: { success: true, data: { accessToken: 'api-token', refreshToken: 'refresh-token' } } }))
  await page.route('**/api/iam/v1/me', (route) => route.fulfill({ json: { success: true, data: { id: 1, username: 'operator', status: 1 } } }))
  await page.route('**/openapi/iam/v1/users/me/permissions', (route) => route.fulfill({ json: { userId: 1, permissionPayload } }))
  await page.route('**/api/iam/v1/menus?*', (route) => route.fulfill({ json: menus }))
}

async function login(page) {
  await page.goto('/')
  await page.getByLabel('用户名').fill('operator')
  await page.getByLabel('密码').fill('password')
  await page.locator('button[type="submit"]').click()
  await expect(page.getByRole('heading', { name: '供应链运营工作台' })).toBeVisible()
}

test('navigates from system workbench to a real list and detail', async ({ page }) => {
  await mockIam(page, 'purchase:po:read')
  const requisition = {
    id: 18,
    requisitionNo: 'PR-20260728-0018',
    applicantId: 'U-1001',
    purchaseOrgId: 'ORG-01',
    demandDepartmentId: 'D-09',
    status: 2,
    statusName: '审批中',
    reason: '门店补货',
    version: 3,
    lines: [{ id: 181, skuCode: 'SKU-001', requestedQuantity: 24 }],
  }
  await page.route('**/api/purchase/v1/**', (route) => {
    const pathname = new URL(route.request().url()).pathname
    if (pathname.endsWith('/requisitions/18')) {
      return route.fulfill({ json: { success: true, data: requisition } })
    }
    if (pathname.endsWith('/requisitions')) {
      return route.fulfill({ json: { success: true, data: { pageNo: 1, pageSize: 20, total: 1, records: [requisition] } } })
    }
    return route.fulfill({ json: { success: true, data: { pageNo: 1, pageSize: 20, total: 0, records: [] } } })
  })
  await login(page)

  const desktopSystems = page.getByRole('navigation', { name: '供应链子系统导航' })
  await desktopSystems.getByRole('button', { name: '采购管理', exact: true }).click()
  await expect(page.getByRole('heading', { name: '采购管理工作台', exact: true })).toBeVisible()
  await page.locator('.ant-menu-item').filter({ hasText: '业务请购单' }).click()
  await expect(page.getByRole('heading', { name: '请购管理', exact: true })).toBeVisible()
  await page.getByRole('button', { name: 'PR-20260728-0018' }).click()
  await expect(page.getByRole('heading', { name: 'PR-20260728-0018', exact: true })).toBeVisible()
  await expect(page.getByText('门店补货')).toBeVisible()
  await expect(page.getByRole('tab', { name: '明细信息' })).toBeVisible()
  await expect(page.getByRole('button', { name: '批准' })).toHaveCount(0)
  await expect(page.getByRole('button', { name: '驳回' })).toHaveCount(0)
})

test('disables systems outside the IAM permission snapshot', async ({ page }) => {
  await mockIam(page, 'purchase:po:read')
  await page.route('**/api/purchase/v1/**', (route) => route.fulfill({ json: { success: true, data: { pageNo: 1, pageSize: 20, total: 0, records: [] } } }))
  await login(page)

  const desktopSystems = page.getByRole('navigation', { name: '供应链子系统导航' })
  await expect(desktopSystems.getByRole('button', { name: '采购管理', exact: true })).toBeEnabled()
  await expect(desktopSystems.getByRole('button', { name: '供应商协同', exact: true })).toBeDisabled()
  await expect(desktopSystems.getByRole('button', { name: '权限中心', exact: true })).toBeDisabled()
})

test('keeps system navigation usable on a 320px viewport', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 720 })
  await mockIam(page, 'purchase:po:read')
  await page.route('**/api/purchase/v1/**', (route) => route.fulfill({ json: { success: true, data: { pageNo: 1, pageSize: 20, total: 0, records: [] } } }))
  await login(page)

  const mobileNavigation = page.getByRole('navigation', { name: '移动端子系统' })
  await expect(mobileNavigation).toBeVisible()
  await mobileNavigation.getByRole('button', { name: /采购/ }).click()
  await expect(page.getByRole('heading', { name: '采购管理工作台' })).toBeVisible()
  await expect(page.getByRole('button', { name: '打开功能菜单' })).toBeVisible()
})

test('supports the planned tablet and desktop breakpoints', async ({ page }) => {
  await mockIam(page, 'purchase:po:read')
  await page.route('**/api/purchase/v1/**', (route) => route.fulfill({ json: { success: true, data: { pageNo: 1, pageSize: 20, total: 0, records: [] } } }))
  await login(page)

  await page.setViewportSize({ width: 768, height: 900 })
  await expect(page.getByRole('navigation', { name: '移动端子系统' })).toBeVisible()
  await expect(page.getByRole('button', { name: '打开功能菜单' })).toBeVisible()

  for (const width of [1024, 1440]) {
    await page.setViewportSize({ width, height: 900 })
    await expect(page.locator('.system-rail')).toBeVisible()
    await expect(page.locator('.module-sider')).toBeVisible()
    await expect(page.getByRole('navigation', { name: '移动端子系统' })).toBeHidden()
  }
})
