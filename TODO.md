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
- **Phase 18**: 보안 강화 2차 (OWASP Top 10 기반 전 항목) ✅

---

## Phase 16 잔여 (운영 환경)

- [ ] 운영 로그인 무반응 조사 (CORS / Cookie Secure / 브라우저 Network 탭 확인 필요)
- [ ] SSL "주의 요함" 경고 확인 필요 (Mixed Content 또는 브라우저 캐시 문제 추정)
- [ ] 운영 DB에 tb_asset_oa, tb_asset_oa_history 테이블 생성 (ddl-auto: update로 자동생성 예상)
- [ ] 운영 DB에 OA 메뉴, OA 공통코드 시드데이터 INSERT

---

## Phase 19: 인프라 보안 & Docker 강화 (CRITICAL)

> 2026-04-03 소스 재분석 결과. 운영 환경 직접 영향 항목 우선.

### 1단계: 설정 보안 (CRITICAL)

- [ ] application.yml: JWT 기본 시크릿 제거 — 현재 base64 인코딩된 기본값이 소스에 노출 (`${JWT_SECRET}` 환경변수 필수화)
- [ ] application.yml: 기본 CORS 설정에서 `localhost:5173` 제거 (개발 프로필로 분리)
- [ ] application-local.yml: 기본 DB 비밀번호 `1234` 제거 (`${DB_PASSWORD}` 환경변수 필수화)

### 2단계: Docker 이미지 버전 고정 (HIGH)

- [ ] docker-compose.yml: `mariadb:11` → 특정 버전 고정 (예: `mariadb:11.5.2`)
- [ ] docker-compose.yml: 커스텀 이미지 `latest` 태그 → Git SHA 또는 시맨틱 버전 태그로 변경
- [ ] itsm-backend/Dockerfile: `eclipse-temurin:17-jre-alpine` → 특정 버전 고정
- [ ] CI/CD: 이미지 빌드 시 `${{ github.sha }}` 태그 적용

### 3단계: 컨테이너 보안 강화 (HIGH)

- [ ] itsm-backend/Dockerfile: `USER` 디렉티브 추가 (non-root 실행)
- [ ] itsm-backend/Dockerfile: JVM 메모리 제한 설정 (`-Xmx512m -Xms256m`)
- [ ] docker-compose.yml: 서비스별 리소스 제한 추가 (`deploy.limits: cpus, memory`)
- [ ] docker-compose.yml: 커스텀 네트워크 정의 (서비스 간 격리)

### 4단계: CI/CD 파이프라인 강화 (MEDIUM)

- [ ] 배포 후 헬스체크 검증 단계 추가 (`docker compose ps`, `/actuator/health`)
- [ ] 배포 실패 시 자동 롤백 메커니즘 구현
- [ ] 배포 전 DB 백업 단계 추가
- [ ] 컨테이너 이미지 스캐닝 추가 (Trivy)
- [ ] appleboy/scp-action 버전 업데이트 (v0.1.7 → 최신)
- [ ] deploy.yml: 하드코딩된 이메일/도메인 → secrets로 분리

### 5단계: Nginx SSL 강화 (MEDIUM)

- [ ] nginx-itsm.conf: SSL 암호화 스위트 강화 (ECDHE-ECDSA/RSA-AES-GCM 명시)
- [ ] nginx-itsm.conf: `ssl_prefer_server_ciphers on` 추가
- [ ] nginx-itsm.conf: `ssl_session_cache`, `ssl_session_timeout` 추가
- [ ] nginx.conf: 프로덕션에서 Swagger UI 접근 차단

---

## Phase 20: 백엔드 성능 최적화 (HIGH)

> N+1 쿼리, 누락된 인덱스, 비효율적 연산 개선.

### 1단계: 데이터베이스 인덱스 추가 (HIGH)

- [ ] User 엔티티: `login_id`, `status`, `dept_id` 인덱스 추가 (`@Table(indexes = ...)`)
- [ ] Incident 엔티티: `status_cd`, `sla_deadline_at`, `created_at` 인덱스 추가
- [ ] ServiceRequest 엔티티: `status_cd`, `created_at` 인덱스 추가
- [ ] Asset 엔티티: 검색 대상 컬럼 인덱스 추가

### 2단계: DashboardService 쿼리 최적화 (HIGH)

- [ ] `findByStatusCd` 다건 호출 → `findByStatusCdIn` 단일 쿼리로 변경
- [ ] SLA 초과 건수를 Java 스트림 필터 → DB 집계 쿼리(`COUNT WHERE`)로 전환
- [ ] 대시보드 통계 전용 Repository 메서드 작성 (집계 쿼리)

### 3단계: JPA 페치 전략 정리 (MEDIUM)

- [ ] DTO 변환 시 접근하는 연관 엔티티에 대해 JOIN FETCH 일괄 정리
- [ ] `saveAll()` 배치 저장으로 전환 (IncidentAsset 등 반복 save 제거)
- [ ] 댓글, 이력 등 하위 목록 API에 페이징 적용 (현재 전체 조회)

---

## Phase 21: 백엔드 코드 품질 개선 (HIGH)

> 중복 코드 제거, 상수 관리, 일관된 에러 처리.

### 1단계: 중복 코드 통합 (HIGH)

- [ ] `getCurrentUserId()` — 7개 Controller에 중복 → 공통 유틸 또는 ArgumentResolver로 추출
- [ ] `extractAccessTokenFromCookie()` — AuthController, JwtAuthFilter 중복 → `CookieUtils` 유틸 추출
- [ ] CORS origin 파싱 시 `trim()` 누락 수정 (SecurityConfig)

