<template>
  <div class="detail-card">
    <div class="card-header">
      <h3>{{ t('incident.report') }}</h3>
      <button class="btn btn-sm" :disabled="!hasForm" @click="showModal = true">
        {{ report ? t('common.edit') : t('incident.writeReport') }}
      </button>
    </div>

    <!-- 2026-09-16 P3: 양식(tb_report_form, INCIDENT 유형·활성)이 없으면 작성할 수 없다 -->
    <div v-if="!hasForm" class="empty-state">{{ t('incident.reportFormMissing') }}</div>
    <div v-else-if="!report" class="empty-state">{{ t('incident.noReport') }}</div>
    <div v-else class="report-content">
      <dl class="report-fields">
        <div v-for="field in formSchema" :key="field.key" class="report-field">
          <dt>{{ field.label }}</dt>
          <dd>{{ displayValue(savedValues[field.key]) }}</dd>
        </div>
      </dl>
      <div class="report-meta">
        {{ t('incident.createdAt') }}: {{ formatDate(report.createdAt) }}
        <span v-if="report.updatedAt"> | {{ t('incident.updatedAt') }}: {{ formatDate(report.updatedAt) }}</span>
      </div>
    </div>

    <!-- 장애보고서 모달: 양식 스키마(JSON) 기반 동적 폼 -->
    <BaseModal :show="showModal" :title="t('incident.writeReport')" width="640px" @close="showModal = false">
      <DynamicForm :schema="formSchema" :modelValue="modelValue" @update:modelValue="$emit('update:modelValue', $event)" />
      <template #footer>
        <button class="btn btn-secondary" @click="showModal = false">{{ t('common.cancel') }}</button>
        <button class="btn btn-primary" @click="handleSave">{{ t('common.save') }}</button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatDate } from '@/utils/date.js'
import BaseModal from '@/components/common/BaseModal.vue'
import DynamicForm from '@/components/common/DynamicForm.vue'

const { t } = useI18n()

const props = defineProps({
  report: {
    type: Object,
    default: null
  },
  /** tb_report_form.form_schema 를 파싱한 필드 배열 ([{ key, label, type, required, ... }]) */
  formSchema: {
    type: Array,
    default: () => []
  },
  /** 작성 중인 값 (필드 key → 값) */
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'save-report'])

const showModal = ref(false)

const hasForm = computed(() => Array.isArray(props.formSchema) && props.formSchema.length > 0)

/** 저장된 report_content(JSON 문자열)를 객체로. 과거 자유 텍스트 데이터는 빈 객체로 취급 */
const savedValues = computed(() => {
  const raw = props.report?.reportContent
  if (!raw) return {}
  if (typeof raw === 'object') return raw
  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch {
    return {}
  }
})

function displayValue(v) {
  if (v === null || v === undefined || v === '') return '-'
  if (typeof v === 'boolean') return v ? 'Y' : 'N'
  return String(v)
}

const handleSave = () => {
  emit('save-report')
  showModal.value = false
}
</script>

<style scoped>
.detail-card {
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.card-header h3 {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--color-text);
}

.empty-state {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  padding: var(--spacing-sm) 0;
}

.report-fields {
  margin: 0;
}

.report-field {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: var(--spacing-sm);
  padding: 6px 0;
  border-bottom: 1px solid var(--color-border);
}

.report-field dt {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text-secondary);
}

.report-field dd {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text);
  white-space: pre-wrap;
  word-break: break-word;
}

.report-meta {
  margin-top: var(--spacing-sm);
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
