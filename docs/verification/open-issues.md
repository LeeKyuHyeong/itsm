# 미검증·보류·알려진 문제

| ID | 상태 | 도메인 | 내용 | 미검증/보류 이유 | 확인 방법 | 등록 | 해결 기록 |
|---|---|---|---|---|---|---|---|
| O-001 | OPEN | Auth/로그 | `MenuAccessInterceptor` 가 authority 에서 `ROLE_` 를 떼어 `tb_menu_access_log.role_cd` 에 저장 → 수정 후 `PM` 이 저장됨. DDL 주석은 `ROLE_ADMIN` 형태 | 기존 테스트 `MenuAccessInterceptorTest` 가 `PM` 을 기대 — 기대값 변경은 허가 필요 | 결정 후 인터셉터가 authority 를 그대로 저장하도록 바꾸고 테스트 기대값을 `ROLE_PM` 으로 | 2026-09-17 | |
| O-002 | OPEN | Auth | 운영 admin 으로 계정 생성 성공 여부(시나리오 6) | 배포 후 본인 실행 필요 | records/2026-09-17 §6 | 2026-09-17 | |
