import client from '../api/client'
import { cancelAsn, queryAsnDetail, queryAsns, submitAsn } from '../api/asn'
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
} from '../api/purchase'
import {
  confirmPo,
  feedbackPoDifference,
  queryPoConfirms,
  queryQualityIssues,
  rejectPo,
  requestRectification,
  submitRectificationPlan,
  verifyRectification,
} from '../api/supplierCollaboration'
import {
  confirmInventoryReconciliation,
  dispatchInventoryOutbox,
  dispatchWmsOutbox,
  generateInventorySnapshot,
  queryInventoryLedgers,
  queryInventoryReconciliations,
  queryInventorySnapshots,
  queryInventoryStocks,
  queryWmsInboxFailedEvents,
  queryWmsOutboxFailedEvents,
  replayWmsInboxFailedEvent,
  retryWmsOutboxFailedEvent,
} from '../api/warehouseInventory'
import {
  queryBmsBillingObjects,
  queryBmsChargeSources,
  queryBmsSettlementSummary,
  queryOmsChannelOrders,
  queryOmsFulfillments,
  queryOmsOutbounds,
  queryOmsSalesOrders,
  queryTmsExceptions,
  queryTmsFeeSources,
  queryTmsTransportTasks,
  queryTmsWaybills,
} from '../api/fulfillmentSettlement'
import {
  queryIamApps,
  queryIamApprovals,
  queryIamOperationLogs,
  queryIamPermissions,
  queryIamRoles,
  queryIamSecurityPolicies,
  queryIamSsoClients,
  queryIamUsers,
  queryMdmCodeRules,
  queryMdmImportTasks,
  queryMdmPublications,
  queryMdmQualityIssues,
  queryMdmRecords,
  queryMdmTemplates,
  queryMdmTypes,
} from '../api/mdmIam'

const column = (key, title, options = {}) => ({ key, title, ...options })
const get = (path) => (params) => client.get(path, { params })
const getDetail = (path, key) => (id) => client.get(`${path}/${encodeURIComponent(id)}`, key ? { params: { [key]: id } } : undefined)

const reason = (text) => text || '前端业务页面操作'

