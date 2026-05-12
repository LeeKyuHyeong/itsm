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

  it('setMenus handles null input as empty array', () => {
    const store = useMenuStore()
    store.setMenus(null)
    expect(store.menus).toEqual([])
  })

  it('setMenus handles undefined input as empty array', () => {
    const store = useMenuStore()
    store.setMenus(undefined)
    expect(store.menus).toEqual([])
  })

  it('setMenus recursively maps children menus', () => {
    const store = useMenuStore()
    store.setMenus([
      {
        menuId: 1,
        menuNm: '관리',
        menuNmEn: 'Admin',
        menuUrl: '/admin',
        icon: 'mdi-cog',
        children: [
          { menuId: 11, menuNm: '사용자', menuNmEn: 'Users', menuUrl: '/admin/users', icon: 'mdi-account-group', children: [] },
          { menuId: 12, menuNm: '게시판', menuNmEn: 'Boards', menuUrl: '/admin/boards', icon: 'mdi-bulletin-board', children: [] }
        ]
      }
    ])

    expect(store.menus).toHaveLength(1)
    expect(store.menus[0].icon).toBe('admin')
    expect(store.menus[0].children).toHaveLength(2)
    expect(store.menus[0].children[0].id).toBe(11)
    expect(store.menus[0].children[0].icon).toBe('users')
    expect(store.menus[0].children[1].icon).toBe('board')
  })

  it('setMenus falls back to raw icon string when unknown', () => {
    const store = useMenuStore()
    store.setMenus([
      { menuId: 1, menuNm: '커스텀', menuUrl: '/custom', icon: 'mdi-unknown-icon', children: [] }
    ])
    expect(store.menus[0].icon).toBe('mdi-unknown-icon')
  })

  it('setMenus handles missing children array', () => {
    const store = useMenuStore()
    store.setMenus([
      { menuId: 1, menuNm: 'NoChildren', menuUrl: '/x', icon: 'mdi-cog' }
    ])
    expect(store.menus[0].children).toEqual([])
  })
})
