import { request } from '../utils/request'

// ====== 对接后端 GET /api/questions/random ======

/**
 * 获取随机题目
 * @param {string} mode - basic | variant | challenge（暂未在后端实现，预留）
 * @returns {Promise<Object>} 一道随机题目
 */
export function fetchRandomQuestion(mode = 'basic') {
  return import('../utils/request').then(m => m.request(`/questions/random?mode=${mode}`))
}
