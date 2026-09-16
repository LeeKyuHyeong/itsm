# CLAUDE.md

## Commit Rules
- 커밋 메시지에 Claude가 진행했다는 내용(Co-Authored-By 등)을 절대 포함하지 않는다.
- 커밋 전 반드시 `TODO.md`를 확인하여 완료된 항목을 `[x]`로 업데이트한다.

## Development Rules
- **TDD (Test-Driven Development)** 방식으로 개발한다.
  - 1) Red: 실패하는 테스트를 먼저 작성한다.
  - 2) Green: 테스트를 통과하는 최소한의 코드를 작성한다.
  - 3) Refactor: 테스트가 통과하는 상태에서 코드를 개선한다.
- 백엔드: JUnit 5 + MockMvc + Mockito 사용
- 프론트엔드: Vitest + Vue Test Utils 사용
- 기능 구현 전 반드시 테스트 코드를 먼저 작성한다.

## Entity 검수 규칙
- JPA Entity에서 `char(1)` 컬럼(`is_active`, `is_visible`, `allow_comment` 등 Y/N 값)은 반드시 `@Column(columnDefinition = "char(1)")` 어노테이션을 명시해야 한다. 누락 시 Hibernate가 `varchar(255)`로 매핑하여 런타임 오류가 발생한다.
- Entity를 **새로 추가하거나 수정할 때** 반드시 해당 Entity의 모든 `char(1)` 필드에 `columnDefinition`이 정확히 선언되어 있는지 검수한다.

## DB 변경 알림 규칙
- DB 스키마 변경(테이블/컬럼 추가·수정·삭제)이 발생하면 **반드시 사용자에게 변경 내용을 알린다.**
- 운영 DB 반영용 ALTER/INSERT SQL을 함께 제공한다.
- 모든 프로파일이 `ddl-auto: validate` 다(자동 생성 없음). 엔티티를 바꾸면 **`sql/01_ddl.sql` 도 같이 고치고**, 운영용 `sql/phaseNN_*.sql`(멱등: `IF NOT EXISTS` / `INSERT IGNORE`)을 만든다. `SchemaDdlConsistencyTest` 가 엔티티↔DDL 불일치를 잡는다.

## 전수조사(2026-09-16) 이후 지켜야 할 배선 규칙
- **새 API 자원**은 `ApiMenuMapper` 에 (경로 패턴 → 메뉴 URL) 매핑을 추가한다. 매핑이 없으면 메뉴 기반 인가·접근 로그 대상이 아니다. 메뉴 URL 은 `sql/02_dml.sql` 시드에 있어야 한다(`ApiMenuMapperTest` 검증).
- **새 변경 엔드포인트**(POST/PATCH/DELETE)에는 `@Auditable(actionType, targetType)` 을 붙인다(`AuditableWiringTest` 가 컨트롤러별 최소 개수를 검사).
- **새 배치 잡**은 `@Component` 클래스 추가 + `sql/02_dml.sql` 의 `tb_batch_job` 시드 + 운영용 phase SQL 을 함께 낸다. `job_name` = 클래스 단순명(`BatchJobSeedTest` 검증).
- **프론트 저장 페이로드**는 백엔드 `*Request` DTO 필드명이어야 한다. 관리자 화면은 `src/utils/adminPayload.js` 매퍼를 거친다. 목록 매핑처럼 `a ?? b` 로 양쪽 이름을 받는 방어는 저장 쪽 불일치를 숨기므로 쓰지 않는다.
- 알림 식별자는 `notiId`, 배지는 `/notifications/unread-count`. 배치 알림은 24시간 중복 억제 + 알림 정책(`tb_notification_policy`) 게이트를 거친다.
- 프록시 뒤 클라이언트 IP 는 `ClientIpResolver` 로만 얻는다(`getRemoteAddr` 직접 사용 금지).
- 배포 헬스체크 계약은 `GET /api/v1/auth/health` = 200 (`HealthEndpointTest`). 컨테이너 nginx 차단어를 추가할 때 Vue 라우트와 겹치지 않게 한다(`nginx-routes.spec.js`).

## 다국어 & 테마 규칙
- 이 프로젝트는 **다크/라이트 테마** 및 **한국어(ko)/영어(en) 다국어**를 모두 지원한다.
- 프론트엔드 UI 작업 시 두 테마 모두에서 정상 표시되는지 확인한다. CSS 변수(`var(--color-*)`)를 사용하고, 하드코딩된 색상값을 쓰지 않는다.
- 사용자에게 보이는 모든 텍스트는 i18n 키(`t('...')`)를 사용한다. 하드코딩된 한국어/영어 문자열을 직접 넣지 않는다.
- DB에서 관리되는 데이터(메뉴명, 공통코드, 게시판명, 배치명 등)는 `_en` 접미사 컬럼(예: `menu_nm_en`, `code_nm_en`)을 통해 영문명을 함께 저장하고, 프론트엔드에서 locale에 따라 분기 표시한다.

