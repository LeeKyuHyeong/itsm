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
  saveResults(id, data) {
    return api.post(`/inspections/${id}/results`, data)
  }
}
