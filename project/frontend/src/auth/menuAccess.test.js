import { describe, expect, it } from 'vitest'
import { canAccessMenu, decorateMenuItems, hasPermission, parsePermissionCodes, shouldEnforceMenuAccess } from './menuAccess'

describe('menuAccess', () => {
  it('parses json and comma separated permission codes', () => {
    expect(parsePermissionCodes('["purchase:po:read","iam:*"]')).toEqual(['purchase:po:read', 'iam:*'])
    expect(parsePermissionCodes('purchase:po:read, iam:*')).toEqual(['purchase:po:read', 'iam:*'])
    expect(parsePermissionCodes('')).toEqual([])
  })

  it('enforces access only when token and permissions exist', () => {
    expect(shouldEnforceMenuAccess('token', ['iam:*'])).toBe(true)
    expect(shouldEnforceMenuAccess('token', [])).toBe(false)
    expect(shouldEnforceMenuAccess('', ['iam:*'])).toBe(false)
  })

  it('supports global and scoped wildcard permissions', () => {
    expect(hasPermission(['*'], 'purchase:*')).toBe(true)
    expect(hasPermission(['purchase:po:read'], 'purchase:*')).toBe(true)
    expect(hasPermission(['wms:receipt:read'], 'purchase:*')).toBe(false)
  })

  it('checks menu access by configured permission', () => {
    expect(canAccessMenu('purchase', ['purchase:po:read'], true)).toBe(true)
    expect(canAccessMenu('permission', ['purchase:po:read'], true)).toBe(false)
    expect(canAccessMenu('permission', [], false)).toBe(true)
  })

  it('decorates leaf menu items without disabling parent groups', () => {
    const items = [
      { key: 'supplier', label: '供应商', children: [{ key: 'supplier-asn', label: 'ASN' }] },
      { key: 'purchase', label: '采购' },
    ]

    const decorated = decorateMenuItems(items, ['purchase:po:read'], true)

    expect(decorated[0].disabled).toBeFalsy()
    expect(decorated[0].children[0].disabled).toBe(true)
    expect(decorated[1].disabled).toBe(false)
  })
})
