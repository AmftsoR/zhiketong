<template>
  <AppMobileFrame :time="currentTime" frame-class="favorite-frame" scroll-class="favorite-scroll">
    <template #topbar>
      <div class="favorite-topbar">
        <button type="button" class="icon-button" @click="goBack" aria-label="返回">
          <span class="icon-button__arrow"></span>
        </button>
        <h1 class="favorite-topbar__title">我的收藏</h1>
        <span></span>
      </div>
    </template>

    <!-- 空状态 -->
    <section v-if="favoriteList.length === 0" class="empty-state">
      <span class="empty-state__icon">⭐</span>
      <p class="empty-state__title">暂无收藏</p>
      <p class="empty-state__desc">在练习或错题本中点击收藏，题目会出现在这里</p>
    </section>

    <!-- 收藏列表 -->
    <section v-for="item in favoriteList" :key="item.id" class="fav-card page-card">
      <div class="fav-card__head">
        <span class="fav-badge">{{ item.subjectLabel }}</span>
        <span class="fav-card__date">{{ item.date }}</span>
      </div>

      <p class="fav-card__stem">{{ item.stem }}</p>

      <div class="fav-card__footer">
        <div class="fav-card__tag">{{ item.typeLabel }}</div>
        <div class="fav-card__actions">
          <button type="button" class="action-btn action-btn--ghost" @click="toggleAnalysis(item)">看解析</button>
          <button type="button" class="action-btn action-btn--primary" @click="goPractice(item)">去练习</button>
          <button type="button" class="action-btn action-btn--danger" @click="removeFavorite(item)">取消收藏</button>
        </div>
      </div>

      <div v-if="openedId === item.id" class="analysis-panel">
        <div class="analysis-panel__row">
          <span class="analysis-panel__label">正确答案:</span>
          <strong class="analysis-panel__value analysis-panel__value--right">{{ item.correctAnswer }}</strong>
        </div>
        <div class="analysis-panel__box">
          <p class="analysis-panel__box-title">解析：</p>
          <p class="analysis-panel__box-content">{{ item.analysis }}</p>
        </div>
      </div>
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
const openedId = ref('')

const subjectMap = { math: '数学', physics: '物理', english: '英语' }
const typeMap = { single: '单选题', fill: '填空题', judge: '判断题' }

const favoriteList = computed(() =>
  studentStore.favorites.map((item) => ({
    id: item.id,
    subjectLabel: subjectMap[item.subject] || '数学',
    typeLabel: typeMap[item.type] || '单选题',
    date: item.date,
    stem: item.stem,
    correctAnswer: item.correctAnswer,
    analysis: item.analysis || '暂无解析',
    subject: item.subject,
  })),
)

let clockTimer = null

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function goBack() {
  router.push('/profile')
}

function toggleAnalysis(item) {
  openedId.value = openedId.value === item.id ? '' : item.id
}

function goPractice(item) {
  router.push({ path: '/target-practice', query: { source: item.subject, returnTo: 'favorites' } })
}

function removeFavorite(item) {
  studentStore.removeFavorite(item.id)
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 30000)
  // 从后端加载收藏列表
  studentStore.loadFavorites()
})

onBeforeUnmount(() => {
  if (clockTimer) window.clearInterval(clockTimer)
})
</script>

<style scoped>
.favorite-topbar {
  height: 44.67px;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 40px 1fr 40px;
  align-items: center;
}

.icon-button {
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.icon-button__arrow {
  width: 9px;
  height: 15px;
  border-left: 2px solid #333;
  border-bottom: 2px solid #333;
  transform: rotate(45deg);
  margin-left: 6px;
}

.favorite-topbar__title {
  margin: 0;
  color: #000;
  font-size: 1.0625rem;
  font-weight: 600;
  text-align: center;
}

.favorite-scroll {
  overflow: auto;
  padding: 0 0 16px;
}

.favorite-scroll::-webkit-scrollbar {
  width: 0; height: 0;
}

/* 空状态 */
.empty-state {
  margin: 80px 20px 0;
  text-align: center;
}

.empty-state__icon {
  font-size: 3rem;
  display: block;
}

.empty-state__title {
  margin: 16px 0 0;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.empty-state__desc {
  margin: 8px 0 0;
  font-size: 0.8125rem;
  color: #999;
  line-height: 1.5;
}

/* 收藏卡片 */
.fav-card {
  width: calc(100% - 40px);
  margin: 0 20px 12px;
  padding-bottom: 18px;
}

.fav-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.fav-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
  height: 18px;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 0.625rem;
  color: #6c5ce7;
  background: #f0efff;
}

.fav-card__date {
  color: #999;
  font-size: 0.75rem;
}

.fav-card__stem {
  margin: 14px 0 0;
  color: #333;
  font-size: 0.875rem;
  line-height: 1.65;
}

.fav-card__footer {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.fav-card__tag {
  color: #999;
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f5f5f5;
}

.fav-card__actions {
  display: inline-flex;
  gap: 8px;
}

.action-btn {
  min-width: 56px;
  height: 26px;
  border-radius: 12px;
  border: 0;
  padding: 0 12px;
  font-size: 0.75rem;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
}

.action-btn--ghost {
  color: #333;
  background: #fff;
  outline: 0.67px solid #eee;
}

.action-btn--primary {
  color: #6c5ce7;
  background: #f0efff;
}

.action-btn--danger {
  color: #ff7675;
  background: #ffebeb;
}

/* 解析面板 */
.analysis-panel {
  margin-top: 16px;
  border-top: 1px solid #f5f5f5;
  padding-top: 14px;
  display: grid;
  gap: 10px;
}

.analysis-panel__row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.analysis-panel__label {
  color: #999;
  font-size: 0.875rem;
}

.analysis-panel__value {
  font-size: 0.875rem;
  font-weight: 700;
}

.analysis-panel__value--right {
  color: #00b894;
}

.analysis-panel__box {
  padding: 12px 10px;
  border-radius: 8px;
  background: #f8f9fa;
}

.analysis-panel__box-title {
  margin: 0;
  color: #999;
  font-size: 0.8125rem;
  font-weight: 700;
}

.analysis-panel__box-content {
  margin: 8px 0 0;
  color: #999;
  font-size: 0.8125rem;
  line-height: 1.5;
}

@media (max-width: 420px) {
  .fav-card {
    width: calc(100% - 24px);
    margin-left: 12px;
    margin-right: 12px;
  }
}
</style>
