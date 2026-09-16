# 권한 문자열 이중 접두사(ROLE_ROLE_*)로 관리자 전용 API 전부 403
- 일자: 2026-09-17
- 유형: 버그
- 우선순위: P0 (인증·인가)
- 판정: 조건부 (운영 재현 확인은 🙋)

## 1. 요청과 목적
- 사용자가 원한 것: 운영에서 슈퍼관리자(admin)로 사용자 관리 > 계정 생성 시 `Access denied` 가 나는 원인 확인·수정 (시연 계정 `demo` 생성 목적)
- 개발자 확인 결과(결정 사항): 원인 확인 후 수정 진행 ("itsm 오류부터 확인하자")
- 진행 중 둔 가정: 운영 `tb_role.role_cd` 는 시드와 같은 `ROLE_*` 형태다(Phase 0 시드 기준, 운영 DB 직접 조회는 안 함)

## 2. Acceptance Criteria
| # | 구분 | 조건 | 상태 | 근거 |
|---|---|---|---|---|
| 1 | 정상 | JWT roles 클레임이 `ROLE_SUPER_ADMIN` 이어도 SecurityContext authority 는 정확히 `ROLE_SUPER_ADMIN` 이다 | ✅ | `JwtAuthFilterTest#doesNotDoublePrefixRoleCode` |
| 2 | 정상 | 접두사 없는 역할 문자열(`PM`)은 기존처럼 `ROLE_PM` 이 된다 | ✅ | 같은 테스트, 기존 4개 테스트 유지 |
| 3 | 정상 | `@PreAuthorize(HAS_ADMIN_ROLE)` 이 걸린 `UserService.createUser` 를 슈퍼관리자가 호출하면 통과한다 | ✅ (단위) / 🙋 (운영) | `MethodSecurityTest` 는 `@WithMockUser` 라 이 버그를 못 잡았음 → 운영에서 admin 으로 계정 생성 1회 |
| 4 | 권한 | 감사자(ROLE_AUDITOR) 계정은 메뉴 읽기만 되고 POST/PUT/DELETE 는 거부된다 | 🙋 | demo 계정으로 장애 등록 시도 → 403 |
| 5 | 연쇄 | 전체 회귀에 영향 없음 | ✅ | `./gradlew test` 774 passed |

## 3. 변경 사항
- `itsm-core/.../constant/RoleCode.java` — `toAuthority(roleCd)`: 접두사가 없을 때만 `ROLE_` 를 붙인다
- `itsm-api/.../security/JwtAuthFilter.java` — `"ROLE_" + role` → `RoleCode.toAuthority(role)`
- `itsm-api/.../security/CustomUserDetailsService.java` — 동일
- `itsm-api/src/test/.../security/JwtAuthFilterTest.java` — 재현 테스트 추가
- DB·설정 변경: 없음

## 4. 영향 범위 분석
- 검색한 호출처: `"ROLE_" +` 2곳(위 두 파일) 전부 교체. `hasRole` 사용처: `RoleCode.HAS_ADMIN_ROLE` → `UserService` 5곳, `SystemConfigService` 2곳, 게시판 설정 6곳 — 전부 운영에서 항상 403 이었음(2026-04-04 Phase 21 이후)
- `AuthInterceptor` 의 `isSuperAdmin` 단축 판정(`"ROLE_SUPER_ADMIN"::equals)`) — 지금까지 한 번도 참이 아니었고, 수정 후 참이 된다(슈퍼관리자는 메뉴 검사 생략 → 기존에도 전 메뉴 Y 라 결과 동일)
- `MenuAccessInterceptor` — authority 에서 `ROLE_` 를 한 번 떼어 `role_cd` 로 저장. 수정 전엔 이중 접두사 덕에 우연히 `ROLE_PM` 이 저장됐고, 수정 후엔 `PM` 이 저장된다. DDL 주석(`role_cd` = `ROLE_ADMIN` 형태)과 어긋나지만 기존 테스트 `MenuAccessInterceptorTest` 가 `PM` 을 기대하므로 **이번 수정에서 손대지 않음** → open-issues O-001

## 5. 실행한 검증
| 계층 | 명령/방법 | 결과 | 상태 |
|---|---|---|---|
| Unit(재현) | `./gradlew :itsm-api:test --tests "com.itsm.api.security.JwtAuthFilterTest"` (수정 전) | 5 tests, 1 failed | ✅ Red 확인 |
| Unit | 같은 명령 (수정 후) | 5 passed | ✅ |
| 전체 회귀 | `./gradlew test --no-daemon` | 774 tests, 0 failures, 0 errors | ✅ |
| 사용자 시나리오 | 아래 6번 | 배포 후 확인 대기 | 🙋 |
| 배포 | `main` push → deploy.yml (헬스체크 200 게이트) | | 🙋 (배포 결과 기록 예정) |

## 6. 수동 확인 시나리오
1. [전제] 배포 완료 후 admin 으로 로그인
2. [행동] 사용자 관리 > 추가: 아이디 `demo`, 역할 감사자
3. [기대 결과] 생성 성공, 목록에 표시. itsm-api 로그에 `Access denied` 없음
4. [행동] demo 로 로그인 → 장애관리 목록 조회 → 등록 시도
5. [기대 결과] 조회 OK, 등록은 403(권한 없음)
- 결과: ☐ 통과 ☐ 실패

## 7. checklist 점검
- 점검함: 영향 범위(호출처 전부 검색), 권한(비허용 사용자 차단 유지 — AUDITOR 시나리오로 확인 예정), 테스트가 검증 대상을 Mock 으로 가리지 않았는지(MethodSecurityTest 의 @WithMockUser 가 그 사례였음 → 필터 단위 테스트로 보강)
- 해당 없음: 입력·경계값, 트랜잭션, 멀티플레이

## 8. 발견된 문제와 조치
- 문제: `hasRole` 기반 관리자 전용 API 가 운영에서 항상 403 / 원인: role_cd 가 이미 `ROLE_` 를 포함하는데 authority 생성 시 한 번 더 붙임 / 조치: 접두사 없을 때만 붙이는 헬퍼로 통일 / 추가한 테스트: `JwtAuthFilterTest#doesNotDoublePrefixRoleCode`

## 9. 미검증 영역과 남은 위험
- 운영 DB 의 `role_cd` 실제 값은 조회하지 않음(시드와 같다고 가정). 다르면 시나리오 6-3 에서 드러난다
- `MenuAccessInterceptor` 의 저장값 변화(`ROLE_PM` → `PM`) — O-001

## 10. Regression 등록
- regression-list.md 추가 ID: R-001
