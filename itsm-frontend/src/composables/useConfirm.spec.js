import { describe, it, expect, beforeEach } from 'vitest'
import { useConfirm, confirmState } from './useConfirm.js'

describe('useConfirm', () => {
  beforeEach(() => {
    confirmState.show = false
    confirmState.resolve = null
  })

  it('confirm()을 호출하면 모달이 표시된다', () => {
    const { confirm } = useConfirm()
    confirm({ message: '삭제하시겠습니까?' })
    expect(confirmState.show).toBe(true)
    expect(confirmState.message).toBe('삭제하시겠습니까?')
  })

  it('confirm()은 Promise를 반환한다', () => {
    const { confirm } = useConfirm()
    const result = confirm({ message: '테스트' })
    expect(result).toBeInstanceOf(Promise)
  })

  it('handleConfirm()을 호출하면 true로 resolve된다', async () => {
    const { confirm, handleConfirm } = useConfirm()
    const promise = confirm({ message: '테스트' })
    handleConfirm()
    const result = await promise
    expect(result).toBe(true)
    expect(confirmState.show).toBe(false)
  })

  it('handleCancel()을 호출하면 false로 resolve된다', async () => {
    const { confirm, handleCancel } = useConfirm()
    const promise = confirm({ message: '테스트' })
    handleCancel()
    const result = await promise
    expect(result).toBe(false)
    expect(confirmState.show).toBe(false)
  })

  it('title과 type을 설정할 수 있다', () => {
    const { confirm } = useConfirm()
    confirm({ message: '정말?', title: '경고', type: 'danger' })
    expect(confirmState.title).toBe('경고')
    expect(confirmState.type).toBe('danger')
  })
})
