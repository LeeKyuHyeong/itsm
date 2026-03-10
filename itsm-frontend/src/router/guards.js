import { useAuthStore } from '../stores/auth.js'

export function setupGuards(router) {
  router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()
    const requiresAuth = to.meta.requiresAuth !== false

    // 인증이 필요하지 않은 페이지 (로그인 등)
    if (!requiresAuth) {
      // 이미 로그인한 사용자는 대시보드로
      if (authStore.isAuthenticated && to.name === 'Login') {
        return next({ path: '/dashboard' })
      }
      return next()
    }

    // 인증 필요한데 미인증
    if (!authStore.isAuthenticated) {
      return next({ path: '/login' })
    }

    // 역할 기반 접근 제어
    const requiredRoles = to.meta.roles
    if (requiredRoles && requiredRoles.length > 0) {
      if (!authStore.hasAnyRole(requiredRoles)) {
        return next({ path: '/dashboard' })
      }
    }

    next()
  })
}
