<template>
  <VueDatePicker
    v-model="model"
    :locale="currentLocale"
    :enable-time-picker="enableTime"
    :format="displayFormat"
    :placeholder="placeholder"
    :required="required"
    :disabled="disabled"
    :clearable="clearable"
    :auto-apply="true"
    :text-input="true"
    :teleport="true"
    input-class-name="dp-input"
    menu-class-name="dp-menu"
    :select-text="t('common.confirm')"
    :cancel-text="t('common.cancel')"
  />
</template>

<script setup>
import { computed } from 'vue'
import VueDatePicker from '@vuepic/vue-datepicker'
import '@vuepic/vue-datepicker/dist/main.css'
import { useI18n } from 'vue-i18n'

const { t, locale: currentLocale } = useI18n()

const props = defineProps({
  modelValue: {
    type: [String, Date],
    default: ''
  },
  enableTime: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: ''
  },
  required: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  clearable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue'])

const displayFormat = computed(() => {
  return props.enableTime ? 'yyyy-MM-dd HH:mm' : 'yyyy-MM-dd'
})

const model = computed({
  get() {
    return props.modelValue || null
  },
  set(val) {
    if (!val) {
      emit('update:modelValue', '')
      return
    }
    if (val instanceof Date) {
      if (props.enableTime) {
        const pad = (n) => String(n).padStart(2, '0')
        const formatted = `${val.getFullYear()}-${pad(val.getMonth() + 1)}-${pad(val.getDate())}T${pad(val.getHours())}:${pad(val.getMinutes())}`
        emit('update:modelValue', formatted)
      } else {
        const pad = (n) => String(n).padStart(2, '0')
        const formatted = `${val.getFullYear()}-${pad(val.getMonth() + 1)}-${pad(val.getDate())}`
        emit('update:modelValue', formatted)
      }
    } else {
      emit('update:modelValue', val)
    }
  }
})
</script>

<style>
.dp-input {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  background-color: var(--color-bg-white);
  color: var(--color-text);
}

.dp-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-focus);
}

.dp__theme_dark {
  --dp-background-color: var(--color-bg-white);
  --dp-text-color: var(--color-text);
  --dp-hover-color: var(--color-table-row-hover);
  --dp-hover-text-color: var(--color-text);
  --dp-primary-color: var(--color-primary);
  --dp-primary-text-color: var(--color-text-inverse);
  --dp-secondary-color: var(--color-text-disabled);
  --dp-border-color: var(--color-border);
  --dp-menu-border-color: var(--color-border);
  --dp-disabled-color: var(--color-text-disabled);
}

.dp__theme_light {
  --dp-primary-color: var(--color-primary);
  --dp-primary-text-color: var(--color-text-inverse);
}

[data-theme="dark"] .dp__input {
  background-color: var(--color-bg-white);
  color: var(--color-text);
  border-color: var(--color-border);
}

[data-theme="dark"] .dp__menu {
  background-color: var(--color-bg-elevated);
  border-color: var(--color-border);
}
</style>
