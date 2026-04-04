<template>
  <Teleport to="body">
    <div class="toast-container" aria-live="polite">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="toast-item"
          :class="`toast-${toast.type}`"
          role="alert"
        >
          <span class="toast-icon">{{ icon(toast.type) }}</span>
          <span class="toast-message">{{ toast.message }}</span>
          <button class="toast-close" @click="remove(toast.id)" aria-label="Close">&times;</button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { useToast, toastState } from '@/composables/useToast.js'

const { remove } = useToast()
const toasts = toastState.toasts

function icon(type) {
  switch (type) {
    case 'success': return '\u2713'
    case 'error': return '\u2717'
    case 'warning': return '\u26A0'
    default: return '\u2139'
  }
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 400px;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: var(--radius-sm, 6px);
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0,0,0,0.15));
  font-size: var(--font-size-sm, 14px);
  color: #fff;
  min-width: 280px;
}

.toast-success { background-color: var(--color-success, #22c55e); }
.toast-error { background-color: var(--color-danger, #ef4444); }
.toast-warning { background-color: var(--color-warning, #f59e0b); color: var(--color-text, #333); }
.toast-info { background-color: var(--color-primary, #3b82f6); }

.toast-icon { font-size: 16px; flex-shrink: 0; }
.toast-message { flex: 1; }

.toast-close {
  background: none;
  border: none;
  color: inherit;
  font-size: 18px;
  cursor: pointer;
  padding: 0;
  opacity: 0.7;
  line-height: 1;
}
.toast-close:hover { opacity: 1; }

.toast-enter-active { transition: all 0.3s ease; }
.toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from { transform: translateX(100%); opacity: 0; }
.toast-leave-to { transform: translateX(100%); opacity: 0; }
</style>
