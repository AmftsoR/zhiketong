import { computed } from 'vue'
import { useUserStore } from '../stores/userStore'

export function useUserInfo() {
  const userStore = useUserStore()
  const userName = computed(() => userStore.profile?.name || '未登录')

  return { userName }
}