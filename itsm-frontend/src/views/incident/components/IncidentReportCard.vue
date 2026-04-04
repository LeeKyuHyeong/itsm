<template>
  <div class="detail-card">
    <div class="card-header">
      <h3>{{ t('incident.report') }}</h3>
      <button v-if="!report" class="btn btn-sm" @click="showModal = true">{{ t('incident.writeReport') }}</button>
      <button v-else class="btn btn-sm" @click="showModal = true">{{ t('common.edit') }}</button>
    </div>
    <div v-if="!report" class="empty-state">{{ t('incident.noReport') }}</div>
    <div v-else class="report-content">
      <pre>{{ report.reportContent }}</pre>
      <div class="report-meta">
        {{ t('incident.createdAt') }}: {{ formatDate(report.createdAt) }}
        <span v-if="report.updatedAt"> | {{ t('incident.updatedAt') }}: {{ formatDate(report.updatedAt) }}</span>
      </div>
    </div>

    <!-- 장애보고서 모달 -->
    <BaseModal :show="showModal" :title="t('incident.writeReport')" width="640px" @close="showModal = false">
      <div class="form-group">
        <label>{{ t('incident.reportContent') }}</label>
        <textarea :value="modelValue" @input="$emit('update:modelValue', $event.target.value)" rows="10" :placeholder="t('incident.reportPlaceholder')"></textarea>
      </div>
      <template #footer>
        <button class="btn btn-secondary" @click="showModal = false">{{ t('common.cancel') }}</button>
        <button class="btn btn-primary" @click="handleSave">{{ t('common.save') }}</button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import BaseModal from '@/components/common/BaseModal.vue'

const { t } = useI18n()

defineProps({
  report: {
    type: Object,
    default: null
  },
  modelValue: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'save-report'])

const showModal = ref(false)

const handleSave = () => {
  emit('save-report')
  showModal.value = false
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
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
.detail-card h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: var(--font-size-lg);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}
.card-header h3 {
  margin: 0;
}
.empty-state {
  text-align: center;
  padding: var(--spacing-md);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.report-content pre {
  background: var(--color-bg-secondary);
  padding: var(--spacing-md);
  border-radius: 4px;
  white-space: pre-wrap;
  font-size: var(--font-size-sm);
}
.report-meta {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  margin-top: var(--spacing-xs);
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
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  box-sizing: border-box;
}
.btn {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: var(--color-text-inverse); }
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
.btn-sm { padding: 4px 12px; font-size: var(--font-size-xs); }
</style>
