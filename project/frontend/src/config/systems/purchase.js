const page = (id, label, options = {}) => ({ id, label, ...options })

export const purchaseSystem = {
  id: 'purchase',
  code: 'PUR',
  shortName: '采购',
  name: '采购管理',
  domain: '采购执行上下文',
  permission: 'purchase:*',
  goal: '贯通请购、询报价、定标、采购订单、供应商确认、到货与退供。',
  workflow: ['请购已提交', '询价已发布', '供应商已定标', '采购订单已发布', '到货已完成'],
  pages: [
    page('workbench', '采购工作台'),
    page('requisitions', '请购管理', { legacy: 'requisitions' }),
    page('requisition-approvals', '请购审批', { legacy: 'requisitions' }),
    page('rfqs', '询价单', { legacy: 'rfqs' }),
    page('quotations', '报价管理'),
    page('compare-results', '比价定标'),
    page('orders', '采购订单', { legacy: 'orders' }),
    page('supplier-confirms', '供应商确认', { legacy: 'confirms' }),
    page('inbound-tracks', '到货跟踪', { legacy: 'inbounds' }),
    page('supplier-returns', '退供申请'),
    page('prices', '采购价格'),
    page('failed-events', '失败事件', { legacy: 'failedEvents' }),
    page('operation-logs', '操作日志'),
  ],
}
