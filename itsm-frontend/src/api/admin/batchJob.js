import api from '../index.js'

export const batchJobApi = {
  getList() {
    return api.get('/admin/batch-jobs')
  },
  getDetail(id) {
    return api.get(`/admin/batch-jobs/${id}`)
  },
  update(id, data) {
    return api.patch(`/admin/batch-jobs/${id}`, data)
  }
}
