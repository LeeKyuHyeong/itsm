<template>
  <div class="asset-list">
    <div class="page-header">
      <h1 class="page-title">{{ t('asset.title') }}</h1>
      <button class="btn btn-primary" @click="openCreateModal">+ {{ t('common.create') }}</button>
    </div>

    <!-- 탭 -->
    <div class="tab-bar">
      <button v-for="tab in tabs" :key="tab.key" class="tab-btn" :class="{ active: activeTab === tab.key }" @click="switchTab(tab.key)">
        {{ t(tab.label) }}
      </button>
    </div>

    <!-- 필터 -->
    <div class="filter-bar">
      <div class="filter-group">
        <label class="filter-label">{{ t('asset.subCategory') }}</label>
        <select v-model="filters.assetSubCategory" class="filter-select" @change="loadAssets">
          <option value="">{{ t('common.all') }}</option>
          <option v-for="sc in subCategories" :key="sc.code" :value="sc.code">{{ sc.name }}</option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label">{{ t('asset.status') }}</label>
        <select v-model="filters.status" class="filter-select" @change="loadAssets">
          <option value="">{{ t('common.all') }}</option>
          <option value="ACTIVE">{{ t('asset.active') }}</option>
          <option value="INACTIVE">{{ t('asset.inactive') }}</option>
          <option value="DISPOSED">{{ t('asset.disposed') }}</option>
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
        <input v-model="filters.keyword" type="text" class="filter-input" :placeholder="currentSearchPlaceholder" @keyup.enter="loadAssets" />
        <button class="btn btn-default" @click="loadAssets">{{ t('common.search') }}</button>
      </div>
    </div>

    <!-- HW 테이블 (INFRA_HW, OA) -->
    <div v-if="isHwTab" class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('asset.no') }}</th>
            <th>{{ t('asset.assetNm') }}</th>
            <th>{{ t('asset.subCategory') }}</th>
            <th>{{ t('asset.manufacturer') }}/{{ t('asset.model') }}</th>
            <th>{{ t('asset.serialNo') }}</th>
            <th>{{ t('asset.ipAddress') }}</th>
            <th>{{ t('asset.company') }}</th>
            <th>{{ t('asset.manager') }}</th>
            <th>{{ t('asset.status') }}</th>
            <th>{{ t('common.manage') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="10" class="text-center">{{ t('common.loading') }}</td></tr>
          <tr v-else-if="assets.length === 0"><td colspan="10" class="text-center">{{ t('common.noData') }}</td></tr>
          <tr v-for="(asset, index) in assets" :key="asset.assetHwId">
            <td>{{ (pagination.page - 1) * pagination.size + index + 1 }}</td>
            <td><router-link :to="{ name: 'AssetHwDetail', params: { id: asset.assetHwId } }" class="link">{{ asset.assetNm }}</router-link></td>
            <td>{{ getSubCategoryName(asset.assetSubCategory) }}</td>
            <td>{{ [asset.manufacturer, asset.modelNm].filter(Boolean).join(' / ') || '-' }}</td>
            <td>{{ asset.serialNo || '-' }}</td>
            <td>{{ asset.ipAddress || '-' }}</td>
            <td>{{ asset.companyNm || '-' }}</td>
            <td>{{ asset.managerNm || '-' }}</td>
            <td><span :class="['status-badge', `status-${asset.status?.toLowerCase()}`]">{{ statusLabel(asset.status) }}</span></td>
            <td><button class="btn btn-sm btn-default" @click="openEditModal(asset)">{{ t('common.edit') }}</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- SW 테이블 (INFRA_SW) -->
    <div v-else class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('asset.no') }}</th>
            <th>{{ t('asset.swNm') }}</th>
            <th>{{ t('asset.subCategory') }}</th>
            <th>{{ t('asset.version') }}</th>
            <th>{{ t('asset.license') }}</th>
            <th>{{ t('asset.expiredAt') }}</th>
            <th>{{ t('asset.company') }}</th>
            <th>{{ t('asset.manager') }}</th>
            <th>{{ t('asset.status') }}</th>
            <th>{{ t('common.manage') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="10" class="text-center">{{ t('common.loading') }}</td></tr>
          <tr v-else-if="assets.length === 0"><td colspan="10" class="text-center">{{ t('common.noData') }}</td></tr>
          <tr v-for="(asset, index) in assets" :key="asset.assetSwId">
            <td>{{ (pagination.page - 1) * pagination.size + index + 1 }}</td>
            <td><router-link :to="{ name: 'AssetSwDetail', params: { id: asset.assetSwId } }" class="link">{{ asset.swNm }}</router-link></td>
            <td>{{ getSubCategoryName(asset.assetSubCategory) }}</td>
            <td>{{ asset.version || '-' }}</td>
            <td>{{ asset.licenseCnt != null ? t('asset.countUnit', { n: asset.licenseCnt }) : '-' }}</td>
            <td>{{ asset.expiredAt || '-' }}</td>
            <td>{{ asset.companyNm || '-' }}</td>
            <td>{{ asset.managerNm || '-' }}</td>
            <td><span :class="['status-badge', `status-${asset.status?.toLowerCase()}`]">{{ statusLabel(asset.status) }}</span></td>
            <td><button class="btn btn-sm btn-default" @click="openEditModal(asset)">{{ t('common.edit') }}</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalPages > 1" class="pagination">
      <button class="page-btn" :disabled="pagination.page <= 1" @click="goToPage(pagination.page - 1)">{{ t('common.prev') }}</button>
      <button v-for="p in visiblePages" :key="p" class="page-btn"
              :class="{ active: p === pagination.page }" @click="goToPage(p)">{{ p }}</button>
      <button class="page-btn" :disabled="pagination.page >= totalPages" @click="goToPage(pagination.page + 1)">{{ t('common.next') }}</button>
    </div>

    <!-- HW 등록/수정 모달 (INFRA_HW / OA) -->
    <div v-if="showModal && isHwTab" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card modal-lg">
        <div class="modal-header">
          <h2 class="modal-title">{{ isEditing ? t('asset.hwEdit') : t('asset.hwCreate') }}</h2>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="saveAsset">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.assetNm') }} <span class="required">*</span></label>
              <input v-model="hwForm.assetNm" type="text" class="form-input" required />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.subCategory') }} <span class="required">*</span></label>
              <select v-model="hwForm.assetSubCategory" class="form-input" required>
                <option value="">{{ t('common.select') }}</option>
                <option v-for="sc in subCategories" :key="sc.code" :value="sc.code">{{ sc.name }}</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.manufacturer') }}</label>
              <input v-model="hwForm.manufacturer" type="text" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.model') }}</label>
              <input v-model="hwForm.modelNm" type="text" class="form-input" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.serialNo') }}</label>
              <input v-model="hwForm.serialNo" type="text" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.ipAddress') }}</label>
              <input v-model="hwForm.ipAddress" type="text" class="form-input" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.macAddress') }}</label>
              <input v-model="hwForm.macAddress" type="text" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.location') }}</label>
              <input v-model="hwForm.location" type="text" class="form-input" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.introducedAt') }}</label>
              <BaseDatePicker v-model="hwForm.introducedAt" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.warrantyEndAt') }}</label>
              <BaseDatePicker v-model="hwForm.warrantyEndAt" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.company') }} <span class="required">*</span></label>
              <select v-model="hwForm.companyId" class="form-input" required :disabled="isEditing">
                <option value="">{{ t('common.select') }}</option>
                <option v-for="c in companies" :key="c.companyId" :value="c.companyId">{{ c.companyNm }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.manager') }}</label>
              <select v-model="hwForm.managerId" class="form-input">
                <option value="">{{ t('common.select') }}</option>
                <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.userNm }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('asset.description') }}</label>
            <textarea v-model="hwForm.description" class="form-input form-textarea" rows="3"></textarea>
          </div>

          <div v-if="saveError" class="error-message">{{ saveError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="closeModal">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? t('common.saving') : t('common.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- SW 등록/수정 모달 (INFRA_SW) -->
    <div v-if="showModal && !isHwTab" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card modal-lg">
        <div class="modal-header">
          <h2 class="modal-title">{{ isEditing ? t('asset.swEdit') : t('asset.swCreate') }}</h2>
          <button class="modal-close" @click="closeModal">&times;</button>
        </div>
        <form class="modal-body" @submit.prevent="saveAsset">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.swNm') }} <span class="required">*</span></label>
              <input v-model="swForm.swNm" type="text" class="form-input" required />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.subCategory') }} <span class="required">*</span></label>
              <select v-model="swForm.assetSubCategory" class="form-input" required>
                <option value="">{{ t('common.select') }}</option>
                <option v-for="sc in subCategories" :key="sc.code" :value="sc.code">{{ sc.name }}</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.version') }}</label>
              <input v-model="swForm.version" type="text" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.licenseKey') }}</label>
              <input v-model="swForm.licenseKey" type="text" class="form-input" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.licenseCnt') }}</label>
              <input v-model.number="swForm.licenseCnt" type="number" class="form-input" min="0" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.installedAt') }}</label>
              <BaseDatePicker v-model="swForm.installedAt" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.expiredAt') }}</label>
              <BaseDatePicker v-model="swForm.expiredAt" />
            </div>
            <div class="form-group">
              <label class="form-label">{{ t('asset.company') }} <span class="required">*</span></label>
              <select v-model="swForm.companyId" class="form-input" required :disabled="isEditing">
                <option value="">{{ t('common.select') }}</option>
                <option v-for="c in companies" :key="c.companyId" :value="c.companyId">{{ c.companyNm }}</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">{{ t('asset.manager') }}</label>
              <select v-model="swForm.managerId" class="form-input">
                <option value="">{{ t('common.select') }}</option>
                <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.userNm }}</option>
              </select>
            </div>
            <div class="form-group"></div>
          </div>
          <div class="form-group">
            <label class="form-label">{{ t('asset.description') }}</label>
            <textarea v-model="swForm.description" class="form-input form-textarea" rows="3"></textarea>
          </div>

          <div v-if="saveError" class="error-message">{{ saveError }}</div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" @click="closeModal">{{ t('common.cancel') }}</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? t('common.saving') : t('common.save') }}
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
import { assetHwApi, assetSwApi } from '@/api/asset.js'
import { companyApi } from '@/api/company.js'
import { userApi } from '@/api/user.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import BaseDatePicker from '@/components/common/BaseDatePicker.vue'

const { t } = useI18n()
const commonCodeStore = useCommonCodeStore()

const tabs = [
  { key: 'INFRA_HW', label: 'asset.categoryInfraHw' },
  { key: 'INFRA_SW', label: 'asset.categoryInfraSw' },
  { key: 'OA', label: 'asset.categoryOa' }
]

const activeTab = ref('INFRA_HW')
const isHwTab = computed(() => activeTab.value !== 'INFRA_SW')

const assets = ref([])
const companies = ref([])
const users = ref([])
const subCategories = ref([])
const loading = ref(false)
const saving = ref(false)
const saveError = ref('')
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)

