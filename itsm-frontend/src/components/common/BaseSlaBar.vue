<template>
  <div class="sla-bar">
    <div class="sla-track">
      <div
        class="sla-fill"
        :class="barColorClass"
        :style="{ width: clampedPercentage + '%' }"
      />
    </div>
    <span v-if="showLabel" class="sla-label" :class="barColorClass">
      {{ clampedPercentage }}%
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  percentage: {
    type: Number,
    required: true
  },
  showLabel: {
    type: Boolean,
    default: true
  }
})

const clampedPercentage = computed(() => {
  return Math.min(100, Math.max(0, Math.round(props.percentage)))
})

const barColorClass = computed(() => {
  const p = clampedPercentage.value
  if (p < 60) return 'sla-green'
  if (p < 80) return 'sla-yellow'
  if (p < 95) return 'sla-orange'
  return 'sla-red'
})
</script>

<style scoped>
.sla-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.sla-track {
  flex: 1;
  height: 8px;
  background-color: var(--color-badge-gray-bg);
  border-radius: 4px;
  overflow: hidden;
}

.sla-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.sla-label {
  font-size: var(--font-size-xs);
  font-weight: 600;
  min-width: 36px;
  text-align: right;
}

.sla-green {
  background-color: var(--color-success);
  color: var(--color-success);
}

.sla-yellow {
  background-color: var(--color-warning);
  color: var(--color-sla-warning);
}

.sla-orange {
  background-color: var(--color-sla-danger);
  color: var(--color-sla-danger);
}

.sla-red {
  background-color: var(--color-danger);
  color: var(--color-danger);
}
</style>
