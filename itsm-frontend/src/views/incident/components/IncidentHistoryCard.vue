<template>
  <div class="detail-card">
    <h3>{{ t('incident.changeHistory') }}</h3>
    <div v-if="histories.length === 0" class="empty-state">{{ t('incident.noHistory') }}</div>
    <div v-else class="timeline">
      <div v-for="h in histories" :key="h.historyId" class="timeline-item">
        <div class="timeline-dot"></div>
        <div class="timeline-content">
          <div class="timeline-field">{{ h.changedField }}</div>
          <div class="timeline-change">
            <span class="before">{{ h.beforeValue || t('incident.none') }}</span>
            <span class="arrow">&rarr;</span>
            <span class="after">{{ h.afterValue || t('incident.none') }}</span>
          </div>
          <div class="timeline-date">{{ formatDate(h.createdAt) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { formatDate } from '@/utils/date.js'

const { t } = useI18n()

defineProps({
  histories: {
    type: Array,
    required: true
  }
})
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
.timeline {
  position: relative;
  padding-left: 20px;
}
.timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--color-border);
}
.timeline-item {
  position: relative;
  padding-bottom: var(--spacing-md);
}
.timeline-dot {
  position: absolute;
  left: -17px;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-primary);
}
.timeline-content {
  padding-left: var(--spacing-sm);
}
.timeline-field {
  font-weight: 600;
  font-size: var(--font-size-sm);
  margin-bottom: 2px;
}
.timeline-change {
  font-size: var(--font-size-sm);
  display: flex;
  gap: var(--spacing-xs);
  align-items: center;
}
.timeline-change .before {
  color: var(--color-priority-critical);
  text-decoration: line-through;
}
.timeline-change .after {
  color: var(--color-priority-low);
  font-weight: 600;
}
.timeline-change .arrow {
  color: var(--color-text-muted);
}
.timeline-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  margin-top: 2px;
}
</style>
