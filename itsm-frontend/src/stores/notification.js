import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])

  const unreadCount = computed(() =>
    notifications.value.filter(n => !n.readAt).length
  )

  function setNotifications(list) {
    notifications.value = list
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
    markAsRead,
    markAllAsRead,
    clearNotifications
  }
})
