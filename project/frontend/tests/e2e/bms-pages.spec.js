import { expect, test } from '@playwright/test'

test('opens all eight BMS finance pages on real resource definitions', async ({ page }) => {
  const browserErrors = []
  page.on('console', (message) => {
    if (message.type() === 'error') browserErrors.push(message.text())
  })
  page.on('pageerror', (error) => browserErrors.push(error.message))

  await page.route('**/api/iam/v1/auth/login', (route) => route.fulfill({
    json: { success: true, data: { accessToken: 'bms-token', refreshToken: 'refresh-token' } },
  }))
  await page.route('**/api/iam/v1/me', (route) => route.fulfill({
    json: { success: true, data: { id: 7, username: 'finance', status: 1 } },
  }))
  await page.route('**/openapi/iam/v1/users/me/permissions', (route) => route.fulfill({
    json: { userId: 7, permissionPayload: 'bms:*' },
  }))
  await page.route('**/api/iam/v1/menus?*', (route) => route.fulfill({ json: [] }))
  await page.route('**/api/bms/v1/**', (route) => {
    const pathname = new URL(route.request().url()).pathname
    const data = pathname.endsWith('/charge-details')
      ? [{ chargeNo: 'CH-1', objectName: '华东承运商', totalAmount: '100.20', status: 1 }]
      : pathname.endsWith('/billing-rule-views')
        ? [{ ruleNo: 'RULE-1', objectName: '华东承运商', unitPrice: '10.00', status: 2 }]
        : pathname.endsWith('/reconciliations')
          ? [{ reconciliationNo: 'REC-1', objectName: '华东承运商', totalAmount: '100.20', status: 2 }]
          : pathname.endsWith('/bill-views')
            ? [{ billNo: 'BILL-1', objectName: '华东承运商', totalAmount: '100.20', status: 2 }]
            : pathname.endsWith('/invoice-views')
              ? [{ invoiceNo: 'INV-1', objectName: '华东承运商', invoiceAmount: '100.20', status: 2 }]
              : pathname.endsWith('/finance-handoff-views')
                ? [{ handoverNo: 'FIN-1', objectName: '华东承运商', totalAmount: '100.20', status: 1 }]
                : pathname.endsWith('/refund-views')
                  ? [{ refundNo: 'REF-1', objectName: '华东承运商', refundAmount: '10.20', status: 1 }]
                  : pathname.endsWith('/settlement-report-views')
                    ? [{ objectCode: 'BO-1', objectName: '华东承运商', billingPeriod: '2026-07', billAmount: '100.20', invoiceAmount: '100.20', refundAmount: '10.20', netAmount: '90.00' }]
                    : []
    return route.fulfill({ json: data })
  })

  await page.goto('/')
  await page.getByLabel('用户名').fill('finance')
  await page.getByLabel('密码').fill('password')
  await page.locator('button[type="submit"]').click()
  await expect(page.getByRole('heading', { name: '供应链运营工作台' })).toBeVisible()

  const pages = [
    ['charge-details', '费用明细'],
    ['billing-rules', '计费规则'],
    ['reconciliations', '对账管理'],
    ['bills', '账单管理'],
    ['invoices', '发票管理'],
    ['finance-handoffs', '财务交接'],
    ['refunds', '退款与冲正'],
    ['settlement-reports', '结算报表'],
  ]
  for (const [pageId, title] of pages) {
    await page.goto(`/bms/${pageId}`)
    await expect(page.getByRole('heading', { name: title })).toBeVisible()
    await expect(page.locator('.ant-table-row')).toHaveCount(1)
  }

  await expect(page.getByRole('button', { name: '创建异步导出' })).toBeVisible()
  expect(browserErrors).toEqual([])
})
