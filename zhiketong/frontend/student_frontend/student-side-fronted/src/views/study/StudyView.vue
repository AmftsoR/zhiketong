<template>
  <AppMobileFrame :time="currentTime" frame-class="study-frame" scroll-class="study-scroll">
    <template #topbar>
      <div class="study-topbar">
        <h1 class="study-topbar__title">学习</h1>
        <button type="button" class="study-topbar__ai" @click="openAiAssistant" aria-label="AI答疑助手">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <circle cx="10" cy="10" r="9" stroke="#6C5CE7" stroke-width="2"/>
            <path d="M6 8.5C6 7.5 7 6 10 6C13 6 14 7.5 14 8.5C14 10.5 11 10 10 12V13" stroke="#6C5CE7" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="10" cy="16" r="1" fill="#6C5CE7"/>
          </svg>
        </button>
      </div>
    </template>

    <section class="subject-strip">
      <button
        v-for="subject in subjects"
        :key="subject.key"
        type="button"
        class="subject-pill"
        :class="{ 'subject-pill--active': subject.key === selectedSubjectKey }"
        @click="selectSubject(subject.key)"
      >
        {{ subject.label }}
      </button>
    </section>

    <section class="quick-grid">
      <button
        v-for="item in studyShortcuts"
        :key="item.label"
        type="button"
        class="quick-item"
        @click="navigateTo(item.path)"
      >
        <span class="quick-item__icon" :style="{ background: item.color }">
          <svg v-if="item.key === 'weakness'" width="23" height="18" viewBox="0 0 23 18" fill="none">
            <path d="M0 1.88C0.0266667 1.35333 0.21 0.91 0.55 0.55C0.91 0.21 1.35333 0.0266667 1.88 0H5.63C6.15 0.0266667 6.59 0.21 6.95 0.55C7.29 0.91 7.47333 1.35333 7.5 1.88V2.5H15V1.88C15.0267 1.35333 15.21 0.91 15.55 0.55C15.91 0.21 16.3533 0.0266667 16.88 0H20.63C21.15 0.0266667 21.59 0.21 21.95 0.55C22.29 0.91 22.4733 1.35333 22.5 1.88V5.63C22.4733 6.15 22.29 6.59 21.95 6.95C21.59 7.29 21.15 7.47333 20.63 7.5H16.88C16.3533 7.47333 15.91 7.29 15.55 6.95C15.21 6.59 15.0267 6.15 15 5.63V5H7.5V5.63C7.5 5.70333 7.5 5.76667 7.5 5.82L10.63 10H14.38C14.9 10.0267 15.34 10.21 15.7 10.55C16.04 10.91 16.2233 11.3533 16.25 11.88V15.63C16.2233 16.15 16.04 16.59 15.7 16.95C15.34 17.29 14.9 17.4733 14.38 17.5H10.63C10.1033 17.4733 9.66 17.29 9.3 16.95C8.96 16.59 8.77667 16.15 8.75 15.63V11.88C8.75 11.8 8.75 11.7333 8.75 11.68L5.63 7.5H1.88C1.35333 7.47333 0.91 7.29 0.55 6.95C0.21 6.59 0.0266667 6.15 0 5.63V1.88Z" fill="#6C5CE7"/>
          </svg>
          <svg v-else-if="item.key === 'practice'" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M19.45 0.240011C19.87 0.526678 20.0533 0.930012 20 1.45001L17.5 17.7C17.42 18.0867 17.2133 18.3867 16.88 18.6C16.5133 18.78 16.1467 18.79 15.78 18.63L11.09 16.72L8.44 19.61C8.02 20.0033 7.55 20.1067 7.03 19.92C6.53667 19.6867 6.27667 19.2967 6.25 18.75V15.47C6.25 15.3167 6.30333 15.1867 6.41 15.08L12.97 7.93001C13.1767 7.64334 13.1633 7.35668 12.93 7.07001C12.67 6.83668 12.3833 6.82335 12.07 7.03001L4.14 14.1L0.7 12.38C0.26 12.1467 0.0266667 11.7833 0 11.29C0 10.7967 0.21 10.42 0.63 10.16L18.13 0.160011C18.5967 -0.0733218 19.0367 -0.0466552 19.45 0.240011Z" fill="#6C5CE7"/>
          </svg>
          <svg v-else-if="item.key === 'ai'" width="22" height="22" viewBox="0 0 22 22" fill="none">
            <circle cx="11" cy="11" r="10" stroke="#6C5CE7" stroke-width="1.5"/>
            <path d="M7 9.5C7 8 8.2 6.5 11 6.5C13.8 6.5 15 8 15 9.5C15 12 11.5 11 11 13.5V14.5" stroke="#6C5CE7" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="11" cy="17.5" r="1.2" fill="#6C5CE7"/>
          </svg>
          <svg v-else-if="item.key === 'report'" width="22" height="20" viewBox="0 0 22 20" fill="none">
            <path d="M10.63 9.38V0.66C10.65 0.273333 10.8567 0.0533333 11.25 0C12.89 0.0266667 14.36 0.43 15.66 1.21C16.9933 1.96333 18.0367 3.00667 18.79 4.34C19.57 5.64 19.9733 7.11 20 8.75C19.9467 9.14333 19.7267 9.35333 19.34 9.38H10.63ZM0 10.63C0.0533333 8.20333 0.833333 6.14333 2.34 4.45C3.82667 2.73667 5.74333 1.69667 8.09 1.33C8.47667 1.33 8.69667 1.53667 8.75 1.95V11.25L14.88 17.38C15.14 17.6933 15.1133 17.9933 14.8 18.28C13.24 19.4 11.4333 19.9733 9.38 20C7.63333 19.9733 6.05667 19.5433 4.65 18.71C3.24333 17.8767 2.12333 16.7567 1.29 15.35C0.456667 13.9433 0.0266667 12.37 0 10.63ZM20.55 11.25C20.9633 11.3033 21.17 11.5233 21.17 11.91C20.83 14.15 19.8667 16 18.28 17.46C17.9933 17.6933 17.72 17.6933 17.46 17.46L11.25 11.25H20.55Z" fill="#6C5CE7"/>
          </svg>
        </span>
        <span class="quick-item__label">{{ item.label }}</span>
      </button>
    </section>

    <section class="task-card page-card">
      <div class="section-head">
        <h2>今日练习任务</h2>
        <span class="section-head__badge">AI靶向生成</span>
      </div>

      <p class="task-card__subject">{{ practiceTask.subject }}</p>

      <div class="task-card__progress">
        <div class="task-card__progress-info">
          <span>{{ practiceTask.completed }}/{{ practiceTask.total }}</span>
        </div>

        <div class="progress-bar" role="progressbar" :aria-valuenow="taskProgress" aria-valuemin="0" aria-valuemax="100">
          <div class="progress-bar__fill" :style="{ width: `${taskProgress}%` }"></div>
        </div>
      </div>

      <button type="button" class="primary-action" @click="continuePractice">继续练习</button>
    </section>

    <section class="study-activity page-card">
      <div class="section-head section-head--space">
        <h2>最近学习动态</h2>
      </div>

      <ul class="activity-list">
        <li v-for="activity in recentActivities" :key="activity.title" class="activity-item">
          <span class="activity-item__dot" :class="`activity-item__dot--${activity.type}`"></span>
          <div class="activity-item__content">
            <strong>{{ activity.title }}</strong>
            <p>{{ activity.meta }}</p>
          </div>
          <span class="activity-item__time">{{ activity.time }}</span>
        </li>
      </ul>
    </section>

    <template #footer>
      <BottomNav :activeTab="'study'" />
    </template>
  </AppMobileFrame>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import BottomNav from '../../components/layout/BottomNav.vue'
