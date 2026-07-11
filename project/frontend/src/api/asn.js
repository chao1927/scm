import client from './client'

export const queryAsns = (params) => client.get('/supplier/v1/asns', { params })
export const queryAsnDetail = (asnId) => client.get(`/supplier/v1/asns/${asnId}`)
export const createAsn = (data) => client.post('/supplier/v1/asns', data)
export const submitAsn = (asnId, version) => client.post(`/supplier/v1/asns/${asnId}/submit`, { version })
export const cancelAsn = (asnId, reason, version) =>
  client.post(`/supplier/v1/asns/${asnId}/cancel`, { reason, version })
export const shipAsn = (asnId, data) => client.post(`/supplier/v1/asns/${asnId}/ship`, data)