### 2단계: 매직 스트링 상수화 (MEDIUM)

- [ ] 사용자 상태(`ACTIVE`, `LOCKED`, `DELETED`) → 상수 클래스 또는 enum 추출
- [ ] 인시던트 상태(`RECEIVED`, `IN_PROGRESS`, `COMPLETED` 등) → 상수화
- [ ] 역할 코드(`SUPER_ADMIN`, `ITSM_ADMIN` 등) → 상수화

### 3단계: 에러 처리 표준화 (MEDIUM)

- [ ] AuditLogAspect: silent catch → 최소 `log.warn` 추가
- [ ] 인증 null 체크 일관성 확보 (일부 Controller만 검증 중)
- [ ] 입력 검증 강화: 자산 IP/MAC 주소 `@Pattern` 검증, 텍스트 필드 `@Size(max)` 추가

---

## Phase 22: 프론트엔드 성능 & UX 개선 (HIGH)

> 번들 최적화, 로딩/에러 상태, 접근성 개선.

### 1단계: 라우트 지연 로딩 (HIGH)

- [ ] router/index.js: 모든 뷰 컴포넌트를 `() => import()` 동적 임포트로 전환 (초기 번들 30~40% 감소 예상)

### 2단계: UX 개선 (HIGH)

- [ ] `alert()` / `confirm()` 20건 이상 → `BaseConfirm.vue` 컴포넌트로 교체 (이미 존재하나 미사용)
- [ ] 모달/폼 제출 시 로딩 상태 표시 (버튼 disabled + 스피너)
- [ ] API 실패 시 사용자 피드백 표준화 (현재 일부만 alert, 일부는 무반응)
- [ ] 날짜 포맷 `toLocaleString('ko-KR')` 하드코딩 5건 → i18n locale 기반 유틸 추출

### 3단계: 대형 컴포넌트 분할 (MEDIUM)

- [ ] AccountManageView.vue (958줄) → 모달, 필터바, 페이지네이션 분리
- [ ] IncidentDetailView.vue (709줄) → 댓글, 담당자, 이력, 보고서 모달 분리
- [ ] CommonCodeView.vue (631줄) → 그룹/코드 모달 분리

### 4단계: 접근성 (MEDIUM)

- [ ] 모달 ESC 키 닫기, Enter 키 확인 처리
- [ ] SVG 아이콘에 `aria-label` 추가
- [ ] 테이블 행 키보드 내비게이션

---

## Phase 23: i18n 완성 (MEDIUM)

> 하드코딩된 한국어 문자열 제거, 번역 누락 보완.

- [ ] `constants/roles.js`: `ROLE_LABEL` 한국어 → i18n 키로 전환
- [ ] `constants/status.js`: 전체 상태 레이블 (접수, 처리중, 완료 등) → i18n 키로 전환
- [ ] `i18n/locales/en.js` 번역 누락 보완 (현재 약 100줄, 불완전)
- [ ] 에러 메시지 다국어 처리
- [ ] 날짜 포맷 locale 기반 통일 (Phase 22 2단계와 연계)

---

## Phase 24: 테스트 커버리지 확대 (MEDIUM)

> 현재 백엔드 11%, 프론트엔드 10% 미만. 핵심 비즈니스 로직 우선 확보.

### 1단계: 백엔드 서비스 레이어 (HIGH)

- [ ] AuthService 테스트 (로그인, 토큰 갱신, 비밀번호 변경)
- [ ] IncidentService 테스트 (생성, 상태 변경, SLA 계산)
- [ ] DashboardService 테스트 (통계 집계)
- [ ] UserService 테스트 (CRUD, 권한 검증)

### 2단계: 백엔드 통합/보안 테스트 (MEDIUM)

- [ ] MockMvc 기반 Controller 통합 테스트 (주요 엔드포인트)
- [ ] JWT 만료/갱신 시나리오 테스트
- [ ] 역할 기반 접근 제어 검증 테스트
- [ ] Rate Limiting 동작 테스트

### 3단계: 프론트엔드 테스트 (MEDIUM)

- [ ] Store 테스트 확대 (commonCode, notification, menu)
- [ ] API 에러 시나리오 테스트 (MSW 도입 고려)
- [ ] 주요 폼 컴포넌트 유효성 검증 테스트

---

## Phase 25: 추가 개선 (LOW)

> 안정화 후 선택적 진행.

- [ ] 비밀번호 만료(90일) 시 강제 변경 인터셉터 구현
- [ ] 요청 추적용 Request ID 필터 추가 (MDC 기반 Correlation ID)
- [ ] 프로덕션 Swagger UI 접근 차단 (환경별 분기)
- [ ] chart.js 사용 여부 확인 후 미사용 시 제거
- [ ] `.env.example` 파일 추가 (필수 환경변수 목록 문서화)
- [ ] ESLint + Prettier 프론트엔드 도입
- [ ] Spring Boot 3.2.5 → 3.3.x 업그레이드

---

## 설계 원칙 (개발 시 항상 참고)

1. **서비스 중단 최소화** — 동적 폼, DB 기반 설정, 메뉴 동적 관리
2. **추적 가능성** — 모든 변경 이력 자동 적재, 감사 로그
3. **권한 최소화** — RBAC, 이중 방어 (프론트 가드 + 백엔드 Interceptor)
4. **확장 가능한 구조** — 공통코드, 게시판 빌더, JSON 스키마 동적 폼
5. **물리적 삭제 금지** — status / is_active 로 비활성 처리
6. **낙관적 락** — 동시 수정 충돌 방지
