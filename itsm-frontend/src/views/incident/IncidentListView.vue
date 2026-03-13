<template>
  <div class="incident-list">
    <div class="page-header">
      <h2>{{ t('incident.list') }}</h2>
      <button class="btn btn-primary" @click="$router.push('/incidents/new')">{{ t('incident.create') }}</button>
    </div>

    <div class="filter-bar">
      <select v-model="filters.statusCd" @change="search">
        <option value="">{{ t('common.all') }} {{ t('incident.status') }}</option>
        <option value="RECEIVED">{{ t('status.RECEIVED') }}</option>
        <option value="IN_PROGRESS">{{ t('status.IN_PROGRESS') }}</option>
        <option value="COMPLETED">{{ t('status.COMPLETED') }}</option>
        <option value="CLOSED">{{ t('status.CLOSED') }}</option>
        <option value="REJECTED">{{ t('status.REJECTED') }}</option>
      </select>
      <select v-model="filters.priorityCd" @change="search">
        <option value="">{{ t('common.all') }} {{ t('incident.priority') }}</option>
        <option value="CRITICAL">{{ t('priority.CRITICAL') }}</option>
        <option value="HIGH">{{ t('priority.HIGH') }}</option>
        <option value="MEDIUM">{{ t('priority.MEDIUM') }}</option>
        <option value="LOW">{{ t('priority.LOW') }}</option>
      </select>
      <select v-model="filters.incidentTypeCd" @change="search">
        <option value="">{{ t('common.all') }} {{ t('incident.type') }}</option>
        <option v-for="tp in incidentTypes" :key="tp.code" :value="tp.code">{{ tp.name }}</option>
      </select>
      <div class="search-box">
        <input v-model="filters.keyword" :placeholder="t('incident.searchPlaceholder')" @keyup.enter="search" />
        <button class="btn btn-sm" @click="search">{{ t('common.search') }}</button>
      </div>
    </div>

    <BaseTable :columns="columns" :data="incidents" :loading="loading" :empty-message="t('incident.noData')"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { incidentApi } from '@/api/incident.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseTable from '@/components/common/BaseTable.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'
import BaseSlaBar from '@/components/common/BaseSlaBar.vue'

const { t } = useI18n()
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

const columns = computed(() => [
  { key: 'incidentId', label: t('incident.id'), width: '60px', align: 'center' },
  { key: 'title', label: t('incident.incidentTitle') },
  { key: 'incidentTypeCd', label: t('incident.type'), width: '120px' },
  { key: 'priorityCd', label: t('incident.priority'), width: '100px', align: 'center' },
  { key: 'statusCd', label: t('incident.status'), width: '100px', align: 'center' },
  { key: 'companyNm', label: t('incident.company'), width: '120px' },
  { key: 'mainManagerNm', label: t('incident.mainManager'), width: '100px' },
  { key: 'slaPercentage', label: t('incident.slaPercentage'), width: '140px' },
  { key: 'occurredAt', label: t('incident.occurredAt'), width: '150px' }
])

const priorityLabel = (code) => {
  return t(`priority.${code}`, code)
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
