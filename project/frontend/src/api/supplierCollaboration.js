import client from './client'

export const queryPoConfirms = (params) => client.get('/supplier/v1/po-confirms', { params })
export const confirmPo = (id, data) => client.post(`/supplier/v1/po-confirms/${id}/confirm`, data)
export const rejectPo = (id, data) => client.post(`/supplier/v1/po-confirms/${id}/reject`, data)
export const feedbackPoDifference = (id, data) => client.post(`/supplier/v1/po-confirms/${id}/feedback-diff`, data)

export const queryQualityIssues = (params) => client.get('/supplier/v1/quality-issues', { params })
export const requestRectification = (id, data) => client.post(`/supplier/v1/quality-issues/${id}/request-rectification`, data)
export const submitRectificationPlan = (id, data) => client.post(`/supplier/v1/quality-issues/${id}/submit-plan`, data)
export const verifyRectification = (id, data) => client.post(`/supplier/v1/quality-issues/${id}/verify`, data)