export const resourceDefinitions = {
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

  'wms.failed-events': {
    query: async (params) => {
      const [outbox, inbox] = await Promise.all([queryWmsOutboxFailedEvents(params), queryWmsInboxFailedEvents(params)])
      const outboxRecords = extractRecords(outbox).map((row) => ({
        ...row,
        __eventBox: 'outbox',
        eventId: row.eventId ?? row.id,
        eventCode: row.eventCode ?? row.code,
        eventType: row.eventType ?? row.type,
        reason: row.reason ?? row.lastError,
      }))
      const inboxRecords = extractRecords(inbox).map((row) => ({
        ...row,
        __eventBox: 'inbox',
        eventId: row.eventId ?? row.id,
        eventCode: row.eventCode,
        eventType: row.eventType,
        reason: row.reason ?? row.lastError,
      }))
      return { data: { records: [...outboxRecords, ...inboxRecords], total: outboxRecords.length + inboxRecords.length, pageNo: 1, pageSize: params.pageSize } }
    },
    rowKey: ['eventId', 'id', 'inboxId'],
    columns: [column('eventId', '事件 ID'), column('eventCode', '事件编码'), column('eventType', '事件类型'), column('sourceSystem', '来源系统'), column('retryCount', '重试次数'), column('reason', '失败原因')],
    actions: [
      { key: 'retry-outbox', label: '重试 Outbox', permission: 'wms:operations:manage', tone: 'primary', visible: (row) => row.__eventBox === 'outbox', run: (row) => retryWmsOutboxFailedEvent(row.id ?? row.eventId) },
      { key: 'replay-inbox', label: '重放 Inbox', permission: 'wms:event:manage', tone: 'primary', visible: (row) => row.__eventBox === 'inbox', run: (row) => replayWmsInboxFailedEvent(row.id ?? row.inboxId) },
    ],
    pageActions: [
      { key: 'dispatch', label: '投递 WMS Outbox', permission: 'wms:operations:manage', run: () => dispatchWmsOutbox(50) },
    ],
  },

  'inventory.balances': inventoryStocks(),
  'inventory.available': inventoryStocks(),
  'inventory.ledgers': {
    query: queryInventoryLedgers,
    rowKey: ['ledgerNo', 'id'],
    columns: [column('ledgerNo', '流水编号'), column('accountId', '库存账户'), column('type', '业务类型'), column('qtyDelta', '数量变化', { number: true }), column('sourceSystem', '来源系统'), column('sourceNo', '来源单号')],
  },
  'inventory.snapshots': {
    query: queryInventorySnapshots,
    rowKey: ['snapshotNo', 'id'],
    columns: [column('snapshotNo', '快照编号'), column('accountId', '库存账户'), column('onHandQty', '在手数量', { number: true }), column('availableQty', '可用数量', { number: true }), column('snapshotAt', '快照时间', { date: true })],
  },
  'inventory.reconciliations': {
    query: queryInventoryReconciliations,
    rowKey: ['reconcileNo', 'id'],
    columns: [column('reconcileNo', '对账编号'), column('accountId', '库存账户'), column('systemQty', '系统数量', { number: true }), column('wmsQty', 'WMS 数量', { number: true }), column('differenceQty', '差异', { number: true }), column('status', '状态', { status: true }), column('version', '版本')],
    actions: [
      { key: 'confirm', label: '确认对账', permission: 'inventory:reconciliation:manage', tone: 'primary', visible: (row) => row.status === 1, run: (row) => confirmInventoryReconciliation(row.reconcileNo, row.version) },
    ],
  },

  'oms.channel-orders': simpleResource(queryOmsChannelOrders, ['channelOrderNo', 'orderNo'], [['channelOrderNo', '渠道订单'], ['channelCode', '渠道'], ['externalOrderNo', '外部订单'], ['statusName', '状态', 'status'], ['createdAt', '创建时间', 'date']]),
  'oms.sales-orders': { ...simpleResource(queryOmsSalesOrders, ['orderNo', 'id'], [['orderNo', '销售订单'], ['channelCode', '渠道'], ['customerId', '客户'], ['totalAmount', '订单金额', 'number'], ['statusName', '状态', 'status'], ['createdAt', '创建时间', 'date']]), detail: getDetail('/oms/v1/sales-orders') },
  'oms.fulfillments': { ...simpleResource(queryOmsFulfillments, ['fulfillmentNo', 'id'], [['fulfillmentNo', '履约单'], ['orderNo', '销售订单'], ['warehouseCode', '履约仓'], ['promisedAt', '承诺时间', 'date'], ['statusName', '状态', 'status']]), detail: getDetail('/oms/v1/fulfillments') },
  'oms.outbounds': { ...simpleResource(queryOmsOutbounds, ['outboundNo', 'id'], [['outboundNo', '出库单'], ['fulfillmentNo', '履约单'], ['warehouseCode', '仓库'], ['wmsOrderNo', 'WMS 单号'], ['statusName', '状态', 'status']]), detail: getDetail('/oms/v1/outbounds') },

  'tms.transport-tasks': { ...simpleResource(queryTmsTransportTasks, ['taskNo', 'id'], [['taskNo', '运输任务'], ['businessNo', '业务单号'], ['carrierCode', '承运商'], ['serviceLevel', '服务等级'], ['statusName', '状态', 'status'], ['updatedAt', '更新时间', 'date']]), detail: getDetail('/tms/v1/transport-tasks') },
  'tms.waybills': { ...simpleResource(queryTmsWaybills, ['waybillNo', 'id'], [['waybillNo', '运单号'], ['taskNo', '运输任务'], ['carrierCode', '承运商'], ['trackingNo', '物流单号'], ['statusName', '状态', 'status']]), detail: getDetail('/tms/v1/waybills') },
  'tms.exceptions': simpleResource(queryTmsExceptions, ['exceptionNo', 'id'], [['exceptionNo', '异常编号'], ['waybillNo', '运单'], ['exceptionType', '异常类型'], ['description', '异常说明'], ['statusName', '状态', 'status'], ['occurredAt', '发生时间', 'date']]),
  'tms.fee-sources': simpleResource(queryTmsFeeSources, ['feeSourceNo', 'id'], [['feeSourceNo', '费用来源'], ['waybillNo', '运单'], ['feeType', '费用类型'], ['amount', '金额', 'number'], ['currency', '币种'], ['statusName', '状态', 'status']]),

  'bms.billing-subjects': simpleResource(queryBmsBillingObjects, ['subjectNo', 'id'], [['subjectNo', '计费对象'], ['businessType', '业务类型'], ['businessNo', '业务单号'], ['counterpartyName', '结算对象'], ['statusName', '状态', 'status']]),
  'bms.charge-sources': simpleResource(queryBmsChargeSources, ['sourceNo', 'id'], [['sourceNo', '费用来源'], ['businessType', '业务类型'], ['businessNo', '业务单号'], ['amount', '金额', 'number'], ['currency', '币种'], ['statusName', '状态', 'status']]),
  'bms.settlement-reports': simpleResource(queryBmsSettlementSummary, ['periodCode', 'id'], [['periodCode', '结算期间'], ['payableAmount', '应付金额', 'number'], ['receivableAmount', '应收金额', 'number'], ['settledAmount', '已结算', 'number'], ['differenceAmount', '差异金额', 'number']]),

  'mdm.types': simpleResource(queryMdmTypes, ['typeCode', 'id'], [['typeCode', '类型编码'], ['typeName', '类型名称'], ['statusName', '状态', 'status'], ['version', '版本'], ['updatedAt', '更新时间', 'date']]),
  'mdm.field-defs': simpleResource(queryMdmTemplates, ['templateNo', 'id'], [['templateNo', '模板编号'], ['typeCode', '主数据类型'], ['templateName', '模板名称'], ['version', '版本'], ['statusName', '状态', 'status']]),
  'mdm.code-rules': simpleResource(queryMdmCodeRules, ['ruleNo', 'id'], [['ruleNo', '规则编号'], ['typeCode', '主数据类型'], ['ruleName', '规则名称'], ['pattern', '编码格式'], ['statusName', '状态', 'status']]),
  'mdm.products': mdmRecords(),
  'mdm.partners': mdmRecords(),
  'mdm.warehouses': mdmRecords(),
  'mdm.imports': simpleResource(queryMdmImportTasks, ['importTaskNo', 'id'], [['importTaskNo', '导入任务'], ['typeCode', '主数据类型'], ['fileName', '文件名'], ['successCount', '成功数量'], ['failedCount', '失败数量'], ['statusName', '状态', 'status']]),
  'mdm.quality-issues': simpleResource(queryMdmQualityIssues, ['issueNo', 'id'], [['issueNo', '问题编号'], ['typeCode', '主数据类型'], ['recordNo', '数据编号'], ['ruleCode', '规则'], ['severityName', '严重程度'], ['statusName', '状态', 'status']]),
  'mdm.publishes': simpleResource(queryMdmPublications, ['publicationNo', 'id'], [['publicationNo', '发布编号'], ['typeCode', '主数据类型'], ['recordNo', '数据编号'], ['targetSystem', '目标系统'], ['statusName', '状态', 'status'], ['publishedAt', '发布时间', 'date']]),

  'iam.users': simpleResource(queryIamUsers, ['userNo', 'id'], [['username', '用户名'], ['displayName', '姓名'], ['organizationName', '组织'], ['mobile', '手机号'], ['statusName', '状态', 'status'], ['lastLoginAt', '最近登录', 'date']]),
  'iam.roles': simpleResource(queryIamRoles, ['roleCode', 'id'], [['roleCode', '角色编码'], ['roleName', '角色名称'], ['appCode', '应用'], ['userCount', '用户数量'], ['statusName', '状态', 'status']]),
  'iam.permissions': simpleResource(queryIamPermissions, ['permissionCode', 'id'], [['permissionCode', '权限编码'], ['permissionName', '权限名称'], ['resourceType', '资源类型'], ['appCode', '应用'], ['statusName', '状态', 'status']]),
  'iam.approvals': simpleResource(queryIamApprovals, ['approvalNo', 'id'], [['approvalNo', '审批编号'], ['businessType', '业务类型'], ['applicantName', '申请人'], ['currentNodeName', '当前节点'], ['statusName', '状态', 'status'], ['createdAt', '申请时间', 'date']]),
  'iam.operation-logs': simpleResource(queryIamOperationLogs, ['logNo', 'id'], [['operatorName', '操作人'], ['operationType', '操作类型'], ['resourceName', '资源'], ['resultName', '结果'], ['ipAddress', 'IP 地址'], ['occurredAt', '操作时间', 'date']]),
  'iam.security-policies': simpleResource(queryIamSecurityPolicies, ['policyCode', 'id'], [['policyCode', '策略编码'], ['policyName', '策略名称'], ['policyType', '策略类型'], ['statusName', '状态', 'status'], ['updatedAt', '更新时间', 'date']]),
  'iam.apps': simpleResource(queryIamApps, ['appCode', 'id'], [['appCode', '应用编码'], ['appName', '应用名称'], ['clientType', '客户端类型'], ['statusName', '状态', 'status']]),
  'iam.sso-clients': simpleResource(queryIamSsoClients, ['clientId', 'id'], [['clientId', '客户端 ID'], ['clientName', '客户端名称'], ['grantTypes', '授权类型'], ['redirectUris', '回调地址'], ['statusName', '状态', 'status']]),
}

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

