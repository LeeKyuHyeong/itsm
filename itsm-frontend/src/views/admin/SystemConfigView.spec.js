import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

vi.mock('vue-i18n', () => ({ useI18n: () => ({ t: (key) => key }) }))
vi.mock('@/utils/date.js', () => ({ formatDate: (d) => d || '' }))
const toast = { success: vi.fn(), error: vi.fn() }
vi.mock('@/composables/useToast.js', () => ({ useToast: () => toast }))
vi.mock('@/api/admin/systemConfig.js', () => ({
  systemConfigApi: { getList: vi.fn(), update: vi.fn() }
}))

import { systemConfigApi } from '@/api/admin/systemConfig.js'
import SystemConfigView from './SystemConfigView.vue'

/**
 * 2026-09-16 전수조사 P2 — api/admin/systemConfig.js 는 있었지만 부르는 화면이 없었다.
 * P3 에서 login.fail.lock.count / password.expire.days / password.min.length 가 실제로 소비되기 시작했으므로
 * 관리자가 값을 바꿀 화면이 필요하다.
 */
describe('SystemConfigView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    systemConfigApi.getList.mockResolvedValue({
      data: {
        data: [
          {
            configId: 1,
            configKey: 'login.fail.lock.count',
            configVal: '5',
            description: '로그인 실패 잠금 횟수',
            updatedAt: '2026-09-16T00:00:00'
          },
          {
            configId: 2,
            configKey: 'password.expire.days',
            configVal: '90',
            description: '비밀번호 만료 기간 (일)',
            updatedAt: null
          }
        ]
      }
    })
    systemConfigApi.update.mockResolvedValue({
      data: { success: true, data: { configKey: 'login.fail.lock.count', configVal: '3' } }
    })
  })

  it('마운트 시 설정 목록을 불러 키·값·설명을 표로 보여준다', async () => {
    const wrapper = mount(SystemConfigView)
    await flushPromises()

    const rows = wrapper.findAll('tbody tr.config-row')
    expect(rows).toHaveLength(2)
    expect(rows[0].text()).toContain('login.fail.lock.count')
    expect(rows[0].text()).toContain('5')
    expect(rows[0].text()).toContain('로그인 실패 잠금 횟수')
  })

  it('편집 → 저장하면 PATCH /admin/system-configs/{key} 에 { configVal } 만 보내고 목록을 다시 읽는다', async () => {
    const wrapper = mount(SystemConfigView)
    await flushPromises()

    await wrapper.findAll('tbody tr.config-row')[0].find('button.btn-edit').trigger('click')
    await wrapper.find('input.config-input').setValue('3')
    await wrapper.find('button.btn-save').trigger('click')
    await flushPromises()

    expect(systemConfigApi.update).toHaveBeenCalledWith('login.fail.lock.count', { configVal: '3' })
    expect(systemConfigApi.getList).toHaveBeenCalledTimes(2)
    expect(toast.success).toHaveBeenCalled()
  })

  it('저장 실패 시 오류 토스트를 띄우고 편집 상태를 유지한다', async () => {
    systemConfigApi.update.mockRejectedValue({
      response: { data: { error: { message: '권한 없음' } } }
    })
    const wrapper = mount(SystemConfigView)
    await flushPromises()

    await wrapper.findAll('tbody tr.config-row')[0].find('button.btn-edit').trigger('click')
    await wrapper.find('input.config-input').setValue('abc')
    await wrapper.find('button.btn-save').trigger('click')
    await flushPromises()

    expect(toast.error).toHaveBeenCalledWith('권한 없음')
    expect(wrapper.find('input.config-input').exists()).toBe(true)
  })
})
