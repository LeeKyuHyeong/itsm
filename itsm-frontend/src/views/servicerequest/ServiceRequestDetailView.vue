<template>
  <div class="sr-detail" v-if="sr">
    <div class="page-header">
      <h2>{{ t('serviceRequest.detail') }} - #{{ sr.requestId }}</h2>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="$router.push('/service-requests')">{{ t('common.list') }}</button>
        <button v-if="canEdit" class="btn btn-secondary" @click="$router.push(`/service-requests/${sr.requestId}/edit`)">{{ t('common.edit') }}</button>
      </div>
    </div>

    <!-- 상태머신 버튼 -->
    <div class="status-actions">
      <span class="current-status">
        {{ t('incident.currentStatus') }}: <BaseStatusBadge :status="sr.statusCd" />
      </span>
      <div class="status-buttons">
        <button v-for="s in availableTransitions" :key="s.status"
                :class="['btn', s.class]" @click="handleChangeStatus(s.status)">
          {{ s.label }}
        </button>
      </div>
    </div>

    <!-- SLA 카운트다운 -->
    <div class="sla-section" v-if="sr.slaDeadlineAt">
      <div class="sla-info">
        <span class="sla-label">SLA: {{ formatDate(sr.slaDeadlineAt) }}</span>
        <span :class="['sla-countdown', slaUrgency]">{{ slaCountdown }}</span>
      </div>
      <BaseSlaBar :percentage="sr.slaPercentage || 0" :show-label="true" />
    </div>

    <!-- 기본 정보 -->
    <div class="detail-card">
      <h3>{{ t('serviceRequest.basicInfo') }}</h3>
      <div class="info-grid">
        <div class="info-item">
          <label>{{ t('incident.incidentTitle') }}</label>
          <span>{{ sr.title }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('serviceRequest.type') }}</label>
          <span>{{ commonCodeStore.getCodeName('REQUEST_TYPE', sr.requestTypeCd) || sr.requestTypeCd }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.priority') }}</label>
          <span :class="['priority-badge', `priority-${sr.priorityCd}`]">{{ t(`priority.${sr.priorityCd}`) }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.company') }}</label>
          <span>{{ sr.companyNm }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.occurredAt') }}</label>
          <span>{{ formatDate(sr.occurredAt) }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('serviceRequest.rejectCount') }}</label>
          <span>{{ sr.rejectCnt }}{{ t('serviceRequest.countUnit') }}</span>
        </div>
        <div class="info-item full-width">
          <label>{{ t('serviceRequest.content') }}</label>
          <div class="content-box">{{ sr.content }}</div>
        </div>
      </div>
    </div>

    <!-- 담당자 배정 & 진행상태 -->
    <div class="detail-card">
      <div class="card-header">
        <h3>{{ t('incident.assignee') }}</h3>
        <div class="assignee-summary" v-if="assignees.length > 0">
          <span>{{ sr.completedAssigneeCount || 0 }} / {{ sr.assigneeCount || 0 }} {{ t('status.COMPLETED') }}</span>
        </div>
        <button class="btn btn-sm" @click="showAssigneeModal = true">{{ t('common.add') }}</button>
      </div>
      <div v-if="assignees.length === 0" class="empty-state">{{ t('common.noData') }}</div>
      <div v-else class="assignee-list">
        <div v-for="a in assignees" :key="a.userId" class="assignee-item">
          <span class="assignee-name">{{ a.userNm }}</span>
          <span :class="['process-status', `status-${a.processStatus}`]">{{ processStatusLabel(a.processStatus) }}</span>
          <span class="assignee-date">{{ formatDate(a.grantedAt) }}</span>
          <button class="btn-link danger" @click="handleRemoveAssignee(a.userId)">{{ t('serviceRequest.release') }}</button>
        </div>
      </div>
    </div>

    <!-- 처리내용 -->
    <div class="detail-card">
      <div class="card-header">
        <h3>{{ t('serviceRequest.processContent') }}</h3>
      </div>
      <div v-if="processes.length === 0" class="empty-state">{{ t('common.noData') }}</div>
      <div v-else class="process-list">
        <div v-for="p in processes" :key="p.processId" class="process-item">
          <div class="process-header">
            <span class="process-user">{{ t('common.user') }}#{{ p.userId }}</span>
            <span :class="['process-badge', p.isCompleted === 'Y' ? 'completed' : 'pending']">
              {{ p.isCompleted === 'Y' ? t('status.COMPLETED') : t('status.IN_PROGRESS') }}
            </span>
            <span class="process-date">{{ formatDate(p.createdAt) }}</span>
            <button v-if="p.isCompleted !== 'Y'" class="btn-link" @click="handleCompleteProcess(p.processId)">{{ t('serviceRequest.completeProcess') }}</button>
          </div>
          <div class="process-content">{{ p.processContent }}</div>
        </div>
      </div>
      <div class="process-form">
        <div class="form-row-inline">
          <input v-model.number="processUserId" type="number" placeholder="ID" class="input-sm" />
          <textarea v-model="processContent" rows="2"></textarea>
          <button class="btn btn-primary btn-sm" @click="handleAddProcess" :disabled="!processContent.trim() || !processUserId">{{ t('common.create') }}</button>
        </div>
      </div>
    </div>

    <!-- 만족도 -->
    <div class="detail-card" v-if="sr.statusCd === 'CLOSED'">
      <h3>{{ t('serviceRequest.satisfaction') }}</h3>
      <div v-if="sr.satisfactionScore != null" class="satisfaction-result">
        <div class="stars">
          <span v-for="i in 5" :key="i" :class="['star', i <= sr.satisfactionScore ? 'filled' : '']">&#9733;</span>
        </div>
        <p v-if="sr.satisfactionComment" class="satisfaction-comment">{{ sr.satisfactionComment }}</p>
      </div>
      <div v-else class="satisfaction-form">
        <div class="stars-input">
          <span v-for="i in 5" :key="i" :class="['star', i <= satisfactionScore ? 'filled' : '']"
                @click="satisfactionScore = i" style="cursor:pointer">&#9733;</span>
        </div>
        <textarea v-model="satisfactionComment" rows="2"></textarea>
        <button class="btn btn-primary btn-sm" @click="handleSubmitSatisfaction" :disabled="!satisfactionScore">{{ t('serviceRequest.submitSatisfaction') }}</button>
      </div>
    </div>

    <!-- 변경 이력 타임라인 -->
    <div class="detail-card">
      <h3>{{ t('serviceRequest.changeHistory') }}</h3>
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

    <!-- 담당자 추가 모달 -->
    <BaseModal :show="showAssigneeModal" :title="t('common.add')" @close="showAssigneeModal = false">
      <div class="form-group">
        <label>ID</label>
        <input v-model.number="assigneeUserId" type="number" />
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showAssigneeModal = false">{{ t('common.cancel') }}</button>
        <button class="btn btn-primary" @click="handleAssignUser">{{ t('common.add') }}</button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { serviceRequestApi } from '@/api/servicerequest.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'
import BaseSlaBar from '@/components/common/BaseSlaBar.vue'
import BaseModal from '@/components/common/BaseModal.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const commonCodeStore = useCommonCodeStore()
const requestId = computed(() => route.params.id)

const sr = ref(null)
const assignees = ref([])
const processes = ref([])
const histories = ref([])
const assigneeUserId = ref(null)
const processUserId = ref(null)
const processContent = ref('')
const satisfactionScore = ref(0)
const satisfactionComment = ref('')
const showAssigneeModal = ref(false)

const STATUS_TRANSITIONS = {
  RECEIVED: [
    { status: 'ASSIGNED', label: t('status.ASSIGNED'), class: 'btn-primary' },
    { status: 'CANCELLED', label: t('status.CANCELLED'), class: 'btn-secondary' },
    { status: 'REJECTED', label: t('status.REJECTED'), class: 'btn-danger' }
  ],
  ASSIGNED: [
    { status: 'IN_PROGRESS', label: t('status.IN_PROGRESS'), class: 'btn-primary' },
    { status: 'REJECTED', label: t('status.REJECTED'), class: 'btn-danger' }
  ],
  IN_PROGRESS: [
    { status: 'PENDING_COMPLETE', label: t('status.PENDING_COMPLETE'), class: 'btn-success' },
    { status: 'REJECTED', label: t('status.REJECTED'), class: 'btn-danger' }
  ],
  PENDING_COMPLETE: [
    { status: 'CLOSED', label: t('status.CLOSED'), class: 'btn-primary' }
  ],
  REJECTED: [
    { status: 'RECEIVED', label: t('status.RECEIVED'), class: 'btn-primary' }
  ],
  CLOSED: [],
  CANCELLED: []
}

const availableTransitions = computed(() => {
  if (!sr.value) return []
  return STATUS_TRANSITIONS[sr.value.statusCd] || []
})

const canEdit = computed(() => {
  if (!sr.value) return false
  return ['RECEIVED', 'ASSIGNED', 'IN_PROGRESS'].includes(sr.value.statusCd)
})

const slaCountdown = computed(() => {
  if (!sr.value?.slaDeadlineAt) return ''
  const deadline = new Date(sr.value.slaDeadlineAt)
  const now = new Date()
  const diff = deadline - now
  if (diff <= 0) return t('status.OVERDUE')
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${hours}h ${minutes}m`
})

const slaUrgency = computed(() => {
  const pct = sr.value?.slaPercentage
  if (pct == null) return ''
  if (pct >= 95) return 'critical'
  if (pct >= 80) return 'warning'
  return 'normal'
})

const processStatusLabel = (status) => {
  const map = { PENDING: t('status.PENDING'), IN_PROGRESS: t('status.IN_PROGRESS'), COMPLETED: t('status.COMPLETED') }
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
    const res = await serviceRequestApi.getDetail(requestId.value)
    sr.value = res.data.data || res.data
  } catch (e) {
    console.error(t('message.loadFail'), e)
    alert(t('message.loadFail'))
  }
}

const loadAssignees = async () => {
  try {
    const res = await serviceRequestApi.getAssignees(requestId.value)
    assignees.value = res.data.data || res.data || []
  } catch (e) {
    assignees.value = []
  }
}

const loadProcesses = async () => {
  try {
    const res = await serviceRequestApi.getProcesses(requestId.value)
    processes.value = res.data.data || res.data || []
  } catch (e) {
    processes.value = []
  }
}

const loadHistory = async () => {
  try {
    const res = await serviceRequestApi.getHistory(requestId.value)
    histories.value = res.data.data || res.data || []
  } catch (e) {
    histories.value = []
  }
}

const handleChangeStatus = async (status) => {
  if (!confirm(t('serviceRequest.confirmStatusChange', { status }))) return
  try {
    await serviceRequestApi.changeStatus(requestId.value, { status })
    await loadDetail()
    await loadHistory()
  } catch (e) {
    alert(e.response?.data?.error?.message || t('message.saveFail'))
  }
}

const handleAssignUser = async () => {
  if (!assigneeUserId.value) return
  try {
    await serviceRequestApi.assignUser(requestId.value, { userId: assigneeUserId.value })
    showAssigneeModal.value = false
    assigneeUserId.value = null
    await loadAssignees()
    await loadDetail()
  } catch (e) {
    alert(e.response?.data?.error?.message || t('message.saveFail'))
  }
}

const handleRemoveAssignee = async (userId) => {
  if (!confirm(t('message.deleteConfirm'))) return
  try {
    await serviceRequestApi.removeAssignee(requestId.value, userId)
    await loadAssignees()
    await loadDetail()
  } catch (e) {
    alert(t('message.deleteFail'))
  }
}

const handleAddProcess = async () => {
  if (!processContent.value.trim() || !processUserId.value) return
  try {
    await serviceRequestApi.addProcess(requestId.value, {
      userId: processUserId.value,
      processContent: processContent.value
    })
    processContent.value = ''
    await loadProcesses()
  } catch (e) {
    alert(t('message.saveFail'))
  }
}

const handleCompleteProcess = async (processId) => {
  if (!confirm(t('serviceRequest.confirmCompleteProcess'))) return
  try {
    await serviceRequestApi.completeProcess(requestId.value, processId)
    await loadProcesses()
    await loadDetail()
  } catch (e) {
    alert(t('message.saveFail'))
  }
}

const handleSubmitSatisfaction = async () => {
  if (!satisfactionScore.value) return
  try {
    await serviceRequestApi.submitSatisfaction(requestId.value, {
      score: satisfactionScore.value,
      comment: satisfactionComment.value
    })
    await loadDetail()
  } catch (e) {
    alert(e.response?.data?.error?.message || t('message.saveFail'))
  }
}

onMounted(async () => {
  await commonCodeStore.fetchCodes('REQUEST_TYPE')
  await Promise.all([loadDetail(), loadAssignees(), loadProcesses(), loadHistory()])
})
</script>

<style scoped>
.sr-detail {
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
.sla-label { font-weight: 600; font-size: var(--font-size-sm); }
.sla-countdown { font-weight: 700; font-size: var(--font-size-sm); }
.sla-countdown.critical { color: #dc2626; }
.sla-countdown.warning { color: #ea580c; }
.sla-countdown.normal { color: #16a34a; }
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
.card-header h3 { margin: 0; }
.assignee-summary {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  flex: 1;
  text-align: center;
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
.info-item.full-width { grid-column: 1 / -1; }
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
.assignee-name { font-weight: 600; }
.process-status {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: var(--font-size-xs);
  font-weight: 600;
}
.status-PENDING { background: #f3f4f6; color: #6b7280; }
.status-IN_PROGRESS { background: #dbeafe; color: #2563eb; }
.status-COMPLETED { background: #dcfce7; color: #16a34a; }
.assignee-date {
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
  flex: 1;
}
.process-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}
.process-item {
  padding: var(--spacing-sm);
  background: var(--color-bg-secondary);
  border-radius: 4px;
}
.process-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 4px;
  font-size: var(--font-size-xs);
}
.process-user { font-weight: 600; }
.process-badge {
  padding: 1px 6px;
  border-radius: 8px;
  font-size: var(--font-size-xs);
}
.process-badge.completed { background: #dcfce7; color: #16a34a; }
.process-badge.pending { background: #dbeafe; color: #2563eb; }
.process-date {
  color: var(--color-text-muted);
  flex: 1;
}
.process-content {
  font-size: var(--font-size-sm);
  white-space: pre-wrap;
}
.process-form {
  margin-top: var(--spacing-sm);
}
.form-row-inline {
  display: flex;
  gap: var(--spacing-sm);
  align-items: flex-end;
}
.form-row-inline input.input-sm {
  width: 100px;
  padding: 6px 8px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
}
.form-row-inline textarea {
  flex: 1;
  padding: 8px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  resize: none;
}
.satisfaction-result { text-align: center; }
.satisfaction-comment {
  margin-top: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}
.satisfaction-form {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
}
.satisfaction-form textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  resize: none;
}
.stars, .stars-input {
  display: flex;
  gap: 4px;
  font-size: 24px;
}
.star { color: #d1d5db; }
.star.filled { color: #f59e0b; }
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
.timeline-content { padding-left: var(--spacing-sm); }
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
.timeline-change .before { color: #dc2626; text-decoration: line-through; }
.timeline-change .after { color: #16a34a; font-weight: 600; }
.timeline-change .arrow { color: var(--color-text-muted); }
.timeline-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  margin-top: 2px;
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
.form-group input {
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
.btn-link.danger { color: #dc2626; }
</style>
