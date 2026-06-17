import { request } from '../utils/request'

/**
 * 登录
 * POST /api/user/login
 */
export function login(data) {
  return request('/user/login', { method: 'POST', body: data })
}

/**
 * 获取当前用户信息
 * GET /api/user/{id}
 */
export function fetchProfile(userId) {
  return request(`/user/${userId}`)
}
