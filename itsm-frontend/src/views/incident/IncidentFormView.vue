<template>
  <div class="incident-form">
    <div class="page-header">
      <h2>{{ isEdit ? '장애 수정' : '장애 등록' }}</h2>
    </div>

    <form @submit.prevent="handleSubmit" class="form-card">
      <div class="form-group">
        <label class="required">제목</label>
        <input v-model="form.title" type="text" required placeholder="장애 제목을 입력하세요" />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="required">장애 유형</label>
          <select v-model="form.incidentTypeCd" required>
            <option value="">선택</option>
            <option v-for="t in incidentTypes" :key="t.code" :value="t.code">{{ t.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="required">우선순위</label>
          <select v-model="form.priorityCd" required>
            <option value="">선택</option>
            <option value="CRITICAL">긴급</option>
            <option value="HIGH">높음</option>
            <option value="MEDIUM">보통</option>
            <option value="LOW">낮음</option>
          </select>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="required">발생일시</label>
          <input v-model="form.occurredAt" type="datetime-local" required />
        </div>
        <div class="form-group" v-if="!isEdit">
          <label class="required">고객사</label>
          <select v-model="form.companyId" required>
            <option value="">선택</option>
            <option v-for="c in companies" :key="c.companyId" :value="c.companyId">{{ c.companyNm }}</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label class="required">장애 내용</label>
        <textarea v-model="form.content" rows="6" required placeholder="장애 상세 내용을 입력하세요"></textarea>
      </div>

      <div class="form-group" v-if="isEdit">
        <label>처리내용 (주담당자 작성)</label>
        <textarea v-model="form.processContent" rows="4" placeholder="처리 내용을 입력하세요"></textarea>
      </div>

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="$router.back()">취소</button>
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? '처리중...' : (isEdit ? '수정' : '등록') }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { incidentApi } from '@/api/incident.js'
import { useCommonCodeStore } from '@/stores/commonCode.js'
import api from '@/api/index.js'

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
    alert('장애 정보를 불러올 수 없습니다.')
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
    alert('저장에 실패했습니다.')
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
  background: #fff;
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
  color: #dc2626;
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
  color: #fff;
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
