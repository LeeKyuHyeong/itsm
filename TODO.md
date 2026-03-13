# ITSM 개발 TODO

> Vue.js (프론트엔드) + Spring Boot (백엔드) REST API 구조

---

## Phase 11: UI 테마 (라이트/다크 모드)

### 프론트엔드
- [ ] CSS 변수 기반 테마 시스템 구축 (`:root` / `[data-theme="dark"]`)
- [ ] 다크 테마 색상 팔레트 정의
  - [ ] 배경색 (surface, card, sidebar, header)
  - [ ] 텍스트 색상 (primary, secondary, disabled)
  - [ ] 보더, 쉐도우, 오버레이 색상
  - [ ] 상태 뱃지 색상 (green, red, blue, gray 계열 다크 대응)
  - [ ] 차트 색상 대응
- [ ] 테마 토글 버튼 (AppHeader에 추가)
- [ ] localStorage 기반 테마 설정 유지 (`itsm-theme`)
- [ ] 사이드바 다크 테마 대응 (현재 이미 다크 계열이므로 라이트 모드 사이드바 추가)
- [ ] 테이블, 모달, 폼 등 공통 컴포넌트 테마 대응
- [ ] 로그인 화면 테마 대응

---

## Phase 12: 자산관리 재구조화

### 자산 분류 체계

#### 대분류 (3개)
| 대분류 코드 | 대분류명 | 설명 |
|---|---|---|
| INFRA_HW | 운영장비 | 데이터센터/서버실 운영 인프라 장비 |
| INFRA_SW | 운영SW | 인프라 운영에 사용되는 소프트웨어 |
| OA | OA자산 | 사무용 IT 자산 (PC, 노트북, 주변기기 등) |

#### 중분류 — 운영장비 (INFRA_HW)
| 중분류 코드 | 중분류명 | 예시 |
|---|---|---|
| SERVER_RACK | 랙마운트 서버 | Dell PowerEdge R750, HPE DL380 |
| SERVER_BLADE | 블레이드 서버 | HPE Synergy, Cisco UCS |
| SERVER_TOWER | 타워 서버 | Dell T550, HPE ML350 |
| STORAGE_SAN | SAN 스토리지 | EMC Unity, NetApp AFF |
| STORAGE_NAS | NAS | Synology, QNAP Enterprise |
| NETWORK_SWITCH | 네트워크 스위치 | Cisco Catalyst, Arista |
| NETWORK_ROUTER | 라우터 | Cisco ISR, Juniper MX |
| NETWORK_FW | 방화벽 | Palo Alto, FortiGate |
| NETWORK_LB | 로드밸런서 | F5 BIG-IP, Citrix ADC |
| NETWORK_AP | 무선AP | Cisco AP, Aruba |
| SECURITY_IDS | IDS/IPS | Snort, Suricata |
| SECURITY_WAF | WAF | AWS WAF, Imperva |
| POWER_UPS | UPS | APC Smart-UPS |
| POWER_PDU | PDU | Raritan, APC |
| INFRA_KVM | KVM/콘솔 | Raritan KVM |

#### 중분류 — 운영SW (INFRA_SW)
| 중분류 코드 | 중분류명 | 예시 |
|---|---|---|
| SW_OS | 운영체제 | RHEL, Windows Server, Ubuntu |
| SW_DB | 데이터베이스 | Oracle, MySQL, PostgreSQL, MSSQL |
| SW_WAS | WAS/웹서버 | Tomcat, Nginx, Apache, WebLogic |
| SW_MIDDLEWARE | 미들웨어 | RabbitMQ, Kafka, Redis |
| SW_MONITORING | 모니터링 | Zabbix, Grafana, Prometheus |
| SW_BACKUP | 백업솔루션 | Veeam, Acronis, NetBackup |
| SW_SECURITY | 보안솔루션 | V3, 알약, CrowdStrike |
| SW_VIRTUALIZATION | 가상화 | VMware vSphere, Hyper-V, KVM |
| SW_CONTAINER | 컨테이너/오케 | Docker, Kubernetes, OpenShift |
| SW_CICD | CI/CD | Jenkins, GitLab CI, ArgoCD |
| SW_LICENSE | 상용라이선스 | MS Office 볼륨, Adobe CC |

#### 중분류 — OA자산 (OA)
| 중분류 코드 | 중분류명 | 예시 |
|---|---|---|
| OA_DESKTOP | 데스크톱 | Dell OptiPlex, HP EliteDesk |
| OA_LAPTOP | 노트북 | Dell Latitude, Lenovo ThinkPad |
| OA_MONITOR | 모니터 | Dell U2722D, LG 27UK850 |
| OA_PRINTER | 프린터/복합기 | HP LaserJet, Canon IR |
| OA_PHONE | 전화/VoIP | Cisco IP Phone |
| OA_TABLET | 태블릿 | iPad, Galaxy Tab |
| OA_PERIPHERAL | 주변기기 | 키보드, 마우스, 도킹스테이션 |
| OA_PROJECTOR | 프로젝터 | Epson, BenQ |

