<template>
  <div class="change-detail" v-if="change">
    <div class="page-header">
      <h2>{{ t('change.detail') }} - #{{ change.changeId }}</h2>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="$router.push('/changes')">{{ t('common.list') }}</button>
        <button v-if="canEdit" class="btn btn-secondary" @click="$router.push(`/changes/${change.changeId}/edit`)">{{ t('common.edit') }}</button>
      </div>
    </div>

    <!-- 상태머신 버튼 -->
    <div class="status-actions">
      <span class="current-status">
        {{ t('incident.status') }}: <BaseStatusBadge :status="change.statusCd" />
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
      <h3>{{ t('change.basicInfo') }}</h3>
      <div class="info-grid">
        <div class="info-item">
          <label>{{ t('incident.incidentTitle') }}</label>
          <span>{{ change.title }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('change.type') }}</label>
          <span>{{ commonCodeStore.getCodeName('CHANGE_TYPE', change.changeTypeCd) || change.changeTypeCd }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.priority') }}</label>
          <span :class="['priority-badge', `priority-${change.priorityCd}`]">{{ t(`priority.${change.priorityCd}`) }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.company') }}</label>
          <span>{{ change.companyNm }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('change.scheduledAt') }}</label>
          <span>{{ formatDate(change.scheduledAt) }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('change.approvalStatus') }}</label>
          <span>{{ change.approvedCount || 0 }} / {{ change.approverCount || 0 }} {{ t('status.APPROVED') }}</span>
        </div>
        <div class="info-item full-width">
          <label>{{ t('change.content') }}</label>
          <div class="content-box">{{ change.content }}</div>
        </div>
        <div class="info-item full-width" v-if="change.rollbackPlan">
          <label>{{ t('change.rollbackPlan') }}</label>
          <div class="content-box">{{ change.rollbackPlan }}</div>
        </div>
      </div>
    </div>

    <!-- 승인 현황 -->
    <div class="detail-card">
      <div class="card-header">
        <h3>{{ t('change.approverStatus') }}</h3>
        <button v-if="canEdit" class="btn btn-sm" @click="showApproverModal = true">{{ t('common.add') }}</button>
      </div>
      <div v-if="approvers.length === 0" class="empty-state">{{ t('common.noData') }}</div>
      <div v-else class="approver-list">
        <div v-for="a in approvers" :key="a.userId" class="approver-item">
          <span class="approver-order">#{{ a.approveOrder }}</span>
          <span class="approver-name">{{ a.userNm }}</span>
          <span :class="['approve-status', `status-${a.approveStatus}`]">{{ approveStatusLabel(a.approveStatus) }}</span>
          <span v-if="a.approvedAt" class="approver-date">{{ formatDate(a.approvedAt) }}</span>
          <span v-if="a.comment" class="approver-comment">{{ a.comment }}</span>
          <template v-if="change.statusCd === 'APPROVAL_REQUESTED' && a.approveStatus === 'PENDING'">
            <button class="btn-link" @click="handleApprove(a.userId, 'APPROVED')">{{ t('status.APPROVED') }}</button>
            <button class="btn-link danger" @click="handleApprove(a.userId, 'REJECTED')">{{ t('status.REJECTED') }}</button>
          </template>
          <button v-if="canEdit && a.approveStatus === 'PENDING'" class="btn-link danger" @click="handleRemoveApprover(a.userId)">{{ t('common.delete') }}</button>
        </div>
      </div>
    </div>

    <!-- 댓글 -->
    <div class="detail-card">
      <h3>{{ t('board.comment') }}</h3>
      <div v-if="comments.length === 0" class="empty-state">{{ t('common.noData') }}</div>
      <div v-else class="comment-list">
        <div v-for="c in comments" :key="c.commentId" class="comment-item">
          <div class="comment-header">
            <span class="comment-author">{{ t('change.userPrefix') }}#{{ c.createdBy }}</span>
            <span class="comment-date">{{ formatDate(c.createdAt) }}</span>
            <button class="btn-link danger" @click="handleDeleteComment(c.commentId)">{{ t('common.delete') }}</button>
          </div>
          <div class="comment-content">{{ c.content }}</div>
        </div>
      </div>
      <div class="comment-form">
        <textarea v-model="newComment" rows="2" :placeholder="t('board.commentPlaceholder')"></textarea>
        <button class="btn btn-primary btn-sm" @click="handleAddComment" :disabled="!newComment.trim()">{{ t('common.create') }}</button>
      </div>
    </div>

    <!-- 변경 이력 -->
    <div class="detail-card">
      <h3>{{ t('change.changeHistory') }}</h3>
      <div v-if="histories.length === 0" class="empty-state">{{ t('common.noData') }}</div>
      <div v-else class="timeline">
        <div v-for="h in histories" :key="h.historyId" class="timeline-item">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <div class="timeline-field">{{ h.changedField }}</div>
            <div class="timeline-change">
              <span class="before">{{ h.beforeValue || '-' }}</span>
              <span class="arrow">&rarr;</span>
              <span class="after">{{ h.afterValue || '-' }}</span>
            </div>
            <div class="timeline-date">{{ formatDate(h.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 승인자 추가 모달 -->
    <BaseModal :show="showApproverModal" :title="t('common.add')" @close="showApproverModal = false">
      <div class="form-group">
        <label>ID</label>
        <input v-model.number="approverUserId" type="number" />
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showApproverModal = false">{{ t('common.cancel') }}</button>
        <button class="btn btn-primary" @click="handleAddApprover">{{ t('common.add') }}</button>
      </template>
    </BaseModal>

    <!-- 승인/반려 모달 -->
    <BaseModal :show="showApproveModal" :title="approveDecision === 'APPROVED' ? t('status.APPROVED') : t('status.REJECTED')" @close="showApproveModal = false">
      <div class="form-group">
        <label>{{ t('change.opinion') }}</label>
        <textarea v-model="approveComment" rows="3"></textarea>
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showApproveModal = false">{{ t('common.cancel') }}</button>
        <button :class="['btn', approveDecision === 'APPROVED' ? 'btn-primary' : 'btn-danger']"
                @click="submitApprove">
          {{ approveDecision === 'APPROVED' ? t('status.APPROVED') : t('status.REJECTED') }}
        </button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { changeApi } from '@/api/change.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'
import BaseModal from '@/components/common/BaseModal.vue'

const { t } = useI18n()
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
    { status: 'APPROVAL_REQUESTED', label: t('status.REQUESTED'), class: 'btn-primary' },
    { status: 'CANCELLED', label: t('status.CANCELLED'), class: 'btn-secondary' }
  ],
  APPROVED: [
    { status: 'IN_PROGRESS', label: t('status.IN_PROGRESS'), class: 'btn-primary' }
  ],
  IN_PROGRESS: [
    { status: 'COMPLETED', label: t('status.COMPLETED'), class: 'btn-success' }
  ],
  COMPLETED: [
    { status: 'CLOSED', label: t('status.CLOSED'), class: 'btn-primary' }
  ],
  REJECTED: [
    { status: 'DRAFT', label: t('status.DRAFT'), class: 'btn-primary' }
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

const approveStatusLabel = (status) => {
  const map = { PENDING: t('status.PENDING'), APPROVED: t('status.APPROVED'), REJECTED: t('status.REJECTED') }
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
    console.error(t('message.loadFail'), e)
    alert(t('message.loadFail'))
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
  if (!confirm(t('change.confirmStatusChange', { status: t(`status.${status}`) }))) return
  try {
    await changeApi.changeStatus(changeId.value, { status })
    await loadDetail()
    await loadHistory()
  } catch (e) {
    alert(e.response?.data?.error?.message || t('message.saveFail'))
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
    alert(e.response?.data?.error?.message || t('message.saveFail'))
  }
}

const handleRemoveApprover = async (userId) => {
  if (!confirm(t('message.deleteConfirm'))) return
  try {
    await changeApi.removeApprover(changeId.value, userId)
    await loadApprovers()
    await loadDetail()
  } catch (e) {
    alert(t('message.deleteFail'))
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
    alert(e.response?.data?.error?.message || t('message.saveFail'))
  }
}

const handleAddComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await changeApi.addComment(changeId.value, { content: newComment.value })
    newComment.value = ''
    await loadComments()
  } catch (e) {
    alert(t('message.saveFail'))
  }
}

const handleDeleteComment = async (commentId) => {
  if (!confirm(t('message.deleteConfirm'))) return
  try {
    await changeApi.deleteComment(changeId.value, commentId)
    await loadComments()
  } catch (e) {
    alert(t('message.deleteFail'))
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
