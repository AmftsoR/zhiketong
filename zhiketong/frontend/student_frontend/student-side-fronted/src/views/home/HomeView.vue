<template>
  <AppMobileFrame :time="currentTime" frame-class="home-frame" scroll-class="home-scroll">
    <section class="home-hero page-card">
      <div class="home-hero__text">
        <p class="home-hero__title">Hi, 同学</p>
        <p class="home-hero__subtitle">今日已学 {{ studyMinutes }} 分钟，继续加油！</p>
      </div>

      <button class="home-avatar" type="button" @click="jumpToProfile" aria-label="进入我的界面">
        <span>{{ userInitial }}</span>
      </button>
    </section>

    <section class="home-summary page-card">
      <div v-for="metric in metrics" :key="metric.label" class="metric">
        <div class="metric__value">
          <span>{{ metric.value }}</span>
          <small>{{ metric.unit }}</small>
        </div>
        <p class="metric__label">{{ metric.label }}</p>
      </div>
    </section>

    <section class="home-shortcuts page-card">
      <button
        v-for="item in shortcutItems"
        :key="item.label"
        type="button"
        class="shortcut"
        @click="navigateTo(item.path)"
      >
        <span class="shortcut__icon" :style="{ background: item.color }">{{ item.shortLabel }}</span>
        <span class="shortcut__label">{{ item.label }}</span>
      </button>
    </section>

    <section class="home-task page-card">
      <div class="section-head">
        <h2>今日练习任务</h2>
        <span class="section-head__badge">AI靶向生成</span>
      </div>

      <p class="home-task__subject">{{ practiceTask.subject }}</p>

      <div class="home-task__progress">
        <div class="home-task__progress-info">
          <span>{{ practiceTask.completed }}/{{ practiceTask.total }}</span>
          <button type="button" class="link-button" @click="resetTaskProgress">重置</button>
        </div>

        <div class="progress-bar" role="progressbar" :aria-valuenow="taskProgress" aria-valuemin="0" aria-valuemax="100">
          <div class="progress-bar__fill" :style="{ width: `${taskProgress}%` }"></div>
        </div>
      </div>

      <button type="button" class="primary-action" @click="continuePractice">继续练习</button>
    </section>

    <section class="home-wrong-book page-card">
      <div class="section-head section-head--space">
        <h2>最近错题自动收录</h2>
        <button type="button" class="link-button" @click="navigateTo('/wrong-book')">查看更多</button>
      </div>

      <ul class="mistake-list">
        <li v-for="item in recentMistakes" :key="item.title" class="mistake-item">
          <span class="mistake-item__dot"></span>

          <div class="mistake-item__content">
            <strong>{{ item.title }}</strong>
            <p>{{ item.meta }}</p>
          </div>
        </li>
      </ul>
    </section>

    <template #footer>
      <BottomNav :activeTab="'home'" />
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
const studyMinutes = computed(() => studentStore.dashboard.studyMinutes)
const practiceTask = computed(() => studentStore.dashboard.practiceTask)
const metrics = computed(() => studentStore.dashboard.metrics)
const shortcutItems = computed(() => studentStore.dashboard.shortcuts)
const recentMistakes = computed(() => studentStore.dashboard.recentMistakes)

const taskProgress = computed(() => studentStore.taskProgress)
const userInitial = computed(() => studentStore.profile.name.slice(0, 1) || '学')

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

function jumpToProfile() {
  navigateTo('/profile')
}

function continuePractice() {
  if (studentStore.dashboard.practiceTask.completed < studentStore.dashboard.practiceTask.total) {
    studentStore.continuePractice()
    return
  }

  router.push('/target-practice')
}

function resetTaskProgress() {
  studentStore.resetPractice()
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
.home-scroll {
  overflow: auto;
  padding: 0 0 16px;
}

.home-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.home-hero,
.home-summary,
.home-shortcuts,
.home-task,
.home-wrong-book {
  width: calc(100% - 40px);
  margin: 0 20px 12px;
}

.home-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 116px;
}

.home-hero__title {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
  font-weight: 700;
}

.home-hero__subtitle {
  margin: 8px 0 0;
  color: #999;
  font-size: 0.75rem;
}

.home-avatar {
  width: 52px;
  height: 52px;
  border: 0;
  border-radius: 50%;
  color: #fff;
  font-size: 1rem;
  font-weight: 700;
  background: linear-gradient(135deg, #6c5ce7, #8f7cff);
  box-shadow: 0 12px 28px rgba(108, 92, 231, 0.25);
  cursor: pointer;
}

.home-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.metric {
  text-align: center;
}

.metric__value {
  display: inline-flex;
  align-items: flex-start;
  justify-content: center;
  gap: 2px;
  color: #333;
  font-weight: 700;
  line-height: 1;
}

.metric__value span {
  font-size: 1.375rem;
}

.metric__value small {
  margin-top: 4px;
  font-size: 0.75rem;
}

.metric__label {
  margin: 6px 0 0;
  color: #999;
  font-size: 0.75rem;
}

.home-shortcuts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.shortcut {
  padding: 0;
  border: 0;
  background: transparent;
  display: grid;
  justify-items: center;
  gap: 8px;
  cursor: pointer;
}

.shortcut__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #6c5ce7;
  font-size: 0.85rem;
  font-weight: 700;
}

.shortcut__label {
  color: #333;
  font-size: 0.75rem;
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

.home-task__subject {
  margin: 12px 0 16px;
  color: #999;
  font-size: 0.875rem;
}

.home-task__progress {
  display: grid;
  gap: 12px;
}

.home-task__progress-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #999;
  font-size: 0.75rem;
}

.link-button {
  border: 0;
  background: transparent;
  color: #6c5ce7;
  font-size: 0.75rem;
  cursor: pointer;
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

.mistake-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.mistake-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.mistake-item:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.mistake-item__dot {
  width: 14px;
  height: 14px;
  margin-top: 3px;
  border-radius: 50%;
  background: #ff7675;
  flex: 0 0 auto;
}

.mistake-item__content strong {
  display: block;
  color: #000;
  font-size: 0.875rem;
  font-weight: 700;
}

.mistake-item__content p {
  margin: 6px 0 0;
  color: #999;
  font-size: 0.75rem;
}

@media (max-width: 420px) {
  .home-hero,
  .home-summary,
  .home-shortcuts,
  .home-task,
  .home-wrong-book {
    width: calc(100% - 24px);
    margin-left: 12px;
    margin-right: 12px;
  }

  .home-shortcuts {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>