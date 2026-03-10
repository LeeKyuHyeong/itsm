import api from '../index.js'

export const systemConfigApi = {
  getList() {
    return api.get('/admin/system-configs')
  },
  update(key, data) {
    return api.patch(`/admin/system-configs/${key}`, data)
  }
}
