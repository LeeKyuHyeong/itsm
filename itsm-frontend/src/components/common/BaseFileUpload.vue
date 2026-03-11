<template>
  <div class="file-upload">
    <div
      class="drop-zone"
      :class="{ dragging: isDragging }"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="onDrop"
      @click="triggerInput"
    >
      <input
        ref="fileInput"
        type="file"
        class="file-input-hidden"
        :accept="accept"
        :multiple="multiple"
        @change="onFileSelect"
      />
      <div class="drop-zone-content">
        <span class="drop-zone-icon">+</span>
        <p class="drop-zone-text">파일을 드래그하거나 클릭하여 선택</p>
        <p class="drop-zone-hint">최대 {{ maxSize }}MB</p>
      </div>
    </div>

    <ul v-if="files.length > 0" class="file-list">
      <li v-for="(file, index) in files" :key="index" class="file-item">
        <span class="file-name">{{ file.name }}</span>
        <span class="file-size">{{ formatSize(file.size) }}</span>
        <button class="file-remove" @click="removeFile(index)">&times;</button>
      </li>
    </ul>

    <p v-if="error" class="file-error">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  accept: {
    type: String,
    default: ''
  },
  maxSize: {
    type: Number,
    default: 10
  },
  multiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['change'])

const fileInput = ref(null)
const files = ref([])
const isDragging = ref(false)
const error = ref('')

function triggerInput() {
  fileInput.value?.click()
}

function onFileSelect(event) {
  handleFiles(Array.from(event.target.files))
  event.target.value = ''
}

function onDrop(event) {
  isDragging.value = false
  handleFiles(Array.from(event.dataTransfer.files))
}

function handleFiles(newFiles) {
  error.value = ''
  const maxBytes = props.maxSize * 1024 * 1024

  for (const file of newFiles) {
    if (file.size > maxBytes) {
      error.value = `"${file.name}" 파일이 ${props.maxSize}MB를 초과합니다.`
      return
    }
  }

  if (props.multiple) {
    files.value = [...files.value, ...newFiles]
  } else {
    files.value = newFiles.slice(0, 1)
  }

  emit('change', files.value)
}

function removeFile(index) {
  files.value.splice(index, 1)
  emit('change', files.value)
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.drop-zone {
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--spacing-xl);
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--color-bg-white);
}

.drop-zone:hover,
.drop-zone.dragging {
  border-color: var(--color-primary);
  background-color: #f0f5ff;
}

.file-input-hidden {
  display: none;
}

.drop-zone-icon {
  display: block;
  font-size: 2rem;
  color: var(--color-text-disabled);
  margin-bottom: var(--spacing-sm);
}

.drop-zone-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.drop-zone-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-disabled);
  margin-top: var(--spacing-xs);
}

.file-list {
  list-style: none;
  margin-top: var(--spacing-sm);
}

.file-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  margin-bottom: var(--spacing-xs);
}

.file-name {
  flex: 1;
  font-size: var(--font-size-sm);
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.file-remove {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: var(--color-text-secondary);
  padding: 0 4px;
  cursor: pointer;
  line-height: 1;
}

.file-remove:hover {
  color: var(--color-danger);
}

.file-error {
  margin-top: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--color-danger);
}
</style>
