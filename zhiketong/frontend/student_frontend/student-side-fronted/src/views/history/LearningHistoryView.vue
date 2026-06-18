<template>
  <AppMobileFrame :time="currentTime" frame-class="history-frame" scroll-class="history-scroll">
    <template #topbar>
      <div class="history-topbar">
        <button type="button" class="icon-button" @click="goBack" aria-label="返回">
          <span class="icon-button__arrow"></span>
        </button>
        <h1 class="history-topbar__title">学习历史记录</h1>
        <span></span>
      </div>
    </template>

    <!-- 筛选栏 -->
    <section class="filter-bar">
      <button
        v-for="s in subjectOptions"
        :key="s.key"
        type="button"
        class="filter-pill"
        :class="{ 'filter-pill--active': selectedSubject === s.key }"
        @click="selectedSubject = s.key"
      >
        {{ s.label }}
      </button>
    </section>

    <!-- 统计概览 -->
    <section class="stats-card page-card">
      <div class="stats-row">
        <div class="stat-item">
          <strong>{{ stats.total }}</strong>
          <span>答题总数</span>
        </div>
        <div class="stat-item">
          <strong>{{ stats.correctRate }}%</strong>
          <span>正确率</span>
        </div>
        <div class="stat-item">
          <strong>{{ stats.studyDays }}</strong>
          <span>学习天数</span>
        </div>
      </div>
    </section>

    <!-- 加载状态 -->
    <section v-if="loading" class="loading-state">
      <p>加载中...</p>
    </section>

    <!-- 空状态 -->
    <section v-else-if="filteredRecords.length === 0" class="empty-state">
      <span class="empty-state__icon">📋</span>
      <p class="empty-state__title">暂无学习记录</p>
      <p class="empty-state__desc">完成练习后，答题记录会出现在这里</p>
    </section>

    <!-- 答题记录列表 -->
    <section v-for="record in filteredRecords" :key="record.id" class="record-card page-card">
      <div class="record-card__head">
        <span class="record-badge" :class="record.isCorrect ? 'record-badge--correct' : 'record-badge--wrong'">
          {{ record.isCorrect ? '正确' : '错误' }}
        </span>
        <span class="record-card__date">{{ record.dateStr }}</span>
      </div>

      <p class="record-card__stem">{{ record.stem }}</p>

      <div class="record-card__meta">
        <span class="record-card__tag">{{ record.typeLabel }}</span>
        <span v-if="record.subjectLabel" class="record-card__subject">{{ record.subjectLabel }}</span>
      </div>

      <div class="record-card__answer">
        <span class="record-card__label">我的答案: <strong :class="record.isCorrect ? 'text-correct' : 'text-wrong'">{{ record.userAnswer }}</strong></span>
        <span v-if="!record.isCorrect" class="record-card__label">正确答案: <strong class="text-correct">{{ record.correctAnswer }}</strong></span>
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
import { fetchHistory } from '../../api/history'

const router = useRouter()
const studentStore = useStudentStore()

const currentTime = ref('9:41')
const loading = ref(false)
const records = ref([])
const stats = ref({ total: 0, correctRate: 0, studyDays: 0 })
const selectedSubject = ref('all')

const subjectOptions = [
  { key: 'all', label: '全部' },
  { key: 'math', label: '数学' },
  { key: 'physics', label: '物理' },
  { key: 'english', label: '英语' },
]

const typeMap = { single_choice: '单选题', single: '单选题', fill: '填空题', judge: '判断题' }
const subjectMap = { math: '数学', physics: '物理', english: '英语' }

const filteredRecords = computed(() => {
  if (selectedSubject.value === 'all') return records.value
  return records.value.filter(r => r.subject === selectedSubject.value)
})

let clockTimer = null

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function goBack() {
  router.push('/profile')
}

function formatRecords(list) {
  return list.map(r => {
    let dateStr = ''
    if (r.answeredAt) {
      const d = new Date(r.answeredAt)
      dateStr = `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    }
    return {
      ...r,
      dateStr,
      typeLabel: typeMap[r.type] || '未知题型',
      subjectLabel: subjectMap[r.subject] || '',
    }
  })
}

async function loadHistory() {
  loading.value = true
  try {
    const res = await fetchHistory()
    if (res && res.data) {
      records.value = formatRecords(res.data.records || [])
      if (res.data.stats) {
        stats.value = res.data.stats
      }
    }
  } catch (e) {
    console.error('加载历史记录失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 30000)
  loadHistory()
})

onBeforeUnmount(() => {
  if (clockTimer) window.clearInterval(clockTimer)
})
</script>

<style scoped>
.history-topbar {
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

.history-topbar__title {
  margin: 0;
  color: #000;
  font-size: 1.0625rem;
  font-weight: 600;
  text-align: center;
}

.history-scroll {
  overflow: auto;
  padding: 0 0 16px;
}

.history-scroll::-webkit-scrollbar {
  width: 0; height: 0;
}

.filter-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  padding: 2px 20px 12px;
}

.filter-pill {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 31px;
  border: 0;
  border-radius: 15px;
  background: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  padding: 0 14px;
  color: #333;
  font-size: 0.8125rem;
  cursor: pointer;
}

.filter-pill--active {
  background: #f0efff;
  color: #6c5ce7;
}

/* 统计卡片 */
.stats-card {
  width: calc(100% - 40px);
  margin: 0 20px 12px;
  padding: 18px 20px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.stat-item {
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 1.25rem;
  color: #6c5ce7;
}

.stat-item span {
  display: block;
  margin-top: 4px;
  font-size: 0.75rem;
  color: #999;
}

/* 加载 / 空状态 */
.loading-state, .empty-state {
  margin: 60px 20px 0;
  text-align: center;
  color: #999;
}

.empty-state__icon {
  font-size: 2.5rem;
  display: block;
}

.empty-state__title {
  margin: 12px 0 0;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.empty-state__desc {
  margin: 6px 0 0;
  font-size: 0.8125rem;
  color: #999;
}

/* 记录卡片 */
.record-card {
  width: calc(100% - 40px);
  margin: 0 20px 12px;
  padding-bottom: 18px;
}

.record-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.record-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
  height: 18px;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 0.625rem;
}

.record-badge--correct {
  color: #00b894;
  background: #e6fff8;
}

.record-badge--wrong {
  color: #ff7675;
  background: #ffebeb;
}

.record-card__date {
  color: #999;
  font-size: 0.75rem;
}

.record-card__stem {
  margin: 14px 0 0;
  color: #333;
  font-size: 0.875rem;
  line-height: 1.65;
}

.record-card__meta {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.record-card__tag {
  color: #999;
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f5f5f5;
}

.record-card__subject {
  color: #6c5ce7;
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f0efff;
}

.record-card__answer {
  margin-top: 12px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.record-card__label {
  color: #999;
  font-size: 0.8125rem;
}

.text-correct { color: #00b894; }
.text-wrong { color: #ff7675; }

@media (max-width: 420px) {
  .stats-card, .record-card {
    width: calc(100% - 24px);
    margin-left: 12px;
    margin-right: 12px;
  }
  .filter-bar {
    padding-left: 12px;
    padding-right: 12px;
  }
}
</style>
