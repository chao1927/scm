import client from './client'

export const queryRequisitions = (params) => client.get('/purchase/v1/requisitions', { params })
export const submitRequisition = (id, version) => client.post(`/purchase/v1/requisitions/${id}/submit`, { version })
export const approveRequisition = (id, version, approvedQuantities) =>
  client.post(`/purchase/v1/requisitions/${id}/approve`, { version, approvedQuantities })
export const rejectRequisition = (id, version, reason) =>
  client.post(`/purchase/v1/requisitions/${id}/reject`, { version, reason })

export const queryRfqs = (params) => client.get('/purchase/v1/rfqs', { params })
export const publishRfq = (rfqNo, version) => client.post(`/purchase/v1/rfqs/${rfqNo}/publish`, { version })
export const closeRfq = (rfqNo, version, reason) => client.post(`/purchase/v1/rfqs/${rfqNo}/close`, { version, reason })

export const queryPurchaseOrders = (params) => client.get('/purchase/v1/purchase-orders', { params })
export const submitPurchaseOrder = (orderNo, version) =>
  client.post(`/purchase/v1/purchase-orders/${orderNo}/submit`, { version })
export const approvePurchaseOrder = (orderNo, version, approved = true, reason = null) =>
  client.post(`/purchase/v1/purchase-orders/${orderNo}/approve`, { version, approved, reason })
export const publishPurchaseOrder = (orderNo, version, publishMode = 'STANDARD') =>
  client.post(`/purchase/v1/purchase-orders/${orderNo}/publish`, { version, publishMode })
export const cancelPurchaseOrder = (orderNo, version, reason) =>
  client.post(`/purchase/v1/purchase-orders/${orderNo}/cancel`, { version, reason })
export const closePurchaseOrder = (orderNo, version, reason) =>
  client.post(`/purchase/v1/purchase-orders/${orderNo}/close`, { version, reason })

export const queryInbounds = (params) => client.get('/purchase/v1/inbounds', { params })

export const querySupplierConfirms = (params) => client.get('/purchase/v1/supplier-confirms', { params })
export const acceptSupplierConfirmDiff = (confirmId, version, comment) =>
  client.post(`/purchase/v1/supplier-confirms/${confirmId}/accept-diff`, { version, comment })
export const renegotiateSupplierConfirm = (confirmId, version, requirement, comment) =>
  client.post(`/purchase/v1/supplier-confirms/${confirmId}/renegotiate`, { version, requirement, comment })
export const cancelSupplierConfirmOrder = (confirmId, version, reason) =>
  client.post(`/purchase/v1/supplier-confirms/${confirmId}/cancel-order`, { version, reason })

export const queryPurchaseFailedEvents = () => client.get('/purchase/v1/operations/failed-events')
export const replayPurchaseFailedEvent = (id, reason) => client.post(`/purchase/v1/operations/failed-events/${id}/replay`, { reason })
