<template>
  <AppMobileFrame :time="currentTime" frame-class="settings-frame" scroll-class="settings-scroll">
    <template #topbar>
      <div class="settings-topbar">
        <button type="button" class="icon-button" @click="goBack" aria-label="返回">
          <span class="icon-button__arrow"></span>
        </button>
        <h1 class="settings-topbar__title">设置</h1>
        <span></span>
      </div>
    </template>

    <!-- 个人资料编辑 -->
    <section class="settings-card page-card">
      <h2 class="settings-card__title">个人资料</h2>
      <div class="form-group">
        <label>姓名</label>
        <input
          v-model="profileForm.realName"
          class="form-input"
          placeholder="请输入姓名"
        />
      </div>
      <button type="button" class="btn-save" :disabled="profileSaving" @click="saveProfile">
        {{ profileSaving ? '保存中...' : '保存' }}
      </button>
      <p v-if="profileMsg" class="form-msg" :class="profileOk ? 'form-msg--ok' : 'form-msg--err'">{{ profileMsg }}</p>
    </section>

    <!-- 修改密码 -->
    <section class="settings-card page-card">
      <h2 class="settings-card__title">修改密码</h2>
      <div class="form-group">
        <label>旧密码</label>
        <input
          v-model="pwdForm.oldPassword"
          class="form-input"
          type="password"
          placeholder="请输入旧密码"
        />
      </div>
      <div class="form-group">
        <label>新密码</label>
        <input
          v-model="pwdForm.newPassword"
          class="form-input"
          type="password"
          placeholder="请输入新密码（至少6位）"
        />
      </div>
      <div class="form-group">
        <label>确认新密码</label>
        <input
          v-model="pwdForm.confirmPassword"
          class="form-input"
          type="password"
          placeholder="请再次输入新密码"
        />
      </div>
      <button type="button" class="btn-save" :disabled="pwdSaving" @click="changePassword">
        {{ pwdSaving ? '提交中...' : '修改密码' }}
      </button>
      <p v-if="pwdMsg" class="form-msg" :class="pwdOk ? 'form-msg--ok' : 'form-msg--err'">{{ pwdMsg }}</p>
    </section>

    <!-- 通用设置 -->
    <section class="settings-card page-card">
      <h2 class="settings-card__title">通用</h2>
      <div class="general-row">
        <span>清除缓存</span>
        <button type="button" class="btn-action" @click="clearCache">清除</button>
      </div>
      <div class="general-row">
        <span>版本信息</span>
        <span class="general-row__value">v1.0.0</span>
      </div>
    </section>

    <template #footer>
      <BottomNav :activeTab="'profile'" />
    </template>
  </AppMobileFrame>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import BottomNav from '../../components/layout/BottomNav.vue'
import { useStudentStore } from '../../stores/studentStore'
import { request } from '../../utils/request'

const router = useRouter()
const studentStore = useStudentStore()

const currentTime = ref('9:41')

// 个人资料表单
const profileForm = ref({ realName: studentStore.profile.name })
const profileSaving = ref(false)
const profileMsg = ref('')
const profileOk = ref(false)

// 密码表单
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdSaving = ref(false)
const pwdMsg = ref('')
const pwdOk = ref(false)

let clockTimer = null

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function goBack() {
  router.push('/profile')
}

async function saveProfile() {
  if (!profileForm.value.realName.trim()) {
    profileMsg.value = '姓名不能为空'
    profileOk.value = false
    return
  }
  profileSaving.value = true
  profileMsg.value = ''
  try {
    const res = await request('/user/profile', {
      method: 'PUT',
      body: { realName: profileForm.value.realName.trim() },
    })
    if (res && res.code === 200) {
      profileMsg.value = '资料更新成功'
      profileOk.value = true
      studentStore.profile.name = profileForm.value.realName.trim()
    } else {
      profileMsg.value = res?.message || '更新失败'
      profileOk.value = false
    }
  } catch (e) {
    profileMsg.value = '网络错误，请稍后重试'
    profileOk.value = false
  } finally {
    profileSaving.value = false
  }
}

async function changePassword() {
  pwdMsg.value = ''
  pwdOk.value = false

  if (!pwdForm.value.oldPassword) {
    pwdMsg.value = '请输入旧密码'
    return
  }
  if (!pwdForm.value.newPassword || pwdForm.value.newPassword.length < 6) {
    pwdMsg.value = '新密码长度不能少于6位'
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    pwdMsg.value = '两次输入的新密码不一致'
    return
  }

  pwdSaving.value = true
  try {
    const res = await request('/user/password', {
      method: 'PUT',
      body: {
        oldPassword: pwdForm.value.oldPassword,
        newPassword: pwdForm.value.newPassword,
      },
    })
    if (res && res.code === 200) {
      pwdMsg.value = '密码修改成功'
      pwdOk.value = true
      pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    } else {
      pwdMsg.value = res?.message || '密码修改失败'
    }
  } catch (e) {
    pwdMsg.value = '网络错误，请稍后重试'
  } finally {
    pwdSaving.value = false
  }
}

function clearCache() {
  try {
    localStorage.removeItem('zhiketong_token')
    profileMsg.value = ''
    pwdMsg.value = ''
    // 非破坏性：只清除可重建的缓存
    alert('缓存已清除')
  } catch (e) {
    alert('清除缓存失败')
  }
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 30000)
})

onBeforeUnmount(() => {
  if (clockTimer) window.clearInterval(clockTimer)
})
</script>

<style scoped>
.settings-topbar {
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

.settings-topbar__title {
  margin: 0;
  color: #000;
  font-size: 1.0625rem;
  font-weight: 600;
  text-align: center;
}

.settings-scroll {
  overflow: auto;
  padding: 0 0 16px;
}

.settings-scroll::-webkit-scrollbar {
  width: 0; height: 0;
}

.settings-card {
  width: calc(100% - 40px);
  margin: 0 20px 12px;
  padding: 20px;
}

.settings-card__title {
  margin: 0 0 16px;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #333;
}

.form-group {
  margin-bottom: 12px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 0.8125rem;
  color: #555;
  font-weight: 600;
}

.form-input {
  width: 100%;
  height: 38px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 0.875rem;
  color: #333;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.15s;
}

.form-input:focus {
  border-color: #6c5ce7;
}

.btn-save {
  width: 100%;
  height: 40px;
  border: 0;
  border-radius: 8px;
  background: #6c5ce7;
  color: #fff;
  font-size: 0.9375rem;
  cursor: pointer;
  margin-top: 4px;
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-msg {
  margin: 10px 0 0;
  font-size: 0.8125rem;
  text-align: center;
}

.form-msg--ok { color: #00b894; }
.form-msg--err { color: #ff7675; }

/* 通用设置 */
.general-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 0.875rem;
  color: #333;
}

.general-row:last-child {
  border-bottom: 0;
}

.general-row__value {
  color: #999;
  font-size: 0.8125rem;
}

.btn-action {
  min-width: 56px;
  height: 28px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-size: 0.8125rem;
  cursor: pointer;
}

@media (max-width: 420px) {
  .settings-card {
    width: calc(100% - 24px);
    margin-left: 12px;
    margin-right: 12px;
  }
}
</style>
