# ITSM 개발 TODO

> Vue.js (프론트엔드) + Spring Boot (백엔드) REST API 구조
> 구현 우선순위: 1.장애관리(완성도) → 2.서비스요청(MVP) → 3.변경관리(MVP)

---

## Phase 0: 프로젝트 초기 세팅

### 백엔드 (Spring Boot 멀티모듈)
- [x] Gradle 멀티모듈 프로젝트 생성 (`itsm-core`, `itsm-api`, `itsm-batch`)
- [x] `settings.gradle` / `build.gradle` 구성
- [x] Spring Boot 의존성 설정 (Spring Web, JPA, Security, Validation, QueryDSL)
- [x] DB 연결 설정 (MySQL/MariaDB + `application.yml`) — local/prod/test 프로파일 분리
- [x] JPA 공통 설정 (`JpaConfig.java`, Auditing)
- [x] 공통 응답 포맷 구현 (`ApiResponse`, `ErrorResponse`)
- [x] 공통 예외 처리 (`BusinessException`, `ErrorCode`, `GlobalExceptionHandler`)
- [x] Swagger (SpringDoc) 설정

### 프론트엔드 (Vue.js)
- [x] Vite + Vue 3 프로젝트 생성
- [x] 의존성 설치 (Vue Router, Pinia, Axios, Chart.js)
- [x] 프로젝트 폴더 구조 생성
- [x] Axios 인스턴스 및 인터셉터 설정 (JWT 자동 첨부, 에러 핸들링)
- [x] 전역 CSS / 변수 파일 세팅
- [x] 공통 레이아웃 컴포넌트 (`AppLayout`, `AppHeader`, `AppSidebar`, `AppBreadcrumb`)

### DB 스키마
- [x] 전체 40개 테이블 DDL (`sql/01_ddl.sql`)
- [x] 초기 데이터 DML (`sql/02_dml.sql`) — 역할, 메뉴, 관리자, 공통코드, SLA, 시스템설정

---

## Phase 1: 인증 / 계정 / 조직 (기반 시스템)

### DB 스키마
- [x] `tb_company` / `tb_company_history` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_department` / `tb_department_history` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_user` / `tb_user_history` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_role` / `tb_user_role` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_menu` / `tb_role_menu` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_access_log` 테이블 생성 (Phase 0에서 완료)
- [x] 초기 데이터 Insert (역할, 메뉴, 슈퍼관리자 계정, 공통코드) (Phase 0에서 완료)

### 백엔드 — 엔티티 / 리포지토리
- [x] `User`, `UserHistory` 엔티티 (Temporal Data Modeling — valid_from/valid_to)
- [x] `Role`, `UserRole` 엔티티
- [x] `Company`, `CompanyHistory`, `Department`, `DepartmentHistory` 엔티티
- [x] `Menu`, `RoleMenu` 엔티티 (자기참조 구조)
- [x] 각 엔티티 JPA Repository

### 백엔드 — 인증 (JWT + Spring Security)
- [x] `SecurityConfig` (Spring Security 설정, 필터 체인)
- [x] `JwtTokenProvider` (Access Token / Refresh Token 생성, 검증, 파싱)
- [x] `JwtAuthFilter` (요청마다 JWT 검증 필터)
- [x] `CustomUserDetailsService` (DB 기반 인증)
- [x] `AuthController` — 로그인, 로그아웃, 토큰 갱신, 내 정보 조회, 비밀번호 변경
- [x] 비밀번호 정책 검증 (8자 이상, 영문대소문자+숫자+특수문자)
- [x] 로그인 실패 5회 제한 → 계정 잠금 (LOCKED) → 30분 자동 해제
- [x] Refresh Token HttpOnly Cookie 저장
- [x] `tb_access_log` 로그인/로그아웃/실패 이력 적재

### 백엔드 — 권한 (RBAC)
- [x] `AuthInterceptor` (URL별 역할 검증 — 백엔드 이중 방어)
- [x] `MenuAccessInterceptor` (메뉴 접근 로그 — `tb_menu_access_log`)
- [x] 역할별 메뉴 조회 API (`GET /api/v1/admin/menus`)

### 백엔드 — 사용자 / 조직 CRUD
- [x] `UserController` + `UserService` — 목록, 등록, 상세, 수정, 상태변경, 이력조회
- [x] `CompanyController` + `CompanyService` — 회사 CRUD + 이력
- [x] 부서 CRUD (회사 하위)
- [x] 역할 부여/회수 API
- [x] 사용자 변경 시 `tb_user_history` 자동 적재 (Temporal 방식)
- [x] 회사/부서 변경 시 이력 테이블 적재 (단순 이력 방식)
- [x] login_id 마스킹 처리 (DELETED 시 `DELETED_{userId}_{loginId}`)

### 프론트엔드 — 인증
- [x] `LoginView.vue` (로그인 화면)
- [x] `auth.js` Pinia store (JWT, 사용자 정보 관리)
- [x] `guards.js` Vue Router 네비게이션 가드 (인증/권한 체크)
- [x] Axios 인터셉터 — Access Token 만료 시 Refresh Token 자동 갱신
- [x] 최초 로그인 시 비밀번호 강제 변경 화면
- [x] 90일 초과 비밀번호 변경 안내 팝업

### 프론트엔드 — 레이아웃 / 메뉴
- [x] `AppSidebar.vue` — 역할 기반 동적 메뉴 렌더링 (API 조회)
- [x] `AppHeader.vue` — 알림 아이콘, 사용자 정보
- [x] `AppBreadcrumb.vue`

### 프론트엔드 — 계정 / 조직 관리
- [x] `AccountManageView.vue` — 사용자 목록, 등록, 수정, 상태 변경, 역할 관리
- [x] `OrgManageView.vue` — 회사/부서 관리

---

## Phase 2: 공통코드 / 설정 관리

### DB 스키마
- [x] `tb_common_code` / `tb_common_code_detail` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_system_config` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_sla_policy` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_notification_policy` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_notification` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_audit_log` / `tb_menu_access_log` 테이블 생성 (Phase 0에서 완료)
- [x] 초기 공통코드 데이터 Insert (Phase 0에서 완료)

