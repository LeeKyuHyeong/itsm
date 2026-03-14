<template>
  <div class="batch-manage">
    <div class="page-header">
      <h1 class="page-title">{{ t('admin.batchManage') }}</h1>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('admin.number') }}</th>
            <th>{{ t('admin.batchJobName') }}</th>
            <th>{{ t('admin.batchJobNameEn') }}</th>
            <th>{{ t('admin.batchDescription') }}</th>
            <th>{{ t('admin.cronExpression') }}</th>
            <th>{{ t('admin.isActive') }}</th>
            <th>{{ t('admin.lastExecutedAt') }}</th>
            <th>{{ t('admin.lastResult') }}</th>
            <th>{{ t('admin.manage') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="9" class="text-center">{{ t('common.loading') }}</td>
          </tr>
          <tr v-else-if="jobs.length === 0">
            <td colspan="9" class="text-center">{{ t('admin.noBatchJobs') }}</td>
          </tr>
          <tr v-for="(job, index) in jobs" :key="job.batchJobId">
            <td>{{ index + 1 }}</td>
            <td class="job-name">{{ job.jobName }}</td>
            <td>{{ job.jobNameEn || '-' }}</td>
            <td>{{ job.jobDescription || '-' }}</td>
            <td><code class="cron-code">{{ job.cronExpression }}</code></td>
            <td>
              <span class="status-badge" :class="job.isActive === 'Y' ? 'status-active' : 'status-inactive'">
                {{ job.isActive === 'Y' ? t('admin.active') : t('admin.inactive') }}
              </span>
            </td>
            <td>{{ formatDate(job.lastExecutedAt) }}</td>
            <td>
              <span v-if="job.lastResult" class="result-badge" :class="`result-${job.lastResult?.toLowerCase()}`">
                {{ job.lastResult }}
              </span>
              <span v-else>-</span>
            </td>
            <td>
              <button class="btn btn-sm btn-default" @click="openEditModal(job)">{{ t('common.edit') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ t('admin.batchEdit') }}</h2>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="save">
          <div class="form-group">
            <label class="form-label">{{ t('admin.batchJobName') }}</label>
            <input :value="editingJob?.jobName" type="text" class="form-input" disabled />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.batchJobNameEn') }}</label>
            <input v-model="form.jobNameEn" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.cronExpression') }} <span class="required">*</span></label>
            <input v-model="form.cronExpression" type="text" class="form-input" required />
            <small class="form-hint">{{ t('admin.cronHint') }}</small>
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.isActive') }}</label>
            <select v-model="form.isActive" class="form-input">
              <option value="Y">{{ t('admin.active') }}</option>
              <option value="N">{{ t('admin.inactive') }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.batchDescription') }}</label>
            <input v-model="form.jobDescription" type="text" class="form-input" />
          </div>
          <div v-if="saveError" class="error-message">{{ saveError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="closeModal">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? t('common.saving') : t('common.save') }}
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
import { batchJobApi } from '@/api/admin/batchJob.js'

const { t } = useI18n()

const jobs = ref([])
const loading = ref(false)
const saving = ref(false)
const saveError = ref('')
const showModal = ref(false)
const editingJob = ref(null)

const form = reactive({
  cronExpression: '',
  isActive: 'Y',
  jobDescription: '',
  jobNameEn: ''
})

function formatDate(dt) {
  if (!dt) return '-'
  return dt.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadJobs()
})

async function loadJobs() {
  loading.value = true
  try {
    const { data } = await batchJobApi.getList()
    jobs.value = data.data || data || []
  } catch (e) {
    console.error('loadJobs failed:', e)
  } finally {
    loading.value = false
  }
}

function openEditModal(job) {
  editingJob.value = job
  Object.assign(form, {
    cronExpression: job.cronExpression,
    isActive: job.isActive,
    jobDescription: job.jobDescription || '',
    jobNameEn: job.jobNameEn || ''
  })
  saveError.value = ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingJob.value = null
}

async function save() {
  saving.value = true
  saveError.value = ''
  try {
    await batchJobApi.update(editingJob.value.batchJobId, { ...form })
    closeModal()
    loadJobs()
  } catch (e) {
    saveError.value = e.response?.data?.error?.message || e.response?.data?.message || t('message.saveFail')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.batch-manage {
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
  background-color: var(--color-table-header);
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
  border-bottom: 1px solid var(--color-border-light);
  color: var(--color-text);
}

.data-table tbody tr:nth-child(even) {
  background-color: var(--color-table-row-even);
}

.data-table tbody tr:hover {
  background-color: var(--color-table-row-hover);
}

.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: var(--font-size-xs);
  font-weight: 500;
}

.status-active {
  background-color: var(--color-badge-green-bg);
  color: var(--color-success);
}

.status-inactive {
  background-color: var(--color-badge-gray-bg);
  color: var(--color-text-secondary);
}

.text-center {
  text-align: center;
}

/* Batch-specific styles */
.job-name {
  font-weight: 600;
  font-family: monospace;
}

.cron-code {
  background: var(--color-stats-row-bg);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-family: monospace;
}

.result-badge {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.result-success {
  background: var(--color-badge-green-bg);
  color: var(--color-badge-green);
}

.result-failure {
  background: var(--color-badge-pink-bg);
  color: var(--color-badge-pink);
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

.form-hint {
  display: block;
  margin-top: 4px;
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
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
</style>
