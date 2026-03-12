<template>
  <div class="change-detail" v-if="change">
    <div class="page-header">
      <h2>변경요청 상세 - #{{ change.changeId }}</h2>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="$router.push('/changes')">목록</button>
        <button v-if="canEdit" class="btn btn-secondary" @click="$router.push(`/changes/${change.changeId}/edit`)">수정</button>
      </div>
    </div>

    <!-- 상태머신 버튼 -->
    <div class="status-actions">
      <span class="current-status">
        현재 상태: <BaseStatusBadge :status="change.statusCd" />
      </span>
      <div class="status-buttons">
        <button v-for="s in availableTransitions" :key="s.status"
                :class="['btn', s.class]" @click="handleChangeStatus(s.status)">
          {{ s.label }}
        </button>
      </div>
    </div>

    <!-- 기본 정보 -->
    <div class="detail-card">
      <h3>기본 정보</h3>
      <div class="info-grid">
        <div class="info-item">
          <label>제목</label>
          <span>{{ change.title }}</span>
        </div>
        <div class="info-item">
          <label>변경 유형</label>
          <span>{{ commonCodeStore.getCodeName('CHANGE_TYPE', change.changeTypeCd) || change.changeTypeCd }}</span>
        </div>
        <div class="info-item">
          <label>우선순위</label>
          <span :class="['priority-badge', `priority-${change.priorityCd}`]">{{ priorityLabel(change.priorityCd) }}</span>
        </div>
        <div class="info-item">
          <label>고객사</label>
          <span>{{ change.companyNm }}</span>
        </div>
        <div class="info-item">
          <label>변경 예정일시</label>
          <span>{{ formatDate(change.scheduledAt) }}</span>
        </div>
        <div class="info-item">
          <label>승인 현황</label>
          <span>{{ change.approvedCount || 0 }} / {{ change.approverCount || 0 }}명 승인</span>
        </div>
        <div class="info-item full-width">
          <label>변경 내용</label>
          <div class="content-box">{{ change.content }}</div>
        </div>
        <div class="info-item full-width" v-if="change.rollbackPlan">
          <label>롤백 계획</label>
          <div class="content-box">{{ change.rollbackPlan }}</div>
        </div>
      </div>
    </div>

    <!-- 승인 현황 -->
    <div class="detail-card">
      <div class="card-header">
        <h3>승인자 현황</h3>
        <button v-if="canEdit" class="btn btn-sm" @click="showApproverModal = true">승인자 추가</button>
      </div>
      <div v-if="approvers.length === 0" class="empty-state">지정된 승인자가 없습니다.</div>
      <div v-else class="approver-list">
        <div v-for="a in approvers" :key="a.userId" class="approver-item">
          <span class="approver-order">#{{ a.approveOrder }}</span>
          <span class="approver-name">{{ a.userNm }}</span>
          <span :class="['approve-status', `status-${a.approveStatus}`]">{{ approveStatusLabel(a.approveStatus) }}</span>
          <span v-if="a.approvedAt" class="approver-date">{{ formatDate(a.approvedAt) }}</span>
          <span v-if="a.comment" class="approver-comment">{{ a.comment }}</span>
          <template v-if="change.statusCd === 'APPROVAL_REQUESTED' && a.approveStatus === 'PENDING'">
            <button class="btn-link" @click="handleApprove(a.userId, 'APPROVED')">승인</button>
            <button class="btn-link danger" @click="handleApprove(a.userId, 'REJECTED')">반려</button>
          </template>
          <button v-if="canEdit && a.approveStatus === 'PENDING'" class="btn-link danger" @click="handleRemoveApprover(a.userId)">삭제</button>
        </div>
      </div>
    </div>

    <!-- 댓글 -->
    <div class="detail-card">
      <h3>댓글</h3>
      <div v-if="comments.length === 0" class="empty-state">등록된 댓글이 없습니다.</div>
      <div v-else class="comment-list">
        <div v-for="c in comments" :key="c.commentId" class="comment-item">
          <div class="comment-header">
            <span class="comment-author">사용자#{{ c.createdBy }}</span>
            <span class="comment-date">{{ formatDate(c.createdAt) }}</span>
            <button class="btn-link danger" @click="handleDeleteComment(c.commentId)">삭제</button>
          </div>
          <div class="comment-content">{{ c.content }}</div>
        </div>
      </div>
      <div class="comment-form">
        <textarea v-model="newComment" rows="2" placeholder="댓글을 입력하세요"></textarea>
        <button class="btn btn-primary btn-sm" @click="handleAddComment" :disabled="!newComment.trim()">등록</button>
      </div>
    </div>

    <!-- 변경 이력 -->
    <div class="detail-card">
      <h3>변경 이력</h3>
      <div v-if="histories.length === 0" class="empty-state">변경 이력이 없습니다.</div>
      <div v-else class="timeline">
        <div v-for="h in histories" :key="h.historyId" class="timeline-item">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <div class="timeline-field">{{ h.changedField }}</div>
            <div class="timeline-change">
              <span class="before">{{ h.beforeValue || '(없음)' }}</span>
              <span class="arrow">&rarr;</span>
              <span class="after">{{ h.afterValue || '(없음)' }}</span>
            </div>
            <div class="timeline-date">{{ formatDate(h.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 승인자 추가 모달 -->
    <BaseModal :show="showApproverModal" title="승인자 추가" @close="showApproverModal = false">
      <div class="form-group">
        <label>사용자 ID</label>
        <input v-model.number="approverUserId" type="number" placeholder="사용자 ID 입력" />
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showApproverModal = false">취소</button>
        <button class="btn btn-primary" @click="handleAddApprover">추가</button>
      </template>
    </BaseModal>

    <!-- 승인/반려 모달 -->
    <BaseModal :show="showApproveModal" :title="approveDecision === 'APPROVED' ? '승인' : '반려'" @close="showApproveModal = false">
      <div class="form-group">
        <label>의견</label>
        <textarea v-model="approveComment" rows="3" placeholder="의견을 입력하세요"></textarea>
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showApproveModal = false">취소</button>
        <button :class="['btn', approveDecision === 'APPROVED' ? 'btn-primary' : 'btn-danger']"
                @click="submitApprove">
          {{ approveDecision === 'APPROVED' ? '승인' : '반려' }}
        </button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { changeApi } from '@/api/change.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'
import BaseModal from '@/components/common/BaseModal.vue'

const route = useRoute()
const router = useRouter()
const commonCodeStore = useCommonCodeStore()
const changeId = computed(() => route.params.id)

const change = ref(null)
const approvers = ref([])
const comments = ref([])
const histories = ref([])
const newComment = ref('')
const approverUserId = ref(null)
const showApproverModal = ref(false)
const showApproveModal = ref(false)
const approveDecision = ref('')
const approveComment = ref('')
const approveTargetUserId = ref(null)

const STATUS_TRANSITIONS = {
  DRAFT: [
    { status: 'APPROVAL_REQUESTED', label: '승인요청', class: 'btn-primary' },
    { status: 'CANCELLED', label: '취소', class: 'btn-secondary' }
  ],
  APPROVED: [
    { status: 'IN_PROGRESS', label: '실행 시작', class: 'btn-primary' }
  ],
  IN_PROGRESS: [
    { status: 'COMPLETED', label: '완료', class: 'btn-success' }
  ],
  COMPLETED: [
    { status: 'CLOSED', label: '종료', class: 'btn-primary' }
  ],
  REJECTED: [
    { status: 'DRAFT', label: '초안으로 되돌리기', class: 'btn-primary' }
  ],
  APPROVAL_REQUESTED: [],
  CLOSED: [],
  CANCELLED: []
}

const availableTransitions = computed(() => {
  if (!change.value) return []
  return STATUS_TRANSITIONS[change.value.statusCd] || []
})

const canEdit = computed(() => {
  if (!change.value) return false
  return ['DRAFT', 'REJECTED'].includes(change.value.statusCd)
})

const priorityLabel = (code) => {
  const map = { CRITICAL: '긴급', HIGH: '높음', MEDIUM: '보통', LOW: '낮음' }
  return map[code] || code
}

const approveStatusLabel = (status) => {
  const map = { PENDING: '대기', APPROVED: '승인', REJECTED: '반려' }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const loadDetail = async () => {
  try {
    const res = await changeApi.getDetail(changeId.value)
    change.value = res.data.data || res.data
  } catch (e) {
    console.error('변경요청 조회 실패:', e)
    alert('변경요청 정보를 불러올 수 없습니다.')
  }
}

const loadApprovers = async () => {
  try {
    const res = await changeApi.getApprovers(changeId.value)
    approvers.value = res.data.data || res.data || []
  } catch (e) {
    approvers.value = []
  }
}

const loadComments = async () => {
  try {
    const res = await changeApi.getComments(changeId.value)
    comments.value = res.data.data || res.data || []
  } catch (e) {
    comments.value = []
  }
}

const loadHistory = async () => {
  try {
    const res = await changeApi.getHistory(changeId.value)
    histories.value = res.data.data || res.data || []
  } catch (e) {
    histories.value = []
  }
}

const handleChangeStatus = async (status) => {
  if (!confirm(`상태를 "${status}"(으)로 변경하시겠습니까?`)) return
  try {
    await changeApi.changeStatus(changeId.value, { status })
    await loadDetail()
    await loadHistory()
  } catch (e) {
    alert(e.response?.data?.error?.message || '상태 변경에 실패했습니다.')
  }
}

const handleAddApprover = async () => {
  if (!approverUserId.value) return
  try {
    await changeApi.addApprover(changeId.value, { userId: approverUserId.value })
    showApproverModal.value = false
    approverUserId.value = null
    await loadApprovers()
    await loadDetail()
  } catch (e) {
    alert(e.response?.data?.error?.message || '승인자 추가에 실패했습니다.')
  }
}

const handleRemoveApprover = async (userId) => {
  if (!confirm('승인자를 삭제하시겠습니까?')) return
  try {
    await changeApi.removeApprover(changeId.value, userId)
    await loadApprovers()
    await loadDetail()
  } catch (e) {
    alert('승인자 삭제에 실패했습니다.')
  }
}

const handleApprove = (userId, decision) => {
  approveTargetUserId.value = userId
  approveDecision.value = decision
  approveComment.value = ''
  showApproveModal.value = true
}

const submitApprove = async () => {
  try {
    await changeApi.approve(changeId.value, approveTargetUserId.value, {
      decision: approveDecision.value,
      comment: approveComment.value
    })
    showApproveModal.value = false
    await Promise.all([loadDetail(), loadApprovers(), loadHistory()])
  } catch (e) {
    alert(e.response?.data?.error?.message || '승인/반려 처리에 실패했습니다.')
  }
}

const handleAddComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await changeApi.addComment(changeId.value, { content: newComment.value })
    newComment.value = ''
    await loadComments()
  } catch (e) {
    alert('댓글 등록에 실패했습니다.')
  }
}

const handleDeleteComment = async (commentId) => {
  if (!confirm('댓글을 삭제하시겠습니까?')) return
  try {
    await changeApi.deleteComment(changeId.value, commentId)
    await loadComments()
  } catch (e) {
    alert('댓글 삭제에 실패했습니다.')
  }
}

onMounted(async () => {
  await commonCodeStore.fetchCodes('CHANGE_TYPE')
  await Promise.all([loadDetail(), loadApprovers(), loadComments(), loadHistory()])
})
</script>

<style scoped>
.change-detail { padding: var(--spacing-lg); max-width: 960px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--spacing-lg);
}
.page-header h2 { margin: 0; font-size: var(--font-size-xl); }
.header-actions { display: flex; gap: var(--spacing-sm); }
.status-actions {
  display: flex; justify-content: space-between; align-items: center;
  background: var(--color-bg-secondary); padding: var(--spacing-md);
  border-radius: 8px; margin-bottom: var(--spacing-md);
}
.current-status {
  display: flex; align-items: center; gap: var(--spacing-sm); font-weight: 600;
}
.status-buttons { display: flex; gap: var(--spacing-sm); }
.detail-card {
  background: #fff; border: 1px solid var(--color-border);
  border-radius: 8px; padding: var(--spacing-lg); margin-bottom: var(--spacing-md);
}
.detail-card h3 { margin: 0 0 var(--spacing-md) 0; font-size: var(--font-size-lg); }
.card-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--spacing-md);
}
.card-header h3 { margin: 0; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-md); }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-item.full-width { grid-column: 1 / -1; }
.info-item label {
  font-size: var(--font-size-xs); color: var(--color-text-muted); font-weight: 600;
}
.content-box {
  background: var(--color-bg-secondary); padding: var(--spacing-sm);
  border-radius: 4px; white-space: pre-wrap; font-size: var(--font-size-sm);
}
.priority-badge {
  display: inline-block; padding: 2px 8px; border-radius: 10px;
  font-size: var(--font-size-xs); font-weight: 600;
}
.priority-CRITICAL { background: #fee2e2; color: #dc2626; }
.priority-HIGH { background: #fff7ed; color: #ea580c; }
.priority-MEDIUM { background: #fefce8; color: #ca8a04; }
.priority-LOW { background: #f0fdf4; color: #16a34a; }
.approver-list { display: flex; flex-direction: column; gap: var(--spacing-xs); }
.approver-item {
  display: flex; align-items: center; gap: var(--spacing-md);
  padding: var(--spacing-sm); background: var(--color-bg-secondary);
  border-radius: 4px; font-size: var(--font-size-sm);
}
.approver-order {
  font-weight: 700; color: var(--color-primary); min-width: 24px;
}
.approver-name { font-weight: 600; }
.approve-status {
  padding: 2px 8px; border-radius: 10px;
  font-size: var(--font-size-xs); font-weight: 600;
}
.status-PENDING { background: #f3f4f6; color: #6b7280; }
.status-APPROVED { background: #dcfce7; color: #16a34a; }
.status-REJECTED { background: #fee2e2; color: #dc2626; }
.approver-date {
  color: var(--color-text-muted); font-size: var(--font-size-xs);
}
.approver-comment {
  color: var(--color-text-secondary); font-size: var(--font-size-xs);
  font-style: italic; flex: 1;
}
.comment-list {
  display: flex; flex-direction: column; gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}
.comment-item {
  padding: var(--spacing-sm); background: var(--color-bg-secondary); border-radius: 4px;
}
.comment-header {
  display: flex; align-items: center; gap: var(--spacing-sm);
  margin-bottom: 4px; font-size: var(--font-size-xs);
}
.comment-author { font-weight: 600; }
.comment-date { color: var(--color-text-muted); flex: 1; }
.comment-content { font-size: var(--font-size-sm); white-space: pre-wrap; }
.comment-form { display: flex; gap: var(--spacing-sm); align-items: flex-end; }
.comment-form textarea {
  flex: 1; padding: 8px; border: 1px solid var(--color-border);
  border-radius: 4px; font-size: var(--font-size-sm); resize: none;
}
.timeline { position: relative; padding-left: 20px; }
.timeline::before {
  content: ''; position: absolute; left: 6px; top: 0; bottom: 0;
  width: 2px; background: var(--color-border);
}
.timeline-item { position: relative; padding-bottom: var(--spacing-md); }
.timeline-dot {
  position: absolute; left: -17px; top: 4px; width: 10px; height: 10px;
  border-radius: 50%; background: var(--color-primary);
}
.timeline-content { padding-left: var(--spacing-sm); }
.timeline-field { font-weight: 600; font-size: var(--font-size-sm); margin-bottom: 2px; }
.timeline-change {
  font-size: var(--font-size-sm); display: flex; gap: var(--spacing-xs); align-items: center;
}
.timeline-change .before { color: #dc2626; text-decoration: line-through; }
.timeline-change .after { color: #16a34a; font-weight: 600; }
.timeline-change .arrow { color: var(--color-text-muted); }
.timeline-date { font-size: var(--font-size-xs); color: var(--color-text-muted); margin-top: 2px; }
.empty-state {
  text-align: center; padding: var(--spacing-md);
  color: var(--color-text-muted); font-size: var(--font-size-sm);
}
.form-group { margin-bottom: var(--spacing-md); }
.form-group label {
  display: block; margin-bottom: var(--spacing-xs);
  font-weight: 600; font-size: var(--font-size-sm);
}
.form-group input, .form-group textarea {
  width: 100%; padding: 8px 12px; border: 1px solid var(--color-border);
  border-radius: 4px; font-size: var(--font-size-sm); box-sizing: border-box;
}
.btn {
  padding: 6px 16px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: #fff; }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
.btn-success { background: #16a34a; color: #fff; }
.btn-danger { background: #dc2626; color: #fff; }
.btn-sm { padding: 4px 12px; font-size: var(--font-size-xs); }
.btn-link {
  background: none; border: none; color: var(--color-primary);
  cursor: pointer; font-size: var(--font-size-xs); padding: 0;
}
.btn-link.danger { color: #dc2626; }
</style>
