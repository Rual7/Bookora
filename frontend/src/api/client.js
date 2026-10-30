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
  const headers = { Accept: 'application/json' }
  if (options.body !== undefined) {
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
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new ApiError(response.status, problem?.detail ?? response.statusText, problem?.errors)
  }
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

export const api = {
  get: (path, signal) => request(path, { signal }),
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
}
