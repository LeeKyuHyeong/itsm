<template>
  <div class="inspection-form">
    <div class="page-header">
      <h2>{{ isEdit ? t('inspection.edit') : t('inspection.create') }}</h2>
    </div>

    <form @submit.prevent="handleSubmit" class="form-card">
      <div class="form-group">
        <label class="required">{{ t('incident.incidentTitle') }}</label>
        <input v-model="form.title" type="text" required />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="required">{{ t('inspection.type') }}</label>
          <select v-model="form.inspectionTypeCd" required>
            <option value="">{{ t('common.select') }}</option>
            <option v-for="tp in inspectionTypes" :key="tp.code" :value="tp.code">{{ tp.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="required">{{ t('inspection.scheduledDate') }}</label>
          <BaseDatePicker v-model="form.scheduledAt" required />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group" v-if="!isEdit">
          <label class="required">{{ t('incident.company') }}</label>
          <select v-model="form.companyId" required>
            <option value="">{{ t('common.select') }}</option>
            <option v-for="c in companies" :key="c.companyId" :value="c.companyId">{{ c.companyNm }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>{{ t('incident.assignee') }}</label>
          <select v-model="form.managerId">
            <option value="">{{ t('common.select') }}</option>
            <option v-for="u in users" :key="u.userId" :value="u.userId">{{ u.userNm }}</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label>{{ t('incident.description') }}</label>
        <textarea v-model="form.description" rows="4"></textarea>
      </div>

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="$router.back()">{{ t('common.cancel') }}</button>
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? t('common.processing') : (isEdit ? t('common.edit') : t('common.create')) }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { inspectionApi } from '@/api/inspection.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import api from '@/api/index.js'
import BaseDatePicker from '@/components/common/BaseDatePicker.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const commonCodeStore = useCommonCodeStore()

const isEdit = computed(() => !!route.params.id)
const submitting = ref(false)
const inspectionTypes = ref([])
const companies = ref([])
const users = ref([])

const form = reactive({
  title: '',
  inspectionTypeCd: '',
  scheduledAt: '',
  companyId: '',
  managerId: '',
  description: ''
})

const loadDetail = async () => {
  if (!isEdit.value) return
  try {
    const res = await inspectionApi.getDetail(route.params.id)
    const data = res.data.data || res.data
    form.title = data.title
    form.inspectionTypeCd = data.inspectionTypeCd
    form.scheduledAt = data.scheduledAt || ''
    form.managerId = data.managerId || ''
    form.description = data.description || ''
    form.companyId = data.companyId
  } catch (e) {
    console.error(t('message.loadFail'), e)
    alert(t('message.loadFail'))
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (isEdit.value) {
      const payload = {
        title: form.title,
        inspectionTypeCd: form.inspectionTypeCd,
        scheduledAt: form.scheduledAt,
        managerId: form.managerId ? Number(form.managerId) : null,
        description: form.description || null
      }
      await inspectionApi.update(route.params.id, payload)
      router.push(`/inspections/${route.params.id}`)
    } else {
      const payload = {
        title: form.title,
        inspectionTypeCd: form.inspectionTypeCd,
        scheduledAt: form.scheduledAt,
        companyId: form.companyId ? Number(form.companyId) : null,
        managerId: form.managerId ? Number(form.managerId) : null,
        description: form.description || null
      }
      const res = await inspectionApi.create(payload)
      const data = res.data.data || res.data
      router.push(`/inspections/${data.inspectionId}`)
    }
  } catch (e) {
    console.error(t('message.saveFail'), e)
    alert(t('message.saveFail'))
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const codes = await commonCodeStore.fetchCodes('INSPECTION_TYPE')
    inspectionTypes.value = codes || []
  } catch (e) {
    console.error(t('message.loadFail'), e)
  }
  if (!isEdit.value) {
    try {
      const res = await api.get('/companies', { params: { size: 100 } })
      const data = res.data.data || res.data
      companies.value = data.content || data || []
    } catch (e) {
      console.error(t('message.loadFail'), e)
    }
  }
  try {
    const res = await api.get('/users', { params: { size: 100 } })
    const data = res.data.data || res.data
    users.value = data.content || data || []
  } catch (e) {
    console.error(t('message.loadFail'), e)
  }
  loadDetail()
})
</script>

<style scoped>
.inspection-form { padding: var(--spacing-lg); max-width: 800px; }
.page-header { margin-bottom: var(--spacing-lg); }
.page-header h2 { margin: 0; font-size: var(--font-size-xl); }
.form-card {
  background: var(--color-bg-white); border: 1px solid var(--color-border);
  border-radius: 8px; padding: var(--spacing-lg);
}
.form-group { margin-bottom: var(--spacing-md); }
.form-group label {
  display: block; margin-bottom: var(--spacing-xs);
  font-weight: 600; font-size: var(--font-size-sm);
}
.form-group label.required::after { content: ' *'; color: var(--color-btn-danger); }
.form-group input, .form-group select, .form-group textarea {
  width: 100%; padding: 8px 12px; border: 1px solid var(--color-border);
  border-radius: 4px; font-size: var(--font-size-sm); box-sizing: border-box;
}
.form-group textarea { resize: vertical; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-md); }
.form-actions {
  display: flex; justify-content: flex-end; gap: var(--spacing-sm);
  margin-top: var(--spacing-lg); padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border);
}
.btn {
  padding: 8px 20px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: var(--color-text-inverse); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
</style>