### DB 스키마 변경
- [ ] `tb_asset_hw` 테이블에 `asset_category` (대분류), `asset_sub_category` (중분류) 컬럼 추가
- [ ] `tb_asset_sw` 테이블에 `asset_category`, `asset_sub_category` 컬럼 추가
- [ ] 공통코드에 자산 대분류/중분류 코드 등록 (ASSET_CATEGORY, ASSET_SUB_CATEGORY 그룹)
- [ ] 마이그레이션 SQL 작성 (기존 데이터 분류 매핑)

### 백엔드
- [ ] `AssetHw`, `AssetSw` 엔티티에 분류 필드 추가
- [ ] 자산 목록 API에 대분류/중분류 필터 추가
- [ ] 자산 통계 API (분류별 자산 현황)

### 프론트엔드
- [ ] 자산 목록 뷰 3탭 구성 (운영장비 / 운영SW / OA)
- [ ] 중분류 필터 드롭다운 (대분류 선택 시 중분류 연동)
- [ ] 자산 등록/수정 폼에 대분류/중분류 선택 추가
- [ ] 대시보드에 자산 분류별 현황 위젯 추가

---

## Phase 13: 데모/시뮬레이션 배치

> 목적: 실제 운영 중인 것처럼 보이도록 자동으로 데이터를 생성하여 통계 및 현황 데이터를 풍부하게 만든다.

### 자산 자동 등록 배치
- [ ] `AssetAutoRegisterJob` — 분류체계 기반 자산 자동 생성
  - 운영장비 100+대, 운영SW 50+건, OA자산 200+대 초기 세팅
  - 자산별 리얼한 제조사/모델/시리얼번호 생성
  - 자산 간 연관관계 자동 매핑 (서버↔OS, 서버↔DB 등)
  - 위치 정보 (데이터센터 A/B, 사무실 N층 등)
  - 실행 주기: 초기 1회 + 월 1~2건 신규 자산 추가

### 장애 시뮬레이션 배치
- [ ] `IncidentSimulationJob` — 장애 자동 생성 및 처리
  - 일 2~5건 장애 자동 접수 (시간대별 가중치: 업무시간 높음)
  - 장애 유형: 서버 다운, 네트워크 장애, DB 성능저하, 스토리지 용량, 보안 이벤트 등
  - 자동 담당자 배정 (접수 후 10~60분)
  - 자동 처리 완료 (배정 후 1~8시간, 우선순위에 따라 차등)
  - SLA 초과 건 5~10% 비율로 발생
  - 댓글/이력 자동 생성 (처리 과정 기록)
  - 장애보고서 자동 작성 (완료 건)
  - 실행 주기: 매 30분

### 서비스요청 시뮬레이션 배치
- [ ] `ServiceRequestSimulationJob` — SR 자동 생성 및 처리
  - 일 3~8건 SR 자동 접수
  - 요청 유형: 계정 생성/변경, 권한 요청, SW 설치, HW 교체, VPN 설정 등
  - 자동 배정 → 처리 → 완료 흐름
  - 만족도 자동 입력 (3.5~5.0 분포)
  - 실행 주기: 매 1시간

### 변경관리 시뮬레이션 배치
- [ ] `ChangeSimulationJob` — 변경 자동 생성 및 승인
  - 주 2~4건 변경 요청 생성
  - 변경 유형: 패치 적용, 설정 변경, 업그레이드, 인프라 확장 등
  - 승인 → 실행 → 완료 자동 흐름 (1~3일 소요)
  - 반려 10~15% 비율
  - 실행 주기: 일 1회

### 정기점검 시뮬레이션 배치
- [ ] `InspectionSimulationJob` — 점검 자동 수행
  - 월간/주간 정기점검 일정 자동 생성
  - 체크리스트 결과 자동 입력 (정상 90%, 이상 10%)
  - 이상 발견 시 장애 자동 연계 등록
  - 실행 주기: 일 1회

### 트래픽/모니터링 시뮬레이션 배치
- [ ] `TrafficSimulationJob` — 시스템 활동 로그 생성
  - 사용자 로그인/로그아웃 이력 자동 생성 (업무시간 집중)
  - 메뉴 접근 로그 생성 (페이지별 조회수)
  - 알림 자동 발생 및 읽음 처리
  - 실행 주기: 매 15분

### 통계 집계 배치
- [ ] `StatisticsAggregationJob` — 대시보드용 통계 데이터 집계
  - 일별/주별/월별 장애 건수, 평균 처리시간, SLA 준수율
  - 서비스요청 처리 현황 및 만족도 추이
  - 자산 가동률 및 장애 빈도 (자산별)
  - 담당자별 업무 처리 통계
  - 실행 주기: 일 1회 (새벽 2시)

---

## 설계 원칙 (개발 시 항상 참고)

1. **서비스 중단 최소화** — 동적 폼, DB 기반 설정, 메뉴 동적 관리
2. **추적 가능성** — 모든 변경 이력 자동 적재, 감사 로그
3. **권한 최소화** — RBAC, 이중 방어 (프론트 가드 + 백엔드 Interceptor)
4. **확장 가능한 구조** — 공통코드, 게시판 빌더, JSON 스키마 동적 폼
5. **물리적 삭제 금지** — status / is_active 로 비활성 처리
6. **낙관적 락** — 동시 수정 충돌 방지
