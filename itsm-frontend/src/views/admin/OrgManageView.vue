<template>
  <div class="org-manage">
    <div class="page-header">
      <h1 class="page-title">조직 관리</h1>
    </div>

    <div class="org-layout">
      <!-- Company Section -->
      <div class="org-panel">
        <div class="panel-header">
          <h2 class="panel-title">회사 목록</h2>
          <button class="btn btn-primary btn-sm" @click="openCompanyDialog()">+ 회사 추가</button>
        </div>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>번호</th>
                <th>회사명</th>
                <th>대표자</th>
                <th>연락처</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loadingCompanies">
                <td colspan="5" class="text-center">로딩 중...</td>
              </tr>
              <tr v-else-if="companies.length === 0">
                <td colspan="5" class="text-center">등록된 회사가 없습니다.</td>
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
                  <button class="btn btn-sm btn-default" @click.stop="openCompanyDialog(company)">수정</button>
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
            부서 목록
            <span v-if="selectedCompanyName" class="panel-subtitle">- {{ selectedCompanyName }}</span>
          </h2>
          <button
            class="btn btn-primary btn-sm"
            :disabled="!selectedCompanyId"
            @click="openDeptDialog()"
          >
            + 부서 추가
          </button>
        </div>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>번호</th>
                <th>부서명</th>
                <th>부서코드</th>
                <th>상위부서</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!selectedCompanyId">
                <td colspan="5" class="text-center text-secondary">회사를 선택해주세요.</td>
              </tr>
              <tr v-else-if="loadingDepts">
                <td colspan="5" class="text-center">로딩 중...</td>
              </tr>
              <tr v-else-if="departments.length === 0">
                <td colspan="5" class="text-center">등록된 부서가 없습니다.</td>
              </tr>
              <tr v-for="(dept, index) in departments" :key="dept.id">
                <td>{{ index + 1 }}</td>
                <td>{{ dept.name }}</td>
                <td>{{ dept.code || '-' }}</td>
                <td>{{ dept.parentName || '-' }}</td>
                <td>
                  <button class="btn btn-sm btn-default" @click="openDeptDialog(dept)">수정</button>
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
          <h2 class="modal-title">{{ editingCompany ? '회사 수정' : '회사 추가' }}</h2>
          <button class="modal-close" @click="closeCompanyModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="saveCompany">
          <div class="form-group">
            <label class="form-label">회사명</label>
            <input v-model="companyForm.name" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">대표자</label>
            <input v-model="companyForm.representative" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">사업자번호</label>
            <input v-model="companyForm.businessNumber" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">연락처</label>
            <input v-model="companyForm.phone" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">주소</label>
            <input v-model="companyForm.address" type="text" class="form-input" />
          </div>
          <div v-if="companySaveError" class="error-message">{{ companySaveError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="closeCompanyModal">취소</button>
            <button type="submit" class="btn btn-primary" :disabled="companySaving">
              {{ companySaving ? '저장 중...' : '저장' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Department Modal -->
    <div v-if="showDeptModal" class="modal-overlay" @click.self="closeDeptModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ editingDept ? '부서 수정' : '부서 추가' }}</h2>
          <button class="modal-close" @click="closeDeptModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="saveDept">
          <div class="form-group">
            <label class="form-label">부서명</label>
            <input v-model="deptForm.name" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">부서코드</label>
            <input v-model="deptForm.code" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">상위부서</label>
            <select v-model="deptForm.parentId" class="form-input">
              <option value="">없음 (최상위)</option>
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
            <button type="button" class="btn btn-default" @click="closeDeptModal">취소</button>
            <button type="submit" class="btn btn-primary" :disabled="deptSaving">
              {{ deptSaving ? '저장 중...' : '저장' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { companyApi } from '@/api/company.js'

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
    companySaveError.value = error.response?.data?.message || '저장 중 오류가 발생했습니다.'
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
    deptSaveError.value = error.response?.data?.message || '저장 중 오류가 발생했습니다.'
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
  background-color: #f8f9fa;
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
  background-color: #f8f9fa;
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
  border-bottom: 1px solid #f0f0f0;
  color: var(--color-text);
}

.data-table tbody tr:nth-child(even) {
  background-color: #fafbfc;
}

.data-table tbody tr:hover {
  background-color: #f0f5ff;
}

.clickable-row {
  cursor: pointer;
}

.clickable-row.selected {
  background-color: #e8f0fe !important;
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
