<template>
  <div class="inspection-list">
    <div class="page-header">
      <h2>점검관리 목록</h2>
      <button class="btn btn-primary" @click="$router.push('/inspections/new')">점검 등록</button>
    </div>

    <div class="filter-bar">
      <select v-model="filters.statusCd" @change="search">
        <option value="">전체 상태</option>
        <option value="SCHEDULED">예정</option>
        <option value="IN_PROGRESS">진행중</option>
        <option value="ON_HOLD">보류</option>
        <option value="COMPLETED">완료</option>
        <option value="CLOSED">종료</option>
      </select>
      <select v-model="filters.inspectionTypeCd" @change="search">
        <option value="">전체 유형</option>
        <option v-for="t in inspectionTypes" :key="t.code" :value="t.code">{{ t.name }}</option>
      </select>
      <div class="search-box">
        <input v-model="filters.keyword" placeholder="제목/설명 검색" @keyup.enter="search" />
        <button class="btn btn-sm" @click="search">검색</button>
      </div>
    </div>

    <BaseTable :columns="columns" :data="inspections" :loading="loading" empty-message="등록된 점검이 없습니다."
               @row-click="goDetail">
      <template #statusCd="{ row }">
        <BaseStatusBadge :status="row.statusCd" />
      </template>
      <template #scheduledAt="{ row }">
        {{ row.scheduledAt || '-' }}
      </template>
      <template #progress="{ row }">
        <span v-if="row.itemCount > 0">{{ row.completedItemCount }}/{{ row.itemCount }}</span>
        <span v-else class="text-muted">-</span>
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
import { inspectionApi } from '@/api/inspection.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseTable from '@/components/common/BaseTable.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'

const router = useRouter()
const commonCodeStore = useCommonCodeStore()

const loading = ref(false)
const inspections = ref([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const inspectionTypes = ref([])

const filters = reactive({
  keyword: '',
  statusCd: '',
  inspectionTypeCd: ''
})

const columns = [
  { key: 'inspectionId', label: 'ID', width: '60px', align: 'center' },
  { key: 'title', label: '제목' },
  { key: 'inspectionTypeCd', label: '유형', width: '100px' },
  { key: 'statusCd', label: '상태', width: '100px', align: 'center' },
  { key: 'companyNm', label: '고객사', width: '120px' },
  { key: 'scheduledAt', label: '예정일', width: '120px' },
  { key: 'progress', label: '진행률', width: '100px', align: 'center' },
  { key: 'createdAt', label: '등록일시', width: '150px' }
]

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
    if (filters.inspectionTypeCd) params.inspectionTypeCd = filters.inspectionTypeCd

    const res = await inspectionApi.getList(params)
    const data = res.data.data || res.data
    inspections.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e) {
    console.error('점검 목록 조회 실패:', e)
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
  router.push(`/inspections/${row.inspectionId}`)
}

onMounted(async () => {
  try {
    const codes = await commonCodeStore.fetchCodes('INSPECTION_TYPE')
    inspectionTypes.value = codes || []
  } catch (e) {
    console.error('공통코드 조회 실패:', e)
  }
  fetchList()
})
</script>

<style scoped>
.inspection-list { padding: var(--spacing-lg); }
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
.text-muted { color: var(--color-text-muted); }
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
