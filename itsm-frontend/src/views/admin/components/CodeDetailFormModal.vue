<template>
  <BaseModal :show="show" :title="isEditing ? t('admin.codeEdit') : t('admin.codeAdd')" @close="$emit('close')">
    <form @submit.prevent="$emit('save')">
      <div class="form-group">
        <label class="form-label">{{ t('admin.codeValue') }}</label>
        <input v-model="form.codeVal" type="text" class="form-input" :disabled="isEditing" required />
      </div>
      <div class="form-group">
        <label class="form-label">{{ t('admin.codeName') }}</label>
        <input v-model="form.codeNm" type="text" class="form-input" required />
      </div>
      <div class="form-group">
        <label class="form-label">{{ t('admin.codeNameEn') }}</label>
        <input v-model="form.codeNmEn" type="text" class="form-input" />
      </div>
      <div class="form-group">
        <label class="form-label">{{ t('admin.sortOrder') }}</label>
        <input v-model.number="form.sortOrder" type="number" class="form-input" />
      </div>
      <div v-if="error" class="error-message">{{ error }}</div>
      <div class="modal-actions">
        <button type="button" class="btn btn-default" @click="$emit('close')">{{ t('common.cancel') }}</button>
        <button type="submit" class="btn btn-primary" :disabled="saving">
          {{ saving ? t('common.saving') : t('common.save') }}
        </button>
      </div>
    </form>
    <template #footer />
  </BaseModal>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import BaseModal from '@/components/common/BaseModal.vue'

const { t } = useI18n()

defineProps({
  show: {
    type: Boolean,
    required: true
  },
  isEditing: {
    type: Boolean,
    default: false
  },
  form: {
    type: Object,
    required: true
  },
  saving: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  }
})

defineEmits(['close', 'save'])
</script>

<style scoped>
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
  background-color: var(--color-bg-white);
  color: var(--color-text);
  outline: none;
}

.form-input:focus {
  border-color: var(--color-primary);
}

.form-input:disabled {
  background-color: var(--color-bg);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-lg);
}

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

.btn-primary:hover:not(:disabled) {
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

.error-message {
  padding: 8px 12px;
  background-color: var(--color-notice-error-bg);
  border: 1px solid var(--color-notice-error-border);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-sm);
}
</style>
