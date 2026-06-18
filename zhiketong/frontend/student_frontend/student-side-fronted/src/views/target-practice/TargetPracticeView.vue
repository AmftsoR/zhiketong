<template>
  <AppMobileFrame :time="currentTime" frame-class="practice-frame" scroll-class="practice-scroll">
    <template #topbar>
      <div class="practice-topbar">
        <button type="button" class="icon-button" @click="goBack" aria-label="返回">
          <span class="icon-button__arrow"></span>
        </button>
        <div class="practice-topbar__center">
          <h1 class="practice-topbar__title">靶向练习推送</h1>
          <span class="practice-topbar__subject">{{ subjectLabel }}</span>
        </div>
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
          :disabled="showResult"
          @click="pickOption(option.key)"
        >
          <span class="option-item__label">{{ option.key }}.</span>
          <span class="option-item__text" v-html="option.text"></span>
          <span v-if="showResult && option.key === question.correctAnswer" class="option-item__marker option-item__marker--correct">✓</span>
          <span v-else-if="showResult && option.key === selectedAnswer && option.key !== question.correctAnswer" class="option-item__marker option-item__marker--wrong">✗</span>
        </button>
      </div>

      <!-- 回答正确提示 -->
      <div v-if="showResult && isCorrect" class="result-feedback result-feedback--correct">
        <span class="result-feedback__icon">✓</span>
        <span>回答正确！</span>
      </div>

      <!-- 回答错误提示 + 查看正确答案按钮 -->
      <div v-if="showResult && !isCorrect && !showCorrectAnswer" class="result-feedback result-feedback--wrong">
        <span class="result-feedback__icon">✗</span>
        <span>回答错误，再想想？</span>
        <button type="button" class="show-answer-btn" @click="showCorrectAnswerAction">查看正确答案</button>
      </div>

      <div v-if="showCorrectAnswer" class="answer-panel">
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
        <button
          v-if="showResult && (isCorrect || showCorrectAnswer)"
          type="button"
          class="practice-footer__button"
          @click="nextQuestion"
        >
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
const showCorrectAnswer = computed(() => practice.showCorrectAnswer)
const isCorrect = computed(() => showResult.value && selectedAnswer.value === question.value.correctAnswer)
const modes = computed(() => practice.modes)
const practiceTips = computed(() => practice.tips)
const progress = computed(() => practice.progress)
const questionList = computed(() => studentStore.currentPracticeQuestions)
const question = computed(() => studentStore.currentPracticeQuestion)
const progressPercent = computed(() => {
  if (!progress.value.total) return 0
  return Math.round((progress.value.current / progress.value.total) * 100)
})
const nextButtonLabel = computed(() => (currentQuestionIndex.value < questionList.value.length - 1 ? '同类变式' : '完成本组'))
const subjectLabelMap = { math: '数学', physics: '物理', english: '英语' }
const subjectLabel = computed(() => subjectLabelMap[studentStore.weaknessMap.selectedSubjectKey] || '数学')
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
  const returnTo = route.query.returnTo
  if (returnTo === 'wrong-book') {
    router.push('/wrong-book')
  } else if (returnTo === 'favorites') {
    router.push('/favorites')
  } else {
    router.push('/study')
  }
}

function selectMode(key) {
  studentStore.selectPracticeMode(key)
}

function pickOption(key) {
  studentStore.pickPracticeAnswer(key)
}

function optionClass(key) {
  if (!showResult.value) {
    return {
      'option-item--selected': selectedAnswer.value === key,
    }
  }
  return {
    'option-item--correct': key === question.value.correctAnswer,
    'option-item--wrong': selectedAnswer.value === key && key !== question.value.correctAnswer,
  }
}

function showCorrectAnswerAction() {
  studentStore.showCorrectAnswerAction()
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

.practice-topbar__center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.practice-topbar__title {
  margin: 0;
  color: #000;
  font-size: 0.9375rem;
  font-weight: 600;
  text-align: center;
}

.practice-topbar__subject {
  font-size: 0.6875rem;
  color: #6c5ce7;
  background: #f0efff;
  padding: 1px 10px;
  border-radius: 999px;
  font-weight: 600;
}

.practice-topbar__spacer {
  width: 40px;
}

.practice-scroll {
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

.option-item--selected {
  border-color: #6c5ce7;
  background: #f0efff;
}

.option-item--wrong {
  background: #fff5f5;
  border-color: #ff7675;
}

.option-item--correct {
  background: #f0fff4;
  border-color: #00b894;
}

.option-item:disabled {
  cursor: default;
  pointer-events: none;
}

.option-item__marker {
  position: absolute;
  right: 16px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  color: #fff;
}

.option-item__marker--wrong {
  background: #ff7675;
}

.option-item__marker--correct {
  background: #00b894;
}

.result-feedback {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
}

.result-feedback--correct {
  background: #f0fff4;
  color: #00b894;
}

.result-feedback--wrong {
  background: #fff5f5;
  color: #ff7675;
  flex-wrap: wrap;
}

.result-feedback__icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  color: #fff;
}

.result-feedback--correct .result-feedback__icon {
  background: #00b894;
}

.result-feedback--wrong .result-feedback__icon {
  background: #ff7675;
}

.show-answer-btn {
  margin-left: auto;
  padding: 6px 14px;
  border: 1px solid #6c5ce7;
  border-radius: 999px;
  background: transparent;
  color: #6c5ce7;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease;
}

.show-answer-btn:hover {
  background: #f0efff;
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