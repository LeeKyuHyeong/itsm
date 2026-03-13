<template>
  <div class="asset-hw-list">
    <div class="page-header">
      <h1 class="page-title">{{ t('asset.hwList') }}</h1>
      <button class="btn btn-primary" @click="openCreateModal">+ {{ t('common.create') }}</button>
    </div>

    <!-- 검색/필터 -->
    <div class="filter-bar">
      <div class="filter-group">
        <label class="filter-label">{{ t('asset.status') }}</label>
        <select v-model="filters.status" class="filter-select" @change="loadAssets">
          <option value="">{{ t('common.all') }}</option>
          <option value="ACTIVE">활성</option>
          <option value="INACTIVE">비활성</option>
          <option value="DISPOSED">폐기</option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label">{{ t('asset.assetType') }}</label>
        <select v-model="filters.assetTypeCd" class="filter-select" @change="loadAssets">
          <option value="">{{ t('common.all') }}</option>
          <option v-for="t in assetTypes" :key="t.code" :value="t.code">{{ t.name }}</option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label">{{ t('asset.company') }}</label>
        <select v-model="filters.companyId" class="filter-select" @change="loadAssets">
          <option value="">{{ t('common.all') }}</option>
          <option v-for="c in companies" :key="c.companyId" :value="c.companyId">{{ c.companyNm }}</option>
        </select>
      </div>
      <div class="filter-group search-group">
        <input v-model="filters.keyword" type="text" class="filter-input"
               placeholder="자산명, 시리얼번호, IP주소 검색" @keyup.enter="loadAssets" />
        <button class="btn btn-default" @click="loadAssets">{{ t('common.search') }}</button>
      </div>
    </div>

    <!-- 테이블 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>번호</th>
            <th>{{ t('asset.assetNm') }}</th>
            <th>{{ t('asset.assetType') }}</th>
            <th>{{ t('asset.manufacturer') }}/{{ t('asset.model') }}</th>
            <th>{{ t('asset.serialNo') }}</th>
            <th>IP주소</th>
            <th>{{ t('asset.company') }}</th>
            <th>담당자</th>
            <th>{{ t('asset.status') }}</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="text-center">로딩 중...</td>
          </tr>
          <tr v-else-if="assets.length === 0">
            <td colspan="10" class="text-center">데이터가 없습니다.</td>
          </tr>
          <tr v-for="(asset, index) in assets" :key="asset.assetHwId">
            <td>{{ (pagination.page - 1) * pagination.size + index + 1 }}</td>
            <td>
              <router-link :to="{ name: 'AssetHwDetail', params: { id: asset.assetHwId } }" class="link">
                {{ asset.assetNm }}
              </router-link>
            </td>
            <td>{{ getCodeName('ASSET_HW_TYPE', asset.assetTypeCd) }}</td>
            <td>{{ [asset.manufacturer, asset.modelNm].filter(Boolean).join(' / ') || '-' }}</td>
            <td>{{ asset.serialNo || '-' }}</td>
            <td>{{ asset.ipAddress || '-' }}</td>
            <td>{{ asset.companyNm || '-' }}</td>
            <td>{{ asset.managerNm || '-' }}</td>
            <td><span :class="['status-badge', `status-${asset.status?.toLowerCase()}`]">{{ statusLabel(asset.status) }}</span></td>
            <td>
              <div class="action-buttons">
                <button class="btn btn-sm btn-default" @click="openEditModal(asset)">{{ t('common.edit') }}</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalPages > 1" class="pagination">
      <button class="page-btn" :disabled="pagination.page <= 1" @click="goToPage(pagination.page - 1)">이전</button>
      <button v-for="p in visiblePages" :key="p" class="page-btn"
              :class="{ active: p === pagination.page }" @click="goToPage(p)">{{ p }}</button>
      <button class="page-btn" :disabled="pagination.page >= totalPages" @click="goToPage(pagination.page + 1)">다음</button>
    </div>

    <!-- 등록/수정 모달 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card modal-lg">
        <div class="modal-header">
          <h2 class="modal-title">{{ isEditing ? 'HW 자산 수정' : 'HW 자산 등록' }}</h2>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="saveAsset">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.assetNm') }} <span class="required">*</span></label>
              <input v-model="form.assetNm" type="text" class="form-input" required />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.assetType') }} <span class="required">*</span></label>
              <select v-model="form.assetTypeCd" class="form-input" required>
                <option value="">선택</option>
                <option v-for="t in assetTypes" :key="t.code" :value="t.code">{{ t.name }}</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.manufacturer') }}</label>
              <input v-model="form.manufacturer" type="text" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.model') }}</label>
              <input v-model="form.modelNm" type="text" class="form-input" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.serialNo') }}</label>
              <input v-model="form.serialNo" type="text" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">IP주소</label>
              <input v-model="form.ipAddress" type="text" class="form-input" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">MAC주소</label>
              <input v-model="form.macAddress" type="text" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.location') }}</label>
              <input v-model="form.location" type="text" class="form-input" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">도입일</label>
              <input v-model="form.introducedAt" type="date" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">유지보수 만료일</label>
              <input v-model="form.warrantyEndAt" type="date" class="form-input" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.company') }} <span class="required">*</span></label>
              <select v-model="form.companyId" class="form-input" required :disabled="isEditing">
                <option value="">선택</option>
                <option v-for="c in companies" :key="c.companyId" :value="c.companyId">{{ c.companyNm }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">담당자</label>
              <select v-model="form.managerId" class="form-input">
                <option value="">선택</option>
                <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.userNm }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">비고</label>
            <textarea v-model="form.description" class="form-input form-textarea" rows="3"></textarea>
          </div>

          <div v-if="saveError" class="error-message">{{ saveError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="closeModal">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? '저장 중...' : t('common.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { assetHwApi } from '@/api/asset.js'
import { companyApi } from '@/api/company.js'
import { userApi } from '@/api/user.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'

const { t } = useI18n()
const commonCodeStore = useCommonCodeStore()

const assets = ref([])
const companies = ref([])
const users = ref([])
const assetTypes = ref([])
const loading = ref(false)
const saving = ref(false)
const saveError = ref('')
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)

const filters = reactive({ status: '', assetTypeCd: '', companyId: '', keyword: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })

const form = reactive({
  assetNm: '', assetTypeCd: '', manufacturer: '', modelNm: '',
  serialNo: '', ipAddress: '', macAddress: '', location: '',
  introducedAt: '', warrantyEndAt: '', companyId: '', managerId: '', description: ''
})

const totalPages = computed(() => Math.ceil(pagination.total / pagination.size) || 1)
const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, pagination.page - 2)
  const end = Math.min(totalPages.value, pagination.page + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

function statusLabel(status) {
  const map = { ACTIVE: '활성', INACTIVE: '비활성', DISPOSED: '폐기' }
  return map[status] || status
}

function getCodeName(group, code) {
  return commonCodeStore.getCodeName(group, code) || code
}

onMounted(async () => {
  await commonCodeStore.fetchCodes('ASSET_HW_TYPE')
  assetTypes.value = commonCodeStore.getCodes('ASSET_HW_TYPE')
  loadCompanies()
  loadUsers()
  loadAssets()
})

async function loadCompanies() {
  try {
    const { data } = await companyApi.getList({ size: 100 })
    const result = data.data || data
    companies.value = result.content || result || []
  } catch (e) { console.error('회사 목록 로드 실패:', e) }
}

async function loadUsers() {
  try {
    const { data } = await userApi.getList({ size: 200, status: 'ACTIVE' })
    const result = data.data || data
    users.value = result.content || result || []
  } catch (e) { console.error('사용자 목록 로드 실패:', e) }
}

async function loadAssets() {
  loading.value = true
  try {
    const params = { page: pagination.page - 1, size: pagination.size }
    if (filters.status) params.status = filters.status
    if (filters.assetTypeCd) params.assetTypeCd = filters.assetTypeCd
    if (filters.companyId) params.companyId = filters.companyId
    if (filters.keyword) params.keyword = filters.keyword

    const { data } = await assetHwApi.getList(params)
    const result = data.data || data
    assets.value = result.content || result || []
    pagination.total = result.totalElements || 0
  } catch (e) {
    console.error('자산 목록 로드 실패:', e)
    assets.value = []
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  Object.assign(form, {
    assetNm: '', assetTypeCd: '', manufacturer: '', modelNm: '',
    serialNo: '', ipAddress: '', macAddress: '', location: '',
    introducedAt: '', warrantyEndAt: '', companyId: '', managerId: '', description: ''
  })
  saveError.value = ''
  showModal.value = true
}

function openEditModal(asset) {
  isEditing.value = true
  editingId.value = asset.assetHwId
  Object.assign(form, {
    assetNm: asset.assetNm || '',
    assetTypeCd: asset.assetTypeCd || '',
    manufacturer: asset.manufacturer || '',
    modelNm: asset.modelNm || '',
    serialNo: asset.serialNo || '',
    ipAddress: asset.ipAddress || '',
    macAddress: asset.macAddress || '',
    location: asset.location || '',
    introducedAt: asset.introducedAt || '',
    warrantyEndAt: asset.warrantyEndAt || '',
    companyId: asset.companyId || '',
    managerId: asset.managerId || '',
    description: asset.description || ''
  })
  saveError.value = ''
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function saveAsset() {
  saving.value = true
  saveError.value = ''
  try {
    const payload = { ...form }
    if (!payload.companyId) delete payload.companyId
    if (!payload.managerId) payload.managerId = null
    if (!payload.introducedAt) payload.introducedAt = null
    if (!payload.warrantyEndAt) payload.warrantyEndAt = null

    if (isEditing.value) {
      const { companyId, ...updatePayload } = payload
      await assetHwApi.update(editingId.value, updatePayload)
    } else {
      await assetHwApi.create(payload)
    }
    closeModal()
    loadAssets()
  } catch (e) {
    saveError.value = e.response?.data?.error?.message || e.response?.data?.message || t('message.saveFail')
  } finally {
    saving.value = false
  }
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  pagination.page = page
  loadAssets()
}
</script>

<style scoped>
.asset-hw-list { max-width: 1400px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-lg); }
.page-title { font-size: var(--font-size-xl); font-weight: 700; color: var(--color-text); }
.filter-bar { display: flex; align-items: flex-end; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); flex-wrap: wrap; }
.filter-group { display: flex; flex-direction: column; gap: var(--spacing-xs); }
.filter-label { font-size: var(--font-size-sm); color: var(--color-text-secondary); font-weight: 500; }
.filter-select, .filter-input { padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-size: var(--font-size-sm); }
.search-group { flex-direction: row; align-items: flex-end; gap: var(--spacing-sm); }
.table-container { overflow-x: auto; margin-bottom: var(--spacing-lg); }
.data-table { width: 100%; border-collapse: collapse; background: var(--color-bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.data-table th, .data-table td { padding: 10px 14px; text-align: left; border-bottom: 1px solid var(--color-border); font-size: var(--font-size-sm); white-space: nowrap; }
.data-table th { background: #f8f9fa; font-weight: 600; color: var(--color-text-secondary); }
.data-table tr:hover { background: #f8f9ff; }
.text-center { text-align: center; }
.link { color: var(--color-primary); text-decoration: none; font-weight: 500; }
.link:hover { text-decoration: underline; }
.status-badge { padding: 2px 10px; border-radius: 12px; font-size: var(--font-size-xs); font-weight: 600; }
.status-active { background: #e6f4ea; color: #1e7e34; }
.status-inactive { background: #fef3cd; color: #856404; }
.status-disposed { background: #f8d7da; color: #721c24; }
.action-buttons { display: flex; gap: var(--spacing-xs); }
.btn { padding: 6px 16px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-size-sm); background: var(--color-bg-white); }
.btn-primary { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.btn-primary:hover { background: var(--color-primary-dark); }
.btn-default:hover { background: #f1f3f4; }
.btn-sm { padding: 3px 10px; font-size: var(--font-size-xs); }
.pagination { display: flex; justify-content: center; gap: var(--spacing-xs); }
.page-btn { padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); background: var(--color-bg-white); cursor: pointer; font-size: var(--font-size-sm); }
.page-btn.active { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: var(--color-bg-white); border-radius: var(--radius-md); box-shadow: var(--shadow-lg); width: 90%; max-height: 90vh; overflow-y: auto; }
.modal-lg { max-width: 700px; }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: var(--spacing-lg); border-bottom: 1px solid var(--color-border); }
.modal-title { font-size: var(--font-size-lg); font-weight: 700; }
.modal-close { background: none; border: none; font-size: 24px; cursor: pointer; color: var(--color-text-secondary); }
.modal-body { padding: var(--spacing-lg); }
.modal-footer { display: flex; justify-content: flex-end; gap: var(--spacing-sm); padding-top: var(--spacing-lg); }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-md); }
.form-group { margin-bottom: var(--spacing-md); }
.form-label { display: block; font-size: var(--font-size-sm); font-weight: 500; margin-bottom: var(--spacing-xs); color: var(--color-text); }
.required { color: var(--color-danger); }
.form-input { width: 100%; padding: 8px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-size: var(--font-size-sm); box-sizing: border-box; }
.form-textarea { resize: vertical; }
.error-message { color: var(--color-danger); font-size: var(--font-size-sm); margin-bottom: var(--spacing-sm); }
</style>
