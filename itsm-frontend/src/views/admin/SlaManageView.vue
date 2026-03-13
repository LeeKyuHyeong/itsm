<template>
  <div class="sla-manage">
    <div class="page-header">
      <h1 class="page-title">{{ t('admin.slaManage') }}</h1>
      <button class="btn btn-primary" @click="openDialog()">+ {{ t('common.add') }}</button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>번호</th>
            <th>우선순위</th>
            <th>처리기한(시간)</th>
            <th>경고기준(%)</th>
            <th>{{ t('asset.company') }}</th>
            <th>사용여부</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center">로딩 중...</td>
          </tr>
          <tr v-else-if="slaList.length === 0">
            <td colspan="7" class="text-center">등록된 SLA 정책이 없습니다.</td>
          </tr>
          <tr v-for="(sla, index) in slaList" :key="sla.id">
            <td>{{ index + 1 }}</td>
            <td>{{ sla.priorityName || sla.priorityCd || '-' }}</td>
            <td>{{ sla.resolutionTimeHours }}</td>
            <td>{{ sla.warningThresholdPercent }}%</td>
            <td>{{ sla.companyName || '공통' }}</td>
            <td>
              <span class="status-badge" :class="sla.active !== false ? 'status-active' : 'status-inactive'">
                {{ sla.active !== false ? '사용' : '미사용' }}
              </span>
            </td>
            <td>
              <button class="btn btn-sm btn-default" @click="openDialog(sla)">{{ t('common.edit') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- SLA Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ editing ? 'SLA 정책 수정' : 'SLA 정책 추가' }}</h2>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="save">
          <div class="form-group">
            <label class="form-label">{{ t('asset.company') }} (선택)</label>
            <select v-model="form.companyId" class="form-input">
              <option value="">공통 (전체 적용)</option>
              <option v-for="c in companies" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">우선순위코드</label>
            <input v-model="form.priorityCd" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">처리기한 (시간)</label>
            <input v-model.number="form.resolutionTimeHours" type="number" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">경고기준 (%)</label>
            <input v-model.number="form.warningThresholdPercent" type="number" class="form-input" min="0" max="100" required />
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
import { slaApi } from '@/api/admin/sla.js'
import { companyApi } from '@/api/company.js'

const { t } = useI18n()

const slaList = ref([])
const companies = ref([])
const loading = ref(false)

// Modal
const showModal = ref(false)
const editing = ref(null)
const saving = ref(false)
const saveError = ref('')
const form = reactive({
  companyId: '',
  priorityCd: '',
  resolutionTimeHours: 0,
  warningThresholdPercent: 0
})

onMounted(() => {
  loadSlaList()
  loadCompanies()
})

async function loadSlaList() {
  loading.value = true
  try {
    const { data } = await slaApi.getList({ size: 100 })
    const result = data.data || data
    slaList.value = result.content || result.items || result || []
  } catch (error) {
    console.error('SLA 목록 로드 실패:', error)
    slaList.value = []
  } finally {
    loading.value = false
  }
}

async function loadCompanies() {
  try {
    const { data } = await companyApi.getList({ size: 100 })
    const result = data.data || data
    companies.value = result.content || result.items || result || []
  } catch (error) {
    console.error('회사 목록 로드 실패:', error)
  }
}

function openDialog(sla = null) {
  editing.value = sla
  saveError.value = ''
  if (sla) {
    Object.assign(form, {
      companyId: sla.companyId || '',
      priorityCd: sla.priorityCd || '',
      resolutionTimeHours: sla.resolutionTimeHours || 0,
      warningThresholdPercent: sla.warningThresholdPercent || 0
    })
  } else {
    Object.assign(form, {
      companyId: '',
      priorityCd: '',
      resolutionTimeHours: 0,
      warningThresholdPercent: 0
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
    const payload = { ...form }
    if (!payload.companyId) delete payload.companyId

    if (editing.value) {
      await slaApi.update(editing.value.id, payload)
    } else {
      await slaApi.create(payload)
    }
    closeModal()
    loadSlaList()
  } catch (error) {
    saveError.value = error.response?.data?.message || t('message.saveFail')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.sla-manage {
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
