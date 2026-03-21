import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth.js'

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
})
