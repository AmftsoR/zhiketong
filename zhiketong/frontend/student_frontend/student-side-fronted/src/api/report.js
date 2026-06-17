import { request } from '../utils/request'

/**
 * 获取学生自己的学情报告
 * GET /api/report/my-analysis?period=current|prev|term
 */
export function fetchMyReport(period = 'current') {
  return request(`/report/my-analysis?period=${period}`)
}

/**
 * 分享报告给老师
 * POST /api/report/share
 * @param {string} reportData - JSON string of report data
 * @returns {{ shareToken, shareUrl }}
 */
export function shareReport(reportData) {
  return request('/report/share', {
    method: 'POST',
    body: { reportData },
  })
}

/**
 * 查看分享的报告（无需登录）
 * GET /api/report/shared?token=xxx
 */
export function getSharedReport(token) {
  return request(`/report/shared?token=${token}`)
}
