<template>
  <div class="asset-oa-detail">
    <div class="page-header">
      <div class="page-header-left">
        <button class="btn btn-default" @click="$router.push({ name: 'AssetOaList' })">&larr; {{ t('common.list') }}</button>
        <h1 class="page-title">{{ t('asset.oaDetail') }}</h1>
      </div>
      <div class="page-header-right">
        <button v-if="asset && asset.status === 'ACTIVE'" class="btn btn-default" @click="changeStatus('INACTIVE')">{{ t('asset.deactivate') }}</button>
        <button v-if="asset && asset.status === 'INACTIVE'" class="btn btn-default" @click="changeStatus('ACTIVE')">{{ t('asset.activate') }}</button>
        <button v-if="asset && asset.status !== 'DISPOSED'" class="btn btn-danger" @click="changeStatus('DISPOSED')">{{ t('asset.dispose') }}</button>
      </div>
    </div>

    <div v-if="loading" class="loading-text">{{ t('common.loading') }}</div>

    <template v-if="asset">
      <!-- 기본 정보 -->
      <div class="detail-card">
        <h2 class="card-title">{{ t('asset.basicInfo') }}</h2>
        <div class="detail-grid">
          <div class="detail-item"><span class="detail-label">{{ t('asset.assetNm') }}</span><span class="detail-value">{{ asset.assetNm }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.assetType') }}</span><span class="detail-value">{{ getCodeName('ASSET_OA_TYPE', asset.assetTypeCd) }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.manufacturer') }}</span><span class="detail-value">{{ asset.manufacturer || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.model') }}</span><span class="detail-value">{{ asset.modelNm || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.serialNo') }}</span><span class="detail-value">{{ asset.serialNo || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.ipAddress') }}</span><span class="detail-value">{{ asset.ipAddress || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.macAddress') }}</span><span class="detail-value">{{ asset.macAddress || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.location') }}</span><span class="detail-value">{{ asset.location || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.introducedAt') }}</span><span class="detail-value">{{ asset.introducedAt || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.warrantyEndAt') }}</span><span class="detail-value">{{ asset.warrantyEndAt || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.company') }}</span><span class="detail-value">{{ asset.companyNm || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.manager') }}</span><span class="detail-value">{{ asset.managerNm || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.status') }}</span>
            <span :class="['status-badge', `status-${asset.status?.toLowerCase()}`]">{{ statusLabel(asset.status) }}</span>
          </div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.createdAt') }}</span><span class="detail-value">{{ formatDate(asset.createdAt) }}</span></div>
        </div>
        <div v-if="asset.description" class="detail-item full-width">
          <span class="detail-label">{{ t('asset.description') }}</span>
          <span class="detail-value">{{ asset.description }}</span>
        </div>
      </div>

      <!-- 변경이력 -->
      <div class="detail-card">
        <h2 class="card-title">{{ t('asset.changeHistory') }}</h2>
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ t('asset.changedField') }}</th>
              <th>{{ t('asset.beforeValue') }}</th>
              <th>{{ t('asset.afterValue') }}</th>
              <th>{{ t('asset.changedAt') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="histories.length === 0">
              <td colspan="4" class="text-center">{{ t('asset.noHistory') }}</td>
            </tr>
            <tr v-for="h in histories" :key="h.historyId">
              <td>{{ h.changedField }}</td>
              <td>{{ h.beforeValue || '-' }}</td>
              <td>{{ h.afterValue || '-' }}</td>
              <td>{{ formatDate(h.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { assetOaApi } from '@/api/asset.js'
import { useToast } from '@/composables/useToast.js'
import { useConfirm } from '@/composables/useConfirm.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'

const { t } = useI18n()
const route = useRoute()
const commonCodeStore = useCommonCodeStore()
const toast = useToast()
const { confirm } = useConfirm()
const assetOaId = Number(route.params.id)

const asset = ref(null)
const histories = ref([])
const loading = ref(false)

function statusLabel(status) {
  const map = { ACTIVE: t('asset.active'), INACTIVE: t('asset.inactive'), DISPOSED: t('asset.disposed') }
  return map[status] || status
}

function getCodeName(group, code) {
  return commonCodeStore.getCodeName(group, code) || code
}

function formatDate(dt) {
  if (!dt) return '-'
  return dt.replace('T', ' ').substring(0, 16)
}

onMounted(async () => {
  await commonCodeStore.fetchCodes('ASSET_OA_TYPE')
  loadDetail()
  loadHistory()
})

async function loadDetail() {
  loading.value = true
  try {
    const { data } = await assetOaApi.getDetail(assetOaId)
    asset.value = data.data || data
  } catch (e) {
    console.error('loadDetail failed:', e)
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  try {
    const { data } = await assetOaApi.getHistory(assetOaId)
    histories.value = data.data || data || []
  } catch (e) { console.error('loadHistory failed:', e) }
}

async function changeStatus(status) {
  if (!await confirm({ message: t('asset.confirmStatusChange', { status: statusLabel(status) }) })) return
  try {
    await assetOaApi.changeStatus(assetOaId, status)
    loadDetail()
    loadHistory()
  } catch (e) {
    toast.error(e.response?.data?.error?.message || t('asset.statusChangeFail'))
  }
}
</script>

<style scoped>
.asset-oa-detail { max-width: 1200px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-lg); }
.page-header-left { display: flex; align-items: center; gap: var(--spacing-md); }
.page-header-right { display: flex; gap: var(--spacing-sm); }
.page-title { font-size: var(--font-size-xl); font-weight: 700; color: var(--color-text); }
.loading-text { text-align: center; padding: var(--spacing-xl); color: var(--color-text-secondary); }
.detail-card { background: var(--color-bg-white); border-radius: var(--radius-md); padding: var(--spacing-lg); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-sm); }
.card-title { font-size: var(--font-size-lg); font-weight: 700; margin-bottom: var(--spacing-md); }
.detail-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--spacing-md); }
.detail-item { display: flex; flex-direction: column; gap: 2px; }
.detail-item.full-width { margin-top: var(--spacing-md); }
.detail-label { font-size: var(--font-size-xs); color: var(--color-text-secondary); font-weight: 500; }
.detail-value { font-size: var(--font-size-sm); color: var(--color-text); }
.status-badge { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: var(--font-size-xs); font-weight: 600; }
.status-active { background: var(--color-badge-green-bg); color: var(--color-badge-green); }
.status-inactive { background: var(--color-badge-yellow-bg); color: var(--color-badge-yellow); }
.status-disposed { background: var(--color-badge-pink-bg); color: var(--color-badge-pink); }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 10px 14px; text-align: left; border-bottom: 1px solid var(--color-border); font-size: var(--font-size-sm); }
.data-table th { background: var(--color-table-header); font-weight: 600; color: var(--color-text-secondary); }
.text-center { text-align: center; }
.btn { padding: 6px 16px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-size-sm); background: var(--color-bg-white); }
.btn-primary { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }
.btn-danger { background: var(--color-danger); color: var(--color-text-inverse); border-color: var(--color-danger); }
.btn-default:hover { background: var(--color-btn-default-hover); }
.btn-sm { padding: 3px 10px; font-size: var(--font-size-xs); }
</style>
