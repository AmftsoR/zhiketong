const BASE_URL = '/api'
const TOKEN_KEY = 'zhiketong_teacher_token'

function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function saveToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export async function request(url, options = {}) {
  const fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`

  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  }

  const token = getToken()
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }

  if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
    config.body = JSON.stringify(options.body)
  }

  try {
    const response = await fetch(fullUrl, config)

    const contentType = response.headers.get('content-type') || ''
    let data
    if (contentType.includes('application/json')) {
      data = await response.json()
    } else {
      data = await response.text()
    }

    if (!response.ok) {
      if (response.status === 401 || (data && data.code === 401)) {
        clearToken()
      }
      const message = (data && data.message) || `请求失败 (${response.status})`
      throw new Error(message)
    }

    return data
  } catch (err) {
    console.error('[request error]', fullUrl, err)
    throw err
  }
}

export default request
