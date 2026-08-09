import { beforeEach, describe, expect, it, vi } from 'vitest'
import client from './client'
import { queryIamMenus } from './mdmIam'

vi.mock('./client', () => ({
  default: {
    get: vi.fn(),
  },
}))

describe('IAM read-model queries', () => {
  beforeEach(() => {
    client.get.mockReset()
  })

  it('always scopes the menu read model to the SCM web application', () => {
    queryIamMenus({ pageNo: 1, pageSize: 10 })

    expect(client.get).toHaveBeenCalledWith('/iam/v1/menus', {
      params: { pageNo: 1, pageSize: 10, appCode: 'SCM_WEB' },
    })
  })
})
