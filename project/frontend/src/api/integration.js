import client from './client'

const operator = () => Number(sessionStorage.getItem('operator_id') || '1')
const idempotencyKey = () => crypto.randomUUID()

export const listRoutes = () => client.get('/integration/v1/routes')
export const createRoute = (data) => client.post('/integration/v1/routes', {
  ...data,
  operatorId: operator(),
  idempotencyKey: idempotencyKey(),
})
export const disableRoute = (routeNo, expectedVersion) => client.post(`/integration/v1/routes/${routeNo}/disable`, {
  expectedVersion,
  operatorId: operator(),
  idempotencyKey: idempotencyKey(),
})

export const listEndpoints = () => client.get('/integration/v1/endpoints')
export const createEndpoint = (data) => client.post('/integration/v1/endpoints', {
  ...data,
  operatorId: operator(),
  idempotencyKey: idempotencyKey(),
})
export const disableEndpoint = (endpointNo, expectedVersion) =>
  client.post(`/integration/v1/endpoints/${endpointNo}/disable`, {
    expectedVersion,
    operatorId: operator(),
    idempotencyKey: idempotencyKey(),
  })
export const verifyEndpoint = (endpointNo) => client.post(`/integration/v1/endpoints/${endpointNo}/verify`)

export const listMessages = (params) => client.get('/integration/v1/messages', { params })
export const dispatchMessage = (messageNo, data) => client.post(`/integration/v1/messages/${messageNo}/dispatch`, {
  ...data,
  operatorId: operator(),
  idempotencyKey: idempotencyKey(),
})
export const retryMessage = (messageNo, expectedVersion) => client.post(`/integration/v1/messages/${messageNo}/retry`, {
  expectedVersion,
  operatorId: operator(),
  idempotencyKey: idempotencyKey(),
})

export const listDeadLetters = () => client.get('/integration/v1/dead-letters')
export const replayDeadLetter = (deadLetterNo) => client.post(`/integration/v1/dead-letters/${deadLetterNo}/replay`, {
  operatorId: operator(),
  idempotencyKey: idempotencyKey(),
})

export const runDispatch = (limit) => client.post('/integration/v1/dispatch-runs', {
  limit,
  operatorId: operator(),
  idempotencyKey: idempotencyKey(),
})
export const listDeliveryAttempts = (params) => client.get('/integration/v1/delivery-attempts', { params })
export const getIntegrationSummary = () => client.get('/integration/v1/operations/summary')
