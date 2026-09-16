# 미검증·보류·알려진 문제

| ID | 상태 | 도메인 | 내용 | 미검증/보류 이유 | 확인 방법 | 등록 | 해결 기록 |
|---|---|---|---|---|---|---|---|
| O-001 | OPEN | Auth/로그 | `MenuAccessInterceptor` 가 authority 에서 `ROLE_` 를 떼어 `tb_menu_access_log.role_cd` 에 저장 → 수정 후 `PM` 이 저장됨. DDL 주석은 `ROLE_ADMIN` 형태 | 기존 테스트 `MenuAccessInterceptorTest` 가 `PM` 을 기대 — 기대값 변경은 허가 필요 | 결정 후 인터셉터가 authority 를 그대로 저장하도록 바꾸고 테스트 기대값을 `ROLE_PM` 으로 | 2026-09-17 | |
| O-002 | OPEN | Auth | 운영 admin 으로 계정 생성 성공 여부(시나리오 6) | 배포 후 본인 실행 필요 | records/2026-09-17 §6 | 2026-09-17 | 생성은 성공(본인 확인 09-17). 시나리오 6-4(demo 로그인 후 조회) 는 O-003 차단에 가려져 미확인 |
| O-003 | OPEN | 보안/fail2ban | `fail2ban/filter.d/nginx-scanner.conf` 두 번째 정규식이 **본문 0바이트 403** 을 스캐너로 센다. 운영에서 감사자(ROLE_AUDITOR) 시연 계정으로 로그인한 직후 SPA 의 병렬 API 호출이 403 4건(00:38:45 같은 초)을 남겨 집 IP 가 80/443 24시간 차단됨(`Ban 121.165.29.78`). 권한 부족한 정상 사용자가 서버 전체에서 차단되는 구조 | 결정 필요: ① 필터에서 `403 0` 제거 ② API 403 에 본문 부여 ③ 감사자 로그인 직후 어떤 API 가 403 인지(메뉴 매트릭스 `tb_role_menu`) 규명. itsm 은 09-17 이력서 제외 상태라 우선순위 본인 결정 | 운영 `docker logs itsm-fail2ban \| grep Found` + nginx access.log 의 403 줄(경로 확인) | 2026-09-17 | |
