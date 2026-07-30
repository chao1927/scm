import {
  acceptSupplierConfirmDiff,
  approvePurchaseOrder,
  approveRequisition,
  cancelPurchaseOrder,
  cancelSupplierConfirmOrder,
  closePurchaseOrder,
  closeRfq,
  publishPurchaseOrder,
  publishRfq,
  queryInbounds,
  queryPurchaseFailedEvents,
  queryPurchaseOrders,
  queryRequisitions,
  queryRfqs,
  querySupplierConfirms,
  rejectRequisition,
  renegotiateSupplierConfirm,
  replayPurchaseFailedEvent,
  submitPurchaseOrder,
  submitRequisition,
} from '../../api/purchase'
import { column, get, getDetail, reason } from './helpers'

function purchaseRequisitions() {
  return {
    query: queryRequisitions,
    detail: getDetail('/purchase/v1/requisitions'),
    rowKey: ['id', 'requisitionNo'],
    columns: [column('requisitionNo', '请购单号'), column('applicantId', '申请人'), column('purchaseOrgId', '采购组织'), column('demandDepartmentId', '需求部门'), column('statusName', '状态', { status: true }), column('reason', '请购原因'), column('updatedAt', '更新时间', { date: true })],
    actions: [
      { key: 'submit', label: '提交', permission: 'purchase:requisition:submit', tone: 'primary', visible: (row) => row.status === 1, run: (row) => submitRequisition(row.id, row.version) },
      { key: 'approve', label: '批准', permission: 'purchase:requisition:approve', tone: 'primary', visible: (row) => row.status === 2, run: (row) => approveRequisition(row.id, row.version, (row.lines || []).map((line) => ({ lineId: line.id, approvedQuantity: line.requestedQuantity }))) },
      { key: 'reject', label: '驳回', permission: 'purchase:requisition:approve', danger: true, visible: (row) => row.status === 2, run: (row, text) => rejectRequisition(row.id, row.version, reason(text)) },
    ],
  }
}

