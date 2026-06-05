<template>
  <AppMobileFrame :time="currentTime" frame-class="analysis-frame" scroll-class="analysis-scroll">
    <template #topbar>
      <div class="analysis-topbar">
        <button type="button" class="icon-button" @click="goBack" aria-label="返回">
          <span class="icon-button__arrow"></span>
        </button>
        <h1 class="analysis-topbar__title">学情分析报告</h1>

        <button type="button" class="month-filter" @click="toggleMonthMenu">
          <span>{{ selectedMonth.label }}</span>
          <span class="month-filter__arrow"></span>
        </button>
      </div>
    </template>

    <section class="hero-card page-card">
      <div class="hero-card__score">
        <span>{{ report.overallScore }}</span>
      </div>

      <div class="hero-card__content">
        <h2>综合学习效能指数</h2>
        <p>{{ report.summary }}</p>
      </div>
    </section>

    <section class="radar-card page-card">
      <div class="card-head">
        <h2>核心能力雷达图</h2>
      </div>

      <div class="radar-chart" aria-hidden="true">
        <div class="radar-chart__grid radar-chart__grid--outer"></div>
        <div class="radar-chart__grid radar-chart__grid--inner"></div>
        <div class="radar-chart__shape radar-chart__shape--class"></div>
        <div class="radar-chart__shape radar-chart__shape--mine"></div>
        <div
          v-for="label in report.radarLabels"
          :key="label.text"
          class="radar-chart__label"
          :style="{ left: label.left, top: label.top }"
        >
          {{ label.text }}
        </div>
      </div>

      <div class="legend-row">
        <div class="legend-row__item">
          <span class="legend-dot legend-dot--mine"></span>
          <span>我的能力</span>
        </div>
        <div class="legend-row__item">
          <span class="legend-dot legend-dot--class"></span>
          <span>班级平均</span>
        </div>
      </div>
    </section>

    <section class="chapter-card page-card">
      <div class="card-head">
        <h2>章节知识点掌握度 (量化)</h2>
      </div>

      <div class="chapter-list">
        <div v-for="chapter in report.chapters" :key="chapter.name" class="chapter-item">
          <div class="chapter-item__row">
            <span>{{ chapter.name }}</span>
            <strong :style="{ color: chapter.color }">{{ chapter.value }}%</strong>
          </div>

          <div class="chapter-item__track">
            <div class="chapter-item__fill" :style="{ width: `${chapter.value}%`, background: chapter.color }"></div>
          </div>
        </div>
      </div>
    </section>

    <section class="report-actions page-card">
      <button type="button" class="report-actions__button report-actions__button--ghost" @click="downloadReport">
        导出报告
      </button>
      <button type="button" class="report-actions__button report-actions__button--primary" @click="shareReport">
        分享给老师
      </button>
    </section>

    <template #floating>
      <transition name="fade-slide">
        <div v-if="showMonthMenu" class="month-panel">
          <button
            v-for="month in monthOptions"
            :key="month.key"
            type="button"
            class="month-panel__item"
            :class="{ 'month-panel__item--active': month.key === selectedMonthKey }"
            @click="selectMonth(month.key)"
          >
            {{ month.label }}
          </button>
        </div>
      </transition>
    </template>
  </AppMobileFrame>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import { useStudentStore } from '../../stores/studentStore'

const router = useRouter()
const studentStore = useStudentStore()

const currentTime = ref('9:41')
const selectedMonthKey = computed({
  get: () => studentStore.analysis.selectedMonthKey,
  set: (value) => studentStore.setReportMonth(value),
})
const showMonthMenu = computed(() => studentStore.analysis.showMonthMenu)
const monthOptions = computed(() => studentStore.analysis.monthOptions)
const report = computed(() => studentStore.currentReport)
const selectedMonth = computed(() => monthOptions.value.find((month) => month.key === selectedMonthKey.value) || monthOptions.value[0])

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

function toggleMonthMenu() {
  studentStore.toggleReportMonthMenu()
}

function selectMonth(key) {
  studentStore.setReportMonth(key)
}

function downloadReport() {
  showMonthMenu.value = false
}

