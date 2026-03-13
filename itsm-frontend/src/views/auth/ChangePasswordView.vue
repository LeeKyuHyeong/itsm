<template>
  <div class="change-password-page">
    <div class="change-password-card">
      <div class="card-header">
        <h1 class="card-title">{{ t('auth.changePassword') }}</h1>
        <p class="card-subtitle">{{ t('auth.changePasswordDesc') }}</p>
      </div>

      <form class="password-form" @submit.prevent="handleChangePassword">
        <div class="form-group">
          <label for="currentPassword" class="form-label">{{ t('auth.currentPassword') }}</label>
          <input
            id="currentPassword"
            v-model="form.currentPassword"
            type="password"
            class="form-input"
            :placeholder="t('auth.currentPasswordPlaceholder')"
            :disabled="loading"
            required
          />
        </div>

        <div class="form-group">
          <label for="newPassword" class="form-label">{{ t('auth.newPassword') }}</label>
          <input
            id="newPassword"
            v-model="form.newPassword"
            type="password"
            class="form-input"
            :class="{ 'input-error': form.newPassword && !isNewPasswordValid }"
            :placeholder="t('auth.newPasswordPlaceholder')"
            :disabled="loading"
            required
          />
          <ul class="password-hints">
            <li :class="{ valid: hasMinLength }">{{ t('auth.minLength') }}</li>
            <li :class="{ valid: hasUppercase }">{{ t('auth.hasUppercase') }}</li>
            <li :class="{ valid: hasLowercase }">{{ t('auth.hasLowercase') }}</li>
            <li :class="{ valid: hasDigit }">{{ t('auth.hasDigit') }}</li>
            <li :class="{ valid: hasSpecialChar }">{{ t('auth.hasSpecial') }}</li>
          </ul>
        </div>

        <div class="form-group">
          <label for="confirmPassword" class="form-label">{{ t('auth.confirmPassword') }}</label>
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            type="password"
            class="form-input"
            :class="{ 'input-error': form.confirmPassword && !isConfirmMatch }"
            :placeholder="t('auth.confirmPasswordPlaceholder')"
            :disabled="loading"
            required
          />
          <p v-if="form.confirmPassword && !isConfirmMatch" class="hint-error">
            {{ t('auth.passwordMismatch') }}
          </p>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="success-message">
          {{ successMessage }}
        </div>

        <button
          type="submit"
          class="submit-btn"
          :disabled="loading || !isFormValid"
        >
          <span v-if="loading" class="spinner"></span>
          <span v-else>{{ t('auth.changePassword') }}</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { authApi } from '@/api/auth.js'
import { useAuthStore } from '@/stores/auth.js'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const hasMinLength = computed(() => form.newPassword.length >= 8)
const hasUppercase = computed(() => /[A-Z]/.test(form.newPassword))
const hasLowercase = computed(() => /[a-z]/.test(form.newPassword))
const hasDigit = computed(() => /[0-9]/.test(form.newPassword))
const hasSpecialChar = computed(() => /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(form.newPassword))

const isNewPasswordValid = computed(() =>
  hasMinLength.value && hasUppercase.value && hasLowercase.value && hasDigit.value && hasSpecialChar.value
)

const isConfirmMatch = computed(() => form.newPassword === form.confirmPassword)

const isFormValid = computed(() =>
  form.currentPassword && isNewPasswordValid.value && isConfirmMatch.value
)

async function handleChangePassword() {
  if (!isFormValid.value) return

  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    await authApi.changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })

    successMessage.value = t('auth.passwordChanged')

    // 사용자 정보 갱신
    await authStore.fetchMe()

    setTimeout(() => {
      router.push('/dashboard')
    }, 1500)
  } catch (error) {
    const message = error.response?.data?.message
    if (message) {
      errorMessage.value = message
    } else {
      errorMessage.value = t('auth.passwordChangeError')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.change-password-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--color-sidebar-bg) 0%, var(--color-sidebar-border) 50%, var(--color-sidebar-bg) 100%);
}

.change-password-card {
  width: 100%;
  max-width: 440px;
  background: var(--color-bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 40px;
}

.card-header {
  text-align: center;
  margin-bottom: 28px;
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
}

.card-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-top: var(--spacing-xs);
}

.password-form {
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

.form-input.input-error {
  border-color: var(--color-danger);
}

.form-input:disabled {
  background-color: var(--color-bg);
  cursor: not-allowed;
}

.password-hints {
  list-style: none;
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  padding: 0;
  margin: 0;
}

.password-hints li {
  font-size: var(--font-size-xs);
  color: var(--color-text-disabled);
  position: relative;
  padding-left: 16px;
}

.password-hints li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1.5px solid var(--color-text-disabled);
}

.password-hints li.valid {
  color: var(--color-success);
}

.password-hints li.valid::before {
  background-color: var(--color-success);
  border-color: var(--color-success);
}

.hint-error {
  font-size: var(--font-size-xs);
  color: var(--color-danger);
}

.error-message {
  padding: 10px 12px;
  background-color: var(--color-notice-error-bg);
  border: 1px solid var(--color-notice-error-border);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  font-size: var(--font-size-sm);
}

.success-message {
  padding: 10px 12px;
  background-color: var(--color-notice-success-bg);
  border: 1px solid var(--color-notice-success-border);
  border-radius: var(--radius-sm);
  color: var(--color-success);
  font-size: var(--font-size-sm);
}

.submit-btn {
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

.submit-btn:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.submit-btn:disabled {
  opacity: 0.5;
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
