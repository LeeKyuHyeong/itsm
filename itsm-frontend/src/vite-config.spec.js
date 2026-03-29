import { describe, it, expect } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

describe('Vite production build config', () => {
  const configContent = readFileSync(
    resolve(__dirname, '..', 'vite.config.js'),
    'utf-8'
  )

  it('should drop console statements in production build via esbuild', () => {
    // vite.config.js must configure esbuild.drop to remove console in production
    expect(configContent).toContain('esbuild')
    expect(configContent).toContain("'console'")
  })

  it('should only drop console in production mode', () => {
    // The drop config should be conditional on production mode
    expect(configContent).toMatch(/mode\s*===\s*['"]production['"]/)
  })
})
