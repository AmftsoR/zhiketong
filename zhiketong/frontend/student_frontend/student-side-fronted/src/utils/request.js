const BASE_URL = '/api'

export async function request(url, options = {}) {
  const fullUrl = url.startsWith('http') ? url : `${BASE_URL}${url}`

  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  }

  // 避免覆盖已有的 body
  if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
    config.body = JSON.stringify(options.body)
  }

  try {
    const response = await fetch(fullUrl, config)

    // DELETE 可能返回纯文本
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
