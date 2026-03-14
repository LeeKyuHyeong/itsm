import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCommonCodeStore } from './commonCode.js'

vi.mock('@/api/index.js', () => ({
  default: {
    get: vi.fn()
  }
}))

const mockLocale = { value: 'ko' }
vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    locale: mockLocale
  })
}))

import api from '@/api/index.js'

describe('commonCode store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockLocale.value = 'ko'
    vi.restoreAllMocks()
  })

  it('initial state has empty codeMap', () => {
    const store = useCommonCodeStore()
    expect(store.codeMap).toEqual({})
  })

  it('setCodes stores codes for a group', () => {
    const store = useCommonCodeStore()
    const codes = [
      { code: 'HIGH', name: '높음', nameEn: 'High' },
      { code: 'LOW', name: '낮음', nameEn: 'Low' }
    ]
    store.setCodes('PRIORITY', codes)
    expect(store.codeMap['PRIORITY']).toEqual(codes)
  })

  it('getCodes returns Korean names when locale is ko', () => {
    const store = useCommonCodeStore()
    store.setCodes('TEST_GROUP', [
      { code: 'A', name: '가', nameEn: 'Alpha' }
    ])
    const result = store.getCodes('TEST_GROUP')
    expect(result[0].name).toBe('가')
  })

  it('getCodes returns English names when locale is en', () => {
    mockLocale.value = 'en'
    const store = useCommonCodeStore()
    store.setCodes('TEST_GROUP', [
      { code: 'A', name: '가', nameEn: 'Alpha' }
    ])
    const result = store.getCodes('TEST_GROUP')
    expect(result[0].name).toBe('Alpha')
  })

  it('getCodes falls back to Korean name when nameEn is empty and locale is en', () => {
    mockLocale.value = 'en'
    const store = useCommonCodeStore()
    store.setCodes('TEST_GROUP', [
      { code: 'A', name: '가', nameEn: '' }
    ])
    const result = store.getCodes('TEST_GROUP')
    expect(result[0].name).toBe('가')
  })

  it('getCodes returns empty array for unknown group', () => {
    const store = useCommonCodeStore()
    expect(store.getCodes('UNKNOWN')).toEqual([])
  })

  it('getCodeName returns Korean name when locale is ko', () => {
    const store = useCommonCodeStore()
    store.setCodes('STATUS', [
      { code: 'OPEN', name: '열림', nameEn: 'Open' },
      { code: 'CLOSED', name: '닫힘', nameEn: 'Closed' }
    ])
    expect(store.getCodeName('STATUS', 'OPEN')).toBe('열림')
    expect(store.getCodeName('STATUS', 'CLOSED')).toBe('닫힘')
  })

  it('getCodeName returns English name when locale is en', () => {
    mockLocale.value = 'en'
    const store = useCommonCodeStore()
    store.setCodes('STATUS', [
      { code: 'OPEN', name: '열림', nameEn: 'Open' },
      { code: 'CLOSED', name: '닫힘', nameEn: 'Closed' }
    ])
    expect(store.getCodeName('STATUS', 'OPEN')).toBe('Open')
    expect(store.getCodeName('STATUS', 'CLOSED')).toBe('Closed')
  })

  it('getCodeName falls back to Korean when nameEn is empty', () => {
    mockLocale.value = 'en'
    const store = useCommonCodeStore()
    store.setCodes('STATUS', [
      { code: 'OPEN', name: '열림', nameEn: '' }
    ])
    expect(store.getCodeName('STATUS', 'OPEN')).toBe('열림')
  })

  it('getCodeName returns code itself when not found', () => {
    const store = useCommonCodeStore()
    store.setCodes('STATUS', [{ code: 'OPEN', name: '열림' }])
    expect(store.getCodeName('STATUS', 'UNKNOWN')).toBe('UNKNOWN')
  })

  it('clearCodes resets codeMap', () => {
    const store = useCommonCodeStore()
    store.setCodes('G1', [{ code: 'A', name: 'A' }])
    store.setCodes('G2', [{ code: 'B', name: 'B' }])
    store.clearCodes()
    expect(store.codeMap).toEqual({})
  })

  it('fetchCodes calls API and stores mapped result with nameEn', async () => {
    const store = useCommonCodeStore()
    api.get.mockResolvedValue({
      data: {
        data: [
          { codeVal: 'P1', codeNm: '우선순위1', codeNmEn: 'Priority1' },
          { codeVal: 'P2', codeNm: '우선순위2', codeNmEn: 'Priority2' }
        ]
      }
    })

    await store.fetchCodes('PRIORITY')

    expect(api.get).toHaveBeenCalledWith('/common-codes/PRIORITY')
    expect(store.codeMap['PRIORITY']).toEqual([
      { code: 'P1', name: '우선순위1', nameEn: 'Priority1' },
      { code: 'P2', name: '우선순위2', nameEn: 'Priority2' }
    ])
  })

  it('fetchCodes skips if already cached', async () => {
    const store = useCommonCodeStore()
    store.setCodes('CACHED', [{ code: 'X', name: 'X' }])

    await store.fetchCodes('CACHED')

    expect(api.get).not.toHaveBeenCalled()
  })

  it('fetchCodes handles API error gracefully', async () => {
    const store = useCommonCodeStore()
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    api.get.mockRejectedValue(new Error('Network error'))

    await store.fetchCodes('FAIL_GROUP')

    expect(consoleSpy).toHaveBeenCalled()
    expect(store.codeMap['FAIL_GROUP']).toBeUndefined()
  })
})
