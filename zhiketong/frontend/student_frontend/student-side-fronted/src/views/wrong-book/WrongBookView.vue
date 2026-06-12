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
          <button type="button" class="action-btn action-btn--danger" @click="handleDelete(item)">移除</button>
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

    <!-- 添加错题弹窗 -->
    <Teleport to="body">
      <div v-if="showAddModal" class="add-modal-mask" @click.self="closeAddForm">
        <div class="add-modal-card">
          <div class="add-modal__head">
            <h3>添加错题</h3>
            <button type="button" class="add-modal__close" @click="closeAddForm" aria-label="关闭">×</button>
          </div>

          <form class="add-modal__form" @submit.prevent="submitNewMistake">
            <div class="form-group">
              <label>题目（题干）<span class="required">*</span></label>
              <textarea
                v-model="addForm.stem"
                class="form-input form-textarea"
                placeholder="请输入题目内容"
                required
              ></textarea>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>科目</label>
                <select v-model="addForm.subject" class="form-input">
                  <option value="math">数学</option>
                  <option value="physics">物理</option>
                  <option value="english">英语</option>
                </select>
              </div>
              <div class="form-group">
                <label>题型</label>
                <select v-model="addForm.type" class="form-input">
                  <option value="single">单选题</option>
                  <option value="fill">填空题</option>
                  <option value="judge">判断题</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>正确答案<span class="required">*</span></label>
                <input
                  v-model="addForm.correctAnswer"
                  class="form-input"
                  placeholder="如：D"
                  required
                />
              </div>
              <div class="form-group">
                <label>我的答案</label>
                <input
                  v-model="addForm.myAnswer"
                  class="form-input"
                  placeholder="如：B（选填）"
                />
              </div>
            </div>

            <div class="form-group">
              <label>选项（JSON格式）</label>
              <input
                v-model="addForm.options"
                class="form-input"
                placeholder='如：[{"key":"A","text":"选项A"},{"key":"B","text":"选项B"}]'
              />
            </div>

            <div class="form-group">
              <label>解析</label>
              <textarea
                v-model="addForm.analysis"
                class="form-input form-textarea"
                placeholder="请输入题目解析（选填）"
              ></textarea>
            </div>

            <p v-if="addFormError" class="add-modal__error">{{ addFormError }}</p>

            <div class="add-modal__actions">
              <button type="button" class="btn-cancel" @click="closeAddForm">取消</button>
              <button type="submit" class="btn-submit" :disabled="addFormSubmitting">
                {{ addFormSubmitting ? '提交中...' : '确认添加' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <template #floating>
      <button type="button" class="floating-add" @click="openAddForm" aria-label="添加错题">
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

// ===== 添加错题表单 =====
const showAddModal = ref(false)
const addForm = ref({ subject: 'math', type: 'single', stem: '', options: '', correctAnswer: '', myAnswer: '', analysis: '' })
const addFormError = ref('')
const addFormSubmitting = ref(false)

function openAddForm() {
  addForm.value = { subject: 'math', type: 'single', stem: '', options: '', correctAnswer: '', myAnswer: '', analysis: '' }
  addFormError.value = ''
  addFormSubmitting.value = false
  showAddModal.value = true
}

function closeAddForm() {
  showAddModal.value = false
  addFormError.value = ''
}

async function submitNewMistake() {
  addFormError.value = ''
  addFormSubmitting.value = true

  try {
    const success = await studentStore.addMistakeEntry({
      subject: addForm.value.subject,
      type: addForm.value.type,
      difficulty: 'medium',
      stem: addForm.value.stem,
      options: addForm.value.options || '[]',
      correctAnswer: addForm.value.correctAnswer,
      myAnswer: addForm.value.myAnswer || '',
      analysis: addForm.value.analysis || '',
    })

    if (success) {
      showAddModal.value = false
    } else {
      addFormError.value = '添加失败，请检查网络连接或稍后重试'
    }
  } catch (e) {
    addFormError.value = '发生未知错误，请稍后重试'
  } finally {
    addFormSubmitting.value = false
  }
}

async function handleDelete(item) {
  const questionId = item.questionId || item.id
  await studentStore.removeMistake(questionId)
}

function scrollToTop() {
  scrollAnchor.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 30000)

  // 从后端加载错题数据
  studentStore.loadMistakes()

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

/* ===== 添加错题弹窗 ===== */
.add-modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 9999;
  display: grid;
  place-items: center;
  padding: 20px;
}

.add-modal-card {
  width: 100%;
  max-width: 400px;
  max-height: 85vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 14px;
  padding: 22px 20px 20px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.add-modal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.add-modal__head h3 {
  margin: 0;
  font-size: 1.0625rem;
  font-weight: 700;
  color: #333;
}

.add-modal__close {
  width: 28px;
  height: 28px;
  border: 0;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 1.125rem;
  color: #999;
  cursor: pointer;
  display: grid;
  place-items: center;
  line-height: 1;
}

.add-modal__form {
  display: grid;
  gap: 14px;
}

.form-group {
  display: grid;
  gap: 6px;
  flex: 1;
}

.form-group label {
  color: #555;
  font-size: 0.8125rem;
  font-weight: 600;
}

.required {
  color: #ff7675;
}

.form-input {
  width: 100%;
  height: 36px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0 10px;
  font-size: 0.8125rem;
  color: #333;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.15s;
}

.form-input:focus {
  border-color: #6c5ce7;
}

.form-textarea {
  height: 80px;
  padding: 10px;
  resize: vertical;
}

.form-row {
  display: flex;
  gap: 12px;
}

.add-modal__actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 4px;
}

.btn-cancel,
.btn-submit {
  min-width: 80px;
  height: 36px;
  border: 0;
  border-radius: 8px;
  font-size: 0.8125rem;
  cursor: pointer;
  padding: 0 18px;
}

.btn-cancel {
  color: #888;
  background: #f5f5f5;
}

.btn-submit {
  color: #fff;
  background: #6c5ce7;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.add-modal__error {
  margin: 0;
  padding: 8px 12px;
  border-radius: 6px;
  background: #ffebeb;
  color: #ff7675;
  font-size: 0.8125rem;
  text-align: center;
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