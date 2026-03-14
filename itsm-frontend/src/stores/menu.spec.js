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

  it('setMenus maps API format to internal format', () => {
    const store = useMenuStore()
    const apiMenus = [
      { menuId: 1, menuNm: '대시보드', menuNmEn: 'Dashboard', menuUrl: '/dashboard', icon: 'mdi-view-dashboard', children: [] },
      { menuId: 2, menuNm: '장애관리', menuNmEn: 'Incident Management', menuUrl: '/incidents', icon: 'mdi-alert-circle', children: [] }
    ]
    store.setMenus(apiMenus)
    expect(store.menus).toEqual([
      { id: 1, name: '대시보드', nameEn: 'Dashboard', path: '/dashboard', icon: 'dashboard', children: [] },
      { id: 2, name: '장애관리', nameEn: 'Incident Management', path: '/incidents', icon: 'incident', children: [] }
    ])
  })

  it('setMenus handles missing menuNmEn gracefully', () => {
    const store = useMenuStore()
    const apiMenus = [
      { menuId: 1, menuNm: '대시보드', menuUrl: '/dashboard', icon: 'mdi-view-dashboard', children: [] }
    ]
    store.setMenus(apiMenus)
    expect(store.menus[0].nameEn).toBe('')
  })

  it('clearMenus resets menus', () => {
    const store = useMenuStore()
    store.setMenus([{ menuId: 1, menuNm: 'Test', children: [] }])
    store.clearMenus()
    expect(store.menus).toEqual([])
  })
})
