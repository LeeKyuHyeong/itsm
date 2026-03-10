# ITSM 토이프로젝트 설계 정리

## 프로젝트 방향

- **기술 스택**: Vue.js (프론트엔드) + Spring Boot (백엔드) REST API 구조
- **포지셔닝**: 3개 기관 ITSM 운영 경험을 바탕으로, JSP 기반 레거시를 Vue.js + Spring Boot REST API 구조로 현대화한다면
- **설계 1원칙**: 서비스 중단 최소화
- **설계 2원칙**: 추적 가능성 (Traceability) — 누가, 언제, 무엇을 바꿨는지 항상 기록
- **설계 3원칙**: 권한 최소화 (Least Privilege) — 각 역할은 자기 담당 영역만 접근 가능
- **설계 4원칙**: 확장 가능한 구조 (Extensibility) — 모듈 추가 시 기존 코드 수정 최소화

---

## 용어 정리 (국내 실무 용어 ↔ 일반 ITSM 용어)

| 국내 실무 용어 | 일반 ITSM 용어 |
|---|---|
| 장애관리 | 티켓 / 인시던트 관리 |
| 서비스요청 | 서비스 요청 |
| 변경관리 | 변경관리 |
| 자산관리 | CMDB (Configuration Management Database) |

---

## 구현 범위 및 우선순위

| 순위 | 모듈 | 구현 수준 |
|---|---|---|
| 1 | 장애관리 (티켓) | 완성도 있게 |
| 2 | 서비스요청 | MVP 수준 |
| 3 | 변경관리 | MVP 수준 |

> **MVP(Minimum Viable Product)**: 핵심 흐름만 동작하는 최소 기능 수준.
> 포트폴리오에서 "있긴 하다"를 보여주되, 깊이는 장애관리에 집중.

---

## 핵심 설계 개념: CMDB

자산(CI, Configuration Item)이 중심 엔티티이고, 모든 모듈이 자산에 연결되는 구조.

```
자산 (HW/SW)
  ├── 장애관리    → 이 서버에서 장애 발생
  ├── 서비스요청  → 이 SW 계정 추가 요청
  ├── 변경관리    → 이 서버 패치 예정
  └── 정기점검    → 이 장비 점검 일정
```

- HW ↔ SW 연관관계도 CMDB 범주
- 단순 티켓 시스템이 아닌 자산 중심 설계가 차별 포인트

---

## 사용자 역할 정의

| 역할 | 설명 |
|---|---|
| 슈퍼관리자 | 시스템 전체 설정, ITSM관리자 계정 관리 |
| ITSM관리자 | 운영 전반 관리 |
| 유지보수팀 - PM | 프로젝트 관리 |
| 유지보수팀 - 개발자 | 개발 담당 |
| 유지보수팀 - 보안담당자 | 보안 담당 |
| 유지보수팀 - DB담당자 | DB 담당 |
| 유지보수팀 - 네트워크담당자 | 네트워크 담당 |
| 유지보수팀 - 서버담당자 | 서버 담당 |
| 고객사 인원 | 부서별 일반 사용자 |
| 외부사용자 | 협력업체 등 정기점검 인원 |
| 감사자 | 읽기 전용, 전체 이력 조회 |
| 비로그인 | 제한적 접근 (파일 다운로드 등) |

---

## 장애관리 모듈 주요 기능 (완성도 있게 구현)

- 티켓 CRUD + 상태머신 (접수 → 처리중 → 완료 → 종료)
- 담당자 배정 + 권한 분리 (Spring Security)
- 우선순위 / SLA 카운트다운
- 댓글 / 히스토리 로그
- 대시보드 통계 (Vue Chart)
- Spring Batch로 SLA 초과 자동 알림
- **장애보고서 동적 폼** (JSON 스키마 기반, 서버 재시작 없이 관리자가 양식 변경 가능)

---

## 설계 어필 포인트 (면접 소재)

1. **동적 폼 설계**
   > "장애보고서 양식을 서버 재시작 없이 관리자가 직접 변경할 수 있도록 JSON 스키마 기반 동적 폼으로 설계했습니다"

2. **CMDB 기반 구조**
   > "단순 티켓 시스템이 아니라 CMDB 기반으로 자산과 장애/변경이 연결되는 구조로 설계했습니다"

3. **서비스 중단 최소화 원칙**
   > "배포나 설정 변경 시 서비스 영향을 최소화하는 방향으로 설계했습니다 — 동적 폼, 공지/설정값 DB 관리, 메뉴 동적 관리 등"

4. **현대화 관점**
   > "3개 기관에서 JSP 기반 ITSM을 운영한 경험을 바탕으로, Vue.js + Spring Boot REST API 구조로 현대화하면 어떨지 직접 설계했습니다"

---

## 제외 항목

- **근태관리**: ITSM 도메인과 맞지 않는 HR/ERP 영역. 현 회사 ITSM에 있는 건 파견 유지보수팀의 현실적 타협이지, 설계상 올바른 위치가 아님.

---

## 메뉴 / 화면 정의 원칙

**1. 권한별 메뉴 관리**
- 역할에 따라 보이는 메뉴가 다르게 구성
- 메뉴 자체가 안 보이는 것이 1차 접근 차단

**2. URL 직접 접근 방어 (Interceptor)**
- 메뉴가 안 보여도 URL 직접 입력으로 접근 시도 가능
- 모든 요청은 Interceptor에서 권한 검증을 반드시 통과해야 함
- 프론트 라우터 가드(Vue Router Guard) + 백엔드 Interceptor 이중 방어

**3. 동적 보고서 폼 관리**
- 장애보고서 등 보고서 양식은 ITSM관리자 / 슈퍼관리자가 설정 화면에서 수정 가능
- 수정된 양식 구조는 DB에 JSON 형태로 저장
- 프론트는 DB에서 JSON을 읽어 화면을 동적으로 렌더링
- 서버 재시작 없이 양식 변경 가능 (서비스 중단 최소화 원칙 적용)

```
[관리자 설정 화면] → 양식 수정 → DB 저장 (JSON)
                                        ↓
[장애보고서 작성 화면] ← JSON 조회 ← DB
```

---

## 설정 관리 (DB 저장 기반, 서버 재시작 없이 변경)

> 모든 항목은 관리자 설정 화면에서 수정 → DB 저장 → 즉시 반영

### 메뉴 / 화면 구성
- 메뉴 목록 + 역할별 권한 매핑
- 메뉴 순서, 노출 여부

### 보고서 / 양식
- 장애보고서 양식
- 정기점검 체크리스트 양식
- 서비스요청 양식 (요청 유형별로 다른 폼)
- 변경관리 신청 양식

### 공통코드
- 장애유형, 우선순위, 상태값, 부서코드 등
- 고객사 부서 목록
- 담당자 카테고리 (보안/DB/네트워크 등)
> 하드코딩 방지 — 항목 추가 시 재배포 불필요

### 알림 / 정책
- SLA 기준값 (우선순위별 처리 기한)
- 알림 발송 조건 (SLA 몇 % 초과 시 알림 등)
- 알림 수신 대상 (역할별)
- 비밀번호 정책 (최소 길이, 특수문자 포함, 만료 기간)

### 시스템 설정
- 공지사항 (로그인 화면 or 메인 화면 노출)
- 시스템 점검 공지 (배너 노출 여부, 점검 시간)

### 게시판 관리 (게시판 빌더)
- 관리자가 게시판 유형을 직접 추가/수정/삭제 가능
- 게시판 생성 시 설정 항목:
  - 게시판 이름, 설명
  - 게시판 유형 (공지형 / 자유형 / 자료형 등)
  - 접근 권한 (어떤 역할이 읽기/쓰기 가능한지)
  - 첨부파일 허용 여부
  - 허용 확장자 (게시판별 개별 설정 — 예: 자료실은 pdf/xlsx 허용, 공지게시판은 이미지만)
  - 파일 최대 용량 (게시판별 개별 설정)
  - 댓글 허용 여부

```
[게시판 관리 화면]
  → 게시판 추가/설정 → DB 저장
                          ↓
          ┌───────────────┴───────────────┐
          ↓                               ↓
[사이드바 메뉴 자동 반영]        [해당 게시판 화면 동적 렌더링]
(메뉴 조회 API → Vue 사이드바)   (설정 조회 → 화면 구성)
```

> 게시판 추가 시 별도 메뉴 등록 작업 불필요 — 메뉴가 자동으로 생성됨 (서비스 중단 최소화 + 확장 가능한 구조 원칙 동시 적용)

---

## 메뉴 구조 (전체)

```
ITSM
├── 대시보드

├── 장애관리
│   ├── 장애 목록
│   ├── 장애 등록
│   └── 장애보고서

├── 서비스요청
│   ├── 요청 목록
│   └── 요청 등록

├── 변경관리
│   ├── 변경 목록
│   └── 변경 등록

├── 자산관리 (CMDB)
│   ├── HW 자산 목록
│   ├── SW 자산 목록
│   └── HW-SW 연관관계

├── 정기점검관리
│   ├── 점검 일정
│   └── 점검 체크리스트

├── 보고관리
│   ├── 보고서 목록
│   └── 보고서 출력 / PDF 다운로드

├── 게시판
│   └── (게시판 빌더로 동적 생성된 게시판 목록 자동 반영)

├── 설정관리
│   ├── 메뉴 / 권한 관리
│   ├── 보고서 양식 관리 (동적 폼 JSON 편집)
│   ├── 공통코드 관리
│   ├── 알림 / 정책 관리
│   │   ├── SLA 기준값
│   │   ├── 알림 발송 조건
│   │   └── 알림 수신 대상
│   ├── 시스템 설정
│   │   ├── 공지사항 관리
│   │   └── 시스템 점검 공지
│   ├── 게시판 관리 (게시판 빌더)
│   └── 조직 관리
│       ├── 회사 관리
│       └── 부서 관리

└── 계정관리
    ├── 사용자 목록
    └── 역할 관리
```

---

## 역할별 메뉴 접근 권한 (RBAC)

> ✅ 접근 가능 / ❌ 접근 불가

| 메뉴 | 슈퍼관리자 | ITSM관리자 | PM | 개발자 | 보안 | DB | 네트워크 | 서버 | 고객사 | 외부사용자 | 감사자 | 비로그인 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 대시보드 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| 장애관리 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| 서비스요청 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| 변경관리 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ |
| 자산관리 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ |
| 정기점검관리 | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ |
| 보고관리 | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ |
| 게시판 | 게시판별 접근 권한 개별 설정 (게시판 빌더에서 역할별 읽기/쓰기 지정) ||||||||||||
| 설정관리 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 계정관리 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

> 세부 조정이 필요한 부분은 추후 수정 가능 (RBAC이므로 역할 단위로 일괄 변경)

**주요 정책 결정사항**

- **고객사 인원**: 변경관리/자산관리 직접 접근 불가. 서비스요청으로 요청 → 유지보수팀이 검토 후 처리하는 흐름으로 진행
- **게시판**: 전체공개 / 유지보수팀 전용 / 고객사 전용 등 게시판 빌더에서 역할별 읽기/쓰기 권한을 게시판 생성 시 개별 지정

---

## 테이블 목록

### 시스템 / 계정
- `tb_user` — 사용자 (현재 유효 정보)
- `tb_user_history` — 사용자 변경 이력 (Temporal Data Modeling)
- `tb_role` — 역할
- `tb_user_role` — 사용자-역할 매핑
- `tb_menu` — 메뉴
- `tb_role_menu` — 역할-메뉴 매핑
- `tb_company` — 회사
- `tb_department` — 부서

### 자산 (CMDB)
- `tb_asset_hw` — HW 자산
- `tb_asset_sw` — SW 자산
- `tb_asset_relation` — HW-SW 연관관계
- `tb_asset_history` — 자산 정보 변경 이력 (자산관리 내 수정 기록, 변경관리와 별개)

### 장애관리
- `tb_incident` — 장애 티켓
- `tb_incident_comment` — 장애 댓글
- `tb_incident_history` — 장애 이력 (상태변경 추적)
- `tb_incident_report` — 장애보고서

### 서비스요청
- `tb_service_request` — 서비스 요청
- `tb_service_request_assignee` — 담당자 지정 (1명 이상)
- `tb_service_request_history` — 요청 이력

### 변경관리
- `tb_change` — 변경 요청
- `tb_change_history` — 변경 이력

### 정기점검
- `tb_inspection` — 점검 일정
- `tb_inspection_item` — 점검 체크리스트 항목
- `tb_inspection_result` — 점검 결과

### 보고관리
- `tb_report_form` — 보고서 양식 (동적 폼 JSON)
- `tb_report` — 생성된 보고서

