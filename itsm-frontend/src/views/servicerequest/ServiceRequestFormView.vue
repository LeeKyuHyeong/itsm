<template>
  <div class="sr-form">
    <div class="page-header">
      <h2>{{ isEdit ? t('serviceRequest.edit') : t('serviceRequest.create') }}</h2>
    </div>

    <form @submit.prevent="handleSubmit" class="form-card">
      <div class="form-group">
        <label class="required">{{ t('incident.incidentTitle') }}</label>
        <input v-model="form.title" type="text" required :placeholder="t('serviceRequest.titlePlaceholder')" />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="required">{{ t('serviceRequest.type') }}</label>
          <select v-model="form.requestTypeCd" required>
            <option value="">{{ t('common.select') }}</option>
            <option v-for="tp in requestTypes" :key="tp.code" :value="tp.code">{{ tp.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="required">{{ t('incident.priority') }}</label>
          <select v-model="form.priorityCd" required>
            <option value="">{{ t('common.select') }}</option>
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
          <input v-model="form.occurredAt" type="datetime-local" required />
        </div>
        <div class="form-group" v-if="!isEdit">
          <label class="required">{{ t('incident.company') }}</label>
          <select v-model="form.companyId" required>
            <option value="">{{ t('common.select') }}</option>
            <option v-for="c in companies" :key="c.companyId" :value="c.companyId">{{ c.companyNm }}</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label class="required">{{ t('serviceRequest.content') }}</label>
        <textarea v-model="form.content" rows="6" required></textarea>
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
import { serviceRequestApi } from '@/api/servicerequest.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import api from '@/api/index.js'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const commonCodeStore = useCommonCodeStore()

const isEdit = computed(() => !!route.params.id)
const submitting = ref(false)
const requestTypes = ref([])
const companies = ref([])

const form = reactive({
  title: '',
  content: '',
  requestTypeCd: '',
  priorityCd: '',
  occurredAt: '',
  companyId: ''
})

const loadDetail = async () => {
  if (!isEdit.value) return
  try {
    const res = await serviceRequestApi.getDetail(route.params.id)
    const data = res.data.data || res.data
    form.title = data.title
    form.content = data.content
    form.requestTypeCd = data.requestTypeCd
    form.priorityCd = data.priorityCd
    form.occurredAt = data.occurredAt ? data.occurredAt.slice(0, 16) : ''
    form.companyId = data.companyId
  } catch (e) {
    console.error(t('message.loadFail'), e)
    alert(t('message.loadFail'))
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const payload = {
      title: form.title,
      content: form.content,
      requestTypeCd: form.requestTypeCd,
      priorityCd: form.priorityCd,
      occurredAt: form.occurredAt ? form.occurredAt + ':00' : null
    }
    if (isEdit.value) {
      await serviceRequestApi.update(route.params.id, payload)
      router.push(`/service-requests/${route.params.id}`)
    } else {
      payload.companyId = form.companyId ? Number(form.companyId) : null
      const res = await serviceRequestApi.create(payload)
      const data = res.data.data || res.data
      router.push(`/service-requests/${data.requestId}`)
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
    const codes = await commonCodeStore.fetchCodes('REQUEST_TYPE')
    requestTypes.value = codes || []
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
  loadDetail()
})
</script>

<style scoped>
.sr-form {
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
.btn-primary { background: var(--color-primary); color: var(--color-text-inverse); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
</style>
