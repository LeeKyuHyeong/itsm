import api from './index.js'

export const authApi = {
  login(credentials) {
    return api.post('/auth/login', credentials)
  },
  logout() {
    return api.post('/auth/logout')
  },
  refresh() {
    return api.post('/auth/refresh', null, { withCredentials: true })
  },
  getMe() {
    return api.get('/auth/me')
  },
  changePassword(payload) {
    return api.patch('/auth/password', payload)
  }
}
