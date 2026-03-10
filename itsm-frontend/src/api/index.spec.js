import { describe, it, expect, vi, beforeEach } from 'vitest'
import api from './index.js'

describe('Axios instance (api/index.js)', () => {
  it('has baseURL set to /api/v1', () => {
    expect(api.defaults.baseURL).toBe('/api/v1')
  })

  it('has Content-Type header set to application/json', () => {
    // axios stores post/put/patch Content-Type separately from common headers
    const hasContentType =
      api.defaults.headers['Content-Type'] === 'application/json' ||
      api.defaults.headers.post?.['Content-Type'] === 'application/json' ||
      api.defaults.headers.common?.['Content-Type'] === 'application/json'
    expect(hasContentType).toBe(true)
  })

  it('has request interceptor that attaches Authorization header when token exists', async () => {
    const storage = {}
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation((key) => storage[key] || null)
    storage['accessToken'] = 'test-jwt-token'

    // Simulate interceptor by creating a mock config
    const interceptors = api.interceptors.request.handlers
    expect(interceptors.length).toBeGreaterThan(0)

    const config = { headers: {} }
    const fulfilled = interceptors[0].fulfilled
    const result = fulfilled(config)
    expect(result.headers.Authorization).toBe('Bearer test-jwt-token')

    vi.restoreAllMocks()
  })

  it('does not attach Authorization header when no token', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockReturnValue(null)

    const config = { headers: {} }
    const fulfilled = api.interceptors.request.handlers[0].fulfilled
    const result = fulfilled(config)
    expect(result.headers.Authorization).toBeUndefined()

    vi.restoreAllMocks()
  })

  it('has response interceptor', () => {
    const responseInterceptors = api.interceptors.response.handlers
    expect(responseInterceptors.length).toBeGreaterThan(0)
  })

  it('response interceptor returns data on success', () => {
    const fulfilled = api.interceptors.response.handlers[0].fulfilled
    const response = { data: { success: true, data: { id: 1 } } }
    const result = fulfilled(response)
    expect(result).toEqual(response)
  })

  it('response interceptor rejects on error', async () => {
    const rejected = api.interceptors.response.handlers[0].rejected
    const error = { response: { status: 500, data: { message: 'Server Error' } } }

    await expect(rejected(error)).rejects.toEqual(error)
  })
})
