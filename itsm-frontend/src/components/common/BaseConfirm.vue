<template>
  <BaseModal :show="show" :title="title" width="400px" @close="$emit('cancel')">
    <p class="confirm-message">{{ message }}</p>
    <template #footer>
      <button class="btn btn-default" @click="$emit('cancel')">
        {{ cancelText }}
      </button>
      <button
        class="btn"
        :class="confirmButtonClass"
        @click="$emit('confirm')"
      >
        {{ confirmText }}
      </button>
    </template>
  </BaseModal>
</template>

<script setup>
import { computed } from 'vue'
import BaseModal from './BaseModal.vue'

const props = defineProps({
  show: {
    type: Boolean,
    required: true
  },
  title: {
    type: String,
    default: '확인'
  },
  message: {
    type: String,
    default: ''
  },
  confirmText: {
    type: String,
    default: '확인'
  },
  cancelText: {
    type: String,
    default: '취소'
  },
  type: {
    type: String,
    default: 'info',
    validator: (v) => ['danger', 'warning', 'info'].includes(v)
  }
})

defineEmits(['confirm', 'cancel'])

const confirmButtonClass = computed(() => {
  switch (props.type) {
    case 'danger': return 'btn-danger'
    case 'warning': return 'btn-warning'
    default: return 'btn-primary'
  }
})
</script>

<style scoped>
.confirm-message {
  font-size: var(--font-size-sm);
  color: var(--color-text);
  line-height: 1.6;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-default {
  background-color: white;
  color: var(--color-text);
  border-color: var(--color-border);
}

.btn-default:hover {
  background-color: var(--color-bg);
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
}

.btn-primary:hover {
  background-color: var(--color-primary-dark);
}

.btn-danger {
  background-color: var(--color-danger);
  color: white;
}

.btn-danger:hover {
  opacity: 0.9;
}

.btn-warning {
  background-color: var(--color-warning);
  color: var(--color-text);
}

.btn-warning:hover {
  opacity: 0.9;
}
</style>
