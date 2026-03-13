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
      <button class="theme-toggle" :title="isDark ? 'Light Mode' : 'Dark Mode'" @click="toggleTheme">
        <svg v-if="isDark" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="5"/>
          <line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/>
          <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
          <line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/>
          <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
        </svg>
        <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
        </svg>
      </button>

      <div class="locale-switcher">
        <button
          v-for="loc in locales"
          :key="loc.code"
          class="locale-btn"
          :class="{ active: currentLocale === loc.code }"
          @click="switchLocale(loc.code)"
        >{{ loc.label }}</button>
      </div>

      <button class="notification-btn" :title="t('notification.title')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
      </button>

      <div class="user-info">
        <span class="user-name">{{ user?.name || t('common.noData') }}</span>
        <span v-if="primaryRole" class="role-badge">{{ primaryRole }}</span>
      </div>

      <button class="logout-btn" @click="handleLogout">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <polyline points="16 17 21 12 16 7" />
          <line x1="21" y1="12" x2="9" y2="12" />
        </svg>
        <span>{{ t('auth.logout') }}</span>
      </button>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth.js'
import { ROLE_LABEL } from '@/constants/roles.js'
import { setLocale } from '@/i18n/index.js'

const { t, locale: currentLocale } = useI18n()

const isDark = ref(false)

onMounted(() => {
  const saved = localStorage.getItem('itsm-theme') || 'light'
  isDark.value = saved === 'dark'
  applyTheme(saved)
})

function toggleTheme() {
  isDark.value = !isDark.value
  const theme = isDark.value ? 'dark' : 'light'
  applyTheme(theme)
  localStorage.setItem('itsm-theme', theme)
}

function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme === 'dark' ? 'dark' : '')
}

defineEmits(['toggleSidebar'])

const router = useRouter()
const authStore = useAuthStore()

const locales = [
  { code: 'ko', label: 'KO' },
  { code: 'en', label: 'EN' }
]

const user = computed(() => authStore.user)

const primaryRole = computed(() => {
  const roles = user.value?.roles
  if (!roles || roles.length === 0) return ''
  return ROLE_LABEL[roles[0]] || roles[0]
})

function switchLocale(code) {
  setLocale(code)
}

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
  background-color: var(--color-primary-bg);
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
  background-color: var(--color-logout-hover);
  border-color: var(--color-danger);
  color: var(--color-danger);
}

.locale-switcher {
  display: flex;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.locale-btn {
  padding: 4px 10px;
  border: none;
  background: none;
  font-size: var(--font-size-xs);
  font-weight: 600;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.locale-btn + .locale-btn {
  border-left: 1px solid var(--color-border);
}

.locale-btn.active {
  background-color: var(--color-primary);
  color: var(--color-text-inverse);
}

.locale-btn:hover:not(.active) {
  background-color: var(--color-bg);
}

.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: background-color 0.2s;
}

.theme-toggle:hover {
  background-color: var(--color-bg);
}
</style>
