import { request } from '../utils/request'

/**
 * 获取错题列表
 * @returns {Promise<{code: number, message: string, data: Array}>}
 */
export function fetchMistakes() {
  return request('/mistakes')
}

/**
 * 手动添加错题
 * @param {{subject: string, type: string, difficulty: string, stem: string, options: string, correctAnswer: string, myAnswer: string, analysis: string}} data
 */
export function createMistake(data) {
  return request('/mistakes', {
    method: 'POST',
    body: data,
  })
}

/**
 * 删除错题
 * @param {number} questionId
 */
export function deleteMistake(questionId) {
  return request(`/mistakes/${questionId}`, { method: 'DELETE' })
}

/**
 * 提交答案
 * @param {{questionId: number, userAnswer: string}} data
 */
export function submitAnswer(data) {
  return request('/answer/submit', {
    method: 'POST',
    body: data,
  })
}
