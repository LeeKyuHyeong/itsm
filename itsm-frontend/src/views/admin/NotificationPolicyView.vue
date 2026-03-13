<template>
  <div class="noti-policy-manage">
    <div class="page-header">
      <h1 class="page-title">{{ t('admin.notificationPolicy') }}</h1>
      <button class="btn btn-primary" @click="openDialog()">+ {{ t('common.add') }}</button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>번호</th>
            <th>알림유형</th>
            <th>발송조건</th>
            <th>대상역할</th>
            <th>사용여부</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="text-center">로딩 중...</td>
          </tr>
          <tr v-else-if="policies.length === 0">
            <td colspan="6" class="text-center">등록된 알림 정책이 없습니다.</td>
          </tr>
          <tr v-for="(policy, index) in policies" :key="policy.id">
            <td>{{ index + 1 }}</td>
            <td>{{ policy.typeName || policy.typeCd || '-' }}</td>
            <td>{{ policy.conditionDescription || policy.conditionExpr || '-' }}</td>
            <td>{{ policy.targetRoleName || policy.targetRoleCd || '-' }}</td>
            <td>
              <span class="status-badge" :class="policy.active !== false ? 'status-active' : 'status-inactive'">
                {{ policy.active !== false ? '사용' : '미사용' }}
              </span>
            </td>
            <td>
              <div class="action-buttons">
                <button class="btn btn-sm btn-default" @click="openDialog(policy)">{{ t('common.edit') }}</button>
                <button
                  v-if="policy.active !== false"
                  class="btn btn-sm btn-danger"
                  @click="toggleStatus(policy, false)"
                >
                  비활성화
                </button>
                <button
                  v-else
                  class="btn btn-sm btn-primary"
                  @click="toggleStatus(policy, true)"
                >
                  활성화
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Policy Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ editing ? '알림 정책 수정' : '알림 정책 추가' }}</h2>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="save">
          <div class="form-group">
            <label class="form-label">알림유형코드</label>
            <input v-model="form.typeCd" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">발송조건</label>
            <input v-model="form.conditionExpr" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">대상역할코드</label>
            <input v-model="form.targetRoleCd" type="text" class="form-input" required />
          </div>
          <div v-if="saveError" class="error-message">{{ saveError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="closeModal">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? '저장 중...' : t('common.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { notificationPolicyApi } from '@/api/admin/notificationPolicy.js'

const { t } = useI18n()

const policies = ref([])
const loading = ref(false)

// Modal
const showModal = ref(false)
const editing = ref(null)
const saving = ref(false)
const saveError = ref('')
const form = reactive({
  typeCd: '',
  conditionExpr: '',
  targetRoleCd: ''
})

onMounted(() => {
  loadPolicies()
})

async function loadPolicies() {
  loading.value = true
  try {
    const { data } = await notificationPolicyApi.getList({ size: 100 })
    const result = data.data || data
    policies.value = result.content || result.items || result || []
  } catch (error) {
    console.error('알림 정책 목록 로드 실패:', error)
    policies.value = []
  } finally {
    loading.value = false
  }
}

function openDialog(policy = null) {
  editing.value = policy
  saveError.value = ''
  if (policy) {
    Object.assign(form, {
      typeCd: policy.typeCd || '',
      conditionExpr: policy.conditionExpr || '',
      targetRoleCd: policy.targetRoleCd || ''
    })
  } else {
    Object.assign(form, {
      typeCd: '',
      conditionExpr: '',
      targetRoleCd: ''
    })
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editing.value = null
}

async function save() {
  saving.value = true
  saveError.value = ''
  try {
    if (editing.value) {
      await notificationPolicyApi.update(editing.value.id, { ...form })
    } else {
      await notificationPolicyApi.create({ ...form })
    }
    closeModal()
    loadPolicies()
  } catch (error) {
    saveError.value = error.response?.data?.message || t('message.saveFail')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(policy, active) {
  try {
    await notificationPolicyApi.changeStatus(policy.id, { active })
    loadPolicies()
  } catch (error) {
    alert(error.response?.data?.message || '상태 변경 중 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
.noti-policy-manage {
  max-width: 1200px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
}

.page-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--color-text);
}

.table-container {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background-color: #f8f9fa;
  padding: 10px 12px;
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  text-align: left;
  border-bottom: 2px solid var(--color-border);
  white-space: nowrap;
}

.data-table td {
  padding: 10px 12px;
  font-size: var(--font-size-sm);
  border-bottom: 1px solid #f0f0f0;
  color: var(--color-text);
}

.data-table tbody tr:nth-child(even) {
  background-color: #fafbfc;
}

.data-table tbody tr:hover {
  background-color: #f0f5ff;
}

.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: var(--font-size-xs);
  font-weight: 500;
}

.status-active {
  background-color: #e6f4ea;
  color: var(--color-success);
}

.status-inactive {
  background-color: #f1f3f4;
  color: var(--color-text-secondary);
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.text-center {
  text-align: center;
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
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-danger {
  background-color: var(--color-danger);
  color: white;
  border-color: var(--color-danger);
}

.btn-danger:hover {
  opacity: 0.9;
}

.btn-default {
  background-color: white;
  color: var(--color-text);
  border-color: var(--color-border);
}

.btn-default:hover {
  background-color: var(--color-bg);
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
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
  max-width: 480px;
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
}

.modal-close:hover {
  color: var(--color-text);
}

.modal-body {
  padding: var(--spacing-lg);
}

.modal-body .form-group {
  margin-bottom: var(--spacing-md);
}

.modal-body .form-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.modal-body .form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  outline: none;
}

.modal-body .form-input:focus {
  border-color: var(--color-primary);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-lg);
}

.error-message {
  padding: 8px 12px;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-sm);
  color: var(--color-danger);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-sm);
}
</style>
