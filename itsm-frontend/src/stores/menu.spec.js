import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMenuStore } from './menu.js'

describe('menu store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('initial state has empty menus', () => {
    const store = useMenuStore()
    expect(store.menus).toEqual([])
  })

  it('setMenus updates menus', () => {
    const store = useMenuStore()
    const menus = [
      { id: 1, name: '대시보드', path: '/dashboard', children: [] },
      { id: 2, name: '장애관리', path: '/incidents', children: [] }
    ]
    store.setMenus(menus)
    expect(store.menus).toEqual(menus)
  })

  it('clearMenus resets menus', () => {
    const store = useMenuStore()
    store.setMenus([{ id: 1, name: 'Test' }])
    store.clearMenus()
    expect(store.menus).toEqual([])
  })
})