### 게시판
- `tb_board_config` — 게시판 설정 (빌더로 생성된 게시판 정보)
- `tb_board_post` — 게시글
- `tb_board_comment` — 게시글 댓글
- `tb_board_file` — 첨부파일

### 공통 / 설정
- `tb_common_code` — 공통코드 그룹
- `tb_common_code_detail` — 공통코드 상세
- `tb_notification` — 알림
- `tb_notification_policy` — 알림 발송 정책
- `tb_sla_policy` — SLA 기준값
- `tb_system_config` — 시스템 설정 (공지, 점검 배너 등)

### 로그
- `tb_audit_log` — 시스템 전반 행위 기록 (누가 뭘 바꿨는지)
- `tb_access_log` — 로그인/로그아웃, IP, 시간, 성공여부
- `tb_menu_access_log` — 역할별 메뉴 접근 통계

> 총 **38개 테이블**

---

## 테이블 설계 정책

### Temporal Data Modeling (사용자 / 자산 이력)

이력이 쌓이는 테이블(`tb_user_history`, `tb_asset_history`)은 `valid_from` / `valid_to` 를 `DATETIME` 타입으로 관리.

```
valid_from                valid_to                status
2024-03-15 09:00:00  |  2024-03-15 14:30:00  |  ACTIVE   ← 오전 수정
2024-03-15 14:30:00  |  2024-03-15 17:00:00  |  ACTIVE   ← 오후 수정
2024-03-15 17:00:00  |  NULL                 |  ACTIVE   ← 현재 유효
```

- `valid_to = NULL` → 현재 유효한 데이터
- 날짜가 아닌 **시각(DATETIME)** 으로 관리 → 같은 날 다중 수정 정확하게 구분
- 이력은 절대 삭제하지 않음 → 수정 취소도 새 row 삽입으로 처리

### 사용자 status 정책

| status | 설명 |
|---|---|
| `ACTIVE` | 재직 중 |
| `INACTIVE` | 일시 비활성 (장기휴직 등) |
| `RESIGNED` | 퇴사 |
| `ABSENT` | 퇴사~재입사 사이 부재 기간 |
| `DELETED` | 개인정보 파기 완료 |

**재입사 처리 흐름**
```
user_id | login_id  | status    | valid_from           | valid_to
  1     | hong1234  | RESIGNED  | 2020-03-02 09:00:00  | 2023-12-31 18:00:00
  1     | hong1234  | ABSENT    | 2023-12-31 18:00:00  | 2024-06-01 09:00:00
  1     | hong1234  | ACTIVE    | 2024-06-01 09:00:00  | NULL
```

- `user_id` (PK, auto-increment) 는 절대 재사용 안 됨
- 재입사 시 **권한은 초기화** 후 재부여 (보안)
- `employee_no` (사번) 는 회사 정책에 따라 재사용 가능 — `user_id` 와 별도 컬럼으로 분리

### login_id 재사용 처리

DELETED 처리 시 `login_id` 를 마스킹하여 새 사람이 동일 ID 사용 가능하게 함.

```
user_id | login_id              | status
  1     | DELETED_1_hong1234    | DELETED   ← 파기 시 마스킹
  15    | hong1234              | ACTIVE    ← 새 사람이 동일 ID 사용 가능
```

### 자산 이력 설계 정책

- `changed_field` — 어떤 항목이 바뀌었는지 명시
- `before_value` / `after_value` — 변경 전/후 값 저장
- `batch_job_id` — 일괄 변경 시 같은 작업 묶음 추적 (선택적)

### 엣지케이스 처리 원칙

| 케이스 | 처리 방법 |
|---|---|
| 동시 수정 | 낙관적 락 (Optimistic Lock) |
| 수정 취소 | 이력 삭제 금지, 새 row 삽입으로 처리 |
| 현재 유효 row 중복 | `valid_to IS NULL` row는 1개만 존재하도록 애플리케이션 레벨 검증 |
| 폐기 / 퇴사 | status 컬럼으로 명시적 관리 |
| 개인정보 파기 | login_id 마스킹 + status = DELETED |
| 일괄 변경 추적 | batch_job_id 선택적 저장 |

---

## ERD 설계

### 1. 시스템/계정 영역

#### 논리적 ERD

**관계 구조**
```
COMPANY_HISTORY          DEPARTMENT_HISTORY        USER_HISTORY
      │                         │                       │
      ▼                         ▼                       ▼
   COMPANY ──────────< DEPARTMENT ──────────< USER >── USER_ROLE >── ROLE ──< ROLE_MENU >── MENU
                                                                                                │
                                                                                           (자기참조)
```

- COMPANY 1 : N DEPARTMENT (회사는 여러 부서를 가짐, 단일 depth)
- DEPARTMENT 1 : N USER (부서는 여러 사용자를 가짐)
- USER N : M ROLE → USER_ROLE 중간 테이블
- ROLE N : M MENU → ROLE_MENU 중간 테이블
- MENU 1 : N MENU (상위/하위 메뉴 자기참조)
- COMPANY 1 : N COMPANY_HISTORY (회사 정보 변경 시 이력 적재 — 단순 이력 방식)
- DEPARTMENT 1 : N DEPARTMENT_HISTORY (부서 정보 변경 시 이력 적재 — 단순 이력 방식)
- USER 1 : N USER_HISTORY (사용자 변경 시 이력 적재 — Temporal Data Modeling 방식)

> 이력 방식 구분: USER는 특정 시점 기준 조회가 필요하여 Temporal 방식, COMPANY/DEPARTMENT는 변경 빈도가 낮고 단순 추적 목적이라 이력 테이블 방식 적용

**엔티티 및 속성**

| 엔티티 | 주요 속성 |
|---|---|
| COMPANY | 회사ID, 회사명, 사업자번호, 대표자, 연락처, 상태 |
| COMPANY_HISTORY | 이력ID, 회사ID, 회사명, 사업자번호, 대표자명, 변경항목, 변경전값, 변경후값, 변경일시, 변경자ID |
| DEPARTMENT | 부서ID, 부서명, 회사ID, 상태 |
| DEPARTMENT_HISTORY | 이력ID, 부서ID, 부서명, 소속회사ID, 변경항목, 변경전값, 변경후값, 변경일시, 변경자ID |
| USER | 사용자ID, 로그인ID, 비밀번호, 이름, 사번, 부서ID, 이메일, 연락처, 상태, valid_from, valid_to |
| USER_HISTORY | 이력ID, 사용자ID, 로그인ID, 이름, 사번, 부서ID, 이메일, 연락처, 상태, 변경항목, 변경전값, 변경후값, valid_from, valid_to, 배치작업ID, 변경자ID |
| ROLE | 역할ID, 역할명, 역할코드, 설명, 상태 |
| USER_ROLE | 사용자ID, 역할ID, 부여일시, 부여자ID |
| MENU | 메뉴ID, 상위메뉴ID, 메뉴명, URL, 아이콘, 순서, 노출여부, 상태 |
| ROLE_MENU | 역할ID, 메뉴ID, 읽기여부, 쓰기여부, 등록일시 |

---

#### 물리적 ERD

**tb_company**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| company_id | BIGINT | PK, AUTO_INCREMENT | 회사ID |
| company_nm | VARCHAR(100) | NOT NULL | 회사명 |
| biz_no | VARCHAR(20) | UNIQUE | 사업자번호 |
| ceo_nm | VARCHAR(50) | | 대표자명 |
| tel | VARCHAR(20) | | 연락처 |
| default_pm_id | BIGINT | FK(tb_user) | 대표 PM (담당자 공백 시 자동 배정) |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 상태 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자 |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자 |

**tb_department**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| dept_id | BIGINT | PK, AUTO_INCREMENT | 부서ID |
| dept_nm | VARCHAR(100) | NOT NULL | 부서명 |
| company_id | BIGINT | FK(tb_company), NOT NULL | 회사ID |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 상태 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자 |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자 |

**tb_user**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| user_id | BIGINT | PK, AUTO_INCREMENT | 사용자ID (절대 재사용 안 됨) |
| login_id | VARCHAR(50) | UNIQUE | 로그인ID (DELETED 시 마스킹) |
| password | VARCHAR(255) | NOT NULL | 비밀번호 (BCrypt 암호화) |
| user_nm | VARCHAR(50) | NOT NULL | 이름 |
| employee_no | VARCHAR(30) | | 사번 (재사용 가능) |
| dept_id | BIGINT | FK(tb_department) | 부서ID |
| email | VARCHAR(100) | | 이메일 |
| tel | VARCHAR(20) | | 연락처 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | ACTIVE / INACTIVE / RESIGNED / ABSENT / DELETED |
| valid_from | DATETIME | NOT NULL | 유효 시작일시 |
| valid_to | DATETIME | | 유효 종료일시 (NULL = 현재 유효) |
| last_login_at | DATETIME | | 마지막 로그인 일시 |
| pwd_changed_at | DATETIME | | 비밀번호 변경일시 |
| login_fail_cnt | INT | NOT NULL, DEFAULT 0 | 로그인 실패 횟수 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자 |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자 |

**tb_user_history**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| history_id | BIGINT | PK, AUTO_INCREMENT | 이력ID |
| user_id | BIGINT | FK(tb_user), NOT NULL | 사용자ID |
| login_id | VARCHAR(50) | NOT NULL | 변경 시점 로그인ID |
| user_nm | VARCHAR(50) | NOT NULL | 변경 시점 이름 |
| employee_no | VARCHAR(30) | | 변경 시점 사번 |
| dept_id | BIGINT | | 변경 시점 부서ID |
| email | VARCHAR(100) | | 변경 시점 이메일 |
| tel | VARCHAR(20) | | 변경 시점 연락처 |
| status | VARCHAR(20) | NOT NULL | 변경 시점 상태 |
| changed_field | VARCHAR(100) | | 변경된 항목명 |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| valid_from | DATETIME | NOT NULL | 유효 시작일시 |
| valid_to | DATETIME | | 유효 종료일시 |
| batch_job_id | VARCHAR(50) | | 일괄변경 시 배치작업ID |
| created_at | DATETIME | NOT NULL | 이력 생성일시 |
| created_by | BIGINT | FK(tb_user) | 변경자ID |

**tb_role**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| role_id | BIGINT | PK, AUTO_INCREMENT | 역할ID |
| role_nm | VARCHAR(50) | NOT NULL | 역할명 |
| role_cd | VARCHAR(50) | UNIQUE, NOT NULL | 역할코드 (ex. ROLE_ADMIN) |
| description | VARCHAR(200) | | 설명 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 상태 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자 |

**tb_user_role**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| user_id | BIGINT | PK, FK(tb_user) | 사용자ID |
| role_id | BIGINT | PK, FK(tb_role) | 역할ID |
| granted_at | DATETIME | NOT NULL | 부여일시 |
| granted_by | BIGINT | FK(tb_user) | 부여자ID |

**tb_menu**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| menu_id | BIGINT | PK, AUTO_INCREMENT | 메뉴ID |
| parent_menu_id | BIGINT | FK(tb_menu) | 상위메뉴ID (NULL = 최상위) |
| menu_nm | VARCHAR(100) | NOT NULL | 메뉴명 |
| menu_url | VARCHAR(200) | | URL |
| icon | VARCHAR(50) | | 아이콘 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 정렬순서 |
| is_visible | CHAR(1) | NOT NULL, DEFAULT 'Y' | 노출여부 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | 상태 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자 |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자 |

**tb_role_menu**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| role_id | BIGINT | PK, FK(tb_role) | 역할ID |
| menu_id | BIGINT | PK, FK(tb_menu) | 메뉴ID |
| can_read | CHAR(1) | NOT NULL, DEFAULT 'Y' | 읽기 권한 |
| can_write | CHAR(1) | NOT NULL, DEFAULT 'N' | 쓰기 권한 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자 |

**tb_company_history**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| history_id | BIGINT | PK, AUTO_INCREMENT | 이력ID |
| company_id | BIGINT | FK(tb_company), NOT NULL | 회사ID |
| company_nm | VARCHAR(100) | NOT NULL | 변경 전 회사명 |
| biz_no | VARCHAR(20) | | 변경 전 사업자번호 |
| ceo_nm | VARCHAR(50) | | 변경 전 대표자명 |
| changed_field | VARCHAR(100) | | 변경된 항목명 |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| created_at | DATETIME | NOT NULL | 이력 생성일시 |
| created_by | BIGINT | FK(tb_user) | 변경자ID |

