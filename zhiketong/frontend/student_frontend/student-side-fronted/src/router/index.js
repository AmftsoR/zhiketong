import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/home/HomeView.vue'
import StudyView from '../views/study/StudyView.vue'
import WeaknessMapView from '../views/weakness-map/WeaknessMapView.vue'
import TargetPracticeView from '../views/target-practice/TargetPracticeView.vue'
import WrongBookView from '../views/wrong-book/WrongBookView.vue'
import AnalysisReportView from '../views/analysis-report/AnalysisReportView.vue'
import AiAssistantView from '../views/ai-assistant/AiAssistantView.vue'
import ProfileView from '../views/profile/ProfileView.vue'
import FavoriteView from '../views/favorite/FavoriteView.vue'
import NotFoundView from '../views/not-found/NotFoundView.vue'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', name: 'home', component: HomeView, meta: { title: '首页界面' } },
  {
    path: '/study',
    name: 'study',
    component: StudyView,
    meta: { title: '学习界面' },
  },
  {
    path: '/weakness-map',
    name: 'weakness-map',
    component: WeaknessMapView,
    meta: { title: '薄弱点图谱' },
  },
  {
    path: '/target-practice',
    name: 'target-practice',
    component: TargetPracticeView,
    meta: { title: '靶向练习推送' },
  },
  {
    path: '/wrong-book',
    name: 'wrong-book',
    component: WrongBookView,
    meta: { title: '个人错题本' },
  },
  {
    path: '/analysis-report',
    name: 'analysis-report',
    component: AnalysisReportView,
    meta: { title: '学习分析报告' },
  },
  {
    path: '/ai-assistant',
    name: 'ai-assistant',
    component: AiAssistantView,
    meta: { title: 'AI启发式答题助手' },
  },
  { path: '/favorites', name: 'favorites', component: FavoriteView, meta: { title: '我的收藏' } },
  { path: '/profile', name: 'profile', component: ProfileView, meta: { title: '我的界面' } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView, meta: { title: '页面不存在' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} · 智课通学生端`
  }
})

export default router