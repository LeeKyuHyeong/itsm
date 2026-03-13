<template>
  <div class="report-list">
    <div class="page-header">
      <h2>{{ t('report.list') }}</h2>
    </div>

    <div class="filter-bar">
      <select v-model="filters.refType" @change="search">
        <option value="">{{ t('report.allRefType') }}</option>
        <option value="INCIDENT">{{ t('report.incident') }}</option>
        <option value="CHANGE">{{ t('report.change') }}</option>
        <option value="INSPECTION">{{ t('report.inspection') }}</option>
      </select>
      <div class="search-box">
        <input v-model="filters.refId" :placeholder="t('report.refIdPlaceholder')" @keyup.enter="search" />
        <button class="btn btn-sm" @click="search">{{ t('common.search') }}</button>
      </div>
    </div>

    <BaseTable :columns="columns" :data="reports" :loading="loading" :empty-message="t('report.noData')"
               @row-click="goDetail">
      <template #createdAt="{ row }">
        {{ formatDate(row.createdAt) }}
      </template>
    </BaseTable>

    <BasePagination v-if="totalPages > 1" :current-page="page" :total-pages="totalPages"
                    :total-elements="totalElements" @page-change="changePage" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { reportApi } from '@/api/report.js'
import BaseTable from '@/components/common/BaseTable.vue'
import BasePagination from '@/components/common/BasePagination.vue'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const reports = ref([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const filters = reactive({
  refType: '',
  refId: ''
})

const columns = computed(() => [
  { key: 'reportId', label: 'ID', width: '60px', align: 'center' },
  { key: 'formNm', label: t('report.formNm'), width: '200px' },
  { key: 'refType', label: t('report.refType'), width: '100px' },
  { key: 'refId', label: t('report.refId'), width: '80px', align: 'center' },
  { key: 'createdAt', label: t('report.createdAt'), width: '150px' }
])

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
    if (filters.refType) params.refType = filters.refType
    if (filters.refId) params.refId = filters.refId

    const res = await reportApi.getList(params)
    const data = res.data.data || res.data
    reports.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e) {
    console.error('fetchList failed:', e)
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
  router.push(`/reports/${row.reportId}`)
}

onMounted(fetchList)
</script>

<style scoped>
.report-list { padding: var(--spacing-lg); }
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
  border-radius: 4px; font-size: var(--font-size-sm); width: 120px;
}
.btn {
  padding: 6px 16px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-sm {
  padding: 6px 12px; background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
}
</style>