### 백엔드 — 공통코드
- [x] `CommonCode`, `CommonCodeDetail` 엔티티
- [x] `CommonCodeController` — 그룹/상세 CRUD API
- [x] 프론트에서 공통코드 캐싱용 조회 API

### 백엔드 — 시스템 설정
- [x] `SystemConfig` 엔티티
- [x] `SystemConfigController` — 설정 조회/수정 API
- [x] 공지사항, 시스템 점검 배너 설정

### 백엔드 — SLA / 알림 정책
- [x] `SlaPolicy` 엔티티
- [x] `SlaController` — SLA 정책 CRUD API (고객사별/전체 기본값)
- [x] `NotificationPolicy` 엔티티
- [x] 알림 정책 CRUD API

### 백엔드 — 알림
- [x] `Notification` 엔티티
- [x] `NotificationController` — 내 알림 목록, 읽음 처리, 전체 읽음
- [x] 알림 발송 공통 서비스 (`NotificationService`)

### 백엔드 — 감사 로그
- [x] `AuditLogAspect` (AOP 기반 자동 적재)
- [x] `AuditLog` 엔티티

### 프론트엔드 — 설정 관리
- [x] `CommonCodeView.vue` — 공통코드 관리 화면
- [x] `SlaManageView.vue` — SLA 정책 관리
- [x] `NotificationPolicyView.vue` — 알림 정책 관리
- [x] `MenuManageView.vue` — 메뉴/권한 관리
- [x] `commonCode.js` Pinia store — 공통코드 캐싱

### 프론트엔드 — 알림
- [x] `notification.js` Pinia store
- [x] `NotificationDropdown.vue` — 헤더 알림 드롭다운

### 프론트엔드 — 공통 컴포넌트
- [x] `BaseTable.vue` — 공통 테이블 (정렬, 페이지네이션)
- [x] `BasePagination.vue`
- [x] `BaseModal.vue`
- [x] `BaseConfirm.vue` — 확인/취소 다이얼로그
- [x] `BaseFileUpload.vue`
- [x] `BaseStatusBadge.vue` — 상태 뱃지 (색상 자동)
- [x] `BaseSlaBar.vue` — SLA 경과율 프로그레스바
- [x] `DynamicForm.vue` — 동적 폼 렌더러 (JSON 스키마 기반)