import { useStudentStore } from '../../stores/studentStore'

const router = useRouter()
const route = useRoute()
const studentStore = useStudentStore()

const currentTime = ref('9:41')
const practiceTask = computed(() => studentStore.dashboard.practiceTask)
const taskProgress = computed(() => studentStore.taskProgress)
const subjects = computed(() => studentStore.weaknessMap.subjects)
const selectedSubjectKey = computed(() => studentStore.weaknessMap.selectedSubjectKey)

const studyShortcuts = [
  { key: 'weakness', label: '薄弱点图谱', path: '/weakness-map', color: 'linear-gradient(135deg, #dbeafe, #c4b5fd)' },
  { key: 'practice', label: '靶向练习', path: '/target-practice', color: 'linear-gradient(135deg, #d1fae5, #bfdbfe)' },
  { key: 'ai', label: 'AI答疑助手', path: '/ai-assistant', color: 'linear-gradient(135deg, #fde68a, #fca5a5)' },
  { key: 'report', label: '学情报告', path: '/analysis-report', color: 'linear-gradient(135deg, #c7d2fe, #ddd6fe)' },
]

const recentActivities = [
  { title: '完成靶向练习', meta: '数学-函数 | 基础巩固 3题', time: '10分钟前', type: 'practice' },
  { title: '新增错题收录', meta: '物理-动力学 | 牛顿第二定律误用', time: '30分钟前', type: 'wrong' },
  { title: '薄弱点图谱更新', meta: '英语-阅读 | 长难句结构识别', time: '1小时前', type: 'weakness' },
  { title: 'AI答疑对话', meta: '数学-函数 | 定义域求解讨论', time: '2小时前', type: 'ai' },
]

