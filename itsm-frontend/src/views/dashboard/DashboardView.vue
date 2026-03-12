<template>
  <div class="dashboard">
    <h2>대시보드</h2>

    <div class="stats-grid" v-if="stats">
      <!-- 상태별 장애 건수 -->
      <div class="stat-card">
        <h3>상태별 장애 현황</h3>
        <div class="status-stats">
          <div v-for="(count, status) in stats.statusCounts" :key="status" class="status-item">
            <BaseStatusBadge :status="status" />
            <span class="count">{{ count }}건</span>
          </div>
        </div>
      </div>

      <!-- 우선순위별 미처리 -->
      <div class="stat-card">
        <h3>우선순위별 미처리</h3>
        <div class="priority-stats">
          <div v-for="(count, priority) in stats.priorityCounts" :key="priority" class="priority-item">
            <span :class="['priority-badge', `priority-${priority}`]">{{ priorityLabel(priority) }}</span>
            <span class="count">{{ count }}건</span>
          </div>
        </div>
      </div>

      <!-- SLA 현황 -->
      <div class="stat-card sla-card">
        <h3>SLA 현황</h3>
        <div class="sla-stats">
          <div class="sla-item danger">
            <span class="sla-number">{{ stats.slaOverdueCount }}</span>
            <span class="sla-label">초과</span>
          </div>
          <div class="sla-item warning">
            <span class="sla-number">{{ stats.slaWarningCount }}</span>
            <span class="sla-label">임박 (80%+)</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 최근 장애 목록 -->
    <div class="recent-card" v-if="stats">
      <h3>최근 장애</h3>
      <BaseTable :columns="columns" :data="stats.recentIncidents || []" :loading="loading"
                 empty-message="등록된 장애가 없습니다." @row-click="goDetail">
        <template #priorityCd="{ row }">
          <span :class="['priority-badge', `priority-${row.priorityCd}`]">{{ priorityLabel(row.priorityCd) }}</span>
        </template>
        <template #statusCd="{ row }">
          <BaseStatusBadge :status="row.statusCd" />
        </template>
        <template #occurredAt="{ row }">
          {{ formatDate(row.occurredAt) }}
        </template>
      </BaseTable>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/index.js'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'

const router = useRouter()
const stats = ref(null)
const loading = ref(false)

const columns = [
  { key: 'incidentId', label: 'ID', width: '60px', align: 'center' },
  { key: 'title', label: '제목' },
  { key: 'priorityCd', label: '우선순위', width: '100px', align: 'center' },
  { key: 'statusCd', label: '상태', width: '100px', align: 'center' },
  { key: 'companyNm', label: '고객사', width: '120px' },
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

const goDetail = (row) => {
  router.push(`/incidents/${row.incidentId}`)
}

const loadStats = async () => {
  loading.value = true
  try {
    const res = await api.get('/dashboard/stats')
    stats.value = res.data.data || res.data
  } catch (e) {
    console.error('대시보드 조회 실패:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  padding: var(--spacing-lg);
}
.dashboard h2 {
  margin: 0 0 var(--spacing-lg) 0;
  font-size: var(--font-size-xl);
}
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}
.stat-card {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-lg);
}
.stat-card h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: var(--font-size-md);
}
.status-stats, .priority-stats {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}
.status-item, .priority-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.count {
  font-weight: 700;
  font-size: var(--font-size-md);
}
.sla-stats {
  display: flex;
  gap: var(--spacing-lg);
  justify-content: center;
}
.sla-item {
  text-align: center;
}
.sla-number {
  display: block;
  font-size: 32px;
  font-weight: 700;
}
.sla-item.danger .sla-number {
  color: #dc2626;
}
.sla-item.warning .sla-number {
  color: #ea580c;
}
.sla-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
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
.recent-card {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-lg);
}
.recent-card h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: var(--font-size-md);
}
</style>
