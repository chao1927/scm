import { cancelAsn, queryAsnDetail, queryAsns, submitAsn } from '../../api/asn'
import {
  confirmPo,
  feedbackPoDifference,
  queryPoConfirms,
  queryQualityIssues,
  rejectPo,
  requestRectification,
  submitRectificationPlan,
  verifyRectification,
} from '../../api/supplierCollaboration'
import { column, get, getDetail, reason } from './helpers'

export const supplierResources = {
  'supplier.profile': {
    query: get('/supplier/v1/admissions'),
    detail: getDetail('/supplier/v1/admissions'),
    rowKey: ['admissionNo', 'id'],
    columns: [column('admissionNo', '准入单号'), column('supplierName', '供应商'), column('supplierCode', '供应商编码'), column('statusName', '状态', { status: true }), column('updatedAt', '更新时间', { date: true })],
  },
  'supplier.po-confirms': {
    query: queryPoConfirms,
    detail: getDetail('/supplier/v1/po-confirms'),
    rowKey: ['id', 'confirmId', 'orderNo'],
    columns: [column('orderNo', '采购订单'), column('supplierName', '供应商'), column('purchaseOrgName', '采购组织'), column('statusName', '确认状态', { status: true }), column('expectedDeliveryDate', '承诺交期', { date: true }), column('updatedAt', '更新时间', { date: true })],
    actions: [
      { key: 'confirm', label: '确认', permission: 'supplier:purchase_order:confirm', tone: 'primary', visible: (row) => [1, 2].includes(row.status) && row.lines?.length, run: (row, text) => confirmPo(row.id || row.confirmId, { version: row.version, remark: reason(text), lines: row.lines.map((line) => ({ orderLineId: line.orderLineId ?? line.id, confirmedQty: line.confirmedQty ?? line.orderQuantity, confirmedDeliveryDate: line.confirmedDeliveryDate ?? line.requestedDeliveryDate })) }) },
      { key: 'feedback-diff', label: '反馈数量差异', permission: 'supplier:purchase_order:feedback_diff', visible: (row) => [1, 2].includes(row.status) && row.lines?.length, run: (row, text) => feedbackPoDifference(row.id || row.confirmId, { version: row.version, diffType: 1, remark: reason(text), lines: row.lines.map((line) => ({ orderLineId: line.orderLineId ?? line.id, confirmedQty: line.confirmedQty ?? line.orderQuantity, confirmedDeliveryDate: line.confirmedDeliveryDate ?? line.requestedDeliveryDate, reason: reason(text) })) }) },
      { key: 'reject', label: '拒绝', permission: 'supplier:purchase_order:reject', danger: true, visible: (row) => [1, 2].includes(row.status), run: (row, text) => rejectPo(row.id || row.confirmId, { version: row.version, reasonCode: 1, remark: reason(text) }) },
    ],
  },
  'supplier.asns': {
    query: queryAsns,
    detail: queryAsnDetail,
    rowKey: ['asnId', 'asnNo'],
    columns: [column('asnNo', 'ASN 单号'), column('purchaseOrderId', '采购订单'), column('supplierId', '供应商'), column('warehouseId', '目的仓'), column('estimatedArrivalAt', '预计到仓', { date: true }), column('statusName', '状态', { status: true })],
    actions: [
      { key: 'submit', label: '提交', permission: 'supplier:asn:submit', tone: 'primary', visible: (row) => row.status === 1, run: (row) => submitAsn(row.asnId, row.version) },
      { key: 'cancel', label: '取消', permission: 'supplier:asn:cancel', danger: true, visible: (row) => [1, 2, 3, 4].includes(row.status), run: (row, text) => cancelAsn(row.asnId, reason(text), row.version) },
    ],
    create: { label: '新建 ASN', permission: 'supplier:asn:create', kind: 'asn' },
  },
  'supplier.reconciliations': {
    query: get('/supplier/v1/reconciliations'),
    rowKey: ['reconciliationNo', 'id'],
    columns: [column('reconciliationNo', '对账单号'), column('supplierName', '供应商'), column('periodCode', '账期'), column('payableAmount', '应付金额', { number: true }), column('differenceAmount', '差异金额', { number: true }), column('statusName', '状态', { status: true })],
  },
  'supplier.quality-issues': {
    query: queryQualityIssues,
    detail: getDetail('/supplier/v1/quality-issues'),
    rowKey: ['id', 'issueNo'],
    columns: [column('issueNo', '问题单号'), column('supplierName', '供应商'), column('issueTypeName', '问题类型'), column('severityName', '严重程度'), column('statusName', '状态', { status: true }), column('dueAt', '整改期限', { date: true })],
    actions: [
      { key: 'rectify', label: '发起整改', permission: 'supplier:quality:rectify', tone: 'primary', visible: (row) => [1, 2].includes(row.status) && row.dueAt, run: (row) => requestRectification(row.id, { version: row.version, deadline: row.dueAt }) },
      { key: 'submit-plan', label: '提交整改方案', permission: 'supplier:quality:submit_plan', tone: 'primary', visible: (row) => [2, 3].includes(row.status), run: (row, text) => submitRectificationPlan(row.id, { version: row.version, plan: reason(text) }) },
      { key: 'verify', label: '验证通过', permission: 'supplier:quality:verify', tone: 'primary', visible: (row) => [3, 4].includes(row.status), run: (row, text) => verifyRectification(row.id, { version: row.version, passed: true, comment: reason(text) }) },
    ],
  },
  'supplier.rectifications': {
    query: queryQualityIssues,
    rowKey: ['id', 'issueNo'],
    columns: [column('issueNo', '整改编号'), column('supplierName', '供应商'), column('rectificationPlan', '整改方案'), column('statusName', '状态', { status: true }), column('dueAt', '完成期限', { date: true })],
  },
}
