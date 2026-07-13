import client from './client'

export const dispatchWmsOutbox = (limit) => client.post('/wms/v1/operations/outbox/dispatch', { limit })
export const queryWmsOutboxFailedEvents = (params) => client.get('/wms/v1/operations/outbox/failed-events', { params })
export const retryWmsOutboxFailedEvent = (eventId) => client.post(`/wms/v1/operations/outbox/failed-events/${eventId}/retry`)
export const queryWmsInboxFailedEvents = (params) => client.get('/wms/v1/operations/inbox/failed-events', { params })
export const replayWmsInboxFailedEvent = (inboxId) => client.post(`/wms/v1/operations/inbox/failed-events/${inboxId}/replay`)

export const queryInventoryStocks = (params) => client.get('/inventory/v1/stocks', { params })
export const queryInventoryLedgers = (params) => client.get('/inventory/v1/stock-ledgers', { params })
export const dispatchInventoryOutbox = (limit) => client.post('/inventory/v1/operations/outbox/dispatch', { limit })
export const generateInventorySnapshot = (accountId) => client.post('/inventory/v1/snapshots/generate', { accountId })
export const queryInventorySnapshots = (params) => client.get('/inventory/v1/snapshots', { params })
export const queryInventoryReconciliations = (params) => client.get('/inventory/v1/inventory-reconciliations', { params })
export const confirmInventoryReconciliation = (reconcileNo, version) =>
  client.post(`/inventory/v1/inventory-reconciliations/${reconcileNo}/confirm`, { version })
