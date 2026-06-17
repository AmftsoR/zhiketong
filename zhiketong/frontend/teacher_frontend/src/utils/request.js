const BASE_URL = '/api'

/**
 * 统一 HTTP 请求封装（基于 fetch）
 */
export async function request(url, options = {}) {
  const fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`

  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  }

  // 自动序列化 JSON body
  if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
    config.body = JSON.stringify(options.body)
  }

  // 从 localStorage 读取 token 并附加
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
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
