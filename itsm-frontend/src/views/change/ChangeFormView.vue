<template>
  <div class="change-form">
    <div class="page-header">
      <h2>{{ isEdit ? t('change.edit') : t('change.create') }}</h2>
    </div>

    <form @submit.prevent="handleSubmit" class="form-card">
      <div class="form-group">
        <label class="required">{{ t('incident.incidentTitle') }}</label>
        <input v-model="form.title" type="text" required />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="required"><!-- 변경 유형 -->변경 유형</label>
          <select v-model="form.changeTypeCd" required>
            <option value="">{{ t('common.all', '선택') }}</option>
            <option v-for="tp in changeTypes" :key="tp.code" :value="tp.code">{{ tp.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="required">{{ t('incident.priority') }}</label>
          <select v-model="form.priorityCd" required>
            <option value="">{{ t('common.all', '선택') }}</option>
            <option value="CRITICAL">{{ t('priority.CRITICAL') }}</option>
            <option value="HIGH">{{ t('priority.HIGH') }}</option>
            <option value="MEDIUM">{{ t('priority.MEDIUM') }}</option>
            <option value="LOW">{{ t('priority.LOW') }}</option>
          </select>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label><!-- 변경 예정일시 -->변경 예정일시</label>
          <input v-model="form.scheduledAt" type="datetime-local" />
        </div>
        <div class="form-group" v-if="!isEdit">
          <label class="required">{{ t('incident.company') }}</label>
          <select v-model="form.companyId" required>
            <option value="">{{ t('common.all', '선택') }}</option>
            <option v-for="c in companies" :key="c.companyId" :value="c.companyId">{{ c.companyNm }}</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label class="required"><!-- 변경 내용 -->변경 내용</label>
        <textarea v-model="form.content" rows="6" required></textarea>
      </div>

      <div class="form-group">
        <label><!-- 롤백 계획 -->롤백 계획</label>
        <textarea v-model="form.rollbackPlan" rows="4"></textarea>
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
import { changeApi } from '@/api/change.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import api from '@/api/index.js'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const commonCodeStore = useCommonCodeStore()

const isEdit = computed(() => !!route.params.id)
const submitting = ref(false)
const changeTypes = ref([])
const companies = ref([])

const form = reactive({
  title: '',
  content: '',
  changeTypeCd: '',
  priorityCd: '',
  scheduledAt: '',
  rollbackPlan: '',
  companyId: ''
})

const loadDetail = async () => {
  if (!isEdit.value) return
  try {
    const res = await changeApi.getDetail(route.params.id)
    const data = res.data.data || res.data
    form.title = data.title
    form.content = data.content
    form.changeTypeCd = data.changeTypeCd
    form.priorityCd = data.priorityCd
    form.scheduledAt = data.scheduledAt ? data.scheduledAt.slice(0, 16) : ''
    form.rollbackPlan = data.rollbackPlan || ''
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
      changeTypeCd: form.changeTypeCd,
      priorityCd: form.priorityCd,
      scheduledAt: form.scheduledAt ? form.scheduledAt + ':00' : null,
      rollbackPlan: form.rollbackPlan || null
    }
    if (isEdit.value) {
      await changeApi.update(route.params.id, payload)
      router.push(`/changes/${route.params.id}`)
    } else {
      payload.companyId = form.companyId ? Number(form.companyId) : null
      const res = await changeApi.create(payload)
      const data = res.data.data || res.data
      router.push(`/changes/${data.changeId}`)
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
    const codes = await commonCodeStore.fetchCodes('CHANGE_TYPE')
    changeTypes.value = codes || []
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
.change-form { padding: var(--spacing-lg); max-width: 800px; }
.page-header { margin-bottom: var(--spacing-lg); }
.page-header h2 { margin: 0; font-size: var(--font-size-xl); }
.form-card {
  background: #fff; border: 1px solid var(--color-border);
  border-radius: 8px; padding: var(--spacing-lg);
}
.form-group { margin-bottom: var(--spacing-md); }
.form-group label {
  display: block; margin-bottom: var(--spacing-xs);
  font-weight: 600; font-size: var(--font-size-sm);
}
.form-group label.required::after { content: ' *'; color: #dc2626; }
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
.btn-primary { background: var(--color-primary); color: #fff; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
</style>
