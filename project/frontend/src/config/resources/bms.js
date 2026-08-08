import client from '../../api/client'
import {
  queryBmsBillingObjects,
  queryBmsChargeSources,
} from '../../api/fulfillmentSettlement'
import { column, get, simpleResource } from './helpers'

const queryCharges = get('/bms/v1/charge-details')
const queryRules = get('/bms/v1/billing-rule-views')
const queryReconciliations = get('/bms/v1/reconciliations')
const queryBills = get('/bms/v1/bill-views')
const queryInvoices = get('/bms/v1/invoice-views')
const queryFinanceHandoffs = get('/bms/v1/finance-handoff-views')
const queryRefunds = get('/bms/v1/refund-views')

const exportStatuses = {
  1: '待生成',
  2: '生成中',
  3: '等待重试',
  4: '已完成',
  5: '最终失败',
}

const externalTaskStatuses = {
  1: '待执行',
  2: '已成功',
  3: '等待重试',
  4: '最终失败',
  6: '执行中',
}

const unwrap = (result) => {
  const payload = result?.data ?? result ?? []
  return payload?.records || payload?.items || payload?.content || payload
}

const withExternalTaskStatus = (query) => async (params) => {
  const result = await query(params)
  const rows = unwrap(result).map((row) => ({
    ...row,
    externalTaskStatusName: externalTaskStatuses[row.externalTaskStatus]
      || row.externalTaskStatus,
  }))
  return rows
}

async function querySettlementWorkspace(params) {
  const [summaryResult, taskResult] = await Promise.all([
    client.get('/bms/v1/settlement-report-views', { params }),
    client.get('/bms/v1/report-exports', { params }),
  ])
  const summaries = unwrap(summaryResult).map((row) => ({
    ...row,
    rowType: '结算汇总',
    statusName: '可导出',
    __rowKind: 'summary',
    __identity: `summary:${row.objectCode}:${row.billingPeriod}`,
  }))
  const tasks = unwrap(taskResult).map((row) => ({
    ...row,
    rowType: '导出任务',
    statusName: exportStatuses[row.status] || row.status,
    __rowKind: 'export',
    __identity: `export:${row.exportNo}`,
  }))
  return [...summaries, ...tasks]
}

const createSettlementExport = (row) => client.post('/bms/v1/report-exports', {
  objectCode: row.objectCode,
  billingPeriod: row.billingPeriod,
  idempotencyKey: `bms-ui:${row.objectCode}:${row.billingPeriod}:${crypto.randomUUID()}`,
})

const retrySettlementExport = (row, reason) => (
  client.post(`/bms/v1/report-exports/${encodeURIComponent(row.exportNo)}/retry`, { reason })
)

const retryExternalTask = (row, reason) => (
  client.post(`/bms/v1/external-tasks/${encodeURIComponent(row.externalTaskNo)}/retry`, {
    reason,
  })
)

