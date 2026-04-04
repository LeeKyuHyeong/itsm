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
      <div class="filter-group">
        <label class="filter-label">{{ t('admin.department') }}</label>
        <select v-model="filters.deptId" class="filter-select" @change="loadUsers">
          <option value="">{{ t('common.all') }}</option>
          <option v-for="d in allDepartments" :key="d.deptId" :value="d.deptId">
            {{ d.deptNm }}
          </option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label">{{ t('admin.role') }}</label>
        <select v-model="filters.roleCd" class="filter-select" @change="loadUsers">
          <option value="">{{ t('common.all') }}</option>
          <option v-for="(label, code) in allRoles" :key="code" :value="code">
            {{ label }}
          </option>
        </select>
      </div>
      <div class="filter-group search-group">
        <input
          v-model="filters.keyword"
          type="text"
          class="filter-input"
          :placeholder="t('admin.searchByNameIdEmail')"
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
    <UserFormModal
      :show="showUserModal"
      :is-editing="isEditing"
      :form="userForm"
      :companies="companies"
      :departments="departments"
      :saving="saving"
      :error="saveError"
      @close="closeUserModal"
      @save="saveUser"
      @load-departments="onLoadDepartments"
      @update:form="onUpdateUserForm"
    />

    <!-- Role Assignment Modal -->
    <RoleManageModal
      :show="showRoleModal"
      :user="roleTarget"
      :available-roles="availableRoles"
      :saving="false"
      :error="roleError"
      @close="closeRoleModal"
      @add-role="addRoleToUser"
      @remove-role="removeRoleFromUser"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { userApi } from '@/api/user.js'
import { companyApi } from '@/api/company.js'
import { ROLES } from '@/constants/roles.js'
import { useToast } from '@/composables/useToast.js'
import { useConfirm } from '@/composables/useConfirm.js'
import UserFormModal from './components/UserFormModal.vue'
import RoleManageModal from './components/RoleManageModal.vue'

const toast = useToast()
const { confirm } = useConfirm()

const { t, te } = useI18n()

const users = ref([])
const loading = ref(false)
const saving = ref(false)
const saveError = ref('')
const companies = ref([])
const departments = ref([])

const filters = reactive({
  status: '',
  deptId: '',
  roleCd: '',
  keyword: ''
})

const allDepartments = ref([])
const allRoles = computed(() => {
  const result = {}
  for (const roleCode of Object.values(ROLES)) {
    result[roleCode] = roleLabel(roleCode)
  }
  return result
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
  loadAllDepartments()
})

async function loadUsers() {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    if (filters.status) params.status = filters.status
    if (filters.deptId) params.deptId = filters.deptId
    if (filters.roleCd) params.roleCd = filters.roleCd
    if (filters.keyword) params.keyword = filters.keyword

    const { data } = await userApi.getList(params)
    const result = data.data || data
    const list = result.content || result.items || result || []
    users.value = list.map(u => ({
      ...u,
      id: u.userId ?? u.id,
      name: u.userNm ?? u.name ?? '',
      departmentName: u.deptName ?? u.departmentName ?? '',
      phone: u.tel ?? u.phone ?? ''
    }))
    pagination.total = result.totalElements || result.total || 0
  } catch (error) {
    console.error('Failed to load user list:', error)
    users.value = []
  } finally {
    loading.value = false
  }
}

async function loadCompanies() {
  try {
    const { data } = await companyApi.getList({ size: 100 })
    const result = data.data || data
    const list = result.content || result.items || result || []
    companies.value = list.map(c => ({
      ...c,
      id: c.companyId ?? c.id,
      name: c.companyNm ?? c.name ?? ''
    }))
  } catch (error) {
    console.error('Failed to load company list:', error)
  }
}

async function loadAllDepartments() {
  try {
    const { data } = await companyApi.getList({ size: 100 })
    const result = data.data || data
    const companyList = result.content || result.items || result || []
    const deptPromises = companyList.map(c => {
      const companyId = c.companyId ?? c.id
      return companyApi.getDepartments(companyId).then(res => {
        const depts = res.data.data || res.data || []
        return (Array.isArray(depts) ? depts : []).map(d => ({
          deptId: d.deptId ?? d.id,
          deptNm: d.deptNm ?? d.name ?? ''
        }))
      }).catch(() => [])
    })
    const deptArrays = await Promise.all(deptPromises)
    allDepartments.value = deptArrays.flat()
  } catch (error) {
    console.error('Failed to load all departments:', error)
  }
}

async function loadDepartments() {
  departments.value = []
  if (!userForm.companyId) return
  try {
    const { data } = await companyApi.getDepartments(userForm.companyId)
    const list = data.data || data || []
    departments.value = (Array.isArray(list) ? list : []).map(d => ({
      ...d,
      id: d.deptId ?? d.id,
      name: d.deptNm ?? d.name ?? ''
    }))
  } catch (error) {
    console.error('Failed to load departments:', error)
  }
}

function onLoadDepartments(companyId) {
  userForm.companyId = companyId
  loadDepartments()
}

function onUpdateUserForm(newForm) {
  Object.assign(userForm, newForm)
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
  if (!await confirm({ message: t('admin.confirmStatusChange', { name: user.name, status: statusText }) })) return

  try {
    await userApi.changeStatus(user.id, { status: newStatus })
    loadUsers()
  } catch (error) {
    toast.error(error.response?.data?.message || t('admin.statusChangeError'))
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

async function addRoleToUser(roleCode) {
  const role = roleCode || newRole.value
  if (!role || !roleTarget.value) return
  roleError.value = ''
  try {
    await userApi.assignRole(roleTarget.value.id, { role })
    // Update local state
    if (!roleTarget.value.roles) roleTarget.value.roles = []
    roleTarget.value.roles.push(role)
    newRole.value = ''
    loadUsers()
  } catch (error) {
    roleError.value = error.response?.data?.message || t('admin.roleAddError')
  }
}

async function removeRoleFromUser(role) {
  if (!roleTarget.value) return
  if (!await confirm({ message: t('message.deleteConfirm') })) return
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

</style>