let clockTimer = null

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function navigateTo(path) {
  if (route.path !== path) {
    router.push(path)
  }
}

function selectSubject(key) {
  studentStore.selectStudySubject(key)
}

function openAiAssistant() {
  router.push('/ai-assistant')
}

function continuePractice() {
  // 每次点击递增进度并直接跳转到靶向练习界面
  if (practiceTask.value.completed < practiceTask.value.total) {
    studentStore.continuePractice()
  }
  router.push('/target-practice')
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 30000)
})

onBeforeUnmount(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
})
</script>

<style scoped>
.study-topbar {
  height: 44.67px;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
}

.study-topbar__title {
  margin: 0;
  color: #333;
  font-size: 1.0625rem;
  font-weight: 600;
}

.study-topbar__ai {
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.study-scroll {
  overflow: auto;
  padding: 0 0 16px;
}

.study-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.subject-strip {
  display: flex;
  gap: 10px;
  overflow: auto;
  padding: 4px 20px 2px;
}

.subject-strip::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.subject-pill {
  border: 0;
  border-radius: 999px;
  background: #fff;
  color: #6a6b88;
  padding: 10px 16px;
  font-size: 0.82rem;
  font-weight: 700;
  white-space: nowrap;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}

.subject-pill--active {
  color: #6c5ce7;
  background: rgba(108, 92, 231, 0.12);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 14px 20px 0;
}

.quick-item {
  padding: 0;
  border: 0;
  background: transparent;
  display: grid;
  justify-items: center;
  gap: 8px;
  cursor: pointer;
}

.quick-item__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
}

.quick-item__label {
  color: #333;
  font-size: 0.75rem;
}

.task-card,
.study-activity {
  width: calc(100% - 40px);
  margin: 16px 20px 0;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: space-between;
}

.section-head--space {
  margin-bottom: 6px;
}

.section-head h2 {
  margin: 0;
  color: #000;
  font-size: 1rem;
}

.section-head__badge {
  padding: 4px 8px;
  border-radius: 6px;
  color: #6c5ce7;
  background: #f0efff;
  font-size: 0.625rem;
  white-space: nowrap;
}

.task-card__subject {
  margin: 12px 0 16px;
  color: #999;
  font-size: 0.875rem;
}

.task-card__progress {
  display: grid;
  gap: 12px;
}

.task-card__progress-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #999;
  font-size: 0.75rem;
}

.progress-bar {
  height: 6px;
  border-radius: 999px;
  background: #eee;
  overflow: hidden;
}

.progress-bar__fill {
  height: 100%;
  border-radius: inherit;
  background: #6c5ce7;
  transition: width 0.3s ease;
}

.primary-action {
  margin-top: 14px;
  width: 96px;
  height: 36px;
  border: 0;
  border-radius: 20px;
  color: #fff;
  font-size: 0.875rem;
  font-weight: 600;
  background: #6c5ce7;
  box-shadow: 0 4px 10px rgba(108, 92, 231, 0.3);
  cursor: pointer;
}

.activity-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.activity-item:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.activity-item__dot {
  width: 10px;
  height: 10px;
  margin-top: 5px;
  border-radius: 50%;
  flex: 0 0 auto;
}

.activity-item__dot--practice {
  background: #6c5ce7;
}

.activity-item__dot--wrong {
  background: #ff7675;
}

.activity-item__dot--weakness {
  background: #fdcb6e;
}

.activity-item__dot--ai {
  background: #00b894;
}

.activity-item__content {
  flex: 1 1 auto;
  min-width: 0;
}

.activity-item__content strong {
  display: block;
  color: #333;
  font-size: 0.875rem;
  font-weight: 700;
}

.activity-item__content p {
  margin: 4px 0 0;
  color: #999;
  font-size: 0.75rem;
}

.activity-item__time {
  color: #ccc;
  font-size: 0.6875rem;
  white-space: nowrap;
  flex: 0 0 auto;
}

@media (max-width: 420px) {
  .task-card,
  .study-activity {
    width: calc(100% - 24px);
    margin-left: 12px;
    margin-right: 12px;
  }

  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
