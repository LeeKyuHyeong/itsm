<template>
  <div class="board-manage">
    <div class="page-header">
      <h2>게시판 관리</h2>
      <button class="btn btn-primary" @click="openCreateModal">게시판 추가</button>
    </div>

    <table class="data-table" v-if="boards.length">
      <thead>
        <tr>
          <th width="60">ID</th>
          <th>게시판명</th>
          <th width="100">유형</th>
          <th width="80">댓글허용</th>
          <th width="80">상태</th>
          <th width="60">순서</th>
          <th width="150">허용확장자</th>
          <th width="100">파일크기(MB)</th>
          <th width="120">작업</th>
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
              {{ b.isActive === 'Y' ? '활성' : '비활성' }}
            </span>
          </td>
          <td>{{ b.sortOrder }}</td>
          <td>{{ b.allowExt || '-' }}</td>
          <td>{{ b.maxFileSize || '-' }}</td>
          <td>
            <button class="btn-link" @click="openEditModal(b)">수정</button>
            <button class="btn-link text-danger" @click="handleDelete(b.boardId)">삭제</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="empty-msg">등록된 게시판이 없습니다.</p>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ isEditMode ? '게시판 수정' : '게시판 추가' }}</h3>
        <form @submit.prevent="handleSave">
          <div class="form-group">
            <label class="required">게시판명</label>
            <input v-model="form.boardNm" required />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="required">유형코드</label>
              <input v-model="form.boardTypeCd" required />
            </div>
            <div class="form-group">
              <label>정렬순서</label>
              <input v-model.number="form.sortOrder" type="number" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>허용 확장자</label>
              <input v-model="form.allowExt" placeholder="pdf,doc,hwp" />
            </div>
            <div class="form-group">
              <label>최대 파일크기(MB)</label>
              <input v-model.number="form.maxFileSize" type="number" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>댓글 허용</label>
              <select v-model="form.allowComment">
                <option value="Y">허용</option>
                <option value="N">비허용</option>
              </select>
            </div>
            <div class="form-group">
              <label>활성 상태</label>
              <select v-model="form.isActive">
                <option value="Y">활성</option>
                <option value="N">비활성</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label class="required">권한 설정 (JSON)</label>
            <textarea v-model="form.rolePermission" rows="3" required></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="showModal = false">취소</button>
            <button type="submit" class="btn btn-primary">{{ isEditMode ? '수정' : '추가' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { boardApi } from '@/api/board.js'

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
    alert('저장에 실패했습니다.')
  }
}

const handleDelete = async (boardId) => {
  if (!confirm('게시판을 삭제하시겠습니까?')) return
  try {
    await boardApi.deleteConfig(boardId)
    loadBoards()
  } catch (e) {
    alert('삭제에 실패했습니다.')
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
.data-table { width: 100%; border-collapse: collapse; background: #fff; }
.data-table th, .data-table td {
  padding: 8px 12px; text-align: left;
  border-bottom: 1px solid var(--color-border); font-size: var(--font-size-sm);
}
.data-table th { font-weight: 600; background: var(--color-bg-secondary); }
.status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 10px;
  font-size: var(--font-size-xs); font-weight: 600;
}
.status-badge.active { background: #dcfce7; color: #16a34a; }
.status-badge.inactive { background: #f3f4f6; color: #6b7280; }
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal {
  background: #fff; border-radius: 8px; padding: var(--spacing-lg);
  width: 600px; max-height: 80vh; overflow-y: auto;
}
.modal h3 { margin: 0 0 var(--spacing-md); }
.form-group { margin-bottom: var(--spacing-sm); }
.form-group label {
  display: block; margin-bottom: 4px; font-weight: 600; font-size: var(--font-size-sm);
}
.form-group label.required::after { content: ' *'; color: #dc2626; }
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
.btn-primary { background: var(--color-primary); color: #fff; }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
.btn-link {
  background: none; border: none; cursor: pointer;
  color: var(--color-primary); font-size: var(--font-size-xs); padding: 2px 4px;
}
.text-danger { color: #dc2626; }
</style>
