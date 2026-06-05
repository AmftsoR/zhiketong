import { ref } from 'vue'

export function useCounter(initialValue = 0) {
  const count = ref(initialValue)
  const increase = () => {
    count.value += 1
  }

  return { count, increase }
}