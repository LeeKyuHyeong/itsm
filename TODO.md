# ITSM 개발 TODO

> Vue.js (프론트엔드) + Spring Boot (백엔드) REST API 구조

---

## 다음 작업 (Next Up)

추가 작업 없음. Phase 26 적용 후 운영 DB 마이그레이션 필요.

## 소스 전수조사 (2026-09-16) — "구현했는데 실제로는 안 돌던 것"

Part 별 1커밋. 조사 기록·근거는 `D:\dev\checklist-itsm-source-audit.md`(리포 밖).

- [x] P7 인프라·CI/CD — 헬스체크 엔드포인트 부재(401 을 healthy 로 오판) → `/api/v1/auth/health` 신설 + 200 만 통과 / nginx `admin` 차단어가 `/admin/*` 화면을 444 로 끊음 → 제거 + 회귀 테스트 / 컨테이너 nginx 에 realip 없어 rate limit 공용·fail2ban 무력 → `X-Real-IP` 복원 / fail2ban 필터 3개 중 2개 정규식이 로그와 불일치 → 재작성 / CLAUDE.md·.env.example 드리프트
  - 운영 반영 후 확인: `/admin/menus` F5 → 200, `fail2ban-client status nginx-scanner` 에 실제 원격 IP, `docker compose logs itsm-api | grep health`
- [ ] P5 추적성 (감사 로그 AOP 미사용, LoginHistory 미기록, 알림 배지 갱신 경로)
- [ ] P3 설정 화면 → 소비 코드 (동적 폼 미연결, SLA/알림 정책 소비처)
- [ ] P4 배치·스케줄러
- [ ] P1 인증·인가 체인 (로그인 레이트리밋 `getRemoteAddr` 이 프록시 IP — P7 에서 발견, 여기서 수정)
- [ ] P2 프론트↔백엔드 API 계약
- [ ] P6 데이터 계층
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
