import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth.js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)

  const isAuthenticated = computed(() => user.value !== null)
  const mustChangePassword = computed(() => user.value?.mustChangePassword ?? false)

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

  async function login(loginId, password) {
    const { data } = await authApi.login({ loginId, password })
    const result = data.data
    setToken(result.accessToken)
    const meInfo = await authApi.getMe()
    setUser(meInfo.data.data)
    return result
  }

  async function logout() {
    try { await authApi.logout() } catch (e) { /* ignore */ }
    clearAuth()
  }

  async function fetchMe() {
    const { data } = await authApi.getMe()
    setUser(data.data)
    return data.data
  }

  return {
    user,
    isAuthenticated,
    mustChangePassword,
    setToken,
    setUser,
    clearAuth,
    hasRole,
    hasAnyRole,
    login,
    logout,
    fetchMe
  }
})