function inventoryStocks() {
  return {
    query: queryInventoryStocks,
    rowKey: ['id', 'accountId', 'accountNo'],
    columns: [column('id', '库存账户'), column('warehouseId', '仓库'), column('ownerId', '货主'), column('sku', 'SKU'), column('batchNo', '批次'), column('onHandQty', '在手数量', { number: true }), column('availableQty', '可用数量', { number: true }), column('reservedQty', '预占数量', { number: true }), column('frozenQty', '冻结数量', { number: true })],
    actions: [
      { key: 'snapshot', label: '生成快照', permission: 'inventory:reconciliation:manage', run: (row) => generateInventorySnapshot(row.id ?? row.accountId) },
    ],
    pageActions: [
      { key: 'dispatch', label: '投递库存 Outbox', permission: 'inventory:event:manage', run: () => dispatchInventoryOutbox(50) },
    ],
  }
}

function mdmRecords() {
  return {
    query: queryMdmRecords,
    detail: getDetail('/mdm/v1/master-data-records'),
    rowKey: ['recordNo', 'id'],
    columns: [column('recordNo', '数据编号'), column('typeCode', '主数据类型'), column('dataCode', '业务编码'), column('dataName', '名称'), column('statusName', '状态', { status: true }), column('version', '版本'), column('updatedAt', '更新时间', { date: true })],
  }
}

