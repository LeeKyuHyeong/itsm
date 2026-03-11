<template>
  <header class="app-header">
    <div class="header-left">
      <button class="sidebar-toggle" @click="$emit('toggleSidebar')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="3" y1="6" x2="21" y2="6" />
          <line x1="3" y1="12" x2="21" y2="12" />
          <line x1="3" y1="18" x2="21" y2="18" />
        </svg>
      </button>
      <span class="header-title">ITSM</span>
    </div>

    <div class="header-right">
      <button class="notification-btn" title="알림">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
      </button>

      <div class="user-info">
        <span class="user-name">{{ user?.name || '사용자' }}</span>
        <span v-if="primaryRole" class="role-badge">{{ primaryRole }}</span>
      </div>

      <button class="logout-btn" @click="handleLogout">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <polyline points="16 17 21 12 16 7" />
          <line x1="21" y1="12" x2="9" y2="12" />
        </svg>
        <span>로그아웃</span>
      </button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'
import { ROLE_LABEL } from '@/constants/roles.js'

defineEmits(['toggleSidebar'])

const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)

const primaryRole = computed(() => {
  const roles = user.value?.roles
  if (!roles || roles.length === 0) return ''
  return ROLE_LABEL[roles[0]] || roles[0]
})

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  height: var(--header-height);
  background-color: var(--color-bg-white);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-md);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  transition: background-color 0.2s;
}

.sidebar-toggle:hover {
  background-color: var(--color-bg);
}

.header-title {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: 1px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.notification-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  transition: background-color 0.2s;
}

.notification-btn:hover {
  background-color: var(--color-bg);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-name {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text);
}

.role-badge {
  display: inline-block;
  padding: 2px 8px;
  background-color: #e8f0fe;
  color: var(--color-primary);
  border-radius: 12px;
  font-size: var(--font-size-xs);
  font-weight: 500;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  background: none;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  transition: all 0.2s;
}

.logout-btn:hover {
  background-color: #fef2f2;
  border-color: var(--color-danger);
  color: var(--color-danger);
}
</style>
