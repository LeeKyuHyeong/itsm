import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
})

// Response interceptor: 에러 핸들링
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // 401 Unauthorized - Access Token 만료 시 Refresh 시도
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      try {
        await axios.post('/api/v1/auth/refresh', null, {
          withCredentials: true
        })
        return api(originalRequest)
      } catch (refreshError) {
        const { default: router } = await import('@/router/index.js')
        router.push('/login')
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default api
