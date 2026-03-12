<template>
  <div class="inspection-detail">
    <div class="page-header">
      <div>
        <h2>점검 상세 #{{ inspection.inspectionId }}</h2>
        <BaseStatusBadge v-if="inspection.statusCd" :status="inspection.statusCd" />
      </div>
      <div class="header-actions">
        <button v-if="inspection.statusCd === 'SCHEDULED'" class="btn btn-secondary"
                @click="$router.push(`/inspections/${inspection.inspectionId}?edit=true`)">수정</button>
        <button v-for="s in availableTransitions" :key="s" class="btn btn-primary"
                @click="doChangeStatus(s)">{{ statusLabel(s) }}</button>
      </div>
    </div>

    <div class="detail-grid">
      <div class="detail-section">
        <h3>기본 정보</h3>
        <div class="info-table">
          <div class="info-row"><span class="label">제목</span><span>{{ inspection.title }}</span></div>
          <div class="info-row"><span class="label">유형</span><span>{{ inspection.inspectionTypeCd }}</span></div>
          <div class="info-row"><span class="label">예정일</span><span>{{ inspection.scheduledAt || '-' }}</span></div>
          <div class="info-row"><span class="label">고객사</span><span>{{ inspection.companyNm || '-' }}</span></div>
          <div class="info-row"><span class="label">설명</span><span>{{ inspection.description || '-' }}</span></div>
          <div class="info-row" v-if="inspection.completedAt">
            <span class="label">완료일시</span><span>{{ formatDate(inspection.completedAt) }}</span>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-header">
          <h3>점검 항목 ({{ items.length }})</h3>
          <button v-if="canEditItems" class="btn btn-sm" @click="showAddItem = true">항목 추가</button>
        </div>
        <div v-if="showAddItem" class="add-item-form">
          <input v-model="newItem.itemNm" placeholder="항목명" />
          <select v-model="newItem.isRequired">
            <option value="Y">필수</option>
            <option value="N">선택</option>
          </select>
          <button class="btn btn-primary btn-xs" @click="addItem">추가</button>
          <button class="btn btn-secondary btn-xs" @click="showAddItem = false">취소</button>
        </div>
        <table v-if="items.length" class="data-table">
          <thead>
            <tr>
              <th width="40">#</th>
              <th>항목명</th>
              <th width="60">필수</th>
              <th width="120">결과</th>
              <th width="60">정상</th>
              <th v-if="canEditItems" width="50"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in items" :key="item.itemId">
              <td>{{ item.sortOrder }}</td>
              <td>{{ item.itemNm }}</td>
              <td>{{ item.isRequired === 'Y' ? 'O' : '-' }}</td>
              <td>
                <template v-if="isInProgress">
                  <input v-model="resultMap[item.itemId].resultValue" placeholder="결과" class="input-sm" />
                </template>
                <template v-else>{{ getResultValue(item.itemId) }}</template>
              </td>
              <td>
                <template v-if="isInProgress">
                  <select v-model="resultMap[item.itemId].isNormal" class="input-sm">
                    <option value="Y">정상</option>
                    <option value="N">비정상</option>
                  </select>
                </template>
                <template v-else>
                  <span :class="getResultNormal(item.itemId) === 'N' ? 'text-danger' : ''">
                    {{ getResultNormal(item.itemId) === 'N' ? 'X' : (getResultNormal(item.itemId) === 'Y' ? 'O' : '-') }}
                  </span>
                </template>
              </td>
              <td v-if="canEditItems">
                <button class="btn-icon" @click="removeItem(item.itemId)">X</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty-msg">등록된 점검 항목이 없습니다.</p>
        <button v-if="isInProgress && items.length" class="btn btn-primary" style="margin-top: 8px"
                @click="saveResults">결과 저장</button>
      </div>

      <div class="detail-section">
        <h3>변경 이력</h3>
        <div v-if="history.length" class="timeline">
          <div v-for="h in history" :key="h.historyId" class="timeline-item">
            <div class="timeline-header">
              <span class="field-name">{{ h.changedField }}</span>
              <span class="time">{{ formatDate(h.createdAt) }}</span>
            </div>
            <div class="timeline-body">
              <span class="before">{{ h.beforeValue || '-' }}</span>
              <span class="arrow">&rarr;</span>
              <span class="after">{{ h.afterValue || '-' }}</span>
            </div>
          </div>
        </div>
        <p v-else class="empty-msg">변경 이력이 없습니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { inspectionApi } from '@/api/inspection.js'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'

