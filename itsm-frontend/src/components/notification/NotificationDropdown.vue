<template>
  <div class="notification-dropdown" ref="dropdownRef">
    <button class="bell-button" @click="toggleDropdown">
      <svg class="bell-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
      <span v-if="notificationStore.unreadCount > 0" class="badge">
        {{ notificationStore.unreadCount > 99 ? '99+' : notificationStore.unreadCount }}
      </span>
    </button>

    <div v-if="isOpen" class="dropdown-panel">
      <div class="dropdown-header">
        <h3 class="dropdown-title">알림</h3>
        <button
          v-if="notificationStore.unreadCount > 0"
          class="mark-all-btn"
          @click="handleMarkAllAsRead"
        >
          전체 읽음
        </button>
      </div>

      <div class="dropdown-body">
        <div v-if="notificationStore.notifications.length === 0" class="empty-message">
          알림이 없습니다.
        </div>
        <div
          v-for="noti in notificationStore.notifications"
          :key="noti.id"
          class="noti-item"
          :class="{ unread: !noti.readAt }"
          @click="handleClickNotification(noti)"
        >
          <div class="noti-title">{{ noti.title }}</div>
          <div class="noti-time">{{ formatTimeAgo(noti.createdAt) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification.js'
import { notificationApi } from '@/api/notification.js'

const router = useRouter()
const notificationStore = useNotificationStore()

const isOpen = ref(false)
const dropdownRef = ref(null)

onMounted(() => {
  notificationStore.fetchNotifications()
  document.addEventListener('click', handleOutsideClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})

function handleOutsideClick(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    isOpen.value = false
  }
}

function toggleDropdown() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    notificationStore.fetchNotifications()
  }
}

async function handleClickNotification(noti) {
  if (!noti.readAt) {
    try {
      await notificationApi.markAsRead(noti.id)
      notificationStore.markAsRead(noti.id)
    } catch (e) {
      console.error('알림 읽음 처리 실패:', e)
    }
  }
  if (noti.refLink) {
    router.push(noti.refLink)
  }
  isOpen.value = false
}

async function handleMarkAllAsRead() {
  try {
    await notificationApi.markAllAsRead()
    notificationStore.markAllAsRead()
  } catch (e) {
    console.error('전체 읽음 처리 실패:', e)
  }
}

function formatTimeAgo(dateStr) {
  if (!dateStr) return ''
  const now = new Date()
  const date = new Date(dateStr)
  const diffMs = now - date
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '방금 전'
  if (diffMin < 60) return `${diffMin}분 전`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour}시간 전`
  const diffDay = Math.floor(diffHour / 24)
  if (diffDay < 30) return `${diffDay}일 전`
  return date.toLocaleDateString('ko-KR')
}
</script>

<style scoped>
.notification-dropdown {
  position: relative;
}

.bell-button {
  position: relative;
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--spacing-xs);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.bell-button:hover {
  color: var(--color-text);
}

.bell-icon {
  width: 20px;
  height: 20px;
}

.badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background-color: var(--color-danger);
  color: white;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.dropdown-panel {
  position: absolute;
  top: calc(100% + var(--spacing-xs));
  right: 0;
  width: 320px;
  max-height: 400px;
  background: var(--color-bg-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
}

.dropdown-title {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text);
}

.mark-all-btn {
  background: none;
  border: none;
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  cursor: pointer;
  padding: 2px 4px;
}

.mark-all-btn:hover {
  color: var(--color-primary-dark);
}

.dropdown-body {
  overflow-y: auto;
  flex: 1;
}

.empty-message {
  padding: var(--spacing-lg);
  text-align: center;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.noti-item {
  padding: var(--spacing-sm) var(--spacing-md);
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.15s;
}

.noti-item:hover {
  background-color: var(--color-bg);
}

.noti-item.unread {
  background-color: #f0f5ff;
}

.noti-item.unread:hover {
  background-color: #e8f0fe;
}

.noti-title {
  font-size: var(--font-size-sm);
  color: var(--color-text);
  margin-bottom: 2px;
  line-height: 1.4;
}

.noti-item.unread .noti-title {
  font-weight: 600;
}

.noti-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}
</style>
