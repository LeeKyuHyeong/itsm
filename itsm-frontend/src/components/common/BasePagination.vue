<template>
  <div class="pagination">
    <div class="pagination-info">
      총 {{ totalElements }}건
    </div>
    <div class="pagination-controls">
      <button
        class="pagination-btn"
        :disabled="currentPage <= 1"
        @click="$emit('page-change', currentPage - 1)"
      >
        &laquo; 이전
      </button>
      <button
        v-for="page in visiblePages"
        :key="page"
        class="pagination-btn"
        :class="{ active: page === currentPage }"
        @click="$emit('page-change', page)"
      >
        {{ page }}
      </button>
      <button
        class="pagination-btn"
        :disabled="currentPage >= totalPages"
        @click="$emit('page-change', currentPage + 1)"
      >
        다음 &raquo;
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  },
  totalElements: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 10
  }
})

defineEmits(['page-change'])

const visiblePages = computed(() => {
  const total = props.totalPages
  const current = props.currentPage
  const maxVisible = 5

  if (total <= maxVisible) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  let start = Math.max(1, current - Math.floor(maxVisible / 2))
  let end = start + maxVisible - 1

  if (end > total) {
    end = total
    start = Math.max(1, end - maxVisible + 1)
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) 0;
}

.pagination-info {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.pagination-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 var(--spacing-sm);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-white);
  font-size: var(--font-size-sm);
  color: var(--color-text);
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled):not(.active) {
  background-color: var(--color-bg);
  border-color: var(--color-primary-light);
}

.pagination-btn.active {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
  font-weight: 600;
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
