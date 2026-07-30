export function permissionCodesFromSnapshot(snapshot) {
  const payload = snapshot?.permissionPayload
  if (!payload) {
    return []
  }
  return [...new Set(payload.split(',').map((code) => code.trim()).filter(Boolean))]
}

export function menuOverridesFromResources(resources) {
  if (!Array.isArray(resources)) {
    return new Map()
  }
  return new Map(resources
    .filter((menu) => menu?.menuCode && menu.status === 1)
    .map((menu) => [menu.menuCode, {
      label: menu.menuName,
      parentCode: menu.parentCode,
      sortNo: menu.sortNo ?? 0,
    }]))
}

export function applyMenuResources(items, resources) {
  const overrides = menuOverridesFromResources(resources)
  if (overrides.size === 0) {
    return items
  }
  return items
    .map((item) => {
      const children = item.children ? applyMenuResources(item.children, resources) : undefined
      const override = overrides.get(item.key)
      if (!override && (!children || children.length === 0)) {
        return null
      }
      return { ...item, label: override?.label || item.label, sortNo: override?.sortNo ?? item.sortNo ?? 0, children }
    })
    .filter(Boolean)
    .sort((left, right) => left.sortNo - right.sortNo)
}

export function storeTokens(result) {
  const data = result?.data
  if (!data?.accessToken || !data?.refreshToken) {
    throw new Error('IAM 登录响应缺少令牌')
  }
  sessionStorage.setItem('access_token', data.accessToken)
  sessionStorage.setItem('refresh_token', data.refreshToken)
}

export function clearTokens() {
  sessionStorage.removeItem('access_token')
  sessionStorage.removeItem('refresh_token')
  sessionStorage.removeItem('permission_codes')
}
