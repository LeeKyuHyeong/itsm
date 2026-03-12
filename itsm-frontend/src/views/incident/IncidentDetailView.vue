<template>
  <div class="incident-detail" v-if="incident">
    <div class="page-header">
      <h2>장애 상세 - #{{ incident.incidentId }}</h2>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="$router.push('/incidents')">목록</button>
        <button v-if="canEdit" class="btn btn-secondary" @click="$router.push(`/incidents/${incident.incidentId}/edit`)">수정</button>
      </div>
    </div>

    <!-- 상태머신 버튼 -->
    <div class="status-actions">
      <span class="current-status">
        현재 상태: <BaseStatusBadge :status="incident.statusCd" />
      </span>
      <div class="status-buttons">
        <button v-for="s in availableTransitions" :key="s.status"
                :class="['btn', s.class]" @click="handleChangeStatus(s.status)">
          {{ s.label }}
        </button>
      </div>
    </div>

    <!-- SLA 카운트다운 -->
    <div class="sla-section" v-if="incident.slaDeadlineAt">
      <div class="sla-info">
        <span class="sla-label">SLA 기한: {{ formatDate(incident.slaDeadlineAt) }}</span>
        <span :class="['sla-countdown', slaUrgency]">{{ slaCountdown }}</span>
      </div>
      <BaseSlaBar :percentage="incident.slaPercentage || 0" :show-label="true" />
    </div>

    <!-- 기본 정보 -->
    <div class="detail-card">
      <h3>기본 정보</h3>
      <div class="info-grid">
        <div class="info-item">
          <label>제목</label>
          <span>{{ incident.title }}</span>
        </div>
        <div class="info-item">
          <label>장애 유형</label>
          <span>{{ commonCodeStore.getCodeName('INCIDENT_TYPE', incident.incidentTypeCd) || incident.incidentTypeCd }}</span>
        </div>
        <div class="info-item">
          <label>우선순위</label>
          <span :class="['priority-badge', `priority-${incident.priorityCd}`]">{{ priorityLabel(incident.priorityCd) }}</span>
        </div>
        <div class="info-item">
          <label>고객사</label>
          <span>{{ incident.companyNm }}</span>
        </div>
        <div class="info-item">
          <label>발생일시</label>
          <span>{{ formatDate(incident.occurredAt) }}</span>
        </div>
        <div class="info-item">
          <label>주담당자</label>
          <span>{{ incident.mainManagerNm || '-' }}
            <button v-if="canEdit" class="btn-link" @click="showAssignManagerModal = true">[변경]</button>
          </span>
        </div>
        <div class="info-item full-width">
          <label>장애 내용</label>
          <div class="content-box">{{ incident.content }}</div>
        </div>
        <div class="info-item full-width" v-if="incident.processContent">
          <label>처리내용</label>
          <div class="content-box">{{ incident.processContent }}</div>
        </div>
      </div>
    </div>

    <!-- 담당자 배정 -->
    <div class="detail-card">
      <div class="card-header">
        <h3>담당자 배정</h3>
        <button class="btn btn-sm" @click="showAssigneeModal = true">담당자 추가</button>
      </div>
      <div v-if="assignees.length === 0" class="empty-state">배정된 담당자가 없습니다.</div>
      <div v-else class="assignee-list">
        <div v-for="a in assignees" :key="a.userId" class="assignee-item">
          <span>{{ a.userNm }}</span>
          <span class="assignee-date">{{ formatDate(a.grantedAt) }}</span>
          <button class="btn-link danger" @click="handleRemoveAssignee(a.userId)">해제</button>
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
            <span class="comment-author">{{ c.createdByNm || `사용자#${c.createdBy}` }}</span>
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

    <!-- 변경 이력 타임라인 -->
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

    <!-- 장애보고서 -->
    <div class="detail-card">
      <div class="card-header">
        <h3>장애보고서</h3>
        <button v-if="!report" class="btn btn-sm" @click="showReportModal = true">작성</button>
        <button v-else class="btn btn-sm" @click="showReportModal = true">수정</button>
      </div>
      <div v-if="!report" class="empty-state">작성된 장애보고서가 없습니다.</div>
      <div v-else class="report-content">
        <pre>{{ report.reportContent }}</pre>
        <div class="report-meta">
          작성일: {{ formatDate(report.createdAt) }}
          <span v-if="report.updatedAt"> | 수정일: {{ formatDate(report.updatedAt) }}</span>
        </div>
      </div>
    </div>

    <!-- 담당자 추가 모달 -->
    <BaseModal :show="showAssigneeModal" title="담당자 추가" @close="showAssigneeModal = false">
      <div class="form-group">
        <label>사용자 ID</label>
        <input v-model.number="assigneeUserId" type="number" placeholder="사용자 ID 입력" />
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showAssigneeModal = false">취소</button>
        <button class="btn btn-primary" @click="handleAssignUser">추가</button>
      </template>
    </BaseModal>

    <!-- 주담당자 변경 모달 -->
    <BaseModal :show="showAssignManagerModal" title="주담당자 변경" @close="showAssignManagerModal = false">
      <div class="form-group">
        <label>주담당자 ID</label>
        <input v-model.number="mainManagerId" type="number" placeholder="사용자 ID 입력" />
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showAssignManagerModal = false">취소</button>
        <button class="btn btn-primary" @click="handleAssignMainManager">변경</button>
      </template>
    </BaseModal>

    <!-- 장애보고서 모달 -->
    <BaseModal :show="showReportModal" title="장애보고서 작성" width="640px" @close="showReportModal = false">
      <div class="form-group">
        <label>보고서 내용 (JSON)</label>
        <textarea v-model="reportContent" rows="10" placeholder='{"summary":"장애 요약","cause":"원인","solution":"해결방안","prevention":"재발방지"}'></textarea>
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showReportModal = false">취소</button>
        <button class="btn btn-primary" @click="handleSaveReport">저장</button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { incidentApi } from '@/api/incident.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'
