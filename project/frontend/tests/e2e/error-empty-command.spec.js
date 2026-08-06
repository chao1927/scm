import { expect, test } from '@playwright/test'

const requisition = {
  id: 18,
  requisitionNo: 'PR-QA-0018',
  applicantId: 'U-QA',
  purchaseOrgId: 'ORG-QA',
  demandDepartmentId: 'D-QA',
  status: 2,
  statusName: '审批中',
  reason: '门店补货',
  version: 3,
  lines: [{ id: 181, skuCode: 'SKU-QA', requestedQuantity: 24 }],
}

test.beforeEach(async ({ page }) => {
  await mockIam(page)
})

test('shows a retryable error outside the table and never disguises it as empty data', async ({ page }) => {
  let backendAvailable = false
  await page.route('**/api/purchase/v1/requisitions?*', (route) => {
    if (!backendAvailable) return route.fulfill({ status: 503, json: { message: '请购读模型暂不可用' } })
    return route.fulfill({ json: emptyPage() })
  })
  await login(page)

  await page.goto('/purchase/requisitions')
  const alert = page.getByRole('alert')
  await expect(alert).toContainText('数据加载失败', { timeout: 10000 })
  await expect(alert).toContainText('请购读模型暂不可用')
  await expect(page.getByText('暂无业务数据')).toHaveCount(0)

  backendAvailable = true
  await alert.getByRole('button', { name: '重新加载' }).click()
  await expect(alert).toHaveCount(0)
  await expect(page.getByText('暂无业务数据')).toBeVisible()
})

test('requires a reason and supports confirm, cancel and Escape command flows', async ({ page }) => {
  let rejectedReason
  await page.route('**/api/purchase/v1/requisitions?*', (route) => route.fulfill({ json: pageWith(requisition) }))
  await page.route('**/api/purchase/v1/requisitions/18/reject', async (route) => {
    rejectedReason = (await route.request().postDataJSON()).reason
    await route.fulfill({ json: { success: true, data: {} } })
  })
  await login(page)
  await page.goto('/purchase/requisitions')

  const rejectButton = page.getByRole('button', { name: '驳回' })
  await rejectButton.focus()
  await page.keyboard.press('Enter')
  let dialog = page.getByRole('dialog', { name: '确认驳回' })
  await expect(dialog).toBeVisible()
  const reasonInput = dialog.getByRole('textbox', { name: '操作原因' })
  await reasonInput.fill('')
  await dialog.getByRole('button', { name: '确认执行' }).click()
  await expect(dialog.getByText('请输入操作原因')).toBeVisible()
  await reasonInput.fill('重复请购，退回重新整理')
  await dialog.getByRole('button', { name: '确认执行' }).click()
  await expect(dialog).toHaveCount(0)
  expect(rejectedReason).toBe('重复请购，退回重新整理')

  await rejectButton.click()
  dialog = page.getByRole('dialog', { name: '确认驳回' })
  await dialog.getByRole('button', { name: '取消' }).click()
  await expect(dialog).toHaveCount(0)

  await rejectButton.click()
  await expect(page.getByRole('dialog', { name: '确认驳回' })).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(page.getByRole('dialog', { name: '确认驳回' })).toHaveCount(0)
})

test('renders a controlled not-found error for a missing detail instead of a blank page', async ({ page }) => {
  await page.route('**/api/purchase/v1/requisitions/404', (route) => route.fulfill({
    status: 404,
    json: { message: '请购单不存在' },
  }))
  await login(page)

  await page.goto('/purchase/requisitions/404')
  await expect(page.getByText('数据加载失败')).toBeVisible()
  await expect(page.getByText('请购单不存在')).toBeVisible()
  await expect(page.getByText('未找到该业务对象')).toHaveCount(0)
  await expect(page.getByRole('button', { name: '重新加载' })).toBeVisible()
})

async function mockIam(page) {
  await page.route('**/api/iam/v1/auth/login', (route) => route.fulfill({
    json: { success: true, data: { accessToken: 'qa-token', refreshToken: 'qa-refresh' } },
  }))
  await page.route('**/api/iam/v1/me', (route) => route.fulfill({
    json: { success: true, data: { id: 18, username: 'qa-operator', status: 1 } },
  }))
  await page.route('**/openapi/iam/v1/users/me/permissions', (route) => route.fulfill({
    json: { userId: 18, permissionPayload: 'purchase:*' },
  }))
  await page.route('**/api/iam/v1/menus?*', (route) => route.fulfill({ json: [] }))
}

async function login(page) {
  await page.goto('/')
  await page.getByLabel('用户名').fill('qa-operator')
  await page.getByLabel('密码').fill('password')
  await page.locator('button[type="submit"]').click()
  await expect(page.getByRole('heading', { name: '供应链运营工作台' })).toBeVisible()
}

function emptyPage() {
  return { success: true, data: { pageNo: 1, pageSize: 20, total: 0, records: [] } }
}

function pageWith(record) {
  return { success: true, data: { pageNo: 1, pageSize: 20, total: 1, records: [record] } }
}