export const bmsResources = {
  'bms.billing-subjects': simpleResource(queryBmsBillingObjects, ['subjectNo', 'id'], [['subjectNo', '计费对象'], ['businessType', '业务类型'], ['businessNo', '业务单号'], ['counterpartyName', '结算对象'], ['statusName', '状态', 'status']]),
  'bms.charge-sources': simpleResource(queryBmsChargeSources, ['sourceNo', 'id'], [['sourceNo', '费用来源'], ['businessType', '业务类型'], ['businessNo', '业务单号'], ['amount', '金额', 'number'], ['currency', '币种'], ['statusName', '状态', 'status']]),
  'bms.charge-details': simpleResource(queryCharges, ['chargeNo'], [['chargeNo', '费用编号'], ['objectName', '结算对象'], ['feeType', '费用类型'], ['quantity', '计费数量', 'number'], ['unitPrice', '含义单价', 'number'], ['amount', '未税金额', 'number'], ['taxAmount', '税额', 'number'], ['totalAmount', '价税合计', 'number'], ['currency', '币种'], ['billingPeriod', '账期'], ['status', '状态', 'status']]),
  'bms.billing-rules': simpleResource(queryRules, ['ruleNo'], [['ruleNo', '规则编号'], ['objectName', '结算对象'], ['feeType', '费用类型'], ['unitPrice', '计费单价', 'number'], ['taxRate', '税率', 'number'], ['effectiveFrom', '生效日期', 'date'], ['effectiveTo', '失效日期', 'date'], ['ruleVersion', '规则版本'], ['status', '状态', 'status']]),
  'bms.reconciliations': simpleResource(queryReconciliations, ['reconciliationNo'], [['reconciliationNo', '对账单号'], ['objectName', '结算对象'], ['direction', '结算方向'], ['billingPeriod', '账期'], ['totalAmount', '对账金额', 'number'], ['currency', '币种'], ['status', '状态', 'status'], ['updatedAt', '更新时间', 'date']]),
  'bms.bills': simpleResource(queryBills, ['billNo'], [['billNo', '账单号'], ['reconciliationNo', '对账单号'], ['objectName', '结算对象'], ['direction', '结算方向'], ['billingPeriod', '账期'], ['totalAmount', '账单金额', 'number'], ['currency', '币种'], ['status', '状态', 'status'], ['updatedAt', '更新时间', 'date']]),
  'bms.invoices': simpleResource(queryInvoices, ['invoiceNo'], [['invoiceNo', '发票编号'], ['billNo', '账单号'], ['objectName', '结算对象'], ['billAmount', '账单金额', 'number'], ['invoiceAmount', '开票金额', 'number'], ['currency', '币种'], ['billingPeriod', '账期'], ['status', '状态', 'status'], ['updatedAt', '更新时间', 'date']]),
  'bms.finance-handoffs': {
    ...simpleResource(withExternalTaskStatus(queryFinanceHandoffs), ['handoverNo'], [['handoverNo', '交接编号'], ['billNo', '账单号'], ['objectName', '结算对象'], ['totalAmount', '交接金额', 'number'], ['currency', '币种'], ['voucherNo', 'ERP 凭证'], ['externalTaskStatusName', '外部任务', 'status'], ['externalAttemptCount', '尝试次数', 'number'], ['externalLastError', '失败原因'], ['status', '业务状态', 'status']]),
    actions: [{ key: 'retry-external', label: '重试财务任务', permission: 'bms:external-task:retry', visible: (row) => row.externalTaskNo && row.externalTaskStatus === 4, run: retryExternalTask }],
  },
  'bms.refunds': {
    ...simpleResource(withExternalTaskStatus(queryRefunds), ['refundNo'], [['refundNo', '退款编号'], ['billNo', '账单号'], ['objectName', '结算对象'], ['billAmount', '原账单金额', 'number'], ['refundAmount', '退款金额', 'number'], ['currency', '币种'], ['externalTaskStatusName', '支付任务', 'status'], ['externalAttemptCount', '尝试次数', 'number'], ['externalLastError', '失败原因'], ['status', '业务状态', 'status']]),
    actions: [{ key: 'retry-external', label: '重试退款任务', permission: 'bms:external-task:retry', visible: (row) => row.externalTaskNo && row.externalTaskStatus === 4, run: retryExternalTask }],
  },
  'bms.settlement-reports': {
    query: querySettlementWorkspace,
    rowKey: ['__identity', 'exportNo', 'objectCode'],
    columns: [column('rowType', '记录类型'), column('objectName', '结算对象'), column('objectCode', '对象编码'), column('billingPeriod', '结算期间'), column('billAmount', '账单金额', { number: true }), column('invoiceAmount', '开票金额', { number: true }), column('refundAmount', '退款金额', { number: true }), column('netAmount', '净结算额', { number: true }), column('exportNo', '导出任务号'), column('recordCount', '导出行数', { number: true }), column('lastError', '失败原因'), column('statusName', '状态', { status: true })],
    actions: [
      { key: 'create-export', label: '创建异步导出', permission: 'bms:report:export', visible: (row) => row.__rowKind === 'summary', run: createSettlementExport },
      { key: 'retry-export', label: '重试导出', permission: 'bms:report:retry', visible: (row) => row.__rowKind === 'export' && row.status === 5, run: retrySettlementExport },
    ],
  },
  'bms.operation-logs': simpleResource(get('/bms/v1/operation-logs'), ['id'], [
    ['createdAt', '操作时间', 'date'], ['operatorId', '操作人 ID'],
    ['operationType', '操作内容'], ['businessNo', '业务单号'],
    ['idempotencyKey', '幂等键'],
  ]),
}
