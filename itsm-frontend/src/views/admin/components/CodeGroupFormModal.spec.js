import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CodeGroupFormModal from './CodeGroupFormModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key) => key
  })
}))

function mountModal(propsOverride = {}) {
  return mount(CodeGroupFormModal, {
    props: {
      show: true,
      isEditing: false,
      form: { groupCd: '', groupNm: '', groupNmEn: '', description: '' },
      saving: false,
      error: '',
      ...propsOverride
    },
    global: {
      stubs: {
        Teleport: false,
        BaseModal: {
          template: '<div class="base-modal-stub" v-if="show"><slot /></div>',
          props: ['show', 'title'],
          emits: ['close']
        }
      }
    }
  })
}

describe('CodeGroupFormModal', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('렌더링 시 BaseModal이 표시된다', () => {
    const wrapper = mountModal()
    expect(wrapper.find('.base-modal-stub').exists()).toBe(true)
  })

  it('isEditing=true일 때 groupCd input이 disabled된다', () => {
    const wrapper = mountModal({
      isEditing: true,
      form: { groupCd: 'EXISTING', groupNm: '기존', groupNmEn: '', description: '' }
    })
    const groupCdInput = wrapper.findAll('input[type="text"]')[0]
    expect(groupCdInput.attributes('disabled')).toBeDefined()
  })

  it('isEditing=false일 때 groupCd input이 활성화된다', () => {
    const wrapper = mountModal({ isEditing: false })
    const groupCdInput = wrapper.findAll('input[type="text"]')[0]
    expect(groupCdInput.attributes('disabled')).toBeUndefined()
  })

  it('groupCd input이 form.groupCd와 v-model로 바인딩된다', async () => {
    const form = { groupCd: '', groupNm: '', groupNmEn: '', description: '' }
    const wrapper = mountModal({ form })
    const groupCdInput = wrapper.findAll('input[type="text"]')[0]

    await groupCdInput.setValue('NEW_CODE')

    expect(form.groupCd).toBe('NEW_CODE')
  })

  it('groupNm input이 form.groupNm과 v-model로 바인딩된다', async () => {
    const form = { groupCd: '', groupNm: '', groupNmEn: '', description: '' }
    const wrapper = mountModal({ form })
    const groupNmInput = wrapper.findAll('input[type="text"]')[1]

    await groupNmInput.setValue('우선순위')

    expect(form.groupNm).toBe('우선순위')
  })

  it('form submit 시 save 이벤트를 emit한다', async () => {
    const wrapper = mountModal()
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.emitted('save')).toBeTruthy()
  })

  it('취소 버튼 클릭 시 close 이벤트를 emit한다', async () => {
    const wrapper = mountModal()
    await wrapper.find('.btn-default').trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('saving=true일 때 저장 버튼이 disabled된다', () => {
    const wrapper = mountModal({ saving: true })
    expect(wrapper.find('.btn-primary').attributes('disabled')).toBeDefined()
  })

  it('error prop이 있을 때 에러 메시지가 표시된다', () => {
    const wrapper = mountModal({ error: '중복된 코드' })
    const errorEl = wrapper.find('.error-message')
    expect(errorEl.exists()).toBe(true)
    expect(errorEl.text()).toContain('중복된 코드')
  })

  it('error prop이 비어있을 때 에러 메시지가 표시되지 않는다', () => {
    const wrapper = mountModal({ error: '' })
    expect(wrapper.find('.error-message').exists()).toBe(false)
  })

  it('groupNmEn과 description은 required가 아니다', () => {
    const wrapper = mountModal()
    const inputs = wrapper.findAll('input[type="text"]')
    expect(inputs[0].attributes('required')).toBeDefined() // groupCd
    expect(inputs[1].attributes('required')).toBeDefined() // groupNm
    expect(inputs[2].attributes('required')).toBeUndefined() // groupNmEn
    expect(inputs[3].attributes('required')).toBeUndefined() // description
  })
})
