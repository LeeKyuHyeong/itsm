<template>
  <div class="menu-manage">
    <div class="page-header">
      <h1 class="page-title">{{ t('admin.menuManage') }}</h1>
      <button class="btn btn-primary" @click="openDialog()">+ {{ t('common.add') }}</button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('admin.menuName') }}</th>
            <th>{{ t('admin.menuNameEn') }}</th>
            <th>{{ t('admin.path') }}</th>
            <th>{{ t('admin.icon') }}</th>
            <th>{{ t('admin.sortOrder') }}</th>
            <th>{{ t('admin.useYn') }}</th>
            <th>{{ t('admin.manage') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center">{{ t('common.loading') }}</td>
          </tr>
          <tr v-else-if="flatMenus.length === 0">
            <td colspan="7" class="text-center">{{ t('admin.noMenus') }}</td>
          </tr>
          <tr v-for="menu in flatMenus" :key="menu.id">
            <td>
              <span :style="{ paddingLeft: (menu.depth || 0) * 24 + 'px' }">
                {{ menu.depth > 0 ? 'ㄴ ' : '' }}{{ menu.name }}
              </span>
            </td>
            <td>{{ menu.nameEn || '-' }}</td>
            <td>{{ menu.path || '-' }}</td>
            <td>{{ menu.icon || '-' }}</td>
            <td>{{ menu.sortOrder }}</td>
            <td>
              <span class="status-badge" :class="menu.active !== false ? 'status-active' : 'status-inactive'">
                {{ menu.active !== false ? t('admin.inUse') : t('admin.notInUse') }}
              </span>
            </td>
            <td>
              <button class="btn btn-sm btn-default" @click="openDialog(menu)">{{ t('common.edit') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Menu Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">{{ editing ? t('admin.menuEdit') : t('admin.menuAdd') }}</h2>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="save">
          <div class="form-group">
            <label class="form-label">{{ t('admin.menuName') }}</label>
            <input v-model="form.name" type="text" class="form-input" required />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.menuNameEn') }}</label>
            <input v-model="form.nameEn" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.path') }}</label>
            <input v-model="form.path" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.icon') }}</label>
            <input v-model="form.icon" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.sortOrder') }}</label>
            <input v-model.number="form.sortOrder" type="number" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('admin.parentMenu') }}</label>
            <select v-model="form.parentId" class="form-input">
              <option value="">{{ t('admin.noneTopLevel') }}</option>
              <option
                v-for="m in topMenus"
                :key="m.id"
                :value="m.id"
              >
                {{ m.name }}
              </option>
            </select>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { menuApi } from '@/api/admin/menu.js'

const { t } = useI18n()

const menus = ref([])
const loading = ref(false)

// Modal
const showModal = ref(false)
const editing = ref(null)
const saving = ref(false)
const saveError = ref('')
const form = reactive({
  name: '',
  nameEn: '',
  path: '',
  icon: '',
  sortOrder: 0,
  parentId: ''
})

const topMenus = computed(() => {
  return menus.value.filter(m => !m.parentId && m.id !== editing.value?.id)
})

const flatMenus = computed(() => {
  const result = []
  const topLevel = menus.value.filter(m => !m.parentId)
  topLevel.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))

  for (const menu of topLevel) {
    result.push({ ...menu, depth: 0 })
    const children = menus.value
      .filter(m => m.parentId === menu.id)
      .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    for (const child of children) {
      result.push({ ...child, depth: 1 })
    }
  }
  return result
})

onMounted(() => {
  loadMenus()
})

async function loadMenus() {
  loading.value = true
  try {
    const { data } = await menuApi.getList()
    const result = data.data || data
    if (Array.isArray(result)) {
      const flat = []
      function flatten(list, parentId) {
        for (const item of list) {
          const { children, ...rest } = item
          flat.push({
            id: rest.menuId,
            name: rest.menuNm,
            nameEn: rest.menuNmEn || '',
            path: rest.menuUrl,
            icon: rest.icon,
            sortOrder: rest.sortOrder,
            active: rest.isVisible !== 'N',
            parentId: parentId || null
          })
          if (children && children.length > 0) {
            flatten(children, rest.menuId)
          }
        }
      }
      flatten(result, null)
      menus.value = flat
    } else {
      const list = result.content || result.items || result || []
      menus.value = list
    }
  } catch (error) {
    console.error('메뉴 목록 로드 실패:', error)
    menus.value = []
  } finally {
    loading.value = false
  }
}

function openDialog(menu = null) {
  editing.value = menu
  saveError.value = ''
  if (menu) {
    Object.assign(form, {
      name: menu.name || '',
      nameEn: menu.nameEn || '',
      path: menu.path || '',
      icon: menu.icon || '',
      sortOrder: menu.sortOrder || 0,
      parentId: menu.parentId || ''
    })
  } else {
    Object.assign(form, {
      name: '',
      nameEn: '',
      path: '',
      icon: '',
      sortOrder: 0,
      parentId: ''
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
    if (!payload.parentId) delete payload.parentId

    if (editing.value) {
      await menuApi.update(editing.value.id, payload)
    } else {
      await menuApi.create(payload)
    }
    closeModal()
    loadMenus()
  } catch (error) {
    saveError.value = error.response?.data?.message || t('message.saveFail')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.menu-manage {
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
