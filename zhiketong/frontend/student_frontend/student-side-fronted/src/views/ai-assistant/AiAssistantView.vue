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
        <span v-if="message.streaming && (!message.lines.length || (message.lines.length === 1 && !message.lines[0]))" class="typing-dots typing-dots--inline"><span></span><span></span><span></span></span>

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

    <!-- 流式打字指示器 -->
    <article v-if="isStreaming" class="message-row message-row--assistant">
      <div class="message-avatar message-avatar--assistant">
        <span>AI</span>
      </div>
      <div class="message-bubble message-bubble--assistant message-bubble--typing">
        <span class="typing-dots"><span></span><span></span><span></span></span>
      </div>
    </article>

    <article v-if="studentThinking && !isStreaming" class="message-row message-row--student">
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

        <textarea
          ref="inputRef"
          v-model="draftMessage"
          class="assistant-footer__input"
          :placeholder="inputPlaceholder"
          @keydown.enter.exact.prevent="sendMessage"
          rows="1"
        ></textarea>

        <button type="button" class="assistant-footer__send" :class="{ 'assistant-footer__send--disabled': isStreaming }" @click="sendMessage" :disabled="isStreaming" aria-label="发送">
          <span v-if="!isStreaming" class="assistant-footer__send-icon"></span>
          <span v-else class="assistant-footer__send-spinner"></span>
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
import { streamChat } from '../../api/ai'

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
const isStreaming = computed(() => assistant.streaming)

let clockTimer = null
let activeController = null

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
  router.push('/study')
}

function clearChat() {
  // 取消正在进行的流式请求
  if (activeController) {
    activeController.abort()
    activeController = null
  }
  studentStore.clearAssistantChat()
  scrollToBottom()
}

function handleQuickAction(actionKey) {
  if (actionKey === 'course') {
    inputPlaceholder.value = '请帮我查看相关课件内容'
    sendMessage()
    return
  }

  if (actionKey === 'knowledge') {
    router.push('/analysis-report')
  }
}

function pickAttachment() {
  inputPlaceholder.value = '请帮我查看相关课件'
  draftMessage.value = '请帮我查看相关课件'
  inputRef.value?.focus()
}

function buildHistory() {
  // 从现有消息构建对话历史（排除欢迎消息和流式中的消息）
  const history = []
  for (const msg of assistant.messages) {
    if (msg.streaming || msg.id === 'assistant-welcome') continue
    if (msg.role === 'student' && msg.lines.length > 0) {
      history.push({ role: 'user', content: msg.lines.join('\n') })
    } else if (msg.role === 'assistant' && msg.lines.length > 0) {
      history.push({ role: 'assistant', content: msg.lines.join('\n') })
    }
  }
  return history
}

function sendMessage() {
  if (isStreaming.value) return // 流式进行中不允许发送

  const content = draftMessage.value.trim()
  if (!content) {
    // 没有输入内容时不发送
    return
  }

  // 添加用户消息
  studentStore.addStudentMessage(content)
  draftMessage.value = ''
  inputPlaceholder.value = '继续说出你的思考过程...'
  scrollToBottom()

  // 开始AI流式回复
  studentStore.startAssistantStream()
  scrollToBottom()

  const history = buildHistory()

  activeController = streamChat(content, history, {
    onToken(token) {
      studentStore.appendStreamToken(token)
      scrollToBottom()
    },
    onDone(fullContent) {
      studentStore.finishAssistantStream(fullContent)
      activeController = null
      scrollToBottom()
    },
    onError(err) {
      studentStore.errorAssistantStream(err.message || '未知错误')
      activeController = null
      scrollToBottom()
    },
  })
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
  if (activeController) {
    activeController.abort()
  }
})
</script>

<style scoped>
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
  min-height: 38.67px;
  max-height: 80px;
  border: 0;
  border-radius: 20px;
  background: #f5f5f5;
  color: #333;
  padding: 10px 15px;
  font-size: 0.875rem;
  font-family: inherit;
  line-height: 1.4;
  resize: none;
  outline: none;
  cursor: text;
}

.assistant-footer__input::placeholder {
  color: #999;
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

.message-bubble--typing {
  min-width: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 20px;
}

.typing-dots {
  display: flex;
  gap: 5px;
  align-items: center;
}

.typing-dots--inline {
  display: inline-flex;
  vertical-align: middle;
  margin-left: 2px;
}

.typing-dots span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #6c5ce7;
  animation: typing-bounce 1.4s ease-in-out infinite;
}

.typing-dots span:nth-child(1) { animation-delay: 0s; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing-bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

.assistant-footer__send--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.assistant-footer__send-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid #ddd;
  border-top-color: #6c5ce7;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 420px) {
  .assistant-top-warning {
    margin-left: 0;
    margin-right: 0;
  }
}
</style>