<template>
  <div class="board-post-form">
    <div class="page-header">
      <h2>{{ isEdit ? '게시글 수정' : '게시글 작성' }}</h2>
    </div>

    <form @submit.prevent="handleSubmit" class="form-card">
      <div class="form-group">
        <label class="required">제목</label>
        <input v-model="form.title" type="text" required placeholder="제목을 입력하세요" />
      </div>

      <div class="form-group">
        <label class="required">내용</label>
        <textarea v-model="form.content" rows="10" required placeholder="내용을 입력하세요"></textarea>
      </div>

      <div class="form-group">
        <label>
          <input v-model="isNotice" type="checkbox" /> 공지사항으로 등록
        </label>
      </div>

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="$router.back()">취소</button>
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? '처리중...' : (isEdit ? '수정' : '등록') }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { boardApi } from '@/api/board.js'

const route = useRoute()
const router = useRouter()
const boardId = route.params.boardId
const postId = route.params.postId

const isEdit = computed(() => !!postId)
const submitting = ref(false)
const isNotice = ref(false)

const form = ref({
  title: '',
  content: ''
})

const loadDetail = async () => {
  if (!isEdit.value) return
  try {
    const res = await boardApi.getPost(boardId, postId)
    const data = res.data.data || res.data
    form.value.title = data.title
    form.value.content = data.content
    isNotice.value = data.isNotice === 'Y'
  } catch (e) {
    console.error('게시글 조회 실패:', e)
    alert('게시글을 불러올 수 없습니다.')
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const payload = {
      title: form.value.title,
      content: form.value.content,
      isNotice: isNotice.value ? 'Y' : 'N'
    }
    if (isEdit.value) {
      await boardApi.updatePost(boardId, postId, payload)
      router.push(`/boards/${boardId}/posts/${postId}`)
    } else {
      const res = await boardApi.createPost(boardId, payload)
      const data = res.data.data || res.data
      router.push(`/boards/${boardId}/posts/${data.postId}`)
    }
  } catch (e) {
    console.error('게시글 저장 실패:', e)
    alert('저장에 실패했습니다.')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.board-post-form { padding: var(--spacing-lg); max-width: 800px; }
.page-header { margin-bottom: var(--spacing-lg); }
.page-header h2 { margin: 0; font-size: var(--font-size-xl); }
.form-card {
  background: #fff; border: 1px solid var(--color-border);
  border-radius: 8px; padding: var(--spacing-lg);
}
.form-group { margin-bottom: var(--spacing-md); }
.form-group label {
  display: block; margin-bottom: var(--spacing-xs);
  font-weight: 600; font-size: var(--font-size-sm);
}
.form-group label.required::after { content: ' *'; color: #dc2626; }
.form-group input[type="text"], .form-group textarea {
  width: 100%; padding: 8px 12px; border: 1px solid var(--color-border);
  border-radius: 4px; font-size: var(--font-size-sm); box-sizing: border-box;
}
.form-group textarea { resize: vertical; }
.form-group input[type="checkbox"] { margin-right: 6px; }
.form-actions {
  display: flex; justify-content: flex-end; gap: var(--spacing-sm);
  margin-top: var(--spacing-lg); padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border);
}
.btn {
  padding: 8px 20px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: #fff; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
</style>
