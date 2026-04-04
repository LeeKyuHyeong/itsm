<template>
  <div class="detail-card">
    <h3>{{ t('incident.comment') }}</h3>
    <div v-if="comments.length === 0" class="empty-state">{{ t('incident.noComment') }}</div>
    <div v-else class="comment-list">
      <div v-for="c in comments" :key="c.commentId" class="comment-item">
        <div class="comment-header">
          <span class="comment-author">{{ c.createdByNm || `${t('incident.user')}#${c.createdBy}` }}</span>
          <span class="comment-date">{{ formatDate(c.createdAt) }}</span>
          <button class="btn-link danger" @click="$emit('delete-comment', c.commentId)">{{ t('common.delete') }}</button>
        </div>
        <div class="comment-content">{{ c.content }}</div>
      </div>
    </div>
    <div class="comment-form">
      <textarea :value="modelValue" @input="$emit('update:modelValue', $event.target.value)" rows="2" :placeholder="t('incident.commentPlaceholder')"></textarea>
      <button class="btn btn-primary btn-sm" @click="$emit('add-comment')" :disabled="!modelValue.trim()">{{ t('common.create') }}</button>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { formatDate } from '@/utils/date.js'

const { t } = useI18n()

defineProps({
  comments: {
    type: Array,
    required: true
  },
  modelValue: {
    type: String,
    required: true
  }
})

defineEmits(['update:modelValue', 'add-comment', 'delete-comment'])
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
.empty-state {
  text-align: center;
  padding: var(--spacing-md);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}
.comment-item {
  padding: var(--spacing-sm);
  background: var(--color-bg-secondary);
  border-radius: 4px;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 4px;
  font-size: var(--font-size-xs);
}
.comment-author {
  font-weight: 600;
}
.comment-date {
  color: var(--color-text-muted);
  flex: 1;
}
.comment-content {
  font-size: var(--font-size-sm);
  white-space: pre-wrap;
}
.comment-form {
  display: flex;
  gap: var(--spacing-sm);
  align-items: flex-end;
}
.comment-form textarea {
  flex: 1;
  padding: 8px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  resize: none;
}
.btn {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: var(--color-text-inverse); }
.btn-sm { padding: 4px 12px; font-size: var(--font-size-xs); }
.btn-link {
  background: none;
  border: none;
  color: var(--color-primary);
  cursor: pointer;
  font-size: var(--font-size-xs);
  padding: 0;
}
.btn-link.danger {
  color: var(--color-btn-danger);
}
</style>
