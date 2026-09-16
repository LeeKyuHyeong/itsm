# Regression 목록 — 반드시 다시 확인할 시나리오

| ID | 도메인 | 시나리오 | 검증 방법 | 출처 | 등록일 |
|---|---|---|---|---|---|
| R-001 | Auth | JWT roles 가 `ROLE_*` 형태여도 authority 에 `ROLE_` 가 한 번만 붙어 `hasRole()` 이 통과한다 | `JwtAuthFilterTest#doesNotDoublePrefixRoleCode` + 운영 admin 계정 생성 1회 | records/2026-09-17_role-authority-double-prefix.md | 2026-09-17 |
