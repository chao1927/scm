const page = (id, label) => ({ id, label })

export const bmsSystem = {
  id: 'bms',
  code: 'BMS',
  shortName: '结算',
  name: '计费结算',
  domain: '计费结算上下文',
  permission: 'bms:*',
  goal: '把业务费用事实转化为可对账、可结算、可开票和可追溯的财务事实。',
  workflow: ['费用事实已接收', '计费规则已匹配', '对账单已确认', '账单已生成', '财务已交接'],
  pages: [
    page('workbench', 'BMS 工作台'),
    page('billing-subjects', '计费对象'),
    page('charge-sources', '费用来源'),
    page('charge-details', '费用明细'),
    page('billing-rules', '计费规则'),
    page('reconciliations', '对账管理'),
    page('bills', '账单管理'),
    page('invoices', '发票管理'),
    page('finance-handoffs', '财务交接'),
    page('refunds', '退款与冲正'),
    page('settlement-reports', '结算报表'),
    page('operation-logs', '操作日志'),
  ],
}
