# ITSM
나만의 ITSM ( vue.js + spring boot )

## 기술 스택

| 구분 | 기술 |
|------|------|
| Frontend | Vue 3, Vite, Pinia, Vue Router, Chart.js |
| Backend | Spring Boot 3.2, JDK 17, Gradle (멀티모듈) |
| Database | MySQL (MariaDB 호환) |
| Test | Vitest + Vue Test Utils (FE), JUnit 5 + MockMvc + Mockito (BE) |

## 프로젝트 구조

```
ITSM/
├── itsm-frontend/        # Vue 3 + Vite
├── itsm-backend/
│   ├── itsm-core/        # 공통 엔티티, 설정
│   ├── itsm-api/         # REST API 서버 (포트 8080)
│   └── itsm-batch/       # 배치 처리
└── sql/                  # DDL 스크립트
```

## 로컬 실행 방법

### 1. MySQL 준비

로컬에 MySQL이 설치되어 있어야 합니다. (포트 3306)

```bash
# DB 생성
mysql -u root -p1234 -e "CREATE DATABASE IF NOT EXISTS ITSM"

# DDL 실행
mysql -u root -p1234 ITSM < sql/*.sql
```

- 계정: `root` / `1234` (application-local.yml 기준)
- DB명: `ITSM`

### 2. Backend (IntelliJ)

1. IntelliJ에서 `itsm-backend` 폴더를 Open
2. Gradle import 완료 후 JDK 17 설정 확인
3. `itsm-api` 모듈의 메인 클래스 실행 (Run)
   - profile: `local` (기본 활성화)
   - 포트: `8080`
   - Swagger UI: http://localhost:8080/swagger-ui.html

### 3. Frontend (VSCode)

```bash
cd itsm-frontend
npm install
npm run dev
```

- 포트: `3000`
- 접속: http://localhost:3000
- `/api/*` 요청은 Vite proxy를 통해 `localhost:8080`으로 전달

### 실행 순서

**MySQL → Backend → Frontend**

| 구성요소 | 포트 | 실행 방법 |
|---------|------|----------|
| MySQL | 3306 | 로컬 MySQL 서비스 |
| Backend (Spring Boot) | 8080 | IntelliJ에서 itsm-api Run |
| Frontend (Vue 3 + Vite) | 3000 | `npm run dev` |
