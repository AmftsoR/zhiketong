import { request } from '../utils/request'

/**
 * 教师登录
 * POST /api/user/login
 */
export function login(username, password) {
  return request('/user/login', { method: 'POST', body: { username, password } })
}

/**
 * 获取用户信息
 * GET /api/user/{id}
 */
export function fetchUser(userId) {
  return request(`/user/${userId}`)
}