import BaseSlaBar from '@/components/common/BaseSlaBar.vue'
import BaseModal from '@/components/common/BaseModal.vue'

const route = useRoute()
const router = useRouter()
const commonCodeStore = useCommonCodeStore()
const incidentId = computed(() => route.params.id)

const incident = ref(null)
const assignees = ref([])
const comments = ref([])
const histories = ref([])
const report = ref(null)
const newComment = ref('')
const assigneeUserId = ref(null)
const mainManagerId = ref(null)
const reportContent = ref('')
const showAssigneeModal = ref(false)
const showAssignManagerModal = ref(false)
const showReportModal = ref(false)

const STATUS_TRANSITIONS = {
  RECEIVED: [
    { status: 'IN_PROGRESS', label: '처리 시작', class: 'btn-primary' },
    { status: 'REJECTED', label: '반려', class: 'btn-danger' }
  ],
  IN_PROGRESS: [
    { status: 'COMPLETED', label: '처리 완료', class: 'btn-success' },
    { status: 'REJECTED', label: '반려', class: 'btn-danger' }
  ],
  COMPLETED: [
    { status: 'CLOSED', label: '종료', class: 'btn-primary' }
  ],
  REJECTED: [
    { status: 'RECEIVED', label: '재접수', class: 'btn-primary' }
  ],
  CLOSED: []
}

const availableTransitions = computed(() => {
  if (!incident.value) return []
  return STATUS_TRANSITIONS[incident.value.statusCd] || []
})

const canEdit = computed(() => {
  if (!incident.value) return false
  return ['RECEIVED', 'IN_PROGRESS'].includes(incident.value.statusCd)
})

