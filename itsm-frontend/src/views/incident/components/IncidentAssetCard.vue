<template>
  <div class="detail-card">
    <div class="card-header">
      <h3>{{ t('incident.linkedAssets') }}</h3>
      <button v-if="canEdit" class="btn btn-sm" @click="openModal">{{ t('incident.linkAsset') }}</button>
    </div>

    <div v-if="assets.length === 0" class="empty-state">{{ t('incident.noLinkedAsset') }}</div>
    <div v-else class="asset-list">
      <div v-for="a in assets" :key="`${a.assetType}-${a.assetId}`" class="asset-item">
        <span class="asset-type-badge">{{ a.assetType }}</span>
        <span class="asset-name">{{ a.assetNm || `#${a.assetId}` }}</span>
        <span class="asset-date">{{ formatDate(a.createdAt) }}</span>
        <button v-if="canEdit" class="btn-link danger" @click="$emit('unlink-asset', a.assetType, a.assetId)">
          {{ t('incident.unlinkAsset') }}
        </button>
      </div>
    </div>

    <!-- 자산 연결 모달: 유형(HW/SW/OA) → 자산 목록에서 선택 -->
    <BaseModal :show="showModal" :title="t('incident.linkAsset')" @close="showModal = false">
      <div class="form-group">
        <label>{{ t('incident.assetType') }}</label>
        <select v-model="assetType" class="form-input asset-type" @change="loadCandidates">
          <option v-for="type in ASSET_TYPES" :key="type" :value="type">{{ type }}</option>
        </select>
      </div>
      <div class="form-group">
        <label>{{ t('asset.assetNm') }}</label>
        <select v-model="selectedAssetId" class="form-input asset-picker" :disabled="loadingCandidates">
          <option value="">{{ t('common.selectPlaceholder') }}</option>
          <option v-for="c in candidates" :key="c.id" :value="String(c.id)">{{ c.label }}</option>
        </select>
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showModal = false">{{ t('common.cancel') }}</button>
        <button class="btn btn-primary" :disabled="!selectedAssetId" @click="handleLink">{{ t('common.add') }}</button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatDate } from '@/utils/date.js'
import BaseModal from '@/components/common/BaseModal.vue'
import { assetHwApi, assetSwApi, assetOaApi } from '@/api/asset.js'

/**
 * 장애 ↔ 자산(CMDB) 연결 카드 (2026-09-16 전수조사 P2).
 * 백엔드 /incidents/{id}/assets 는 있었지만 부르는 화면이 없어 "모든 모듈이 자산에 연결" 설계가 화면에는 없었다.
 */
const { t } = useI18n()

defineProps({
  assets: { type: Array, default: () => [] },
  canEdit: { type: Boolean, default: false }
})
const emit = defineEmits(['link-asset', 'unlink-asset'])

const ASSET_TYPES = ['HW', 'SW', 'OA']
const showModal = ref(false)
const assetType = ref('HW')
const selectedAssetId = ref('')
const candidates = ref([])
const loadingCandidates = ref(false)

const API_BY_TYPE = {
  HW: { api: assetHwApi, id: (a) => a.assetHwId, label: (a) => (a.serialNo ? `${a.assetNm} (${a.serialNo})` : a.assetNm) },
  SW: { api: assetSwApi, id: (a) => a.assetSwId, label: (a) => (a.version ? `${a.swNm} ${a.version}` : a.swNm) },
  OA: { api: assetOaApi, id: (a) => a.assetOaId, label: (a) => (a.serialNo ? `${a.assetNm} (${a.serialNo})` : a.assetNm) }
}

async function loadCandidates() {
  const spec = API_BY_TYPE[assetType.value]
  selectedAssetId.value = ''
  loadingCandidates.value = true
  try {
    const res = await spec.api.getList({ size: 200, status: 'ACTIVE' })
    const data = res.data.data
    const list = Array.isArray(data) ? data : data?.content || []
    candidates.value = list.map((a) => ({ id: spec.id(a), label: spec.label(a) }))
  } catch (e) {
    candidates.value = []
  } finally {
    loadingCandidates.value = false
  }
}

async function openModal() {
  showModal.value = true
  await loadCandidates()
}

function handleLink() {
  if (!selectedAssetId.value) return
  emit('link-asset', { assetType: assetType.value, assetId: Number(selectedAssetId.value) })
  showModal.value = false
}
</script>

<style scoped>
.detail-card {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}
.card-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
}
.empty-state {
  text-align: center;
  padding: var(--spacing-md);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.asset-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border);
  font-size: var(--font-size-sm);
}
.asset-type-badge {
  display: inline-block;
  min-width: 34px;
  text-align: center;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--color-bg-secondary);
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
  font-weight: 600;
}
.asset-name {
  flex: 1;
  color: var(--color-text);
}
.asset-date {
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
}
.form-group {
  margin-bottom: var(--spacing-md);
}
.form-group label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-weight: 600;
  font-size: var(--font-size-sm);
}
.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  box-sizing: border-box;
  background: var(--color-bg-white);
  color: var(--color-text);
}
.btn-link {
  background: none;
  border: none;
  color: var(--color-primary);
  cursor: pointer;
  font-size: var(--font-size-sm);
}
.btn-link.danger {
  color: var(--color-danger);
}
</style>