const route = useRoute()
const inspectionId = route.params.id

const inspection = ref({})
const items = ref([])
const results = ref([])
const history = ref([])
const showAddItem = ref(false)
const newItem = ref({ itemNm: '', isRequired: 'Y' })
const resultMap = ref({})

const TRANSITIONS = {
  SCHEDULED: ['IN_PROGRESS', 'ON_HOLD'],
  IN_PROGRESS: ['COMPLETED', 'ON_HOLD'],
  ON_HOLD: ['SCHEDULED', 'IN_PROGRESS'],
  COMPLETED: ['CLOSED'],
  CLOSED: []
}

const STATUS_LABELS = {
  SCHEDULED: '예정', IN_PROGRESS: '진행 시작', ON_HOLD: '보류',
  COMPLETED: '완료', CLOSED: '종료'
}

const availableTransitions = computed(() => TRANSITIONS[inspection.value.statusCd] || [])
const canEditItems = computed(() => ['SCHEDULED', 'ON_HOLD'].includes(inspection.value.statusCd))
const isInProgress = computed(() => inspection.value.statusCd === 'IN_PROGRESS')

const statusLabel = (s) => STATUS_LABELS[s] || s

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const getResultValue = (itemId) => {
  const r = results.value.find(r => r.itemId === itemId)
  return r ? r.resultValue : '-'
}

const getResultNormal = (itemId) => {
  const r = results.value.find(r => r.itemId === itemId)
  return r ? r.isNormal : null
}

const initResultMap = () => {
  const map = {}
  items.value.forEach(item => {
    const existing = results.value.find(r => r.itemId === item.itemId)
    map[item.itemId] = {
      itemId: item.itemId,
      resultValue: existing ? existing.resultValue : '',
      isNormal: existing ? existing.isNormal : 'Y',
      remark: existing ? existing.remark : ''
    }
  })
  resultMap.value = map
}

const loadAll = async () => {
  try {
    const [detailRes, itemsRes, resultsRes, historyRes] = await Promise.all([
      inspectionApi.getDetail(inspectionId),
      inspectionApi.getItems(inspectionId),
      inspectionApi.getResults(inspectionId),
      inspectionApi.getHistory(inspectionId)
    ])
    inspection.value = detailRes.data.data || detailRes.data
    items.value = (itemsRes.data.data || itemsRes.data) || []
    results.value = (resultsRes.data.data || resultsRes.data) || []
    history.value = (historyRes.data.data || historyRes.data) || []
    initResultMap()
  } catch (e) {
    console.error('점검 상세 조회 실패:', e)
  }
}

const doChangeStatus = async (status) => {
  if (!confirm(`상태를 '${statusLabel(status)}'(으)로 변경하시겠습니까?`)) return
  try {
    await inspectionApi.changeStatus(inspectionId, { status })
    loadAll()
  } catch (e) {
    alert(e.response?.data?.error || '상태 변경에 실패했습니다.')
  }
}

const addItem = async () => {
  if (!newItem.value.itemNm.trim()) return
  try {
    await inspectionApi.addItem(inspectionId, {
      itemNm: newItem.value.itemNm,
      sortOrder: items.value.length + 1,
      isRequired: newItem.value.isRequired
    })
    newItem.value = { itemNm: '', isRequired: 'Y' }
    showAddItem.value = false
    loadAll()
  } catch (e) {
    alert('항목 추가에 실패했습니다.')
  }
}

