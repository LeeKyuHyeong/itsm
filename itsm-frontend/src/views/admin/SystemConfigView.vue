<template>
  <div class="system-config">
    <div class="page-header">
      <h1 class="page-title">{{ t('admin.systemConfigManage') }}</h1>
    </div>

    <p class="page-desc">{{ t('admin.systemConfigDesc') }}</p>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ t('admin.configKey') }}</th>
            <th>{{ t('admin.configVal') }}</th>
            <th>{{ t('admin.configDescription') }}</th>
            <th>{{ t('admin.updatedAt') }}</th>
            <th>{{ t('admin.manage') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="text-center">{{ t('common.loading') }}</td>
          </tr>
          <tr v-else-if="configs.length === 0">
            <td colspan="5" class="text-center">{{ t('admin.noConfig') }}</td>
          </tr>
          <tr v-for="c in configs" :key="c.configKey" class="config-row">
            <td><code>{{ c.configKey }}</code></td>
            <td>
              <input
                v-if="editingKey === c.configKey"
                v-model="editingVal"
                class="form-input config-input"
                type="text"
                @keyup.enter="save(c)"
              />
              <span v-else>{{ c.configVal }}</span>
            </td>
            <td>{{ c.description }}</td>
            <td>{{ formatDate(c.updatedAt) || '-' }}</td>
            <td>
              <template v-if="editingKey === c.configKey">
                <button class="btn btn-sm btn-primary btn-save" :disabled="saving" @click="save(c)">{{ t('common.save') }}</button>
                <button class="btn btn-sm btn-default" @click="cancel">{{ t('common.cancel') }}</button>
              </template>
              <button v-else class="btn btn-sm btn-default btn-edit" @click="edit(c)">{{ t('common.edit') }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { systemConfigApi } from '@/api/admin/systemConfig.js'
import { formatDate } from '@/utils/date.js'
import { useToast } from '@/composables/useToast.js'

/**
 * 시스템 설정 (tb_system_config) 관리 화면 — 2026-09-16 전수조사 P2.
 * api/admin/systemConfig.js 는 있었지만 화면이 없었다. P3 에서 login.fail.lock.count / password.expire.days /
 * password.min.length 가 실제로 소비되므로 관리자가 바꿀 곳이 필요하다. 값은 재시작 없이 즉시 반영된다(캐시 evict).
 */
const { t } = useI18n()
const toast = useToast()

const configs = ref([])
const loading = ref(false)
const saving = ref(false)
const editingKey = ref(null)
const editingVal = ref('')

async function load() {
  loading.value = true
  try {
    const { data } = await systemConfigApi.getList()
    const result = data.data || data
    configs.value = Array.isArray(result) ? result : result.content || []
  } catch (error) {
    console.error('Failed to load system configs:', error)
    configs.value = []
  } finally {
    loading.value = false
  }
}

function edit(c) {
  editingKey.value = c.configKey
  editingVal.value = c.configVal ?? ''
}

function cancel() {
  editingKey.value = null
  editingVal.value = ''
}

async function save(c) {
  saving.value = true
  try {
    await systemConfigApi.update(c.configKey, { configVal: editingVal.value })
    toast.success(t('message.saveSuccess'))
    cancel()
    await load()
  } catch (error) {
    toast.error(error.response?.data?.error?.message || t('message.saveFail'))
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.system-config {
  max-width: 1200px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}
.page-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--color-text);
}
.page-desc {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-md);
}
.config-input {
  width: 100%;
  max-width: 320px;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: var(--font-size-sm);
  background: var(--color-bg-white);
  color: var(--color-text);
}
code {
  font-size: var(--font-size-sm);
  color: var(--color-text);
}
.btn-sm + .btn-sm {
  margin-left: 4px;
}
</style>
