export const menuPermissions = {
  'supplier-asn': 'supplier:asn:read',
  purchase: 'purchase:*',
  wms: 'wms:*',
  inventory: 'inventory:*',
  oms: 'oms:*',
  tms: 'tms:*',
  bms: 'bms:*',
  mdm: 'mdm:*',
  integration: 'integration:*',
  permission: 'iam:*',
}

export function parsePermissionCodes(raw) {
  if (!raw) {
    return []
  }
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed.filter(Boolean).map(String) : []
  } catch {
    return raw.split(',').map((item) => item.trim()).filter(Boolean)
  }
}

export function shouldEnforceMenuAccess(accessToken, permissionCodes) {
  return Boolean(accessToken && permissionCodes.length > 0)
}

export function hasPermission(permissionCodes, requiredPermission) {
  if (!requiredPermission) {
    return true
  }
  if (permissionCodes.includes(requiredPermission) || permissionCodes.includes('*')) {
    return true
  }
  if (requiredPermission.endsWith(':*')) {
    const prefix = requiredPermission.slice(0, -1)
    return permissionCodes.some((code) => code.startsWith(prefix))
  }
  return false
}

export function canAccessMenu(key, permissionCodes, enforceAccess) {
  if (!enforceAccess) {
    return true
  }
  return hasPermission(permissionCodes, menuPermissions[key])
}

export function decorateMenuItems(items, permissionCodes, enforceAccess) {
  return items.map((item) => {
    const children = item.children ? decorateMenuItems(item.children, permissionCodes, enforceAccess) : undefined
    const isLeaf = !children || children.length === 0
    return {
      ...item,
      children,
      disabled: item.disabled || (isLeaf && !canAccessMenu(item.key, permissionCodes, enforceAccess)),
    }
  })
}
