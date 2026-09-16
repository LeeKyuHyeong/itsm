# ITSM
나만의 ITSM ( Vue 3 + Spring Boot )

> 설계 문서: `ITSM.md` (맨 위 "구현 현황 주석" 부터 볼 것) · 작업 이력: `TODO.md` · 개발 규칙: `CLAUDE.md`

## 기술 스택

| 구분 | 기술 |
|------|------|
| Frontend | Vue 3, Vite, Pinia, Vue Router, vue-i18n (ko/en), 다크/라이트 테마 |
| Backend | Spring Boot 4.0.6, JDK 17, Gradle 멀티모듈 (core / api / batch) |
| Database | MariaDB 11.8 (운영) · MySQL/MariaDB (로컬), `ddl-auto: validate` — 스키마는 `sql/` 이 진실 |
| Test | Vitest + Vue Test Utils (FE), JUnit 5 + MockMvc + Mockito (BE) — CI 에서 둘 다 게이트 |
| Infra | Docker Compose 5컨테이너(api·batch·frontend·db·fail2ban), GitHub Actions 배포(테스트 → 이미지 → SCP/SSH → 헬스체크·롤백) |

## 프로젝트 구조

```
ITSM/
├── itsm-frontend/        # Vue 3 + Vite (컨테이너에서는 nginx 가 정적 서빙 + /api 프록시)
├── itsm-backend/
│   ├── itsm-core/        # 엔티티, 리포지토리, 공통 DTO/예외
│   ├── itsm-api/         # REST API 서버 (포트 8080)
│   └── itsm-batch/       # DB(tb_batch_job) 기반 동적 스케줄러 + 잡 15종 (Spring Batch 미사용)
├── sql/                  # 01_ddl → 02_dml(시스템 기본) → 03_seed_data(데모) 순. phaseNN_*.sql 은 운영 반영용 멱등 스크립트
├── deploy/ fail2ban/     # 호스트 nginx 원본, fail2ban 필터 (CI 가 서버로 전송)
└── docker-compose.yml
```

## 로컬 실행 방법

### 1. DB 준비 (MySQL 또는 MariaDB, 포트 3306)

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ITSM"
mysql -u root -p ITSM < sql/01_ddl.sql
mysql -u root -p ITSM < sql/02_dml.sql
mysql -u root -p ITSM < sql/03_seed_data.sql
```

- 접속 정보는 환경변수 `DB_USERNAME` / `DB_PASSWORD` (기본값은 `itsm-api/src/main/resources/application-local.yml`).
- `ddl-auto` 는 모든 프로파일에서 `validate` 이므로 **DDL 을 먼저 넣지 않으면 기동에 실패**한다.
- 기본 관리자 계정은 `02_dml.sql` 의 `tb_user` 참고.

### 2. Backend

```bash
cd itsm-backend
./gradlew :itsm-api:bootRun      # profile local, 포트 8080, Swagger: http://localhost:8080/swagger-ui.html
./gradlew :itsm-batch:bootRun    # 선택 — 배치 스케줄러 (웹 서버 없음)
./gradlew test                   # 전 모듈 테스트
```

### 3. Frontend

```bash
cd itsm-frontend
npm install
npm run dev        # 포트는 실행 시 출력 참고, /api/* 는 Vite proxy 로 localhost:8080 에 전달
npm run test:run
```

### 실행 순서

**DB → Backend(api) → Frontend** (batch 는 필요할 때만)

## 운영

- 배포는 `main` 푸시 → `.github/workflows/deploy.yml`. 헬스체크는 `GET /api/v1/auth/health` 가 200 이어야 통과하며 실패 시 이전 이미지 태그로 롤백한다.
- 운영 DB 스키마/시드 변경은 `sql/phaseNN_*.sql` 을 서버에서 수동 실행한다 (자동 마이그레이션 없음).
