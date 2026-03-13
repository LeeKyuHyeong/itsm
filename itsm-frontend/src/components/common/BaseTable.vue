<template>
  <div class="table-container">
    <table class="data-table">
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :style="columnStyle(col)"
          >
            {{ col.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td :colspan="columns.length" class="text-center">{{ t('common.loading') }}</td>
        </tr>
        <tr v-else-if="!data || data.length === 0">
          <td :colspan="columns.length" class="text-center text-secondary">
            {{ emptyMessage || t('common.noData') }}
          </td>
        </tr>
        <template v-else>
          <tr
            v-for="(row, index) in data"
            :key="index"
            class="clickable-row"
            @click="$emit('row-click', row, index)"
          >
            <td
              v-for="col in columns"
              :key="col.key"
              :style="cellStyle(col)"
            >
              <slot :name="`cell-${col.key}`" :row="row" :index="index" :value="row[col.key]">
                {{ row[col.key] ?? '-' }}
              </slot>
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps({
  columns: {
    type: Array,
    required: true
  },
  data: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  emptyMessage: {
    type: String,
    default: ''
  }
})

defineEmits(['row-click'])

function columnStyle(col) {
  const style = {}
  if (col.width) style.width = col.width
  if (col.align) style.textAlign = col.align
  return style
}

function cellStyle(col) {
  const style = {}
  if (col.align) style.textAlign = col.align
  return style
}
</script>

<style scoped>
.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background-color: var(--color-table-header);
  padding: 8px 12px;
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-secondary);
  text-align: left;
  border-bottom: 2px solid var(--color-border);
  white-space: nowrap;
}

.data-table td {
  padding: 8px 12px;
  font-size: var(--font-size-sm);
  border-bottom: 1px solid var(--color-border-light);
  color: var(--color-text);
}

.data-table tbody tr:nth-child(even) {
  background-color: var(--color-table-row-even);
}

.data-table tbody tr:hover {
  background-color: var(--color-table-row-hover);
}

.clickable-row {
  cursor: pointer;
}

.text-center {
  text-align: center;
}

.text-secondary {
  color: var(--color-text-secondary);
}
</style>
