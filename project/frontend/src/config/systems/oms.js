const page = (id, label, options = {}) => ({ id, label, ...options })

export const omsSystem = {
  id: 'oms',
  code: 'OMS',
  shortName: '订单',
  name: '订单履约',
  domain: '订单履约上下文',
  permission: 'oms:*',
  goal: '把渠道接入、审单、分仓、预占、出库、取消和售后组织为可补偿流程。',
  workflow: ['渠道订单已接收', '订单已审核', '库存已预占', '履约单已下发', '订单已完成'],
  pages: [
    page('workbench', 'OMS 工作台'),
    page('channel-orders', '渠道订单接入', { legacy: 'channelOrders' }),
    page('sales-orders', '销售订单', { legacy: 'salesOrders' }),
    page('audit-results', '订单审单'),
    page('fulfillments', '分仓履约', { legacy: 'fulfillments' }),
    page('reservations', '库存预占'),
    page('outbounds', '出库单', { legacy: 'outbounds' }),
    page('cancel-requests', '取消管理'),
    page('after-sales', '售后管理'),
    page('exceptions', '异常处理'),
    page('operation-logs', '操作日志'),
  ],
}
