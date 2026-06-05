<template>
  <AppMobileFrame :time="currentTime" frame-class="assistant-frame" scroll-class="assistant-scroll">
    <template #banner>
      <div class="assistant-top-warning">
        <span class="assistant-top-warning__icon"></span>
        <p>根据教育政策，AI助手仅提供思路引导，不直接给答案</p>
      </div>
    </template>

    <template #topbar>
      <div class="assistant-topbar">
        <button type="button" class="icon-button" @click="goBack" aria-label="返回">
          <span class="icon-button__arrow"></span>
        </button>
        <h1 class="assistant-topbar__title">AI 启发式答疑助理</h1>
        <button type="button" class="assistant-topbar__action" @click="clearChat" aria-label="清空对话">
          <span class="assistant-topbar__action-icon"></span>
        </button>
      </div>
    </template>

    <article v-for="message in visibleMessages" :key="message.id" class="message-row" :class="`message-row--${message.role}`">
      <div class="message-avatar" :class="`message-avatar--${message.role}`">
        <span>{{ message.avatar }}</span>
      </div>

      <div class="message-bubble" :class="`message-bubble--${message.role}`">
        <p v-for="(line, index) in message.lines" :key="`${message.id}-${index}`" v-html="line"></p>

        <template v-if="message.quickActions?.length">
          <div class="message-actions">
            <button
              v-for="action in message.quickActions"
              :key="action.key"
              type="button"
              class="message-actions__button"
              @click="handleQuickAction(action.key)"
            >
              {{ action.label }}
            </button>
          </div>
        </template>

        <template v-if="message.showHint">
          <div class="hint-block">
            <strong>{{ message.hintTitle }}</strong>
            <p>{{ message.hintText }}</p>
          </div>
        </template>
      </div>
    </article>

    <article v-if="studentThinking" class="message-row message-row--student">
      <div class="message-avatar message-avatar--student">
        <span>👦</span>
      </div>
      <div class="message-bubble message-bubble--student">
        <p>{{ draftMessage }}</p>
      </div>
    </article>

    <div ref="scrollAnchor"></div>

    <template #footer>
      <footer class="assistant-footer">
        <button type="button" class="assistant-footer__left-icon" @click="pickAttachment" aria-label="查看课件"></button>

        <button type="button" class="assistant-footer__input" @click="focusInput">
          <span>{{ inputPlaceholder }}</span>
        </button>

        <button type="button" class="assistant-footer__send" @click="sendMessage" aria-label="发送">
          <span class="assistant-footer__send-icon"></span>
        </button>
      </footer>
    </template>
  </AppMobileFrame>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import { useStudentStore } from '../../stores/studentStore'

const router = useRouter()
const studentStore = useStudentStore()

const currentTime = ref('9:41')
const scrollAnchor = ref(null)
const assistant = studentStore.assistant
const visibleMessages = computed(() => assistant.messages)
const draftMessage = computed({
  get: () => assistant.draftMessage,
  set: (value) => studentStore.appendAssistantDraft(value),
})
const inputPlaceholder = computed({
  get: () => assistant.inputPlaceholder,
  set: (value) => studentStore.setAssistantPlaceholder(value),
})
const studentThinking = computed(() => Boolean(draftMessage.value))

let clockTimer = null

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function scrollToBottom() {
  nextTick(() => {
    scrollAnchor.value?.scrollIntoView({ behavior: 'smooth', block: 'end' })
  })
}

function goBack() {
  router.back()
}

function clearChat() {
  studentStore.clearAssistantChat()
  scrollToBottom()
}

function handleQuickAction(actionKey) {
  if (actionKey === 'course') {
    inputPlaceholder.value = '已为你打开课件入口...'
    return
  }

  if (actionKey === 'knowledge') {
    router.push('/analysis-report')
  }
}

function pickAttachment() {
  inputPlaceholder.value = '查看课件：函数定义域基础'
}

function focusInput() {
  draftMessage.value = '老师，我先试着说一下：分母不能为0，所以 x-1 ≠ 0。'
  scrollToBottom()
}

