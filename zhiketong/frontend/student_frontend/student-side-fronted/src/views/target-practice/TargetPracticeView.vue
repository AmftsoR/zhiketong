<template>
  <AppMobileFrame :time="currentTime" frame-class="practice-frame" scroll-class="practice-scroll">
    <template #topbar>
      <div class="practice-topbar">
        <button type="button" class="icon-button" @click="goBack" aria-label="返回">
          <span class="icon-button__arrow"></span>
        </button>
        <h1 class="practice-topbar__title">靶向练习推送</h1>
        <div class="practice-topbar__spacer"></div>
      </div>
    </template>

    <section class="progress-card page-card">
      <div class="progress-card__head">
        <div>
          <p class="progress-card__title">今日练习 {{ progress.current }}/{{ progress.total }}</p>
          <p class="progress-card__subtitle">预计用时 {{ progress.estimatedMinutes }} 分钟</p>
        </div>
      </div>

      <div class="progress-bar" role="progressbar" :aria-valuenow="progressPercent" aria-valuemin="0" aria-valuemax="100">
        <div class="progress-bar__fill" :style="{ width: `${progressPercent}%` }"></div>
      </div>
    </section>

    <div class="mode-tabs page-card">
      <button
        v-for="mode in modes"
        :key="mode.key"
        type="button"
        class="mode-tabs__item"
        :class="{ 'mode-tabs__item--active': mode.key === activeMode }"
        @click="selectMode(mode.key)"
      >
        {{ mode.label }}
      </button>
    </div>

    <section class="question-card page-card">
      <div class="question-card__header">
        <span class="question-badge">{{ question.type }}</span>
        <span class="question-source">根源: {{ question.source }}</span>
      </div>

      <div class="question-stem" v-html="question.stem"></div>

      <div class="options-list">
        <button
          v-for="option in question.options"
          :key="option.key"
          type="button"
          class="option-item"
          :class="optionClass(option.key)"
          @click="pickOption(option.key)"
        >
          <span class="option-item__label">{{ option.key }}.</span>
          <span class="option-item__text" v-html="option.text"></span>

          <span v-if="showResult && option.key === selectedAnswer" class="option-item__marker option-item__marker--selected"></span>
          <span v-else-if="showResult && option.key === question.correctAnswer" class="option-item__marker option-item__marker--correct"></span>
        </button>
      </div>

      <div class="answer-panel">
        <div class="answer-row">
          <span class="answer-row__label">我的答案:</span>
          <strong class="answer-row__value answer-row__value--wrong">{{ selectedAnswer || '未作答' }}</strong>
        </div>

        <div class="answer-row">
          <span class="answer-row__label">正确答案:</span>
          <strong class="answer-row__value answer-row__value--right">{{ question.correctAnswer }}</strong>
        </div>

        <div class="analysis-box">
          <p class="analysis-box__title">AI 归因解析：</p>
          <p class="analysis-box__content">{{ question.analysis }}</p>
        </div>
      </div>
    </section>

    <section class="tips-card page-card">
      <button
        v-for="tip in practiceTips"
        :key="tip.key"
        type="button"
        class="tips-card__item"
        :class="{ 'tips-card__item--active': activeMode === tip.key }"
        @click="selectMode(tip.key)"
      >
        {{ tip.label }}
      </button>
    </section>

    <template #footer>
      <footer class="practice-footer">
        <button type="button" class="practice-footer__button" @click="nextQuestion">
          下一题 ({{ nextButtonLabel }})
        </button>

        <p class="practice-footer__hint">错一题、练一类、通一片</p>
      </footer>
    </template>
  </AppMobileFrame>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import { useStudentStore } from '../../stores/studentStore'

const router = useRouter()
const route = useRoute()
const studentStore = useStudentStore()

const currentTime = ref('9:41')
const practice = studentStore.practice
const activeMode = computed({
  get: () => practice.activeMode,
  set: (value) => studentStore.selectPracticeMode(value),
})
const selectedAnswer = computed({
  get: () => practice.selectedAnswer,
  set: (value) => studentStore.pickPracticeAnswer(value),
})
const showResult = computed(() => practice.showResult)
const modes = computed(() => practice.modes)
const practiceTips = computed(() => practice.tips)
const progress = computed(() => practice.progress)
const questionList = computed(() => studentStore.currentPracticeQuestions)
const question = computed(() => studentStore.currentPracticeQuestion)
const progressPercent = computed(() => Math.round((progress.value.current / progress.value.total) * 100))
const nextButtonLabel = computed(() => (currentQuestionIndex.value < questionList.value.length - 1 ? '同类变式' : '完成本组'))
const sourceHint = computed(() => route.query.source || '函数定义域')
const currentQuestionIndex = computed(() => practice.currentQuestionIndex)

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

function selectMode(key) {
  studentStore.selectPracticeMode(key)
}

function pickOption(key) {
  studentStore.pickPracticeAnswer(key)
}

function optionClass(key) {
  return {
    'option-item--selected': showResult.value && selectedAnswer.value === key && key !== question.value.correctAnswer,
    'option-item--wrong': showResult.value && selectedAnswer.value === key && key !== question.value.correctAnswer,
    'option-item--correct': showResult.value && key === question.value.correctAnswer,
  }
}

