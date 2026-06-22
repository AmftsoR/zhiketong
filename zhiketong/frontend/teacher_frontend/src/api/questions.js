import { request } from '../utils/request'

/** 分页查询 GET /api/questions/page */
export function fetchQuestions(params = {}) {
  const q = new URLSearchParams()
  if (params.page) q.set('page', params.page)
  if (params.size) q.set('size', params.size)
  if (params.knowledgePointId) q.set('knowledgePointId', params.knowledgePointId)
  if (params.type) q.set('type', params.type)
  if (params.difficulty) q.set('difficulty', params.difficulty)
  if (params.keyword) q.set('keyword', params.keyword)
  return request(`/questions/page?${q}`)
}

/** 详情 GET /api/questions/{id} */
export function getQuestion(id) { return request(`/questions/${id}`) }

/** 新增 POST /api/questions */
export function createQuestion(data) { return request('/questions', { method: 'POST', body: data }) }

/** 修改 PUT /api/questions/{id} */
export function updateQuestion(id, data) { return request(`/questions/${id}`, { method: 'PUT', body: data }) }

/** 删除 DELETE /api/questions/{id} */
export function deleteQuestion(id) { return request(`/questions/${id}`, { method: 'DELETE' }) }

/** Excel 导入 POST /api/questions/import */
export function importQuestions(file) {
  const fd = new FormData(); fd.append('file', file)
  return request('/questions/import', { method: 'POST', headers: {}, body: fd })
}
