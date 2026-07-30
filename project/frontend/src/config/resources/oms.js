import {
  queryOmsChannelOrders,
  queryOmsFulfillments,
  queryOmsOutbounds,
  queryOmsSalesOrders,
} from '../../api/fulfillmentSettlement'
import { get, getDetail, simpleResource } from './helpers'

export const omsResources = {
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
