import { request } from '../utils/request'

/** 获取学习历史记录 */
export function fetchHistory(params = {}) {
  const query = new URLSearchParams()
  if (params.subject) query.set('subject', params.subject)
  if (params.startDate) query.set('startDate', params.startDate)
  if (params.endDate) query.set('endDate', params.endDate)
  const qs = query.toString()
  return request(`/history/list${qs ? '?' + qs : ''}`)
}
