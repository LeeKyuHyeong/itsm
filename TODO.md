# ITSM 개발 TODO

> Vue.js (프론트엔드) + Spring Boot (백엔드) REST API 구조

---

## 다음 작업 (Next Up)

추가 작업 없음. Phase 26 적용 후 운영 DB 마이그레이션 필요.

## 소스 전수조사 (2026-09-16) — "구현했는데 실제로는 안 돌던 것"

Part 별 1커밋. 조사 기록·근거는 `D:\dev\checklist-itsm-source-audit.md`(리포 밖).

- [x] P7 인프라·CI/CD — 헬스체크 엔드포인트 부재(401 을 healthy 로 오판) → `/api/v1/auth/health` 신설 + 200 만 통과 / nginx `admin` 차단어가 `/admin/*` 화면을 444 로 끊음 → 제거 + 회귀 테스트 / 컨테이너 nginx 에 realip 없어 rate limit 공용·fail2ban 무력 → `X-Real-IP` 복원 / fail2ban 필터 3개 중 2개 정규식이 로그와 불일치 → 재작성 / CLAUDE.md·.env.example 드리프트
  - 운영 반영 후 확인: `/admin/menus` F5 → 200, `fail2ban-client status nginx-scanner` 에 실제 원격 IP, `docker compose logs itsm-api | grep health`
- [x] P5 추적성 — `@Auditable` 사용처 0 → 도메인 컨트롤러 변경 엔드포인트 51곳에 부착 + 어스펙트가 `ApiResponse` 를 벗겨 targetType 에 맞는 id 를 고르도록 수정(`AuditableWiringTest` 가 컨텍스트에서 발동 검증) / 알림 드롭다운이 어디에도 마운트되지 않음(헤더 종 = 장식) + `noti.id`(실제 `notiId`) 로 읽음 처리 불가 + 배지 갱신 호출처 0 → 헤더에 마운트, 필드 계약 정정, `/unread-count` 60초 폴링
  - 운영 반영 후 확인: 장애 등록 1건 → `SELECT * FROM tb_audit_log ORDER BY log_id DESC LIMIT 3`, 헤더 종에 배지 표시
  - 미수정(다음 Part): 메뉴 접근 로그·메뉴 기반 인가가 `menu_url`(프론트 경로) ↔ API URI 불일치로 무효 → P1 / `tb_login_history`·`tb_sim_menu_access_log` DDL 부재 → P6 / 이력 미기록 경로(담당자 배정·승인자 추가·역할 부여 등) 목록은 체크리스트 §5
- [x] P3 설정 화면 → 소비 코드 — 메뉴 관리 화면이 부르던 POST/PATCH `/admin/menus` 가 백엔드에 없음 → 신설 / 장애보고서 "동적 폼": `DynamicForm.vue` 미사용·양식 시드 없음·`reportFormId=1` 하드코딩·자유 텍스트를 JSON 컬럼에 저장 → 양식 시드(`sql/phase27_p3_report_form_seed.sql`) + 카드가 `form_schema` 로 렌더링 + 서버 JSON/양식 검증 / `tb_system_config` 읽는 코드 0 → `SystemConfigReader`(잠금 횟수·만료일·최소 길이) / SLA `warning_pct` 를 배치가 안 읽고 0.8 고정 → 정책 조회 / 알림 정책 소비처 0 → 배치 발송 시 비활성 정책 게이트
  - **운영 DB 반영 필요**: `sql/phase27_p3_report_form_seed.sql` (INSERT IGNORE 1행, 스키마 변경 없음)
  - 운영 반영 후 확인: 장애 상세 → 보고서 작성 버튼 활성 → 저장 → `SELECT JSON_VALID(report_content) FROM tb_incident_report`; 메뉴 관리에서 메뉴 1건 수정 저장 → 사이드바 반영
  - 미수정(설계 범위, 체크리스트 §6): 게시판 빌더로 만든 게시판에 도달하는 메뉴가 없음 / `system.maintenance.*` 미소비 / 알림 정책의 `target_role_cd`·`trigger_condition` 미소비 / SR 에는 SLA 배치 없음
