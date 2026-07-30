import {
  confirmInventoryReconciliation,
  dispatchInventoryOutbox,
  generateInventorySnapshot,
  queryInventoryLedgers,
  queryInventoryReconciliations,
  queryInventorySnapshots,
  queryInventoryStocks,
} from '../../api/warehouseInventory'
import { column } from './helpers'

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

export const inventoryResources = {
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
}
