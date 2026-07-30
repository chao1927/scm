import {
  queryTmsCarriers,
  queryTmsExceptions,
  queryTmsFeeSources,
  queryTmsLabels,
  queryTmsOperationLogs,
  queryTmsSignatures,
  queryTmsTracks,
  queryTmsTransportTasks,
  queryTmsWaybills,
} from '../../api/fulfillmentSettlement'
import { getDetail, simpleResource } from './helpers'

export const tmsResources = {
  'tms.transport-tasks': { ...simpleResource(queryTmsTransportTasks, ['taskNo', 'id'], [['taskNo', '运输任务'], ['businessNo', '业务单号'], ['carrierCode', '承运商'], ['serviceLevel', '服务等级'], ['statusName', '状态', 'status'], ['updatedAt', '更新时间', 'date']]), detail: getDetail('/tms/v1/transport-tasks') },
  'tms.waybills': { ...simpleResource(queryTmsWaybills, ['waybillNo', 'id'], [['waybillNo', '运单号'], ['taskNo', '运输任务'], ['carrierCode', '承运商'], ['trackingNo', '物流单号'], ['statusName', '状态', 'status']]), detail: getDetail('/tms/v1/waybills') },
  'tms.labels': simpleResource(queryTmsLabels, ['labelNo'], [['labelNo', '面单号'], ['waybillNo', '运单号'], ['packageNo', '包裹号'], ['carrierName', '承运商'], ['templateVersion', '模板版本'], ['printCount', '打印次数', 'number'], ['status', '状态', 'status'], ['updatedAt', '更新时间', 'date']]),
  'tms.tracks': simpleResource(queryTmsTracks, ['trackNo'], [['trackNo', '轨迹编号'], ['waybillNo', '运单号'], ['carrierName', '承运商'], ['nodeCode', '轨迹节点'], ['description', '轨迹描述'], ['location', '当前位置'], ['trackAt', '轨迹时间', 'date']]),
  'tms.signatures': simpleResource(queryTmsSignatures, ['receiptNo'], [['receiptNo', '签收编号'], ['waybillNo', '运单号'], ['carrierName', '承运商'], ['signedBy', '签收人'], ['result', '签收结果', 'status'], ['rejectReason', '拒收原因'], ['signedAt', '签收时间', 'date']]),
  'tms.exceptions': simpleResource(queryTmsExceptions, ['exceptionNo', 'id'], [['exceptionNo', '异常编号'], ['waybillNo', '运单'], ['exceptionType', '异常类型'], ['description', '异常说明'], ['statusName', '状态', 'status'], ['occurredAt', '发生时间', 'date']]),
  'tms.fee-sources': simpleResource(queryTmsFeeSources, ['feeSourceNo', 'id'], [['feeSourceNo', '费用来源'], ['waybillNo', '运单'], ['feeType', '费用类型'], ['amount', '金额', 'number'], ['currency', '币种'], ['statusName', '状态', 'status']]),
  'tms.carriers': simpleResource(queryTmsCarriers, ['carrierCode'], [['carrierCode', '承运商编码'], ['carrierName', '承运商名称'], ['taskCount', '运输任务数', 'number'], ['waybillCount', '运单数', 'number'], ['lastUsedAt', '最近使用时间', 'date']]),
  'tms.operation-logs': simpleResource(queryTmsOperationLogs, ['idempotencyKey', 'businessNo'], [['operationType', '操作/回调类型'], ['businessNo', '业务编号'], ['carrierCode', '承运商'], ['operatorId', '操作人'], ['idempotencyKey', '幂等键'], ['createdAt', '发生时间', 'date']]),
}
