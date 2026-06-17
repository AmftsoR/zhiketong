import { createRouter, createWebHistory } from 'vue-router'
import TeacherLayout from '../layouts/TeacherLayout.vue'
import LoginView from '../views/login/LoginView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { title: '教师登录', noAuth: true },
  },
  {
    path: '/',
    redirect: '/overview',
  },
  {
    path: '/',
    component: TeacherLayout,
    children: [
      {
        path: 'overview',
        name: 'overview',
        component: () => import('../views/teacher/OverviewView.vue'),
        meta: { title: '数据总览' },
      },
      {
        path: 'class',
        name: 'class-manage',
        component: () => import('../views/teacher/ClassManageView.vue'),
        meta: { title: '班级管理', showHeaderSearch: true },
      },
      {
        path: 'analysis',
        name: 'analysis',
        component: () => import('../views/teacher/AnalysisView.vue'),
        meta: { title: '学情分析' },
      },
      {
        path: 'bank',
        name: 'question-bank',
        component: () => import('../views/teacher/QuestionBankView.vue'),
        meta: { title: '题库管理' },
      },
      {
        path: 'settings',
        name: 'settings',
        component: () => import('../views/teacher/SettingsView.vue'),
        meta: { title: '设置中心' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/overview' },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 路由守卫：未登录跳转登录页
router.beforeEach(async (to) => {
  if (to.meta.noAuth) return true

  try {
    const { useUserStore } = await import('../stores/userStore')
    const userStore = useUserStore()

    if (!userStore.user) {
      await userStore.restoreSession()
    }

    if (!userStore.isLoggedIn) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    return true
  } catch (e) {
    console.error('路由守卫异常:', e)
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
