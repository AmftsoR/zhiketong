import { request } from '../utils/request'

/**
 * 登录
 * @param {{username: string, password: string}} data
 * @returns {Promise<{code: number, message: string, data: {token: string, user: object}}>}
 */
export function login(data) {
  return request('/login', { method: 'POST', body: data })
}

/**
 * 获取当前登录用户信息
 */
export function fetchCurrentUser() {
  return request('/user/current')
}
