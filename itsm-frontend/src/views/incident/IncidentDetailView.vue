<template>
  <div class="incident-detail" v-if="incident">
    <div class="page-header">
      <h2>{{ t('incident.detail') }} - #{{ incident.incidentId }}</h2>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="$router.push('/incidents')">{{ t('common.list') }}</button>
        <button v-if="canEdit" class="btn btn-secondary" @click="$router.push(`/incidents/${incident.incidentId}/edit`)">{{ t('common.edit') }}</button>
      </div>
    </div>

    <!-- 상태머신 버튼 -->
    <div class="status-actions">
      <span class="current-status">
        {{ t('incident.currentStatus') }}: <BaseStatusBadge :status="incident.statusCd" />
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
        <span class="sla-label">{{ t('incident.slaDeadline') }}: {{ formatDate(incident.slaDeadlineAt) }}</span>
        <span :class="['sla-countdown', slaUrgency]">{{ slaCountdown }}</span>
      </div>
      <BaseSlaBar :percentage="incident.slaPercentage || 0" :show-label="true" />
    </div>

    <!-- 기본 정보 -->
    <div class="detail-card">
      <h3>{{ t('incident.basicInfo') }}</h3>
      <div class="info-grid">
        <div class="info-item">
          <label>{{ t('incident.incidentTitle') }}</label>
          <span>{{ incident.title }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.type') }}</label>
          <span>{{ commonCodeStore.getCodeName('INCIDENT_TYPE', incident.incidentTypeCd) || incident.incidentTypeCd }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.priority') }}</label>
          <span :class="['priority-badge', `priority-${incident.priorityCd}`]">{{ priorityLabel(incident.priorityCd) }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.company') }}</label>
          <span>{{ incident.companyNm }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.occurredAt') }}</label>
          <span>{{ formatDate(incident.occurredAt) }}</span>
        </div>
        <div class="info-item">
          <label>{{ t('incident.mainManager') }}</label>
          <span>{{ incident.mainManagerNm || '-' }}
            <button v-if="canEdit" class="btn-link" @click="showAssignManagerModal = true">[{{ t('incident.change') }}]</button>
          </span>
        </div>
        <div class="info-item full-width">
          <label>{{ t('incident.content') }}</label>
          <div class="content-box">{{ incident.content }}</div>
        </div>
        <div class="info-item full-width" v-if="incident.processContent">
          <label>{{ t('incident.processContent') }}</label>
          <div class="content-box">{{ incident.processContent }}</div>
        </div>
      </div>
    </div>

    <!-- 담당자 배정 -->
    <div class="detail-card">
      <div class="card-header">
        <h3>{{ t('incident.assigneeManagement') }}</h3>
        <button class="btn btn-sm" @click="showAssigneeModal = true">{{ t('incident.addAssignee') }}</button>
      </div>
      <div v-if="assignees.length === 0" class="empty-state">{{ t('incident.noAssignee') }}</div>
      <div v-else class="assignee-list">
        <div v-for="a in assignees" :key="a.userId" class="assignee-item">
          <span>{{ a.userNm }}</span>
          <span class="assignee-date">{{ formatDate(a.grantedAt) }}</span>
          <button class="btn-link danger" @click="handleRemoveAssignee(a.userId)">{{ t('incident.removeAssignee') }}</button>
        </div>
      </div>
    </div>

    <!-- 댓글 -->
    <IncidentCommentCard
      :comments="comments"
      v-model="newComment"
      @add-comment="handleAddComment"
      @delete-comment="handleDeleteComment"
    />

    <!-- 변경 이력 타임라인 -->
    <IncidentHistoryCard :histories="histories" />

    <!-- 장애보고서 -->
    <IncidentReportCard
      :report="report"
      v-model="reportContent"
      @save-report="handleSaveReport"
    />

    <!-- 담당자 추가 모달 -->
    <BaseModal :show="showAssigneeModal" :title="t('incident.addAssignee')" @close="showAssigneeModal = false">
      <div class="form-group">
        <label>{{ t('incident.userId') }}</label>
        <input v-model.number="assigneeUserId" type="number" :placeholder="t('incident.userIdPlaceholder')" />
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showAssigneeModal = false">{{ t('common.cancel') }}</button>
        <button class="btn btn-primary" @click="handleAssignUser">{{ t('common.add') }}</button>
      </template>
    </BaseModal>

    <!-- 주담당자 변경 모달 -->
    <BaseModal :show="showAssignManagerModal" :title="t('incident.changeMainManager')" @close="showAssignManagerModal = false">
      <div class="form-group">
        <label>{{ t('incident.mainManagerId') }}</label>
        <input v-model.number="mainManagerId" type="number" :placeholder="t('incident.userIdPlaceholder')" />
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showAssignManagerModal = false">{{ t('common.cancel') }}</button>
        <button class="btn btn-primary" @click="handleAssignMainManager">{{ t('incident.change') }}</button>
      </template>
    </BaseModal>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { incidentApi } from '@/api/incident.js'
import { formatDate } from '@/utils/date.js'
import { useToast } from '@/composables/useToast.js'
import { useConfirm } from '@/composables/useConfirm.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'
import BaseSlaBar from '@/components/common/BaseSlaBar.vue'
import BaseModal from '@/components/common/BaseModal.vue'
import IncidentCommentCard from './components/IncidentCommentCard.vue'
import IncidentHistoryCard from './components/IncidentHistoryCard.vue'
import IncidentReportCard from './components/IncidentReportCard.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const commonCodeStore = useCommonCodeStore()
const toast = useToast()
const { confirm } = useConfirm()
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

const STATUS_TRANSITIONS = {
  RECEIVED: [
    { status: 'IN_PROGRESS', label: t('incident.statusAction.startProcess'), class: 'btn-primary' },
    { status: 'REJECTED', label: t('status.REJECTED'), class: 'btn-danger' }
  ],
  IN_PROGRESS: [
    { status: 'COMPLETED', label: t('incident.statusAction.complete'), class: 'btn-success' },
    { status: 'REJECTED', label: t('status.REJECTED'), class: 'btn-danger' }
  ],
  COMPLETED: [
    { status: 'CLOSED', label: t('status.CLOSED'), class: 'btn-primary' }
  ],
  REJECTED: [
    { status: 'RECEIVED', label: t('incident.statusAction.reReceive'), class: 'btn-primary' }
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
  if (diff <= 0) return t('incident.slaExceeded')
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return t('incident.slaRemaining', { hours, minutes })
})

const slaUrgency = computed(() => {
  const pct = incident.value?.slaPercentage
  if (pct == null) return ''
  if (pct >= 95) return 'critical'
  if (pct >= 80) return 'warning'
  return 'normal'
})

const priorityLabel = (code) => {
  return t(`priority.${code}`, code)
}

const loadDetail = async () => {
  try {
    const res = await incidentApi.getDetail(incidentId.value)
    incident.value = res.data.data || res.data
  } catch (e) {
    console.error('Failed to load incident:', e)
    toast.error(t('message.loadFail'))
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
  if (!await confirm({ message: t('incident.confirmStatusChange', { status }) })) return
  try {
    await incidentApi.changeStatus(incidentId.value, { status })
    await loadDetail()
    await loadHistory()
  } catch (e) {
    toast.error(e.response?.data?.error?.message || t('incident.statusChangeFail'))
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
    toast.error(e.response?.data?.error?.message || t('incident.assignFail'))
  }
}

const handleRemoveAssignee = async (userId) => {
  if (!await confirm({ message: t('incident.confirmRemoveAssignee') })) return
  try {
    await incidentApi.removeAssignee(incidentId.value, userId)
    await loadAssignees()
  } catch (e) {
    toast.error(t('incident.removeAssigneeFail'))
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
    toast.error(e.response?.data?.error?.message || t('incident.changeMainManagerFail'))
  }
}

const handleAddComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await incidentApi.addComment(incidentId.value, { content: newComment.value })
    newComment.value = ''
    await loadComments()
  } catch (e) {
    toast.error(t('incident.commentAddFail'))
  }
}

const handleDeleteComment = async (commentId) => {
  if (!await confirm({ message: t('incident.confirmDeleteComment') })) return
  try {
    await incidentApi.deleteComment(incidentId.value, commentId)
    await loadComments()
  } catch (e) {
    toast.error(t('incident.commentDeleteFail'))
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
    await loadReport()
  } catch (e) {
    toast.error(e.response?.data?.error?.message || t('incident.reportSaveFail'))
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
  background: var(--color-bg-white);
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
  color: var(--color-priority-critical);
}
.sla-countdown.warning {
  color: var(--color-priority-high);
}
.sla-countdown.normal {
  color: var(--color-priority-low);
}
.detail-card {
  background: var(--color-bg-white);
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
.priority-CRITICAL { background: var(--color-priority-critical-bg); color: var(--color-priority-critical); }
.priority-HIGH { background: var(--color-priority-high-bg); color: var(--color-priority-high); }
.priority-MEDIUM { background: var(--color-priority-medium-bg); color: var(--color-priority-medium); }
.priority-LOW { background: var(--color-priority-low-bg); color: var(--color-priority-low); }
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
.btn-primary { background: var(--color-primary); color: var(--color-text-inverse); }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
.btn-success { background: var(--color-btn-success); color: var(--color-text-inverse); }
.btn-danger { background: var(--color-btn-danger); color: var(--color-text-inverse); }
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
  color: var(--color-btn-danger);
}
</style>
