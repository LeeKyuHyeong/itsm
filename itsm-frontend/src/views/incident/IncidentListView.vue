<template>
  <div class="incident-list">
    <div class="page-header">
      <h2>장애 목록</h2>
      <button class="btn btn-primary" @click="$router.push('/incidents/new')">장애 등록</button>
    </div>

    <div class="filter-bar">
      <select v-model="filters.statusCd" @change="search">
        <option value="">전체 상태</option>
        <option value="RECEIVED">접수</option>
        <option value="IN_PROGRESS">처리중</option>
        <option value="COMPLETED">완료</option>
        <option value="CLOSED">종료</option>
        <option value="REJECTED">반려</option>
      </select>
      <select v-model="filters.priorityCd" @change="search">
        <option value="">전체 우선순위</option>
        <option value="CRITICAL">긴급</option>
        <option value="HIGH">높음</option>
        <option value="MEDIUM">보통</option>
        <option value="LOW">낮음</option>
      </select>
      <select v-model="filters.incidentTypeCd" @change="search">
        <option value="">전체 유형</option>
        <option v-for="t in incidentTypes" :key="t.code" :value="t.code">{{ t.name }}</option>
      </select>
      <div class="search-box">
        <input v-model="filters.keyword" placeholder="제목/내용 검색" @keyup.enter="search" />
        <button class="btn btn-sm" @click="search">검색</button>
      </div>
    </div>

    <BaseTable :columns="columns" :data="incidents" :loading="loading" empty-message="등록된 장애가 없습니다."
               @row-click="goDetail">
      <template #priorityCd="{ row }">
        <span :class="['priority-badge', `priority-${row.priorityCd}`]">
          {{ priorityLabel(row.priorityCd) }}
        </span>
      </template>
      <template #statusCd="{ row }">
        <BaseStatusBadge :status="row.statusCd" />
      </template>
      <template #slaPercentage="{ row }">
        <BaseSlaBar v-if="row.slaPercentage != null" :percentage="row.slaPercentage" :show-label="true" />
        <span v-else class="text-muted">-</span>
      </template>
      <template #occurredAt="{ row }">
        {{ formatDate(row.occurredAt) }}
      </template>
    </BaseTable>

    <BasePagination v-if="totalPages > 1" :current-page="page" :total-pages="totalPages"
                    :total-elements="totalElements" @page-change="changePage" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { incidentApi } from '@/api/incident.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseTable from '@/components/common/BaseTable.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'
import BaseSlaBar from '@/components/common/BaseSlaBar.vue'

const router = useRouter()
const commonCodeStore = useCommonCodeStore()

const loading = ref(false)
const incidents = ref([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const incidentTypes = ref([])

const filters = reactive({
  keyword: '',
  statusCd: '',
  priorityCd: '',
  incidentTypeCd: ''
})

const columns = [
  { key: 'incidentId', label: 'ID', width: '60px', align: 'center' },
  { key: 'title', label: '제목' },
  { key: 'incidentTypeCd', label: '유형', width: '120px' },
  { key: 'priorityCd', label: '우선순위', width: '100px', align: 'center' },
  { key: 'statusCd', label: '상태', width: '100px', align: 'center' },
  { key: 'companyNm', label: '고객사', width: '120px' },
  { key: 'mainManagerNm', label: '주담당자', width: '100px' },
  { key: 'slaPercentage', label: 'SLA 경과율', width: '140px' },
  { key: 'occurredAt', label: '발생일시', width: '150px' }
]

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

const fetchList = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.statusCd) params.statusCd = filters.statusCd
    if (filters.priorityCd) params.priorityCd = filters.priorityCd
    if (filters.incidentTypeCd) params.incidentTypeCd = filters.incidentTypeCd

    const res = await incidentApi.getList(params)
    const data = res.data.data || res.data
    incidents.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e) {
    console.error('장애 목록 조회 실패:', e)
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 0
  fetchList()
}

const changePage = (p) => {
  page.value = p
  fetchList()
}

const goDetail = (row) => {
  router.push(`/incidents/${row.incidentId}`)
}

onMounted(async () => {
  try {
    const codes = await commonCodeStore.fetchCodes('INCIDENT_TYPE')
    incidentTypes.value = codes || []
  } catch (e) {
    console.error('공통코드 조회 실패:', e)
  }
  fetchList()
})
</script>

<style scoped>
.incident-list {
  padding: var(--spacing-lg);
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
.filter-bar {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
  flex-wrap: wrap;
  align-items: center;
}
.filter-bar select {
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
}
.search-box {
  display: flex;
  gap: var(--spacing-xs);
}
.search-box input {
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  width: 200px;
}
.priority-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: var(--font-size-xs);
  font-weight: 600;
}
.priority-CRITICAL {
  background: #fee2e2;
  color: #dc2626;
}
.priority-HIGH {
  background: #fff7ed;
  color: #ea580c;
}
.priority-MEDIUM {
  background: #fefce8;
  color: #ca8a04;
}
.priority-LOW {
  background: #f0fdf4;
  color: #16a34a;
}
.text-muted {
  color: var(--color-text-muted);
}
.btn {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: var(--font-size-sm);
}
.btn-primary {
  background: var(--color-primary);
  color: #fff;
}
.btn-sm {
  padding: 6px 12px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
}
</style>