**tb_department_history**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| history_id | BIGINT | PK, AUTO_INCREMENT | 이력ID |
| dept_id | BIGINT | FK(tb_department), NOT NULL | 부서ID |
| dept_nm | VARCHAR(100) | NOT NULL | 변경 전 부서명 |
| company_id | BIGINT | | 변경 전 소속 회사ID |
| changed_field | VARCHAR(100) | | 변경된 항목명 |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| created_at | DATETIME | NOT NULL | 이력 생성일시 |
| created_by | BIGINT | FK(tb_user) | 변경자ID |

> 테이블 총 **40개**로 업데이트

---

### 2. 자산(CMDB) 영역

#### 논리적 ERD

**관계 구조**
```
COMPANY
  └──< ASSET_HW >── ASSET_RELATION ──< ASSET_SW
          │                                │
          └──< ASSET_HW_HISTORY     ASSET_SW_HISTORY
```

- ASSET_HW N : M ASSET_SW → ASSET_RELATION 중간 테이블 (HW 위에 어떤 SW가 설치됐는지)
- ASSET_HW 1 : N ASSET_HW_HISTORY (HW 자산 정보 변경 시 이력 적재)
- ASSET_SW 1 : N ASSET_SW_HISTORY (SW 자산 정보 변경 시 이력 적재)
- ASSET_HW, ASSET_SW 모두 COMPANY 와 연결 (어느 고객사 자산인지)

**엔티티 및 속성**

| 엔티티 | 주요 속성 |
|---|---|
| ASSET_HW | 자산ID, 자산명, 자산유형, 제조사, 모델명, 시리얼번호, IP, 설치위치, 도입일, 회사ID, 담당자ID, 상태 |
| ASSET_SW | 자산ID, 소프트웨어명, 버전, 라이선스키, 라이선스수, 설치일, 만료일, 회사ID, 담당자ID, 상태 |
| ASSET_RELATION | HW자산ID, SW자산ID, 설치일, 삭제일, 등록자ID |
| ASSET_HW_HISTORY | 이력ID, HW자산ID, 변경항목, 변경전값, 변경후값, 변경일시, 변경자ID, 배치작업ID |
| ASSET_SW_HISTORY | 이력ID, SW자산ID, 변경항목, 변경전값, 변경후값, 변경일시, 변경자ID, 배치작업ID |

---

#### 물리적 ERD

**tb_asset_hw**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| asset_hw_id | BIGINT | PK, AUTO_INCREMENT | HW자산ID |
| asset_nm | VARCHAR(100) | NOT NULL | 자산명 |
| asset_type_cd | VARCHAR(50) | NOT NULL | 자산유형코드 (공통코드 — 서버/네트워크/PC 등) |
| manufacturer | VARCHAR(100) | | 제조사 |
| model_nm | VARCHAR(100) | | 모델명 |
| serial_no | VARCHAR(100) | UNIQUE | 시리얼번호 |
| ip_address | VARCHAR(50) | | IP주소 |
| mac_address | VARCHAR(50) | | MAC주소 |
| location | VARCHAR(200) | | 설치위치 (랙/층/건물 등) |
| introduced_at | DATE | | 도입일 |
| warranty_end_at | DATE | | 유지보수 만료일 |
| company_id | BIGINT | FK(tb_company), NOT NULL | 소속 고객사ID |
| manager_id | BIGINT | FK(tb_user) | 담당자ID |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | ACTIVE / INACTIVE / DISPOSED |
| description | TEXT | | 비고 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자 |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자 |

**tb_asset_sw**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| asset_sw_id | BIGINT | PK, AUTO_INCREMENT | SW자산ID |
| sw_nm | VARCHAR(100) | NOT NULL | 소프트웨어명 |
| sw_type_cd | VARCHAR(50) | NOT NULL | SW유형코드 (공통코드 — OS/WAS/DB/보안 등) |
| version | VARCHAR(50) | | 버전 |
| license_key | VARCHAR(200) | | 라이선스키 |
| license_cnt | INT | | 라이선스 수량 |
| installed_at | DATE | | 설치일 |
| expired_at | DATE | | 라이선스 만료일 |
| company_id | BIGINT | FK(tb_company), NOT NULL | 소속 고객사ID |
| manager_id | BIGINT | FK(tb_user) | 담당자ID |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | ACTIVE / INACTIVE / DISPOSED |
| description | TEXT | | 비고 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자 |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자 |

**tb_asset_relation**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| asset_hw_id | BIGINT | PK, FK(tb_asset_hw) | HW자산ID |
| asset_sw_id | BIGINT | PK, FK(tb_asset_sw) | SW자산ID |
| installed_at | DATE | | 설치일 |
| removed_at | DATE | | 삭제일 (NULL = 현재 설치 중) |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자 |

**tb_asset_hw_history**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| history_id | BIGINT | PK, AUTO_INCREMENT | 이력ID |
| asset_hw_id | BIGINT | FK(tb_asset_hw), NOT NULL | HW자산ID |
| changed_field | VARCHAR(100) | NOT NULL | 변경된 항목명 |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| batch_job_id | VARCHAR(50) | | 일괄변경 시 배치작업ID |
| created_at | DATETIME | NOT NULL | 이력 생성일시 |
| created_by | BIGINT | FK(tb_user) | 변경자ID |

**tb_asset_sw_history**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| history_id | BIGINT | PK, AUTO_INCREMENT | 이력ID |
| asset_sw_id | BIGINT | FK(tb_asset_sw), NOT NULL | SW자산ID |
| changed_field | VARCHAR(100) | NOT NULL | 변경된 항목명 |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| batch_job_id | VARCHAR(50) | | 일괄변경 시 배치작업ID |
| created_at | DATETIME | NOT NULL | 이력 생성일시 |
| created_by | BIGINT | FK(tb_user) | 변경자ID |

---

### 3. 장애관리 영역

#### 논리적 ERD

**관계 구조**
```
ASSET_HW ──┐
           ├──< INCIDENT >── INCIDENT_ASSIGNEE >── USER
ASSET_SW ──┘       │
                   ├──< INCIDENT_COMMENT
                   ├──< INCIDENT_HISTORY
                   └──── INCIDENT_REPORT
```

- INCIDENT N : M ASSET_HW, ASSET_SW (장애 하나에 여러 자산 연관 가능, 자산 하나에 여러 장애 발생 가능)
- INCIDENT 1 : N INCIDENT_ASSIGNEE (담당자 복수 지정, 별도 테이블)
- INCIDENT 1 : N INCIDENT_COMMENT (댓글 여러 개)
- INCIDENT 1 : N INCIDENT_HISTORY (상태 변경 등 모든 변경 이력)
- INCIDENT 1 : 1 INCIDENT_REPORT (장애보고서는 장애 당 1개)

**상태머신**
```
접수 → 처리중 → 완료 → 종료
 │                      ↑
 └──── 반려 ────────────┘
```
- 완료: 주담당자가 처리내역 작성 후 완료 처리
- 종료: ITSM관리자 또는 PM이 최종 확인 후 종료
- 반려: 처리내역 미흡 시 다시 처리중으로 되돌림

**수정 권한 정책**
- 장애 내용, 처리내용, 장애보고서는 **작성한 본인만 수정 가능**
- 담당자 지정, 상태 변경은 PM / ITSM관리자 가능
- 수정 시 `tb_incident_history` 에 이력 자동 적재

**엔티티 및 속성**

| 엔티티 | 주요 속성 |
|---|---|
| INCIDENT | 장애ID, 제목, 내용, 장애유형, 우선순위, 상태, 발생일시, 완료일시, 등록자ID, 주담당자ID, 회사ID |
| INCIDENT_ASSET | 장애ID, 자산유형(HW/SW), 자산ID (연관 자산 매핑) |
| INCIDENT_ASSIGNEE | 장애ID, 사용자ID, 지정일시, 지정자ID |
| INCIDENT_COMMENT | 댓글ID, 장애ID, 내용, 작성자ID, 작성일시 |
| INCIDENT_HISTORY | 이력ID, 장애ID, 변경항목, 변경전값, 변경후값, 변경일시, 변경자ID |
| INCIDENT_REPORT | 보고서ID, 장애ID, 보고서양식ID, 보고서내용(JSON), 작성자ID, 작성일시 |

---

#### 물리적 ERD

**tb_incident**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| incident_id | BIGINT | PK, AUTO_INCREMENT | 장애ID |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 장애 내용 |
| incident_type_cd | VARCHAR(50) | NOT NULL | 장애유형코드 (공통코드) |
| priority_cd | VARCHAR(20) | NOT NULL | 우선순위코드 (공통코드 — CRITICAL/HIGH/MEDIUM/LOW) |
| status_cd | VARCHAR(20) | NOT NULL, DEFAULT 'RECEIVED' | 상태 (RECEIVED/IN_PROGRESS/COMPLETED/CLOSED/REJECTED) |
| occurred_at | DATETIME | NOT NULL | 장애 발생일시 |
| completed_at | DATETIME | | 처리 완료일시 |
| closed_at | DATETIME | | 최종 종료일시 |
| sla_deadline_at | DATETIME | | SLA 처리 기한 |
| company_id | BIGINT | FK(tb_company), NOT NULL | 고객사ID |
| main_manager_id | BIGINT | FK(tb_user) | 주담당자ID |
| process_content | TEXT | | 처리내용 (주담당자 작성) |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 등록자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_incident_asset**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| incident_id | BIGINT | PK, FK(tb_incident) | 장애ID |
| asset_type | VARCHAR(10) | PK, NOT NULL | 자산유형 (HW / SW) |
| asset_id | BIGINT | PK, NOT NULL | 자산ID (asset_type에 따라 HW or SW) |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자ID |

**tb_incident_assignee**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| incident_id | BIGINT | PK, FK(tb_incident) | 장애ID |
| user_id | BIGINT | PK, FK(tb_user) | 담당자ID |
| granted_at | DATETIME | NOT NULL | 지정일시 |
| granted_by | BIGINT | FK(tb_user) | 지정자ID |

**tb_incident_comment**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| comment_id | BIGINT | PK, AUTO_INCREMENT | 댓글ID |
| incident_id | BIGINT | FK(tb_incident), NOT NULL | 장애ID |
| content | TEXT | NOT NULL | 댓글 내용 |
| created_at | DATETIME | NOT NULL | 작성일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 작성자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_incident_history**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| history_id | BIGINT | PK, AUTO_INCREMENT | 이력ID |
| incident_id | BIGINT | FK(tb_incident), NOT NULL | 장애ID |
| changed_field | VARCHAR(100) | NOT NULL | 변경된 항목명 |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| created_at | DATETIME | NOT NULL | 변경일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 변경자ID |

**tb_incident_report**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| report_id | BIGINT | PK, AUTO_INCREMENT | 보고서ID |
| incident_id | BIGINT | FK(tb_incident), NOT NULL, UNIQUE | 장애ID (장애당 1개) |
| report_form_id | BIGINT | FK(tb_report_form), NOT NULL | 보고서양식ID (동적 폼) |
| report_content | JSON | NOT NULL | 보고서 내용 (양식 기반 JSON) |
| created_at | DATETIME | NOT NULL | 작성일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 작성자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

---

### 4. 서비스요청 영역

#### 논리적 ERD

**관계 구조**
```
ASSET_HW ──┐
           ├──< SERVICE_REQUEST >── SERVICE_REQUEST_ASSIGNEE >── USER
ASSET_SW ──┘          │
                      ├──< SERVICE_REQUEST_PROCESS (담당자별 처리내역)
                      └──< SERVICE_REQUEST_HISTORY
```

- SERVICE_REQUEST N : M ASSET_HW, ASSET_SW (요청 관련 자산 연관, 선택적)
- SERVICE_REQUEST 1 : N SERVICE_REQUEST_ASSIGNEE (담당자 복수 지정)
- SERVICE_REQUEST 1 : N SERVICE_REQUEST_PROCESS (담당자별 처리내역 각각 작성)
- SERVICE_REQUEST 1 : N SERVICE_REQUEST_HISTORY (상태 변경 이력)

**상태머신**
```
접수 ←─────────────────────────────────┐
 │                                      │ (담당자 전원 제거 시 자동)
 ↓                                      │
담당자배정 → 처리중 → 완료대기 → 종료
                ↑         │
                └─────────┘
                  반려 (PM)
 └── 요청취소 (접수 상태에서만 요청자 가능 → 이후 read only)
```

- **접수**: 요청 등록 직후. 요청자 내용/첨부파일 언제든 수정 가능. 담당자 지정 가능
- **담당자배정**: 담당자 1명 이상 지정된 상태. 담당자 0명이 되면 저장 불가 ("최소 1명의 담당자가 필요합니다" alert)
- **처리중**: 모든 담당자 수락 후 자동 전환. 담당자 변경 불가. 담당자별 처리내용 작성/수정 가능
- **완료대기**: 모든 담당자 `process_status = COMPLETED` 시 자동 전환. 화면에 N/N 완료 카운트 표시. PM 종료 버튼 활성화
- **종료**: PM 최종 승인 → 요청자 종료 알림 + 만족도 조사 발송. 이후 전체 read only
- **요청취소**: 접수 상태에서 요청자만 가능. 취소 후 종료와 동일하게 read only. 재활성화 불가, 재요청 시 신규 등록
- **반려**: PM → 처리중으로 되돌림. 반려 횟수 화면 표시. 횟수 제한 없음, PM이 직접 조치

