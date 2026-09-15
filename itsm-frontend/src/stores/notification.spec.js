import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationStore } from './notification.js'

vi.mock('@/api/notification.js', () => ({
  notificationApi: {
    getList: vi.fn(),
    getUnreadCount: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn()
  }
}))

import { notificationApi } from '@/api/notification.js'

// 백엔드 NotificationResponse 의 식별자는 `notiId` 다 (`id` 아님).
// 2026-09-16 전수조사 P5: 프론트가 `id` 를 읽어 markAsRead(undefined) 를 호출하던 것을 계약대로 고침.

describe('notification store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks() // vi.fn() 호출 이력 초기화 (restoreAllMocks 는 spy 만 되돌린다)
  })

  it('initial state has empty notifications', () => {
    const store = useNotificationStore()
    expect(store.notifications).toEqual([])
    expect(store.unreadCount).toBe(0)
  })

  it('setNotifications updates list', () => {
    const store = useNotificationStore()
    const list = [
      { notiId: 1, title: '알림1', readAt: null },
      { notiId: 2, title: '알림2', readAt: '2026-01-01T00:00:00Z' }
    ]
    store.setNotifications(list)
    expect(store.notifications).toEqual(list)
  })

  it('unreadCount computed from list', () => {
    const store = useNotificationStore()
    store.setNotifications([
      { notiId: 1, title: '알림1', readAt: null },
      { notiId: 2, title: '알림2', readAt: null },
      { notiId: 3, title: '알림3', readAt: '2026-01-01T00:00:00Z' }
    ])
    expect(store.unreadCount).toBe(2)
  })

  it('unreadCount is 0 when all are read', () => {
    const store = useNotificationStore()
    store.setNotifications([
      { notiId: 1, title: '알림1', readAt: '2026-01-01T00:00:00Z' },
      { notiId: 2, title: '알림2', readAt: '2026-01-01T00:00:00Z' }
    ])
    expect(store.unreadCount).toBe(0)
  })

  it('markAsRead updates single notification by notiId', () => {
    const store = useNotificationStore()
    store.setNotifications([
      { notiId: 1, title: '알림1', readAt: null },
      { notiId: 2, title: '알림2', readAt: null }
    ])

    store.markAsRead(1)

    expect(store.notifications[0].readAt).toBeTruthy()
    expect(store.notifications[1].readAt).toBeNull()
    expect(store.unreadCount).toBe(1)
  })

  it('markAsRead does nothing for unknown id', () => {
    const store = useNotificationStore()
    store.setNotifications([{ notiId: 1, title: '알림1', readAt: null }])

    store.markAsRead(999)

    expect(store.notifications[0].readAt).toBeNull()
    expect(store.unreadCount).toBe(1)
  })

  it('markAllAsRead updates all notifications', () => {
    const store = useNotificationStore()
    store.setNotifications([
      { notiId: 1, title: '알림1', readAt: null },
      { notiId: 2, title: '알림2', readAt: null },
      { notiId: 3, title: '알림3', readAt: '2026-01-01T00:00:00Z' }
    ])

    store.markAllAsRead()

    expect(store.unreadCount).toBe(0)
    store.notifications.forEach((n) => {
      expect(n.readAt).toBeTruthy()
    })
  })

  it('clearNotifications resets list and count', () => {
    const store = useNotificationStore()
    store.setNotifications([{ notiId: 1, title: '알림1', readAt: null }])
    store.clearNotifications()
    expect(store.notifications).toEqual([])
    expect(store.unreadCount).toBe(0)
  })

  it('fetchNotifications calls API (no params — backend ignores them) and sets data from a plain array', async () => {
    const store = useNotificationStore()
    const mockData = [
      { notiId: 1, title: '알림1', readAt: null },
      { notiId: 2, title: '알림2', readAt: '2026-01-01T00:00:00Z' }
    ]
    notificationApi.getList.mockResolvedValue({ data: { data: mockData } })

    await store.fetchNotifications()

    expect(notificationApi.getList).toHaveBeenCalledWith()
    expect(store.notifications).toEqual(mockData)
    expect(store.unreadCount).toBe(1)
  })

  it('fetchNotifications handles API error gracefully', async () => {
    const store = useNotificationStore()
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    notificationApi.getList.mockRejectedValue(new Error('Network error'))

    await store.fetchNotifications()

    expect(consoleSpy).toHaveBeenCalled()
  })

  it('fetchUnreadCount uses GET /notifications/unread-count and sets unreadCount without loading the list', async () => {
    const store = useNotificationStore()
    notificationApi.getUnreadCount.mockResolvedValue({ data: { data: 4 } })

    await store.fetchUnreadCount()

    expect(notificationApi.getUnreadCount).toHaveBeenCalled()
    expect(notificationApi.getList).not.toHaveBeenCalled()
    expect(store.unreadCount).toBe(4)
    expect(store.notifications).toEqual([])
  })

  it('fetchUnreadCount handles API error gracefully', async () => {
    const store = useNotificationStore()
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    notificationApi.getUnreadCount.mockRejectedValue(new Error('Network error'))

    await store.fetchUnreadCount()

    expect(consoleSpy).toHaveBeenCalled()
  })

  it('fetchNotifications still tolerates a paged shape (content)', async () => {
    const store = useNotificationStore()
    const mockData = [{ notiId: 5, title: 'paged', readAt: null }]
    notificationApi.getList.mockResolvedValue({ data: { data: { content: mockData } } })

    await store.fetchNotifications()

    expect(store.notifications).toEqual(mockData)
  })

  it('markAllAsRead preserves already-read notifications timestamp', () => {
    const store = useNotificationStore()
    const existingDate = '2025-12-01T00:00:00Z'
    store.setNotifications([
      { notiId: 1, title: '읽음', readAt: existingDate },
      { notiId: 2, title: '안읽음', readAt: null }
    ])

    store.markAllAsRead()

    expect(store.notifications[0].readAt).toBe(existingDate)
    expect(store.notifications[1].readAt).toBeTruthy()
  })
})
