const page = (id, label, options = {}) => ({ id, label, ...options })

export const supplierSystem = {
  id: 'supplier',
  code: 'SUP',
  shortName: '供应商',
  name: '供应商协同',
  domain: '供应商协同上下文',
  permission: 'supplier:*',
  goal: '管理供应商档案、订单确认、ASN、质量整改、对账与绩效协同。',
  workflow: ['订单待确认', '供应商已确认', 'ASN 已提交', '到仓收货', '对账完成'],
  pages: [
    page('workbench', '供应商工作台'),
    page('profile', '供应商档案'),
    page('user-bindings', '账号绑定'),
    page('skus', '供应商商品'),
    page('po-confirms', '订单协同', { legacy: 'supplier-order' }),
    page('asns', 'ASN 管理', { legacy: 'supplier-asn' }),
    page('returns', '退供协同'),
    page('reconciliations', '对账协同'),
    page('quality-issues', '质量协同', { legacy: 'supplier-quality' }),
    page('scores', '供应商评分'),
    page('rectifications', '整改管理', { legacy: 'supplier-quality' }),
    page('operation-logs', '操作日志'),
  ],
}
