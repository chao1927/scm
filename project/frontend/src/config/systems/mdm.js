const page = (id, label) => ({ id, label })

export const mdmSystem = {
  id: 'mdm',
  code: 'MDM',
  shortName: '主数据',
  name: '主数据',
  domain: '主数据上下文',
  permission: 'mdm:*',
  goal: '统一类型、字段、编码、审核、发布、质量和变更追溯。',
  workflow: ['主数据草稿已创建', '主数据已提交审核', '主数据已启用', '发布任务已创建', '下游回执已返回'],
  pages: [
    page('workbench', '主数据工作台'),
    page('types', '主数据类型'),
    page('field-defs', '字段模板'),
    page('code-rules', '编码规则'),
    page('products', '商品主数据'),
    page('partners', '合作伙伴'),
    page('warehouses', '仓储主数据'),
    page('approvals', '主数据审核'),
    page('publishes', '主数据发布'),
    page('imports', '导入导出'),
    page('quality-issues', '质量问题'),
    page('change-logs', '变更日志'),
  ],
}
