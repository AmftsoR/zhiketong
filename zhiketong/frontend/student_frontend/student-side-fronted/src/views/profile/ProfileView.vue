<template>
  <AppMobileFrame :time="currentTime" frame-class="profile-frame" scroll-class="profile-scroll">
    <template #topbar>
      <div class="profile-topbar">
        <h1 class="profile-topbar__title">我的界面</h1>
        <button type="button" class="profile-topbar__notify" @click="openNotificationPanel" aria-label="消息通知">
          <span class="profile-topbar__notify-icon"></span>
        </button>
      </div>
    </template>

    <section class="profile-hero">
      <div class="profile-hero__background"></div>

      <div class="profile-hero__header">
        <img class="profile-avatar" :src="profile.avatar" :alt="profile.name" />

        <div class="profile-hero__title-wrap">
          <div class="profile-hero__name-row">
            <h2>{{ profile.name }}</h2>
            <span class="vip-badge">智课通 VIP</span>
          </div>
          <p>{{ profile.className }} | 学号: {{ profile.studentId }}</p>
        </div>

        <button type="button" class="profile-hero__more" @click="goToSettings" aria-label="进入设置">
          <span class="profile-hero__more-icon"></span>
        </button>
      </div>

      <div class="profile-stats">
        <div v-for="stat in stats" :key="stat.label" class="profile-stat">
          <strong>{{ stat.value }}</strong>
          <span>{{ stat.label }}</span>
        </div>
      </div>
    </section>

    <section class="menu-card page-card">
      <button
        v-for="item in topMenus"
        :key="item.key"
        type="button"
        class="menu-card__item"
        @click="handleMenu(item.key)"
      >
        <span class="menu-card__icon" :class="`menu-card__icon--${item.key}`"></span>
        <span class="menu-card__label">{{ item.label }}</span>
        <span class="menu-card__arrow"></span>
      </button>
    </section>

    <section class="menu-card page-card">
      <button
        v-for="item in bottomMenus"
        :key="item.key"
        type="button"
        class="menu-card__item"
        @click="handleMenu(item.key)"
      >
        <span class="menu-card__icon" :class="`menu-card__icon--${item.key}`"></span>
        <span class="menu-card__label">{{ item.label }}</span>
        <span class="menu-card__arrow"></span>
      </button>
    </section>

    <template #footer>
      <BottomNav :activeTab="'profile'" />
    </template>
  </AppMobileFrame>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import BottomNav from '../../components/layout/BottomNav.vue'
import { useStudentStore } from '../../stores/studentStore'

const router = useRouter()
const studentStore = useStudentStore()

const currentTime = ref('9:41')
const profile = computed(() => studentStore.profile)
const stats = computed(() => [
  { label: '学习天数', value: String(profile.value.studyDays) },
  { label: '累计积分', value: profile.value.points.toLocaleString('zh-CN') },
  { label: '获得徽章', value: String(profile.value.badges) },
])

const topMenus = [
  { key: 'history', label: '学习历史记录' },
  { key: 'support', label: '帮助与客服' },
  { key: 'settings', label: '设置' },
]

const bottomMenus = [
  { key: 'goal', label: '我的学习目标' },
  { key: 'export', label: '错题本导出 / 打印' },
  { key: 'favorite', label: '我的收藏' },
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
  if (router.currentRoute.value.path !== path) {
    router.push(path)
  }
}

function goToSettings() {
  handleMenu('settings')
}

function handleMenu(key) {
  if (key === 'history') {
    router.push('/analysis-report')
    return
  }

  if (key === 'support') {
    router.push('/ai-assistant')
    return
  }

  if (key === 'settings') {
    studentStore.profile.name = '张同学'
    return
  }

  if (key === 'goal') {
    router.push('/analysis-report')
    return
  }

  if (key === 'export') {
    router.push('/wrong-book')
    return
  }

  if (key === 'favorite') {
    router.push('/wrong-book')
  }
}

