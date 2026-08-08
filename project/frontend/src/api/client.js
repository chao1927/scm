import axios from 'axios'

export function normalizeApiError(error) {
  const status = error?.response?.status
  const responseData = error?.response?.data
  if (responseData?.message) return responseData
  if (status === 401) return { message: '登录已失效，请重新登录', status }
  if (status === 403) return { message: '无权访问当前功能', status }
  if (status) return { message: `请求失败（HTTP ${status}）`, status }
  return { message: '网络请求失败，请检查服务是否已启动', status: null }
}

function configureClient(baseURL) {
  const instance = axios.create({ baseURL, timeout: 10000 })
  instance.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('access_token')
  config.headers.Authorization = token ? `Bearer ${token}` : undefined
  config.headers['X-Request-Id'] = crypto.randomUUID()
  config.headers['X-Trace-Id'] = crypto.randomUUID()
  config.headers['X-Org-Id'] = sessionStorage.getItem('org_id') || '1'
  if (config.method !== 'get') {
    config.headers['X-Idempotency-Key'] = crypto.randomUUID()
  }
  return config
  })

  instance.interceptors.response.use(
    (response) => response.data,
    (error) => Promise.reject(normalizeApiError(error)),
  )
  return instance
}

const client = configureClient('/api')

export const openApiClient = configureClient('')

export default client