---

## Phase 3: 자산관리 (CMDB)

### DB 스키마
- [x] `tb_asset_hw` / `tb_asset_hw_history` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_asset_sw` / `tb_asset_sw_history` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_asset_relation` 테이블 생성 (Phase 0에서 완료)

### 백엔드
- [x] `AssetHw`, `AssetSw`, `AssetRelation` 엔티티 + History 엔티티
- [x] `AssetHwController` — HW 자산 CRUD + 이력 조회 API
- [x] `AssetSwController` — SW 자산 CRUD + 이력 조회 API
- [x] 자산 연관관계 등록/삭제 API
- [x] 자산 수정 시 `tb_asset_hw_history` / `tb_asset_sw_history` 자동 적재

### 프론트엔드
- [x] `AssetHwListView.vue` — HW 자산 목록 (검색, 필터, 페이지네이션)
- [x] `AssetHwDetailView.vue` — HW 자산 상세 (연관 SW, 변경이력, 관련 장애/변경)
- [x] `AssetSwListView.vue` — SW 자산 목록
- [x] `AssetSwDetailView.vue` — SW 자산 상세

---

## Phase 4: 장애관리 (완성도 있게 구현)

### DB 스키마
- [x] `tb_incident` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_incident_asset` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_incident_assignee` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_incident_comment` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_incident_history` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_incident_report` 테이블 생성 (Phase 0에서 완료)

### 백엔드
- [x] `Incident`, `IncidentAsset`, `IncidentAssignee` 엔티티
- [x] `IncidentComment`, `IncidentHistory`, `IncidentReport` 엔티티
- [x] `IncidentController` + `IncidentService`
  - [x] 장애 CRUD API
  - [x] 상태 변경 API (상태머신: 접수→처리중→완료→종료, 반려)
  - [x] 담당자 지정/해제 API
  - [x] 댓글 CRUD API
  - [x] 변경 이력 조회 API
  - [x] 장애보고서 작성/수정 API
- [x] 상태머신 검증 (유효하지 않은 상태 전이 차단)
- [ ] 수정 권한 검증 (상태별 + 역할별 권한 매트릭스)
- [x] SLA 기한 자동 계산 (`sla_deadline_at = 접수일시 + deadline_hours`)
- [x] 수정 시 `tb_incident_history` 자동 적재
- [x] 연관 자산 매핑 (HW/SW)
- [x] `SlaCalculator` — 경과율 실시간 계산, 반려 시 기한 연장

### 프론트엔드
- [x] `IncidentListView.vue` — 장애 목록 (상태필터, 우선순위필터, 검색, SLA 경과율 표시)
- [x] `IncidentFormView.vue` — 장애 등록/수정 (자산 연결, 우선순위 선택)
- [x] `IncidentDetailView.vue` — 장애 상세
  - [x] 상태머신 버튼 (현재 상태에 따라 활성/비활성)
  - [x] 담당자 배정 영역
  - [x] 처리내용 작성 영역 (주담당자만)
  - [x] 댓글 영역
  - [x] 변경 이력 타임라인
  - [x] SLA 카운트다운 / 프로그레스바
  - [x] 장애보고서 작성 (동적 폼 — `DynamicForm.vue` 활용)

### 대시보드
- [x] `DashboardView.vue`
  - [x] 상태별 장애 건수 (도넛/파이 차트)
  - [x] 우선순위별 미처리 건수
  - [x] SLA 초과/임박 건수
  - [x] 최근 장애 목록
  - [ ] 월별 장애 추이 (라인 차트)

---

## Phase 5: 서비스요청 (MVP)

### DB 스키마
- [x] `tb_service_request` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_service_request_asset` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_service_request_assignee` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_service_request_process` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_service_request_history` 테이블 생성 (Phase 0에서 완료)

### 백엔드
- [ ] 엔티티: `ServiceRequest`, `ServiceRequestAsset`, `ServiceRequestAssignee`, `ServiceRequestProcess`, `ServiceRequestHistory`
- [ ] `ServiceRequestController` + `ServiceRequestService`
  - [ ] SR CRUD API
  - [ ] 상태 변경 API (접수→담당자배정→처리중→완료대기→종료, 요청취소, 반려)
  - [ ] 담당자 지정/제거 API
  - [ ] 처리내용 작성/수정/완료 API (담당자별)
  - [ ] 댓글 CRUD API
  - [ ] 만족도 제출 API
  - [ ] 변경 이력 조회 API
- [ ] 상태 자동 전환 (담당자 전원 COMPLETED → 완료대기)
- [ ] 담당자 전원 제거 시 접수로 자동 되돌림
- [ ] SLA 기한 자동 계산 (요청일 기준, 반려 시 연장)
- [ ] 담당자 퇴사/비활성화 엣지케이스 처리
- [ ] 중복 등록 방지 (debounce)

### 프론트엔드
- [ ] `ServiceRequestListView.vue` — SR 목록
- [ ] `ServiceRequestFormView.vue` — SR 등록/수정
- [ ] `ServiceRequestDetailView.vue` — SR 상세
  - [ ] 상태머신 UI
  - [ ] 담당자별 처리현황 (N/N 완료 카운트)
  - [ ] 반려 횟수 표시
  - [ ] 만족도 조사 영역

---

## Phase 6: 변경관리 (MVP)

### DB 스키마
- [x] `tb_change` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_change_asset` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_change_approver` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_change_history` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_change_comment` 테이블 생성 (Phase 0에서 완료)

### 백엔드
- [ ] 엔티티: `Change`, `ChangeAsset`, `ChangeApprover`, `ChangeHistory`, `ChangeComment`
- [ ] `ChangeController` + `ChangeService`
  - [ ] 변경 CRUD API
  - [ ] 상태 변경 API (초안→승인요청→승인완료→실행중→완료→종료, 반려)
  - [ ] 승인자 지정 API
  - [ ] 승인/반려 처리 API (순차 승인)
  - [ ] 댓글 CRUD API
  - [ ] 변경 이력 조회 API
- [ ] 순차 승인 로직 (approve_order 기반)
- [ ] 승인자 전원 승인 시 자동 상태 전환

### 프론트엔드
- [ ] `ChangeListView.vue` — 변경 목록
- [ ] `ChangeFormView.vue` — 변경 등록/수정 (롤백 계획 포함)
- [ ] `ChangeDetailView.vue` — 변경 상세
  - [ ] 승인 현황 (승인자별 상태)
  - [ ] 상태머신 UI
  - [ ] 댓글 영역

---

## Phase 7: 정기점검 / 보고관리

### DB 스키마
- [x] `tb_inspection` / `tb_inspection_asset` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_inspection_item` / `tb_inspection_result` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_inspection_history` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_report_form` / `tb_report` 테이블 생성 (Phase 0에서 완료)

