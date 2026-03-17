<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">ITSM</h1>
        <p class="login-subtitle">IT Service Management System</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="loginId" class="form-label">{{ t('auth.username') }}</label>
          <input
            id="loginId"
            v-model="loginId"
            type="text"
            class="form-input"
            :placeholder="t('auth.usernamePlaceholder')"
            autocomplete="username"
            :disabled="loading"
            required
          />
        </div>

        <div class="form-group">
          <label for="password" class="form-label">{{ t('auth.password') }}</label>
          <input
            id="password"
            v-model="password"
            type="password"
            class="form-input"
            :placeholder="t('auth.passwordPlaceholder')"
            autocomplete="current-password"
            :disabled="loading"
            required
          />
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>{{ t('auth.login') }}</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth.js'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const loginId = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')

async function handleLogin() {
  if (!loginId.value || !password.value) {
    errorMessage.value = t('auth.inputRequired')
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    await authStore.login(loginId.value, password.value)

    if (authStore.mustChangePassword) {
      router.push('/change-password')
    } else {
      router.push('/dashboard')
    }
  } catch (error) {
    const status = error.response?.status
    const code = error.response?.data?.code

    if (status === 423 || code === 'ACCOUNT_LOCKED') {
      errorMessage.value = t('auth.accountLocked')
    } else if (status === 401) {
      errorMessage.value = t('auth.invalidCredentials')
    } else {
      errorMessage.value = t('auth.loginError')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--color-sidebar-bg) 0%, var(--color-sidebar-border) 50%, var(--color-sidebar-bg) 100%);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--color-bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: 2px;
}

.login-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-top: var(--spacing-xs);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.form-label {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text);
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-base);
  transition: border-color 0.2s;
  outline: none;
}

.form-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-focus);
}

.form-input:disabled {
  background-color: var(--color-bg);
  cursor: not-allowed;
}

.error-message {
  padding: 10px 12px;
  background-color: var(--color-notice-error-bg);
  border: 1px solid var(--color-notice-error-border);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  font-size: var(--font-size-sm);
}

.login-btn {
  width: 100%;
  padding: 12px;
  background-color: var(--color-primary);
  color: var(--color-text-inverse);
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-base);
  font-weight: 600;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
}

.login-btn:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-spinner-border);
  border-top-color: var(--color-text-inverse);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
