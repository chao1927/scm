import {
  queryOmsChannelOrders,
  queryOmsFulfillments,
  queryOmsOutbounds,
  queryOmsSalesOrders,
} from '../../api/fulfillmentSettlement'
import client from '../../api/client'
import { get, getDetail, simpleResource } from './helpers'

const unwrap = (result) => result?.data ?? result

const localDateTime = (value) => {
  const pad = (number) => String(number).padStart(2, '0')
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}`
    + `T${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`
}

const defaultMetricPeriod = () => {
  const periodEnd = new Date()
  const periodStart = new Date(periodEnd)
  periodStart.setDate(periodStart.getDate() - 30)
  return {
    periodStart: localDateTime(periodStart),
    periodEnd: localDateTime(periodEnd),
  }
}

async function queryFulfillmentMetricsWorkspace(params = {}) {
  const period = {
    ...defaultMetricPeriod(),
    ...(params.periodStart ? { periodStart: params.periodStart } : {}),
    ...(params.periodEnd ? { periodEnd: params.periodEnd } : {}),
  }
  const [metricResult, exportResult] = await Promise.all([
    client.get('/oms/v1/metrics/fulfillment', { params: period }),
    client.get('/oms/v1/metric-exports'),
  ])
  const metric = unwrap(metricResult) || {}
  const exports = unwrap(exportResult) || []
  return [
    {
      ...metric,
      ...period,
      rowType: '履约指标',
      statusName: '可导出',
      __rowKind: 'summary',
      __identity: `summary:${period.periodStart}:${period.periodEnd}`,
    },
    ...exports.map((task) => ({
      ...task,
      rowType: '导出任务',
      __rowKind: 'export',
      __identity: `export:${task.exportNo}`,
    })),
  ]
}

const createMetricExport = (row) => client.post('/oms/v1/metric-exports', {
  periodStart: row.periodStart,
  periodEnd: row.periodEnd,
})

const retryMetricExport = (row) => client.post(
  `/oms/v1/metric-exports/${encodeURIComponent(row.exportNo)}/retry`,
  { version: row.version },
)

async function downloadMetricExport(row) {
  const blob = await client.get(
    `/oms/v1/metric-exports/${encodeURIComponent(row.exportNo)}/file`,
    { responseType: 'blob' },
  )
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = row.fileName || `${row.exportNo}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

export const omsResources = {
  'oms.workbench': {
    query: queryFulfillmentMetricsWorkspace,
    rowKey: ['__identity', 'exportNo'],
    columns: [
      ['rowType', '记录类型'],
      ['periodStart', '统计开始', 'date'],
      ['periodEnd', '统计结束', 'date'],
      ['orderCount', '订单量', 'number'],
      ['completedOrderCount', '完成量', 'number'],
      ['cancelledOrderCount', '取消量', 'number'],
      ['fulfillmentRate', '履约率', 'number'],
      ['averageFulfillmentDurationSeconds', '平均履约时长(秒)', 'number'],
      ['exportNo', '导出任务号'],
      ['recordCount', '导出行数', 'number'],
      ['lastError', '失败原因'],
      ['statusName', '状态', 'status'],
    ].map(([key, title, format]) => ({
      key,
      title,
      status: format === 'status',
      date: format === 'date',
      number: format === 'number',
    })),
    actions: [
      { key: 'create-export', label: '创建异步导出', permission: 'oms:metrics:export', visible: (row) => row.__rowKind === 'summary', run: createMetricExport },
      { key: 'retry-export', label: '重试导出', permission: 'oms:metrics:retry', visible: (row) => row.__rowKind === 'export' && [3, 5].includes(row.status), run: retryMetricExport },
      { key: 'download-export', label: '下载 CSV', permission: 'oms:metrics:download', visible: (row) => row.__rowKind === 'export' && row.status === 4, run: downloadMetricExport },
    ],
  },
  'oms.channel-orders': simpleResource(queryOmsChannelOrders, ['channelOrderNo', 'orderNo'], [['channelOrderNo', '渠道订单'], ['channelCode', '渠道'], ['externalOrderNo', '外部订单'], ['statusName', '状态', 'status'], ['createdAt', '创建时间', 'date']]),
  'oms.sales-orders': { ...simpleResource(queryOmsSalesOrders, ['orderNo', 'id'], [['orderNo', '销售订单'], ['channelCode', '渠道'], ['customerId', '客户'], ['totalAmount', '订单金额', 'number'], ['statusName', '状态', 'status'], ['createdAt', '创建时间', 'date']]), detail: getDetail('/oms/v1/sales-orders') },
  'oms.fulfillments': { ...simpleResource(queryOmsFulfillments, ['fulfillmentNo', 'id'], [['fulfillmentNo', '履约单'], ['orderNo', '销售订单'], ['warehouseCode', '履约仓'], ['promisedAt', '承诺时间', 'date'], ['statusName', '状态', 'status']]), detail: getDetail('/oms/v1/fulfillments') },
  'oms.outbounds': { ...simpleResource(queryOmsOutbounds, ['outboundNo', 'id'], [['outboundNo', '出库单'], ['fulfillmentNo', '履约单'], ['warehouseCode', '仓库'], ['wmsOrderNo', 'WMS 单号'], ['statusName', '状态', 'status']]), detail: getDetail('/oms/v1/outbounds') },
  'oms.audit-results': {
    ...simpleResource(get('/oms/v1/audit-results'), ['resultId'], [
      ['resultId', '审单结果 ID'],
      ['salesOrderNo', '销售订单'],
      ['channelCode', '渠道'],
      ['auditType', '审单类型'],
      ['auditResult', '审单结果'],
      ['exceptionReason', '异常原因'],
      ['statusName', '处理状态', 'status'],
      ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/oms/v1/audit-results'),
  },
  'oms.reservations': {
    ...simpleResource(get('/oms/v1/reservations'), ['reservationRefNo'], [
      ['reservationRefNo', '预占引用'],
      ['reservationNo', '中央库存预占号'],
      ['salesOrderNo', '销售订单'],
      ['fulfillmentNo', '履约单'],
      ['warehouseCode', '仓库'],
      ['reserveQty', '请求数量', 'number'],
      ['reservedQty', '实际数量', 'number'],
      ['statusName', '状态', 'status'],
      ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/oms/v1/reservations'),
  },
  'oms.cancel-requests': {
    ...simpleResource(get('/oms/v1/cancel-requests'), ['cancellationNo'], [
      ['cancellationNo', '取消申请'],
      ['salesOrderNo', '销售订单'],
      ['fulfillmentNo', '履约单'],
      ['warehouseCode', '仓库'],
      ['reason', '取消原因'],
      ['statusName', '状态', 'status'],
      ['wmsCancelled', 'WMS 已取消'],
      ['stockReleased', '库存已释放'],
      ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/oms/v1/cancel-requests'),
  },
  'oms.after-sales': {
    ...simpleResource(get('/oms/v1/after-sales'), ['afterSaleNo'], [
      ['afterSaleNo', '售后单号'],
      ['afterSaleType', '售后类型'],
      ['salesOrderNo', '销售订单'],
      ['warehouseCode', '退货仓'],
      ['refundAmount', '退款金额', 'number'],
      ['refundedAmount', '已退金额', 'number'],
      ['statusName', '状态', 'status'],
      ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/oms/v1/after-sales'),
  },
  'oms.exceptions': {
    ...simpleResource(get('/oms/v1/exceptions'), ['exceptionNo'], [
      ['exceptionNo', '异常单号'],
      ['salesOrderNo', '销售订单'],
      ['fulfillmentNo', '履约单'],
      ['exceptionType', '异常类型'],
      ['responsibleParty', '责任方'],
      ['warehouseCode', '仓库'],
      ['reason', '异常原因'],
      ['statusName', '状态', 'status'],
      ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/oms/v1/exceptions'),
  },
  'oms.operation-logs': {
    ...simpleResource(get('/oms/v1/operation-logs'), ['logId'], [
      ['logId', '日志 ID'],
      ['operationType', '操作类型'],
      ['businessNo', '业务单号'],
      ['salesOrderNo', '销售订单'],
      ['operatorId', '操作人'],
      ['warehouseCode', '仓库'],
      ['createdAt', '操作时间', 'date'],
    ]),
    detail: getDetail('/oms/v1/operation-logs'),
  },
}
