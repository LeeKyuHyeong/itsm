<template>
  <div class="board-post-detail">
    <div class="page-header">
      <h2>
        <span v-if="post.isNotice === 'Y'" class="notice-badge">[공지]</span>
        {{ post.title }}
      </h2>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="$router.push(`/boards/${boardId}`)">목록</button>
        <button class="btn btn-secondary" @click="editPost">수정</button>
        <button class="btn btn-danger" @click="deletePost">삭제</button>
      </div>
    </div>

    <div class="post-meta">
      <span>조회 {{ post.viewCnt }}</span>
      <span>{{ formatDate(post.createdAt) }}</span>
    </div>

    <div class="post-content">
      <p>{{ post.content }}</p>
    </div>

    <div class="comments-section" v-if="allowComment">
      <h3>댓글 ({{ comments.length }})</h3>

      <div class="comment-form">
        <textarea v-model="newComment" rows="2" placeholder="댓글을 입력하세요"></textarea>
        <button class="btn btn-primary btn-sm" @click="addComment" :disabled="!newComment.trim()">등록</button>
      </div>

      <div v-for="c in comments" :key="c.commentId" class="comment-item">
        <div class="comment-header">
          <span class="comment-author">사용자 #{{ c.createdBy }}</span>
          <span class="comment-date">{{ formatDate(c.createdAt) }}</span>
        </div>
        <div v-if="editingCommentId === c.commentId" class="comment-edit">
          <textarea v-model="editCommentContent" rows="2"></textarea>
          <div class="comment-edit-actions">
            <button class="btn btn-primary btn-xs" @click="saveEditComment(c.commentId)">저장</button>
            <button class="btn btn-secondary btn-xs" @click="editingCommentId = null">취소</button>
          </div>
        </div>
        <div v-else class="comment-body">
          <p>{{ c.content }}</p>
          <div class="comment-actions">
            <button class="btn-link" @click="startEditComment(c)">수정</button>
            <button class="btn-link text-danger" @click="deleteComment(c.commentId)">삭제</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { boardApi } from '@/api/board.js'

const route = useRoute()
const router = useRouter()
const boardId = route.params.boardId
const postId = route.params.postId

const post = ref({})
const comments = ref([])
const newComment = ref('')
const allowComment = ref(true)
const editingCommentId = ref(null)
const editCommentContent = ref('')

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const loadPost = async () => {
  try {
    const res = await boardApi.getPost(boardId, postId)
    post.value = res.data.data || res.data
  } catch (e) {
    console.error('게시글 조회 실패:', e)
    alert('게시글을 불러올 수 없습니다.')
  }
}

const loadComments = async () => {
  try {
    const res = await boardApi.getComments(boardId, postId)
    comments.value = (res.data.data || res.data) || []
  } catch (e) {
    console.error('댓글 조회 실패:', e)
  }
}

const addComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await boardApi.addComment(boardId, postId, { content: newComment.value })
    newComment.value = ''
    loadComments()
  } catch (e) {
    alert('댓글 등록에 실패했습니다.')
  }
}

const startEditComment = (comment) => {
  editingCommentId.value = comment.commentId
  editCommentContent.value = comment.content
}

const saveEditComment = async (commentId) => {
  try {
    await boardApi.updateComment(boardId, postId, commentId, { content: editCommentContent.value })
    editingCommentId.value = null
    loadComments()
  } catch (e) {
    alert('댓글 수정에 실패했습니다.')
  }
}

const deleteComment = async (commentId) => {
  if (!confirm('댓글을 삭제하시겠습니까?')) return
  try {
    await boardApi.deleteComment(boardId, postId, commentId)
    loadComments()
  } catch (e) {
    alert('댓글 삭제에 실패했습니다.')
  }
}

const editPost = () => {
  router.push(`/boards/${boardId}/posts/${postId}?edit=true`)
}

const deletePost = async () => {
  if (!confirm('게시글을 삭제하시겠습니까?')) return
  try {
    await boardApi.deletePost(boardId, postId)
    router.push(`/boards/${boardId}`)
  } catch (e) {
    alert('게시글 삭제에 실패했습니다.')
  }
}

onMounted(async () => {
  await loadPost()
  if (allowComment.value) {
    loadComments()
  }
})
</script>

<style scoped>
.board-post-detail { padding: var(--spacing-lg); max-width: 900px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: var(--spacing-sm);
}
.page-header h2 { margin: 0; font-size: var(--font-size-xl); }
.notice-badge { color: #dc2626; font-weight: 600; }
.header-actions { display: flex; gap: var(--spacing-xs); flex-shrink: 0; }
.post-meta {
  display: flex; gap: var(--spacing-md); color: var(--color-text-muted);
  font-size: var(--font-size-sm); margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-sm); border-bottom: 1px solid var(--color-border);
}
.post-content {
  background: #fff; border: 1px solid var(--color-border);
  border-radius: 8px; padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg); min-height: 200px;
  white-space: pre-wrap; word-break: break-word;
}
.post-content p { margin: 0; }
.comments-section {
  background: #fff; border: 1px solid var(--color-border);
  border-radius: 8px; padding: var(--spacing-md);
}
.comments-section h3 { margin: 0 0 var(--spacing-md); font-size: var(--font-size-md); }
.comment-form { display: flex; gap: var(--spacing-sm); margin-bottom: var(--spacing-md); align-items: flex-start; }
.comment-form textarea {
  flex: 1; padding: 8px; border: 1px solid var(--color-border);
  border-radius: 4px; font-size: var(--font-size-sm); resize: vertical;
}
.comment-item {
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid var(--color-border);
}
.comment-item:last-child { border-bottom: none; }
.comment-header {
  display: flex; justify-content: space-between; margin-bottom: 4px;
}
.comment-author { font-weight: 600; font-size: var(--font-size-sm); }
.comment-date { font-size: var(--font-size-xs); color: var(--color-text-muted); }
.comment-body { font-size: var(--font-size-sm); }
.comment-body p { margin: 0 0 4px; }
.comment-actions { display: flex; gap: var(--spacing-sm); }
.comment-edit textarea {
  width: 100%; padding: 6px; border: 1px solid var(--color-border);
  border-radius: 4px; font-size: var(--font-size-sm); margin-bottom: 4px; box-sizing: border-box;
}
.comment-edit-actions { display: flex; gap: var(--spacing-xs); }
.btn {
  padding: 6px 16px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: #fff; }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
.btn-danger { background: #fee2e2; color: #dc2626; border: 1px solid #fca5a5; }
.btn-sm { padding: 4px 12px; font-size: var(--font-size-xs); }
.btn-xs { padding: 3px 8px; font-size: var(--font-size-xs); }
.btn-link {
  background: none; border: none; cursor: pointer;
  color: var(--color-primary); font-size: var(--font-size-xs); padding: 0;
}
.text-danger { color: #dc2626; }
</style>