function shareReport() {
  router.push('/profile')
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
.analysis-page {
  width: 100%;
}

.analysis-frame {
  max-width: 375px;
  margin: 0 auto;
  min-height: calc(100svh - 48px);
  background: #f5f7fa;
  border-radius: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 10px solid #fff;
  position: relative;
}

.analysis-status {
  height: 44px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #333;
  font-size: 0.875rem;
  font-weight: 600;
}

.analysis-status__indicators {
  display: inline-flex;
  gap: 7px;
  align-items: center;
}

.status-icon {
  display: inline-block;
  background: #333;
  border-radius: 999px;
}

.status-icon--signal {
  width: 14px;
  height: 14px;
}

.status-icon--wifi {
  width: 16px;
  height: 12px;
}

.status-icon--battery {
  width: 18px;
  height: 10px;
}

.analysis-topbar {
  height: 44.67px;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 40px 1fr auto;
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

.analysis-topbar__title {
  margin: 0;
  color: #000;
  font-size: 1.0625rem;
  font-weight: 600;
  text-align: center;
}

.month-filter {
  border: 0;
  background: transparent;
  color: #6c5ce7;
  font-size: 0.875rem;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.month-filter__arrow {
  width: 8px;
  height: 8px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg) translateY(-1px);
}

.analysis-scroll {
  height: calc(100svh - 109px);
  overflow: auto;
  padding: 0 0 16px;
}

.analysis-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.hero-card,
.radar-card,
.chapter-card,
.report-actions {
  width: calc(100% - 40px);
  margin: 0 20px 12px;
}

.hero-card {
  min-height: 110px;
  display: flex;
  align-items: center;
  gap: 18px;
  background: linear-gradient(135deg, #6c5ce7 0%, #a29bfe 100%);
  color: #fff;
  box-shadow: 0 8px 16px rgba(108, 92, 231, 0.2);
}

.hero-card__score {
  width: 45.61px;
  height: 70px;
  border-radius: 22.81px;
  outline: 4px solid rgba(255, 255, 255, 0.3);
  outline-offset: -4px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
}

.hero-card__score span {
  font-size: 1.5rem;
  font-weight: 700;
}

.hero-card__content h2 {
  margin: 0;
  font-size: 1.125rem;
}

.hero-card__content p {
  margin: 6px 0 0;
  font-size: 0.75rem;
  line-height: 1.5;
  opacity: 0.9;
}

.card-head h2 {
  margin: 0;
  color: #000;
  font-size: 0.9375rem;
}

.radar-chart {
  position: relative;
  width: 100%;
  height: 180px;
  margin-top: 18px;
}

.radar-chart__grid,
.radar-chart__shape {
  position: absolute;
  inset: 0;
  margin: auto;
  border-radius: 50%;
}

.radar-chart__grid--outer {
  width: 180px;
  height: 180px;
  border: 1.8px solid rgba(108, 92, 231, 0.25);
}

.radar-chart__grid--inner {
  width: 126px;
  height: 126px;
  border: 1.8px solid rgba(108, 92, 231, 0.18);
}

.radar-chart__shape--class {
  width: 90px;
  height: 81px;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  background: rgba(108, 92, 231, 0.1);
  border: 1.8px solid #6c5ce7;
}

.radar-chart__shape--mine {
  width: 144px;
  height: 135px;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  background: rgba(108, 92, 231, 0.4);
  border: 1.8px solid #6c5ce7;
}

.radar-chart__label {
  position: absolute;
  transform: translate(-50%, -50%);
  color: rgba(0, 0, 0, 0.64);
  font-size: 0.8125rem;
  font-weight: 500;
}

.legend-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.legend-row__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 0.75rem;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-dot--mine {
  background: rgba(108, 92, 231, 0.4);
}

.legend-dot--class {
  border: 1px solid #6c5ce7;
  background: rgba(108, 92, 231, 0.1);
}

.chapter-list {
  margin-top: 18px;
  display: grid;
  gap: 18px;
}

.chapter-item__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #333;
  font-size: 0.75rem;
}

.chapter-item__row strong {
  font-weight: 400;
}

.chapter-item__track {
  height: 6px;
  margin-top: 6px;
  border-radius: 999px;
  background: #eee;
  overflow: hidden;
}

.chapter-item__fill {
  height: 100%;
  border-radius: inherit;
  transition: width 0.3s ease;
}

.report-actions {
  display: flex;
  gap: 10px;
  padding: 0;
  background: transparent;
  box-shadow: none;
}

.report-actions__button {
  flex: 1 1 0;
  height: 40px;
  border-radius: 20px;
  border: 0;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
}

.report-actions__button--ghost {
  color: #333;
  background: #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.report-actions__button--primary {
  color: #fff;
  background: #6c5ce7;
  box-shadow: 0 4px 10px rgba(108, 92, 231, 0.3);
}

.month-panel {
  position: absolute;
  top: 88px;
  right: 14px;
  width: 110px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.15);
  padding: 8px;
  display: grid;
  gap: 6px;
}

.month-panel__item {
  border: 0;
  background: transparent;
  border-radius: 12px;
  padding: 10px 12px;
  text-align: left;
  color: #333;
  font-size: 0.8125rem;
  cursor: pointer;
}

.month-panel__item--active {
  background: rgba(108, 92, 231, 0.08);
  color: #6c5ce7;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@media (max-width: 420px) {
  .analysis-frame {
    max-width: 100%;
    border-radius: 0;
    border-width: 0;
  }

  .hero-card,
  .radar-card,
  .chapter-card,
  .report-actions {
    width: calc(100% - 24px);
    margin-left: 12px;
    margin-right: 12px;
  }
}
</style>