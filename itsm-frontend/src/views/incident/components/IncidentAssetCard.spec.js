import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

vi.mock('vue-i18n', () => ({ useI18n: () => ({ t: (key) => key }) }))
vi.mock('@/utils/date.js', () => ({ formatDate: (d) => d || '' }))
vi.mock('@/api/asset.js', () => ({
  assetHwApi: { getList: vi.fn() },
  assetSwApi: { getList: vi.fn() },
  assetOaApi: { getList: vi.fn() }
}))

import { assetHwApi, assetSwApi } from '@/api/asset.js'
import IncidentAssetCard from './IncidentAssetCard.vue'

/**
 * 2026-09-16 전수조사 P2 — 설계 핵심 "CMDB: 모든 모듈이 자산에 연결" 인데 장애↔자산 연결 API
 * (incidentApi.getAssets/addAsset/removeAsset)를 부르는 화면이 없었다. 이 카드가 그 경로를 연다.
 */
const BaseModalStub = {
  props: ['show', 'title', 'width'],
  template: '<div v-if="show" class="modal-stub"><slot /><slot name="footer" /></div>'
}

function mountCard(props = {}) {
  return mount(IncidentAssetCard, {
    props: { assets: [], canEdit: true, ...props },
    global: { stubs: { BaseModal: BaseModalStub } }
  })
}

describe('IncidentAssetCard (장애 ↔ 자산 연결)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    assetHwApi.getList.mockResolvedValue({
      data: {
        data: {
          content: [
            { assetHwId: 5, assetNm: 'WEB-01', serialNo: 'SN5' },
            { assetHwId: 6, assetNm: 'DB-01', serialNo: 'SN6' }
          ]
        }
      }
    })
    assetSwApi.getList.mockResolvedValue({
      data: { data: { content: [{ assetSwId: 9, swNm: 'Tomcat' }] } }
    })
  })

  it('연결된 자산이 없으면 안내 문구, 있으면 유형·이름을 나열하고 해제 버튼을 보여준다', () => {
    const empty = mountCard()
    expect(empty.text()).toContain('incident.noLinkedAsset')

    const wrapper = mountCard({
      assets: [
        { assetType: 'HW', assetId: 5, assetNm: 'WEB-01', createdAt: '2026-09-16T00:00:00' },
        { assetType: 'SW', assetId: 9, assetNm: 'Tomcat', createdAt: '2026-09-16T00:00:00' }
      ]
    })
    const rows = wrapper.findAll('.asset-item')
    expect(rows).toHaveLength(2)
    expect(rows[0].text()).toContain('HW')
    expect(rows[0].text()).toContain('WEB-01')
    expect(rows[0].find('.btn-link.danger').exists()).toBe(true)
  })

  it('해제 버튼은 unlink-asset 을 (assetType, assetId) 로 emit 한다', async () => {
    const wrapper = mountCard({ assets: [{ assetType: 'HW', assetId: 5, assetNm: 'WEB-01' }] })

    await wrapper.find('.asset-item .btn-link.danger').trigger('click')

    expect(wrapper.emitted('unlink-asset')).toEqual([['HW', 5]])
  })

  it('연결 모달: 유형을 고르면 해당 자산 목록을 조회해 선택지로 보여주고, 선택 후 연결하면 link-asset 을 emit 한다', async () => {
    const wrapper = mountCard()

    await wrapper.find('button.btn').trigger('click') // 자산 연결 버튼
    await flushPromises()
    expect(assetHwApi.getList).toHaveBeenCalledTimes(1) // 기본 유형 HW

    const options = wrapper.findAll('select.asset-picker option')
    expect(options.map((o) => o.text())).toEqual([
      'common.selectPlaceholder',
      'WEB-01 (SN5)',
      'DB-01 (SN6)'
    ])

    await wrapper.find('select.asset-type').setValue('SW')
    await flushPromises()
    expect(assetSwApi.getList).toHaveBeenCalledTimes(1)
    expect(wrapper.findAll('select.asset-picker option').map((o) => o.text())).toEqual([
      'common.selectPlaceholder',
      'Tomcat'
    ])

    await wrapper.find('select.asset-picker').setValue('9')
    await wrapper.find('.btn-primary').trigger('click')

    expect(wrapper.emitted('link-asset')).toEqual([[{ assetType: 'SW', assetId: 9 }]])
  })

  it('canEdit=false 면 연결/해제 버튼이 없다', () => {
    const wrapper = mountCard({
      canEdit: false,
      assets: [{ assetType: 'HW', assetId: 5, assetNm: 'WEB-01' }]
    })

    expect(wrapper.find('button.btn').exists()).toBe(false)
    expect(wrapper.find('.btn-link.danger').exists()).toBe(false)
  })
})
