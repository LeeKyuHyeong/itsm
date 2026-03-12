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
    return api.put(`/reports/${id}`, data)
  },
  delete(id) {
    return api.delete(`/reports/${id}`)
  },
  getForms(params) {
    return api.get('/report-forms', { params })
  },
  createForm(data) {
    return api.post('/report-forms', data)
  },
  updateForm(id, data) {
    return api.put(`/report-forms/${id}`, data)
  },
  deleteForm(id) {
    return api.delete(`/report-forms/${id}`)
  }
}
