# ITSM 개발 TODO

> Vue.js (프론트엔드) + Spring Boot (백엔드) REST API 구조

---

## 완료된 Phase

- **Phase 11**: UI 테마 (라이트/다크 모드) ✅
- **Phase 12**: 자산관리 재구조화 ✅
- **Phase 13**: 데모/시뮬레이션 배치 ✅
- **Phase 14**: 전체 i18n (ko/en) 대응 ✅
- **Phase 15**: CI/CD + 운영 배포 ✅

---

## Phase 15: CI/CD + 운영 배포 (완료)

- [x] GitHub Actions CI/CD 파이프라인 구성 (test → build → deploy)
- [x] Docker Compose 구성 (itsm-api, itsm-batch, itsm-frontend, MariaDB)
- [x] Backend Dockerfile (itsm-api, itsm-batch 공용)
- [x] Frontend Dockerfile (multi-stage: node build → nginx)
- [x] 운영 포트 배정 (프론트엔드: 8084, DB: 3310)
- [x] 환경변수 기반 운영 설정 (DB_PASSWORD, JWT_SECRET, CORS_ORIGINS)
- [x] 호스트 nginx 리버스 프록시 설정 (itsm.kiryong.com → localhost:8084)
- [x] Let's Encrypt SSL 인증서 발급 (certbot)
- [x] Flyway 제거, DB 스키마는 ddl-auto: update + 수동 관리로 전환
- [ ] SSL "주의 요함" 경고 확인 필요 (Mixed Content 또는 브라우저 캐시 문제 추정)

---

## 설계 원칙 (개발 시 항상 참고)

1. **서비스 중단 최소화** — 동적 폼, DB 기반 설정, 메뉴 동적 관리
2. **추적 가능성** — 모든 변경 이력 자동 적재, 감사 로그
3. **권한 최소화** — RBAC, 이중 방어 (프론트 가드 + 백엔드 Interceptor)
4. **확장 가능한 구조** — 공통코드, 게시판 빌더, JSON 스키마 동적 폼
5. **물리적 삭제 금지** — status / is_active 로 비활성 처리
6. **낙관적 락** — 동시 수정 충돌 방지
