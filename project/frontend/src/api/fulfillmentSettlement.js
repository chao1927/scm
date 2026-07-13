import client from './client'

export const queryOmsChannelOrders = () => client.get('/oms/v1/channel-orders')
export const queryOmsSalesOrders = () => client.get('/oms/v1/sales-orders')
export const queryOmsFulfillments = () => client.get('/oms/v1/fulfillments')
export const queryOmsOutbounds = () => client.get('/oms/v1/outbounds')

export const queryTmsTransportTasks = (params) => client.get('/tms/v1/transport-tasks', { params })
export const queryTmsWaybills = () => client.get('/tms/v1/waybills')
export const queryTmsExceptions = () => client.get('/tms/v1/transport-exceptions')
export const queryTmsFeeSources = () => client.get('/tms/v1/fee-sources')

export const queryBmsBillingObjects = (params) => client.get('/bms/v1/billing-subjects', { params })
export const queryBmsChargeSources = (params) => client.get('/bms/v1/charge-sources', { params })
export const queryBmsSettlementSummary = (params) => client.get('/bms/v1/reports/settlement-summary', { params })
