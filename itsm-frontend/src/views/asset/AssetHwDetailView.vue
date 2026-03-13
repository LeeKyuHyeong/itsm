<template>
  <div class="asset-hw-detail">
    <div class="page-header">
      <div class="page-header-left">
        <button class="btn btn-default" @click="$router.push({ name: 'AssetHwList' })">&larr; {{ t('common.list') }}</button>
        <h1 class="page-title">{{ t('asset.hwDetail') }}</h1>
      </div>
      <div class="page-header-right">
        <button v-if="asset && asset.status === 'ACTIVE'" class="btn btn-default" @click="changeStatus('INACTIVE')">비활성화</button>
        <button v-if="asset && asset.status === 'INACTIVE'" class="btn btn-default" @click="changeStatus('ACTIVE')">활성화</button>
        <button v-if="asset && asset.status !== 'DISPOSED'" class="btn btn-danger" @click="changeStatus('DISPOSED')">폐기</button>
      </div>
    </div>

    <div v-if="loading" class="loading-text">로딩 중...</div>

    <template v-if="asset">
      <!-- 기본 정보 -->
      <div class="detail-card">
        <h2 class="card-title">기본 정보</h2>
        <div class="detail-grid">
          <div class="detail-item"><span class="detail-label">{{ t('asset.assetNm') }}</span><span class="detail-value">{{ asset.assetNm }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.assetType') }}</span><span class="detail-value">{{ getCodeName('ASSET_HW_TYPE', asset.assetTypeCd) }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.manufacturer') }}</span><span class="detail-value">{{ asset.manufacturer || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.model') }}</span><span class="detail-value">{{ asset.modelNm || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.serialNo') }}</span><span class="detail-value">{{ asset.serialNo || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">IP주소</span><span class="detail-value">{{ asset.ipAddress || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">MAC주소</span><span class="detail-value">{{ asset.macAddress || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.location') }}</span><span class="detail-value">{{ asset.location || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">도입일</span><span class="detail-value">{{ asset.introducedAt || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">유지보수 만료일</span><span class="detail-value">{{ asset.warrantyEndAt || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.company') }}</span><span class="detail-value">{{ asset.companyNm || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">담당자</span><span class="detail-value">{{ asset.managerNm || '-' }}</span></div>
          <div class="detail-item"><span class="detail-label">{{ t('asset.status') }}</span>
            <span :class="['status-badge', `status-${asset.status?.toLowerCase()}`]">{{ statusLabel(asset.status) }}</span>
          </div>
          <div class="detail-item"><span class="detail-label">등록일</span><span class="detail-value">{{ formatDate(asset.createdAt) }}</span></div>
        </div>
        <div v-if="asset.description" class="detail-item full-width">
          <span class="detail-label">비고</span>
          <span class="detail-value">{{ asset.description }}</span>
        </div>
      </div>

      <!-- 연관 SW -->
      <div class="detail-card">
        <div class="card-header">
          <h2 class="card-title">연관 SW 자산</h2>
          <button class="btn btn-sm btn-primary" @click="showRelationModal = true">+ SW 연결</button>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>SW명</th>
              <th>설치일</th>
              <th>등록일</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="relations.length === 0">
              <td colspan="4" class="text-center">연관 SW가 없습니다.</td>
            </tr>
            <tr v-for="r in relations" :key="r.assetSwId">
              <td>
                <router-link :to="{ name: 'AssetSwDetail', params: { id: r.assetSwId } }" class="link">
                  {{ r.assetSwNm || `SW#${r.assetSwId}` }}
                </router-link>
              </td>
              <td>{{ r.installedAt || '-' }}</td>
              <td>{{ formatDate(r.createdAt) }}</td>
              <td><button class="btn btn-sm btn-danger" @click="removeRelation(r.assetSwId)">해제</button></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 변경이력 -->
      <div class="detail-card">
        <h2 class="card-title">변경 이력</h2>
        <table class="data-table">
          <thead>
            <tr>
              <th>변경항목</th>
              <th>변경 전</th>
              <th>변경 후</th>
              <th>변경일시</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="histories.length === 0">
              <td colspan="4" class="text-center">변경 이력이 없습니다.</td>
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

    <!-- SW 연결 모달 -->
    <div v-if="showRelationModal" class="modal-overlay" @click.self="showRelationModal = false">
      <div class="modal-card">
        <div class="modal-header">
          <h2 class="modal-title">SW 자산 연결</h2>
          <button class="modal-close" @click="showRelationModal = false">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="addRelation">
          <div class="form-group">
            <label class="form-label">SW 자산 <span class="required">*</span></label>
            <select v-model="relationForm.assetSwId" class="form-input" required>
              <option value="">선택</option>
              <option v-for="sw in swAssets" :key="sw.assetSwId" :value="sw.assetSwId">
                {{ sw.swNm }} ({{ sw.version || '-' }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">설치일</label>
            <input v-model="relationForm.installedAt" type="date" class="form-input" />
          </div>
          <div v-if="relationError" class="error-message">{{ relationError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="showRelationModal = false">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary">연결</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { assetHwApi, assetSwApi } from '@/api/asset.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'

const { t } = useI18n()
const route = useRoute()
const commonCodeStore = useCommonCodeStore()
const assetHwId = Number(route.params.id)

const asset = ref(null)
const relations = ref([])
const histories = ref([])
const swAssets = ref([])
const loading = ref(false)
const showRelationModal = ref(false)
const relationError = ref('')
const relationForm = reactive({ assetSwId: '', installedAt: '' })

function statusLabel(status) {
  const map = { ACTIVE: '활성', INACTIVE: '비활성', DISPOSED: '폐기' }
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
  await commonCodeStore.fetchCodes('ASSET_HW_TYPE')
  loadDetail()
  loadRelations()
  loadHistory()
  loadSwAssets()
})

async function loadDetail() {
  loading.value = true
  try {
    const { data } = await assetHwApi.getDetail(assetHwId)
    asset.value = data.data || data
  } catch (e) {
    console.error('자산 상세 로드 실패:', e)
  } finally {
    loading.value = false
  }
}

async function loadRelations() {
  try {
    const { data } = await assetHwApi.getRelations(assetHwId)
    relations.value = data.data || data || []
  } catch (e) { console.error('연관관계 로드 실패:', e) }
}

async function loadHistory() {
  try {
    const { data } = await assetHwApi.getHistory(assetHwId)
    histories.value = data.data || data || []
  } catch (e) { console.error('이력 로드 실패:', e) }
}

async function loadSwAssets() {
  try {
    const { data } = await assetSwApi.getList({ size: 200, status: 'ACTIVE' })
    const result = data.data || data
    swAssets.value = result.content || result || []
  } catch (e) { console.error('SW 자산 목록 로드 실패:', e) }
}

async function changeStatus(status) {
  if (!confirm(`상태를 '${statusLabel(status)}'(으)로 변경하시겠습니까?`)) return
  try {
    await assetHwApi.changeStatus(assetHwId, status)
    loadDetail()
    loadHistory()
  } catch (e) {
    alert(e.response?.data?.error?.message || '상태 변경 실패')
  }
}

async function addRelation() {
  relationError.value = ''
  try {
    const payload = {
      assetHwId,
      assetSwId: Number(relationForm.assetSwId),
      installedAt: relationForm.installedAt || null
    }
    await assetHwApi.addRelation(payload)
    showRelationModal.value = false
    relationForm.assetSwId = ''
    relationForm.installedAt = ''
    loadRelations()
  } catch (e) {
    relationError.value = e.response?.data?.error?.message || '연결 실패'
  }
}

async function removeRelation(assetSwId) {
  if (!confirm(t('message.deleteConfirm'))) return
  try {
    await assetHwApi.removeRelation(assetHwId, assetSwId)
    loadRelations()
  } catch (e) {
    alert(e.response?.data?.error?.message || '해제 실패')
  }
}
</script>

<style scoped>
.asset-hw-detail { max-width: 1200px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-lg); }
.page-header-left { display: flex; align-items: center; gap: var(--spacing-md); }
.page-header-right { display: flex; gap: var(--spacing-sm); }
.page-title { font-size: var(--font-size-xl); font-weight: 700; color: var(--color-text); }
.loading-text { text-align: center; padding: var(--spacing-xl); color: var(--color-text-secondary); }
.detail-card { background: var(--color-bg-white); border-radius: var(--radius-md); padding: var(--spacing-lg); margin-bottom: var(--spacing-lg); box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-md); }
.card-title { font-size: var(--font-size-lg); font-weight: 700; margin-bottom: var(--spacing-md); }
.card-header .card-title { margin-bottom: 0; }
.detail-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--spacing-md); }
.detail-item { display: flex; flex-direction: column; gap: 2px; }
.detail-item.full-width { margin-top: var(--spacing-md); }
.detail-label { font-size: var(--font-size-xs); color: var(--color-text-secondary); font-weight: 500; }
.detail-value { font-size: var(--font-size-sm); color: var(--color-text); }
.status-badge { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: var(--font-size-xs); font-weight: 600; }
.status-active { background: #e6f4ea; color: #1e7e34; }
.status-inactive { background: #fef3cd; color: #856404; }
.status-disposed { background: #f8d7da; color: #721c24; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 10px 14px; text-align: left; border-bottom: 1px solid var(--color-border); font-size: var(--font-size-sm); }
.data-table th { background: #f8f9fa; font-weight: 600; color: var(--color-text-secondary); }
.text-center { text-align: center; }
.link { color: var(--color-primary); text-decoration: none; }
.link:hover { text-decoration: underline; }
.btn { padding: 6px 16px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-size-sm); background: var(--color-bg-white); }
.btn-primary { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.btn-danger { background: var(--color-danger); color: #fff; border-color: var(--color-danger); }
.btn-default:hover { background: #f1f3f4; }
.btn-sm { padding: 3px 10px; font-size: var(--font-size-xs); }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: var(--color-bg-white); border-radius: var(--radius-md); box-shadow: var(--shadow-lg); width: 90%; max-width: 500px; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: var(--spacing-lg); border-bottom: 1px solid var(--color-border); }
.modal-title { font-size: var(--font-size-lg); font-weight: 700; }
.modal-close { background: none; border: none; font-size: 24px; cursor: pointer; color: var(--color-text-secondary); }
.modal-body { padding: var(--spacing-lg); }
.modal-footer { display: flex; justify-content: flex-end; gap: var(--spacing-sm); padding-top: var(--spacing-lg); }
.form-group { margin-bottom: var(--spacing-md); }
.form-label { display: block; font-size: var(--font-size-sm); font-weight: 500; margin-bottom: var(--spacing-xs); }
.required { color: var(--color-danger); }
.form-input { width: 100%; padding: 8px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-size: var(--font-size-sm); box-sizing: border-box; }
.error-message { color: var(--color-danger); font-size: var(--font-size-sm); margin-bottom: var(--spacing-sm); }
</style>
