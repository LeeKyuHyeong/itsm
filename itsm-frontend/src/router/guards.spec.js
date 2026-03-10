import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { setupGuards } from './guards.js'
import { useAuthStore } from '../stores/auth.js'
import { createRouter, createWebHistory } from 'vue-router'

const DummyComponent = { template: '<div>dummy</div>' }

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/login', name: 'Login', component: DummyComponent, meta: { requiresAuth: false } },
      { path: '/dashboard', name: 'Dashboard', component: DummyComponent, meta: { requiresAuth: true } },
      { path: '/admin/users', name: 'AdminUsers', component: DummyComponent, meta: { requiresAuth: true, roles: ['SUPER_ADMIN', 'ITSM_ADMIN'] } }
    ]
  })
}

describe('navigation guards', () => {
  let router, authStore

  beforeEach(() => {
    setActivePinia(createPinia())
    router = createTestRouter()
    setupGuards(router)
    authStore = useAuthStore()
  })

  it('redirects to /login when not authenticated and route requires auth', async () => {
    router.push('/dashboard')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('allows access to login page when not authenticated', async () => {
    router.push('/login')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('redirects authenticated user from login to /dashboard', async () => {
    authStore.setUser({ id: 1, roles: ['DEVELOPER'] })
    authStore.setToken('token')
    router.push('/login')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('allows authenticated user to access protected route', async () => {
    authStore.setUser({ id: 1, roles: ['DEVELOPER'] })
    authStore.setToken('token')
    router.push('/dashboard')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('redirects to /dashboard when user lacks required role', async () => {
    authStore.setUser({ id: 1, roles: ['DEVELOPER'] })
    authStore.setToken('token')
    router.push('/admin/users')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('allows access when user has required role', async () => {
    authStore.setUser({ id: 1, roles: ['ITSM_ADMIN'] })
    authStore.setToken('token')
    router.push('/admin/users')
    await router.isReady()
    expect(router.currentRoute.value.path).toBe('/admin/users')
  })
})