---

**서비스요청 엣지케이스 처리 정책**

**담당자 관련**

| 케이스 | 처리 방법 |
|---|---|
| 담당자 퇴사/비활성화 (접수/담당자배정) | 해당 담당자 자동 제거 + 고객사 대표 PM(`tb_company.default_pm_id`) 자동 배정 + PM 알림 |
| 담당자 퇴사/비활성화 (처리중) | 처리내용 유지, 비활성 표시 + PM 알림 → PM 수동 조치 (최소 1명 유지 원칙) |
| 담당자 퇴사/비활성화 (완료대기/종료) | 완료된 건이므로 이력 유지, 비활성 표시만 |
| 본인이 요청한 건에 본인 담당자 지정 | 허용 (처리 후 등록으로 간주) |
| 담당자 최소 인원 미달 | 담당자 0명 시 저장 불가, alert 처리 |
| 처리중 담당자 교체 | 시스템상 불가 (UI 차단 + API 레벨 검증) |
| 동일 인원 중복 지정 | DB UNIQUE 제약 + 화면단 중복 방지 |

**상태 전환 관련**

| 케이스 | 처리 방법 |
|---|---|
| PM 종료 버튼 활성화 시점 | 담당자 전원 `COMPLETED` → 완료대기 자동 전환, PM 종료 버튼 활성화. 화면에 N/N 완료 카운트 표시 |
| 반려 반복 | 반려 횟수 화면 표시. 횟수 제한 없음, PM이 직접 판단해서 조치 |
| 담당자 전원 제거 시 | 담당자배정 → 접수로 자동 되돌림 |

**요청 내용 관련**

| 케이스 | 처리 방법 |
|---|---|
| 접수 상태 요청자 수정 | 담당자 지정 여부와 무관하게 언제든 수정 가능 |
| 중복 요청 감지 | 등록 버튼 3초 내 중복 클릭 방지 (debounce). 제목 중복 시 경고 표시 (차단 아님) |
| 첨부파일 수정 | 해당 상태에서 내용 수정 권한이 있는 인원만 수정 가능 |

**SLA 관련**

| 케이스 | 처리 방법 |
|---|---|
| SLA 기산점 | 요청일 기준 (고객사 관점 — 얼마나 처리가 안 되고 있는지) |
| 반려 시 SLA 재계산 | 반려 시간만큼 SLA 기한 연장 (`sla_deadline_at` 업데이트) |
| SLA 초과 감지 | Spring Batch로 초과 건 감지 → PM/ITSM관리자 에스컬레이션 알림 발송. 자동 처리 없음 |

**알림 관련**

| 케이스 | 처리 방법 |
|---|---|
| 알림 수신 확인 | `tb_notification` 에 `read_at` 컬럼으로 읽음 처리 |
| 완료대기 장기 유지 | Spring Batch로 2일 초과 건 감지 → PM/ITSM관리자 에스컬레이션 알림만 발송. 자동 상태 변경 없음 |

> **알림 범위 정의**: 시스템 내부 알림 (화면 우측 상단 알림 아이콘). `tb_notification` 에 적재 후 읽음/안읽음 처리. SMS/이메일/카카오 등 외부 알림은 포트폴리오 범위 제외

**퇴사/비활성화 담당자 통계 처리 정책**

- `user_id` 는 절대 삭제되지 않으므로 `tb_service_request_process` 의 처리내역은 그대로 유지
- 통계 집계 시 퇴사/비활성 사용자 포함하여 정상 집계
- 화면 표시 시 비활성 사용자 이름 옆 **(비활성)** 표시로 구분

**엔티티 및 속성**

| 엔티티 | 주요 속성 |
|---|---|
| SERVICE_REQUEST | 요청ID, 제목, 요청내용, 요청유형, 우선순위, 상태, 요청일시, 완료일시, 등록자ID, 회사ID, 만족도점수 |
| SERVICE_REQUEST_ASSET | 요청ID, 자산유형(HW/SW), 자산ID |
| SERVICE_REQUEST_ASSIGNEE | 요청ID, 사용자ID, 처리상태, 지정일시, 지정자ID |
| SERVICE_REQUEST_PROCESS | 처리ID, 요청ID, 담당자ID, 처리내용, 완료여부, 작성일시 |
| SERVICE_REQUEST_HISTORY | 이력ID, 요청ID, 변경항목, 변경전값, 변경후값, 변경일시, 변경자ID |

---

#### 물리적 ERD

**tb_service_request**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| request_id | BIGINT | PK, AUTO_INCREMENT | 요청ID |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 요청 내용 |
| request_type_cd | VARCHAR(50) | NOT NULL | 요청유형코드 (공통코드) |
| priority_cd | VARCHAR(20) | NOT NULL | 우선순위코드 (공통코드) |
| status_cd | VARCHAR(20) | NOT NULL, DEFAULT 'RECEIVED' | 상태 (RECEIVED/ASSIGNED/IN_PROGRESS/PENDING_COMPLETE/CLOSED/CANCELLED/REJECTED) |
| occurred_at | DATETIME | NOT NULL | 요청일시 |
| completed_at | DATETIME | | 완료일시 |
| closed_at | DATETIME | | 종료일시 |
| sla_deadline_at | DATETIME | | SLA 처리 기한 (요청일 기준, 반려 시 연장) |
| reject_cnt | INT | NOT NULL, DEFAULT 0 | 반려 횟수 (화면 표시용) |
| company_id | BIGINT | FK(tb_company), NOT NULL | 고객사ID |
| satisfaction_score | TINYINT | | 만족도 점수 (1~5) |
| satisfaction_comment | VARCHAR(500) | | 만족도 의견 |
| satisfaction_submitted_at | DATETIME | | 만족도 제출일시 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 등록자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_service_request_asset**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| request_id | BIGINT | PK, FK(tb_service_request) | 요청ID |
| asset_type | VARCHAR(10) | PK, NOT NULL | 자산유형 (HW / SW) |
| asset_id | BIGINT | PK, NOT NULL | 자산ID |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자ID |

**tb_service_request_assignee**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| request_id | BIGINT | PK, FK(tb_service_request) | 요청ID |
| user_id | BIGINT | PK, FK(tb_user) | 담당자ID |
| process_status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 처리상태 (PENDING/IN_PROGRESS/COMPLETED) |
| granted_at | DATETIME | NOT NULL | 지정일시 |
| granted_by | BIGINT | FK(tb_user) | 지정자ID |

**tb_service_request_process**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| process_id | BIGINT | PK, AUTO_INCREMENT | 처리ID |
| request_id | BIGINT | FK(tb_service_request), NOT NULL | 요청ID |
| user_id | BIGINT | FK(tb_user), NOT NULL | 담당자ID |
| process_content | TEXT | NOT NULL | 처리내용 |
| is_completed | CHAR(1) | NOT NULL, DEFAULT 'N' | 완료여부 |
| completed_at | DATETIME | | 완료일시 |
| created_at | DATETIME | NOT NULL | 작성일시 |
| updated_at | DATETIME | | 수정일시 |

**tb_service_request_history**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| history_id | BIGINT | PK, AUTO_INCREMENT | 이력ID |
| request_id | BIGINT | FK(tb_service_request), NOT NULL | 요청ID |
| changed_field | VARCHAR(100) | NOT NULL | 변경된 항목명 |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| created_at | DATETIME | NOT NULL | 변경일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 변경자ID |

---

### 5. 변경관리 영역

#### 논리적 ERD

**관계 구조**
```
ASSET_HW ──┐
           ├──< CHANGE >── CHANGE_APPROVER >── USER
ASSET_SW ──┘      │
                  ├──< CHANGE_HISTORY
                  └──< CHANGE_COMMENT
```

- CHANGE N : M ASSET_HW, ASSET_SW → CHANGE_ASSET 중간 테이블 (변경 대상 자산)
- CHANGE 1 : N CHANGE_APPROVER (승인자 지정, 단계별 승인 가능)
- CHANGE 1 : N CHANGE_HISTORY (상태 변경 이력)
- CHANGE 1 : N CHANGE_COMMENT (댓글)

**상태머신**
```
초안 → 승인요청 → 승인완료 → 실행중 → 완료 → 종료
          │                              │
          └───── 반려 ──────────────────┘
                 (승인자)
```
- **초안**: 변경 요청 작성 중. 등록자만 수정 가능
- **승인요청**: PM or 등록자가 승인 요청. 수정 불가
- **승인완료**: 승인자 전원 승인 시 자동 전환
- **실행중**: 실제 변경 작업 진행 중. 담당자가 진행상황 코멘트 작성
- **완료**: 담당자가 작업 완료 처리
- **종료**: PM 최종 확인
- **반려**: 승인자가 반려 → 초안으로 되돌림. 등록자 재수정 후 재승인 요청 가능

**엔티티 및 속성**

| 엔티티 | 주요 속성 |
|---|---|
| CHANGE | 변경ID, 제목, 변경내용, 변경유형, 우선순위, 상태, 변경예정일시, 롤백계획, 등록자ID, 회사ID |
| CHANGE_ASSET | 변경ID, 자산유형(HW/SW), 자산ID |
| CHANGE_APPROVER | 변경ID, 사용자ID, 승인순서, 승인상태, 승인일시, 의견 |
| CHANGE_HISTORY | 이력ID, 변경ID, 변경항목, 변경전값, 변경후값, 변경일시, 변경자ID |
| CHANGE_COMMENT | 댓글ID, 변경ID, 내용, 작성자ID, 작성일시 |

---

#### 물리적 ERD

**tb_change**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| change_id | BIGINT | PK, AUTO_INCREMENT | 변경ID |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 변경 내용 |
| change_type_cd | VARCHAR(50) | NOT NULL | 변경유형코드 (공통코드 — 긴급/일반/정기 등) |
| priority_cd | VARCHAR(20) | NOT NULL | 우선순위코드 (공통코드) |
| status_cd | VARCHAR(20) | NOT NULL, DEFAULT 'DRAFT' | 상태 (DRAFT/APPROVAL_REQUESTED/APPROVED/IN_PROGRESS/COMPLETED/CLOSED/REJECTED) |
| scheduled_at | DATETIME | | 변경 예정일시 |
| rollback_plan | TEXT | | 롤백 계획 |
| company_id | BIGINT | FK(tb_company), NOT NULL | 고객사ID |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 등록자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_change_asset**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| change_id | BIGINT | PK, FK(tb_change) | 변경ID |
| asset_type | VARCHAR(10) | PK, NOT NULL | 자산유형 (HW / SW) |
| asset_id | BIGINT | PK, NOT NULL | 자산ID |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자ID |

**tb_change_approver**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| change_id | BIGINT | PK, FK(tb_change) | 변경ID |
| user_id | BIGINT | PK, FK(tb_user) | 승인자ID |
| approve_order | INT | NOT NULL | 승인 순서 (1부터 순차 승인) |
| approve_status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 승인상태 (PENDING/APPROVED/REJECTED) |
| approved_at | DATETIME | | 승인/반려 일시 |
| comment | VARCHAR(500) | | 승인 의견 |

**tb_change_history**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| history_id | BIGINT | PK, AUTO_INCREMENT | 이력ID |
| change_id | BIGINT | FK(tb_change), NOT NULL | 변경ID |
| changed_field | VARCHAR(100) | NOT NULL | 변경된 항목명 |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| created_at | DATETIME | NOT NULL | 변경일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 변경자ID |

**tb_change_comment**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| comment_id | BIGINT | PK, AUTO_INCREMENT | 댓글ID |
| change_id | BIGINT | FK(tb_change), NOT NULL | 변경ID |
| content | TEXT | NOT NULL | 댓글 내용 |
| created_at | DATETIME | NOT NULL | 작성일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 작성자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

---

### 6. 정기점검 / 보고관리 영역

#### 정기점검 논리적 ERD

**관계 구조**
```
ASSET_HW ──┐
           ├──< INSPECTION >── INSPECTION_ITEM
ASSET_SW ──┘       │                │
                   │                └──< INSPECTION_RESULT
                   └──< INSPECTION_HISTORY
```