## 운영 Nginx 구조 (중요)
- 운영 서버에는 **호스트 Nginx**와 **컨테이너 Nginx** 두 레이어가 있다.
  - 호스트 Nginx (`/etc/nginx/conf.d/itsm.conf`): SSL 종단 + 리버스 프록시 + HSTS 등 보안 헤더. `deploy.yml`이 **파일이 없을 때 한 번만** `deploy/nginx-itsm.conf`를 복사하고 certbot을 실행한다(2026-09-05 서버 실측: `itsm.conf` 존재 → 이후 배포에서는 건너뜀). 호스트 conf를 바꾸려면 서버에서 직접 수정 + 이 리포의 `deploy/nginx-itsm.conf`도 같이 갱신한다.
  - 컨테이너 Nginx (`itsm-frontend/nginx.conf`): 정적 파일 서빙 + API 리버스 프록시 + CSP 등 보안 헤더(정적 파일 한정) + Rate Limiting + 스캐너 차단(444) + **X-Real-IP 로 실제 클라이언트 IP 복원**(realip, 2026-09-16). 이게 없으면 `$remote_addr`이 도커 게이트웨이 IP 라 rate limit 이 전 사용자 공용이 되고 fail2ban 이 무력화된다.
- **요청 흐름**: `브라우저 → 호스트 Nginx (443/SSL) → 컨테이너 Nginx (8084) → Spring API (8080)`
- **스캐너 차단 목록과 Vue 라우트가 겹치면 안 된다** — `location ~* ^/(…)` 에 `admin`을 넣었다가 `/admin/*` 관리자 화면 새로고침이 444 로 끊겼다(2026-09-16 제거). 차단어를 추가할 때 `router/routes/*.js` 의 path 와 대조한다.
- **배포 헬스체크는 `GET /api/v1/auth/health` 가 200 일 때만 통과**한다(`HealthEndpointTest` 가 계약). 이 엔드포인트를 지우거나 인증을 걸면 모든 배포가 롤백된다.

### Nginx 설정 시 주의사항
- **`add_header`는 반드시 `location` 블록 안에 작성**한다. `server` 블록에 넣으면 모든 location에 적용되어 API upstream의 CORS 헤더를 덮어쓴다.
- 컨테이너 Nginx에서 보안 헤더(`CSP`, `X-Frame-Options` 등)는 `location /` (정적 파일)에만 적용하고, `location /api/`에는 넣지 않는다 (Spring Security가 처리).
- 호스트 Nginx(`deploy/nginx-itsm.conf`)의 `add_header`는 `server` 블록에 있고 HSTS 는 여기에만 있다(Spring 은 `X-Forwarded-Proto` 를 해석하지 않아 HSTS 를 붙이지 않는다). 호스트에는 CORS 관련 헤더를 추가하지 않는다 — API 의 CORS 헤더는 Spring 이 내려주고 두 nginx 는 그대로 통과시킨다.
- **CSP `script-src`에 `'unsafe-eval'` 필수** — `vue-i18n` 런타임 메시지 컴파일러가 `new Function()`을 사용하므로, 없으면 Vue 앱 마운트가 실패한다.
- `deploy/nginx-itsm.conf`는 소스 관리용이며, 운영 서버에는 `default.conf`를 사용한다. **이 파일을 운영에 복사하지 않는다.**

### GitHub Actions Secrets (배포에 필요)
- `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`: Docker Hub 인증
- `DB_PASSWORD`: MariaDB root 비밀번호
- `JWT_SECRET`: JWT 서명 키 (base64, 256bit 이상)
- `CORS_ORIGINS`: `https://itsm.kyuhyeong.com`
- `DOMAIN`: `itsm.kyuhyeong.com`
- `CERTBOT_EMAIL`: SSL 인증서 발급용 이메일
- `SERVER_HOST`, `SERVER_PORT`, `SERVER_USER`, `SERVER_SSH_KEY`: 운영 서버 SSH 접속 정보 (`SERVER_IP` 는 f318337 에서 `SERVER_HOST` 로 통일됨)

## 서버 인프라 (SSOT 참조)

- **서버/배포 인프라 SSOT: `D:\server-infra.md`** (로컬 전용, git 미추적 — 리포·운영서버에 없음)
- 포트·도메인·방화벽·컨테이너 TZ 규칙(`Asia/Seoul` 의무)·배포 반영 매트릭스(푸시 시 서버 자동/수동 반영 범위)·트러블슈팅은 전부 그 문서 참조.
- 리포별 `server-infra-*.md`는 폐지됨(2026-06-06). **인프라(compose/nginx/포트/배포) 변경 시 `D:\server-infra.md`를 함께 최신화할 것.**

## 검증 설정

> 전역 `~/.claude/CLAUDE.md`의 검증 규칙(AC → 검증 실행 → 기록)이 이 저장소에 적용될 때의 값. 검증 기록은 `docs/verification/`(2026-09-16 기준 아직 없음 — 첫 기록 시 `~/.claude/verification/templates.md` §1 구조로 생성).

