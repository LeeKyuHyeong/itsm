export const ROLES = {
  SUPER_ADMIN: 'ROLE_SUPER_ADMIN',     // 슈퍼관리자
  ITSM_ADMIN: 'ROLE_ITSM_ADMIN',       // ITSM관리자
  PM: 'ROLE_PM',                       // 유지보수팀 - PM
  DEVELOPER: 'ROLE_DEVELOPER',         // 유지보수팀 - 개발자
  SECURITY: 'ROLE_SECURITY',           // 유지보수팀 - 보안담당자
  DBA: 'ROLE_DBA',                     // 유지보수팀 - DB담당자
  NETWORK: 'ROLE_NETWORK',             // 유지보수팀 - 네트워크담당자
  SERVER: 'ROLE_SERVER',               // 유지보수팀 - 서버담당자
  CUSTOMER: 'ROLE_CUSTOMER',           // 고객사 인원
  EXTERNAL: 'ROLE_EXTERNAL',           // 외부사용자
  AUDITOR: 'ROLE_AUDITOR'              // 감사자
}

export const ROLE_LABEL = {
  [ROLES.SUPER_ADMIN]: '슈퍼관리자',
  [ROLES.ITSM_ADMIN]: 'ITSM관리자',
  [ROLES.PM]: 'PM',
  [ROLES.DEVELOPER]: '개발자',
  [ROLES.SECURITY]: '보안담당자',
  [ROLES.DBA]: 'DB담당자',
  [ROLES.NETWORK]: '네트워크담당자',
  [ROLES.SERVER]: '서버담당자',
  [ROLES.CUSTOMER]: '고객사 인원',
  [ROLES.EXTERNAL]: '외부사용자',
  [ROLES.AUDITOR]: '감사자'
}

// 관리자 역할
export const ADMIN_ROLES = [ROLES.SUPER_ADMIN, ROLES.ITSM_ADMIN]

// 유지보수팀 역할
export const MAINTENANCE_ROLES = [
  ROLES.PM, ROLES.DEVELOPER, ROLES.SECURITY,
  ROLES.DBA, ROLES.NETWORK, ROLES.SERVER
]
