import api from './index.js'

export const companyApi = {
  getList(params) {
    return api.get('/companies', { params })
  },
  create(data) {
    return api.post('/companies', data)
  },
  getDetail(companyId) {
    return api.get(`/companies/${companyId}`)
  },
  update(companyId, data) {
    return api.patch(`/companies/${companyId}`, data)
  },
  getDepartments(companyId) {
    return api.get(`/companies/${companyId}/departments`)
  },
  createDepartment(companyId, data) {
    return api.post(`/companies/${companyId}/departments`, data)
  },
  updateDepartment(deptId, data) {
    return api.patch(`/departments/${deptId}`, data)
  }
}
