import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key) => key })
}))

const push = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

vi.mock('@/api/notification.js', () => ({
  notificationApi: {
    getList: vi.fn(),
    getUnreadCount: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn()
  }
}))

import { notificationApi } from '@/api/notification.js'
import NotificationDropdown from './NotificationDropdown.vue'

/**
 * 2026-09-16 전수조사 P5 — 알림 드롭다운은 어디에도 마운트되지 않았고(AppHeader 의 종 버튼은 장식),
 * 마운트했더라도 `noti.id`(실제 필드는 notiId) 로 markAsRead(undefined) 를 호출했으며,
 * 배지 갱신 경로(fetchUnreadCount)는 호출처가 없었다. 이 스펙이 그 세 가지를 고정한다.
 */
describe('NotificationDropdown', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
    push.mockReset()
    notificationApi.getUnreadCount.mockResolvedValue({ data: { data: 3 } })
    notificationApi.getList.mockResolvedValue({
      data: {
        data: [
          {
            notiId: 5,
            title: 'SLA 경고',
            readAt: null,
            refType: 'INCIDENT',
            refId: 7,
            createdAt: '2026-09-16T00:00:00'
          },
          {
            notiId: 6,
            title: '변경 승인 요청',
            readAt: null,
            refType: 'CHANGE',
            refId: 2,
            createdAt: '2026-09-15T00:00:00'
          }
        ]
      }
    })
    notificationApi.markAsRead.mockResolvedValue({ data: { success: true } })
    notificationApi.markAllAsRead.mockResolvedValue({ data: { success: true } })
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.clearAllMocks()
  })

  it('마운트 시 unread-count 만 조회해 배지를 그린다 (목록은 열 때까지 안 부른다)', async () => {
    const wrapper = mount(NotificationDropdown)
    await flushPromises()

    expect(notificationApi.getUnreadCount).toHaveBeenCalledTimes(1)
    expect(notificationApi.getList).not.toHaveBeenCalled()
    expect(wrapper.find('.badge').text()).toBe('3')
  })

  it('60초마다 unread-count 를 다시 조회하고, 언마운트 후에는 멈춘다', async () => {
    const wrapper = mount(NotificationDropdown)
    await flushPromises()
    expect(notificationApi.getUnreadCount).toHaveBeenCalledTimes(1)

    await vi.advanceTimersByTimeAsync(60_000)
    expect(notificationApi.getUnreadCount).toHaveBeenCalledTimes(2)

    wrapper.unmount()
    await vi.advanceTimersByTimeAsync(120_000)
    expect(notificationApi.getUnreadCount).toHaveBeenCalledTimes(2)
  })

  it('종을 누르면 목록을 불러 렌더링하고, 항목 클릭 시 notiId 로 읽음 처리 후 안전 링크로 이동한다', async () => {
    const wrapper = mount(NotificationDropdown)
    await flushPromises()

    await wrapper.find('.bell-button').trigger('click')
    await flushPromises()

    expect(notificationApi.getList).toHaveBeenCalledTimes(1)
    const items = wrapper.findAll('.noti-item')
    expect(items).toHaveLength(2)
    expect(items[0].classes()).toContain('unread')

    await items[0].trigger('click')
    await flushPromises()

    expect(notificationApi.markAsRead).toHaveBeenCalledWith(5)
    expect(push).toHaveBeenCalledWith('/incidents/7')
    expect(wrapper.find('.badge').text()).toBe('1')
  })

  it('전체 읽음을 누르면 API 를 부르고 배지가 사라진다', async () => {
    const wrapper = mount(NotificationDropdown)
    await flushPromises()
    await wrapper.find('.bell-button').trigger('click')
    await flushPromises()

    await wrapper.find('.mark-all-btn').trigger('click')
    await flushPromises()

    expect(notificationApi.markAllAsRead).toHaveBeenCalledTimes(1)
    expect(wrapper.find('.badge').exists()).toBe(false)
  })
})
