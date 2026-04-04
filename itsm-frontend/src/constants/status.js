// 장애관리 상태
export const INCIDENT_STATUS = {
  REGISTERED: 'REGISTERED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  CLOSED: 'CLOSED',
  REJECTED: 'REJECTED'
}

// 장애관리 상태 전이 (현재 상태 → 가능한 다음 상태들)
export const INCIDENT_STATUS_TRANSITIONS = {
  [INCIDENT_STATUS.REGISTERED]: [INCIDENT_STATUS.IN_PROGRESS, INCIDENT_STATUS.REJECTED],
  [INCIDENT_STATUS.IN_PROGRESS]: [INCIDENT_STATUS.COMPLETED],
  [INCIDENT_STATUS.COMPLETED]: [INCIDENT_STATUS.CLOSED],
  [INCIDENT_STATUS.REJECTED]: [INCIDENT_STATUS.REGISTERED],
  [INCIDENT_STATUS.CLOSED]: []
}

// 서비스요청 상태
export const SR_STATUS = {
  REGISTERED: 'REGISTERED',
  ASSIGNED: 'ASSIGNED',
  IN_PROGRESS: 'IN_PROGRESS',
  PENDING_COMPLETE: 'PENDING_COMPLETE',
  CLOSED: 'CLOSED',
  CANCELLED: 'CANCELLED',
  REJECTED: 'REJECTED'
}

// 변경관리 상태
export const CHANGE_STATUS = {
  DRAFT: 'DRAFT',
  APPROVAL_REQUESTED: 'APPROVAL_REQUESTED',
  APPROVED: 'APPROVED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  CLOSED: 'CLOSED',
  REJECTED: 'REJECTED'
}

// 정기점검 상태
export const INSPECTION_STATUS = {
  SCHEDULED: 'SCHEDULED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  CLOSED: 'CLOSED',
  ON_HOLD: 'ON_HOLD'
}

// 우선순위
export const PRIORITY = {
  CRITICAL: 'CRITICAL',
  HIGH: 'HIGH',
  MEDIUM: 'MEDIUM',
  LOW: 'LOW'
}

// 사용자 상태
export const USER_STATUS = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  LOCKED: 'LOCKED',
  DELETED: 'DELETED'
}

/**
 * i18n 기반 상태 라벨 조회.
 * 컴포넌트 내에서 t() 함수를 전달하여 사용:
 *   getStatusLabel(t, 'RECEIVED') → '접수' (ko) / 'Received' (en)
 */
export function getStatusLabel(t, statusCode) {
  return t(`status.${statusCode}`, statusCode)
}

/**
 * i18n 기반 우선순위 라벨 조회.
 */
export function getPriorityLabel(t, priorityCode) {
  return t(`priority.${priorityCode}`, priorityCode)
}

/**
 * i18n 기반 사용자 상태 라벨 조회.
 */
export function getUserStatusLabel(t, statusCode) {
  return t(`status.${statusCode}`, statusCode)
}