function simpleResource(query, rowKey, definitions) {
  return {
    query,
    rowKey,
    columns: definitions.map(([key, title, format]) => column(key, title, {
      status: format === 'status',
      date: format === 'date',
      number: format === 'number',
    })),
  }
}

function extractRecords(result) {
  const data = result?.data ?? result
  return data?.records || data?.items || data?.content || (Array.isArray(data) ? data : [])
}

export function resourceKey(systemId, pageId) {
  return `${systemId}.${pageId}`
}

export function findResourceDefinition(systemId, pageId) {
  return resourceDefinitions[resourceKey(systemId, pageId)]
}

export function normalizePage(result, fallback = {}) {
  const data = result?.data ?? result ?? {}
  const records = data.records || data.items || data.content || data.list || (Array.isArray(data) ? data : [])
  const zeroBasedPage = Number.isFinite(Number(data.number)) ? Number(data.number) + 1 : undefined
  return {
    records,
    total: Number(data.total ?? data.totalElements ?? records.length),
    pageNo: Number(data.pageNo ?? zeroBasedPage ?? fallback.pageNo ?? 1),
    pageSize: Number(data.pageSize ?? data.size ?? fallback.pageSize ?? 20),
  }
}

export function recordIdentity(definition, record) {
  for (const key of definition?.rowKey || []) {
    if (record?.[key] !== undefined && record?.[key] !== null) return String(record[key])
  }
  return String(record?.id || record?.key || '')
}