### 백엔드 — 정기점검
- [ ] 엔티티: `Inspection`, `InspectionAsset`, `InspectionItem`, `InspectionResult`, `InspectionHistory`
- [ ] `InspectionController` + `InspectionService`
  - [ ] 점검 CRUD API
  - [ ] 상태 변경 API (예정→진행중→완료→종료, 보류)
  - [ ] 체크리스트 항목 등록/수정 API
  - [ ] 점검 결과 입력/수정 API

### 백엔드 — 보고관리
- [ ] `ReportForm`, `Report` 엔티티
- [ ] `ReportController` — 양식 CRUD, 보고서 작성/조회/수정 API
- [ ] 보고서 양식 관리 (JSON 스키마 기반 동적 폼)
- [ ] 참조유형별(INCIDENT/SR/CHANGE/INSPECTION) 연결

### 프론트엔드 — 정기점검
- [ ] `InspectionListView.vue` — 점검 목록/일정
- [ ] `InspectionFormView.vue` — 점검 등록
- [ ] `InspectionDetailView.vue` — 점검 상세 (체크리스트 결과 입력)

### 프론트엔드 — 보고관리
- [ ] `ReportListView.vue` — 보고서 목록
- [ ] `ReportDetailView.vue` — 보고서 상세/출력/PDF 다운로드
- [ ] 보고서 양식 관리 화면 (설정관리 내)

