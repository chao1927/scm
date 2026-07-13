import { expect, test } from '@playwright/test'

test('allows all menus in development mode when no permission snapshot exists', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByRole('heading', { name: '供应链运营工作台' })).toBeVisible()
  await page.locator('.ant-menu-item').filter({ hasText: '采购中心' }).click()
  await expect(page.getByRole('heading', { name: '采购中心' })).toBeVisible()
})

test('disables unauthorized leaf menus when permission snapshot exists', async ({ page }) => {
  await page.addInitScript(() => {
    window.sessionStorage.setItem('access_token', 'test-token')
    window.sessionStorage.setItem('permission_codes', 'purchase:po:read')
  })

  await page.goto('/')

  const purchaseItem = page.locator('.ant-menu-item').filter({ hasText: '采购中心' })
  const permissionItem = page.locator('.ant-menu-item').filter({ hasText: '权限管理' })
  const integrationItem = page.locator('.ant-menu-item').filter({ hasText: '集成中心' })

  await expect(purchaseItem).not.toHaveClass(/ant-menu-item-disabled/)
  await expect(permissionItem).toHaveClass(/ant-menu-item-disabled/)
  await expect(integrationItem).toHaveClass(/ant-menu-item-disabled/)

  await purchaseItem.click()
  await expect(page.getByRole('heading', { name: '采购中心' })).toBeVisible()
})
