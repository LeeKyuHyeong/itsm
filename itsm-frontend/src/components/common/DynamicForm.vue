<template>
  <div class="dynamic-form">
    <div v-for="field in schema" :key="field.key" class="form-group">
      <label class="form-label">
        {{ field.label }}
        <span v-if="field.required" class="form-required">*</span>
      </label>

      <!-- text -->
      <input
        v-if="field.type === 'text' || !field.type"
        type="text"
        class="form-input"
        :value="modelValue[field.key]"
        :placeholder="field.placeholder"
        :required="field.required"
        :readonly="readonly"
        @input="updateField(field.key, $event.target.value)"
      />

      <!-- number -->
      <input
        v-else-if="field.type === 'number'"
        type="number"
        class="form-input"
        :value="modelValue[field.key]"
        :placeholder="field.placeholder"
        :required="field.required"
        :readonly="readonly"
        @input="updateField(field.key, Number($event.target.value))"
      />

      <!-- date -->
      <BaseDatePicker
        v-else-if="field.type === 'date'"
        :modelValue="modelValue[field.key]"
        :required="field.required"
        :disabled="readonly"
        @update:modelValue="updateField(field.key, $event)"
      />

      <!-- textarea -->
      <textarea
        v-else-if="field.type === 'textarea'"
        class="form-input form-textarea"
        :value="modelValue[field.key]"
        :placeholder="field.placeholder"
        :required="field.required"
        :readonly="readonly"
        rows="3"
        @input="updateField(field.key, $event.target.value)"
      />

      <!-- select -->
      <select
        v-else-if="field.type === 'select'"
        class="form-input"
        :value="modelValue[field.key]"
        :required="field.required"
        :disabled="readonly"
        @change="updateField(field.key, $event.target.value)"
      >
        <option value="">{{ field.placeholder || t('common.selectPlaceholder') }}</option>
        <option
          v-for="opt in field.options"
          :key="opt.value"
          :value="opt.value"
        >
          {{ opt.label }}
        </option>
      </select>

      <!-- checkbox -->
      <label v-else-if="field.type === 'checkbox'" class="form-checkbox">
        <input
          type="checkbox"
          :checked="modelValue[field.key]"
          :disabled="readonly"
          @change="updateField(field.key, $event.target.checked)"
        />
        <span class="form-checkbox-text">{{ field.placeholder || '' }}</span>
      </label>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import BaseDatePicker from '@/components/common/BaseDatePicker.vue'

const { t } = useI18n()

const props = defineProps({
  schema: {
    type: Array,
    required: true
  },
  modelValue: {
    type: Object,
    required: true
  },
  readonly: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

function updateField(key, value) {
  if (props.readonly) return
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}
</script>

<style scoped>
.dynamic-form {
  width: 100%;
}

.form-group {
  margin-bottom: var(--spacing-md);
}

.form-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.form-required {
  color: var(--color-danger);
  margin-left: 2px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  color: var(--color-text);
  background: var(--color-bg-white);
  outline: none;
  transition: border-color 0.2s;
}

.form-input:focus {
  border-color: var(--color-primary);
}

.form-input:read-only,
.form-input:disabled {
  background-color: var(--color-bg);
  color: var(--color-text-secondary);
  cursor: not-allowed;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-checkbox {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  font-size: var(--font-size-sm);
  color: var(--color-text);
}

.form-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.form-checkbox-text {
  user-select: none;
}
</style>
