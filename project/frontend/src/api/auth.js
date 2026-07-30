import client, { openApiClient } from './client'

export const login = (username, password) => client.post('/iam/v1/auth/login', { username, password })
export const logout = (refreshToken) => client.post('/iam/v1/auth/logout', { refreshToken })
export const queryCurrentUser = () => client.get('/iam/v1/me')
export const queryMenus = () => client.get('/iam/v1/menus', { params: { appCode: 'SCM_WEB' } })
export const queryPermissionSnapshot = () => openApiClient.get('/openapi/iam/v1/users/me/permissions', {
  headers: { 'X-App-Code': 'SCM_WEB' },
})
