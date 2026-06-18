import { request } from '../utils/request'

const BASE_URL = '/api'
const TOKEN_KEY = 'zhiketong_token'

function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/**
 * 流式聊天 — 使用 fetch + ReadableStream 读取 SSE
 * @param {string} message - 用户消息
 * @param {Array} history - 对话历史 [{role, content}, ...]
 * @param {object} callbacks - { onToken, onDone, onError }
 * @returns {AbortController} 用于取消请求
 */
export function streamChat(message, history = [], callbacks = {}) {
  const controller = new AbortController()
  const token = getToken()

  fetch(`${BASE_URL}/ai/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ message, history }),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        const text = await response.text()
        callbacks.onError && callbacks.onError(new Error(`请求失败 (${response.status}): ${text}`))
        return
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      let fullContent = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        let currentEvent = 'token'
        for (const line of lines) {
          if (line === '') {
            // SSE 事件边界，重置事件类型
            currentEvent = 'token'
            continue
          }
          if (line.startsWith('event:')) {
            currentEvent = line.substring(6).trim()
          } else if (line.startsWith('data:')) {
            const data = line.substring(5).trim()
            if (data === '[DONE]') continue

            if (currentEvent === 'error') {
              callbacks.onError && callbacks.onError(new Error(data))
              return
            }
            if (currentEvent === 'done') {
              callbacks.onDone && callbacks.onDone(data)
              return
            }
            // token 事件
            fullContent += data
            callbacks.onToken && callbacks.onToken(data, fullContent)
          }
        }
      }

      // 流结束但没有 done 事件时，用累积的内容作为完成
      if (fullContent) {
        callbacks.onDone && callbacks.onDone(fullContent)
      }
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        callbacks.onError && callbacks.onError(err)
      }
    })

  return controller
}

/**
 * AI 生成靶向练习题
 * POST /api/ai/generate-questions
 */
export function generateQuestions(subject, mode, count = 3) {
  return request('/ai/generate-questions', {
    method: 'POST',
    body: { subject, mode, count },
  })
}

/**
 * 获取学生AI上下文
 * GET /api/ai/context
 */
export function fetchAiContext() {
  return request('/ai/context')
}
