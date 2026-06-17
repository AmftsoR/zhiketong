import { request } from '../utils/request'

/**
 * 获取教师所带班级的学情统计
 * GET /api/teacher/class-statistics?teacherId=
 */
export function fetchClassStatistics(teacherId) {
  return request(`/teacher/class-statistics?teacherId=${teacherId}`)
}

/**
 * 获取学生学情详情
 * GET /api/teacher/student-detail?studentId=
 */
export function fetchStudentDetail(studentId) {
  return request(`/teacher/student-detail?studentId=${studentId}`)
}

/**
 * 获取班级排行榜
 * GET /api/leaderboard?classId=&limit=
 */
export function fetchLeaderboard(classId, limit = 20) {
  return request(`/leaderboard?classId=${classId}&limit=${limit}`)
}
