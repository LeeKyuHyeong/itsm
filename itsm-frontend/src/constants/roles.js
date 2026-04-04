export const ROLES = {
  SUPER_ADMIN: 'ROLE_SUPER_ADMIN',
  ITSM_ADMIN: 'ROLE_ITSM_ADMIN',
  PM: 'ROLE_PM',
  DEVELOPER: 'ROLE_DEVELOPER',
  SECURITY: 'ROLE_SECURITY',
  DBA: 'ROLE_DBA',
  NETWORK: 'ROLE_NETWORK',
  SERVER: 'ROLE_SERVER',
  CUSTOMER: 'ROLE_CUSTOMER',
  EXTERNAL: 'ROLE_EXTERNAL',
  AUDITOR: 'ROLE_AUDITOR'
}

/**
 * i18n 기반 역할 라벨 조회.
 * 컴포넌트 내에서 t() 함수를 전달하여 사용:
 *   getRoleLabel(t, ROLES.SUPER_ADMIN) → '슈퍼관리자' (ko) / 'Super Admin' (en)
 */
export function getRoleLabel(t, roleCode) {
  return t(`role.${roleCode}`, roleCode)
}

// 관리자 역할
export const ADMIN_ROLES = [ROLES.SUPER_ADMIN, ROLES.ITSM_ADMIN]

// 유지보수팀 역할
export const MAINTENANCE_ROLES = [
  ROLES.PM, ROLES.DEVELOPER, ROLES.SECURITY,
  ROLES.DBA, ROLES.NETWORK, ROLES.SERVER
]
