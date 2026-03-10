import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'

describe('App.vue', () => {
  it('renders router-view', async () => {
    const TestComponent = { template: '<div class="test-page">Test Page</div>' }
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/', component: TestComponent }]
    })
    router.push('/')
    await router.isReady()

    const wrapper = mount(App, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.find('.test-page').exists()).toBe(true)
    expect(wrapper.find('.test-page').text()).toBe('Test Page')
  })
})
