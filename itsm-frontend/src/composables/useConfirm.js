import { reactive } from 'vue'

export const confirmState = reactive({
  show: false,
  title: '',
  message: '',
  type: 'info',
  confirmText: '',
  cancelText: '',
  resolve: null
})

export function useConfirm() {
  function confirm({ message, title = '', type = 'info', confirmText = '', cancelText = '' }) {
    confirmState.message = message
    confirmState.title = title
    confirmState.type = type
    confirmState.confirmText = confirmText
    confirmState.cancelText = cancelText
    confirmState.show = true

    return new Promise((resolve) => {
      confirmState.resolve = resolve
    })
  }

  function handleConfirm() {
    confirmState.show = false
    if (confirmState.resolve) {
      confirmState.resolve(true)
      confirmState.resolve = null
    }
  }

  function handleCancel() {
    confirmState.show = false
    if (confirmState.resolve) {
      confirmState.resolve(false)
      confirmState.resolve = null
    }
  }

  return { confirm, handleConfirm, handleCancel, state: confirmState }
}
