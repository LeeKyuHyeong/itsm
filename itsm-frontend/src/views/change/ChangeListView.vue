<template>
  <div class="change-list">
    <div class="page-header">
      <h2>변경관리 목록</h2>
      <button class="btn btn-primary" @click="$router.push('/changes/new')">변경요청 등록</button>
    </div>

    <div class="filter-bar">
      <select v-model="filters.statusCd" @change="search">
        <option value="">전체 상태</option>
        <option value="DRAFT">초안</option>
        <option value="APPROVAL_REQUESTED">승인요청</option>
        <option value="APPROVED">승인완료</option>
        <option value="IN_PROGRESS">실행중</option>
        <option value="COMPLETED">완료</option>
        <option value="CLOSED">종료</option>
        <option value="REJECTED">반려</option>
        <option value="CANCELLED">취소</option>
      </select>
      <select v-model="filters.priorityCd" @change="search">
        <option value="">전체 우선순위</option>
        <option value="CRITICAL">긴급</option>
        <option value="HIGH">높음</option>
        <option value="MEDIUM">보통</option>
        <option value="LOW">낮음</option>
      </select>
      <select v-model="filters.changeTypeCd" @change="search">
        <option value="">전체 유형</option>
        <option v-for="t in changeTypes" :key="t.code" :value="t.code">{{ t.name }}</option>
      </select>
      <div class="search-box">
        <input v-model="filters.keyword" placeholder="제목/내용 검색" @keyup.enter="search" />
        <button class="btn btn-sm" @click="search">검색</button>
      </div>
    </div>

    <BaseTable :columns="columns" :data="changes" :loading="loading" empty-message="등록된 변경요청이 없습니다."
               @row-click="goDetail">
      <template #priorityCd="{ row }">
        <span :class="['priority-badge', `priority-${row.priorityCd}`]">
          {{ priorityLabel(row.priorityCd) }}
        </span>
      </template>
      <template #statusCd="{ row }">
        <BaseStatusBadge :status="row.statusCd" />
      </template>
      <template #scheduledAt="{ row }">
        {{ formatDate(row.scheduledAt) }}
      </template>
      <template #createdAt="{ row }">
        {{ formatDate(row.createdAt) }}
      </template>
    </BaseTable>

    <BasePagination v-if="totalPages > 1" :current-page="page" :total-pages="totalPages"
                    :total-elements="totalElements" @page-change="changePage" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { changeApi } from '@/api/change.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseTable from '@/components/common/BaseTable.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'

const router = useRouter()
const commonCodeStore = useCommonCodeStore()

const loading = ref(false)
const changes = ref([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const changeTypes = ref([])

const filters = reactive({
  keyword: '',
  statusCd: '',
  priorityCd: '',
  changeTypeCd: ''
})

const columns = [
  { key: 'changeId', label: 'ID', width: '60px', align: 'center' },
  { key: 'title', label: '제목' },
  { key: 'changeTypeCd', label: '유형', width: '100px' },
  { key: 'priorityCd', label: '우선순위', width: '100px', align: 'center' },
  { key: 'statusCd', label: '상태', width: '110px', align: 'center' },
  { key: 'companyNm', label: '고객사', width: '120px' },
  { key: 'scheduledAt', label: '예정일시', width: '150px' },
  { key: 'createdAt', label: '등록일시', width: '150px' }
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
    if (filters.changeTypeCd) params.changeTypeCd = filters.changeTypeCd

    const res = await changeApi.getList(params)
    const data = res.data.data || res.data
    changes.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e) {
    console.error('변경 목록 조회 실패:', e)
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
  router.push(`/changes/${row.changeId}`)
}

onMounted(async () => {
  try {
    const codes = await commonCodeStore.fetchCodes('CHANGE_TYPE')
    changeTypes.value = codes || []
  } catch (e) {
    console.error('공통코드 조회 실패:', e)
  }
  fetchList()
})
</script>

<style scoped>
.change-list { padding: var(--spacing-lg); }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--spacing-lg);
}
.page-header h2 { margin: 0; font-size: var(--font-size-xl); }
.filter-bar {
  display: flex; gap: var(--spacing-sm); margin-bottom: var(--spacing-md);
  flex-wrap: wrap; align-items: center;
}
.filter-bar select {
  padding: 6px 12px; border: 1px solid var(--color-border);
  border-radius: 4px; font-size: var(--font-size-sm);
}
.search-box { display: flex; gap: var(--spacing-xs); }
.search-box input {
  padding: 6px 12px; border: 1px solid var(--color-border);
  border-radius: 4px; font-size: var(--font-size-sm); width: 200px;
}
.priority-badge {
  display: inline-block; padding: 2px 8px; border-radius: 10px;
  font-size: var(--font-size-xs); font-weight: 600;
}
.priority-CRITICAL { background: #fee2e2; color: #dc2626; }
.priority-HIGH { background: #fff7ed; color: #ea580c; }
.priority-MEDIUM { background: #fefce8; color: #ca8a04; }
.priority-LOW { background: #f0fdf4; color: #16a34a; }
.btn {
  padding: 6px 16px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: #fff; }
.btn-sm {
  padding: 6px 12px; background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
}
</style>
