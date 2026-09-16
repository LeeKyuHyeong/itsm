/**
 * 관리자 화면 폼 → 백엔드 요청 DTO 변환.
 *
 * 2026-09-16 전수조사 P2: SLA·알림정책·조직·계정 화면은 목록은 방어적으로 매핑했지만 저장은 화면 필드명을
 * 그대로 보내 백엔드 DTO 와 하나도 맞지 않았다(저장이 항상 400). 여기서 백엔드 *CreateRequest/*UpdateRequest
 * 필드명으로 바꾼다. 필드명이 바뀌면 이 파일과 adminPayload.spec.js 만 고치면 된다.
 */

function toNumberOrUndefined(v) {
  if (v === '' || v === null || v === undefined) return undefined
  const n = Number(v)
  return Number.isFinite(n) ? n : undefined
}

function compact(obj) {
  return Object.fromEntries(Object.entries(obj).filter(([, v]) => v !== undefined))
}

// SlaPolicyCreateRequest { companyId?, priorityCd, deadlineHours, warningPct }
export function toSlaCreatePayload(form) {
  return compact({
    companyId: toNumberOrUndefined(form.companyId),
    priorityCd: form.priorityCd,
    deadlineHours: Number(form.resolutionTimeHours),
    warningPct: Number(form.warningThresholdPercent)
  })
}

// SlaPolicyUpdateRequest { deadlineHours, warningPct }
export function toSlaUpdatePayload(form) {
  return {
    deadlineHours: Number(form.resolutionTimeHours),
    warningPct: Number(form.warningThresholdPercent)
  }
}

// NotificationPolicyCreateRequest { notiTypeCd, triggerCondition, targetRoleCd }
export function toNotificationPolicyCreatePayload(form) {
  return {
    notiTypeCd: form.typeCd,
    triggerCondition: form.conditionExpr,
    targetRoleCd: form.targetRoleCd
  }
}

// NotificationPolicyUpdateRequest { triggerCondition, targetRoleCd }
export function toNotificationPolicyUpdatePayload(form) {
  return {
    triggerCondition: form.conditionExpr,
    targetRoleCd: form.targetRoleCd
  }
}

// IsActiveChangeRequest { isActive: 'Y' | 'N' }
export function toIsActivePayload(active) {
  return { isActive: active ? 'Y' : 'N' }
}

// CompanyCreateRequest / CompanyUpdateRequest { companyNm, bizNo, ceoNm, tel, defaultPmId? }  (address 는 DTO 에 없음)
export function toCompanyPayload(form) {
  return {
    companyNm: form.name,
    ceoNm: form.representative,
    bizNo: form.businessNumber,
    tel: form.phone
  }
}

// DepartmentCreateRequest / DepartmentUpdateRequest { deptNm }  (code/parentId 는 DTO 에 없음)
export function toDepartmentPayload(form) {
  return { deptNm: form.name }
}

// UserCreateRequest { loginId, password, userNm, employeeNo?, deptId?, email, tel }  (companyId 는 부서로 결정됨)
export function toUserCreatePayload(form) {
  return compact({
    loginId: form.loginId,
    password: form.password,
    userNm: form.name,
    email: form.email,
    tel: form.phone,
    deptId: toNumberOrUndefined(form.departmentId)
  })
}

// UserUpdateRequest { userNm, employeeNo?, deptId?, email, tel }
export function toUserUpdatePayload(form) {
  return compact({
    userNm: form.name,
    email: form.email,
    tel: form.phone,
    deptId: toNumberOrUndefined(form.departmentId)
  })
}
