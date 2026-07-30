import { expect, test } from '@playwright/test'

const systems = [
  ['供应商协同', '供应商协同工作台'],
  ['采购管理', '采购管理工作台'],
  ['仓储执行', '仓储执行工作台'],
  ['中央库存', '中央库存工作台'],
  ['订单履约', '订单履约工作台'],
  ['运输管理', '运输管理工作台'],
  ['计费结算', '计费结算工作台'],
  ['主数据', '主数据工作台'],
  ['权限中心', '权限中心工作台'],
]

const browserErrors = new WeakMap()

test.beforeEach(async ({ page }) => {
  const errors = []
  browserErrors.set(page, errors)
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text())
  })
  page.on('pageerror', (error) => errors.push(error.message))
  await mockAuthenticatedShell(page)
})

test.afterEach(async ({ page }) => {
  expect(browserErrors.get(page)).toEqual([])
})

test('navigates all nine system workbenches through the shared desktop rail', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  await login(page)

  for (const [systemName, workbenchTitle] of systems) {
    await page.getByRole('button', { name: systemName, exact: true }).click()
    await expect(page.getByRole('heading', { name: workbenchTitle })).toBeVisible()
  }
})

test('supports keyboard Tab and Enter navigation in the shared desktop shell', async ({ page }) => {
  await page.setViewportSize({ width: 1024, height: 900 })
  await login(page)

  await page.getByRole('button', { name: '打开平台工作台' }).focus()
  await page.keyboard.press('Tab')
  await expect(page.getByRole('button', { name: '供应商协同', exact: true })).toBeFocused()
  await page.keyboard.press('Enter')
  await expect(page.getByRole('heading', { name: '供应商协同工作台' })).toBeVisible()
})

test('keeps the shared shell usable at 320, 768, 1024 and 1440 widths', async ({ page }) => {
  await login(page)

  for (const width of [320, 768]) {
    await page.setViewportSize({ width, height: 900 })
    const mobileNavigation = page.getByRole('navigation', { name: '移动端子系统' })
    await expect(mobileNavigation).toBeVisible()
    await expect(page.getByRole('button', { name: '打开功能菜单' })).toBeVisible()
    await mobileNavigation.getByRole('button', { name: /采购/ }).click()
    await expect(page.getByRole('heading', { name: '采购管理工作台' })).toBeVisible()
  }

  for (const width of [1024, 1440]) {
    await page.setViewportSize({ width, height: 900 })
    await expect(page.locator('.system-rail')).toBeVisible()
    await expect(page.locator('.module-sider')).toBeVisible()
    await expect(page.getByRole('navigation', { name: '移动端子系统' })).toBeHidden()
  }
})

async function mockAuthenticatedShell(page) {
  await page.route('**/api/**', (route) => route.fulfill({
    json: { success: true, data: { pageNo: 1, pageSize: 20, total: 0, records: [] } },
  }))
  await page.route('**/api/iam/v1/auth/login', (route) => route.fulfill({
    json: { success: true, data: { accessToken: 'shell-token', refreshToken: 'shell-refresh' } },
  }))
  await page.route('**/api/iam/v1/me', (route) => route.fulfill({
    json: { success: true, data: { id: 9, username: 'qa-shell', status: 1 } },
  }))
  await page.route('**/openapi/iam/v1/users/me/permissions', (route) => route.fulfill({
    json: { userId: 9, permissionPayload: '*' },
  }))
  await page.route('**/api/iam/v1/menus?*', (route) => route.fulfill({ json: [] }))
}

async function login(page) {
  await page.goto('/')
  await page.getByLabel('用户名').fill('qa-shell')
  await page.getByLabel('密码').fill('password')
  await page.locator('button[type="submit"]').click()
  await expect(page.getByRole('heading', { name: '供应链运营工作台' })).toBeVisible()
}
