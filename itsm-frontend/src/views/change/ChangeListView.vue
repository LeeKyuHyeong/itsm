<template>
  <div class="change-list">
    <div class="page-header">
      <h2>{{ t('change.list') }}</h2>
      <button class="btn btn-primary" @click="$router.push('/changes/new')">{{ t('change.create') }}</button>
    </div>

    <div class="filter-bar">
      <select v-model="filters.statusCd" @change="search">
        <option value="">{{ t('common.allStatus') }}</option>
        <option value="DRAFT">{{ t('status.DRAFT') }}</option>
        <option value="APPROVAL_REQUESTED">{{ t('status.REQUESTED') }}</option>
        <option value="APPROVED">{{ t('status.APPROVED') }}</option>
        <option value="IN_PROGRESS">{{ t('status.IN_PROGRESS') }}</option>
        <option value="COMPLETED">{{ t('status.COMPLETED') }}</option>
        <option value="CLOSED">{{ t('status.CLOSED') }}</option>
        <option value="REJECTED">{{ t('status.REJECTED') }}</option>
        <option value="CANCELLED">{{ t('status.CANCELLED') }}</option>
      </select>
      <select v-model="filters.priorityCd" @change="search">
        <option value="">{{ t('common.allPriority') }}</option>
        <option value="CRITICAL">{{ t('priority.CRITICAL') }}</option>
        <option value="HIGH">{{ t('priority.HIGH') }}</option>
        <option value="MEDIUM">{{ t('priority.MEDIUM') }}</option>
        <option value="LOW">{{ t('priority.LOW') }}</option>
      </select>
      <select v-model="filters.changeTypeCd" @change="search">
        <option value="">{{ t('common.allType') }}</option>
        <option v-for="tp in changeTypes" :key="tp.code" :value="tp.code">{{ tp.name }}</option>
      </select>
      <div class="search-box">
        <input v-model="filters.keyword" :placeholder="t('common.search')" @keyup.enter="search" />
        <button class="btn btn-sm" @click="search">{{ t('common.search') }}</button>
      </div>
    </div>

    <BaseTable :columns="columns" :data="changes" :loading="loading" :empty-message="t('common.noData')"
               @row-click="goDetail">
      <template #priorityCd="{ row }">
        <span :class="['priority-badge', `priority-${row.priorityCd}`]">
          {{ t(`priority.${row.priorityCd}`) }}
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { changeApi } from '@/api/change.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseTable from '@/components/common/BaseTable.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'

const { t } = useI18n()
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

const columns = computed(() => [
  { key: 'changeId', label: 'ID', width: '60px', align: 'center' },
  { key: 'title', label: t('incident.incidentTitle') },
  { key: 'changeTypeCd', label: t('change.type'), width: '100px' },
  { key: 'priorityCd', label: t('incident.priority'), width: '100px', align: 'center' },
  { key: 'statusCd', label: t('incident.status'), width: '110px', align: 'center' },
  { key: 'companyNm', label: t('incident.company'), width: '120px' },
  { key: 'scheduledAt', label: t('change.scheduledAt'), width: '150px' },
  { key: 'createdAt', label: t('board.createdAt'), width: '150px' }
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
    console.error(t('message.loadFail'), e)
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
    console.error(t('message.loadFail'), e)
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
