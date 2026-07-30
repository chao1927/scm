import client from '../../api/client'

export const column = (key, title, options = {}) => ({ key, title, ...options })

export const get = (path) => (params) => client.get(path, { params })

export const getDetail = (path, key) => (id) => (
  client.get(
    `${path}/${encodeURIComponent(id)}`,
    key ? { params: { [key]: id } } : undefined,
  )
)

export const reason = (text) => text || '前端业务页面操作'

export function simpleResource(query, rowKey, definitions) {
  return {
    query,
    rowKey,
    columns: definitions.map(([key, title, format]) => column(key, title, {
      status: format === 'status',
      date: format === 'date',
      number: format === 'number',
    })),
  }
}

export function extractRecords(result) {
  const data = result?.data ?? result
  return data?.records || data?.items || data?.content || (Array.isArray(data) ? data : [])
}

export function normalizePage(result, fallback = {}) {
  const data = result?.data ?? result ?? {}
  const records = data.records || data.items || data.content || data.list || (Array.isArray(data) ? data : [])
  const zeroBasedPage = Number.isFinite(Number(data.number)) ? Number(data.number) + 1 : undefined
  return {
    records,
    total: Number(data.total ?? data.totalElements ?? records.length),
    pageNo: Number(data.pageNo ?? zeroBasedPage ?? fallback.pageNo ?? 1),
    pageSize: Number(data.pageSize ?? data.size ?? fallback.pageSize ?? 20),
  }
}

export function recordIdentity(definition, record) {
  for (const key of definition?.rowKey || []) {
    if (record?.[key] !== undefined && record?.[key] !== null) return String(record[key])
  }
  return String(record?.id || record?.key || '')
}