const removeItem = async (itemId) => {
  if (!confirm('항목을 삭제하시겠습니까?')) return
  try {
    await inspectionApi.deleteItem(inspectionId, itemId)
    loadAll()
  } catch (e) {
    alert('항목 삭제에 실패했습니다.')
  }
}

const saveResults = async () => {
  try {
    for (const itemId of Object.keys(resultMap.value)) {
      const r = resultMap.value[itemId]
      await inspectionApi.saveResult(inspectionId, {
        itemId: Number(r.itemId),
        resultValue: r.resultValue,
        isNormal: r.isNormal,
        remark: r.remark
      })
    }
    alert('결과가 저장되었습니다.')
    loadAll()
  } catch (e) {
    alert('결과 저장에 실패했습니다.')
  }
}

onMounted(loadAll)
</script>

<style scoped>
.inspection-detail { padding: var(--spacing-lg); max-width: 1000px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: var(--spacing-lg);
}
.page-header h2 { margin: 0 8px 0 0; font-size: var(--font-size-xl); display: inline; }
.header-actions { display: flex; gap: var(--spacing-xs); }
.detail-grid { display: flex; flex-direction: column; gap: var(--spacing-lg); }
.detail-section {
  background: #fff; border: 1px solid var(--color-border);
  border-radius: 8px; padding: var(--spacing-md);
}
.detail-section h3 { margin: 0 0 var(--spacing-sm); font-size: var(--font-size-md); }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--spacing-sm); }
.section-header h3 { margin: 0; }
.info-table { display: grid; gap: var(--spacing-xs); }
.info-row { display: grid; grid-template-columns: 100px 1fr; gap: var(--spacing-sm); padding: 4px 0; }
.info-row .label { font-weight: 600; color: var(--color-text-secondary); font-size: var(--font-size-sm); }
.data-table { width: 100%; border-collapse: collapse; font-size: var(--font-size-sm); }
.data-table th, .data-table td {
  padding: 6px 8px; text-align: left;
  border-bottom: 1px solid var(--color-border);
}
.data-table th { font-weight: 600; background: var(--color-bg-secondary); }
.input-sm { width: 100%; padding: 4px 6px; border: 1px solid var(--color-border); border-radius: 4px; font-size: var(--font-size-xs); box-sizing: border-box; }
.add-item-form {
  display: flex; gap: var(--spacing-xs); align-items: center;
  margin-bottom: var(--spacing-sm); padding: 8px; background: var(--color-bg-secondary); border-radius: 4px;
}
.add-item-form input { flex: 1; padding: 4px 8px; border: 1px solid var(--color-border); border-radius: 4px; font-size: var(--font-size-sm); }
.add-item-form select { padding: 4px 8px; border: 1px solid var(--color-border); border-radius: 4px; font-size: var(--font-size-sm); }
.timeline { display: flex; flex-direction: column; gap: var(--spacing-xs); }
.timeline-item { padding: 8px; border-left: 3px solid var(--color-primary); padding-left: 12px; }
.timeline-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
.field-name { font-weight: 600; font-size: var(--font-size-sm); }
.time { font-size: var(--font-size-xs); color: var(--color-text-muted); }
.timeline-body { font-size: var(--font-size-sm); }
.before { color: #dc2626; }
.arrow { margin: 0 4px; }
.after { color: #16a34a; }
.text-danger { color: #dc2626; font-weight: 600; }
.empty-msg { color: var(--color-text-muted); font-size: var(--font-size-sm); padding: 8px 0; }
.btn {
  padding: 6px 16px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: #fff; }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
.btn-sm { padding: 4px 12px; font-size: var(--font-size-xs); background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
.btn-xs { padding: 3px 8px; font-size: var(--font-size-xs); }
.btn-icon { background: none; border: none; cursor: pointer; color: #dc2626; font-weight: 600; }
</style>
