import { describe, it, expect } from 'vitest'

describe('Project setup', () => {
  it('has Vue as a dependency', async () => {
    const vue = await import('vue')
    expect(vue.createApp).toBeDefined()
  })

  it('has Pinia as a dependency', async () => {
    const pinia = await import('pinia')
    expect(pinia.createPinia).toBeDefined()
  })

  it('has Vue Router as a dependency', async () => {
    const router = await import('vue-router')
    expect(router.createRouter).toBeDefined()
  })

  it('has Axios as a dependency', async () => {
    const axios = await import('axios')
    expect(axios.default).toBeDefined()
  })

  it('router has default route redirecting to /dashboard', async () => {
    const routerModule = await import('./router/index.js')
    const router = routerModule.default
    const homeRoute = router.getRoutes().find(r => r.name === 'Home')
    expect(homeRoute).toBeDefined()
    expect(homeRoute.redirect).toBe('/dashboard')
  })
})
