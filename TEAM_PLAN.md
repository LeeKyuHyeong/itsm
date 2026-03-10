# ITSM Agent Team 계획

> 팀명: `itsm_dev_team`

---

## 사전 설정

settings.json에 아래 추가 필요:
```json
{
  "env": {
    "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1"
  }
}
```

---

## 팀 구조

**Lead (나)** = 전체 조율, 태스크 분배, 코드 리뷰, 품질 관리

---

## Phase 0~3: 기반 시스템 구축

파일 충돌 없이 **레이어별 분리**가 가능한 구간.

### Teammate 구성 (3명)

| Teammate | 역할 | 담당 영역 (파일) | Agent Type |
|---|---|---|---|
| `backend-dev` | 백엔드 개발 | `itsm-backend/` 전체 (core + api) | general-purpose |
| `frontend-dev` | 프론트엔드 개발 | `itsm-frontend/` 전체 | general-purpose |
| `schema-dev` | DB 스키마 + SQL | `sql/` 폴더 (DDL, DML, 마이그레이션) | general-purpose |

### 병렬 실행 가능 작업

```
Phase 0 (초기 세팅) — 3개 완전 병렬
├── schema-dev : DB 스키마 SQL 작성 (DDL + 초기 데이터 DML)
├── backend-dev: Gradle 멀티모듈 구성, 의존성, application.yml, 공통 설정
└── frontend-dev: Vite + Vue3 프로젝트 생성, 의존성, 폴더 구조, Axios 설정

Phase 1 (인증/계정/조직) — 의존 관계 있음
Step 1 (병렬):
├── schema-dev : 시스템/계정 테이블 DDL + 초기 데이터
├── backend-dev: 엔티티, Repository, Enum, 공통 예외 (itsm-core)
└── frontend-dev: 공통 레이아웃, 공통 컴포넌트 (BaseTable, BaseModal 등)

Step 2 (schema-dev 완료 후 → backend-dev 진행, frontend-dev는 계속 병렬):
├── backend-dev: Security(JWT), AuthController, UserService, RBAC 인터셉터
│                + 테스트 코드 (TDD)
└── frontend-dev: LoginView, auth store, Router Guard, 사이드바
                   + 테스트 코드 (TDD)

Phase 2 (공통코드/설정) — 3개 병렬
├── schema-dev : 공통/설정 테이블 DDL + 공통코드 초기 데이터
├── backend-dev: CommonCode CRUD, SlaPolicy, Notification, AuditLog AOP
└── frontend-dev: 설정관리 화면들, 알림 드롭다운, 공통코드 store

Phase 3 (자산관리 CMDB) — 3개 병렬
├── schema-dev : 자산 테이블 DDL
├── backend-dev: AssetHw/Sw 엔티티, CRUD API, 이력 자동 적재
└── frontend-dev: 자산 목록/상세 화면
```

---

## Phase 4~6: 도메인 모듈 개발

장애관리, 서비스요청, 변경관리는 **서로 다른 파일**을 다루므로
**도메인별 풀스택 분리**로 전환하면 완전 병렬 가능.

### Teammate 구성 변경 (3명 → 도메인별)

| Teammate | 역할 | 담당 파일 | Agent Type |
|---|---|---|---|
| `incident-dev` | 장애관리 풀스택 | `**/incident/**` (backend + frontend) | general-purpose |
| `sr-dev` | 서비스요청 풀스택 | `**/servicerequest/**` (backend + frontend) | general-purpose |
| `change-dev` | 변경관리 풀스택 | `**/change/**` (backend + frontend) | general-purpose |

### 병렬 실행 가능 작업

