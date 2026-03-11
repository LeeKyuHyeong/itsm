import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCommonCodeStore } from './commonCode.js'

vi.mock('@/api/index.js', () => ({
  default: {
    get: vi.fn()
  }
}))

import api from '@/api/index.js'

describe('commonCode store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.restoreAllMocks()
  })

  it('initial state has empty codeMap', () => {
    const store = useCommonCodeStore()
    expect(store.codeMap).toEqual({})
  })

  it('setCodes stores codes for a group', () => {
    const store = useCommonCodeStore()
    const codes = [
      { code: 'HIGH', name: '높음' },
      { code: 'LOW', name: '낮음' }
    ]
    store.setCodes('PRIORITY', codes)
    expect(store.codeMap['PRIORITY']).toEqual(codes)
  })

  it('getCodes returns stored codes', () => {
    const store = useCommonCodeStore()
    const codes = [{ code: 'A', name: 'Alpha' }]
    store.setCodes('TEST_GROUP', codes)
    expect(store.getCodes('TEST_GROUP')).toEqual(codes)
  })

  it('getCodes returns empty array for unknown group', () => {
    const store = useCommonCodeStore()
    expect(store.getCodes('UNKNOWN')).toEqual([])
  })

  it('getCodeName returns name for matching code', () => {
    const store = useCommonCodeStore()
    store.setCodes('STATUS', [
      { code: 'OPEN', name: '열림' },
      { code: 'CLOSED', name: '닫힘' }
    ])
    expect(store.getCodeName('STATUS', 'OPEN')).toBe('열림')
    expect(store.getCodeName('STATUS', 'CLOSED')).toBe('닫힘')
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

  it('fetchCodes calls API and stores mapped result', async () => {
    const store = useCommonCodeStore()
    api.get.mockResolvedValue({
      data: {
        data: [
          { codeVal: 'P1', codeNm: '우선순위1' },
          { codeVal: 'P2', codeNm: '우선순위2' }
        ]
      }
    })

    await store.fetchCodes('PRIORITY')

    expect(api.get).toHaveBeenCalledWith('/common-codes/PRIORITY')
    expect(store.codeMap['PRIORITY']).toEqual([
      { code: 'P1', name: '우선순위1' },
      { code: 'P2', name: '우선순위2' }
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
