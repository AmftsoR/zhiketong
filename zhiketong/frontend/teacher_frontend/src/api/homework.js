import { request } from '../utils/request'

/** 分页 GET /api/homeworks/page */
export function fetchHomeworkList(p = {}) {
  const q = new URLSearchParams()
  if (p.teacherId) q.set('teacherId', p.teacherId); if (p.className) q.set('className', p.className)
  if (p.difficulty) q.set('difficulty', p.difficulty); if (p.page) q.set('page', p.page); if (p.size) q.set('size', p.size)
  return request(`/homeworks/page?${q}`)
}

/** 详情 GET /api/homeworks/{id} */
export function getHomework(id) { return request(`/homeworks/${id}`) }

/** 创建 POST /api/homeworks */
export function createHomework(data) { return request('/homeworks', { method: 'POST', body: data }) }

/** 发布 PUT /api/homeworks/{id}/publish */
export function publishHomework(id) { return request(`/homeworks/${id}/publish`, { method: 'PUT' }) }

/** 删除 DELETE /api/homeworks/{id} */
export function deleteHomework(id) { return request(`/homeworks/${id}`, { method: 'DELETE' }) }
