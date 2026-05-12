import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import UserFormModal from './UserFormModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key) => key
  })
}))

function mountModal(propsOverride = {}) {
  const defaultForm = {
    loginId: '',
    name: '',
    email: '',
    password: '',
    phone: '',
    companyId: '',
    departmentId: ''
  }
  return mount(UserFormModal, {
    props: {
      show: true,
      isEditing: false,
      form: defaultForm,
      companies: [{ id: 1, name: '회사1' }, { id: 2, name: '회사2' }],
      departments: [{ id: 10, name: '부서1' }, { id: 11, name: '부서2' }],
      saving: false,
      error: '',
      ...propsOverride
    }
  })
}

describe('UserFormModal', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('show=false일 때 렌더링되지 않는다', () => {
    const wrapper = mountModal({ show: false })
    expect(wrapper.find('.modal-overlay').exists()).toBe(false)
  })

  it('show=true일 때 모달이 렌더링된다', () => {
    const wrapper = mountModal()
    expect(wrapper.find('.modal-overlay').exists()).toBe(true)
    expect(wrapper.find('.modal-title').exists()).toBe(true)
  })

  it('isEditing=false일 때 비밀번호 필드가 표시된다', () => {
    const wrapper = mountModal({ isEditing: false })
    const passwordInput = wrapper.find('input[type="password"]')
    expect(passwordInput.exists()).toBe(true)
  })

  it('isEditing=true일 때 비밀번호 필드가 숨겨지고 loginId가 disabled된다', () => {
    const wrapper = mountModal({ isEditing: true })
    expect(wrapper.find('input[type="password"]').exists()).toBe(false)
    const loginInput = wrapper.find('input[type="text"]')
    expect(loginInput.attributes('disabled')).toBeDefined()
  })

  it('이름 input 변경 시 update:form 이벤트를 emit한다', async () => {
    const wrapper = mountModal()
    const inputs = wrapper.findAll('input[type="text"]')
    const nameInput = inputs[1] // [0]=loginId, [1]=name

    await nameInput.setValue('홍길동')

    const emits = wrapper.emitted('update:form')
    expect(emits).toBeTruthy()
    expect(emits[emits.length - 1][0]).toMatchObject({ name: '홍길동' })
  })

  it('email input 변경 시 update:form 이벤트가 email 값을 포함한다', async () => {
    const wrapper = mountModal()
    const emailInput = wrapper.find('input[type="email"]')

    await emailInput.setValue('test@example.com')

    const emits = wrapper.emitted('update:form')
    expect(emits[emits.length - 1][0]).toMatchObject({ email: 'test@example.com' })
  })

  it('회사 변경 시 update:form과 load-departments 이벤트를 emit하고 departmentId가 초기화된다', async () => {
    const wrapper = mountModal({
      form: {
        loginId: 'u1', name: 'n', email: 'e@e.com', password: '', phone: '',
        companyId: 1, departmentId: 10
      }
    })
    const companySelect = wrapper.findAll('select')[0]

    await companySelect.setValue('2')

    const formEmits = wrapper.emitted('update:form')
    const loadEmits = wrapper.emitted('load-departments')
    expect(formEmits).toBeTruthy()
    expect(formEmits[formEmits.length - 1][0]).toMatchObject({ companyId: '2', departmentId: '' })
    expect(loadEmits).toBeTruthy()
    expect(loadEmits[loadEmits.length - 1][0]).toBe('2')
  })

  it('오버레이 클릭 시 close 이벤트를 emit한다', async () => {
    const wrapper = mountModal()
    await wrapper.find('.modal-overlay').trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('취소 버튼 클릭 시 close 이벤트를 emit한다', async () => {
    const wrapper = mountModal()
    await wrapper.find('.btn-default').trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('form submit 시 save 이벤트를 emit한다', async () => {
    const wrapper = mountModal()
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.emitted('save')).toBeTruthy()
  })

  it('saving=true일 때 저장 버튼이 disabled된다', () => {
    const wrapper = mountModal({ saving: true })
    const saveBtn = wrapper.find('.btn-primary')
    expect(saveBtn.attributes('disabled')).toBeDefined()
  })

  it('error prop이 있을 때 에러 메시지가 표시된다', () => {
    const wrapper = mountModal({ error: '저장 실패' })
    const errorEl = wrapper.find('.error-message')
    expect(errorEl.exists()).toBe(true)
    expect(errorEl.text()).toContain('저장 실패')
  })

  it('error prop이 비어있을 때 에러 메시지가 표시되지 않는다', () => {
    const wrapper = mountModal({ error: '' })
    expect(wrapper.find('.error-message').exists()).toBe(false)
  })

  it('회사 select에 props로 받은 회사 목록이 옵션으로 렌더링된다', () => {
    const wrapper = mountModal({
      companies: [
        { id: 1, name: '회사1' },
        { id: 2, name: '회사2' },
        { id: 3, name: '회사3' }
      ]
    })
    const companyOptions = wrapper.findAll('select')[0].findAll('option')
    expect(companyOptions).toHaveLength(4) // placeholder + 3 companies
    expect(companyOptions[1].text()).toBe('회사1')
    expect(companyOptions[3].text()).toBe('회사3')
  })

  it('부서 select에 props로 받은 부서 목록이 옵션으로 렌더링된다', () => {
    const wrapper = mountModal({
      departments: [{ id: 100, name: '부서A' }]
    })
    const deptOptions = wrapper.findAll('select')[1].findAll('option')
    expect(deptOptions).toHaveLength(2)
    expect(deptOptions[1].text()).toBe('부서A')
  })

  it('모달 카드 내부 클릭은 close를 emit하지 않는다', async () => {
    const wrapper = mountModal()
    await wrapper.find('.modal-card').trigger('click')

    expect(wrapper.emitted('close')).toBeFalsy()
  })
})
