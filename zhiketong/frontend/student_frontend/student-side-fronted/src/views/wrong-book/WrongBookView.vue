<template>
  <AppMobileFrame :time="currentTime" frame-class="wrong-book-frame" scroll-class="wrong-book-scroll">
    <template #topbar>
      <div class="wrong-book-topbar">
        <button type="button" class="icon-button" @click="goBack" aria-label="返回">
          <span class="icon-button__arrow"></span>
        </button>

        <h1 class="wrong-book-topbar__title">个人错题本</h1>

        <button type="button" class="floating-summary" @click="scrollToTop" aria-label="回到顶部">
          <span class="floating-summary__icon"></span>
        </button>
      </div>
    </template>

    <section class="filter-bar" ref="scrollAnchor">
      <button
        v-for="subject in subjectOptions"
        :key="subject.key"
        type="button"
        class="filter-pill"
        :class="{ 'filter-pill--active': selectedSubject === subject.key }"
        @click="selectedSubject = subject.key"
      >
        <span>{{ subject.label }}</span>
        <span class="filter-pill__arrow"></span>
      </button>

      <button
        v-for="type in typeOptions"
        :key="type.key"
        type="button"
        class="filter-pill"
        :class="{ 'filter-pill--active': selectedType === type.key }"
        @click="selectedType = type.key"
      >
        <span>{{ type.label }}</span>
        <span class="filter-pill__arrow"></span>
      </button>

      <button
        type="button"
        class="filter-pill filter-pill--wide"
        :class="{ 'filter-pill--active': selectedView === 'attribution' }"
        @click="selectedView = selectedView === 'attribution' ? 'all' : 'attribution'"
      >
        <span>知识点归因</span>
        <span class="filter-pill__icon"></span>
      </button>
    </section>

    <section v-for="item in filteredMistakes" :key="item.id" class="mistake-card page-card">
      <div class="mistake-card__head">
        <span class="mistake-badge" :class="`mistake-badge--${item.level}`">{{ item.levelLabel }}</span>
        <span class="mistake-card__date">{{ item.date }}</span>
      </div>

      <p class="mistake-card__stem">{{ item.stem }}</p>

      <p class="mistake-card__origin">
        <span class="origin-dot"></span>
        <span>知识根源: {{ item.root }}</span>
      </p>

      <div class="mistake-card__footer">
        <div class="mistake-card__wrong-count">
          <span>做错</span>
          <strong>{{ item.wrongCount }}</strong>
          <span>次</span>
        </div>

        <div class="mistake-card__actions">
          <button type="button" class="action-btn action-btn--ghost" @click="openAnalysis(item)">看解析</button>
          <button type="button" class="action-btn action-btn--primary" @click="doVariant(item)">做变式</button>
        </div>
      </div>

      <div v-if="openedAnalysisId === item.id" class="analysis-panel">
        <div class="analysis-panel__row">
          <span class="analysis-panel__label">我的答案:</span>
          <strong class="analysis-panel__value analysis-panel__value--wrong">{{ item.myAnswer }}</strong>
        </div>
        <div class="analysis-panel__row">
          <span class="analysis-panel__label">正确答案:</span>
          <strong class="analysis-panel__value analysis-panel__value--right">{{ item.correctAnswer }}</strong>
        </div>
        <div class="analysis-panel__box">
          <p class="analysis-panel__box-title">AI 归因解析：</p>
          <p class="analysis-panel__box-content">{{ item.analysis }}</p>
        </div>
      </div>
    </section>

    <template #floating>
      <button type="button" class="floating-add" @click="createReviewTask" aria-label="新增复习任务">
        <span class="floating-add__icon"></span>
      </button>
    </template>

    <template #footer>
      <BottomNav :activeTab="'wrong-book'" />
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
const selectedSubject = computed({
  get: () => studentStore.wrongBook.selectedSubject,
  set: (value) => studentStore.setWrongBookSubject(value),
})
const selectedType = computed({
  get: () => studentStore.wrongBook.selectedType,
  set: (value) => studentStore.setWrongBookType(value),
})
const selectedView = computed({
  get: () => studentStore.wrongBook.selectedView,
  set: (value) => studentStore.toggleWrongBookView(value),
})
const openedAnalysisId = computed({
  get: () => studentStore.wrongBook.openedAnalysisId,
  set: (value) => studentStore.setOpenedAnalysisId(value),
})
const scrollAnchor = ref(null)

