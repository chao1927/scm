import client, { openApiClient } from './client'

export const login = (username, password, deviceDigest) => client.post('/iam/v1/auth/login', {
  username, password, appCode: 'SCM_WEB', deviceDigest,
})
export const verifyMfaChallenge = (challengeNo, sessionId, deviceDigest, method, code) => client.post(
  `/iam/v1/mfa/challenges/${encodeURIComponent(challengeNo)}/verify`,
  { method, code, sessionId, purpose: 'LOGIN', deviceDigest },
)
export const completeMfaLogin = (challengeNo, sessionId, deviceDigest) => client.post(
  '/iam/v1/auth/mfa/complete', { challengeNo, sessionId, deviceDigest },
)
export const logout = (refreshToken) => client.post('/iam/v1/auth/logout', { refreshToken })
export const queryCurrentUser = () => client.get('/iam/v1/me')
export const queryMenus = () => client.get('/iam/v1/menus', { params: { appCode: 'SCM_WEB' } })
export const queryPermissionSnapshot = () => openApiClient.get('/openapi/iam/v1/users/me/permissions', {
  headers: { 'X-App-Code': 'SCM_WEB' },
})
