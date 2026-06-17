<template>
  <AppMobileFrame time="9:41" frame-class="login-frame" scroll-class="login-scroll">
    <div class="login-body">
      <div class="login-header">
        <div class="login-logo">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="14" fill="url(#logo-grad)"/>
            <path d="M14 24L21 31L34 18" stroke="#fff" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="logo-grad" x1="0" y1="0" x2="48" y2="48">
                <stop offset="0%" stop-color="#6C5CE7"/>
                <stop offset="100%" stop-color="#a29bfe"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="login-title">智课通</h1>
        <p class="login-subtitle">AI 驱动的智能学习平台</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="input-group">
          <label class="input-label" for="username">用户名</label>
          <input
            id="username"
            v-model.trim="form.username"
            type="text"
            class="input-field"
            placeholder="请输入用户名"
            autocomplete="username"
            :disabled="submitting"
          />
        </div>

        <div class="input-group">
          <label class="input-label" for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="input-field"
            placeholder="请输入密码"
            autocomplete="current-password"
            :disabled="submitting"
          />
        </div>

        <p v-if="errorMsg" class="login-error">{{ errorMsg }}</p>

        <button type="submit" class="login-btn" :disabled="submitting">
          <span v-if="submitting" class="login-btn__spinner"></span>
          <span v-else>登 录</span>
        </button>
      </form>

      <p class="login-hint">测试账号：test_student / 123456</p>
    </div>
  </AppMobileFrame>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import { useUserStore } from '../../stores/userStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({ username: '', password: '' })
const errorMsg = ref('')
const submitting = ref(false)

async function handleLogin() {
  errorMsg.value = ''

  if (!form.username) {
    errorMsg.value = '请输入用户名'
    return
  }
  if (!form.password) {
    errorMsg.value = '请输入密码'
    return
  }

  submitting.value = true
  try {
    const result = await userStore.login(form.username, form.password)
    if (result.ok) {
    router.replace('/home')
    } else {
      errorMsg.value = result.error || '用户名或密码错误'
    }
  } catch {
    errorMsg.value = '登录失败，请检查网络连接'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-scroll {
  overflow: auto;
}

.login-scroll::-webkit-scrollbar {
  width: 0; height: 0;
}

.login-body {
  padding: 48px 28px 0;
  display: grid;
  gap: 0;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-logo {
  display: inline-flex;
  margin-bottom: 16px;
  filter: drop-shadow(0 4px 12px rgba(108, 92, 231, 0.25));
}

.login-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a2e;
}

.login-subtitle {
  margin: 6px 0 0;
  font-size: 0.8125rem;
  color: #999;
}

.login-form {
  display: grid;
  gap: 18px;
}

.input-group {
  display: grid;
  gap: 6px;
}

.input-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #555;
}

.input-field {
  width: 100%;
  height: 44px;
  border: 1.5px solid #e8e8e8;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 0.9375rem;
  color: #333;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #fafafa;
}

.input-field:focus {
  border-color: #6c5ce7;
  box-shadow: 0 0 0 3px rgba(108, 92, 231, 0.1);
  background: #fff;
}

.input-field:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-error {
  margin: -6px 0 0;
  padding: 8px 12px;
  border-radius: 8px;
  background: #fff5f5;
  color: #ff7675;
  font-size: 0.8125rem;
  text-align: center;
}

.login-btn {
  width: 100%;
  height: 46px;
  margin-top: 4px;
  border: 0;
  border-radius: 10px;
  background: linear-gradient(135deg, #6c5ce7, #a29bfe);
  color: #fff;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: opacity 0.2s, transform 0.15s;
  box-shadow: 0 4px 14px rgba(108, 92, 231, 0.3);
}

.login-btn:hover {
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.login-btn__spinner {
  width: 20px;
  height: 20px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.login-hint {
  margin: 24px 0 0;
  text-align: center;
  color: #ccc;
  font-size: 0.75rem;
}

@media (max-width: 420px) {
  .login-body {
    padding: 32px 20px 0;
  }
}
</style>
