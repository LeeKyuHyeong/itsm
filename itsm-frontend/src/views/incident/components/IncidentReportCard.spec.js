import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key) => key })
}))
// utils/date.js 가 i18n 인스턴스를 끌어오므로 스텁
vi.mock('@/utils/date.js', () => ({ formatDate: (d) => d || '' }))

import IncidentReportCard from './IncidentReportCard.vue'

/**
 * 2026-09-16 전수조사 P3 — "JSON 스키마 기반 동적 폼" 은 DynamicForm.vue 가 어디에도 쓰이지 않았고,
 * 장애보고서는 사용자가 textarea 에 JSON 을 손으로 타이핑하는 구조였다(reportFormId=1 하드코딩, 양식 시드 없음).
 * 이 스펙은 카드가 양식 스키마로 DynamicForm 을 렌더링하고, 저장된 내용을 라벨별로 보여주는 것을 고정한다.
 */
const BaseModalStub = {
  props: ['show', 'title', 'width'],
  template: '<div v-if="show" class="modal-stub"><slot /><slot name="footer" /></div>'
}

const schema = [
  { key: 'summary', label: '장애 요약', type: 'text', required: true },
  { key: 'cause', label: '원인', type: 'textarea' },
  { key: 'solution', label: '해결 방안', type: 'textarea' }
]

function mountCard(props = {}) {
  return mount(IncidentReportCard, {
    props: {
      report: null,
      formSchema: schema,
      modelValue: {},
      ...props
    },
    global: { stubs: { BaseModal: BaseModalStub } }
  })
}

describe('IncidentReportCard (동적 폼)', () => {
  it('보고서 작성 버튼을 누르면 양식 스키마의 필드가 DynamicForm 으로 렌더링된다', async () => {
    const wrapper = mountCard()

    await wrapper.find('button.btn').trigger('click')

    const labels = wrapper.findAll('.dynamic-form .form-label').map((l) => l.text())
    expect(labels).toEqual(['장애 요약 *', '원인', '해결 방안'])
    expect(wrapper.find('textarea[rows="10"]').exists()).toBe(false) // 옛 자유 텍스트 입력은 없다
  })

  it('필드 입력 시 update:modelValue 로 객체를 올려보낸다', async () => {
    const wrapper = mountCard({ modelValue: { summary: '' } })
    await wrapper.find('button.btn').trigger('click')

    await wrapper.find('.dynamic-form input[type="text"]').setValue('DB 커넥션 고갈')

    const emitted = wrapper.emitted('update:modelValue')
    expect(emitted).toBeTruthy()
    expect(emitted[emitted.length - 1][0]).toEqual({ summary: 'DB 커넥션 고갈' })
  })

  it('저장 버튼은 save-report 를 emit 한다', async () => {
    const wrapper = mountCard({ modelValue: { summary: 'x' } })
    await wrapper.find('button.btn').trigger('click')

    await wrapper.find('.btn-primary').trigger('click')

    expect(wrapper.emitted('save-report')).toHaveLength(1)
  })

  it('저장된 보고서(JSON 문자열)는 스키마 라벨별로 값을 보여준다', () => {
    const wrapper = mountCard({
      report: {
        reportContent: JSON.stringify({ summary: 'DB 커넥션 고갈', cause: '풀 크기 부족' }),
        createdAt: '2026-09-16T10:00:00'
      }
    })

    const rows = wrapper.findAll('.report-field')
    expect(rows).toHaveLength(3)
    expect(rows[0].text()).toContain('장애 요약')
    expect(rows[0].text()).toContain('DB 커넥션 고갈')
    expect(rows[1].text()).toContain('풀 크기 부족')
    expect(wrapper.find('pre').exists()).toBe(false)
  })

  it('활성 양식이 없으면 작성 버튼이 비활성이고 안내 문구를 보여준다', () => {
    const wrapper = mountCard({ formSchema: [] })

    expect(wrapper.find('button.btn').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('incident.reportFormMissing')
  })
})