- INSPECTION N : M ASSET_HW, ASSET_SW → INSPECTION_ASSET 중간 테이블
- INSPECTION 1 : N INSPECTION_ITEM (점검 체크리스트 항목)
- INSPECTION_ITEM 1 : N INSPECTION_RESULT (항목별 점검 결과)
- INSPECTION 1 : N INSPECTION_HISTORY (상태 변경 이력)

**상태머신**
```
예정 → 진행중 → 완료 → 종료
        │
        └── 보류 (외부사용자 부재 등)
```
- **예정**: 점검 일정 등록. PM/ITSM관리자가 등록
- **진행중**: 점검 시작. 담당자(외부사용자 포함)가 체크리스트 항목별 결과 입력
- **완료**: 모든 체크리스트 항목 입력 완료
- **종료**: PM 최종 확인
- **보류**: 점검 진행 불가 사유 발생 시 (일정 재조율)

**엔티티 및 속성**

| 엔티티 | 주요 속성 |
|---|---|
| INSPECTION | 점검ID, 제목, 점검유형, 상태, 점검예정일, 점검완료일, 담당자ID, 회사ID |
| INSPECTION_ASSET | 점검ID, 자산유형, 자산ID |
| INSPECTION_ITEM | 항목ID, 점검ID, 항목명, 항목순서, 필수여부 |
| INSPECTION_RESULT | 결과ID, 항목ID, 점검ID, 결과값, 정상여부, 비고, 작성자ID, 작성일시 |
| INSPECTION_HISTORY | 이력ID, 점검ID, 변경항목, 변경전값, 변경후값, 변경일시, 변경자ID |

---

#### 정기점검 물리적 ERD

**tb_inspection**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| inspection_id | BIGINT | PK, AUTO_INCREMENT | 점검ID |
| title | VARCHAR(200) | NOT NULL | 점검 제목 |
| inspection_type_cd | VARCHAR(50) | NOT NULL | 점검유형코드 (공통코드 — 월간/분기/연간 등) |
| status_cd | VARCHAR(20) | NOT NULL, DEFAULT 'SCHEDULED' | 상태 (SCHEDULED/IN_PROGRESS/COMPLETED/CLOSED/ON_HOLD) |
| scheduled_at | DATE | NOT NULL | 점검 예정일 |
| completed_at | DATETIME | | 점검 완료일시 |
| closed_at | DATETIME | | 종료일시 |
| company_id | BIGINT | FK(tb_company), NOT NULL | 고객사ID |
| manager_id | BIGINT | FK(tb_user) | 담당자ID |
| description | TEXT | | 점검 설명 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 등록자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_inspection_asset**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| inspection_id | BIGINT | PK, FK(tb_inspection) | 점검ID |
| asset_type | VARCHAR(10) | PK, NOT NULL | 자산유형 (HW / SW) |
| asset_id | BIGINT | PK, NOT NULL | 자산ID |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자ID |

**tb_inspection_item**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| item_id | BIGINT | PK, AUTO_INCREMENT | 항목ID |
| inspection_id | BIGINT | FK(tb_inspection), NOT NULL | 점검ID |
| item_nm | VARCHAR(200) | NOT NULL | 항목명 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 항목 순서 |
| is_required | CHAR(1) | NOT NULL, DEFAULT 'Y' | 필수 여부 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 등록자ID |

**tb_inspection_result**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| result_id | BIGINT | PK, AUTO_INCREMENT | 결과ID |
| inspection_id | BIGINT | FK(tb_inspection), NOT NULL | 점검ID |
| item_id | BIGINT | FK(tb_inspection_item), NOT NULL | 항목ID |
| result_value | TEXT | | 결과값 |
| is_normal | CHAR(1) | NOT NULL, DEFAULT 'Y' | 정상여부 (Y/N) |
| remark | VARCHAR(500) | | 비고 |
| created_at | DATETIME | NOT NULL | 작성일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 작성자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_inspection_history**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| history_id | BIGINT | PK, AUTO_INCREMENT | 이력ID |
| inspection_id | BIGINT | FK(tb_inspection), NOT NULL | 점검ID |
| changed_field | VARCHAR(100) | NOT NULL | 변경된 항목명 |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| created_at | DATETIME | NOT NULL | 변경일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 변경자ID |

---

#### 보고관리 논리적 ERD

**관계 구조**
```
REPORT_FORM ──< REPORT
                  │
                  └── (장애/서비스요청/변경/점검과 선택적 연결)
```

- REPORT_FORM 1 : N REPORT (양식 하나에 여러 보고서 생성 가능)
- REPORT 는 장애/서비스요청/변경/점검 중 하나와 선택적으로 연결 (ref_type + ref_id 방식)

**엔티티 및 속성**

| 엔티티 | 주요 속성 |
|---|---|
| REPORT_FORM | 양식ID, 양식명, 양식유형, 양식구조(JSON), 사용여부, 등록자ID |
| REPORT | 보고서ID, 양식ID, 참조유형, 참조ID, 보고서내용(JSON), 작성자ID, 작성일시 |

---

#### 보고관리 물리적 ERD

**tb_report_form**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| form_id | BIGINT | PK, AUTO_INCREMENT | 양식ID |
| form_nm | VARCHAR(100) | NOT NULL | 양식명 |
| form_type_cd | VARCHAR(50) | NOT NULL | 양식유형코드 (공통코드 — 장애/서비스요청/변경/점검 등) |
| form_schema | JSON | NOT NULL | 양식 구조 JSON (동적 폼 정의) |
| is_active | CHAR(1) | NOT NULL, DEFAULT 'Y' | 사용여부 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 등록자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_report**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| report_id | BIGINT | PK, AUTO_INCREMENT | 보고서ID |
| form_id | BIGINT | FK(tb_report_form), NOT NULL | 양식ID |
| ref_type | VARCHAR(20) | NOT NULL | 참조유형 (INCIDENT/SERVICE_REQUEST/CHANGE/INSPECTION) |
| ref_id | BIGINT | NOT NULL | 참조ID (ref_type에 따라 해당 테이블 PK) |
| report_content | JSON | NOT NULL | 보고서 내용 (작성 당시 JSON 스냅샷) |
| created_at | DATETIME | NOT NULL | 작성일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 작성자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

---

### 7. 게시판 / 공통 / 로그 영역

#### 게시판 논리적 ERD

**관계 구조**
```
BOARD_CONFIG ──< BOARD_POST ──< BOARD_COMMENT
                    │
                    └──< BOARD_FILE
```

- BOARD_CONFIG 1 : N BOARD_POST (게시판 설정 하나에 여러 게시글)
- BOARD_POST 1 : N BOARD_COMMENT (게시글에 댓글)
- BOARD_POST 1 : N BOARD_FILE (게시글에 첨부파일)

**엔티티 및 속성**

| 엔티티 | 주요 속성 |
|---|---|
| BOARD_CONFIG | 게시판ID, 게시판명, 게시판유형, 허용확장자, 최대용량, 댓글허용여부, 역할별권한(JSON), 사용여부 |
| BOARD_POST | 게시글ID, 게시판ID, 제목, 내용, 조회수, 작성자ID, 작성일시 |
| BOARD_COMMENT | 댓글ID, 게시글ID, 내용, 작성자ID, 작성일시 |
| BOARD_FILE | 파일ID, 게시글ID, 원본파일명, 저장파일명, 파일경로, 파일크기, 확장자 |

---

#### 게시판 물리적 ERD

**tb_board_config**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| board_id | BIGINT | PK, AUTO_INCREMENT | 게시판ID |
| board_nm | VARCHAR(100) | NOT NULL | 게시판명 |
| board_type_cd | VARCHAR(20) | NOT NULL | 게시판유형 (NOTICE/FREE/ARCHIVE 등) |
| allow_ext | VARCHAR(200) | | 허용 확장자 (콤마 구분, NULL이면 전체 허용) |
| max_file_size | INT | | 파일 최대 용량 (MB) |
| allow_comment | CHAR(1) | NOT NULL, DEFAULT 'Y' | 댓글 허용여부 |
| role_permission | JSON | NOT NULL | 역할별 읽기/쓰기 권한 JSON |
| is_active | CHAR(1) | NOT NULL, DEFAULT 'Y' | 사용여부 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 메뉴 노출 순서 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 등록자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_board_post**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| post_id | BIGINT | PK, AUTO_INCREMENT | 게시글ID |
| board_id | BIGINT | FK(tb_board_config), NOT NULL | 게시판ID |
| title | VARCHAR(200) | NOT NULL | 제목 |
| content | TEXT | NOT NULL | 내용 |
| view_cnt | INT | NOT NULL, DEFAULT 0 | 조회수 |
| is_notice | CHAR(1) | NOT NULL, DEFAULT 'N' | 공지 고정 여부 |
| created_at | DATETIME | NOT NULL | 작성일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 작성자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_board_comment**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| comment_id | BIGINT | PK, AUTO_INCREMENT | 댓글ID |
| post_id | BIGINT | FK(tb_board_post), NOT NULL | 게시글ID |
| content | TEXT | NOT NULL | 내용 |
| created_at | DATETIME | NOT NULL | 작성일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 작성자ID |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

**tb_board_file**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| file_id | BIGINT | PK, AUTO_INCREMENT | 파일ID |
| post_id | BIGINT | FK(tb_board_post), NOT NULL | 게시글ID |
| original_nm | VARCHAR(255) | NOT NULL | 원본 파일명 |
| saved_nm | VARCHAR(255) | NOT NULL | 저장 파일명 (UUID 기반) |
| file_path | VARCHAR(500) | NOT NULL | 저장 경로 |
| file_size | BIGINT | NOT NULL | 파일 크기 (byte) |
| extension | VARCHAR(20) | NOT NULL | 확장자 |
| created_at | DATETIME | NOT NULL | 업로드일시 |
| created_by | BIGINT | FK(tb_user), NOT NULL | 업로드자ID |

---

#### 공통 / 설정 논리적 ERD

**관계 구조**
```
COMMON_CODE ──< COMMON_CODE_DETAIL

NOTIFICATION >── USER

SLA_POLICY
NOTIFICATION_POLICY
SYSTEM_CONFIG
```

**엔티티 및 속성**

| 엔티티 | 주요 속성 |
|---|---|
| COMMON_CODE | 코드그룹ID, 그룹명, 그룹코드, 설명, 사용여부 |
| COMMON_CODE_DETAIL | 상세ID, 그룹ID, 코드값, 코드명, 정렬순서, 사용여부 |
| NOTIFICATION | 알림ID, 수신자ID, 알림유형, 제목, 내용, 참조유형, 참조ID, 읽음일시 |
| NOTIFICATION_POLICY | 정책ID, 알림유형, 발송조건, 수신역할, 사용여부 |
| SLA_POLICY | 정책ID, 우선순위코드, 처리기한(시간), 경고기준(%), 회사ID |
| SYSTEM_CONFIG | 설정ID, 설정키, 설정값, 설명 |

---

#### 공통 / 설정 물리적 ERD

**tb_common_code**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| group_id | BIGINT | PK, AUTO_INCREMENT | 코드그룹ID |
| group_nm | VARCHAR(100) | NOT NULL | 그룹명 |
| group_cd | VARCHAR(50) | UNIQUE, NOT NULL | 그룹코드 |
| description | VARCHAR(200) | | 설명 |
| is_active | CHAR(1) | NOT NULL, DEFAULT 'Y' | 사용여부 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자ID |

**tb_common_code_detail**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| detail_id | BIGINT | PK, AUTO_INCREMENT | 상세ID |
| group_id | BIGINT | FK(tb_common_code), NOT NULL | 코드그룹ID |
| code_val | VARCHAR(50) | NOT NULL | 코드값 |
| code_nm | VARCHAR(100) | NOT NULL | 코드명 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 정렬순서 |
| is_active | CHAR(1) | NOT NULL, DEFAULT 'Y' | 사용여부 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자ID |

**tb_notification**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| noti_id | BIGINT | PK, AUTO_INCREMENT | 알림ID |
| user_id | BIGINT | FK(tb_user), NOT NULL | 수신자ID |
| noti_type_cd | VARCHAR(50) | NOT NULL | 알림유형코드 (INCIDENT/SR/CHANGE/INSPECTION 등) |
| title | VARCHAR(200) | NOT NULL | 알림 제목 |
| content | TEXT | | 알림 내용 |
| ref_type | VARCHAR(20) | | 참조유형 |
| ref_id | BIGINT | | 참조ID (클릭 시 해당 화면으로 이동) |
| read_at | DATETIME | | 읽음 처리 일시 (NULL = 미읽음) |
| created_at | DATETIME | NOT NULL | 발송일시 |

