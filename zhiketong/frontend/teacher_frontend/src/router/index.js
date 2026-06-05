import { createRouter, createWebHistory } from 'vue-router'
import TeacherLayout from '../layouts/TeacherLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
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
  ],
})

export default router