---

## Phase 8: 게시판

### DB 스키마
- [x] `tb_board_config` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_board_post` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_board_comment` 테이블 생성 (Phase 0에서 완료)
- [x] `tb_board_file` 테이블 생성 (Phase 0에서 완료)

### 백엔드
- [ ] `BoardConfig`, `BoardPost`, `BoardComment`, `BoardFile` 엔티티
- [ ] `BoardController` — 게시판 목록, 게시글 CRUD, 댓글 CRUD API
- [ ] 게시판 빌더 API (`/api/v1/admin/boards` — 생성/수정/설정)
- [ ] 역할별 읽기/쓰기 권한 검증 (role_permission JSON 기반)
- [ ] 파일 업로드 — 허용 확장자 화이트리스트, UUID 파일명, 용량 제한
- [ ] 게시판 생성 시 사이드바 메뉴 자동 반영

### 프론트엔드
- [ ] `BoardListView.vue` — 게시글 목록 (게시판별)
- [ ] `BoardPostFormView.vue` — 게시글 작성/수정 (첨부파일)
- [ ] `BoardPostDetailView.vue` — 게시글 상세/댓글
- [ ] `BoardManageView.vue` — 게시판 빌더 (관리자)
- [ ] 사이드바 게시판 메뉴 동적 렌더링

---

## Phase 9: Spring Batch (자동화)

### 배치 모듈 (`itsm-batch`)
- [ ] `BatchConfig` 설정
- [ ] SLA 경고 알림 (매시간 — 경과율 80% 초과 건)
- [ ] SLA 초과 에스컬레이션 (매시간 — 기한 초과 미종료 건)
- [ ] 미배정 장애 알림 (매시간)
- [ ] 완료대기 장기 미처리 SR 알림 (일 1회 — 2일 초과)
- [ ] 반복 장애 감지 (일 1회 — 동일 자산 30일 내 3회 이상)
- [ ] 자산 만료 임박 알림 (일 1회 — 30일/7일 전)
- [ ] 점검 임박 알림 (일 1회 — 7일/3일 전)
- [ ] 미실시 점검 감지 (일 1회)
- [ ] 연간 SLA 보고서 자동 집계 (연 1회)

---

## Phase 10: 대시보드 고도화 / 통계

- [ ] 대시보드에 서비스요청, 변경관리 통계 추가
- [ ] 운영 모니터링 지표 (미처리/지연/미배정 건수)
- [ ] SLA 지표 현황 (장애처리율, 기한준수율, 만족도 평균 등)
- [ ] 시스템 가용률 계산 및 표시
- [ ] 담당자별 처리 건수 통계
- [ ] 월별/분기별 추이 차트

---

## 기술 스택 요약

| 영역 | 기술 |
|---|---|
| 백엔드 프레임워크 | Spring Boot |
| 빌드 도구 | Gradle (멀티모듈) |
| ORM | Spring Data JPA + QueryDSL |
| 인증 | JWT (Access + Refresh Token) + Spring Security |
| 배치 | Spring Batch |
| 프론트엔드 | Vue 3 + Vite |
| 상태관리 | Pinia |
| HTTP 클라이언트 | Axios |
| 라우팅 | Vue Router |
| 차트 | Chart.js (Vue Chart) |
| API 문서 | Swagger (SpringDoc) |

---

## 설계 원칙 (개발 시 항상 참고)

1. **서비스 중단 최소화** — 동적 폼, DB 기반 설정, 메뉴 동적 관리
2. **추적 가능성** — 모든 변경 이력 자동 적재, 감사 로그
3. **권한 최소화** — RBAC, 이중 방어 (프론트 가드 + 백엔드 Interceptor)
4. **확장 가능한 구조** — 공통코드, 게시판 빌더, JSON 스키마 동적 폼
5. **물리적 삭제 금지** — status / is_active 로 비활성 처리
6. **낙관적 락** — 동시 수정 충돌 방지
