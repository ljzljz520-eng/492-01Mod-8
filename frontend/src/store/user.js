import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!user.value)
  const userRole = computed(() => user.value?.role || '')
  const userName = computed(() => user.value?.nickname || user.value?.username || '')
  const userId = computed(() => user.value?.id || null)

  const setUser = (userData) => {
    user.value = userData
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData))
    } else {
      localStorage.removeItem('user')
    }
  }

  const logout = () => {
    setUser(null)
  }

  return { user, isLoggedIn, userRole, userName, userId, setUser, logout }
})
