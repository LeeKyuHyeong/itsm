import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key) => key, te: () => false, locale: ref('ko') })
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() })
}))

vi.mock('@/i18n/index.js', () => ({ setLocale: vi.fn() }))

vi.mock('@/stores/auth.js', () => ({
  useAuthStore: () => ({ user: { userNm: '홍길동', roles: ['ITSM_ADMIN'] }, logout: vi.fn() })
}))

import AppHeader from './AppHeader.vue'

/**
 * 2026-09-16 전수조사 P5 — 헤더의 종 버튼은 클릭 핸들러도 배지도 없는 장식이었고,
 * 실제 알림 컴포넌트(NotificationDropdown)는 어디에도 마운트되지 않았다.
 */
describe('AppHeader', () => {
  it('알림 드롭다운 컴포넌트를 실제로 마운트한다 (장식용 종 버튼이 아니라)', () => {
    const wrapper = mount(AppHeader, {
      global: { stubs: { NotificationDropdown: true } }
    })

    expect(wrapper.findComponent({ name: 'NotificationDropdown' }).exists()).toBe(true)
    expect(wrapper.find('.notification-btn').exists()).toBe(false)
  })
})
