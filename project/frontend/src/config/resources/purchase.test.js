import { describe, expect, it } from 'vitest'
import { purchaseResources } from './purchase'

describe('purchase workbench resource', () => {
  it('connects the workbench to real summary and todo endpoints', () => {
    const resource = purchaseResources['purchase.workbench']

    expect(resource).toBeDefined()
    expect(resource.query).toBeTypeOf('function')
    expect(resource.summary).toBeTypeOf('function')
    expect(resource.rowKey).toEqual(['todoId', 'businessNo'])
    expect(resource.columns.map((item) => item.key)).toEqual([
      'businessNo',
      'businessType',
      'title',
      'statusName',
      'priority',
      'dueDate',
      'updatedAt',
    ])
  })
})
