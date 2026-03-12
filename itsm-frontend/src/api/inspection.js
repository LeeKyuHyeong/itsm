import api from './index.js'

export const inspectionApi = {
  getList(params) {
    return api.get('/inspections', { params })
  },
  create(data) {
    return api.post('/inspections', data)
  },
  getDetail(id) {
    return api.get(`/inspections/${id}`)
  },
  update(id, data) {
    return api.patch(`/inspections/${id}`, data)
  },
  changeStatus(id, data) {
    return api.patch(`/inspections/${id}/status`, data)
  },
  getItems(id) {
    return api.get(`/inspections/${id}/items`)
  },
  addItem(id, data) {
    return api.post(`/inspections/${id}/items`, data)
  },
  deleteItem(id, itemId) {
    return api.delete(`/inspections/${id}/items/${itemId}`)
  },
  getResults(id) {
    return api.get(`/inspections/${id}/results`)
  },
  saveResult(id, data) {
    return api.post(`/inspections/${id}/results`, data)
  },
  getHistory(id) {
    return api.get(`/inspections/${id}/history`)
  }
}