- 유형: 본인 작성·운영 중(itsm.kyuhyeong.com). 위 "Development Rules"의 TDD 를 그대로 따른다. 전역 onboarding §1 특성 테스트 절차 해당 없음.
- 기술 스택: Spring Boot(Java 17) Gradle 멀티모듈(`itsm-backend/`: core·api·batch) + Vue 3/Vite(`itsm-frontend/`) + MariaDB
- 빌드: `itsm-backend/` `./gradlew build` · `itsm-frontend/` `npm run build` + `npm run lint`
- 전체 테스트: 백엔드 `itsm-backend/` `./gradlew test --no-daemon` — 2026-09-16 기준 테스트 클래스 98개·`@Test` 729건, H2 `MODE=MySQL`(`application-test.yml`) / 프론트 `itsm-frontend/` `npm run test:run` — spec 24개·`it` 192건(Vitest)
- 부분 테스트: `./gradlew :itsm-api:test --tests "*HealthEndpointTest"` · `npx vitest run src/components/common/BaseTable.spec.js`
- 로컬 실행: MySQL/MariaDB `localhost:3306/ITSM`(`sql/01_ddl.sql` → `02_dml.sql` → `03_seed_data.sql`, 계정은 `DB_USERNAME`/`DB_PASSWORD` 환경변수) → `./gradlew :itsm-api:bootRun`(local 프로파일, 8080) → `npm run dev`. 상세는 README "로컬 실행 방법"
- 사용자 시나리오 검증 방식: 수동 체크리스트(브라우저, 다크/라이트 × ko/en). 권한 AC 는 역할별 계정으로 API 직접 호출까지 확인
- 프로파일 차이: local(MySQL localhost) / prod(`DB_HOST` MariaDB 컨테이너, 호스트 Nginx + 컨테이너 Nginx 2계층, rate limit·fail2ban·realip) / test(H2). 모든 프로파일 `ddl-auto: validate`
- 테스트 계정(이름·권한만): 시드는 `admin`(ROLE_SUPER_ADMIN) 1개. 역할 8종(SUPER_ADMIN·ITSM_ADMIN·PM·DEVELOPER·DBA·SERVER·NETWORK·SECURITY)은 `sql/02_dml.sql`. 역할별 검증 계정은 로컬에서 직접 만든다
- 외부 연동과 Mock 여부: SMTP 메일 → 테스트는 Mockito, 실발송은 🙋 / 배치 알림은 24시간 중복 억제 + 알림 정책 게이트(위 배선 규칙)
- 배포 방식: `main` push 또는 `workflow_dispatch` → `deploy.yml`(백엔드 테스트 + 프론트 `npm audit`·`test:run` → 이미지 → VPS) → `GET /api/v1/auth/health` 200 아니면 롤백. 배포 후 Smoke: 로그인 → 메뉴 진입 → `/admin/*` 새로고침이 444 가 아님 → 알림 배지 `/notifications/unread-count`
- 검증 기록 위치: docs/verification/

### P0 핵심 시나리오 (초안 — 개발자 확정 필요)
1. 인증·인가: 로그인(JWT) → 메뉴 기반 인가(`ApiMenuMapper`, GET=can_read·그 외=can_write) → 권한 없는 API 직접 호출 차단 → 접근 로그
2. 엔티티 ↔ `sql/01_ddl.sql` 일치(`SchemaDdlConsistencyTest`) + 운영 phase SQL 멱등
3. 변경 엔드포인트 감사 로그(`@Auditable`, `AuditableWiringTest`)
4. 배치: `tb_batch_job` 시드 ↔ `@Component` 잡(`BatchJobSeedTest`) → 실행 이력 → 알림
5. 배포 계약: `/api/v1/auth/health` 200, 컨테이너 nginx 차단어 ↔ Vue 라우트 불겹침(`src/router/nginx-routes.spec.js`)

P1: SR·장애 등 핵심 업무 흐름(상태 전이, 담당자 교체 차단), 알림 · P2: 게시판, 공통코드, 다국어 `_en` 컬럼 · P3: 문구·CSS

### 이 프로젝트만의 규칙
- 스키마 변경 = 엔티티 + `sql/01_ddl.sql` + 운영 `sql/phaseNN_*.sql`(멱등) 3종 세트. 검증 기록 "DB·설정 변경"에 phase 번호를 적고 사용자에게 알린다(위 "DB 변경 알림 규칙")
- 테스트 DB 가 H2 `MODE=MySQL` 이라 MariaDB 전용 SQL·`char(1)` 매핑 오류는 테스트가 못 잡는다 → 위 "Entity 검수 규칙" 수행 + 운영 반영 후 🙋
- 새 API·변경 엔드포인트·배치는 위 "배선 규칙" 3종(`ApiMenuMapper`·`@Auditable`·배치 시드)을 checklist 점검 항목에 넣는다
- 프론트 UI 변경은 두 테마 × 두 언어 수동 확인을 기록에 명시한다
- 커밋 전 `TODO.md` 갱신(위 Commit Rules)
