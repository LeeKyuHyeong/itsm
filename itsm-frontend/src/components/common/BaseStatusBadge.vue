<template>
  <span class="status-badge" :class="badgeClass">{{ status }}</span>
</template>

<script setup>
import { computed } from 'vue'

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
  '활성': 'green',
  '승인완료': 'green',
  COMPLETED: 'green',
  '완료': 'green',

  INACTIVE: 'gray',
  '비활성': 'gray',
  '보류': 'gray',
  CLOSED: 'gray',
  '종료': 'gray',

  LOCKED: 'red',
  '잠김': 'red',
  REJECTED: 'red',
  '반려': 'red',

  PENDING: 'blue',
  '대기': 'blue',
  '접수': 'blue',
  RECEIVED: 'blue',
  IN_PROGRESS: 'blue',
  '처리중': 'blue',
  '진행중': 'blue'
}

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
  background-color: #e6f4ea;
  color: var(--color-success);
}

.badge-gray {
  background-color: #f1f3f4;
  color: var(--color-text-secondary);
}

.badge-red {
  background-color: #fce8e6;
  color: var(--color-danger);
}

.badge-blue {
  background-color: #e8f0fe;
  color: var(--color-info);
}
</style>
