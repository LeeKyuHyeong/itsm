# ITSM 개발 TODO

> Vue.js (프론트엔드) + Spring Boot (백엔드) REST API 구조

---

## 완료된 Phase

- **Phase 11**: UI 테마 (라이트/다크 모드) ✅
- **Phase 12**: 자산관리 재구조화 ✅
- **Phase 13**: 데모/시뮬레이션 배치 ✅
- **Phase 14**: 전체 i18n (ko/en) 대응 ✅
- **Phase 15**: CI/CD + 운영 배포 ✅

---

## Phase 15: CI/CD + 운영 배포 (완료)

- [x] GitHub Actions CI/CD 파이프라인 구성 (test → build → deploy)
- [x] Docker Compose 구성 (itsm-api, itsm-batch, itsm-frontend, MariaDB)
- [x] Backend Dockerfile (itsm-api, itsm-batch 공용)
- [x] Frontend Dockerfile (multi-stage: node build → nginx)
- [x] 운영 포트 배정 (프론트엔드: 8084, DB: 3310)
- [x] 환경변수 기반 운영 설정 (DB_PASSWORD, JWT_SECRET, CORS_ORIGINS)
- [x] 호스트 nginx 리버스 프록시 설정 (itsm.kiryong.com → localhost:8084)
- [x] Let's Encrypt SSL 인증서 발급 (certbot)
- [x] Flyway 제거, DB 스키마는 ddl-auto: update + 수동 관리로 전환
- [ ] SSL "주의 요함" 경고 확인 필요 (Mixed Content 또는 브라우저 캐시 문제 추정)

---

## Phase 16: 운영 이슈 & OA 자산 분리

### 운영 이슈
- [ ] 운영 로그인 무반응 조사 (CORS / Cookie Secure / 브라우저 Network 탭 확인 필요)
- [ ] SSL "주의 요함" 경고 확인 필요 (Mixed Content 또는 브라우저 캐시 문제 추정)

### OA 자산 분리 (tb_asset_hw에서 OA를 별도 테이블·메뉴로 분리)
- [x] Backend: AssetOa Entity + tb_asset_oa 테이블 신규
- [x] Backend: AssetOaHistory Entity + tb_asset_oa_history 테이블 신규
- [x] Backend: AssetOaRepository (JPQL 검색)
- [x] Backend: AssetOaService (CRUD, 이력, 상태변경)
- [x] Backend: AssetOaController (REST API /api/v1/assets/oa)
- [x] Backend: AssetOa DTOs (Create/Update/Response)
- [x] Backend: AssetStatService OA 통계 반영 (별도 테이블 기반)
- [x] Backend: 전체 테스트 통과 (Entity, Service, Controller, Stat)
- [x] Frontend: AssetOaListView.vue + AssetOaDetailView.vue
- [x] Frontend: assetOaApi 추가
- [x] Frontend: 라우터에 /assets/oa, /assets/oa/:id 경로 추가
- [x] Frontend: AssetListView 탭에서 OA 제거
- [x] Frontend: AssetHwListView 카테고리 필터에서 OA 제거
- [x] DB: DDL에 tb_asset_oa, tb_asset_oa_history 추가
- [x] DB: DML에 OA 자산 목록 메뉴 (menu_id=20) 추가
- [x] DB: DML에 ASSET_OA_TYPE 공통코드 (group_id=12) 추가
- [x] i18n: OA 관련 번역 키 추가 (ko/en)
- [ ] 운영 DB에 tb_asset_oa, tb_asset_oa_history 테이블 생성 (ddl-auto: update로 자동생성 예상)
- [ ] 운영 DB에 OA 메뉴, OA 공통코드 시드데이터 INSERT

### 관리자 화면 필드 매핑 버그 수정
- [x] 공통코드관리: API 필드(groupId/detailId/isActive) → 프론트 필드(id/active) 매핑
- [x] 조직관리: API 필드(companyId/companyNm/ceoNm/tel/deptId/deptNm) 매핑
- [x] 계정관리: API 필드(userId/userNm/deptName) 매핑
- [x] SLA관리: API 필드(policyId/deadlineHours/warningPct/isActive) 매핑
- [x] 알림정책: API 필드(policyId/notiTypeCd/triggerCondition/isActive) 매핑

### 배치 작업 즉시 실행 기능
- [x] Backend: BatchJob Entity에 triggerNow 컬럼 추가 (char(1))
- [x] Backend: BatchJobService.executeNow() 구현
- [x] Backend: POST /api/v1/admin/batch-jobs/{id}/execute 엔드포인트
- [x] Backend: DynamicScheduler 5초 간격 triggerNow 감지 및 즉시 실행
- [x] Backend: 테스트 추가 (Service, Controller)
- [x] Frontend: 배치 관리 화면에 즉시 실행 버튼 추가
- [x] i18n: 즉시 실행 관련 번역 키 추가 (ko/en)
- [x] DB: tb_batch_job에 trigger_now CHAR(1) 컬럼 추가 (ddl-auto로 자동생성)

### 시드 데이터
- [x] sql/03_seed_data.sql 생성 (회사, 부서, 사용자, HW/SW/OA 자산, 장애, 서비스요청)

---

## Phase 17: 소스 위험도 분석 및 품질 개선

> 2026-03-17 전체 소스 정적 분석 결과. 위험도 순으로 정리.

