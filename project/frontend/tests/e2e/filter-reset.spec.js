import { expect, test } from '@playwright/test'

test.beforeEach(async ({ page }) => {
  await page.route('**/api/**', (route) => {
    const pathname = new URL(route.request().url()).pathname
    if (!pathname.startsWith('/api/')) return route.continue()
    return route.fulfill({
      json: { success: true, data: { pageNo: 1, pageSize: 20, total: 0, records: [] } },
    })
  })
  await page.route('**/api/iam/v1/auth/login', (route) => route.fulfill({
    json: { success: true, data: { accessToken: 'filter-token', refreshToken: 'filter-refresh' } },
  }))
  await page.route('**/api/iam/v1/me', (route) => route.fulfill({
    json: { success: true, data: { id: 21, username: 'qa-filter', status: 1 } },
  }))
  await page.route('**/openapi/iam/v1/users/me/permissions', (route) => route.fulfill({
    json: { userId: 21, permissionPayload: '*' },
  }))
  await page.route('**/api/iam/v1/menus?*', (route) => route.fulfill({ json: [] }))

  await page.goto('/')
  await page.getByLabel('用户名').fill('qa-filter')
  await page.getByLabel('密码').fill('password')
  await page.locator('button[type="submit"]').click()
  await expect(page.getByRole('heading', { name: '供应链运营工作台' })).toBeVisible()
})

test('reset immediately clears an unsubmitted keyword draft on every shared resource list', async ({ page }) => {
  await page.goto('/inventory/operation-logs')
  const searchInput = page.getByPlaceholder('输入编号、对象或负责人')

  await searchInput.fill('123123123')
  await page.getByRole('button', { name: /重\s*置/ }).click()

  await expect(searchInput).toHaveValue('')
  await expect(page).not.toHaveURL(/keyword=/)
})

test('reset clears both a submitted keyword and the visible input', async ({ page }) => {
  await page.goto('/supplier/asns')
  const searchInput = page.getByPlaceholder('输入编号、对象或负责人')

  await searchInput.fill('ASN-001')
  await searchInput.press('Enter')
  await expect(page).toHaveURL(/keyword=ASN-001/)
  await page.getByRole('button', { name: /重\s*置/ }).click()

  await expect(searchInput).toHaveValue('')
  await expect(page).not.toHaveURL(/keyword=/)
})
