import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationApi } from '@/api/notification.js'

/**
 * 알림 스토어.
 * - 식별자는 백엔드 NotificationResponse 와 같은 `notiId` (2026-09-16 P5: 과거 `id` 를 읽어 markAsRead(undefined) 호출)
 * - unreadCount 는 서버(/unread-count)에서 가볍게 받아 배지를 갱신하고, 전체 목록을 받으면 목록 기준으로 다시 센다.
 */
export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)

  function countUnread(list) {
    return list.filter((n) => !n.readAt).length
  }

  function setNotifications(list) {
    notifications.value = list
    unreadCount.value = countUnread(list)
  }

  function extractList(payload) {
    if (Array.isArray(payload)) return payload
    if (!payload) return []
    return payload.content || payload.items || []
  }

  async function fetchNotifications() {
    try {
      const { data } = await notificationApi.getList()
      setNotifications(extractList(data.data))
    } catch (e) {
      console.error('Failed to load notifications:', e)
    }
  }

  async function fetchUnreadCount() {
    try {
      const { data } = await notificationApi.getUnreadCount()
      const count = Number(data.data)
      unreadCount.value = Number.isFinite(count) ? count : 0
    } catch (e) {
      console.error('Failed to load unread notifications:', e)
    }
  }

  function markAsRead(notiId) {
    const item = notifications.value.find((n) => n.notiId === notiId)
    if (item && !item.readAt) {
      item.readAt = new Date().toISOString()
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  function markAllAsRead() {
    const now = new Date().toISOString()
    notifications.value.forEach((n) => {
      if (!n.readAt) n.readAt = now
    })
    unreadCount.value = 0
  }

  function clearNotifications() {
    notifications.value = []
    unreadCount.value = 0
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
