import { request } from '../utils/request'

export function login(data) {
  return request('/login', { method: 'POST', body: data })
}

export function fetchCurrentUser() {
  return request('/user/current')
}