const slaCountdown = computed(() => {
  if (!incident.value?.slaDeadlineAt) return ''
  const deadline = new Date(incident.value.slaDeadlineAt)
  const now = new Date()
  const diff = deadline - now
  if (diff <= 0) return 'SLA 초과'
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${hours}시간 ${minutes}분 남음`
})

const slaUrgency = computed(() => {
  const pct = incident.value?.slaPercentage
  if (pct == null) return ''
  if (pct >= 95) return 'critical'
  if (pct >= 80) return 'warning'
  return 'normal'
})

const priorityLabel = (code) => {
  const map = { CRITICAL: '긴급', HIGH: '높음', MEDIUM: '보통', LOW: '낮음' }
  return map[code] || code
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
    const res = await incidentApi.getDetail(incidentId.value)
    incident.value = res.data.data || res.data
  } catch (e) {
    console.error('장애 조회 실패:', e)
    alert('장애 정보를 불러올 수 없습니다.')
  }
}

const loadAssignees = async () => {
  try {
    const res = await incidentApi.getAssignees(incidentId.value)
    assignees.value = res.data.data || res.data || []
  } catch (e) {
    assignees.value = []
  }
}

const loadComments = async () => {
  try {
    const res = await incidentApi.getComments(incidentId.value)
    comments.value = res.data.data || res.data || []
  } catch (e) {
    comments.value = []
  }
}

const loadHistory = async () => {
  try {
    const res = await incidentApi.getHistory(incidentId.value)
    histories.value = res.data.data || res.data || []
  } catch (e) {
    histories.value = []
  }
}

const loadReport = async () => {
  try {
    const res = await incidentApi.getReport(incidentId.value)
    report.value = res.data.data || res.data
  } catch (e) {
    report.value = null
  }
}

const handleChangeStatus = async (status) => {
  if (!confirm(`상태를 "${status}"(으)로 변경하시겠습니까?`)) return
  try {
    await incidentApi.changeStatus(incidentId.value, { status })
    await loadDetail()
    await loadHistory()
  } catch (e) {
    alert(e.response?.data?.error?.message || '상태 변경에 실패했습니다.')
  }
}

const handleAssignUser = async () => {
  if (!assigneeUserId.value) return
  try {
    await incidentApi.assignUser(incidentId.value, { userId: assigneeUserId.value })
    showAssigneeModal.value = false
    assigneeUserId.value = null
    await loadAssignees()
  } catch (e) {
    alert(e.response?.data?.error?.message || '담당자 추가에 실패했습니다.')
  }
}

const handleRemoveAssignee = async (userId) => {
  if (!confirm('담당자를 해제하시겠습니까?')) return
  try {
    await incidentApi.removeAssignee(incidentId.value, userId)
    await loadAssignees()
  } catch (e) {
    alert('담당자 해제에 실패했습니다.')
  }
}

const handleAssignMainManager = async () => {
  if (!mainManagerId.value) return
  try {
    await incidentApi.assignMainManager(incidentId.value, { managerId: mainManagerId.value })
    showAssignManagerModal.value = false
    mainManagerId.value = null
    await loadDetail()
  } catch (e) {
    alert(e.response?.data?.error?.message || '주담당자 변경에 실패했습니다.')
  }
}

const handleAddComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await incidentApi.addComment(incidentId.value, { content: newComment.value })
    newComment.value = ''
    await loadComments()
  } catch (e) {
    alert('댓글 등록에 실패했습니다.')
  }
}

const handleDeleteComment = async (commentId) => {
  if (!confirm('댓글을 삭제하시겠습니까?')) return
  try {
    await incidentApi.deleteComment(incidentId.value, commentId)
    await loadComments()
  } catch (e) {
    alert('댓글 삭제에 실패했습니다.')
  }
}

const handleSaveReport = async () => {
  try {
    const payload = { reportFormId: 1, reportContent: reportContent.value }
    if (report.value) {
      await incidentApi.updateReport(incidentId.value, payload)
    } else {
      await incidentApi.saveReport(incidentId.value, payload)
    }
    showReportModal.value = false
    await loadReport()
  } catch (e) {
    alert(e.response?.data?.error?.message || '보고서 저장에 실패했습니다.')
  }
}

onMounted(async () => {
  await commonCodeStore.fetchCodes('INCIDENT_TYPE')
  await Promise.all([loadDetail(), loadAssignees(), loadComments(), loadHistory(), loadReport()])
  if (report.value) {
    reportContent.value = report.value.reportContent
  }
})
</script>

<style scoped>
.incident-detail {
  padding: var(--spacing-lg);
  max-width: 960px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}
.page-header h2 {
  margin: 0;
  font-size: var(--font-size-xl);
}
.header-actions {
  display: flex;
  gap: var(--spacing-sm);
}
.status-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-bg-secondary);
  padding: var(--spacing-md);
  border-radius: 8px;
  margin-bottom: var(--spacing-md);
}
.current-status {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 600;
}
.status-buttons {
  display: flex;
  gap: var(--spacing-sm);
}
.sla-section {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}
.sla-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}
.sla-label {
  font-weight: 600;
  font-size: var(--font-size-sm);
}
.sla-countdown {
  font-weight: 700;
  font-size: var(--font-size-sm);
}
.sla-countdown.critical {
  color: #dc2626;
}
.sla-countdown.warning {
  color: #ea580c;
}
.sla-countdown.normal {
  color: #16a34a;
}
.detail-card {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}
.detail-card h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: var(--font-size-lg);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}
.card-header h3 {
  margin: 0;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-item.full-width {
  grid-column: 1 / -1;
}
.info-item label {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  font-weight: 600;
}
.content-box {
  background: var(--color-bg-secondary);
  padding: var(--spacing-sm);
  border-radius: 4px;
  white-space: pre-wrap;
  font-size: var(--font-size-sm);
}
.priority-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: var(--font-size-xs);
  font-weight: 600;
}
.priority-CRITICAL { background: #fee2e2; color: #dc2626; }
.priority-HIGH { background: #fff7ed; color: #ea580c; }
.priority-MEDIUM { background: #fefce8; color: #ca8a04; }
.priority-LOW { background: #f0fdf4; color: #16a34a; }
.assignee-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}
.assignee-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-xs) var(--spacing-sm);
  background: var(--color-bg-secondary);
  border-radius: 4px;
  font-size: var(--font-size-sm);
}
.assignee-date {
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
  flex: 1;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}
.comment-item {
  padding: var(--spacing-sm);
  background: var(--color-bg-secondary);
  border-radius: 4px;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 4px;
  font-size: var(--font-size-xs);
}
.comment-author {
  font-weight: 600;
}
.comment-date {
  color: var(--color-text-muted);
  flex: 1;
}
.comment-content {
  font-size: var(--font-size-sm);
  white-space: pre-wrap;
}
.comment-form {
  display: flex;
  gap: var(--spacing-sm);
  align-items: flex-end;
}
.comment-form textarea {
  flex: 1;
  padding: 8px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  resize: none;
}
.timeline {
  position: relative;
  padding-left: 20px;
}
.timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--color-border);
}
.timeline-item {
  position: relative;
  padding-bottom: var(--spacing-md);
}
.timeline-dot {
  position: absolute;
  left: -17px;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-primary);
}
.timeline-content {
  padding-left: var(--spacing-sm);
}
.timeline-field {
  font-weight: 600;
  font-size: var(--font-size-sm);
  margin-bottom: 2px;
}
.timeline-change {
  font-size: var(--font-size-sm);
  display: flex;
  gap: var(--spacing-xs);
  align-items: center;
}
.timeline-change .before {
  color: #dc2626;
  text-decoration: line-through;
}
.timeline-change .after {
  color: #16a34a;
  font-weight: 600;
}
.timeline-change .arrow {
  color: var(--color-text-muted);
}
.timeline-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  margin-top: 2px;
}
.report-content pre {
  background: var(--color-bg-secondary);
  padding: var(--spacing-md);
  border-radius: 4px;
  white-space: pre-wrap;
  font-size: var(--font-size-sm);
}
.report-meta {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  margin-top: var(--spacing-xs);
}
.empty-state {
  text-align: center;
  padding: var(--spacing-md);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.form-group {
  margin-bottom: var(--spacing-md);
}
.form-group label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-weight: 600;
  font-size: var(--font-size-sm);
}
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  box-sizing: border-box;
}
.btn {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: #fff; }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
.btn-success { background: #16a34a; color: #fff; }
.btn-danger { background: #dc2626; color: #fff; }
.btn-sm { padding: 4px 12px; font-size: var(--font-size-xs); }
.btn-link {
  background: none;
  border: none;
  color: var(--color-primary);
  cursor: pointer;
  font-size: var(--font-size-xs);
  padding: 0;
}
.btn-link.danger {
  color: #dc2626;
}
</style>