```
Phase 4~6 — 3개 완전 병렬 (파일 충돌 없음)
├── incident-dev: 장애관리 (완성도 있게)
│   ├── 엔티티 + Repository
│   ├── 테스트 먼저 작성 (TDD Red)
│   ├── Controller + Service 구현 (TDD Green)
│   ├── 상태머신, SLA 계산, 수정 권한 검증
│   ├── 프론트: 목록/등록/상세/대시보드
│   └── 동적 폼 장애보고서
│
├── sr-dev: 서비스요청 (MVP)
│   ├── 엔티티 + Repository
│   ├── 테스트 먼저 작성 (TDD Red)
│   ├── Controller + Service 구현 (TDD Green)
│   ├── 상태머신, 담당자별 처리, 만족도
│   └── 프론트: 목록/등록/상세
│
└── change-dev: 변경관리 (MVP)
    ├── 엔티티 + Repository
    ├── 테스트 먼저 작성 (TDD Red)
    ├── Controller + Service 구현 (TDD Green)
    ├── 순차 승인 로직
    └── 프론트: 목록/등록/상세
```

---

## Phase 7~9: 후반부

### Teammate 구성 (3명)

| Teammate | 역할 | 담당 | Agent Type |
|---|---|---|---|
| `inspection-dev` | 정기점검 + 보고관리 | `**/inspection/**`, `**/report/**` | general-purpose |
| `board-dev` | 게시판 | `**/board/**` | general-purpose |
| `batch-dev` | Spring Batch | `itsm-batch/` | general-purpose |

```
Phase 7~9 — 3개 병렬
├── inspection-dev: 정기점검 + 보고관리 (backend + frontend)
├── board-dev: 게시판 빌더 + CRUD (backend + frontend)
└── batch-dev: Spring Batch Job 전체 (SLA 알림, 자산 만료, 점검 임박 등)
```

---

## 파일 충돌 방지 규칙

| 규칙 | 내용 |
|---|---|
| Phase 0~3 | **레이어별 분리**: backend-dev는 `itsm-backend/`만, frontend-dev는 `itsm-frontend/`만 |
| Phase 4~6 | **도메인별 분리**: 각 teammate는 자기 도메인 패키지만 수정 |
| 공통 파일 | `build.gradle`, `application.yml`, `router/index.js` 등은 **Lead가 직접 수정** |
| SQL 파일 | schema-dev 전담. 다른 teammate는 SQL 파일 수정 금지 |

---

## Spawn 시 컨텍스트 전달 (중요)

Teammate는 Lead의 대화 기록을 상속받지 않으므로, spawn 시 충분한 컨텍스트를 전달해야 함:

```
각 Teammate spawn prompt에 반드시 포함:
1. ITSM.md 참조 지시 (설계 문서)
2. CLAUDE.md 참조 지시 (개발 규칙 — TDD, 커밋 규칙)
3. 담당 영역의 구체적 파일 경로
4. 담당 테이블/API 목록
5. 다른 teammate와 겹치면 안 되는 파일 범위
```

---

## 태스크 수 가이드라인

문서 권장: teammate당 5~6개 태스크

| Phase | Teammate | 예상 태스크 수 |
|---|---|---|
| 0 | 3명 × 2~3개 | 총 7~9개 |
| 1 | 3명 × 5~6개 | 총 15~18개 |
| 2 | 3명 × 4~5개 | 총 12~15개 |
| 3 | 3명 × 3~4개 | 총 9~12개 |
| 4~6 | 3명 × 6~8개 | 총 18~24개 |
| 7~9 | 3명 × 5~6개 | 총 15~18개 |

---

## 요약: 전체 흐름

```
Phase 0~3: 레이어별 분리 (backend-dev / frontend-dev / schema-dev)
    ↓ 팀 정리 후 재구성
Phase 4~6: 도메인별 분리 (incident-dev / sr-dev / change-dev)
    ↓ 팀 정리 후 재구성
Phase 7~9: 기능별 분리 (inspection-dev / board-dev / batch-dev)
    ↓
Phase 10: Lead 단독 또는 소규모 (대시보드 고도화)
```

각 Phase 전환 시 `cleanup` → 새 팀 `create` 로 teammate를 재구성.
