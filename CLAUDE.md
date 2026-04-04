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
- `ddl-auto: update`로 자동 생성되는 컬럼이라도 변경 사실은 반드시 고지한다.

## 다국어 & 테마 규칙
- 이 프로젝트는 **다크/라이트 테마** 및 **한국어(ko)/영어(en) 다국어**를 모두 지원한다.
- 프론트엔드 UI 작업 시 두 테마 모두에서 정상 표시되는지 확인한다. CSS 변수(`var(--color-*)`)를 사용하고, 하드코딩된 색상값을 쓰지 않는다.
- 사용자에게 보이는 모든 텍스트는 i18n 키(`t('...')`)를 사용한다. 하드코딩된 한국어/영어 문자열을 직접 넣지 않는다.
- DB에서 관리되는 데이터(메뉴명, 공통코드, 게시판명, 배치명 등)는 `_en` 접미사 컬럼(예: `menu_nm_en`, `code_nm_en`)을 통해 영문명을 함께 저장하고, 프론트엔드에서 locale에 따라 분기 표시한다.

## 운영 Nginx 구조 (중요)
- 운영 서버에는 **호스트 Nginx**와 **컨테이너 Nginx** 두 레이어가 있다.
  - 호스트 Nginx (`/etc/nginx/conf.d/default.conf`): SSL 종단 + 리버스 프록시 (Certbot 관리). **ITSM 전용 conf 파일을 별도로 만들지 않는다** — `default.conf`에서 `itsm.kyuhyeong.com` 서버 블록을 Certbot이 관리한다.
  - 컨테이너 Nginx (`itsm-frontend/nginx.conf`): 정적 파일 서빙 + API 리버스 프록시 + 보안 헤더 + Rate Limiting.
- **요청 흐름**: `브라우저 → 호스트 Nginx (443/SSL) → 컨테이너 Nginx (8084) → Spring API (8080)`

### Nginx 설정 시 주의사항
- **`add_header`는 반드시 `location` 블록 안에 작성**한다. `server` 블록에 넣으면 모든 location에 적용되어 API upstream의 CORS 헤더를 덮어쓴다.
- 컨테이너 Nginx에서 보안 헤더(`CSP`, `X-Frame-Options` 등)는 `location /` (정적 파일)에만 적용하고, `location /api/`에는 넣지 않는다 (Spring Security가 처리).
- 호스트 Nginx에는 `add_header`를 넣지 않는다 — 보안 헤더는 컨테이너 Nginx와 Spring Security에서 처리.
- **CSP `script-src`에 `'unsafe-eval'` 필수** — `vue-i18n` 런타임 메시지 컴파일러가 `new Function()`을 사용하므로, 없으면 Vue 앱 마운트가 실패한다.
- `deploy/nginx-itsm.conf`는 소스 관리용이며, 운영 서버에는 `default.conf`를 사용한다. **이 파일을 운영에 복사하지 않는다.**

### GitHub Actions Secrets (배포에 필요)
- `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`: Docker Hub 인증
- `DB_PASSWORD`: MariaDB root 비밀번호
- `JWT_SECRET`: JWT 서명 키 (base64, 256bit 이상)
- `CORS_ORIGINS`: `https://itsm.kyuhyeong.com`
- `DOMAIN`: `itsm.kyuhyeong.com`
- `CERTBOT_EMAIL`: SSL 인증서 발급용 이메일
- `SERVER_IP`, `SERVER_PORT`, `SERVER_USER`, `SERVER_SSH_KEY`: 운영 서버 SSH 접속 정보
