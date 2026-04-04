import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// Mock localStorage and i18n before importing
vi.mock('@/i18n/index.js', () => ({
  getLocale: vi.fn(() => 'ko')
}))

import { formatDate, formatDateShort } from './date.js'

describe('date utils', () => {
  it('null이면 "-"를 반환한다', () => {
    expect(formatDate(null)).toBe('-')
    expect(formatDate(undefined)).toBe('-')
    expect(formatDate('')).toBe('-')
  })

  it('유효한 날짜를 포맷한다', () => {
    const result = formatDate('2026-04-04T14:30:00')
    expect(result).toContain('2026')
    expect(result).toContain('04')
  })

  it('formatDateShort는 날짜만 반환한다', () => {
    const result = formatDateShort('2026-04-04T14:30:00')
    expect(result).toContain('2026')
    // Should not contain time
    expect(result).not.toContain('14')
  })

  it('null이면 formatDateShort도 "-"를 반환한다', () => {
    expect(formatDateShort(null)).toBe('-')
  })
})
