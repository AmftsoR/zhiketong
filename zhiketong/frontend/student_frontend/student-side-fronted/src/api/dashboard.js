// ====== 学情看板、班级管理等接口 — 后端未就绪，全部 mock ======

const USE_MOCK = true

const mockDashboard = () =>
  Promise.resolve({
    code: 200,
    data: {
      studyMinutes: 35,
      practiceTask: { subject: '高一数学 - 集合与函数', completed: 3, total: 10 },
      metrics: [
        { label: '错题数', value: 32, unit: '道' },
        { label: '未完成练习', value: 8, unit: '道' },
        { label: '掌握知识点', value: 68, unit: '个' },
      ],
    },
  })

const mockAnalysis = () =>
  Promise.resolve({
    code: 200,
    data: {
      overallScore: 85,
      summary: '击败了班级 76% 的同学',
      radar: { mine: [82, 68, 74, 79], class: [65, 58, 60, 62] },
      chapters: [
        { name: '集合与逻辑用语', value: 92 },
        { name: '一元二次函数与方程', value: 75 },
        { name: '基本初等函数', value: 45 },
      ],
    },
  })

const mockClassStudents = () =>
  Promise.resolve({
    code: 200,
    data: [
      { id: 1, name: '张同学', status: '在线', completion: 85, accuracy: 72 },
      { id: 2, name: '李同学', status: '离线', completion: 60, accuracy: 55 },
      { id: 3, name: '王同学', status: '在线', completion: 92, accuracy: 88 },
    ],
  })

export function fetchDashboard() {
  if (USE_MOCK) return mockDashboard()
  return import('../utils/request').then(m => m.request('/dashboard'))
}

export function fetchAnalysis(period = 'current') {
  if (USE_MOCK) return mockAnalysis()
  return import('../utils/request').then(m => m.request(`/analysis?period=${period}`))
}

export function fetchClassStudents() {
  if (USE_MOCK) return mockClassStudents()
  return import('../utils/request').then(m => m.request('/class/students'))
}
