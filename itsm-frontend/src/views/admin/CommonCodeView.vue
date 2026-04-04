<template>
  <div class="code-manage">
    <div class="page-header">
      <h1 class="page-title">{{ t('admin.commonCode') }}</h1>
    </div>

    <div class="org-layout">
      <!-- Code Group Section -->
      <div class="org-panel">
        <div class="panel-header">
          <h2 class="panel-title">{{ t('admin.codeGroupList') }}</h2>
          <button class="btn btn-primary btn-sm" @click="openGroupDialog()">+ {{ t('common.add') }}</button>
        </div>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>{{ t('admin.groupCode') }}</th>
                <th>{{ t('admin.groupName') }}</th>
                <th>{{ t('admin.groupNameEn') }}</th>
                <th>{{ t('admin.useYn') }}</th>
                <th>{{ t('admin.manage') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loadingGroups">
                <td colspan="5" class="text-center">{{ t('common.loading') }}</td>
              </tr>
              <tr v-else-if="groups.length === 0">
                <td colspan="5" class="text-center">{{ t('admin.noCodeGroups') }}</td>
              </tr>
              <tr
                v-for="group in groups"
                :key="group.id"
                class="clickable-row"
                :class="{ selected: selectedGroupId === group.id }"
                @click="selectGroup(group)"
              >
                <td>{{ group.groupCd }}</td>
                <td>{{ group.groupNm }}</td>
                <td>{{ group.groupNmEn }}</td>
                <td>
                  <span class="status-badge" :class="group.active !== false ? 'status-active' : 'status-inactive'">
                    {{ group.active !== false ? t('admin.inUse') : t('admin.notInUse') }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-default" @click.stop="openGroupDialog(group)">{{ t('common.edit') }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Code Detail Section -->
      <div class="org-panel">
        <div class="panel-header">
          <h2 class="panel-title">
            {{ t('admin.codeDetailList') }}
            <span v-if="selectedGroupName" class="panel-subtitle">- {{ selectedGroupName }}</span>
          </h2>
          <button
            class="btn btn-primary btn-sm"
            :disabled="!selectedGroupId"
            @click="openDetailDialog()"
          >
            + {{ t('common.add') }}
          </button>
        </div>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>{{ t('admin.codeValue') }}</th>
                <th>{{ t('admin.codeName') }}</th>
                <th>{{ t('admin.codeNameEn') }}</th>
                <th>{{ t('admin.sortOrder') }}</th>
                <th>{{ t('admin.useYn') }}</th>
                <th>{{ t('admin.manage') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!selectedGroupId">
                <td colspan="6" class="text-center text-secondary">{{ t('admin.selectCodeGroup') }}</td>
              </tr>
              <tr v-else-if="loadingDetails">
                <td colspan="6" class="text-center">{{ t('common.loading') }}</td>
              </tr>
              <tr v-else-if="details.length === 0">
                <td colspan="6" class="text-center">{{ t('admin.noCodes') }}</td>
              </tr>
              <tr v-for="detail in details" :key="detail.id">
                <td>{{ detail.codeVal }}</td>
                <td>{{ detail.codeNm }}</td>
                <td>{{ detail.codeNmEn }}</td>
                <td>{{ detail.sortOrder }}</td>
                <td>
                  <span class="status-badge" :class="detail.active !== false ? 'status-active' : 'status-inactive'">
                    {{ detail.active !== false ? t('admin.inUse') : t('admin.notInUse') }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm btn-default" @click="openDetailDialog(detail)">{{ t('common.edit') }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Group Modal -->
    <CodeGroupFormModal
      :show="showGroupModal"
      :is-editing="!!editingGroup"
      :form="groupForm"
      :saving="groupSaving"
      :error="groupSaveError"
      @close="closeGroupModal"
      @save="saveGroup"
    />

    <!-- Detail Modal -->
    <CodeDetailFormModal
      :show="showDetailModal"
      :is-editing="!!editingDetail"
      :form="detailForm"
      :saving="detailSaving"
      :error="detailSaveError"
      @close="closeDetailModal"
      @save="saveDetail"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { commonCodeApi } from '@/api/admin/commonCode.js'
import CodeGroupFormModal from './components/CodeGroupFormModal.vue'
import CodeDetailFormModal from './components/CodeDetailFormModal.vue'

const { t } = useI18n()

const groups = ref([])
const details = ref([])
const loadingGroups = ref(false)
const loadingDetails = ref(false)

const selectedGroupId = ref(null)
const selectedGroupName = computed(() => {
  const g = groups.value.find(g => g.id === selectedGroupId.value)
  return g?.groupNm || ''
})

// Group Modal
const showGroupModal = ref(false)
const editingGroup = ref(null)
const groupSaving = ref(false)
const groupSaveError = ref('')
const groupForm = reactive({
  groupCd: '',
  groupNm: '',
  groupNmEn: '',
  description: ''
})

// Detail Modal
const showDetailModal = ref(false)
const editingDetail = ref(null)
const detailSaving = ref(false)
const detailSaveError = ref('')
const detailForm = reactive({
  codeVal: '',
  codeNm: '',
  codeNmEn: '',
  sortOrder: 0
})

onMounted(() => {
  loadGroups()
})

async function loadGroups() {
  loadingGroups.value = true
  try {
    const { data } = await commonCodeApi.getGroups({ size: 100 })
    const result = data.data || data
    const list = result.content || result.items || result || []
    groups.value = list.map(g => ({
      ...g,
      id: g.groupId ?? g.id,
      active: g.isActive === 'Y' || g.isActive === true || (g.active !== false && g.isActive !== 'N')
    }))
  } catch (error) {
    console.error('코드그룹 목록 로드 실패:', error)
    groups.value = []
  } finally {
    loadingGroups.value = false
  }
}

async function selectGroup(group) {
  selectedGroupId.value = group.id
  await loadDetails()
}

async function loadDetails() {
  if (!selectedGroupId.value) return
  loadingDetails.value = true
  try {
    const { data } = await commonCodeApi.getDetails(selectedGroupId.value)
    const list = data.data || data || []
    details.value = (Array.isArray(list) ? list : []).map(d => ({
      ...d,
      id: d.detailId ?? d.id,
      active: d.isActive === 'Y' || d.isActive === true || (d.active !== false && d.isActive !== 'N')
    }))
  } catch (error) {
    console.error('코드상세 목록 로드 실패:', error)
    details.value = []
  } finally {
    loadingDetails.value = false
  }
}

// Group CRUD
function openGroupDialog(group = null) {
  editingGroup.value = group
  groupSaveError.value = ''
  if (group) {
    Object.assign(groupForm, {
      groupCd: group.groupCd || '',
      groupNm: group.groupNm || '',
      groupNmEn: group.groupNmEn || '',
      description: group.description || ''
    })
  } else {
    Object.assign(groupForm, {
      groupCd: '',
      groupNm: '',
      groupNmEn: '',
      description: ''
    })
  }
  showGroupModal.value = true
}

function closeGroupModal() {
  showGroupModal.value = false
  editingGroup.value = null
}

async function saveGroup() {
  groupSaving.value = true
  groupSaveError.value = ''
  try {
    if (editingGroup.value) {
      await commonCodeApi.updateGroup(editingGroup.value.id, { ...groupForm })
    } else {
      await commonCodeApi.createGroup({ ...groupForm })
    }
    closeGroupModal()
    loadGroups()
  } catch (error) {
    groupSaveError.value = error.response?.data?.message || t('message.saveFail')
  } finally {
    groupSaving.value = false
  }
}

// Detail CRUD
function openDetailDialog(detail = null) {
  editingDetail.value = detail
  detailSaveError.value = ''
  if (detail) {
    Object.assign(detailForm, {
      codeVal: detail.codeVal || '',
      codeNm: detail.codeNm || '',
      codeNmEn: detail.codeNmEn || '',
      sortOrder: detail.sortOrder || 0
    })
  } else {
    Object.assign(detailForm, {
      codeVal: '',
      codeNm: '',
      codeNmEn: '',
      sortOrder: 0
    })
  }
  showDetailModal.value = true
}

function closeDetailModal() {
  showDetailModal.value = false
  editingDetail.value = null
}

async function saveDetail() {
  detailSaving.value = true
  detailSaveError.value = ''
  try {
    if (editingDetail.value) {
      await commonCodeApi.updateDetail(selectedGroupId.value, editingDetail.value.id, { ...detailForm })
    } else {
      await commonCodeApi.createDetail(selectedGroupId.value, { ...detailForm })
    }
    closeDetailModal()
    loadDetails()
  } catch (error) {
    detailSaveError.value = error.response?.data?.message || t('message.saveFail')
  } finally {
    detailSaving.value = false
  }
}
</script>

<style scoped>
.code-manage {
  max-width: 1200px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
}

.page-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--color-text);
}

.org-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
}

.org-panel {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  background-color: var(--color-table-header);
  border-bottom: 1px solid var(--color-border);
}

.panel-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text);
}

.panel-subtitle {
  font-weight: 400;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.table-container {
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background-color: var(--color-table-header);
  padding: 8px 12px;
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  text-align: left;
  border-bottom: 2px solid var(--color-border);
  white-space: nowrap;
}

.data-table td {
  padding: 8px 12px;
  font-size: var(--font-size-sm);
  border-bottom: 1px solid var(--color-border-light);
  color: var(--color-text);
}

.data-table tbody tr:nth-child(even) {
  background-color: var(--color-table-row-even);
}

.data-table tbody tr:hover {
  background-color: var(--color-table-row-hover);
}

.clickable-row {
  cursor: pointer;
}

.clickable-row.selected {
  background-color: var(--color-primary-bg) !important;
}

.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: var(--font-size-xs);
  font-weight: 500;
}

.status-active {
  background-color: var(--color-badge-green-bg);
  color: var(--color-success);
}

.status-inactive {
  background-color: var(--color-badge-gray-bg);
  color: var(--color-text-secondary);
}

.text-center {
  text-align: center;
}

.text-secondary {
  color: var(--color-text-secondary);
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-sm {
  padding: 4px 8px;
  font-size: var(--font-size-xs);
}

.btn-primary {
  background-color: var(--color-primary);
  color: var(--color-text-inverse);
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-default {
  background-color: var(--color-bg-white);
  color: var(--color-text);
  border-color: var(--color-border);
}

.btn-default:hover {
  background-color: var(--color-bg);
}

</style>
