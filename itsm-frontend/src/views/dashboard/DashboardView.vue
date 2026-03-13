<template>
  <div class="dashboard">
    <h2>{{ t('dashboard.title') }}</h2>

    <!-- 요약 카드 (4개) -->
    <div class="summary-grid" v-if="stats">
      <div class="summary-card summary-incident" @click="$router.push('/incidents')">
        <div class="summary-icon">&#x1F6A8;</div>
        <div class="summary-info">
          <span class="summary-count">{{ stats.totalIncidentCount || 0 }}</span>
          <span class="summary-label">{{ t('dashboard.incident') }}</span>
        </div>
      </div>
      <div class="summary-card summary-sr" @click="$router.push('/service-requests')">
        <div class="summary-icon">&#x1F4CB;</div>
        <div class="summary-info">
          <span class="summary-count">{{ stats.totalSrCount || 0 }}</span>
          <span class="summary-label">{{ t('dashboard.serviceRequest') }}</span>
        </div>
      </div>
      <div class="summary-card summary-change" @click="$router.push('/changes')">
        <div class="summary-icon">&#x1F504;</div>
        <div class="summary-info">
          <span class="summary-count">{{ stats.totalChangeCount || 0 }}</span>
          <span class="summary-label">{{ t('dashboard.changeManagement') }}</span>
        </div>
      </div>
      <div class="summary-card summary-inspection" @click="$router.push('/inspections')">
        <div class="summary-icon">&#x1F50D;</div>
        <div class="summary-info">
          <span class="summary-count">{{ stats.totalInspectionCount || 0 }}</span>
          <span class="summary-label">{{ t('dashboard.inspection') }}</span>
        </div>
      </div>
    </div>

    <!-- 자산 분류별 현황 -->
    <div class="asset-category-card" v-if="assetStats">
      <h3>{{ t('dashboard.assetByCategory') }}</h3>
      <div class="asset-category-grid">
        <div class="asset-cat-item" v-for="(count, cat) in assetStats.categoryCounts" :key="cat">
          <span class="asset-cat-count">{{ count }}</span>
          <span class="asset-cat-label">{{ t(`asset.category${catKey(cat)}`) }}</span>
        </div>
      </div>
    </div>

    <div class="stats-grid" v-if="stats">
      <!-- 상태별 장애 건수 -->
      <div class="stat-card">
        <h3>{{ t('dashboard.incidentStatus') }}</h3>
        <div class="status-stats">
          <div v-for="(count, status) in stats.statusCounts" :key="status" class="status-item">
            <BaseStatusBadge :status="status" />
            <span class="count">{{ count }}{{ t('common.count', { n: '' }) }}</span>
          </div>
        </div>
      </div>

      <!-- SLA 현황 -->
      <div class="stat-card sla-card">
        <h3>{{ t('dashboard.slaStatus') }}</h3>
        <div class="sla-stats">
          <div class="sla-item danger">
            <span class="sla-number">{{ stats.slaOverdueCount }}</span>
            <span class="sla-label">{{ t('dashboard.slaOverdue') }}</span>
          </div>
          <div class="sla-item warning">
            <span class="sla-number">{{ stats.slaWarningCount }}</span>
            <span class="sla-label">{{ t('dashboard.slaWarning') }}</span>
          </div>
        </div>
      </div>

      <!-- 운영 모니터링 -->
      <div class="stat-card monitoring-card">
        <h3>{{ t('dashboard.monitoring') }}</h3>
        <div class="monitoring-stats">
          <div class="monitoring-item" :class="{ alert: stats.unassignedIncidentCount > 0 }">
            <span class="monitoring-number">{{ stats.unassignedIncidentCount }}</span>
            <span class="monitoring-label">{{ t('dashboard.unassignedIncident') }}</span>
          </div>
          <div class="monitoring-item" :class="{ alert: stats.delayedIncidentCount > 0 }">
            <span class="monitoring-number">{{ stats.delayedIncidentCount }}</span>
            <span class="monitoring-label">{{ t('dashboard.delayedIncident') }}</span>
          </div>
          <div class="monitoring-item" :class="{ alert: stats.pendingSrCount > 0 }">
            <span class="monitoring-number">{{ stats.pendingSrCount }}</span>
            <span class="monitoring-label">{{ t('dashboard.pendingSr') }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- SR / 변경 / 점검 상태 -->
    <div class="detail-grid" v-if="stats">
      <div class="stat-card">
        <h3>{{ t('dashboard.srStatus') }}</h3>
        <div class="status-stats">
          <div v-for="(count, status) in stats.srStatusCounts" :key="status" class="status-item">
            <BaseStatusBadge :status="status" />
            <span class="count">{{ count }}{{ t('common.count', { n: '' }) }}</span>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <h3>{{ t('dashboard.changeStatus') }}</h3>
        <div class="status-stats">
          <div v-for="(count, status) in stats.changeStatusCounts" :key="status" class="status-item">
            <BaseStatusBadge :status="status" />
            <span class="count">{{ count }}{{ t('common.count', { n: '' }) }}</span>
          </div>
        </div>
      </div>

      <div class="stat-card">
        <h3>{{ t('dashboard.inspectionStatus') }}</h3>
        <div class="status-stats">
          <div v-for="(count, status) in stats.inspectionStatusCounts" :key="status" class="status-item">
            <BaseStatusBadge :status="status" />
            <span class="count">{{ count }}{{ t('common.count', { n: '' }) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 월별 추이 -->
    <div class="trend-card" v-if="stats && stats.monthlyTrend">
      <h3>{{ t('dashboard.monthlyTrend') }}</h3>
      <table class="trend-table">
        <thead>
          <tr>
            <th>{{ t('dashboard.month') }}</th>
            <th>{{ t('dashboard.incident') }}</th>
            <th>{{ t('dashboard.serviceRequest') }}</th>
            <th>{{ t('dashboard.changeManagement') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in stats.monthlyTrend" :key="item.month">
            <td>{{ item.month }}</td>
            <td>
              <div class="bar-cell">
                <div class="bar bar-incident" :style="{ width: barWidth(item.incidentCount) }"></div>
                <span>{{ item.incidentCount }}</span>
              </div>
            </td>
            <td>
              <div class="bar-cell">
                <div class="bar bar-sr" :style="{ width: barWidth(item.srCount) }"></div>
                <span>{{ item.srCount }}</span>
              </div>
            </td>
            <td>
              <div class="bar-cell">
                <div class="bar bar-change" :style="{ width: barWidth(item.changeCount) }"></div>
                <span>{{ item.changeCount }}</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 우선순위별 미처리 -->
    <div class="priority-card" v-if="stats">
      <h3>{{ t('dashboard.priorityOpen') }}</h3>
      <div class="priority-stats">
        <div v-for="(count, priority) in stats.priorityCounts" :key="priority" class="priority-item">
          <span :class="['priority-badge', `priority-${priority}`]">{{ t(`priority.${priority}`) }}</span>
          <span class="count">{{ count }}{{ t('common.count', { n: '' }) }}</span>
        </div>
      </div>
    </div>

    <!-- 최근 장애 목록 -->
    <div class="recent-card" v-if="stats">
      <h3>{{ t('dashboard.recentIncidents') }}</h3>
      <BaseTable :columns="columns" :data="stats.recentIncidents || []" :loading="loading"
                 :empty-message="t('dashboard.noIncidents')" @row-click="goDetail">
        <template #priorityCd="{ row }">
          <span :class="['priority-badge', `priority-${row.priorityCd}`]">{{ t(`priority.${row.priorityCd}`) }}</span>
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import api from '@/api/index.js'
import { assetStatApi } from '@/api/asset.js'
import BaseTable from '@/components/common/BaseTable.vue'
import BaseStatusBadge from '@/components/common/BaseStatusBadge.vue'

const { t } = useI18n()
const router = useRouter()
const stats = ref(null)
const assetStats = ref(null)
const loading = ref(false)

const catKey = (cat) => {
  const map = { INFRA_HW: 'InfraHw', INFRA_SW: 'InfraSw', OA: 'Oa' }
  return map[cat] || cat
}

const columns = computed(() => [
  { key: 'incidentId', label: 'ID', width: '60px', align: 'center' },
  { key: 'title', label: t('incident.incidentTitle') },
  { key: 'priorityCd', label: t('incident.priority'), width: '100px', align: 'center' },
  { key: 'statusCd', label: t('incident.status'), width: '100px', align: 'center' },
  { key: 'companyNm', label: t('incident.company'), width: '120px' },
  { key: 'occurredAt', label: t('incident.occurredAt'), width: '150px' }
])

const maxTrendCount = computed(() => {
  if (!stats.value || !stats.value.monthlyTrend) return 1
  let max = 1
  stats.value.monthlyTrend.forEach(item => {
    max = Math.max(max, item.incidentCount, item.srCount, item.changeCount)
  })
  return max
})

const barWidth = (count) => {
  const pct = Math.min((count / maxTrendCount.value) * 100, 100)
  return pct + '%'
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

const loadAssetStats = async () => {
  try {
    const res = await assetStatApi.getStats()
    assetStats.value = res.data.data || res.data
  } catch (e) {
    console.error('자산 통계 조회 실패:', e)
  }
}

onMounted(() => {
  loadStats()
  loadAssetStats()
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

/* Summary cards */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}
.summary-card {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.summary-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}
.summary-icon {
  font-size: 32px;
}
.summary-info {
  display: flex;
  flex-direction: column;
}
.summary-count {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}
.summary-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}
.summary-incident { border-left: 4px solid var(--color-dashboard-incident); }
.summary-sr { border-left: 4px solid var(--color-dashboard-sr); }
.summary-change { border-left: 4px solid var(--color-dashboard-change); }
.summary-inspection { border-left: 4px solid var(--color-dashboard-inspection); }

/* Stats grid */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

/* Detail grid (SR/Change/Inspection) */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.stat-card {
  background: var(--color-bg-white);
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

/* SLA */
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
  color: var(--color-dashboard-incident);
}
.sla-item.warning .sla-number {
  color: var(--color-dashboard-change);
}
.sla-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

/* Monitoring */
.monitoring-stats {
  display: flex;
  gap: var(--spacing-lg);
  justify-content: center;
}
.monitoring-item {
  text-align: center;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: 8px;
  background: var(--color-stats-row-bg);
}
.monitoring-item.alert {
  background: var(--color-notice-error-bg);
}
.monitoring-number {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-dashboard-inspection);
}
.monitoring-item.alert .monitoring-number {
  color: var(--color-dashboard-incident);
}
.monitoring-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

/* Priority badges */
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

/* Priority card */
.priority-card {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}
.priority-card h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: var(--font-size-md);
}

/* Trend table */
.trend-card {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}
.trend-card h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: var(--font-size-md);
}
.trend-table {
  width: 100%;
  border-collapse: collapse;
}
.trend-table th,
.trend-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
}
.trend-table th {
  font-weight: 600;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}
.bar-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.bar {
  height: 18px;
  border-radius: 4px;
  min-width: 2px;
  transition: width 0.3s;
}
.bar-incident { background: var(--color-dashboard-chart-bar-incident); }
.bar-sr { background: var(--color-dashboard-sr); }
.bar-change { background: var(--color-dashboard-change); }

/* Recent card */
.recent-card {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-lg);
}
.recent-card h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: var(--font-size-md);
}

/* Asset category card */
.asset-category-card { background: var(--color-bg-white); border: 1px solid var(--color-border); border-radius: 8px; padding: var(--spacing-lg); margin-bottom: var(--spacing-lg); }
.asset-category-card h3 { margin: 0 0 var(--spacing-md) 0; font-size: var(--font-size-md); }
.asset-category-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--spacing-md); }
.asset-cat-item { text-align: center; padding: var(--spacing-md); background: var(--color-stats-row-bg); border-radius: 8px; }
.asset-cat-count { display: block; font-size: 28px; font-weight: 700; color: var(--color-primary); }
.asset-cat-label { font-size: var(--font-size-sm); color: var(--color-text-muted); }
</style>
