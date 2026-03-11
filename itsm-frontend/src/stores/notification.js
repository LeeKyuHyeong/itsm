import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationApi } from '@/api/notification.js'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])

  const unreadCount = computed(() =>
    notifications.value.filter(n => !n.readAt).length
  )

  function setNotifications(list) {
    notifications.value = list
  }

  async function fetchNotifications() {
    try {
      const { data } = await notificationApi.getList({ size: 20 })
      const result = data.data || data
      notifications.value = result.content || result.items || result || []
    } catch (e) {
      console.error('알림 목록 로드 실패:', e)
    }
  }

  async function fetchUnreadCount() {
    try {
      const { data } = await notificationApi.getList({ size: 100, unreadOnly: true })
      const result = data.data || data
      const list = result.content || result.items || result || []
      // Merge unread into existing notifications
      list.forEach(n => {
        const existing = notifications.value.find(e => e.id === n.id)
        if (!existing) {
          notifications.value.push(n)
        }
      })
    } catch (e) {
      console.error('미읽은 알림 조회 실패:', e)
    }
  }

  function markAsRead(id) {
    const item = notifications.value.find(n => n.id === id)
    if (item) {
      item.readAt = new Date().toISOString()
    }
  }

  function markAllAsRead() {
    const now = new Date().toISOString()
    notifications.value.forEach(n => {
      if (!n.readAt) n.readAt = now
    })
  }

  function clearNotifications() {
    notifications.value = []
  }

  return {
    notifications,
    unreadCount,
    setNotifications,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    clearNotifications
  }
})
