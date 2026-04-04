import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useToast, toastState } from './useToast.js'

describe('useToast', () => {
  beforeEach(() => {
    toastState.toasts = []
  })

  it('success 토스트를 추가한다', () => {
    const { success } = useToast()
    success('저장 완료')
    expect(toastState.toasts).toHaveLength(1)
    expect(toastState.toasts[0].type).toBe('success')
    expect(toastState.toasts[0].message).toBe('저장 완료')
  })

  it('error 토스트를 추가한다', () => {
    const { error } = useToast()
    error('저장 실패')
    expect(toastState.toasts).toHaveLength(1)
    expect(toastState.toasts[0].type).toBe('error')
  })

  it('warning 토스트를 추가한다', () => {
    const { warning } = useToast()
    warning('주의')
    expect(toastState.toasts[0].type).toBe('warning')
  })

  it('remove로 토스트를 제거한다', () => {
    const { success, remove } = useToast()
    success('테스트')
    const id = toastState.toasts[0].id
    remove(id)
    expect(toastState.toasts).toHaveLength(0)
  })

  it('자동 삭제 타이머가 설정된다', () => {
    vi.useFakeTimers()
    const { success } = useToast()
    success('테스트')
    expect(toastState.toasts).toHaveLength(1)
    vi.advanceTimersByTime(3100)
    expect(toastState.toasts).toHaveLength(0)
    vi.useRealTimers()
  })
})
