import axios from 'axios'

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
    (error) => Promise.reject(error.response?.data || { message: '网络请求失败' }),
  )
  return instance
}

const client = configureClient('/api')

export const openApiClient = configureClient('')

export default client