const subjectOptions = computed(() => studentStore.wrongBook.subjectOptions)
const typeOptions = computed(() => studentStore.wrongBook.typeOptions)
const filteredMistakes = computed(() => studentStore.filteredMistakes)

let clockTimer = null

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function goBack() {
  router.back()
}

function navigateTo(path) {
  if (route.path !== path) {
    router.push(path)
  }
}

function openAnalysis(item) {
  openedAnalysisId.value = openedAnalysisId.value === item.id ? '' : item.id
}

function doVariant(item) {
  router.push({ path: '/target-practice', query: { source: item.root } })
}

function createReviewTask() {
  router.push('/analysis-report')
}

function scrollToTop() {
  scrollAnchor.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 30000)

  if (route.query.source) {
    openedAnalysisId.value = 'mistake-1'
  }
})

onBeforeUnmount(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
})
</script>

<style scoped>
.wrong-book-topbar {
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

.wrong-book-topbar__title {
  margin: 0;
  color: #000;
  font-size: 1.0625rem;
  font-weight: 600;
  text-align: center;
}

.floating-summary {
  width: 23px;
  height: 23px;
  border: 0;
  border-radius: 50%;
  background: #6c5ce7;
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.4);
  justify-self: end;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.floating-summary__icon {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #fff;
}

.wrong-book-scroll {
  overflow: auto;
  padding: 0 0 16px;
}

.wrong-book-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.filter-bar,
.mistake-card {
  width: calc(100% - 40px);
  margin: 0 20px 12px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  padding-top: 2px;
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
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  padding: 0 14px;
  color: #333;
  font-size: 0.8125rem;
  cursor: pointer;
}

.filter-pill--active {
  background: #f0efff;
  color: #6c5ce7;
}

.filter-pill--wide {
  margin-left: auto;
}

.filter-pill__arrow {
  width: 8px;
  height: 8px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg) translateY(-1px);
}

.filter-pill__icon {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  background: currentColor;
}

.mistake-card {
  padding-bottom: 18px;
}

.mistake-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.mistake-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
  height: 18px;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 0.625rem;
}

.mistake-badge--weak {
  color: #ff7675;
  background: #ffebeb;
}

.mistake-badge--medium {
  color: #fdcb6e;
  background: #fff8e6;
}

.mistake-card__date {
  color: #999;
  font-size: 0.75rem;
}

.mistake-card__stem {
  margin: 14px 0 0;
  color: #333;
  font-size: 0.875rem;
  line-height: 1.65;
}

.mistake-card__origin {
  margin: 12px 0 0;
  color: #999;
  font-size: 0.75rem;
  display: flex;
  align-items: center;
  gap: 6px;
}

.origin-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ddd;
}

.mistake-card__footer {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.mistake-card__wrong-count {
  color: #333;
  font-size: 0.75rem;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.mistake-card__wrong-count strong {
  color: #ff7675;
  font-size: 0.875rem;
}

.mistake-card__actions {
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

.analysis-panel__value--wrong {
  color: #ff7675;
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

.floating-add {
  position: absolute;
  right: 20px;
  bottom: 95px;
  width: 50px;
  height: 50px;
  border: 0;
  border-radius: 50%;
  background: #6c5ce7;
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.4);
  display: grid;
  place-items: center;
  cursor: pointer;
}

.floating-add__icon {
  position: relative;
  width: 20px;
  height: 20px;
}

.floating-add__icon::before,
.floating-add__icon::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  background: #fff;
  transform: translate(-50%, -50%);
}

.floating-add__icon::before {
  width: 20px;
  height: 2px;
}

.floating-add__icon::after {
  width: 2px;
  height: 20px;
}

@media (max-width: 420px) {
  .filter-bar,
  .mistake-card {
    width: calc(100% - 24px);
    margin-left: 12px;
    margin-right: 12px;
  }

  .floating-add {
    right: 12px;
  }

  .filter-pill--wide {
    margin-left: 0;
  }
}
</style>