import { reactive } from 'vue'

export const toastState = reactive({
  toasts: []
})

let nextId = 1

export function useToast() {
  function add(message, type = 'info', duration = 3000) {
    const id = nextId++
    toastState.toasts.push({ id, message, type })
    setTimeout(() => remove(id), duration)
    return id
  }

  function success(message) {
    return add(message, 'success')
  }

  function error(message) {
    return add(message, 'error', 5000)
  }

  function warning(message) {
    return add(message, 'warning', 4000)
  }

  function remove(id) {
    const idx = toastState.toasts.findIndex(t => t.id === id)
    if (idx !== -1) toastState.toasts.splice(idx, 1)
  }

  return { success, error, warning, remove, toasts: toastState.toasts }
}
