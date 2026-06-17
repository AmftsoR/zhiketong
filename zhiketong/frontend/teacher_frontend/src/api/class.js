import { request } from '../utils/request'

/**
 * 创建班级
 * POST /api/class/create
 */
export function createClass(className, teacherId) {
  return request('/class/create', { method: 'POST', body: { className, teacherId } })
}

/**
 * 学生加入班级
 * POST /api/class/join
 */
export function joinClass(studentId, classId) {
  return request('/class/join', { method: 'POST', body: { studentId, classId } })
}

/**
 * 获取教师的所有班级
 * GET /api/class/teacher/{teacherId}
 */
export function fetchTeacherClasses(teacherId) {
  return request(`/class/teacher/${teacherId}`)
}
