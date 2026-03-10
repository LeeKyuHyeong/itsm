import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)

  const isAuthenticated = computed(() => user.value !== null)

  function setToken(token) {
    localStorage.setItem('accessToken', token)
  }

  function setUser(userData) {
    user.value = userData
  }

  function clearAuth() {
    user.value = null
    localStorage.removeItem('accessToken')
  }

  function hasRole(role) {
    return user.value?.roles?.includes(role) ?? false
  }

  function hasAnyRole(roles) {
    return roles.some(role => hasRole(role))
  }

  return {
    user,
    isAuthenticated,
    setToken,
    setUser,
    clearAuth,
    hasRole,
    hasAnyRole
  }
})