**tb_notification_policy**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| policy_id | BIGINT | PK, AUTO_INCREMENT | 정책ID |
| noti_type_cd | VARCHAR(50) | NOT NULL | 알림유형코드 |
| trigger_condition | VARCHAR(200) | NOT NULL | 발송 조건 설명 |
| target_role_cd | VARCHAR(50) | NOT NULL | 수신 대상 역할코드 |
| is_active | CHAR(1) | NOT NULL, DEFAULT 'Y' | 사용여부 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자ID |

**tb_sla_policy**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| policy_id | BIGINT | PK, AUTO_INCREMENT | 정책ID |
| company_id | BIGINT | FK(tb_company) | 고객사ID (NULL이면 전체 기본값) |
| priority_cd | VARCHAR(20) | NOT NULL | 우선순위코드 |
| deadline_hours | INT | NOT NULL | 처리 기한 (시간) |
| warning_pct | INT | NOT NULL, DEFAULT 80 | 경고 기준 (% — 기한의 80% 초과 시 알림) |
| is_active | CHAR(1) | NOT NULL, DEFAULT 'Y' | 사용여부 |
| created_at | DATETIME | NOT NULL | 등록일시 |
| created_by | BIGINT | FK(tb_user) | 등록자ID |

**tb_system_config**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| config_id | BIGINT | PK, AUTO_INCREMENT | 설정ID |
| config_key | VARCHAR(100) | UNIQUE, NOT NULL | 설정키 |
| config_val | TEXT | NOT NULL | 설정값 |
| description | VARCHAR(200) | | 설명 |
| updated_at | DATETIME | | 수정일시 |
| updated_by | BIGINT | FK(tb_user) | 수정자ID |

---

#### 로그 물리적 ERD

**tb_audit_log**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| log_id | BIGINT | PK, AUTO_INCREMENT | 로그ID |
| user_id | BIGINT | FK(tb_user) | 행위자ID |
| action_type | VARCHAR(50) | NOT NULL | 행위유형 (CREATE/UPDATE/DELETE/STATUS_CHANGE 등) |
| target_type | VARCHAR(50) | NOT NULL | 대상유형 (INCIDENT/SR/CHANGE 등) |
| target_id | BIGINT | | 대상ID |
| before_value | TEXT | | 변경 전 값 |
| after_value | TEXT | | 변경 후 값 |
| ip_address | VARCHAR(50) | | 요청 IP |
| created_at | DATETIME | NOT NULL | 발생일시 |

**tb_access_log**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| log_id | BIGINT | PK, AUTO_INCREMENT | 로그ID |
| user_id | BIGINT | FK(tb_user) | 사용자ID (로그인 실패 시 NULL 가능) |
| login_id | VARCHAR(50) | | 입력한 로그인ID |
| action_type | VARCHAR(20) | NOT NULL | 행위유형 (LOGIN/LOGOUT/LOGIN_FAIL) |
| ip_address | VARCHAR(50) | NOT NULL | 접속 IP |
| success_yn | CHAR(1) | NOT NULL | 성공여부 |
| fail_reason | VARCHAR(200) | | 실패 사유 |
| created_at | DATETIME | NOT NULL | 발생일시 |

**tb_menu_access_log**
| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| log_id | BIGINT | PK, AUTO_INCREMENT | 로그ID |
| user_id | BIGINT | FK(tb_user), NOT NULL | 사용자ID |
| menu_id | BIGINT | FK(tb_menu), NOT NULL | 메뉴ID |
| role_cd | VARCHAR(50) | | 접근 시점 역할코드 |
| ip_address | VARCHAR(50) | | 접속 IP |
| created_at | DATETIME | NOT NULL | 접근일시 |

---

## 다음 단계

- [x] 메뉴/화면 정의 원칙
- [x] 역할별 메뉴 상세 정의
- [x] 테이블 목록 확정
- [x] 테이블 설계 정책
- [x] ERD 설계 (전 영역)
- [x] 전체 수정 권한 정책 정리
- [x] SLA 자동 계산 설계
- [x] API 설계
- [x] 기술 스펙 확정 (모듈 구조, 폴더 구조)
- [x] 보안 설계 (비밀번호 정책, 로그인 실패 제한, 세션 만료)

---

## 전체 수정 권한 정책

> **기본 원칙**: 모든 콘텐츠는 작성한 본인만 수정 가능. 상태에 따라 수정 가능 여부가 달라짐.

---

### 공통 정책

| 정책 | 내용 |
|---|---|
| 작성자 수정 원칙 | 게시글, 댓글, 처리내용, 보고서 등 모든 콘텐츠는 작성자 본인만 수정 가능 |
| 보고서 수정 | 보고서는 작성자 본인만 수정 가능 (모듈 상태 무관) |
| 관리자 예외 | 슈퍼관리자 / ITSM관리자는 모든 콘텐츠 수정 가능 |
| 감사자 | 모든 모듈 전 상태에서 읽기 전용. 어떠한 수정/등록/삭제도 불가 |
| 이력 보존 | 수정 시 항상 이력 테이블에 변경 전/후 값 자동 적재 |
| 삭제 금지 | 물리적 삭제 없음. status 또는 is_active 로 비활성 처리 |

---

### 장애관리 수정 권한

| 행위 | 접수 | 처리중 | 완료 | 종료 | 반려→처리중 | 감사자 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| 등록자 - 장애내용 수정 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 주담당자 - 처리내용 작성/수정 | ❌ | ✅ | ❌ | ❌ | ✅ | ❌ |
| 주담당자 - 보고서 작성/수정 | ❌ | ✅ | ✅ | ❌ | ✅ | ❌ |
| 주담당자 - 완료 처리 | ❌ | ✅ | ❌ | ❌ | ✅ | ❌ |
| PM/ITSM관리자 - 담당자 지정 | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ |
| PM/ITSM관리자 - 반려 | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| PM/ITSM관리자 - 종료 | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| 댓글 - 작성 | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| 댓글 - 수정 | 작성자 본인만, 상태 무관 |||||| 
| 첨부파일 - 수정/삭제 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 전체 조회 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

---

### 서비스요청 수정 권한

| 행위 | 접수 | 담당자배정 | 처리중 | 완료대기 | 종료 | 요청취소 | 반려→처리중 | 감사자 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 요청자 - 요청내용 수정 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 요청자 - 요청취소 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 요청자 - 첨부파일 수정 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 유지보수팀 - 담당자 지정/추가 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| 유지보수팀 - Self-assign | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| 유지보수팀 - 접수로 되돌림 | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 담당자 - 처리내용 작성/수정 | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ |
| 담당자 - 첨부파일 수정 | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ |
| PM - 반려 | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| PM - 종료 | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| 댓글 - 작성 | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| 댓글 - 수정 | 작성자 본인만, 상태 무관 ||||||||
| 전체 조회 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

---

### 변경관리 수정 권한

| 행위 | 초안 | 승인요청 | 승인완료 | 실행중 | 완료 | 종료 | 반려→초안 | 감사자 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 등록자 - 변경내용 수정 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| 등록자 - 승인 요청 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| 승인자 - 승인/반려 | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 담당자 - 실행 시작 | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 담당자 - 완료 처리 | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| PM - 종료 | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| 댓글 - 작성 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| 댓글 - 수정 | 작성자 본인만, 상태 무관 ||||||||
| 첨부파일 - 수정 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| 전체 조회 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

---

### 정기점검 수정 권한

| 행위 | 예정 | 진행중 | 완료 | 종료 | 보류 | 감사자 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| PM - 점검내용 수정 | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ |
| 담당자 - 체크리스트 결과 입력/수정 | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| 담당자 - 완료 처리 | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| PM - 종료 | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| PM - 보류 처리 | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| 전체 조회 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

---

### 게시판 수정 권한

| 행위 | 내용 |
|---|---|
| 게시글 작성 | 해당 게시판 쓰기 권한이 있는 역할 |
| 게시글 수정/삭제 | 작성자 본인 + 슈퍼관리자/ITSM관리자 |
| 댓글 작성 | 해당 게시판 읽기 권한이 있는 역할 (댓글 허용 게시판만) |
| 댓글 수정/삭제 | 작성자 본인 + 슈퍼관리자/ITSM관리자 |
| 첨부파일 수정 | 게시글 수정 권한과 동일 |
| 감사자 | 게시판 빌더 권한 설정과 무관하게 모든 게시판 읽기 전용 |
| 비로그인 | 게시판 빌더에서 비로그인 읽기 허용 시 읽기만 가능 |

---

### 설정관리 수정 권한

| 항목 | 슈퍼관리자 | ITSM관리자 | 감사자 |
|---|:---:|:---:|:---:|
| 메뉴 / 권한 관리 | ✅ | ✅ | 읽기 |
| 보고서 양식 관리 | ✅ | ✅ | 읽기 |
| 공통코드 관리 | ✅ | ✅ | 읽기 |
| 알림 / 정책 관리 | ✅ | ✅ | 읽기 |
| SLA 기준값 | ✅ | ✅ | 읽기 |
| 시스템 설정 | ✅ | ✅ | ❌ |
| 게시판 관리 | ✅ | ✅ | ❌ |
| 조직 관리 (회사/부서) | ✅ | ✅ | 읽기 |
| 계정 관리 | ✅ | ✅ | 읽기 |
| 슈퍼관리자 계정 관리 | ✅ | ❌ | ❌ |

---

## SLA 설계

### SLA vs 운영 모니터링 구분

| 구분 | 목적 | 주기 |
|---|---|---|
| **SLA** | 계약서 기준 연간 달성률 평가 | 연 1회 |
| **운영 모니터링** | SLA 달성을 위한 일상 관제 | 실시간/일별/월별 |

---

### SLA 지표 (연간 계약 기준)

> 목표 기준값은 `tb_sla_policy` 에 고객사별로 저장. 계약 갱신 시 ITSM관리자가 직접 수정 가능.

| SLA 지표 | 계산 방식 | 목표 기준 예시 |
|---|---|---|
| 장애 처리율 | 연간 종료 건수 / 접수 건수 | 95% 이상 |
| 우선순위별 기한 준수율 | 우선순위별 기한 내 처리 건수 / 전체 건수 | CRITICAL 4h / HIGH 8h / MEDIUM 24h / LOW 72h |
| 서비스요청 처리율 | 연간 종료 건수 / 접수 건수 | 90% 이상 |
| 고객 만족도 평균 | 연간 만족도 점수 평균 (1~5) | 4.0 이상 |
| 정기점검 실시율 | 완료 건수 / 예정 건수 | 100% |
| 정기점검 항목 정상률 | 정상 항목 수 / 전체 항목 수 | 95% 이상 |
| 변경 예정일 준수율 | 예정일 내 완료 건수 / 전체 건수 | 90% 이상 |
| 시스템 가용률 | (전체시간 - 장애 총 소요시간) / 전체시간 | 99.9% 이상 |

---

### 운영 모니터링 지표 (내부 관리용)

| 지표 | 주기 | 모듈 |
|---|---|---|
| 미처리 건수 | 실시간 | 장애, SR |
| 처리 지연 건수 | 실시간 | 장애, SR |
| 미배정 장애 건수 | 실시간 | 장애 |
| 반려율 | 월별 | 장애, SR, 변경 |
| 평균 처리 시간 | 월별 | 장애, SR, 변경 |
| 반복 장애율 | 월별 | 장애 |
| 담당자별 처리 건수 | 월별 | SR |
| 자산 만료 임박 건수 | 일별 | 자산 |
| 미실시 점검 건수 | 실시간 | 정기점검 |

---

### SLA 기한 자동 계산 로직

**등록 시점 자동 계산**
```
sla_deadline_at = 접수일시 + tb_sla_policy.deadline_hours
(고객사 전용 정책 있으면 우선 적용, 없으면 전체 기본값 적용)
```

**경과율 실시간 계산 (조회 시점)**
```
경과율 = (현재시각 - 접수일시) / (sla_deadline_at - 접수일시) * 100
```

**반려 시 기한 연장**
```
sla_deadline_at += (처리중 복귀 시각 - 반려 처리 시각)
```

**시스템 가용률 계산**
```
장애 총 소요시간 = SUM(completed_at - occurred_at) — CRITICAL/HIGH 건만 집계
가용률 = (연간 총 시간 - 장애 총 소요시간) / 연간 총 시간 * 100
```

---

### Spring Batch 자동화 목록

