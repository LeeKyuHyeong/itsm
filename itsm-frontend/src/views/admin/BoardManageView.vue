<template>
  <div class="board-manage">
    <div class="page-header">
      <h2>{{ t('admin.boardManage') }}</h2>
      <button class="btn btn-primary" @click="openCreateModal">{{ t('admin.boardAdd') }}</button>
    </div>

    <table class="data-table" v-if="boards.length">
      <thead>
        <tr>
          <th width="60">ID</th>
          <th>{{ t('admin.boardName') }}</th>
          <th width="100">{{ t('admin.boardType') }}</th>
          <th width="80">{{ t('admin.allowComment') }}</th>
          <th width="80">{{ t('admin.isActive') }}</th>
          <th width="60">{{ t('admin.sortOrder') }}</th>
          <th width="150">{{ t('admin.allowExt') }}</th>
          <th width="100">{{ t('admin.maxFileSize') }}</th>
          <th width="120">{{ t('common.edit') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="b in boards" :key="b.boardId">
          <td>{{ b.boardId }}</td>
          <td>{{ b.boardNm }}</td>
          <td>{{ b.boardTypeCd }}</td>
          <td>{{ b.allowComment === 'Y' ? 'O' : 'X' }}</td>
          <td>
            <span :class="['status-badge', b.isActive === 'Y' ? 'active' : 'inactive']">
              {{ b.isActive === 'Y' ? t('admin.active') : t('admin.inactive') }}
            </span>
          </td>
          <td>{{ b.sortOrder }}</td>
          <td>{{ b.allowExt || '-' }}</td>
          <td>{{ b.maxFileSize || '-' }}</td>
          <td>
            <button class="btn-link" @click="openEditModal(b)">{{ t('common.edit') }}</button>
            <button class="btn-link text-danger" @click="handleDelete(b.boardId)">{{ t('common.delete') }}</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="empty-msg">{{ t('common.noData') }}</p>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ isEditMode ? t('admin.boardEdit') : t('admin.boardAdd') }}</h3>
        <form @submit.prevent="handleSave">
          <div class="form-group">
            <label class="required">{{ t('admin.boardName') }}</label>
            <input v-model="form.boardNm" required />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="required">{{ t('admin.boardType') }}</label>
              <input v-model="form.boardTypeCd" required />
            </div>
            <div class="form-group">
              <label>{{ t('admin.sortOrder') }}</label>
              <input v-model.number="form.sortOrder" type="number" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>{{ t('admin.allowExt') }}</label>
              <input v-model="form.allowExt" placeholder="pdf,doc,hwp" />
            </div>
            <div class="form-group">
              <label>{{ t('admin.maxFileSize') }}</label>
              <input v-model.number="form.maxFileSize" type="number" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>{{ t('admin.allowComment') }}</label>
              <select v-model="form.allowComment">
                <option value="Y">{{ t('admin.allow') }}</option>
                <option value="N">{{ t('admin.disallow') }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>{{ t('admin.isActive') }}</label>
              <select v-model="form.isActive">
                <option value="Y">{{ t('admin.active') }}</option>
                <option value="N">{{ t('admin.inactive') }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label class="required">{{ t('admin.rolePermission') }}</label>
            <textarea v-model="form.rolePermission" rows="3" required></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="showModal = false">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary">{{ isEditMode ? t('common.edit') : t('common.add') }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { boardApi } from '@/api/board.js'

const { t } = useI18n()
const boards = ref([])
const showModal = ref(false)
const isEditMode = ref(false)
const editBoardId = ref(null)

const form = reactive({
  boardNm: '',
  boardTypeCd: '',
  allowExt: '',
  maxFileSize: null,
  allowComment: 'Y',
  rolePermission: '{"read":["USER"],"write":["USER"]}',
  isActive: 'Y',
  sortOrder: 0
})

const resetForm = () => {
  form.boardNm = ''
  form.boardTypeCd = ''
  form.allowExt = ''
  form.maxFileSize = null
  form.allowComment = 'Y'
  form.rolePermission = '{"read":["USER"],"write":["USER"]}'
  form.isActive = 'Y'
  form.sortOrder = 0
}

const loadBoards = async () => {
  try {
    const res = await boardApi.getConfigs()
    boards.value = (res.data.data || res.data) || []
  } catch (e) {
    console.error('게시판 목록 조회 실패:', e)
  }
}

const openCreateModal = () => {
  resetForm()
  isEditMode.value = false
  editBoardId.value = null
  showModal.value = true
}

const openEditModal = (b) => {
  form.boardNm = b.boardNm
  form.boardTypeCd = b.boardTypeCd
  form.allowExt = b.allowExt || ''
  form.maxFileSize = b.maxFileSize
  form.allowComment = b.allowComment
  form.rolePermission = b.rolePermission || '{}'
  form.isActive = b.isActive
  form.sortOrder = b.sortOrder
  isEditMode.value = true
  editBoardId.value = b.boardId
  showModal.value = true
}

const handleSave = async () => {
  try {
    const payload = { ...form }
    if (isEditMode.value) {
      await boardApi.updateConfig(editBoardId.value, payload)
    } else {
      await boardApi.createConfig(payload)
    }
    showModal.value = false
    loadBoards()
  } catch (e) {
    alert(t('message.saveFail'))
  }
}

const handleDelete = async (boardId) => {
  if (!confirm(t('message.deleteConfirm'))) return
  try {
    await boardApi.deleteConfig(boardId)
    loadBoards()
  } catch (e) {
    alert(t('message.deleteFail'))
  }
}

onMounted(loadBoards)
</script>

<style scoped>
.board-manage { padding: var(--spacing-lg); }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--spacing-lg);
}
.page-header h2 { margin: 0; font-size: var(--font-size-xl); }
.data-table { width: 100%; border-collapse: collapse; background: var(--color-bg-white); }
.data-table th, .data-table td {
  padding: 8px 12px; text-align: left;
  border-bottom: 1px solid var(--color-border); font-size: var(--font-size-sm);
}
.data-table th { font-weight: 600; background: var(--color-bg-secondary); }
.status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 10px;
  font-size: var(--font-size-xs); font-weight: 600;
}
.status-badge.active { background: var(--color-btn-success-bg); color: var(--color-btn-success); }
.status-badge.inactive { background: var(--color-badge-gray-bg); color: var(--color-text-secondary); }
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: var(--color-overlay); display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal {
  background: var(--color-form-bg); border-radius: 8px; padding: var(--spacing-lg);
  width: 600px; max-height: 80vh; overflow-y: auto;
}
.modal h3 { margin: 0 0 var(--spacing-md); }
.form-group { margin-bottom: var(--spacing-sm); }
.form-group label {
  display: block; margin-bottom: 4px; font-weight: 600; font-size: var(--font-size-sm);
}
.form-group label.required::after { content: ' *'; color: var(--color-btn-danger); }
.form-group input, .form-group select, .form-group textarea {
  width: 100%; padding: 6px 10px; border: 1px solid var(--color-border);
  border-radius: 4px; font-size: var(--font-size-sm); box-sizing: border-box;
}
.form-group textarea { resize: vertical; font-family: monospace; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-sm); }
.modal-actions {
  display: flex; justify-content: flex-end; gap: var(--spacing-sm);
  margin-top: var(--spacing-md); padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border);
}
.empty-msg { color: var(--color-text-muted); font-size: var(--font-size-sm); }
.btn {
  padding: 6px 16px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: var(--color-text-inverse); }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
.btn-link {
  background: none; border: none; cursor: pointer;
  color: var(--color-primary); font-size: var(--font-size-xs); padding: 2px 4px;
}
.text-danger { color: var(--color-btn-danger); }
</style>
