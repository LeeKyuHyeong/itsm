import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth.js'

vi.mock('@/api/auth.js', () => ({
  authApi: {
    login: vi.fn(),
    logout: vi.fn(),
    getMe: vi.fn()
  }
}))

import { authApi } from '@/api/auth.js'

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.restoreAllMocks()
  })

  it('initial state has null user and not authenticated', () => {
    const store = useAuthStore()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('setUser sets user and updates isAuthenticated', () => {
    const store = useAuthStore()
    const user = { id: 1, loginId: 'admin', name: 'Admin' }
    store.setUser(user)
    expect(store.user).toEqual(user)
    expect(store.isAuthenticated).toBe(true)
  })

  it('clearAuth removes user', () => {
    const store = useAuthStore()
    store.setUser({ id: 1 })
    store.clearAuth()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('hasRole checks if user has the given role', () => {
    const store = useAuthStore()
    store.setUser({ id: 1, roles: ['ITSM_ADMIN', 'PM'] })
    expect(store.hasRole('ITSM_ADMIN')).toBe(true)
    expect(store.hasRole('SUPER_ADMIN')).toBe(false)
  })

  it('hasAnyRole checks if user has any of the given roles', () => {
    const store = useAuthStore()
    store.setUser({ id: 1, roles: ['DEVELOPER'] })
    expect(store.hasAnyRole(['DEVELOPER', 'PM'])).toBe(true)
    expect(store.hasAnyRole(['SUPER_ADMIN', 'ITSM_ADMIN'])).toBe(false)
  })

  it('hasRole returns false when user has no roles array', () => {
    const store = useAuthStore()
    store.setUser({ id: 1 })
    expect(store.hasRole('ITSM_ADMIN')).toBe(false)
  })

  it('hasRole returns false when user is null', () => {
    const store = useAuthStore()
    expect(store.hasRole('ITSM_ADMIN')).toBe(false)
  })

  it('mustChangePassword reflects user.mustChangePassword flag', () => {
    const store = useAuthStore()
    store.setUser({ id: 1, mustChangePassword: true })
    expect(store.mustChangePassword).toBe(true)

    store.setUser({ id: 1, mustChangePassword: false })
    expect(store.mustChangePassword).toBe(false)
  })

  it('mustChangePassword is false when user is null', () => {
    const store = useAuthStore()
    expect(store.mustChangePassword).toBe(false)
  })

  it('login calls API and fetches user info', async () => {
    const store = useAuthStore()
    authApi.login.mockResolvedValue({ data: { data: { userId: 1 } } })
    authApi.getMe.mockResolvedValue({
      data: { data: { userId: 1, loginId: 'admin', roles: ['ADMIN'] } }
    })

    const result = await store.login('admin', 'pwd')

    expect(authApi.login).toHaveBeenCalledWith({ loginId: 'admin', password: 'pwd' })
    expect(authApi.getMe).toHaveBeenCalled()
    expect(store.user).toEqual({ userId: 1, loginId: 'admin', roles: ['ADMIN'] })
    expect(store.isAuthenticated).toBe(true)
    expect(result).toEqual({ userId: 1 })
  })

  it('logout clears user even if API call fails', async () => {
    const store = useAuthStore()
    store.setUser({ id: 1, loginId: 'admin' })
    authApi.logout.mockRejectedValue(new Error('Network error'))

    await store.logout()

    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('logout calls API and clears user on success', async () => {
    const store = useAuthStore()
    store.setUser({ id: 1, loginId: 'admin' })
    authApi.logout.mockResolvedValue({})

    await store.logout()

    expect(authApi.logout).toHaveBeenCalled()
    expect(store.user).toBeNull()
  })

  it('fetchMe loads current user info from API', async () => {
    const store = useAuthStore()
    authApi.getMe.mockResolvedValue({
      data: { data: { userId: 7, loginId: 'tester', roles: ['USER'] } }
    })

    const result = await store.fetchMe()

    expect(authApi.getMe).toHaveBeenCalled()
    expect(store.user).toEqual({ userId: 7, loginId: 'tester', roles: ['USER'] })
    expect(result).toEqual({ userId: 7, loginId: 'tester', roles: ['USER'] })
  })
})
