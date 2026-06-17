import { defineStore } from 'pinia'
import { login as loginApi, fetchCurrentUser } from '../api/auth'
import { saveToken, clearToken } from '../utils/request'
import { useStudentStore } from './studentStore'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('zhiketong_token') || '',
    user: null,
    loading: false,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token && !!state.user,
    userName: (state) => state.user?.realName || state.user?.username || '',
    userInfo: (state) => state.user || {},
  },

  actions: {
    /** 登录，成功返回 true，失败返回后端错误消息 */
    async login(username, password) {
      this.loading = true
      try {
        const res = await loginApi({ username, password })
        if (res.code === 200 && res.data) {
          this.token = res.data.token
          this.user = res.data.user
          saveToken(res.data.token)
          // 同步到学生 profile
          const studentStore = useStudentStore()
          studentStore.syncUserProfile(res.data.user)
          return { ok: true }
        }
        // 后端返回了业务错误（code !== 200）
        return { ok: false, error: res.message || '登录失败' }
      } catch (e) {
        console.error('登录请求异常:', e)
        return { ok: false, error: '网络连接失败，请检查后端服务是否启动' }
      } finally {
        this.loading = false
      }
    },

    /** 尝试用本地 token 恢复会话 */
    async restoreSession() {
      if (!this.token) return false
      try {
        const res = await fetchCurrentUser()
        if (res.code === 200 && res.data) {
          this.user = res.data
          // 同步到学生 profile（res.data 就是用户对象）
          const studentStore = useStudentStore()
          studentStore.syncUserProfile(res.data)
          return true
        }
        this.logout()
        return false
      } catch {
        this.logout()
        return false
      }
    },

    /** 登出 */
    logout() {
      this.token = ''
      this.user = null
      clearToken()
    },
  },
})