const filters = reactive({ assetSubCategory: '', status: '', companyId: '', keyword: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })

const hwForm = reactive({
  assetNm: '', assetSubCategory: '', manufacturer: '', modelNm: '',
  serialNo: '', ipAddress: '', macAddress: '', location: '',
  introducedAt: '', warrantyEndAt: '', companyId: '', managerId: '', description: ''
})

const swForm = reactive({
  swNm: '', assetSubCategory: '', version: '', licenseKey: '', licenseCnt: null,
  installedAt: '', expiredAt: '', companyId: '', managerId: '', description: ''
})

const subCategoryCodeGroup = computed(() => {
  const map = { INFRA_HW: 'ASSET_SUB_INFRA_HW', INFRA_SW: 'ASSET_SUB_INFRA_SW', OA: 'ASSET_SUB_OA' }
  return map[activeTab.value] || 'ASSET_SUB_INFRA_HW'
})

const currentSearchPlaceholder = computed(() => {
  return isHwTab.value ? t('asset.searchHwPlaceholder') : t('asset.searchSwPlaceholder')
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
  const map = { ACTIVE: t('asset.active'), INACTIVE: t('asset.inactive'), DISPOSED: t('asset.disposed') }
  return map[status] || status
}

function getSubCategoryName(code) {
  if (!code) return '-'
  return commonCodeStore.getCodeName(subCategoryCodeGroup.value, code) || code
}

async function switchTab(key) {
  activeTab.value = key
  Object.assign(filters, { assetSubCategory: '', status: '', companyId: '', keyword: '' })
  pagination.page = 1
  pagination.total = 0
  await loadSubCategories()
  loadAssets()
}

async function loadSubCategories() {
  await commonCodeStore.fetchCodes(subCategoryCodeGroup.value)
  subCategories.value = commonCodeStore.getCodes(subCategoryCodeGroup.value)
}

onMounted(async () => {
  await loadSubCategories()
  loadCompanies()
  loadUsers()
  loadAssets()
})

async function loadCompanies() {
  try {
    const { data } = await companyApi.getList({ size: 100 })
    const result = data.data || data
    companies.value = result.content || result || []
  } catch (e) { console.error('loadCompanies failed:', e) }
}

async function loadUsers() {
  try {
    const { data } = await userApi.getList({ size: 200, status: 'ACTIVE' })
    const result = data.data || data
    users.value = result.content || result || []
  } catch (e) { console.error('loadUsers failed:', e) }
}

async function loadAssets() {
  loading.value = true
  try {
    const params = { page: pagination.page - 1, size: pagination.size, assetCategory: activeTab.value }
    if (filters.assetSubCategory) params.assetSubCategory = filters.assetSubCategory
    if (filters.status) params.status = filters.status
    if (filters.companyId) params.companyId = filters.companyId
    if (filters.keyword) params.keyword = filters.keyword

    const api = isHwTab.value ? assetHwApi : assetSwApi
    const { data } = await api.getList(params)
    const result = data.data || data
    assets.value = result.content || result || []
    pagination.total = result.totalElements || 0
  } catch (e) {
    console.error('loadAssets failed:', e)
    assets.value = []
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  saveError.value = ''
  if (isHwTab.value) {
    Object.assign(hwForm, {
      assetNm: '', assetSubCategory: '', manufacturer: '', modelNm: '',
      serialNo: '', ipAddress: '', macAddress: '', location: '',
      introducedAt: '', warrantyEndAt: '', companyId: '', managerId: '', description: ''
    })
  } else {
    Object.assign(swForm, {
      swNm: '', assetSubCategory: '', version: '', licenseKey: '', licenseCnt: null,
      installedAt: '', expiredAt: '', companyId: '', managerId: '', description: ''
    })
  }
  showModal.value = true
}

function openEditModal(asset) {
  isEditing.value = true
  saveError.value = ''
  if (isHwTab.value) {
    editingId.value = asset.assetHwId
    Object.assign(hwForm, {
      assetNm: asset.assetNm || '',
      assetSubCategory: asset.assetSubCategory || '',
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
  } else {
    editingId.value = asset.assetSwId
    Object.assign(swForm, {
      swNm: asset.swNm || '',
      assetSubCategory: asset.assetSubCategory || '',
      version: asset.version || '',
      licenseKey: asset.licenseKey || '',
      licenseCnt: asset.licenseCnt,
      installedAt: asset.installedAt || '',
      expiredAt: asset.expiredAt || '',
      companyId: asset.companyId || '',
      managerId: asset.managerId || '',
      description: asset.description || ''
    })
  }
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function saveAsset() {
  saving.value = true
  saveError.value = ''
  try {
    if (isHwTab.value) {
      const payload = { ...hwForm, assetCategory: activeTab.value }
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
    } else {
      const payload = { ...swForm, assetCategory: activeTab.value }
      if (!payload.companyId) delete payload.companyId
      if (!payload.managerId) payload.managerId = null
      if (!payload.installedAt) payload.installedAt = null
      if (!payload.expiredAt) payload.expiredAt = null

      if (isEditing.value) {
        const { companyId, ...updatePayload } = payload
        await assetSwApi.update(editingId.value, updatePayload)
      } else {
        await assetSwApi.create(payload)
      }
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
.asset-list { max-width: 1400px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-lg); }
.page-title { font-size: var(--font-size-xl); font-weight: 700; color: var(--color-text); }
.tab-bar { display: flex; gap: 0; margin-bottom: var(--spacing-lg); border-bottom: 2px solid var(--color-border); }
.tab-btn { padding: 10px 24px; border: none; background: none; cursor: pointer; font-size: var(--font-size-sm); font-weight: 500; color: var(--color-text-secondary); border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.2s; }
.tab-btn.active { color: var(--color-primary); border-bottom-color: var(--color-primary); font-weight: 600; }
.tab-btn:hover { color: var(--color-primary); }
.filter-bar { display: flex; align-items: flex-end; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); flex-wrap: wrap; }
.filter-group { display: flex; flex-direction: column; gap: var(--spacing-xs); }
.filter-label { font-size: var(--font-size-sm); color: var(--color-text-secondary); font-weight: 500; }
.filter-select, .filter-input { padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-size: var(--font-size-sm); }
.search-group { flex-direction: row; align-items: flex-end; gap: var(--spacing-sm); }
.table-container { overflow-x: auto; margin-bottom: var(--spacing-lg); }
.data-table { width: 100%; border-collapse: collapse; background: var(--color-bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.data-table th, .data-table td { padding: 10px 14px; text-align: left; border-bottom: 1px solid var(--color-border); font-size: var(--font-size-sm); white-space: nowrap; }
.data-table th { background: var(--color-table-header); font-weight: 600; color: var(--color-text-secondary); }
.data-table tr:hover { background: var(--color-table-row-hover); }
.text-center { text-align: center; }
.link { color: var(--color-primary); text-decoration: none; font-weight: 500; }
.link:hover { text-decoration: underline; }
.status-badge { padding: 2px 10px; border-radius: 12px; font-size: var(--font-size-xs); font-weight: 600; }
.status-active { background: var(--color-badge-green-bg); color: var(--color-badge-green); }
.status-inactive { background: var(--color-badge-yellow-bg); color: var(--color-badge-yellow); }
.status-disposed { background: var(--color-badge-pink-bg); color: var(--color-badge-pink); }
.btn { padding: 6px 16px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-size-sm); background: var(--color-bg-white); }
.btn-primary { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }
.btn-primary:hover { background: var(--color-primary-dark); }
.btn-default:hover { background: var(--color-btn-default-hover); }
.btn-sm { padding: 3px 10px; font-size: var(--font-size-xs); }
.pagination { display: flex; justify-content: center; gap: var(--spacing-xs); }
.page-btn { padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); background: var(--color-bg-white); cursor: pointer; font-size: var(--font-size-sm); }
.page-btn.active { background: var(--color-primary); color: var(--color-text-inverse); border-color: var(--color-primary); }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: var(--color-overlay); display: flex; align-items: center; justify-content: center; z-index: 1000; }
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
