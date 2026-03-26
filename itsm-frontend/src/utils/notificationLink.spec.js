import { describe, it, expect } from 'vitest'
import { buildSafeRefLink } from './notificationLink.js'

describe('buildSafeRefLink', () => {
  it('INCIDENT 타입으로 올바른 경로를 생성한다', () => {
    expect(buildSafeRefLink('INCIDENT', 123)).toBe('/incidents/123')
  })

  it('SERVICE_REQUEST 타입으로 올바른 경로를 생성한다', () => {
    expect(buildSafeRefLink('SERVICE_REQUEST', 456)).toBe('/service-requests/456')
  })

  it('CHANGE 타입으로 올바른 경로를 생성한다', () => {
    expect(buildSafeRefLink('CHANGE', 789)).toBe('/changes/789')
  })

  it('INSPECTION 타입으로 올바른 경로를 생성한다', () => {
    expect(buildSafeRefLink('INSPECTION', 10)).toBe('/inspections/10')
  })

  it('ASSET_HW 타입으로 올바른 경로를 생성한다', () => {
    expect(buildSafeRefLink('ASSET_HW', 20)).toBe('/assets/hw/20')
  })

  it('ASSET_SW 타입으로 올바른 경로를 생성한다', () => {
    expect(buildSafeRefLink('ASSET_SW', 30)).toBe('/assets/sw/30')
  })

  it('ASSET_OA 타입으로 올바른 경로를 생성한다', () => {
    expect(buildSafeRefLink('ASSET_OA', 40)).toBe('/assets/oa/40')
  })

  it('허용되지 않은 refType은 null을 반환한다', () => {
    expect(buildSafeRefLink('MALICIOUS_TYPE', 1)).toBeNull()
  })

  it('refType이 null이면 null을 반환한다', () => {
    expect(buildSafeRefLink(null, 1)).toBeNull()
  })

  it('refType이 undefined이면 null을 반환한다', () => {
    expect(buildSafeRefLink(undefined, 1)).toBeNull()
  })

  it('refId가 null이면 null을 반환한다', () => {
    expect(buildSafeRefLink('INCIDENT', null)).toBeNull()
  })

  it('refId가 숫자가 아닌 문자열이면 null을 반환한다', () => {
    expect(buildSafeRefLink('INCIDENT', 'abc')).toBeNull()
  })

  it('refId가 음수이면 null을 반환한다', () => {
    expect(buildSafeRefLink('INCIDENT', -1)).toBeNull()
  })

  it('SIMULATION 타입은 네비게이션 대상이 없으므로 null을 반환한다', () => {
    expect(buildSafeRefLink('SIMULATION', 1)).toBeNull()
  })

  it('외부 URL이 포함된 refType은 차단된다', () => {
    expect(buildSafeRefLink('http://evil.com', 1)).toBeNull()
  })

  it('refId에 경로 조작 문자열이 포함되면 null을 반환한다', () => {
    expect(buildSafeRefLink('INCIDENT', '123/../../admin')).toBeNull()
  })
})
