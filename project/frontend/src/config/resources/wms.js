import {
  dispatchWmsOutbox,
  queryWmsInboxFailedEvents,
  queryWmsOutboxFailedEvents,
  replayWmsInboxFailedEvent,
  retryWmsOutboxFailedEvent,
} from '../../api/warehouseInventory'
import client from '../../api/client'
import { column, extractRecords, get, getDetail, simpleResource } from './helpers'

export const wmsResources = {
  'wms.inbounds': {
    ...simpleResource(get('/wms/v1/inbound-orders'), ['inboundOrderNo', 'inboundId'], [
      ['inboundOrderNo', '入库单号'],
      ['sourceType', '来源类型'],
      ['sourceOrderNo', '来源单号'],
      ['warehouseId', '仓库'],
      ['ownerId', '货主'],
      ['receivedQty', '已收数量', 'number'],
      ['qualifiedQty', '合格数量', 'number'],
      ['putawayQty', '已上架数量', 'number'],
      ['statusName', '状态', 'status'],
      ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/wms/v1/inbound-orders'),
  },
  'wms.receipts': {
    ...simpleResource(get('/wms/v1/receipts'), ['receiptNo', 'receiptId'], [
      ['receiptNo', '收货单号'],
      ['inboundOrderNo', '入库单号'],
      ['warehouseId', '仓库'],
      ['ownerId', '货主'],
      ['skuCode', 'SKU'],
      ['expectedQty', '应收数量', 'number'],
      ['receivedQty', '实收数量', 'number'],
      ['differenceQty', '差异数量', 'number'],
      ['statusName', '状态', 'status'],
    ]),
    detail: getDetail('/wms/v1/receipts'),
  },
  'wms.qc-orders': {
    ...simpleResource(get('/wms/v1/inspections'), ['inspectionNo', 'inspectionId'], [
      ['inspectionNo', '质检单号'],
      ['receiptNo', '收货单号'],
      ['warehouseId', '仓库'],
      ['ownerId', '货主'],
      ['skuCode', 'SKU'],
      ['inspectQty', '质检数量', 'number'],
      ['qualifiedQty', '合格数量', 'number'],
      ['unqualifiedQty', '不合格数量', 'number'],
      ['statusName', '状态', 'status'],
    ]),
    detail: getDetail('/wms/v1/inspections'),
  },
  'wms.putaway-tasks': {
    ...simpleResource(get('/wms/v1/putaway-tasks'), ['taskNo', 'taskId'], [
      ['taskNo', '上架任务号'],
      ['inspectionNo', '质检单号'],
      ['warehouseId', '仓库'],
      ['ownerId', '货主'],
      ['skuCode', 'SKU'],
      ['requiredQty', '应上架数量', 'number'],
      ['putawayQty', '已上架数量', 'number'],
      ['statusName', '状态', 'status'],
    ]),
    detail: getDetail('/wms/v1/putaway-tasks'),
  },
  'wms.stocks': {
    ...simpleResource(get('/wms/v1/stocks'), ['stockKey'], [
      ['warehouseId', '仓库'],
      ['ownerId', '货主'],
      ['locationCode', '库位'],
      ['skuCode', 'SKU'],
      ['batchNo', '批次'],
      ['quantity', '库位数量', 'number'],
      ['lastOccurredAt', '最近变动时间', 'date'],
    ]),
    detail: getDetail('/wms/v1/stocks'),
  },
  'wms.outbounds': {
    ...simpleResource(get('/wms/v1/outbound-orders'), ['outboundNo', 'outboundId'], [
      ['outboundNo', '出库单号'], ['sourceType', '来源类型'], ['sourceNo', '来源单号'],
      ['warehouseId', '仓库'], ['ownerId', '货主'], ['requiredQty', '应拣数量', 'number'],
      ['pickedQty', '已拣数量', 'number'], ['containerCount', '容器数', 'number'],
      ['statusName', '状态', 'status'], ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/wms/v1/outbound-orders'),
  },
  'wms.waves': {
    ...simpleResource(get('/wms/v1/waves'), ['waveNo', 'waveId'], [
      ['waveNo', '波次号'], ['warehouseId', '仓库'], ['ownerId', '货主'],
      ['outboundCount', '出库单数', 'number'], ['taskCount', '拣货任务数', 'number'],
      ['requiredQty', '应拣数量', 'number'], ['pickedQty', '已拣数量', 'number'],
      ['statusName', '状态', 'status'], ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/wms/v1/waves'),
  },
  'wms.picking-orders': {
    ...simpleResource(get('/wms/v1/picking-orders'), ['taskNo', 'taskId'], [
      ['taskNo', '拣货单号'], ['waveNo', '波次号'], ['outboundNo', '出库单号'],
      ['sourceNo', '来源单号'], ['warehouseId', '仓库'], ['ownerId', '货主'],
      ['skuCode', 'SKU'], ['requiredQty', '应拣数量', 'number'],
      ['pickedQty', '已拣数量', 'number'], ['statusName', '状态', 'status'],
    ]),
    detail: getDetail('/wms/v1/picking-orders'),
  },
  'wms.pack-orders': {
    ...simpleResource(get('/wms/v1/pack-orders'), ['packingNo', 'packingId'], [
      ['packingNo', '包装单号'], ['outboundNo', '出库单号'], ['sourceNo', '来源单号'],
      ['warehouseId', '仓库'], ['ownerId', '货主'], ['containerNo', '容器号'],
      ['containerStatusName', '容器状态', 'status'], ['statusName', '复核状态', 'status'],
      ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/wms/v1/pack-orders'),
  },
  'wms.shipments': {
    ...simpleResource(get('/wms/v1/shipments'), ['handoverNo', 'handoverId'], [
      ['handoverNo', '交接单号'], ['outboundNo', '出库单号'], ['sourceNo', '来源单号'],
      ['warehouseId', '仓库'], ['ownerId', '货主'], ['packingCount', '包装单数', 'number'],
      ['statusName', '状态', 'status'], ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/wms/v1/shipments'),
  },
  'wms.return-receipts': {
    ...simpleResource(get('/wms/v1/return-receipts'), ['afterSaleNo', 'operationId'], [
      ['afterSaleNo', '售后单号'], ['rmaNo', 'RMA 单号'], ['warehouseId', '仓库'],
      ['ownerId', '货主'], ['skuCode', 'SKU'], ['expectedQty', '应收数量', 'number'],
      ['receivedQty', '实收数量', 'number'], ['sellableQty', '可售数量', 'number'],
      ['defectiveQty', '残次数量', 'number'], ['statusName', '状态', 'status'],
    ]),
    detail: getDetail('/wms/v1/return-receipts'),
  },
  'wms.count-plans': {
    ...simpleResource(get('/wms/v1/stocktakes'), ['stocktakeNo', 'stocktakeId'], [
      ['stocktakeNo', '盘点单号'], ['warehouseId', '仓库'], ['ownerId', '货主'],
      ['skuCode', 'SKU'], ['differenceQty', '差异数量', 'number'],
      ['statusName', '状态', 'status'], ['version', '版本'], ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/wms/v1/stocktakes'),
    actions: [{
      key: 'confirm-difference',
      label: '确认差异',
      permission: 'wms:operation:write',
      tone: 'primary',
      visible: (row) => row.status === 1,
      run: (row) => client.post('/wms/v1/stocktakes/confirm-difference', {
        no: row.stocktakeNo,
        warehouseId: row.warehouseId,
        ownerId: row.ownerId,
        version: row.version,
      }),
    }],
  },
  'wms.exceptions': {
    ...simpleResource(get('/wms/v1/warehouse-exceptions'), ['exceptionNo', 'exceptionId'], [
      ['exceptionNo', '异常单号'], ['warehouseId', '仓库'], ['ownerId', '货主'],
      ['reason', '异常原因'], ['statusName', '状态', 'status'], ['version', '版本'],
      ['updatedAt', '更新时间', 'date'],
    ]),
    detail: getDetail('/wms/v1/warehouse-exceptions'),
    actions: [{
      key: 'close',
      label: '关闭异常',
      permission: 'wms:operation:write',
      tone: 'primary',
      visible: (row) => row.status === 1,
      run: (row) => client.post('/wms/v1/warehouse-exceptions/close', {
        no: row.exceptionNo,
        warehouseId: row.warehouseId,
        ownerId: row.ownerId,
        version: row.version,
      }),
    }],
  },
  'wms.failed-events': {
    query: async (params) => {
      const [outbox, inbox] = await Promise.all([
        queryWmsOutboxFailedEvents(params),
        queryWmsInboxFailedEvents(params),
      ])
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
      return {
        data: {
          records: [...outboxRecords, ...inboxRecords],
          total: outboxRecords.length + inboxRecords.length,
          pageNo: 1,
          pageSize: params.pageSize,
        },
      }
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
}
