<template>
  <div class="login-screen">
    <div class="login-panel">
      <div class="login-brand">
        <div class="brand-mark">智</div>
        <div class="brand-text">
          <h1 class="brand-name">智课通教师端</h1>
          <p class="brand-desc">AI 驱动的精准教学管理平台</p>
        </div>
      </div>

      <div class="login-card">
        <h2 class="card-title">教师登录</h2>

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

        <p class="login-hint">测试账号：test_teacher / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
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
      const redirect = route.query.redirect || '/overview'
      router.push(redirect)
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
.login-screen {
  min-height: 100vh;
  min-height: 100dvh;
  display: grid;
  place-items: center;
  padding: 32px;
  background: radial-gradient(circle at 30% 0%, #ffffff 0%, #f6f8fd 45%, #edf2f8 100%);
}

.login-panel {
  width: 100%;
  max-width: 420px;
}

/* ===== 品牌区域 ===== */
.login-brand {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 36px;
  justify-content: center;
}

.brand-mark {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #243b78, #3b5cb8);
  color: #fff;
  font-size: 1.5rem;
  font-weight: 700;
  display: grid;
  place-items: center;
  box-shadow: 0 8px 24px rgba(36, 59, 120, 0.3);
  flex-shrink: 0;
}

.brand-text {
  text-align: left;
}

.brand-name {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #243b78;
}

.brand-desc {
  margin: 4px 0 0;
  font-size: 0.8125rem;
  color: #64748b;
}

/* ===== 登录卡片 ===== */
.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 36px 32px 28px;
  box-shadow: 0 8px 32px rgba(36, 59, 120, 0.08);
}

.card-title {
  margin: 0 0 28px;
  font-size: 1.125rem;
  font-weight: 700;
  color: #0f172a;
  text-align: center;
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
  color: #334155;
}

.input-field {
  width: 100%;
  height: 44px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 0.9375rem;
  color: #0f172a;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #f8fafc;
}

.input-field:focus {
  border-color: #243b78;
  box-shadow: 0 0 0 3px rgba(36, 59, 120, 0.08);
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
  background: #fef2f2;
  color: #dc2626;
  font-size: 0.8125rem;
  text-align: center;
}

.login-btn {
  width: 100%;
  height: 46px;
  margin-top: 4px;
  border: 0;
  border-radius: 10px;
  background: linear-gradient(135deg, #243b78, #3b5cb8);
  color: #fff;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: opacity 0.2s, transform 0.15s;
  box-shadow: 0 4px 16px rgba(36, 59, 120, 0.3);
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
  margin: 20px 0 0;
  text-align: center;
  color: #94a3b8;
  font-size: 0.75rem;
}

@media (max-width: 480px) {
  .login-screen {
    padding: 16px;
    align-items: flex-start;
    padding-top: 60px;
  }

  .login-card {
    padding: 28px 20px 24px;
  }

  .login-brand {
    margin-bottom: 24px;
  }
}
</style>
