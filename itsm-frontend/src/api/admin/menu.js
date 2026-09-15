import api from '../index.js'

// 백엔드 AdminMenuController 계약 (2026-09-16 P3 로 POST/PATCH 신설).
// 과거 updateOrder(PATCH /admin/menus/order) 는 백엔드에 없고 호출처도 없어 제거.
export const menuApi = {
  getMyMenus() {
    return api.get('/admin/menus')
  },
  getList() {
    return api.get('/admin/menus')
  },
  create(data) {
    return api.post('/admin/menus', data)
  },
  update(menuId, data) {
    return api.patch(`/admin/menus/${menuId}`, data)
  }
}
