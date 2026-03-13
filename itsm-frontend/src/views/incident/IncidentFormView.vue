<template>
  <div class="incident-form">
    <div class="page-header">
      <h2>{{ isEdit ? t('incident.edit') : t('incident.create') }}</h2>
    </div>

    <form @submit.prevent="handleSubmit" class="form-card">
      <div class="form-group">
        <label class="required">{{ t('incident.incidentTitle') }}</label>
        <input v-model="form.title" type="text" required :placeholder="t('incident.titlePlaceholder')" />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="required">{{ t('incident.type') }}</label>
          <select v-model="form.incidentTypeCd" required>
            <option value="">{{ t('incident.select') }}</option>
            <option v-for="tp in incidentTypes" :key="tp.code" :value="tp.code">{{ tp.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="required">{{ t('incident.priority') }}</label>
          <select v-model="form.priorityCd" required>
            <option value="">{{ t('incident.select') }}</option>
            <option value="CRITICAL">{{ t('priority.CRITICAL') }}</option>
            <option value="HIGH">{{ t('priority.HIGH') }}</option>
            <option value="MEDIUM">{{ t('priority.MEDIUM') }}</option>
            <option value="LOW">{{ t('priority.LOW') }}</option>
          </select>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="required">{{ t('incident.occurredAt') }}</label>
          <BaseDatePicker v-model="form.occurredAt" :enable-time="true" required />
        </div>
        <div class="form-group" v-if="!isEdit">
          <label class="required">{{ t('incident.company') }}</label>
          <select v-model="form.companyId" required>
            <option value="">{{ t('incident.select') }}</option>
            <option v-for="c in companies" :key="c.companyId" :value="c.companyId">{{ c.companyNm }}</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label class="required">{{ t('incident.content') }}</label>
        <textarea v-model="form.content" rows="6" required :placeholder="t('incident.contentPlaceholder')"></textarea>
      </div>

      <div class="form-group" v-if="isEdit">
        <label>{{ t('incident.processContent') }}</label>
        <textarea v-model="form.processContent" rows="4" :placeholder="t('incident.processContentPlaceholder')"></textarea>
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
import { incidentApi } from '@/api/incident.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import api from '@/api/index.js'
import BaseDatePicker from '@/components/common/BaseDatePicker.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const commonCodeStore = useCommonCodeStore()

const isEdit = computed(() => !!route.params.id)
const submitting = ref(false)
const incidentTypes = ref([])
const companies = ref([])

const form = reactive({
  title: '',
  content: '',
  incidentTypeCd: '',
  priorityCd: '',
  occurredAt: '',
  companyId: '',
  processContent: ''
})

const loadDetail = async () => {
  if (!isEdit.value) return
  try {
    const res = await incidentApi.getDetail(route.params.id)
    const data = res.data.data || res.data
    form.title = data.title
    form.content = data.content
    form.incidentTypeCd = data.incidentTypeCd
    form.priorityCd = data.priorityCd
    form.occurredAt = data.occurredAt ? data.occurredAt.slice(0, 16) : ''
    form.companyId = data.companyId
    form.processContent = data.processContent || ''
  } catch (e) {
    console.error('장애 조회 실패:', e)
    alert(t('message.loadFail'))
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const payload = {
      title: form.title,
      content: form.content,
      incidentTypeCd: form.incidentTypeCd,
      priorityCd: form.priorityCd,
      occurredAt: form.occurredAt ? form.occurredAt + ':00' : null
    }
    if (isEdit.value) {
      payload.processContent = form.processContent || null
      await incidentApi.update(route.params.id, payload)
      router.push(`/incidents/${route.params.id}`)
    } else {
      payload.companyId = form.companyId ? Number(form.companyId) : null
      const res = await incidentApi.create(payload)
      const data = res.data.data || res.data
      router.push(`/incidents/${data.incidentId}`)
    }
  } catch (e) {
    console.error('장애 저장 실패:', e)
    alert(t('message.saveFail'))
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const codes = await commonCodeStore.fetchCodes('INCIDENT_TYPE')
    incidentTypes.value = codes || []
  } catch (e) {
    console.error('공통코드 조회 실패:', e)
  }
  if (!isEdit.value) {
    try {
      const res = await api.get('/companies', { params: { size: 100 } })
      const data = res.data.data || res.data
      companies.value = data.content || data || []
    } catch (e) {
      console.error('고객사 조회 실패:', e)
    }
  }
  loadDetail()
})
</script>

<style scoped>
.incident-form {
  padding: var(--spacing-lg);
  max-width: 800px;
}
.page-header {
  margin-bottom: var(--spacing-lg);
}
.page-header h2 {
  margin: 0;
  font-size: var(--font-size-xl);
}
.form-card {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: var(--spacing-lg);
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
.form-group label.required::after {
  content: ' *';
  color: var(--color-priority-critical);
}
.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  box-sizing: border-box;
}
.form-group textarea {
  resize: vertical;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border);
}
.btn {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: var(--font-size-sm);
}
.btn-primary {
  background: var(--color-primary);
  color: var(--color-text-inverse);
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-secondary {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
}
</style>
