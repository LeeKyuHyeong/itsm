import api from './index.js'

export const reportApi = {
  getList(params) {
    return api.get('/reports', { params })
  },
  create(data) {
    return api.post('/reports', data)
  },
  getDetail(id) {
    return api.get(`/reports/${id}`)
  },
  update(id, data) {
    return api.patch(`/reports/${id}`, data)
  },
  getForms(params) {
    return api.get('/report-forms', { params })
  }
}
