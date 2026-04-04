<template>
  <BaseConfirm
    :show="state.show"
    :title="state.title"
    :message="state.message"
    :type="state.type"
    :confirm-text="state.confirmText"
    :cancel-text="state.cancelText"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  />
</template>

<script setup>
import BaseConfirm from './BaseConfirm.vue'
import { useConfirm } from '@/composables/useConfirm.js'

const { state, handleConfirm, handleCancel } = useConfirm()
</script>
