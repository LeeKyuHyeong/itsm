<template>
  <div class="org-manage">
    <div class="page-header">
      <h1 class="page-title">{{ t('admin.orgManage') }}</h1>
    </div>

    <div class="org-layout">
      <!-- Company Section -->
      <div class="org-panel">
        <div class="panel-header">
          <h2 class="panel-title">{{ t('admin.companyList') }}</h2>
          <button class="btn btn-primary btn-sm" @click="openCompanyDialog()">+ {{ t('common.add') }}</button>
        </div>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>{{ t('admin.number') }}</th>
                <th>{{ t('admin.companyName') }}</th>
                <th>{{ t('admin.representative') }}</th>
                <th>{{ t('admin.contact') }}</th>
                <th>{{ t('admin.manage') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loadingCompanies">
                <td colspan="5" class="text-center">{{ t('common.loading') }}</td>
              </tr>
              <tr v-else-if="companies.length === 0">
                <td colspan="5" class="text-center">{{ t('admin.noCompanies') }}</td>
              </tr>
              <tr
                v-for="(company, index) in companies"
                :key="company.id"
                class="clickable-row"
                :class="{ selected: selectedCompanyId === company.id }"
                @click="selectCompany(company)"
              >
                <td>{{ index + 1 }}</td>
                <td>{{ company.name }}</td>
                <td>{{ company.representative || '-' }}</td>
                <td>{{ company.phone || '-' }}</td>
                <td>
                  <button class="btn btn-sm btn-default" @click.stop="openCompanyDialog(company)">{{ t('common.edit') }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Department Section -->
      <div class="org-panel">
        <div class="panel-header">
          <h2 class="panel-title">
            {{ t('admin.departmentList') }}
            <span v-if="selectedCompanyName" class="panel-subtitle">- {{ selectedCompanyName }}</span>
          </h2>
          <button
            class="btn btn-primary btn-sm"
            :disabled="!selectedCompanyId"
            @click="openDeptDialog()"
          >
            + {{ t('common.add') }}
          </button>
        </div>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>{{ t('admin.number') }}</th>
                <th>{{ t('admin.departmentName') }}</th>
                <th>{{ t('admin.departmentCode') }}</th>
                <th>{{ t('admin.parentDepartment') }}</th>
                <th>{{ t('admin.manage') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!selectedCompanyId">
                <td colspan="5" class="text-center text-secondary">{{ t('admin.selectCompany') }}</td>
              </tr>
              <tr v-else-if="loadingDepts">
                <td colspan="5" class="text-center">{{ t('common.loading') }}</td>
              </tr>
              <tr v-else-if="departments.length === 0">
                <td colspan="5" class="text-center">{{ t('admin.noDepartments') }}</td>
              </tr>
              <tr v-for="(dept, index) in departments" :key="dept.id">
                <td>{{ index + 1 }}</td>
                <td>{{ dept.name }}</td>
                <td>{{ dept.code || '-' }}</td>
                <td>{{ dept.parentName || '-' }}</td>
                <td>
                  <button class="btn btn-sm btn-default" @click="openDeptDialog(dept)">{{ t('common.edit') }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Company Modal -->
    <div v-if="showCompanyModal" class="modal-overlay" @click.self="closeCompanyModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ editingCompany ? t('admin.companyEdit') : t('admin.companyAdd') }}</h2>
          <button class="modal-close" @click="closeCompanyModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="saveCompany">
          <div class="form-group">
            <label class="form-label">{{ t('admin.companyName') }}</label>
            <input v-model="companyForm.name" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.representative') }}</label>
            <input v-model="companyForm.representative" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.businessNumber') }}</label>
            <input v-model="companyForm.businessNumber" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.contact') }}</label>
            <input v-model="companyForm.phone" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.address') }}</label>
            <input v-model="companyForm.address" type="text" class="form-input" />
          </div>
          <div v-if="companySaveError" class="error-message">{{ companySaveError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="closeCompanyModal">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary" :disabled="companySaving">
              {{ companySaving ? t('common.saving') : t('common.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Department Modal -->
    <div v-if="showDeptModal" class="modal-overlay" @click.self="closeDeptModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ editingDept ? t('admin.departmentEdit') : t('admin.departmentAdd') }}</h2>
          <button class="modal-close" @click="closeDeptModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="saveDept">
          <div class="form-group">
            <label class="form-label">{{ t('admin.departmentName') }}</label>
            <input v-model="deptForm.name" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.departmentCode') }}</label>
            <input v-model="deptForm.code" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.parentDepartment') }}</label>
            <select v-model="deptForm.parentId" class="form-input">
              <option value="">{{ t('admin.noneTopLevel') }}</option>
              <option
                v-for="d in departments.filter(d => d.id !== editingDept?.id)"
                :key="d.id"
                :value="d.id"
              >
                {{ d.name }}
              </option>
            </select>
          </div>
          <div v-if="deptSaveError" class="error-message">{{ deptSaveError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="closeDeptModal">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary" :disabled="deptSaving">
              {{ deptSaving ? t('common.saving') : t('common.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { companyApi } from '@/api/company.js'

const { t } = useI18n()

const companies = ref([])
const departments = ref([])
const loadingCompanies = ref(false)
const loadingDepts = ref(false)

const selectedCompanyId = ref(null)
const selectedCompanyName = computed(() => {
  const c = companies.value.find(c => c.id === selectedCompanyId.value)
  return c?.name || ''
})

// Company Modal
const showCompanyModal = ref(false)
const editingCompany = ref(null)
const companySaving = ref(false)
const companySaveError = ref('')
const companyForm = reactive({
  name: '',
  representative: '',
  businessNumber: '',
  phone: '',
  address: ''
})

// Dept Modal
const showDeptModal = ref(false)
const editingDept = ref(null)
const deptSaving = ref(false)
const deptSaveError = ref('')
const deptForm = reactive({
  name: '',
  code: '',
  parentId: ''
})

onMounted(() => {
  loadCompanies()
})

async function loadCompanies() {
  loadingCompanies.value = true
  try {
    const { data } = await companyApi.getList({ size: 100 })
    const result = data.data || data
    companies.value = result.content || result.items || result || []
  } catch (error) {
    console.error('회사 목록 로드 실패:', error)
    companies.value = []
  } finally {
    loadingCompanies.value = false
  }
}

async function selectCompany(company) {
  selectedCompanyId.value = company.id
  await loadDepartments()
}

async function loadDepartments() {
  if (!selectedCompanyId.value) return
  loadingDepts.value = true
  try {
    const { data } = await companyApi.getDepartments(selectedCompanyId.value)
    departments.value = data.data || data || []
  } catch (error) {
    console.error('부서 목록 로드 실패:', error)
    departments.value = []
  } finally {
    loadingDepts.value = false
  }
}

// Company CRUD
function openCompanyDialog(company = null) {
  editingCompany.value = company
  companySaveError.value = ''
  if (company) {
    Object.assign(companyForm, {
      name: company.name || '',
      representative: company.representative || '',
      businessNumber: company.businessNumber || '',
      phone: company.phone || '',
      address: company.address || ''
    })
  } else {
    Object.assign(companyForm, {
      name: '',
      representative: '',
      businessNumber: '',
      phone: '',
      address: ''
    })
  }
  showCompanyModal.value = true
}

function closeCompanyModal() {
  showCompanyModal.value = false
  editingCompany.value = null
}

async function saveCompany() {
  companySaving.value = true
  companySaveError.value = ''
  try {
    if (editingCompany.value) {
      await companyApi.update(editingCompany.value.id, { ...companyForm })
    } else {
      await companyApi.create({ ...companyForm })
    }
    closeCompanyModal()
    loadCompanies()
  } catch (error) {
    companySaveError.value = error.response?.data?.message || t('message.saveFail')
  } finally {
    companySaving.value = false
  }
}

// Dept CRUD
function openDeptDialog(dept = null) {
  editingDept.value = dept
  deptSaveError.value = ''
  if (dept) {
    Object.assign(deptForm, {
      name: dept.name || '',
      code: dept.code || '',
      parentId: dept.parentId || ''
    })
  } else {
    Object.assign(deptForm, {
      name: '',
      code: '',
      parentId: ''
    })
  }
  showDeptModal.value = true
}

function closeDeptModal() {
  showDeptModal.value = false
  editingDept.value = null
}

async function saveDept() {
  deptSaving.value = true
  deptSaveError.value = ''
  try {
    const payload = { ...deptForm }
    if (!payload.parentId) delete payload.parentId

    if (editingDept.value) {
      await companyApi.updateDepartment(editingDept.value.id, payload)
    } else {
      await companyApi.createDepartment(selectedCompanyId.value, payload)
    }
    closeDeptModal()
    loadDepartments()
  } catch (error) {
    deptSaveError.value = error.response?.data?.message || t('message.saveFail')
  } finally {
    deptSaving.value = false
  }
}
</script>

<style scoped>
.org-manage {
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

.org-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
}

.org-panel {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  background-color: var(--color-table-header);
  border-bottom: 1px solid var(--color-border);
}

.panel-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text);
}

.panel-subtitle {
  font-weight: 400;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.table-container {
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background-color: var(--color-table-header);
  padding: 8px 12px;
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  text-align: left;
  border-bottom: 2px solid var(--color-border);
  white-space: nowrap;
}

.data-table td {
  padding: 8px 12px;
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

.clickable-row {
  cursor: pointer;
}

.clickable-row.selected {
  background-color: var(--color-primary-bg) !important;
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
