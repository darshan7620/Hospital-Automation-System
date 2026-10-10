const RAW_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export const API_BASE_URL = RAW_BASE_URL.replace(/\/+$/, '')

export class ApiError extends Error {
  constructor(message, { status = 0, path = '', fieldErrors = null } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.path = path
    this.fieldErrors = fieldErrors
  }
}

export function toQueryString(params = {}) {
  const query = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === '') continue
    query.set(key, String(value))
  }
  const qs = query.toString()
  return qs ? `?${qs}` : ''
}

function buildFieldErrorMessage(fieldErrors) {
  if (!Array.isArray(fieldErrors) || fieldErrors.length === 0) return null
  return fieldErrors.map((e) => `${e.field}: ${e.message}`).join('; ')
}

async function toApiError(response) {
  const path = response.url || ''
  let payload = null
  try {
    payload = await response.json()
  } catch {
    payload = null
  }

  const message =
    (payload && (payload.message || payload.error)) ||
    `Request failed with status ${response.status}.`
  const detail = buildFieldErrorMessage(payload?.fieldErrors)

  return new ApiError(detail ? `${message} ${detail}` : message, {
    status: response.status,
    path,
    fieldErrors: payload?.fieldErrors ?? null,
  })
}

export async function request(path, { method = 'GET', body, headers = {}, signal } = {}) {
  const url = `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
  const requestHeaders = { Accept: 'application/json', ...headers }

  let payload
  if (body !== undefined) {
    requestHeaders['Content-Type'] = 'application/json'
    payload = JSON.stringify(body)
  }

  let response
  try {
    response = await fetch(url, {
      method,
      headers: requestHeaders,
      body: payload,
      signal,
    })
  } catch (cause) {
    if (cause?.name === 'AbortError') throw cause
    throw new ApiError(
      'Cannot reach the API server. Check that the backend is running and VITE_API_BASE_URL is correct.',
      { status: 0, path: url },
    )
  }

  if (!response.ok) throw await toApiError(response)
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return null
  }
  return response.json()
}

export const api = {
  get: (path, options) => request(path, { ...options, method: 'GET' }),
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  put: (path, body, options) => request(path, { ...options, method: 'PUT', body }),
  delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
}