| 배치 잡 | 실행 주기 | 내용 |
|---|---|---|
| SLA 경고 알림 | 매시간 | 장애/SR 경과율 80% 초과 건 담당자/PM 알림 |
| SLA 초과 에스컬레이션 | 매시간 | 기한 초과 미종료 건 PM/ITSM관리자 알림 |
| 미배정 장애 알림 | 매시간 | N시간 내 담당자 미배정 장애 PM 알림 |
| 완료대기 장기 미처리 | 일 1회 | 2일 초과 완료대기 SR PM 알림 |
| 반복 장애 감지 | 일 1회 | 동일 자산 30일 내 3회 이상 장애 알림 |
| 자산 만료 임박 알림 | 일 1회 | SW/HW 만료 30일/7일 전 담당자 알림 |
| 점검 임박 알림 | 일 1회 | 점검 예정일 7일/3일 전 담당자 알림 |
| 미실시 점검 감지 | 일 1회 | 예정일 초과 미완료 점검 PM 알림 |
| 연간 SLA 보고서 집계 | 연 1회 (계약 만료 1개월 전) | 전 모듈 SLA 지표 집계 후 보고서 자동 생성 |

---

## 프로젝트 폴더 구조

### 백엔드 (Spring Boot 멀티모듈)

```
itsm-backend/
├── build.gradle                        # 루트 빌드 설정
├── settings.gradle                     # 모듈 등록
│
├── itsm-core/                          # 공유 모듈
│   └── src/main/java/com/itsm/core/
│       ├── domain/                     # 엔티티
│       │   ├── user/
│       │   │   ├── User.java
│       │   │   ├── UserHistory.java
│       │   │   ├── Role.java
│       │   │   └── UserRole.java
│       │   ├── company/
│       │   │   ├── Company.java
│       │   │   ├── CompanyHistory.java
│       │   │   ├── Department.java
│       │   │   └── DepartmentHistory.java
│       │   ├── asset/
│       │   │   ├── AssetHw.java
│       │   │   ├── AssetSw.java
│       │   │   ├── AssetRelation.java
│       │   │   ├── AssetHwHistory.java
│       │   │   └── AssetSwHistory.java
│       │   ├── incident/
│       │   │   ├── Incident.java
│       │   │   ├── IncidentAssignee.java
│       │   │   ├── IncidentComment.java
│       │   │   ├── IncidentHistory.java
│       │   │   └── IncidentReport.java
│       │   ├── servicerequest/
│       │   │   ├── ServiceRequest.java
│       │   │   ├── ServiceRequestAssignee.java
│       │   │   ├── ServiceRequestProcess.java
│       │   │   └── ServiceRequestHistory.java
│       │   ├── change/
│       │   │   ├── Change.java
│       │   │   ├── ChangeApprover.java
│       │   │   ├── ChangeComment.java
│       │   │   └── ChangeHistory.java
│       │   ├── inspection/
│       │   │   ├── Inspection.java
│       │   │   ├── InspectionItem.java
│       │   │   ├── InspectionResult.java
│       │   │   └── InspectionHistory.java
│       │   ├── board/
│       │   │   ├── BoardConfig.java
│       │   │   ├── BoardPost.java
│       │   │   ├── BoardComment.java
│       │   │   └── BoardFile.java
│       │   ├── report/
│       │   │   ├── ReportForm.java
│       │   │   └── Report.java
│       │   └── common/
│       │       ├── CommonCode.java
│       │       ├── CommonCodeDetail.java
│       │       ├── Notification.java
│       │       ├── NotificationPolicy.java
│       │       ├── SlaPolicy.java
│       │       └── SystemConfig.java
│       ├── repository/                 # JPA 레포지토리
│       │   ├── user/
│       │   ├── asset/
│       │   ├── incident/
│       │   └── ...
│       ├── enums/                      # 상태값 Enum
│       │   ├── IncidentStatus.java
│       │   ├── ServiceRequestStatus.java
│       │   ├── ChangeStatus.java
│       │   └── UserStatus.java
│       ├── exception/                  # 공통 예외
│       │   ├── BusinessException.java
│       │   └── ErrorCode.java
│       └── util/                       # 공통 유틸
│           ├── DateUtil.java
│           └── SlaCalculator.java
│
├── itsm-api/                           # API 서버 모듈
│   └── src/main/java/com/itsm/api/
│       ├── ItsmApiApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── WebConfig.java
│       │   ├── JpaConfig.java
│       │   └── SwaggerConfig.java
│       ├── security/
│       │   ├── JwtTokenProvider.java
│       │   ├── JwtAuthFilter.java
│       │   ├── CustomUserDetailsService.java
│       │   └── interceptor/
│       │       ├── AuthInterceptor.java        # 권한 검증
│       │       └── MenuAccessInterceptor.java  # 메뉴 접근 로그
│       ├── controller/
│       │   ├── auth/
│       │   │   └── AuthController.java
│       │   ├── user/
│       │   │   └── UserController.java
│       │   ├── company/
│       │   │   └── CompanyController.java
│       │   ├── asset/
│       │   │   ├── AssetHwController.java
│       │   │   └── AssetSwController.java
│       │   ├── incident/
│       │   │   └── IncidentController.java
│       │   ├── servicerequest/
│       │   │   └── ServiceRequestController.java
│       │   ├── change/
│       │   │   └── ChangeController.java
│       │   ├── inspection/
│       │   │   └── InspectionController.java
│       │   ├── board/
│       │   │   └── BoardController.java
│       │   ├── report/
│       │   │   └── ReportController.java
│       │   ├── notification/
│       │   │   └── NotificationController.java
│       │   └── admin/
│       │       ├── MenuController.java
│       │       ├── CommonCodeController.java
│       │       ├── SlaController.java
│       │       └── SystemConfigController.java
│       ├── service/
│       │   ├── auth/
│       │   ├── user/
│       │   ├── asset/
│       │   ├── incident/
│       │   ├── servicerequest/
│       │   ├── change/
│       │   ├── inspection/
│       │   ├── board/
│       │   ├── report/
│       │   └── admin/
│       ├── dto/
│       │   ├── request/                # 요청 DTO
│       │   └── response/               # 응답 DTO
│       └── aop/
│           └── AuditLogAspect.java     # 감사 로그 AOP
│
└── itsm-batch/                         # 배치 모듈
    └── src/main/java/com/itsm/batch/
        ├── ItsmBatchApplication.java
        ├── config/
        │   └── BatchConfig.java
        └── job/
            ├── sla/
            │   ├── SlaWarningJob.java
            │   └── SlaEscalationJob.java
            ├── asset/
            │   └── AssetExpireAlertJob.java
            ├── incident/
            │   ├── UnassignedIncidentJob.java
            │   └── RepeatIncidentJob.java
            ├── servicerequest/
            │   └── PendingCompleteAlertJob.java
            ├── inspection/
            │   ├── InspectionReminderJob.java
            │   └── OverdueInspectionJob.java
            └── report/
                └── AnnualSlaReportJob.java
```

---

### 프론트엔드 (Vue.js)

```
itsm-frontend/
├── index.html
├── vite.config.js
├── package.json
│
└── src/
    ├── main.js                         # 앱 진입점
    ├── App.vue
    │
    ├── assets/                         # 정적 리소스
    │   ├── styles/
    │   │   ├── main.css
    │   │   ├── variables.css           # 색상/폰트 변수
    │   │   └── common.css
    │   └── images/
    │
    ├── router/                         # Vue Router
    │   ├── index.js                    # 라우터 등록
    │   ├── guards.js                   # 네비게이션 가드 (인증/권한)
    │   └── routes/
    │       ├── auth.js
    │       ├── dashboard.js
    │       ├── incident.js
    │       ├── servicerequest.js
    │       ├── change.js
    │       ├── asset.js
    │       ├── inspection.js
    │       ├── board.js
    │       ├── report.js
    │       └── admin.js
    │
    ├── stores/                         # Pinia 상태관리
    │   ├── auth.js                     # 로그인 상태, JWT, 사용자 정보
    │   ├── menu.js                     # 사이드바 메뉴 (동적)
    │   ├── notification.js             # 알림 목록/읽음 처리
    │   └── commonCode.js               # 공통코드 캐싱
    │
    ├── api/                            # Axios API 호출
    │   ├── index.js                    # Axios 인스턴스, 인터셉터
    │   ├── auth.js
    │   ├── user.js
    │   ├── company.js
    │   ├── asset.js
    │   ├── incident.js
    │   ├── servicerequest.js
    │   ├── change.js
    │   ├── inspection.js
    │   ├── board.js
    │   ├── report.js
    │   ├── notification.js
    │   └── admin/
    │       ├── menu.js
    │       ├── commonCode.js
    │       ├── sla.js
    │       └── systemConfig.js
    │
    ├── composables/                    # 재사용 로직 (Vue Composition)
    │   ├── useAuth.js                  # 권한 체크 헬퍼
    │   ├── usePagination.js            # 페이지네이션
    │   ├── useFileUpload.js            # 파일 업로드
    │   ├── useConfirm.js               # 확인 다이얼로그
    │   └── useSlaProgress.js           # SLA 경과율 계산/색상
    │
    ├── utils/                          # 순수 유틸 함수
    │   ├── date.js                     # 날짜 포맷
    │   ├── formatter.js                # 숫자/텍스트 포맷
    │   └── debounce.js                 # SR 중복 등록 방지
    │
    ├── constants/                      # 상수
    │   ├── status.js                   # 상태값 상수
    │   └── roles.js                    # 역할 상수
    │
    ├── components/                     # 공통 컴포넌트
    │   ├── layout/
    │   │   ├── AppLayout.vue           # 전체 레이아웃
    │   │   ├── AppHeader.vue           # 상단바 (알림 아이콘 포함)
    │   │   ├── AppSidebar.vue          # 동적 사이드바
    │   │   └── AppBreadcrumb.vue
    │   ├── common/
    │   │   ├── BaseTable.vue           # 공통 테이블
    │   │   ├── BasePagination.vue
    │   │   ├── BaseModal.vue
    │   │   ├── BaseConfirm.vue         # 확인/취소 다이얼로그
    │   │   ├── BaseFileUpload.vue
    │   │   ├── BaseStatusBadge.vue     # 상태 뱃지 (색상 자동)
    │   │   ├── BaseSlaBar.vue          # SLA 경과율 프로그레스바
    │   │   └── DynamicForm.vue         # 동적 폼 렌더러 (JSON 스키마 기반)
    │   └── notification/
    │       └── NotificationDropdown.vue
    │
    └── views/                          # 페이지 컴포넌트
        ├── auth/
        │   └── LoginView.vue
        ├── dashboard/
        │   └── DashboardView.vue
        ├── incident/
        │   ├── IncidentListView.vue
        │   ├── IncidentDetailView.vue
        │   └── IncidentFormView.vue
        ├── servicerequest/
        │   ├── ServiceRequestListView.vue
        │   ├── ServiceRequestDetailView.vue
        │   └── ServiceRequestFormView.vue
        ├── change/
        │   ├── ChangeListView.vue
        │   ├── ChangeDetailView.vue
        │   └── ChangeFormView.vue
        ├── asset/
        │   ├── AssetHwListView.vue
        │   ├── AssetHwDetailView.vue
        │   ├── AssetSwListView.vue
        │   └── AssetSwDetailView.vue
        ├── inspection/
        │   ├── InspectionListView.vue
        │   ├── InspectionDetailView.vue
        │   └── InspectionFormView.vue
        ├── board/
        │   ├── BoardListView.vue
        │   ├── BoardPostDetailView.vue
        │   └── BoardPostFormView.vue
        ├── report/
        │   ├── ReportListView.vue
        │   └── ReportDetailView.vue
        └── admin/
            ├── MenuManageView.vue
            ├── CommonCodeView.vue
            ├── BoardManageView.vue
            ├── SlaManageView.vue
            ├── NotificationPolicyView.vue
            ├── OrgManageView.vue
            └── AccountManageView.vue
```

---

## API 설계

### 공통 규칙

| 항목 | 규칙 |
|---|---|
| Base URL | `/api/v1` |
| 인증 | JWT Bearer Token (Authorization 헤더) |
| 응답 포맷 | `{ "success": true, "data": {}, "message": "" }` |
| 에러 포맷 | `{ "success": false, "code": "E001", "message": "" }` |
| 페이지네이션 | `?page=0&size=20&sort=createdAt,desc` |
| 날짜 포맷 | ISO 8601 (`yyyy-MM-dd'T'HH:mm:ss`) |

---

### 인증

| 메서드 | URL | 설명 |
|---|---|---|
| POST | `/api/v1/auth/login` | 로그인 (JWT 발급) |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 |
| GET | `/api/v1/auth/me` | 내 정보 조회 |
| PATCH | `/api/v1/auth/password` | 비밀번호 변경 |

---