- [x] P4 배치·스케줄러 — `tb_batch_job` DDL·시드가 리포에 없음(T-17) → DDL + 잡 15종 시드(`BatchJobSeedTest` 가 job_name↔클래스 1:1 검증, 시뮬레이션 6종은 기본 비활성) / 배치 알림이 매 실행마다 같은 대상에 재발송(dedupe 없음) → 24시간 중복 억제 / 잘못된 CRON 이 저장되면 스케줄러가 로그만 남기고 조용히 멈춤 → 저장 시 검증 / Spring Batch 스타터 + `initialize-schema: always` 잔재(Job/Step 미사용, `BATCH_*` 메타 테이블만 생성) → 제거
  - **운영 DB 반영**: `sql/phase28_p4_batch_job_ddl_seed.sql` (CREATE IF NOT EXISTS + INSERT IGNORE — 운영에 이미 있으면 무변경). `BATCH_*` 테이블 DROP 은 선택(주석)
  - 운영 확인: `SELECT job_name, is_active, last_result FROM tb_batch_job` 의 job_name 이 잡 클래스명과 같은지 · 시뮬레이션 잡 6종의 is_active(운영에 가짜 데이터가 섞이는 중인지) · `SHOW TABLES LIKE 'BATCH_%'`
  - 미수정(체크리스트 §7): `StatisticsAggregationJob` 이 쓰는 `tb_daily_statistics` 를 읽는 코드 0(대시보드는 실시간 집계) / 크론 실행과 수동 실행이 같은 잡에 겹칠 수 있음 / `tb_daily_statistics`·`tb_login_history`·`tb_sim_menu_access_log` DDL 부재 → P6
- [x] P1 인증·인가 체인 — `AuthInterceptor`·`MenuAccessInterceptor` 가 `menu_url`(프론트 라우트)과 API URI 를 대조해 **매칭 0 → 메뉴 기반 인가는 통과 전용, 접근 로그 0건**(감사자 읽기 전용도 미강제) → `ApiMenuMapper`(URI+메서드 → 메뉴 URL, GET=can_read/그 외=can_write, 공용 조회 5종은 읽기 면제) / 로그인 레이트리밋·접근 로그·감사 로그 IP 가 `getRemoteAddr`(컨테이너 nginx IP) → `ClientIpResolver`(신뢰 프록시 + XFF/X-Real-IP) / 시스템 설정·보고서 양식·게시판 설정 서비스에 `@PreAuthorize` 없음 → 관리자 전용 / 역할 부여·회수 이력 미기록 → `tb_user_history`
  - 운영 확인: 감사자 계정으로 장애 등록 시도 → 403 · PM 이 담당자 선택(GET /users) 정상 · `tb_menu_access_log` 행 생성 · `tb_access_log.ip_address` 가 실제 클라이언트 IP
  - ⚠️ 동작 변화: 고객사 역할은 변경관리/자산관리 API 403, 외부사용자는 대시보드 API 403(설계표대로). 프론트는 사이드바만 숨기므로 직접 URL 진입 시 에러 페이지 — 첫 화면 리다이렉트 개선은 백로그
- [x] P2 프론트↔백엔드 API 계약 — 관리자 화면 4개(SLA·알림정책·조직·계정)가 **저장 페이로드를 화면 필드명 그대로 보내 백엔드 DTO 와 전부 불일치(저장 항상 400)** → `utils/adminPayload.js` 매퍼 / 부서 수정 경로 `/departments/{id}`(백엔드는 `/companies/departments/{id}`) 404 → 교정 / 역할 부여가 코드(`ROLE_PM`)를 보내는데 백엔드는 `roleId` 만 → `roleCd` 도 수용, 회수 경로도 코드 허용 / 주담당자 변경이 `{managerId}` 를 보내는데 백엔드는 `{userId}` → 교정 / 보고서 `PUT`→`PATCH` / 장애↔자산 연결 API 를 부르는 화면 없음(CMDB 설계 핵심) → `IncidentAssetCard` / 시스템 설정 화면 없음 → `SystemConfigView` + 라우트 + 메뉴 시드
  - **운영 DB 반영**: `sql/phase29_p2_system_config_menu.sql` (메뉴 1행 + 역할 매핑, 멱등)
  - 운영 확인: SLA/알림정책/회사/부서/사용자 각 1건 저장 · 역할 부여/회수 · 장애 상세에서 자산 연결 · 설정관리 › 시스템 설정 메뉴
  - 미수정(체크리스트 §9): 사용자 이력 화면 없음(`userApi.getHistory` 미사용) / 보고서 작성·양식 관리 화면 없음(API 만) / SLA·공통코드 활성/비활성 토글 UI 없음 / 회사 `address`·부서 `code/parentId` 는 DTO 에 없어 화면 입력이 버려짐
