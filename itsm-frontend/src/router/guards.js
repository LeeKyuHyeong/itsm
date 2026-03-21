import { useAuthStore } from '../stores/auth.js'

let sessionRestoring = false

export function setupGuards(router) {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()
    const requiresAuth = to.meta.requiresAuth !== false

    // user 상태가 없으면 세션 복원 시도 (새로고침 대응 — 쿠키 기반이므로 서버에 직접 확인)
    if (!authStore.isAuthenticated && !sessionRestoring) {
      sessionRestoring = true
      try {
        await authStore.fetchMe()
      } catch (e) {
        authStore.clearAuth()
      } finally {
        sessionRestoring = false
      }
    }

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

    // 비밀번호 변경 필수 확인
    if (authStore.mustChangePassword && to.name !== 'ChangePassword') {
      return next({ path: '/change-password' })
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
