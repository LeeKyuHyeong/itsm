<template>
  <span class="status-badge" :class="badgeClass">{{ statusLabel }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, te } = useI18n()

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: ''
  }
})

const STATUS_COLOR_MAP = {
  ACTIVE: 'green',
  COMPLETED: 'green',
  RESOLVED: 'green',
  APPROVED: 'green',

  INACTIVE: 'gray',
  CLOSED: 'gray',
  CANCELLED: 'gray',

  LOCKED: 'red',
  REJECTED: 'red',
  OVERDUE: 'red',
  ROLLBACK: 'red',

  PENDING: 'blue',
  PENDING_COMPLETE: 'blue',
  ON_HOLD: 'blue',
  RECEIVED: 'blue',
  ASSIGNED: 'blue',
  IN_PROGRESS: 'blue',
  REQUESTED: 'blue',
  APPROVAL_REQUESTED: 'blue',
  DRAFT: 'blue',
  REVIEW: 'blue',
  SCHEDULED: 'blue',
  IMPLEMENTING: 'blue',
  PLANNED: 'blue'
}

const statusLabel = computed(() => {
  const key = `status.${props.status}`
  return te(key) ? t(key) : props.status
})

const badgeClass = computed(() => {
  const color = props.type || STATUS_COLOR_MAP[props.status] || 'gray'
  return `badge-${color}`
})
</script>

<style scoped>
.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: var(--font-size-xs);
  font-weight: 500;
  white-space: nowrap;
}

.badge-green {
  background-color: var(--color-badge-green-bg);
  color: var(--color-success);
}

.badge-gray {
  background-color: var(--color-badge-gray-bg);
  color: var(--color-text-secondary);
}

.badge-red {
  background-color: var(--color-badge-red-bg);
  color: var(--color-danger);
}

.badge-blue {
  background-color: var(--color-badge-blue-bg);
  color: var(--color-info);
}
</style>
