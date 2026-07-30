import { describe, expect, it } from 'vitest'
import { commandDialogAccessibility } from './CommandDialog'

describe('CommandDialog accessibility contract', () => {
  it('associates a stable title and high-risk description for dangerous commands', () => {
    expect(commandDialogAccessibility({ label: '作废', danger: true })).toEqual({
      title: '确认作废',
      titleId: 'command-dialog-title',
      descriptionId: 'command-dialog-danger-description',
      dangerDescription: '该操作可能不可逆，并会影响后续业务流程。请确认目标对象和操作原因后再执行。',
    })
  })

  it('does not add a danger description to an ordinary command', () => {
    expect(commandDialogAccessibility({ label: '提交', danger: false }).descriptionId).toBeUndefined()
  })
})
