const page = (id, label, options = {}) => ({ id, label, ...options })

export const tmsSystem = {
  id: 'tms',
  code: 'TMS',
  shortName: '运输',
  name: '运输管理',
  domain: '运输履约上下文',
  permission: 'tms:*',
  goal: '统一承接运输需求，完成运单、面单、轨迹、签收、异常和费用事实。',
  workflow: ['运输任务已创建', '运单已创建', '包裹已揽收', '运输轨迹已更新', '签收已确认'],
  pages: [
    page('workbench', 'TMS 工作台'),
    page('transport-tasks', '运输任务', { legacy: 'transportTasks' }),
    page('waybills', '运单管理', { legacy: 'waybills' }),
    page('labels', '面单管理'),
    page('tracks', '轨迹查询'),
    page('signatures', '签收管理'),
    page('exceptions', '物流异常', { legacy: 'tmsExceptions' }),
    page('fee-sources', '物流费用来源', { legacy: 'feeSources' }),
    page('carriers', '承运商接口'),
    page('operation-logs', '操作日志'),
  ],
}
