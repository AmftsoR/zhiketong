import { request } from '../utils/request'

/** 获取学生的知识点掌握度 GET /api/knowledge/mastery */
export function fetchMastery(studentId) {
  return request(`/knowledge/mastery?studentId=${studentId}`)
}

/** 获取知识点树 GET /api/knowledge-points/tree */
export function fetchKnowledgeTree() {
  return request('/knowledge-points/tree')
}