### 1단계: 보안 (CRITICAL/HIGH) — 즉시 조치

- [x] AuthController: Refresh Token 쿠키 `setSecure(false)` → 환경별 분기 (prod: true, local: false)
- [x] AuthController: 쿠키에 `SameSite=Strict` 속성 추가 (CSRF 방지)
- [x] LoginView.vue: 기본 자격증명 `admin/admin123!@#` 하드코딩 제거
- [x] application.yml: JWT Secret 하드코딩 제거 → 환경변수 전용으로 변경
- [x] application-prod.yml: HTTPS/SSL 설정 추가 또는 리버스 프록시 의존 명시
- [x] `.env.example` 파일 생성 (DB_PASSWORD, JWT_SECRET, CORS_ORIGINS 등 문서화)

### 2단계: 배치 안정화 (CRITICAL/HIGH)

- [x] AssetAutoRegisterJob: `@Transactional` 추가
- [x] AssetExpiryJob: `@Transactional` 추가
- [x] ChangeSimulationJob: ChangeApprover 생성 시 `createdBy` 미설정 수정 (N/A: 엔티티에 createdBy 필드 없음)
- [x] StatisticsAggregationJob: `@Transactional` 추가
- [x] 8개 알림 Job에 `@Transactional` 추가 (RepeatIncidentJob, UnassignedIncidentJob, SlaOverdueJob, SlaWarningJob, LongPendingSrJob, InspectionAlertJob, MissedInspectionJob, TrafficSimulationJob)
- [x] DynamicScheduler: `checkTriggeredJobs()`/`refreshSchedules()` 간 Map 동시 수정 레이스 컨디션 해결 (ReentrantLock)

### 3단계: 입력 검증 강화 (HIGH/MEDIUM)

- [x] 다수 Controller: `Map<String,Object>` 기반 요청 → DTO + `@Valid` 전환 (ServiceRequestController, IncidentController, ChangeController, InspectionController, BoardController, ReportController, CommonCodeController, SlaPolicyController, NotificationPolicyController, AssetHw/Sw/OaController)
- [x] Map.get() 후 null 체크 없이 캐스팅하는 코드 수정 (NPE 방지) — DTO + @Valid로 해결
- [x] DELETE 작업에 소유자/권한 검증 추가 (댓글 삭제 시 작성자 검증, 배정 해제 시 인증 검증)
- [x] authentication.getPrincipal() null 체크 추가
- [x] UserService: 중복 체크 TOCTOU → DB unique 제약조건 + 예외 핸들링으로 보완

### 4단계: SQL/스키마 정합성 (HIGH/MEDIUM)

- [x] DDL에 `asset_category`, `asset_sub_category` 컬럼 추가 (tb_asset_hw, tb_asset_sw — Entity와 불일치)
- [x] application-prod.yml: `ddl-auto: update` → `validate`로 변경 (운영 안전)
- [x] FK 컬럼 인덱스 추가 (manager_id, created_by — tb_asset_hw, tb_asset_sw)
- [x] docker-compose.yml: JWT_SECRET 기본값 누락 → 필수 환경변수 검증 추가

### 5단계: 프론트엔드 품질 (MEDIUM)

- [x] NotificationDropdown.vue: 한국어 하드코딩 → i18n 키 전환 ("알림", "전체 읽음", "분 전" 등)
- [x] guards.js: fetchMe() 중복 호출 방지 (세션 복원 진행 중 플래그)
- [x] api/index.js: 토큰 갱신 실패 시 `window.location.href` → `router.push('/login')` + 상태 정리
- [x] AppSidebar.vue: `v-html` SVG 렌더링 → 컴포넌트 방식 전환 (XSS 예방)
- [x] 다수 Store: API 응답 구조 처리 통일 (`data.data` vs `data.data || data`)
- [x] AppHeader.vue: `roles[0]` 접근 시 빈 배열 체크 추가

### 6단계: 성능 최적화 (MEDIUM/LOW)

- [x] N+1 쿼리 개선: Incident, ServiceRequest, Change 목록 조회에 `JOIN FETCH` + countQuery 분리
- [x] ChangeApprover, IncidentAssignee, ServiceRequestAssignee 조회 시 `@EntityGraph(user)` 적용

### 기타 (LOW)

- [x] .gitignore: `scrren.png` 오타 수정
- [x] SYSTEM_USER_ID = 1L 하드코딩 → `itsm.system-user-id` 설정값으로 변경 (7개 배치 Job)
- [x] ChangePasswordView.vue: setTimeout 내 router.push → 컴포넌트 언마운트 시 정리
- [x] Frontend Dockerfile: HEALTHCHECK 추가
- [x] application-local.yml: DB 비밀번호 하드코딩 → 환경변수(기본값 유지) 전환

---

## 설계 원칙 (개발 시 항상 참고)

1. **서비스 중단 최소화** — 동적 폼, DB 기반 설정, 메뉴 동적 관리
2. **추적 가능성** — 모든 변경 이력 자동 적재, 감사 로그
3. **권한 최소화** — RBAC, 이중 방어 (프론트 가드 + 백엔드 Interceptor)
4. **확장 가능한 구조** — 공통코드, 게시판 빌더, JSON 스키마 동적 폼
5. **물리적 삭제 금지** — status / is_active 로 비활성 처리
6. **낙관적 락** — 동시 수정 충돌 방지
