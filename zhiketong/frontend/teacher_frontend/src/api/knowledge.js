import { request } from '../utils/request'

/**
 * 获取学生的知识点掌握度
 * GET /api/knowledge/mastery?studentId=
 */
export function fetchMastery(studentId) {
  return request(`/knowledge/mastery?studentId=${studentId}`)
}
