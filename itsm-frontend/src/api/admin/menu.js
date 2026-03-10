import api from '../index.js'

export const menuApi = {
  getList() {
    return api.get('/admin/menus')
  },
  create(data) {
    return api.post('/admin/menus', data)
  },
  update(menuId, data) {
    return api.patch(`/admin/menus/${menuId}`, data)
  },
  updateOrder(data) {
    return api.patch('/admin/menus/order', data)
  }
}
