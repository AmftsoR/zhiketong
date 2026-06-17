import { request } from '../utils/request'

/**
 * 教师登录
 * POST /api/user/login
 */
export function login(data) {
  return request('/user/login', { method: 'POST', body: data })
}

/**
 * 获取当前登录用户信息
 * GET /api/user/current
 */
export function fetchCurrentUser() {
  return request('/user/current')
}
