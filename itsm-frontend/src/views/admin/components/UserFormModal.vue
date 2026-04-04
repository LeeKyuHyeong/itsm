<template>
  <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-card">
      <div class="modal-header">
        <h2 class="modal-title">{{ isEditing ? t('admin.userEdit') : t('admin.userAdd') }}</h2>
        <button class="modal-close" @click="$emit('close')">&times;</button>
      </div>
      <form class="modal-body" @submit.prevent="$emit('save')">
        <div class="form-group">
          <label class="form-label">{{ t('admin.loginId') }}</label>
          <input
            :value="form.loginId"
            type="text"
            class="form-input"
            :disabled="isEditing"
            required
            @input="$emit('update:form', { ...form, loginId: $event.target.value })"
          />
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('admin.name') }}</label>
          <input
            :value="form.name"
            type="text"
            class="form-input"
            required
            @input="$emit('update:form', { ...form, name: $event.target.value })"
          />
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('admin.email') }}</label>
          <input
            :value="form.email"
            type="email"
            class="form-input"
            required
            @input="$emit('update:form', { ...form, email: $event.target.value })"
          />
        </div>
        <div v-if="!isEditing" class="form-group">
          <label class="form-label">{{ t('admin.password') }}</label>
          <input
            :value="form.password"
            type="password"
            class="form-input"
            required
            @input="$emit('update:form', { ...form, password: $event.target.value })"
          />
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('admin.phone') }}</label>
          <input
            :value="form.phone"
            type="text"
            class="form-input"
            @input="$emit('update:form', { ...form, phone: $event.target.value })"
          />
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('admin.company') }}</label>
          <select
            :value="form.companyId"
            class="form-input"
            @change="onCompanyChange($event)"
          >
            <option value="">{{ t('common.select') }}</option>
            <option v-for="c in companies" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">{{ t('admin.department') }}</label>
          <select
            :value="form.departmentId"
            class="form-input"
            @change="$emit('update:form', { ...form, departmentId: $event.target.value })"
          >
            <option value="">{{ t('common.select') }}</option>
            <option v-for="d in departments" :key="d.id" :value="d.id">{{ d.name }}</option>
          </select>
        </div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <div class="modal-footer">
          <button type="button" class="btn btn-default" @click="$emit('close')">{{ t('common.cancel') }}</button>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            {{ saving ? t('common.saving') : t('common.save') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps({
  show: { type: Boolean, required: true },
  isEditing: { type: Boolean, default: false },
  form: { type: Object, required: true },
  companies: { type: Array, default: () => [] },
  departments: { type: Array, default: () => [] },
  saving: { type: Boolean, default: false },
  error: { type: String, default: '' }
})

const emit = defineEmits(['close', 'save', 'load-departments', 'update:form'])

function onCompanyChange(event) {
  const companyId = event.target.value
  emit('update:form', { ...props.form, companyId, departmentId: '' })
  emit('load-departments', companyId)
}
</script>

<style scoped>
/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--color-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.modal-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--color-text-secondary);
  line-height: 1;
  padding: 0;
  cursor: pointer;
}

.modal-close:hover {
  color: var(--color-text);
}

.modal-body {
  padding: var(--spacing-lg);
}

.form-group {
  margin-bottom: var(--spacing-md);
}

.form-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  outline: none;
}

.form-input:focus {
  border-color: var(--color-primary);
}

.form-input:disabled {
  background-color: var(--color-bg);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-lg);
}

.error-message {
  padding: 8px 12px;
  background-color: var(--color-notice-error-bg);
  border: 1px solid var(--color-notice-error-border);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-sm);
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-primary {
  background-color: var(--color-primary);
  color: var(--color-text-inverse);
}

.btn-primary:hover {
  background-color: var(--color-primary-dark);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-default {
  background-color: var(--color-bg-white);
  color: var(--color-text);
  border-color: var(--color-border);
}

.btn-default:hover {
  background-color: var(--color-bg);
}
</style>
