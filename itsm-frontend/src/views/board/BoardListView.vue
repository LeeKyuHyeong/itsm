<template>
  <div class="board-list">
    <div class="page-header">
      <h2>{{ boardConfig.boardNm || t('board.title') }}</h2>
      <button class="btn btn-primary" @click="$router.push(`/boards/${boardId}/posts/new`)">{{ t('board.writePost') }}</button>
    </div>

    <BaseTable :columns="columns" :data="posts" :loading="loading" :empty-message="t('board.noPost')"
               @row-click="goDetail">
      <template #title="{ row }">
        <span v-if="row.isNotice === 'Y'" class="notice-badge">[{{ t('board.notice') }}]</span>
        {{ row.title }}
      </template>
      <template #createdAt="{ row }">
        {{ formatDate(row.createdAt) }}
      </template>
    </BaseTable>

    <BasePagination v-if="totalPages > 1" :current-page="page" :total-pages="totalPages"
                    :total-elements="totalElements" @page-change="changePage" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { boardApi } from '@/api/board.js'
import BaseTable from '@/components/common/BaseTable.vue'
import BasePagination from '@/components/common/BasePagination.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const boardId = route.params.boardId

const loading = ref(false)
const boardConfig = ref({})
const posts = ref([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

const columns = computed(() => [
  { key: 'postId', label: 'ID', width: '60px', align: 'center' },
  { key: 'title', label: t('board.postTitle') },
  { key: 'viewCnt', label: t('board.views'), width: '80px', align: 'center' },
  { key: 'createdAt', label: t('board.createdAt'), width: '150px' }
])

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const res = await boardApi.getPosts(boardId, { page: page.value, size: 20 })
    const data = res.data.data || res.data
    posts.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
    if (posts.value.length && posts.value[0].boardNm) {
      boardConfig.value = { boardNm: posts.value[0].boardNm }
    }
  } catch (e) {
    console.error('게시글 목록 조회 실패:', e)
  } finally {
    loading.value = false
  }
}

const changePage = (p) => {
  page.value = p
  fetchPosts()
}

const goDetail = (row) => {
  router.push(`/boards/${boardId}/posts/${row.postId}`)
}

onMounted(async () => {
  try {
    const res = await boardApi.getConfigs()
    const configs = res.data.data || res.data
    const config = (configs || []).find(c => String(c.boardId) === String(boardId))
    if (config) boardConfig.value = config
  } catch (e) {
    console.error('게시판 설정 조회 실패:', e)
  }
  fetchPosts()
})
</script>

<style scoped>
.board-list { padding: var(--spacing-lg); }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--spacing-lg);
}
.page-header h2 { margin: 0; font-size: var(--font-size-xl); }
.notice-badge {
  display: inline-block; color: #dc2626; font-weight: 600;
  font-size: var(--font-size-xs); margin-right: 4px;
}
.btn {
  padding: 6px 16px; border: none; border-radius: 4px;
  cursor: pointer; font-size: var(--font-size-sm);
}
.btn-primary { background: var(--color-primary); color: #fff; }
</style>
