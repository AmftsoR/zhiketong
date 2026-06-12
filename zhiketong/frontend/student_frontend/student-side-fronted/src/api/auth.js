import { request } from '../utils/request'

const USE_MOCK = true

const mockLogin = () =>
  Promise.resolve({
    code: 200,
    message: 'success',
    data: { token: 'mock-token', user: { id: 1, name: '张同学', className: '高一(1)班' } },
  })

const mockProfile = () =>
  Promise.resolve({
    code: 200,
    message: 'success',
    data: { id: 1, name: '张同学', className: '高一(1)班', studyDays: 128, points: 3450 },
  })

export function login(data) {
  if (USE_MOCK) return mockLogin()
  return request('/login', { method: 'POST', body: data })
}

export function fetchProfile() {
  if (USE_MOCK) return mockProfile()
  return request('/profile')
}