function sendMessage() {
  const content = draftMessage.value.trim() || '我想先确认一下：是不是要先找分母为 0 的情况？'
  studentStore.sendAssistantMessage(content)
  draftMessage.value = ''
  inputPlaceholder.value = '继续说出你的思考过程...'
  scrollToBottom()
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 30000)
  scrollToBottom()
})

onBeforeUnmount(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
})
</script>

<style scoped>
.assistant-page {
  width: 100%;
}

.assistant-frame {
  max-width: 375px;
  margin: 0 auto;
  min-height: calc(100svh - 48px);
  background: #f5f7fa;
  border-radius: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 10px solid #fff;
  position: relative;
  display: flex;
  flex-direction: column;
}

.assistant-status {
  height: 44px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #333;
  font-size: 0.875rem;
  font-weight: 600;
}

.assistant-status__indicators {
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

.assistant-top-warning {
  margin: 0 10px;
  height: 32px;
  background: #fff9e6;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  color: #d48806;
  font-size: 0.6875rem;
}

.assistant-top-warning__icon {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #d48806;
  flex: 0 0 auto;
}

.assistant-topbar {
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

.assistant-topbar__title {
  margin: 0;
  color: #000;
  font-size: 1.0625rem;
  font-weight: 600;
  text-align: center;
}

.assistant-topbar__action {
  width: 24px;
  height: 24px;
  border: 0;
  background: transparent;
  justify-self: end;
  cursor: pointer;
  display: grid;
  place-items: center;
}

.assistant-topbar__action-icon {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #6c5ce7;
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.75);
}

.assistant-scroll {
  flex: 1 1 auto;
  overflow: auto;
  padding: 14px 20px 12px;
}

.assistant-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.message-row--student {
  justify-content: flex-end;
}

.message-row--student .message-bubble {
  background: #6c5ce7;
  color: #fff;
  border-top-left-radius: 16px;
  border-top-right-radius: 2px;
}

.message-row--assistant .message-bubble {
  background: #fff;
  color: #333;
  border-top-left-radius: 2px;
  border-top-right-radius: 16px;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  font-size: 1rem;
}

.message-avatar--student {
  order: 2;
  background: #ffeaa7;
}

.message-avatar--assistant {
  background: #f0efff;
}

.message-avatar--assistant span {
  color: #6c5ce7;
  font-size: 0.75rem;
  font-weight: 700;
}

.message-avatar--student span {
  font-size: 1rem;
}

.message-bubble {
  max-width: 220px;
  padding: 12px 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: grid;
  gap: 8px;
}

.message-bubble p {
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.5;
}

.message-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.message-actions__button {
  border: 1px solid #dddddd;
  background: #fff;
  border-radius: 12px;
  padding: 6px 10px;
  font-size: 0.75rem;
  color: #999;
  cursor: pointer;
}

.hint-block {
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
}

.hint-block strong {
  color: #999;
  font-size: 0.8125rem;
}

.hint-block p {
  margin: 6px 0 0;
  color: #999;
  font-size: 0.8125rem;
  line-height: 1.5;
}

.assistant-footer {
  height: 59.33px;
  border-top: 1px solid #eeeeee;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 10px;
}

.assistant-footer__left-icon {
  width: 24px;
  height: 24px;
  border: 0;
  background: transparent;
  position: relative;
  cursor: pointer;
}

.assistant-footer__left-icon::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 6px;
  background: #cccccc;
}

.assistant-footer__input {
  flex: 1 1 auto;
  height: 38.67px;
  border: 0;
  border-radius: 20px;
  background: #f5f5f5;
  color: #757575;
  padding: 0 15px;
  text-align: left;
  cursor: text;
}

.assistant-footer__input span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.875rem;
}

.assistant-footer__send {
  width: 20px;
  height: 20px;
  border: 0;
  background: transparent;
  cursor: pointer;
  display: grid;
  place-items: center;
}

.assistant-footer__send-icon {
  width: 14px;
  height: 14px;
  background: #6c5ce7;
  clip-path: polygon(0 50%, 100% 0, 72% 50%, 100% 100%);
}

@media (max-width: 420px) {
  .assistant-frame {
    max-width: 100%;
    border-radius: 0;
    border-width: 0;
  }

  .assistant-top-warning {
    margin-left: 0;
    margin-right: 0;
  }
}
</style>