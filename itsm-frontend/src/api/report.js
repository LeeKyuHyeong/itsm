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
    return api.patch(`/reports/${id}`, data) // 2026-09-16 P2: 백엔드는 PATCH
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
    return api.patch(`/report-forms/${id}`, data) // 2026-09-16 P2: 백엔드는 PATCH
  },
  deleteForm(id) {
    return api.delete(`/report-forms/${id}`)
  }
}
