const CSRF_COOKIE = 'XSRF-TOKEN'

export class ApiError extends Error {
  constructor(status, message, fieldErrors = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

function readCookie(name) {
  const match = document.cookie.split('; ').find((row) => row.startsWith(`${name}=`))
  return match ? decodeURIComponent(match.split('=')[1]) : null
}

/**
 * Changing requests must carry the CSRF token from the cookie. The server deletes that cookie on sign-out,
 * so if it is missing we ask for a fresh one first instead of sending a request that would be refused (403).
 */
async function csrfToken() {
  if (!readCookie(CSRF_COOKIE)) {
    await fetch('/api/auth/csrf', { credentials: 'same-origin' })
  }
  return readCookie(CSRF_COOKIE)
}

async function request(path, options = {}) {
  const method = options.method ?? 'GET'
  const headers = { Accept: 'application/json', ...options.headers }
  // A FormData body (a file upload) sets its own Content-Type with the multipart boundary.
  const isUpload = options.body instanceof FormData
  if (options.body !== undefined && !isUpload) {
    headers['Content-Type'] = 'application/json'
  }
  if (method !== 'GET') {
    const token = await csrfToken()
    if (token) headers['X-XSRF-TOKEN'] = token
  }

  const response = await fetch(path, {
    method,
    headers,
    credentials: 'same-origin',
    signal: options.signal,
    body: options.body === undefined || isUpload ? options.body : JSON.stringify(options.body),
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new ApiError(response.status, problem?.detail ?? response.statusText, problem?.errors)
  }
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

/** Fetches a file the signed-in customer may download and hands it to the browser as a save. */
export async function downloadFile(path) {
  const response = await fetch(path, { credentials: 'same-origin' })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new ApiError(response.status, problem?.detail ?? response.statusText)
  }
  const disposition = response.headers.get('Content-Disposition') ?? ''
  const link = document.createElement('a')
  link.href = URL.createObjectURL(await response.blob())
  link.download = /filename="?([^";]+)"?/.exec(disposition)?.[1] ?? 'ebook'
  link.click()
  URL.revokeObjectURL(link.href)
}

export const api = {
  get: (path, signal) => request(path, { signal }),
  post: (path, body, headers) => request(path, { method: 'POST', body, headers }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
}