### 사용자 / 계정

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/users` | 사용자 목록 |
| POST | `/api/v1/users` | 사용자 등록 |
| GET | `/api/v1/users/{userId}` | 사용자 상세 |
| PATCH | `/api/v1/users/{userId}` | 사용자 수정 |
| PATCH | `/api/v1/users/{userId}/status` | 상태 변경 (활성/비활성/퇴사 등) |
| GET | `/api/v1/users/{userId}/history` | 사용자 변경 이력 |
| POST | `/api/v1/users/{userId}/roles` | 역할 부여 |
| DELETE | `/api/v1/users/{userId}/roles/{roleId}` | 역할 회수 |

---

### 조직 (회사 / 부서)

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/companies` | 회사 목록 |
| POST | `/api/v1/companies` | 회사 등록 |
| GET | `/api/v1/companies/{companyId}` | 회사 상세 |
| PATCH | `/api/v1/companies/{companyId}` | 회사 수정 |
| GET | `/api/v1/companies/{companyId}/departments` | 부서 목록 |
| POST | `/api/v1/companies/{companyId}/departments` | 부서 등록 |
| PATCH | `/api/v1/departments/{deptId}` | 부서 수정 |

---

### 자산 (CMDB)

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/assets/hw` | HW 자산 목록 |
| POST | `/api/v1/assets/hw` | HW 자산 등록 |
| GET | `/api/v1/assets/hw/{assetId}` | HW 자산 상세 |
| PATCH | `/api/v1/assets/hw/{assetId}` | HW 자산 수정 |
| GET | `/api/v1/assets/hw/{assetId}/history` | HW 자산 이력 |
| GET | `/api/v1/assets/sw` | SW 자산 목록 |
| POST | `/api/v1/assets/sw` | SW 자산 등록 |
| GET | `/api/v1/assets/sw/{assetId}` | SW 자산 상세 |
| PATCH | `/api/v1/assets/sw/{assetId}` | SW 자산 수정 |
| GET | `/api/v1/assets/sw/{assetId}/history` | SW 자산 이력 |
| POST | `/api/v1/assets/relations` | 자산 연관관계 등록 |
| DELETE | `/api/v1/assets/relations/{relationId}` | 자산 연관관계 삭제 |

---

### 장애관리

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/incidents` | 장애 목록 |
| POST | `/api/v1/incidents` | 장애 등록 |
| GET | `/api/v1/incidents/{incidentId}` | 장애 상세 |
| PATCH | `/api/v1/incidents/{incidentId}` | 장애 수정 |
| PATCH | `/api/v1/incidents/{incidentId}/status` | 상태 변경 |
| POST | `/api/v1/incidents/{incidentId}/assignees` | 담당자 지정 |
| DELETE | `/api/v1/incidents/{incidentId}/assignees/{userId}` | 담당자 해제 |
| GET | `/api/v1/incidents/{incidentId}/comments` | 댓글 목록 |
| POST | `/api/v1/incidents/{incidentId}/comments` | 댓글 등록 |
| PATCH | `/api/v1/incidents/{incidentId}/comments/{commentId}` | 댓글 수정 |
| GET | `/api/v1/incidents/{incidentId}/history` | 변경 이력 |
| POST | `/api/v1/incidents/{incidentId}/report` | 보고서 작성 |
| PATCH | `/api/v1/incidents/{incidentId}/report` | 보고서 수정 |

---

### 서비스요청

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/service-requests` | SR 목록 |
| POST | `/api/v1/service-requests` | SR 등록 |
| GET | `/api/v1/service-requests/{requestId}` | SR 상세 |
| PATCH | `/api/v1/service-requests/{requestId}` | SR 수정 (접수 상태만) |
| PATCH | `/api/v1/service-requests/{requestId}/status` | 상태 변경 |
| PATCH | `/api/v1/service-requests/{requestId}/cancel` | 요청 취소 |
| POST | `/api/v1/service-requests/{requestId}/assignees` | 담당자 지정 |
| DELETE | `/api/v1/service-requests/{requestId}/assignees/{userId}` | 담당자 제거 |
| PATCH | `/api/v1/service-requests/{requestId}/assignees/{userId}/revert` | 접수로 되돌림 |
| POST | `/api/v1/service-requests/{requestId}/processes` | 처리내용 작성 |
| PATCH | `/api/v1/service-requests/{requestId}/processes/{processId}` | 처리내용 수정 |
| PATCH | `/api/v1/service-requests/{requestId}/processes/{processId}/complete` | 처리완료 |
| GET | `/api/v1/service-requests/{requestId}/comments` | 댓글 목록 |
| POST | `/api/v1/service-requests/{requestId}/comments` | 댓글 등록 |
| PATCH | `/api/v1/service-requests/{requestId}/comments/{commentId}` | 댓글 수정 |
| GET | `/api/v1/service-requests/{requestId}/history` | 변경 이력 |
| POST | `/api/v1/service-requests/{requestId}/satisfaction` | 만족도 제출 |

---

### 변경관리

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/changes` | 변경 목록 |
| POST | `/api/v1/changes` | 변경 등록 |
| GET | `/api/v1/changes/{changeId}` | 변경 상세 |
| PATCH | `/api/v1/changes/{changeId}` | 변경 수정 (초안/반려 상태만) |
| PATCH | `/api/v1/changes/{changeId}/status` | 상태 변경 |
| POST | `/api/v1/changes/{changeId}/approvers` | 승인자 지정 |
| PATCH | `/api/v1/changes/{changeId}/approve` | 승인 처리 |
| PATCH | `/api/v1/changes/{changeId}/reject` | 반려 처리 |
| GET | `/api/v1/changes/{changeId}/comments` | 댓글 목록 |
| POST | `/api/v1/changes/{changeId}/comments` | 댓글 등록 |
| PATCH | `/api/v1/changes/{changeId}/comments/{commentId}` | 댓글 수정 |
| GET | `/api/v1/changes/{changeId}/history` | 변경 이력 |

---

### 정기점검

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/inspections` | 점검 목록 |
| POST | `/api/v1/inspections` | 점검 등록 |
| GET | `/api/v1/inspections/{inspectionId}` | 점검 상세 |
| PATCH | `/api/v1/inspections/{inspectionId}` | 점검 수정 |
| PATCH | `/api/v1/inspections/{inspectionId}/status` | 상태 변경 |
| POST | `/api/v1/inspections/{inspectionId}/items` | 체크리스트 항목 등록 |
| PATCH | `/api/v1/inspections/{inspectionId}/items/{itemId}` | 체크리스트 항목 수정 |
| POST | `/api/v1/inspections/{inspectionId}/results` | 점검 결과 입력 |
| PATCH | `/api/v1/inspections/{inspectionId}/results/{resultId}` | 점검 결과 수정 |

---

### 보고관리

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/report-forms` | 보고서 양식 목록 |
| POST | `/api/v1/report-forms` | 보고서 양식 등록 |
| PATCH | `/api/v1/report-forms/{formId}` | 보고서 양식 수정 |
| GET | `/api/v1/reports` | 보고서 목록 |
| POST | `/api/v1/reports` | 보고서 작성 |
| GET | `/api/v1/reports/{reportId}` | 보고서 상세 |
| PATCH | `/api/v1/reports/{reportId}` | 보고서 수정 (작성자만) |

---

### 게시판

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/boards` | 게시판 목록 (사이드바용) |
| GET | `/api/v1/boards/{boardId}/posts` | 게시글 목록 |
| POST | `/api/v1/boards/{boardId}/posts` | 게시글 등록 |
| GET | `/api/v1/boards/{boardId}/posts/{postId}` | 게시글 상세 |
| PATCH | `/api/v1/boards/{boardId}/posts/{postId}` | 게시글 수정 (작성자만) |
| PATCH | `/api/v1/boards/{boardId}/posts/{postId}/deactivate` | 게시글 비활성 |
| GET | `/api/v1/boards/{boardId}/posts/{postId}/comments` | 댓글 목록 |
| POST | `/api/v1/boards/{boardId}/posts/{postId}/comments` | 댓글 등록 |
| PATCH | `/api/v1/boards/{boardId}/posts/{postId}/comments/{commentId}` | 댓글 수정 |

---

### 알림

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/notifications` | 알림 목록 (내 알림) |
| PATCH | `/api/v1/notifications/{notiId}/read` | 읽음 처리 |
| PATCH | `/api/v1/notifications/read-all` | 전체 읽음 처리 |

---

### 설정관리 (관리자)

| 메서드 | URL | 설명 |
|---|---|---|
| GET | `/api/v1/admin/menus` | 메뉴 목록 |
| PATCH | `/api/v1/admin/menus/{menuId}/roles` | 메뉴 역할 권한 수정 |
| GET | `/api/v1/admin/common-codes` | 공통코드 그룹 목록 |
| POST | `/api/v1/admin/common-codes` | 공통코드 그룹 등록 |
| POST | `/api/v1/admin/common-codes/{groupId}/details` | 공통코드 상세 등록 |
| PATCH | `/api/v1/admin/common-codes/{groupId}/details/{detailId}` | 공통코드 상세 수정 |
| GET | `/api/v1/admin/sla-policies` | SLA 정책 목록 |
| POST | `/api/v1/admin/sla-policies` | SLA 정책 등록 |
| PATCH | `/api/v1/admin/sla-policies/{policyId}` | SLA 정책 수정 |
| GET | `/api/v1/admin/boards` | 게시판 설정 목록 |
| POST | `/api/v1/admin/boards` | 게시판 생성 |
| PATCH | `/api/v1/admin/boards/{boardId}` | 게시판 설정 수정 |
| GET | `/api/v1/admin/system-configs` | 시스템 설정 목록 |
| PATCH | `/api/v1/admin/system-configs/{configKey}` | 시스템 설정 수정 |
| GET | `/api/v1/admin/notification-policies` | 알림 정책 목록 |
| PATCH | `/api/v1/admin/notification-policies/{policyId}` | 알림 정책 수정 |

---

## 보안 설계

### 인증 방식

| 항목 | 내용 |
|---|---|
| 방식 | JWT (Access Token + Refresh Token) |
| Access Token 유효기간 | 30분 |
| Refresh Token 유효기간 | 7일 |
| 저장 위치 | Access Token — 메모리 (Pinia store), Refresh Token — HttpOnly Cookie |
| 탈취 대응 | Refresh Token DB 저장 (`tb_user`) → 로그아웃/강제만료 시 서버에서 무효화 가능 |

---

### 비밀번호 정책

| 항목 | 기준 |
|---|---|
| 최소 길이 | 8자 이상 |
| 조합 규칙 | 영문 대소문자 + 숫자 + 특수문자 각 1자 이상 |
| 암호화 | BCrypt (strength 12) |
| 초기 비밀번호 | 관리자가 임시 발급 → 최초 로그인 시 강제 변경 |
| 주기적 변경 | 90일 초과 시 변경 안내 (강제 아님, 안내 팝업) |
| 이전 비밀번호 재사용 | 최근 3개 재사용 불가 (`tb_user_history` 활용) |

---

### 로그인 실패 제한

| 항목 | 기준 |
|---|---|
| 실패 허용 횟수 | 5회 |
| 초과 시 처리 | 계정 잠금 (status → LOCKED) |
| 잠금 해제 | 관리자 수동 해제 또는 30분 후 자동 해제 |
| 실패 이력 | `tb_access_log` 에 적재 |

---

### 세션 / 토큰 만료

| 항목 | 기준 |
|---|---|
| 자동 로그아웃 | Access Token 만료 시 Refresh Token으로 자동 갱신 시도 |
| Refresh Token 만료 | 재로그인 요구 |
| 동시 로그인 | 동일 계정 중복 로그인 허용 (마지막 로그인 기준 Refresh Token 갱신) |
| 강제 로그아웃 | 관리자가 특정 사용자 세션 강제 만료 가능 (DB Refresh Token 삭제) |

---

### 접근 제어 이중 방어

| 계층 | 구현 | 역할 |
|---|---|---|
| 프론트엔드 | `guards.js` 네비게이션 가드 | 화면 진입 전 역할 체크 |
| 백엔드 Interceptor | `AuthInterceptor` | URL 직접 접근 방어 |
| 백엔드 Service | 작성자 본인 여부 체크 | 데이터 레벨 권한 검증 |

---

### 기타 보안 정책

| 항목 | 내용 |
|---|---|
| CORS | 허용 Origin 명시적 설정 (와일드카드 금지) |
| XSS 방지 | 입력값 이스케이프 처리, Content-Security-Policy 헤더 설정 |
| SQL Injection | JPA/QueryDSL 파라미터 바인딩으로 방지 |
| 파일 업로드 | 허용 확장자 화이트리스트 검증, 저장 파일명 UUID 변환 |
| 개인정보 파기 | 퇴사 처리 시 login_id 마스킹 (`DELETED_{userId}_{loginId}`) |
| 감사 로그 | AOP 기반 자동 적재, 감사자 역할로 조회 가능 |
