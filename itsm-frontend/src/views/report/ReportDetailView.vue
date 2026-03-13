<template>
  <div class="report-detail">
    <div class="page-header">
      <h2>{{ t('report.detail') }} #{{ report.reportId }}</h2>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="$router.back()">{{ t('common.list') }}</button>
      </div>
    </div>

    <div class="detail-grid">
      <div class="detail-section">
        <h3>기본 정보</h3>
        <div class="info-table">
          <div class="info-row"><span class="label">양식명</span><span>{{ report.formNm }}</span></div>
          <div class="info-row"><span class="label">양식유형</span><span>{{ report.formTypeCd }}</span></div>
          <div class="info-row"><span class="label">참조유형</span><span>{{ report.refType }}</span></div>
          <div class="info-row"><span class="label">참조ID</span><span>{{ report.refId }}</span></div>
          <div class="info-row"><span class="label">등록일시</span><span>{{ formatDate(report.createdAt) }}</span></div>
        </div>
      </div>

      <div class="detail-section">
        <h3>보고서 내용</h3>
        <pre class="report-content">{{ formattedContent }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { reportApi } from '@/api/report.js'

const { t } = useI18n()
const route = useRoute()
const report = ref({})

const formattedContent = computed(() => {
  if (!report.value.reportContent) return '-'
  try {
    return JSON.stringify(JSON.parse(report.value.reportContent), null, 2)
  } catch {
    return report.value.reportContent
  }
})

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(async () => {
  try {
    const res = await reportApi.getDetail(route.params.id)
    report.value = res.data.data || res.data
  } catch (e) {
    console.error('보고서 조회 실패:', e)
    alert(t('message.loadFail'))
  }
})
</script>

<style scoped>
.report-detail { padding: var(--spacing-lg); max-width: 900px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--spacing-lg);
}
.page-header h2 { margin: 0; font-size: var(--font-size-xl); }
.header-actions { display: flex; gap: var(--spacing-xs); }
.detail-grid { display: flex; flex-direction: column; gap: var(--spacing-lg); }
.detail-section {
  background: #fff; border: 1px solid var(--color-border);
  border-radius: 8px; padding: var(--spacing-md);
}
.detail-section h3 { margin: 0 0 var(--spacing-sm); font-size: var(--font-size-md); }
.info-table { display: grid; gap: var(--spacing-xs); }
.info-row { display: grid; grid-template-columns: 100px 1fr; gap: var(--spacing-sm); padding: 4px 0; }
.info-row .label { font-weight: 600; color: var(--color-text-secondary); font-size: var(--font-size-sm); }
.report-content {
  background: var(--color-bg-secondary); padding: var(--spacing-md);
  border-radius: 4px; font-size: var(--font-size-sm);
  white-space: pre-wrap; word-break: break-word; margin: 0;
}
.btn {
  padding: 6px 16px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-secondary { background: var(--color-bg-secondary); border: 1px solid var(--color-border); }
</style>
