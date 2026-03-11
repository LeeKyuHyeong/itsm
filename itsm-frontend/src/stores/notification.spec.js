import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationStore } from './notification.js'

vi.mock('@/api/notification.js', () => ({
  notificationApi: {
    getList: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn()
  }
}))

import { notificationApi } from '@/api/notification.js'

describe('notification store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.restoreAllMocks()
  })

  it('initial state has empty notifications', () => {
    const store = useNotificationStore()
    expect(store.notifications).toEqual([])
    expect(store.unreadCount).toBe(0)
  })

  it('setNotifications updates list', () => {
    const store = useNotificationStore()
    const list = [
      { id: 1, title: '알림1', readAt: null },
      { id: 2, title: '알림2', readAt: '2026-01-01T00:00:00Z' }
    ]
    store.setNotifications(list)
    expect(store.notifications).toEqual(list)
  })

  it('unreadCount computed correctly', () => {
    const store = useNotificationStore()
    store.setNotifications([
      { id: 1, title: '알림1', readAt: null },
      { id: 2, title: '알림2', readAt: null },
      { id: 3, title: '알림3', readAt: '2026-01-01T00:00:00Z' }
    ])
    expect(store.unreadCount).toBe(2)
  })

  it('unreadCount is 0 when all are read', () => {
    const store = useNotificationStore()
    store.setNotifications([
      { id: 1, title: '알림1', readAt: '2026-01-01T00:00:00Z' },
      { id: 2, title: '알림2', readAt: '2026-01-01T00:00:00Z' }
    ])
    expect(store.unreadCount).toBe(0)
  })

  it('markAsRead updates single notification', () => {
    const store = useNotificationStore()
    store.setNotifications([
      { id: 1, title: '알림1', readAt: null },
      { id: 2, title: '알림2', readAt: null }
    ])

    store.markAsRead(1)

    expect(store.notifications[0].readAt).toBeTruthy()
    expect(store.notifications[1].readAt).toBeNull()
    expect(store.unreadCount).toBe(1)
  })

  it('markAsRead does nothing for unknown id', () => {
    const store = useNotificationStore()
    store.setNotifications([{ id: 1, title: '알림1', readAt: null }])

    store.markAsRead(999)

    expect(store.notifications[0].readAt).toBeNull()
    expect(store.unreadCount).toBe(1)
  })

  it('markAllAsRead updates all notifications', () => {
    const store = useNotificationStore()
    store.setNotifications([
      { id: 1, title: '알림1', readAt: null },
      { id: 2, title: '알림2', readAt: null },
      { id: 3, title: '알림3', readAt: '2026-01-01T00:00:00Z' }
    ])

    store.markAllAsRead()

    expect(store.unreadCount).toBe(0)
    store.notifications.forEach(n => {
      expect(n.readAt).toBeTruthy()
    })
  })

  it('clearNotifications resets list', () => {
    const store = useNotificationStore()
    store.setNotifications([{ id: 1, title: '알림1', readAt: null }])
    store.clearNotifications()
    expect(store.notifications).toEqual([])
    expect(store.unreadCount).toBe(0)
  })

  it('fetchNotifications calls API and sets data', async () => {
    const store = useNotificationStore()
    const mockData = [
      { id: 1, title: '알림1', readAt: null },
      { id: 2, title: '알림2', readAt: '2026-01-01T00:00:00Z' }
    ]
    notificationApi.getList.mockResolvedValue({
      data: { data: { content: mockData } }
    })

    await store.fetchNotifications()

    expect(notificationApi.getList).toHaveBeenCalledWith({ size: 20 })
    expect(store.notifications).toEqual(mockData)
  })

  it('fetchNotifications handles API error gracefully', async () => {
    const store = useNotificationStore()
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    notificationApi.getList.mockRejectedValue(new Error('Network error'))

    await store.fetchNotifications()

    expect(consoleSpy).toHaveBeenCalled()
  })

  it('fetchUnreadCount merges new unread notifications', async () => {
    const store = useNotificationStore()
    store.setNotifications([{ id: 1, title: '기존', readAt: null }])

    notificationApi.getList.mockResolvedValue({
      data: { data: { content: [
        { id: 1, title: '기존', readAt: null },
        { id: 2, title: '신규', readAt: null }
      ] } }
    })

    await store.fetchUnreadCount()

    expect(store.notifications).toHaveLength(2)
    expect(store.notifications.find(n => n.id === 2)).toBeTruthy()
  })
})
