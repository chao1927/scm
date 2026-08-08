const page = (id, label) => ({ id, label })

export const inventorySystem = {
  id: 'inventory',
  code: 'INV',
  shortName: '库存',
  name: '中央库存',
  domain: '库存可用性上下文',
  permission: 'inventory:*',
  goal: '统一管理余额、可用量、预占、冻结、调整、流水、快照与跨系统对账。',
  workflow: ['库存事实已接收', '余额已更新', '可用量已重算', '流水已登记', '读模型已刷新'],
  pages: [
    page('workbench', '库存工作台'),
    page('balances', '库存余额'),
    page('available', '可用库存'),
    page('reservations', '预占管理'),
    page('freezes', '冻结解冻'),
    page('adjustments', '库存调整'),
    page('ledgers', '库存流水'),
    page('snapshots', '库存快照'),
    page('reconciliations', '库存对账'),
    page('event-logs', '事件日志'),
    page('operation-logs', '操作日志'),
  ],
}
