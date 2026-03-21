# ITSM 개발 TODO

> Vue.js (프론트엔드) + Spring Boot (백엔드) REST API 구조

---

## 완료된 Phase

- **Phase 11**: UI 테마 (라이트/다크 모드) ✅
- **Phase 12**: 자산관리 재구조화 ✅
- **Phase 13**: 데모/시뮬레이션 배치 ✅
- **Phase 14**: 전체 i18n (ko/en) 대응 ✅
- **Phase 15**: CI/CD + 운영 배포 ✅
- **Phase 16**: 운영 이슈 & OA 자산 분리 ✅ (운영 DB 반영 미완)
- **Phase 17**: 소스 위험도 분석 및 품질 개선 ✅

---

## Phase 16 잔여 (운영 환경)

- [ ] 운영 로그인 무반응 조사 (CORS / Cookie Secure / 브라우저 Network 탭 확인 필요)
- [ ] SSL "주의 요함" 경고 확인 필요 (Mixed Content 또는 브라우저 캐시 문제 추정)
- [ ] 운영 DB에 tb_asset_oa, tb_asset_oa_history 테이블 생성 (ddl-auto: update로 자동생성 예상)
- [ ] 운영 DB에 OA 메뉴, OA 공통코드 시드데이터 INSERT

---

## Phase 18: 보안 강화 2차

> 2026-03-18 전체 보안 정밀 분석 결과. OWASP Top 10 기준 위험도 순으로 정리.

### 1단계: HTTP 보안 헤더 (CRITICAL) — 현재 전무

> 백엔드·프론트엔드 모두 보안 헤더가 설정되어 있지 않음. 클릭재킹, MIME 스니핑, XSS 반사 공격에 노출.

- [x] nginx.conf: `Content-Security-Policy` 헤더 추가 (`default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self'`)
- [x] nginx.conf: `X-Frame-Options: SAMEORIGIN` 헤더 추가 (클릭재킹 방지)
- [x] nginx.conf: `X-Content-Type-Options: nosniff` 헤더 추가 (MIME 스니핑 방지)
- [x] nginx.conf: `Strict-Transport-Security` 헤더 추가 (HSTS, HTTPS 강제)
- [x] nginx.conf: `Referrer-Policy: strict-origin-when-cross-origin` 헤더 추가
- [x] Spring SecurityConfig: `.headers()` 체인으로 서버 측 보안 헤더도 중복 설정 (프록시 우회 대비)

### 2단계: Access Token 저장소 전환 (CRITICAL)

> 현재 Access Token을 `localStorage`에 저장. XSS 취약점 발생 시 토큰 탈취 가능. Refresh Token은 이미 httpOnly 쿠키로 안전.

- [x] Backend: Access Token도 httpOnly 쿠키로 발급하도록 AuthController 수정
- [x] Backend: SecurityConfig/JwtAuthFilter에서 쿠키 기반 토큰 추출 로직 추가
- [x] Frontend: `localStorage.getItem('accessToken')` 제거 (api/index.js, stores/auth.js, guards.js)
- [x] Frontend: Axios interceptor에서 Authorization 헤더 수동 설정 제거 (쿠키 자동 전송)
- [x] 테스트: 로그인 → API 호출 → 토큰 갱신 → 로그아웃 전체 플로우 검증

### 3단계: CORS 설정 강화 (HIGH)

> `allowedHeaders("*")`로 모든 헤더 허용 중. 불필요한 커스텀 헤더를 통한 공격 벡터 차단 필요.

- [x] SecurityConfig: `allowedHeaders(List.of("*"))` → 명시적 목록으로 변경 (`Content-Type, Accept, X-Requested-With`)
- [x] WebConfig: `.allowedHeaders("*")` → 동일하게 명시적 목록으로 변경
- [x] `exposedHeaders` 설정 추가 (`Content-Disposition` — 파일 다운로드 시 프론트엔드에서 접근 필요)