export const purchaseResources = {
  'purchase.workbench': {
    query: get('/purchase/v1/workbench/todos'),
    summary: get('/purchase/v1/workbench/summary'),
    rowKey: ['todoId', 'businessNo'],
    columns: [
      column('businessNo', '业务单号'),
      column('businessType', '待办类型'),
      column('title', '待办事项'),
      column('statusName', '状态', { status: true }),
      column('priority', '优先级', { status: true }),
      column('dueDate', '到期日期', { date: true }),
      column('updatedAt', '更新时间', { date: true }),
    ],
  },
  'purchase.requisitions': purchaseRequisitions(),
  'purchase.requisition-approvals': purchaseRequisitions(),
  'purchase.rfqs': {
    query: queryRfqs,
    detail: getDetail('/purchase/v1/rfqs'),
    rowKey: ['rfqNo', 'id'],
    columns: [column('rfqNo', 'RFQ 单号'), column('purchaseOrgId', '采购组织'), column('categoryCode', '品类'), column('sourceRequisitionNo', '来源请购'), column('quoteDeadline', '报价截止', { date: true }), column('invitedSupplierCount', '邀请供应商'), column('statusName', '状态', { status: true })],
    actions: [
      { key: 'publish', label: '发布', permission: 'purchase:rfq:publish', tone: 'primary', visible: (row) => row.status === 1, run: (row) => publishRfq(row.rfqNo, row.version) },
      { key: 'close', label: '截标', permission: 'purchase:rfq:close', visible: (row) => [2, 3].includes(row.status), run: (row, text) => closeRfq(row.rfqNo, row.version, reason(text)) },
    ],
  },
  'purchase.compare-results': {
    query: get('/purchase/v1/bid-comparisons'),
    detail: getDetail('/purchase/v1/bid-comparisons'),
    rowKey: ['compareNo', 'id'],
    columns: [column('compareNo', '比价编号'), column('rfqNo', 'RFQ 单号'), column('purchaseOrgId', '采购组织'), column('candidateCount', '候选数量'), column('statusName', '状态', { status: true }), column('updatedAt', '更新时间', { date: true })],
  },
  'purchase.orders': {
    query: queryPurchaseOrders,
    detail: getDetail('/purchase/v1/purchase-orders'),
    rowKey: ['orderNo', 'id'],
    columns: [column('orderNo', '采购订单'), column('supplierName', '供应商'), column('purchaseOrgId', '采购组织'), column('warehouseCode', '仓库'), column('currency', '币种'), column('taxIncludedAmount', '含税金额', { number: true }), column('statusName', '状态', { status: true })],
    actions: [
      { key: 'submit', label: '提交', permission: 'purchase:po:submit', tone: 'primary', visible: (row) => row.status === 1, run: (row) => submitPurchaseOrder(row.orderNo, row.version) },
      { key: 'approve', label: '审批', permission: 'purchase:po:approve', tone: 'primary', visible: (row) => row.status === 2, run: (row) => approvePurchaseOrder(row.orderNo, row.version) },
      { key: 'publish', label: '发布', permission: 'purchase:po:publish', tone: 'primary', visible: (row) => row.status === 3, run: (row) => publishPurchaseOrder(row.orderNo, row.version) },
      { key: 'cancel', label: '取消', permission: 'purchase:po:cancel', danger: true, visible: (row) => [1, 2, 3, 4, 6, 12].includes(row.status), run: (row, text) => cancelPurchaseOrder(row.orderNo, row.version, reason(text)) },
      { key: 'close', label: '关闭剩余', permission: 'purchase:po:close', visible: (row) => [5, 6, 7, 12].includes(row.status), run: (row, text) => closePurchaseOrder(row.orderNo, row.version, reason(text)) },
    ],
  },
  'purchase.supplier-confirms': {
    query: querySupplierConfirms,
    detail: getDetail('/purchase/v1/supplier-confirms'),
    rowKey: ['confirmId', 'id'],
    columns: [column('confirmId', '确认 ID'), column('orderNo', '采购订单'), column('supplierId', '供应商'), column('eventCode', '事件'), column('confirmStatus', '确认状态'), column('processedStatusName', '处理状态', { status: true }), column('occurredAt', '发生时间', { date: true })],
    actions: [
      { key: 'accept', label: '接受差异', permission: 'purchase:supplier_confirm:accept_diff', tone: 'primary', visible: (row) => row.processedStatus === 1, run: (row, text) => acceptSupplierConfirmDiff(row.confirmId, row.version, reason(text)) },
      { key: 'renegotiate', label: '重新协商', permission: 'purchase:supplier_confirm:renegotiate', visible: (row) => row.processedStatus === 1, run: (row, text) => renegotiateSupplierConfirm(row.confirmId, row.version, '请供应商重新确认数量、价格或交期', reason(text)) },
      { key: 'cancel', label: '取消订单', permission: 'purchase:supplier_confirm:cancel_order', danger: true, visible: (row) => row.processedStatus === 1, run: (row, text) => cancelSupplierConfirmOrder(row.confirmId, row.version, reason(text)) },
    ],
  },
  'purchase.inbound-tracks': {
    query: queryInbounds,
    detail: getDetail('/purchase/v1/inbounds'),
    rowKey: ['inboundNo', 'id'],
    columns: [column('inboundNo', '到货单'), column('orderNo', '采购订单'), column('asnNo', 'ASN'), column('supplierId', '供应商'), column('warehouseCode', '仓库'), column('skuCode', 'SKU'), column('receivedQty', '收货数量'), column('putawayQty', '上架数量'), column('statusName', '状态', { status: true })],
  },
  'purchase.supplier-returns': {
    query: get('/purchase/v1/supplier-returns'),
    detail: getDetail('/purchase/v1/supplier-returns'),
    rowKey: ['returnNo', 'id'],
    columns: [column('returnNo', '退供单号'), column('orderNo', '采购订单'), column('supplierId', '供应商'), column('warehouseCode', '仓库'), column('statusName', '状态', { status: true }), column('updatedAt', '更新时间', { date: true })],
  },
  'purchase.prices': {
    query: get('/purchase/v1/purchase-prices'),
    detail: getDetail('/purchase/v1/purchase-prices'),
    rowKey: ['priceNo', 'id'],
    columns: [column('priceNo', '价格编号'), column('supplierId', '供应商'), column('skuCode', 'SKU'), column('taxIncludedPrice', '含税价', { number: true }), column('currency', '币种'), column('statusName', '状态', { status: true })],
  },
  'purchase.failed-events': {
    query: queryPurchaseFailedEvents,
    rowKey: ['id', 'eventId'],
    columns: [column('id', '事件 ID'), column('sourceSystem', '来源系统'), column('eventCode', '事件编码'), column('eventType', '事件类型'), column('retryCount', '重试次数'), column('reason', '失败原因'), column('updatedAt', '更新时间', { date: true })],
    actions: [{ key: 'replay', label: '重放', permission: 'purchase:event:replay', tone: 'primary', run: (row, text) => replayPurchaseFailedEvent(row.id, reason(text)) }],
  },
}