- [x] P6 데이터 계층 — `sql/01_ddl.sql` 이 엔티티와 어긋남: `tb_login_history`·`tb_sim_menu_access_log`·`tb_daily_statistics` CREATE 없음, `tb_service_request` phase26 컬럼 5개 없음(새 설치 시 `validate` 기동 실패) → DDL 보강 + `SchemaDdlConsistencyTest`(모든 @Entity/@Column ↔ DDL 대조) / char(1) 규칙 위반 0 / 상태머신은 4개 엔티티 모두 생성자·`changeStatus` 로만 상태 변경(우회 없음) / 리포 루트 `itsm_data.sql` 은 `.gitignore`(`/*.sql`) 된 **로컬 전용 2026-03-14 덤프**(리포에 없음) — 현재 운영 근거로 쓰지 말 것
  - **운영 DB 반영**: `sql/phase30_p6_schema_drift.sql` (IF NOT EXISTS — 이미 있으면 무변경. 실제로 무언가 만들어지면 그 자체가 기록할 발견)
  - 덤프에서 확인: 2026-03-14 기준 운영 `tb_batch_job` job_name 15개 = 클래스명(시드와 일치), **시뮬레이션 잡 6종 전부 활성**(장애 30분·접속 15분 주기) → 실사용 전환 시 관리자 화면에서 비활성 필요
- [ ] 문서 드리프트

---

## 완료된 Phase

| Phase | 내용 |
|-------|------|
| 11 | UI 테마 (라이트/다크 모드) |
| 12 | 자산관리 재구조화 |
| 13 | 데모/시뮬레이션 배치 |
| 14 | 전체 i18n (ko/en) 대응 |
| 15 | CI/CD + 운영 배포 |
| 16 | 운영 이슈 & OA 자산 분리 |
| 17 | 소스 위험도 분석 및 품질 개선 |
| 18 | 보안 강화 2차 (OWASP Top 10 기반 전 항목) |
| 19 | 인프라 보안 & Docker 강화 (설정 보안, 이미지 버전 고정, 컨테이너 강화, CI/CD 파이프라인, Nginx SSL) |
| 20 | 백엔드 성능 최적화 (DB 인덱스, DashboardService 쿼리 집계화, JPA 페치 전략 정리) |
| 21 | 백엔드 코드 품질 개선 (중복 코드 통합, 매직 스트링 상수화, 에러 처리 표준화) |
| 22 | 프론트엔드 성능 & UX 개선 (toast/confirm 컴포저블, 대형 컴포넌트 분할, 접근성) |
| 23 | i18n 완성 (상수 파일 i18n 전환, console.error 영어화, 날짜 포맷 유틸 추출) |
| 24 | 테스트 커버리지 확대 (백엔드 455 / 프론트엔드 148) |
| 25 | 추가 개선 (Spring Boot 4.0.6 업그레이드, 비밀번호 만료 인터셉터, Request ID 필터, ESLint/Prettier 도입) |
| 26 | SR 일정 관리 컬럼 추가 (접수일/처리예정일/처리변경예정일/변경사유/처리일 + 일정 설정/변경 API) |

---

## 설계 원칙 (개발 시 항상 참고)

1. **서비스 중단 최소화** — 동적 폼, DB 기반 설정, 메뉴 동적 관리
2. **추적 가능성** — 모든 변경 이력 자동 적재, 감사 로그
3. **권한 최소화** — RBAC, 이중 방어 (프론트 가드 + 백엔드 Interceptor)
4. **확장 가능한 구조** — 공통코드, 게시판 빌더, JSON 스키마 동적 폼
5. **물리적 삭제 금지** — status / is_active 로 비활성 처리
6. **낙관적 락** — 동시 수정 충돌 방지
