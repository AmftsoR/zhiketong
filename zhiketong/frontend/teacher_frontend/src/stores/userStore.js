import { defineStore } from 'pinia'
import { login as loginApi, fetchCurrentUser } from '../api/auth'
import { saveToken, clearToken } from '../utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('zhiketong_teacher_token') || '',
    user: null,
    loading: false,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token && !!state.user,
    userName: (state) => state.user?.realName || state.user?.username || '',
    userInfo: (state) => state.user || {},
  },

  actions: {
    async login(username, password) {
      this.loading = true
      try {
        const res = await loginApi({ username, password })
        if (res.code === 200 && res.data) {
          this.token = res.data.token
          this.user = res.data.user
          saveToken(res.data.token)
          return { ok: true }
        }
        return { ok: false, error: res.message || '登录失败' }
      } catch (e) {
        console.error('登录请求异常:', e)
        return { ok: false, error: '网络连接失败，请检查后端服务是否启动' }
      } finally {
        this.loading = false
      }
    },

    async restoreSession() {
      if (!this.token) return false
      try {
        const res = await fetchCurrentUser()
        if (res.code === 200 && res.data) {
          this.user = res.data
          return true
        }
        this.logout()
        return false
      } catch {
        this.logout()
        return false
      }
    },

    logout() {
      this.token = ''
      this.user = null
      clearToken()
    },
  },
})
