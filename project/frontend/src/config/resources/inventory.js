import {
  confirmInventoryReconciliation,
  dispatchInventoryOutbox,
  generateInventorySnapshot,
  queryInventoryLedgers,
  queryInventoryReconciliations,
  queryInventorySnapshots,
  queryInventoryStocks,
} from '../../api/warehouseInventory'
import client from '../../api/client'
import { column } from './helpers'

const operationQuery = (path) => ({ keyword, ...params }) => client.get(path, {
  params: { ...params, sku: keyword || undefined },
})

const exportInventory = (exportType) => client.post(
  '/inventory/v1/exports',
  { exportType, query: {} },
  { headers: { 'X-Idempotency-Key': globalThis.crypto?.randomUUID?.() || `inventory-export-${Date.now()}` } },
)

const exportAction = (exportType, label = '异步导出') => ({
  key: 'export',
  label,
  permission: 'inventory:stock:export',
  run: () => exportInventory(exportType),
})

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
      { ...exportAction('BOOK_PHYSICAL', '导出账实指标'), key: 'export-book-physical' },
      { ...exportAction('STOCK_AGE', '导出库龄指标'), key: 'export-stock-age' },
      { ...exportAction('SLOW_MOVING', '导出滞销指标'), key: 'export-slow-moving' },
      { ...exportAction('EXPIRY', '导出效期指标'), key: 'export-expiry' },
    ],
  }
}

export const inventoryResources = {
  'inventory.balances': inventoryStocks(),
  'inventory.available': inventoryStocks(),
  'inventory.reservations': {
    query: operationQuery('/inventory/v1/reservations'),
    rowKey: ['reservationNo'],
    columns: [column('reservationNo', '预占单号'), column('warehouseId', '仓库'), column('ownerId', '货主'), column('sku', 'SKU'), column('batchNo', '批次'), column('sourceSystem', '来源系统'), column('sourceNo', '来源单号'), column('reservedQty', '预占数量', { number: true }), column('releasedQty', '已释放数量', { number: true }), column('status', '状态', { status: true }), column('updatedAt', '更新时间', { date: true })],
    pageActions: [exportAction('RESERVATION')],
  },
  'inventory.freezes': {
    query: operationQuery('/inventory/v1/freezes'),
    rowKey: ['freezeNo'],
    columns: [column('freezeNo', '冻结单号'), column('warehouseId', '仓库'), column('ownerId', '货主'), column('sku', 'SKU'), column('batchNo', '批次'), column('freezeQty', '冻结数量', { number: true }), column('unfrozenQty', '已解冻数量', { number: true }), column('reason', '冻结原因'), column('status', '状态', { status: true }), column('approvalStatus', '审批状态', { status: true }), column('updatedAt', '更新时间', { date: true })],
    pageActions: [exportAction('FREEZE')],
  },
  'inventory.adjustments': {
    query: operationQuery('/inventory/v1/adjustments'),
    rowKey: ['adjustmentNo'],
    columns: [column('adjustmentNo', '调整单号'), column('warehouseId', '仓库'), column('ownerId', '货主'), column('sku', 'SKU'), column('batchNo', '批次'), column('adjustQty', '调整数量', { number: true }), column('adjustmentType', '调整类型'), column('reason', '调整原因'), column('status', '状态', { status: true }), column('approvalStatus', '审批状态', { status: true }), column('updatedAt', '更新时间', { date: true })],
    pageActions: [exportAction('ADJUSTMENT')],
  },
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
  'inventory.event-logs': {
    query: operationQuery('/inventory/v1/event-logs'),
    rowKey: ['eventCode', 'direction'],
    columns: [column('eventCode', '事件编码'), column('direction', '方向'), column('sourceSystem', '来源系统'), column('eventType', '事件类型'), column('eventVersion', '版本'), column('ownerId', '货主'), column('warehouseId', '仓库'), column('status', '状态', { status: true }), column('retryCount', '重试次数', { number: true }), column('lastError', '最近错误'), column('updatedAt', '更新时间', { date: true })],
    pageActions: [exportAction('EVENT_LOG')],
  },
  'inventory.operation-logs': {
    query: operationQuery('/inventory/v1/operation-logs'),
    rowKey: ['logId'],
    columns: [column('logId', '日志 ID'), column('operationType', '操作类型'), column('targetType', '对象类型'), column('targetNo', '对象编号'), column('ownerId', '货主'), column('warehouseId', '仓库'), column('operatorId', '操作人'), column('operationReason', '操作原因'), column('result', '结果', { status: true }), column('requestId', '请求 ID'), column('operationAt', '操作时间', { date: true })],
    pageActions: [exportAction('OPERATION_LOG')],
  },
}
