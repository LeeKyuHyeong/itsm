import { describe, it, expect } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

import authRoutes from './routes/auth.js'
import dashboardRoutes from './routes/dashboard.js'
import incidentRoutes from './routes/incident.js'
import serviceRequestRoutes from './routes/servicerequest.js'
import changeRoutes from './routes/change.js'
import assetRoutes from './routes/asset.js'
import inspectionRoutes from './routes/inspection.js'
import boardRoutes from './routes/board.js'
import reportRoutes from './routes/report.js'
import adminRoutes from './routes/admin.js'

/**
 * 컨테이너 nginx(nginx.conf)의 스캐너 차단 규칙과 Vue 라우트가 겹치면
 * 해당 화면은 새로고침·딥링크 시 444 로 끊긴다 (클라이언트 내 이동만 동작).
 * 2026-09-16 전수조사 P7: `admin` 이 목록에 있어 /admin/* 관리자 화면 8개가 운영에서 끊기던 것을 발견.
 */

const allRoutes = [
  ...authRoutes,
  ...dashboardRoutes,
  ...incidentRoutes,
  ...serviceRequestRoutes,
  ...changeRoutes,
  ...assetRoutes,
  ...inspectionRoutes,
  ...boardRoutes,
  ...reportRoutes,
  ...adminRoutes
]

/** `location ~* ^/(a|b|c) {` 형태에서 차단 첫 세그먼트 목록을 뽑는다 */
export function parseBlockedPrefixes(nginxConf) {
  const m = nginxConf.match(/location\s+~\*\s+\^\/\(([^)]+)\)/)
  if (!m) return []
  return m[1].split('|').map((s) => s.replace(/\\/g, '').toLowerCase())
}

/** `location ~* \.(php|sql|...)$ {` 형태에서 차단 확장자 목록을 뽑는다 */
export function parseBlockedExtensions(nginxConf) {
  const m = nginxConf.match(/location\s+~\*\s+\\\.\(([^)]+)\)\$/)
  if (!m) return []
  return m[1].split('|').map((s) => s.toLowerCase())
}

/** 라우트 path 중 nginx 규칙에 걸리는 것을 돌려준다 */
export function findConflicts(routePaths, blockedPrefixes, blockedExtensions) {
  return routePaths.filter((path) => {
    const firstSegment = path.split('/').filter(Boolean)[0]?.toLowerCase()
    if (firstSegment && blockedPrefixes.includes(firstSegment)) return true
    const ext = path.split('.').pop().toLowerCase()
    return path.includes('.') && blockedExtensions.includes(ext)
  })
}

describe('nginx 스캐너 차단 규칙 ↔ Vue 라우트 충돌', () => {
  const nginxConf = readFileSync(resolve(__dirname, '..', '..', 'nginx.conf'), 'utf-8')
  const routePaths = allRoutes.map((r) => r.path)

  it('차단 규칙 파서가 실제 nginx.conf 에서 목록을 읽어낸다 (빈 목록이면 파서가 깨진 것)', () => {
    expect(parseBlockedPrefixes(nginxConf).length).toBeGreaterThan(10)
    expect(parseBlockedExtensions(nginxConf)).toContain('php')
  })

  it('검출기 자체 검증: 구버전 규칙(admin 포함)을 넣으면 /admin/* 라우트를 잡아낸다', () => {
    const legacy = 'location ~* ^/(admin|administrator|wp-admin) {'
    const conflicts = findConflicts(routePaths, parseBlockedPrefixes(legacy), [])
    expect(conflicts).toContain('/admin/menus')
    expect(conflicts.length).toBe(adminRoutes.length)
  })

  it('현재 nginx.conf 와 충돌하는 Vue 라우트가 없다', () => {
    const conflicts = findConflicts(
      routePaths,
      parseBlockedPrefixes(nginxConf),
      parseBlockedExtensions(nginxConf)
    )
    expect(conflicts).toEqual([])
  })
})