### 4단계: 메서드 레벨 권한 검증 (HIGH)

> 현재 Interceptor 기반 URL 매칭만 존재. 서비스 메서드에 `@PreAuthorize` 없어 우회 가능성 존재.

- [ ] SecurityConfig: `@EnableMethodSecurity` 활성화
- [ ] 관리자 전용 Service 메서드에 `@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ITSM_ADMIN')")` 추가 (UserService, CommonCodeService, SlaPolicyService, NotificationPolicyService, BatchJobService)
- [ ] 데이터 소유자 검증이 필요한 메서드에 커스텀 권한 체크 추가 (본인 데이터만 수정/삭제)

### 5단계: Open Redirect 방지 (MEDIUM)

> NotificationDropdown.vue에서 `noti.refLink`를 검증 없이 `router.push()`. 악의적 링크 주입 시 리다이렉트 가능.

- [ ] NotificationDropdown.vue: `refLink`를 허용된 내부 경로 화이트리스트로 검증 후 이동
- [ ] Backend: 알림 생성 시 `refLink` 값이 내부 경로(`/incidents/`, `/boards/` 등)인지 서버 측 검증

### 6단계: AuthInterceptor 성능 및 보안 (MEDIUM)

> `menuRepository.findAll()`을 매 요청마다 호출. 성능 이슈 + 메뉴 데이터 변조 시 실시간 반영 위험.

- [ ] AuthInterceptor: 메뉴 목록 캐싱 (Spring `@Cacheable` 또는 인메모리 캐시, TTL 5분)
- [ ] X-Forwarded-For 헤더 파싱 강화: 신뢰할 수 있는 프록시 IP 검증 로직 추가 (IP 스푸핑 방지)

### 7단계: 프로덕션 로깅 정리 (MEDIUM)

> 프론트엔드에 `console.error()` 79건. 프로덕션에서 내부 정보 노출 가능.

- [ ] Vite 빌드 설정: 프로덕션 빌드 시 `console.log/warn/error` 자동 제거 (`esbuild.drop: ['console']`)
- [ ] 또는 환경별 로거 유틸 도입 (개발에서만 출력)

### 8단계: 의존성 보안 감사 (LOW)

> Axios 1.7.0 등 일부 패키지 구버전. 보안 패치 누락 가능.

- [ ] `npm audit` 실행 및 취약점 수정
- [ ] Axios 최신 버전 업데이트
- [ ] Backend: `./gradlew dependencyCheckAnalyze` (OWASP Dependency-Check 플러그인 추가 고려)
- [ ] CI/CD에 `npm audit --audit-level=high` 단계 추가 (빌드 시 자동 감사)

### 9단계: 추가 보안 강화 (LOW)

- [ ] BCrypt 라운드 수 10 → 12로 상향 (SecurityConfig passwordEncoder)
- [ ] 로그인 API에 분산 환경 Rate Limiting 추가 (Redis 기반 또는 Spring Cloud Gateway)
- [ ] JWT 토큰 블랙리스트 구현 (로그아웃 시 토큰 즉시 무효화, 현재는 만료까지 유효)
- [ ] 비밀번호 최대 길이 제한 추가 (BCrypt는 72바이트 초과 시 잘림 — 128자 제한 권장)

---

## 설계 원칙 (개발 시 항상 참고)

1. **서비스 중단 최소화** — 동적 폼, DB 기반 설정, 메뉴 동적 관리
2. **추적 가능성** — 모든 변경 이력 자동 적재, 감사 로그
3. **권한 최소화** — RBAC, 이중 방어 (프론트 가드 + 백엔드 Interceptor)
4. **확장 가능한 구조** — 공통코드, 게시판 빌더, JSON 스키마 동적 폼
5. **물리적 삭제 금지** — status / is_active 로 비활성 처리
6. **낙관적 락** — 동시 수정 충돌 방지
