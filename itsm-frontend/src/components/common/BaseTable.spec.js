import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import BaseTable from './BaseTable.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key, params) => {
      if (params && typeof params === 'object') {
        return key + ' ' + JSON.stringify(params)
      }
      return key
    }
  })
}))

const columns = [
  { key: 'id', label: 'ID', width: '80px' },
  { key: 'name', label: '이름' }
]

const data = [
  { id: 1, name: 'Alice' },
  { id: 2, name: 'Bob' },
  { id: 3, name: 'Charlie' }
]

function mountTable(propsOverride = {}) {
  return mount(BaseTable, {
    props: { columns, data, ...propsOverride },
    attachTo: document.body
  })
}

describe('BaseTable', () => {
  beforeEach(() => {
    document.body.innerHTML = ''
  })

  it('컬럼과 데이터로 테이블을 렌더링한다', () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr')
    expect(rows).toHaveLength(3)
    expect(rows[0].text()).toContain('Alice')
  })

  it('loading=true일 때 로딩 메시지를 표시한다', () => {
    const wrapper = mountTable({ loading: true })
    expect(wrapper.find('tbody').text()).toContain('common.loading')
    expect(wrapper.findAll('tbody tr.clickable-row')).toHaveLength(0)
  })

  it('데이터가 비었을 때 빈 메시지를 표시한다', () => {
    const wrapper = mountTable({ data: [] })
    expect(wrapper.find('tbody').text()).toContain('common.noData')
  })

  it('emptyMessage prop이 있으면 기본 메시지 대신 사용된다', () => {
    const wrapper = mountTable({ data: [], emptyMessage: '커스텀 빈 메시지' })
    expect(wrapper.find('tbody').text()).toContain('커스텀 빈 메시지')
  })

  it('행 클릭 시 row-click 이벤트를 emit한다', async () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')

    await rows[1].trigger('click')

    const emits = wrapper.emitted('row-click')
    expect(emits).toBeTruthy()
    expect(emits[0][0]).toEqual({ id: 2, name: 'Bob' })
    expect(emits[0][1]).toBe(1)
  })

  it('행이 tabindex=0을 갖는다 (키보드 포커스 가능)', () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')

    rows.forEach(row => {
      expect(row.attributes('tabindex')).toBe('0')
    })
  })

  it('Enter 키를 누르면 row-click 이벤트를 emit한다', async () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')

    await rows[0].trigger('keydown', { key: 'Enter' })

    const emits = wrapper.emitted('row-click')
    expect(emits).toBeTruthy()
    expect(emits[0][0]).toEqual({ id: 1, name: 'Alice' })
  })

  it('Space 키를 누르면 row-click 이벤트를 emit한다', async () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')

    await rows[2].trigger('keydown', { key: ' ' })

    const emits = wrapper.emitted('row-click')
    expect(emits).toBeTruthy()
    expect(emits[0][0]).toEqual({ id: 3, name: 'Charlie' })
  })

  it('화살표 아래 키로 다음 행에 포커스 이동한다', async () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')
    rows[0].element.focus()

    await rows[0].trigger('keydown', { key: 'ArrowDown' })
    await nextTick()

    expect(document.activeElement).toBe(rows[1].element)
  })

  it('화살표 위 키로 이전 행에 포커스 이동한다', async () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')
    rows[2].element.focus()

    await rows[2].trigger('keydown', { key: 'ArrowUp' })
    await nextTick()

    expect(document.activeElement).toBe(rows[1].element)
  })

  it('첫 행에서 화살표 위 키는 첫 행에 머문다', async () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')
    rows[0].element.focus()

    await rows[0].trigger('keydown', { key: 'ArrowUp' })
    await nextTick()

    expect(document.activeElement).toBe(rows[0].element)
  })

  it('마지막 행에서 화살표 아래 키는 마지막 행에 머문다', async () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')
    rows[2].element.focus()

    await rows[2].trigger('keydown', { key: 'ArrowDown' })
    await nextTick()

    expect(document.activeElement).toBe(rows[2].element)
  })

  it('Home 키로 첫 행에 포커스 이동한다', async () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')
    rows[2].element.focus()

    await rows[2].trigger('keydown', { key: 'Home' })
    await nextTick()

    expect(document.activeElement).toBe(rows[0].element)
  })

  it('End 키로 마지막 행에 포커스 이동한다', async () => {
    const wrapper = mountTable()
    const rows = wrapper.findAll('tbody tr.clickable-row')
    rows[0].element.focus()

    await rows[0].trigger('keydown', { key: 'End' })
    await nextTick()

    expect(document.activeElement).toBe(rows[2].element)
  })

  it('컬럼 width와 align이 스타일로 적용된다', () => {
    const wrapper = mountTable({
      columns: [{ key: 'id', label: 'ID', width: '100px', align: 'right' }]
    })
    const th = wrapper.find('th')
    expect(th.attributes('style')).toContain('width: 100px')
    expect(th.attributes('style')).toContain('text-align: right')
  })

  it('cell-{key} 슬롯이 셀 내용을 커스터마이즈한다', () => {
    const wrapper = mount(BaseTable, {
      props: { columns, data: [{ id: 1, name: 'X' }] },
      slots: {
        'cell-name': '<span class="custom-cell">커스텀</span>'
      }
    })
    expect(wrapper.find('.custom-cell').exists()).toBe(true)
  })
})