function openNotificationPanel() {
  router.push('/analysis-report')
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
.profile-topbar {
  height: 44.67px;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
}

.profile-topbar__title {
  margin: 0;
  color: #000;
  font-size: 1.0625rem;
  font-weight: 600;
}

.profile-topbar__notify {
  width: 12.25px;
  height: 14px;
  border: 0;
  background: transparent;
  cursor: pointer;
  display: grid;
  place-items: center;
}

.profile-topbar__notify-icon {
  width: 12px;
  height: 11px;
  background: #6c5ce7;
  clip-path: polygon(0 20%, 50% 0, 100% 20%, 100% 100%, 0 100%);
}

.profile-scroll {
  flex: 1 1 auto;
  overflow: auto;
  padding: 0 0 12px;
}

.profile-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.profile-hero {
  position: relative;
  margin: 0 20px 12px;
  min-height: 266.33px;
  overflow: hidden;
  border-bottom-left-radius: 30px;
  border-bottom-right-radius: 30px;
}

.profile-hero__background {
  position: absolute;
  inset: 0;
  background: linear-gradient(168deg, #6c5ce7 0%, #a29bfe 100%);
  border-bottom-left-radius: 30px;
  border-bottom-right-radius: 30px;
}

.profile-hero__header {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 52px 1fr 24px;
  gap: 14px;
  align-items: center;
  padding: 20px;
}

.profile-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.25);
}

.profile-hero__title-wrap {
  color: #fff;
}

.profile-hero__name-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.profile-hero__name-row h2 {
  margin: 0;
  font-size: 1.25rem;
}

.vip-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 60.75px;
  height: 18px;
  padding: 0 6px;
  border-radius: 10px;
  background: #fdcb6e;
  color: #8c6100;
  font-size: 0.625rem;
  font-weight: 700;
}

.profile-hero__title-wrap p {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.75rem;
}

.profile-hero__more {
  width: 8.75px;
  height: 14px;
  border: 0;
  background: transparent;
  justify-self: end;
  cursor: pointer;
}

.profile-hero__more-icon {
  width: 7px;
  height: 12px;
  display: inline-block;
  background: rgba(255, 255, 255, 0.7);
  clip-path: polygon(0 0, 100% 50%, 0 100%);
}

.profile-stats {
  position: relative;
  z-index: 1;
  margin: 8px 20px 20px;
  padding: 15px 0;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(2.5px);
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.profile-stat {
  text-align: center;
  color: #fff;
}

.profile-stat strong {
  display: block;
  font-size: 1.125rem;
}

.profile-stat span {
  display: block;
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.75rem;
}

.menu-card {
  width: calc(100% - 40px);
  margin: 0 20px 12px;
  padding: 0;
  overflow: hidden;
  border-radius: 16px;
}

.menu-card__item {
  width: 100%;
  height: 62.67px;
  padding: 0 20px;
  border: 0;
  border-bottom: 0.67px solid #f5f5f5;
  background: #fff;
  display: grid;
  grid-template-columns: 30px 1fr auto;
  gap: 14px;
  align-items: center;
  cursor: pointer;
}

.menu-card__item:last-child {
  border-bottom: 0;
}

.menu-card__icon {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  position: relative;
}

.menu-card__icon::before {
  content: '';
  width: 15px;
  height: 15px;
  background: currentColor;
  display: block;
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
}

.menu-card__icon--history {
  color: #2f54eb;
  background: #f0f5ff;
}

.menu-card__icon--support {
  color: #eb2f96;
  background: #fff0f6;
}

.menu-card__icon--settings {
  color: #595959;
  background: #f5f5f5;
}

.menu-card__icon--goal {
  color: #fa8c16;
  background: #fff4e6;
}

.menu-card__icon--export {
  color: #1890ff;
  background: #e6f7ff;
}

.menu-card__icon--favorite {
  color: #52c41a;
  background: #f6ffed;
}

.menu-card__label {
  color: #333;
  font-size: 0.9375rem;
  text-align: left;
}

.menu-card__arrow {
  width: 7px;
  height: 12px;
  background: #ccc;
  clip-path: polygon(0 0, 100% 50%, 0 100%);
}

@media (max-width: 420px) {
  .profile-hero,
  .menu-card {
    width: calc(100% - 24px);
    margin-left: 12px;
    margin-right: 12px;
  }
}
</style>