<template>
  <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-card">
      <div class="modal-header">
        <h2 class="modal-title">{{ t('admin.roleManage') }} - {{ user?.name }}</h2>
        <button class="modal-close" @click="$emit('close')">&times;</button>
      </div>
      <div class="modal-body">
        <div class="role-section">
          <h3 class="role-section-title">{{ t('admin.currentRoles') }}</h3>
          <div v-if="user?.roles?.length" class="role-list">
            <div v-for="role in user.roles" :key="role" class="role-item">
              <span class="role-tag">{{ roleLabel(role) }}</span>
              <button class="btn btn-sm btn-danger" @click="$emit('remove-role', role)">{{ t('common.delete') }}</button>
            </div>
          </div>
          <p v-else class="text-secondary">{{ t('admin.noRolesAssigned') }}</p>
        </div>

        <div class="role-section">
          <h3 class="role-section-title">{{ t('admin.addRole') }}</h3>
          <div class="role-add-row">
            <select v-model="selectedRole" class="form-input">
              <option value="">{{ t('admin.selectRole') }}</option>
              <option
                v-for="(label, key) in availableRoles"
                :key="key"
                :value="key"
              >
                {{ label }}
              </option>
            </select>
            <button
              class="btn btn-primary"
              :disabled="!selectedRole"
              @click="onAddRole"
            >
              {{ t('common.add') }}
            </button>
          </div>
        </div>

        <div v-if="error" class="error-message">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, te } = useI18n()

const props = defineProps({
  show: { type: Boolean, required: true },
  user: { type: Object, default: null },
  availableRoles: { type: Object, default: () => ({}) },
  saving: { type: Boolean, default: false },
  error: { type: String, default: '' }
})

const emit = defineEmits(['close', 'add-role', 'remove-role'])

const selectedRole = ref('')

watch(() => props.show, (val) => {
  if (val) {
    selectedRole.value = ''
  }
})

function roleLabel(role) {
  const key = `role.${role}`
  return te(key) ? t(key) : role
}

function onAddRole() {
  if (!selectedRole.value) return
  emit('add-role', selectedRole.value)
  selectedRole.value = ''
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

.text-secondary {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

/* Role Section */
.role-section {
  margin-bottom: var(--spacing-lg);
}

.role-section-title {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.role-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  background-color: var(--color-bg);
  border-radius: var(--radius-sm);
}

.role-tag {
  display: inline-block;
  padding: 1px 6px;
  background-color: var(--color-primary-bg);
  color: var(--color-primary);
  border-radius: 4px;
  font-size: var(--font-size-xs);
}

.role-add-row {
  display: flex;
  gap: var(--spacing-sm);
}

.role-add-row .form-input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  outline: none;
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

.btn-sm {
  padding: 4px 8px;
  font-size: var(--font-size-xs);
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

.btn-danger {
  background-color: var(--color-danger);
  color: var(--color-text-inverse);
  border-color: var(--color-danger);
}

.btn-danger:hover {
  opacity: 0.9;
}
</style>
