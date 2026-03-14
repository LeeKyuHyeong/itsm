<template>
  <div class="account-manage">
    <div class="page-header">
      <h1 class="page-title">{{ t('admin.accountManage') }}</h1>
      <button class="btn btn-primary" @click="openCreateDialog">
        + {{ t('common.add') }}
      </button>
    </div>

    <!-- Search & Filters -->
    <div class="filter-bar">
      <div class="filter-group">
        <label class="filter-label">{{ t('asset.status') }}</label>
        <select v-model="filters.status" class="filter-select" @change="loadUsers">
          <option value="">{{ t('common.all') }}</option>
          <option value="ACTIVE">{{ t('status.ACTIVE') }}</option>
          <option value="INACTIVE">{{ t('status.INACTIVE') }}</option>
          <option value="LOCKED">{{ t('status.LOCKED') }}</option>
          <option value="RESIGNED">{{ t('admin.resigned') }}</option>
          <option value="DELETED">{{ t('status.DELETED') }}</option>
        </select>
      </div>
      <div class="filter-group search-group">
        <input
          v-model="filters.keyword"
          type="text"
          class="filter-input"
          :placeholder="t('admin.searchByNameOrId')"
          @keyup.enter="loadUsers"
        />
        <button class="btn btn-default" @click="loadUsers">{{ t('common.search') }}</button>
      </div>
    </div>

    <!-- User Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('admin.number') }}</th>
            <th>{{ t('admin.loginId') }}</th>
            <th>{{ t('admin.name') }}</th>
            <th>{{ t('admin.email') }}</th>
            <th>{{ t('admin.department') }}</th>
            <th>{{ t('asset.status') }}</th>
            <th>{{ t('admin.role') }}</th>
            <th>{{ t('admin.manage') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="text-center">{{ t('common.loading') }}</td>
          </tr>
          <tr v-else-if="users.length === 0">
            <td colspan="8" class="text-center">{{ t('common.noData') }}</td>
          </tr>
          <tr v-for="(user, index) in users" :key="user.id">
            <td>{{ (pagination.page - 1) * pagination.size + index + 1 }}</td>
            <td>{{ user.loginId }}</td>
            <td>{{ user.name }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.departmentName || '-' }}</td>
            <td>
              <span class="status-badge" :class="'status-' + (user.status || '').toLowerCase()">
                {{ statusLabel(user.status) }}
              </span>
            </td>
            <td>
              <div class="role-tags">
                <span
                  v-for="role in (user.roles || [])"
                  :key="role"
                  class="role-tag"
                >
                  {{ roleLabel(role) }}
                </span>
                <span v-if="!user.roles || user.roles.length === 0">-</span>
              </div>
            </td>
            <td>
              <div class="action-buttons">
                <button class="btn btn-sm btn-default" @click="openEditDialog(user)">{{ t('common.edit') }}</button>
                <button class="btn btn-sm btn-default" @click="openRoleDialog(user)">{{ t('admin.role') }}</button>
                <button
                  v-if="user.status === 'ACTIVE'"
                  class="btn btn-sm btn-danger"
                  @click="changeUserStatus(user, 'INACTIVE')"
                >
                  {{ t('admin.deactivate') }}
                </button>
                <button
                  v-if="user.status === 'INACTIVE' || user.status === 'LOCKED'"
                  class="btn btn-sm btn-primary"
                  @click="changeUserStatus(user, 'ACTIVE')"
                >
                  {{ t('admin.activate') }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination">
      <button
        class="page-btn"
        :disabled="pagination.page <= 1"
        @click="goToPage(pagination.page - 1)"
      >
        {{ t('common.prev') }}
      </button>
      <button
        v-for="p in visiblePages"
        :key="p"
        class="page-btn"
        :class="{ active: p === pagination.page }"
        @click="goToPage(p)"
      >
        {{ p }}
      </button>
      <button
        class="page-btn"
        :disabled="pagination.page >= totalPages"
        @click="goToPage(pagination.page + 1)"
      >
        {{ t('common.next') }}
      </button>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showUserModal" class="modal-overlay" @click.self="closeUserModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ isEditing ? t('admin.userEdit') : t('admin.userAdd') }}</h2>
          <button class="modal-close" @click="closeUserModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="saveUser">
          <div class="form-group">
            <label class="form-label">{{ t('admin.loginId') }}</label>
            <input
              v-model="userForm.loginId"
              type="text"
              class="form-input"
              :disabled="isEditing"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.name') }}</label>
            <input v-model="userForm.name" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.email') }}</label>
            <input v-model="userForm.email" type="email" class="form-input" required />
          </div>
          <div v-if="!isEditing" class="form-group">
            <label class="form-label">{{ t('admin.password') }}</label>
            <input v-model="userForm.password" type="password" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.phone') }}</label>
            <input v-model="userForm.phone" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.company') }}</label>
            <select v-model="userForm.companyId" class="form-input" @change="loadDepartments">
              <option value="">{{ t('common.select') }}</option>
              <option v-for="c in companies" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.department') }}</label>
            <select v-model="userForm.departmentId" class="form-input">
              <option value="">{{ t('common.select') }}</option>
              <option v-for="d in departments" :key="d.id" :value="d.id">{{ d.name }}</option>
            </select>
          </div>
          <div v-if="saveError" class="error-message">{{ saveError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="closeUserModal">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? t('common.saving') : t('common.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Role Assignment Modal -->
    <div v-if="showRoleModal" class="modal-overlay" @click.self="closeRoleModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ t('admin.roleManage') }} - {{ roleTarget?.name }}</h2>
          <button class="modal-close" @click="closeRoleModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="role-section">
            <h3 class="role-section-title">{{ t('admin.currentRoles') }}</h3>
            <div v-if="roleTarget?.roles?.length" class="role-list">
              <div v-for="role in roleTarget.roles" :key="role" class="role-item">
                <span class="role-tag">{{ roleLabel(role) }}</span>
                <button class="btn btn-sm btn-danger" @click="removeRoleFromUser(role)">{{ t('common.delete') }}</button>
              </div>
            </div>
            <p v-else class="text-secondary">{{ t('admin.noRolesAssigned') }}</p>
          </div>

          <div class="role-section">
            <h3 class="role-section-title">{{ t('admin.addRole') }}</h3>
            <div class="role-add-row">
              <select v-model="newRole" class="form-input">
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
                :disabled="!newRole"
                @click="addRoleToUser"
              >
                {{ t('common.add') }}
              </button>
            </div>
          </div>

          <div v-if="roleError" class="error-message">{{ roleError }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { userApi } from '@/api/user.js'
import { companyApi } from '@/api/company.js'
import { ROLES } from '@/constants/roles.js'

const { t, te } = useI18n()

const users = ref([])
const loading = ref(false)
const saving = ref(false)
const saveError = ref('')
const companies = ref([])
const departments = ref([])

const filters = reactive({
  status: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const totalPages = computed(() => Math.ceil(pagination.total / pagination.size) || 1)

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, pagination.page - 2)
  const end = Math.min(totalPages.value, pagination.page + 2)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// User Modal
const showUserModal = ref(false)
const isEditing = ref(false)
const editingUserId = ref(null)
const userForm = reactive({
  loginId: '',
  name: '',
  email: '',
  password: '',
  phone: '',
  companyId: '',
  departmentId: ''
})

// Role Modal
const showRoleModal = ref(false)
const roleTarget = ref(null)
const newRole = ref('')
const roleError = ref('')

function statusLabel(status) {
  if (!status) return '-'
  if (status === 'RESIGNED') return t('admin.resigned')
  return t(`status.${status}`, status)
}

function roleLabel(role) {
  const key = `role.${role}`
  return te(key) ? t(key) : role
}

const availableRoles = computed(() => {
  const assigned = roleTarget.value?.roles || []
  const result = {}
  for (const roleCode of Object.values(ROLES)) {
    if (!assigned.includes(roleCode)) {
      result[roleCode] = roleLabel(roleCode)
    }
  }
  return result
})

onMounted(() => {
  loadUsers()
  loadCompanies()
})

async function loadUsers() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (filters.status) params.status = filters.status
    if (filters.keyword) params.keyword = filters.keyword

    const { data } = await userApi.getList(params)
    const result = data.data || data
    users.value = result.content || result.items || result || []
    pagination.total = result.totalElements || result.total || 0
  } catch (error) {
    console.error('사용자 목록 로드 실패:', error)
    users.value = []
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

async function loadDepartments() {
  departments.value = []
  if (!userForm.companyId) return
  try {
    const { data } = await companyApi.getDepartments(userForm.companyId)
    departments.value = data.data || data || []
  } catch (error) {
    console.error('부서 목록 로드 실패:', error)
  }
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  pagination.page = page
  loadUsers()
}

function openCreateDialog() {
  isEditing.value = false
  editingUserId.value = null
  Object.assign(userForm, {
    loginId: '',
    name: '',
    email: '',
    password: '',
    phone: '',
    companyId: '',
    departmentId: ''
  })
  saveError.value = ''
  departments.value = []
  showUserModal.value = true
}

function openEditDialog(user) {
  isEditing.value = true
  editingUserId.value = user.id
  Object.assign(userForm, {
    loginId: user.loginId || '',
    name: user.name || '',
    email: user.email || '',
    password: '',
    phone: user.phone || '',
    companyId: user.companyId || '',
    departmentId: user.departmentId || ''
  })
  saveError.value = ''
  if (user.companyId) {
    loadDepartments()
  }
  showUserModal.value = true
}

function closeUserModal() {
  showUserModal.value = false
}

async function saveUser() {
  saving.value = true
  saveError.value = ''
  try {
    const payload = { ...userForm }
    if (!payload.companyId) delete payload.companyId
    if (!payload.departmentId) delete payload.departmentId

    if (isEditing.value) {
      delete payload.loginId
      delete payload.password
      await userApi.update(editingUserId.value, payload)
    } else {
      await userApi.create(payload)
    }
    closeUserModal()
    loadUsers()
  } catch (error) {
    saveError.value = error.response?.data?.message || t('message.saveFail')
  } finally {
    saving.value = false
  }
}

async function changeUserStatus(user, newStatus) {
  const statusText = statusLabel(newStatus)
  if (!confirm(t('admin.confirmStatusChange', { name: user.name, status: statusText }))) return

  try {
    await userApi.changeStatus(user.id, { status: newStatus })
    loadUsers()
  } catch (error) {
    alert(error.response?.data?.message || t('admin.statusChangeError'))
  }
}

function openRoleDialog(user) {
  roleTarget.value = { ...user }
  newRole.value = ''
  roleError.value = ''
  showRoleModal.value = true
}

function closeRoleModal() {
  showRoleModal.value = false
  roleTarget.value = null
}

async function addRoleToUser() {
  if (!newRole.value || !roleTarget.value) return
  roleError.value = ''
  try {
    await userApi.assignRole(roleTarget.value.id, { role: newRole.value })
    // Update local state
    if (!roleTarget.value.roles) roleTarget.value.roles = []
    roleTarget.value.roles.push(newRole.value)
    newRole.value = ''
    loadUsers()
  } catch (error) {
    roleError.value = error.response?.data?.message || t('admin.roleAddError')
  }
}

async function removeRoleFromUser(role) {
  if (!roleTarget.value) return
  if (!confirm(t('message.deleteConfirm'))) return
  roleError.value = ''
  try {
    await userApi.removeRole(roleTarget.value.id, role)
    roleTarget.value.roles = roleTarget.value.roles.filter(r => r !== role)
    loadUsers()
  } catch (error) {
    roleError.value = error.response?.data?.message || t('admin.roleRemoveError')
  }
}
</script>

<style scoped>
.account-manage {
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

/* Filter Bar */
.filter-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.filter-label {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.filter-select {
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  outline: none;
}

.filter-select:focus {
  border-color: var(--color-primary);
}

.search-group {
  margin-left: auto;
}

.filter-input {
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  width: 220px;
  outline: none;
}

.filter-input:focus {
  border-color: var(--color-primary);
}

/* Table */
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

/* Status Badge */
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

.status-locked {
  background-color: var(--color-badge-red-bg);
  color: var(--color-danger);
}

.status-resigned {
  background-color: var(--color-badge-orange-bg);
  color: var(--color-badge-orange);
}

.status-deleted {
  background-color: var(--color-badge-gray-bg);
  color: var(--color-text-disabled);
}

/* Role Tags */
.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.role-tag {
  display: inline-block;
  padding: 1px 6px;
  background-color: var(--color-primary-bg);
  color: var(--color-primary);
  border-radius: 4px;
  font-size: var(--font-size-xs);
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 4px;
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

.btn-default {
  background-color: var(--color-bg-white);
  color: var(--color-text);
  border-color: var(--color-border);
}

.btn-default:hover {
  background-color: var(--color-bg);
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: var(--spacing-md);
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-white);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background-color: var(--color-bg);
}

.page-btn.active {
  background-color: var(--color-primary);
  color: var(--color-text-inverse);
  border-color: var(--color-primary);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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

.modal-body .form-input:disabled {
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

/* Role Modal Specific */
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
</style>
