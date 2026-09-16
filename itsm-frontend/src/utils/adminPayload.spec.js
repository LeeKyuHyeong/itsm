import { describe, it, expect } from 'vitest'
import {
  toSlaCreatePayload,
  toSlaUpdatePayload,
  toNotificationPolicyCreatePayload,
  toNotificationPolicyUpdatePayload,
  toIsActivePayload,
  toCompanyPayload,
  toDepartmentPayload,
  toUserCreatePayload,
  toUserUpdatePayload
} from './adminPayload.js'

/**
 * 2026-09-16 전수조사 P2 — 관리자 화면 4개(SLA·알림정책·조직·계정)는 목록은 방어적으로 매핑했지만
 * 저장 페이로드는 화면 필드명(resolutionTimeHours, typeCd, name, phone …)을 그대로 보내
 * 백엔드 DTO(deadlineHours, notiTypeCd, companyNm, tel …)와 하나도 맞지 않았다 → 저장이 항상 400.
 * 이 매퍼가 백엔드 *CreateRequest/*UpdateRequest 필드명으로 바꾼다.
 */
describe('adminPayload', () => {
  it('SLA 생성: deadlineHours/warningPct 로 보내고 companyId 가 비면 뺀다 (전사 기본 정책)', () => {
    expect(
      toSlaCreatePayload({
        companyId: '',
        priorityCd: 'HIGH',
        resolutionTimeHours: 8,
        warningThresholdPercent: 80
      })
    ).toEqual({ priorityCd: 'HIGH', deadlineHours: 8, warningPct: 80 })
    expect(
      toSlaCreatePayload({
        companyId: 3,
        priorityCd: 'LOW',
        resolutionTimeHours: 72,
        warningThresholdPercent: 90
      })
    ).toEqual({ companyId: 3, priorityCd: 'LOW', deadlineHours: 72, warningPct: 90 })
  })

  it('SLA 수정: 백엔드 SlaPolicyUpdateRequest 는 deadlineHours/warningPct 만 받는다', () => {
    expect(
      toSlaUpdatePayload({
        companyId: 3,
        priorityCd: 'LOW',
        resolutionTimeHours: 48,
        warningThresholdPercent: 70
      })
    ).toEqual({ deadlineHours: 48, warningPct: 70 })
  })

  it('알림 정책 생성/수정: notiTypeCd/triggerCondition/targetRoleCd', () => {
    expect(
      toNotificationPolicyCreatePayload({
        typeCd: 'SLA_WARNING',
        conditionExpr: 'elapsed>=80',
        targetRoleCd: 'PM'
      })
    ).toEqual({ notiTypeCd: 'SLA_WARNING', triggerCondition: 'elapsed>=80', targetRoleCd: 'PM' })
    expect(
      toNotificationPolicyUpdatePayload({
        typeCd: 'SLA_WARNING',
        conditionExpr: 'x',
        targetRoleCd: 'DBA'
      })
    ).toEqual({ triggerCondition: 'x', targetRoleCd: 'DBA' })
  })

  it('활성 여부: boolean → IsActiveChangeRequest { isActive: Y/N }', () => {
    expect(toIsActivePayload(true)).toEqual({ isActive: 'Y' })
    expect(toIsActivePayload(false)).toEqual({ isActive: 'N' })
  })

  it('회사: name/representative/businessNumber/phone → companyNm/ceoNm/bizNo/tel (address 는 DTO 에 없어 제외)', () => {
    expect(
      toCompanyPayload({
        name: 'ACME',
        representative: '홍길동',
        businessNumber: '123-45-67890',
        phone: '02-1234',
        address: '서울'
      })
    ).toEqual({ companyNm: 'ACME', ceoNm: '홍길동', bizNo: '123-45-67890', tel: '02-1234' })
  })

  it('부서: name → deptNm (code/parentId 는 DTO 에 없어 제외)', () => {
    expect(toDepartmentPayload({ name: 'IT팀', code: 'IT', parentId: 3 })).toEqual({
      deptNm: 'IT팀'
    })
  })

  it('사용자 생성: name/phone/departmentId → userNm/tel/deptId, 빈 부서는 제외, companyId 는 DTO 에 없음', () => {
    expect(
      toUserCreatePayload({
        loginId: 'hong',
        password: 'Password1!',
        name: '홍길동',
        email: 'h@x.com',
        phone: '010',
        companyId: 1,
        departmentId: 7
      })
    ).toEqual({
      loginId: 'hong',
      password: 'Password1!',
      userNm: '홍길동',
      email: 'h@x.com',
      tel: '010',
      deptId: 7
    })
    expect(
      toUserCreatePayload({
        loginId: 'a',
        password: 'p',
        name: 'n',
        email: '',
        phone: '',
        companyId: '',
        departmentId: ''
      })
    ).toEqual({ loginId: 'a', password: 'p', userNm: 'n', email: '', tel: '' })
  })

  it('사용자 수정: loginId/password 없이 userNm/tel/deptId/email', () => {
    expect(
      toUserUpdatePayload({
        loginId: 'hong',
        password: 'x',
        name: '홍길동2',
        email: 'h@x.com',
        phone: '011',
        departmentId: '8'
      })
    ).toEqual({ userNm: '홍길동2', email: 'h@x.com', tel: '011', deptId: 8 })
  })
})
