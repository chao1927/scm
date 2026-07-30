import { describe, expect, it } from 'vitest'
import { applyMenuResources, menuOverridesFromResources, permissionCodesFromSnapshot } from './session'

describe('IAM session mapping', () => {
  it('maps and de-duplicates permission snapshot payload', () => {
    expect(permissionCodesFromSnapshot({ permissionPayload: 'supplier:quality:read, iam:*,supplier:quality:read' }))
      .toEqual(['supplier:quality:read', 'iam:*'])
    expect(permissionCodesFromSnapshot(null)).toEqual([])
  })

  it('keeps enabled IAM menu resources only', () => {
    const result = menuOverridesFromResources([
      { menuCode: 'workbench', menuName: '工作台', status: 1, sortNo: 10 },
      { menuCode: 'permission', menuName: '权限', status: 2, sortNo: 20 },
    ])
    expect(result.get('workbench').label).toBe('工作台')
    expect(result.has('permission')).toBe(false)
  })

  it('uses IAM resources to filter, rename and order menu definitions', () => {
    const items = [{ key: 'purchase', label: '采购', sortNo: 20 }, { key: 'workbench', label: '工作台', sortNo: 10 }]
    const resources = [{ menuCode: 'purchase', menuName: '采购运营', status: 1, sortNo: 5 }]
    expect(applyMenuResources(items, resources)).toEqual([{ key: 'purchase', label: '采购运营', sortNo: 5, children: undefined }])
  })
})