function nextQuestion() {
  studentStore.nextPracticeQuestion()
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
.practice-page {
  width: 100%;
}

.practice-frame {
  max-width: 375px;
  margin: 0 auto;
  min-height: calc(100svh - 48px);
  background: #f5f7fa;
  border-radius: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 10px solid #fff;
  display: flex;
  flex-direction: column;
}

.practice-status {
  height: 44px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #333;
  font-size: 0.875rem;
  font-weight: 600;
}

.practice-status__indicators {
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

.practice-topbar {
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

.practice-topbar__title {
  margin: 0;
  color: #000;
  font-size: 1.0625rem;
  font-weight: 600;
  text-align: center;
}

.practice-topbar__spacer {
  width: 40px;
}

.practice-scroll {
  height: calc(100svh - 109px - 65px);
  overflow: auto;
  padding: 0 0 16px;
}

.practice-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.progress-card,
.mode-tabs,
.question-card,
.tips-card {
  width: calc(100% - 40px);
  margin: 0 20px 12px;
}

.progress-card {
  padding-bottom: 20px;
}

.progress-card__title {
  margin: 0;
  color: #000;
  font-size: 0.875rem;
  font-weight: 700;
}

.progress-card__subtitle {
  margin: 0;
  color: #999;
  font-size: 0.75rem;
}

.progress-bar {
  height: 6px;
  margin-top: 10px;
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

.mode-tabs {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  padding: 0;
  background: transparent;
  box-shadow: none;
}

.mode-tabs__item,
.tips-card__item {
  border: 0;
  background: transparent;
  cursor: pointer;
  color: #999;
  font-size: 0.875rem;
  font-weight: 600;
}

.mode-tabs__item {
  padding: 10px 14px;
  border-radius: 999px;
}

.mode-tabs__item--active,
.tips-card__item--active {
  color: #6c5ce7;
  background: rgba(108, 92, 231, 0.08);
}

.question-card {
  padding-bottom: 20px;
}

.question-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.question-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
  height: 18px;
  padding: 0 6px;
  color: #6c5ce7;
  background: #f0efff;
  border-radius: 4px;
  font-size: 0.625rem;
}

.question-source {
  color: #999;
  font-size: 0.75rem;
}

.question-stem {
  margin-top: 14px;
  color: #333;
  font-size: 1rem;
  line-height: 1.8;
}

.question-stem :deep(.math) {
  font-weight: 600;
}

.options-list {
  display: grid;
  gap: 14px;
  margin-top: 14px;
}

.option-item {
  position: relative;
  width: 100%;
  border: 1px solid #eee;
  border-radius: 8px;
  background: #fafafa;
  padding: 12px 44px 12px 16px;
  text-align: left;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #333;
  transition: transform 0.15s ease, border-color 0.15s ease, background-color 0.15s ease;
}

.option-item:hover {
  transform: translateY(-1px);
}

.option-item__label {
  font-size: 0.9375rem;
  font-weight: 400;
}

.option-item__text {
  font-size: 0.9375rem;
}

.option-item--selected.option-item--wrong {
  background: #fff5f5;
  border-color: #ff7675;
}

.option-item--correct {
  background: #f0fff4;
  border-color: #00b894;
}

.option-item__marker {
  position: absolute;
  right: 16px;
  width: 15px;
  height: 15px;
  border-radius: 50%;
}

.option-item__marker--selected {
  background: #ff7675;
}

.option-item__marker--correct {
  background: #00b894;
}

.answer-panel {
  border-top: 1px solid #eee;
  margin-top: 18px;
  padding-top: 18px;
  display: grid;
  gap: 12px;
}

.answer-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.answer-row__label {
  color: #999;
  font-size: 0.875rem;
}

.answer-row__value {
  font-size: 0.875rem;
  font-weight: 700;
}

.answer-row__value--wrong {
  color: #ff7675;
}

.answer-row__value--right {
  color: #00b894;
}

.analysis-box {
  padding: 12px 10px;
  border-radius: 8px;
  background: #f8f9fa;
}

.analysis-box__title {
  margin: 0;
  color: #999;
  font-size: 0.8125rem;
  font-weight: 700;
}

.analysis-box__content {
  margin: 8px 0 0;
  color: #999;
  font-size: 0.8125rem;
  line-height: 1.5;
}

.tips-card {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 0;
  background: transparent;
  box-shadow: none;
}

.tips-card__item {
  flex: 1 1 0;
  padding: 10px 12px;
  border-radius: 999px;
  text-align: center;
}

.practice-footer {
  padding: 0 20px 14px;
}

.practice-footer__button {
  width: 100%;
  height: 51px;
  border: 0;
  border-radius: 25px;
  background: #6c5ce7;
  color: #fff;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(108, 92, 231, 0.3);
}

.practice-footer__hint {
  margin: 12px 0 0;
  text-align: center;
  color: #999;
  font-size: 0.75rem;
}

@media (max-width: 420px) {
  .practice-frame {
    max-width: 100%;
    border-radius: 0;
    border-width: 0;
  }

  .progress-card,
  .mode-tabs,
  .question-card,
  .tips-card {
    width: calc(100% - 24px);
    margin-left: 12px;
    margin-right: 12px;
  }
}
</style>