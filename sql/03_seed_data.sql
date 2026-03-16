-- ============================================================
-- ITSM 시드 데이터 (Base Data)
-- 실행 순서: 회사 → 부서 → 사용자 → 역할매핑 → 자산(HW/SW/OA) → 연관관계 → 장애 → 서비스요청
-- ============================================================

-- ============================================================
-- 1. 회사 (company_id 2~5)
-- ============================================================
INSERT INTO tb_company (company_id, company_nm, biz_no, ceo_nm, tel, status, created_at, created_by) VALUES
(2, '한국금융시스템', '110-81-12345', '김재현', '02-1234-5678', 'ACTIVE', NOW(), 1),
(3, '삼성전자 서비스', '124-86-54321', '박성민', '031-200-1234', 'ACTIVE', NOW(), 1),
(4, 'LG CNS',         '110-81-98765', '이정환', '02-2099-5000', 'ACTIVE', NOW(), 1),
(5, 'SK텔레콤',       '104-81-33333', '유영상', '02-6100-2114', 'ACTIVE', NOW(), 1);

-- ============================================================
-- 2. 부서 (dept_id 2~10)
-- ============================================================
INSERT INTO tb_department (dept_id, dept_nm, company_id, status, created_at, created_by) VALUES
(2,  '인프라운영팀',    1, 'ACTIVE', NOW(), 1),
(3,  '보안팀',          1, 'ACTIVE', NOW(), 1),
(4,  '개발팀',          1, 'ACTIVE', NOW(), 1),
(5,  'IT전략팀',        2, 'ACTIVE', NOW(), 1),
(6,  '전산실',          2, 'ACTIVE', NOW(), 1),
(7,  '기술지원팀',      3, 'ACTIVE', NOW(), 1),
(8,  '클라우드사업부',  4, 'ACTIVE', NOW(), 1),
(9,  '네트워크팀',      5, 'ACTIVE', NOW(), 1),
(10, 'IT기획팀',        5, 'ACTIVE', NOW(), 1);

-- ============================================================
-- 3. 사용자 (user_id 2~15)
--    비밀번호: 모두 'password' (BCrypt)
-- ============================================================
INSERT INTO tb_user (user_id, login_id, password, user_nm, employee_no, dept_id, email, tel, status, valid_from, created_at, created_by) VALUES
(2,  'pmkim',     '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '김철수',     'EMP0002', 2,  'pmkim@itsm.local',      '010-1111-0002', 'ACTIVE', NOW(), NOW(), 1),
(3,  'devlee',    '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '이영희',     'EMP0003', 4,  'devlee@itsm.local',     '010-1111-0003', 'ACTIVE', NOW(), NOW(), 1),
(4,  'secpark',   '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '박보안',     'EMP0004', 3,  'secpark@itsm.local',    '010-1111-0004', 'ACTIVE', NOW(), NOW(), 1),
(5,  'dbachoi',   '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '최디비',     'EMP0005', 2,  'dbachoi@itsm.local',    '010-1111-0005', 'ACTIVE', NOW(), NOW(), 1),
(6,  'netjung',   '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '정네트',     'EMP0006', 2,  'netjung@itsm.local',    '010-1111-0006', 'ACTIVE', NOW(), NOW(), 1),
(7,  'srvhan',    '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '한서버',     'EMP0007', 2,  'srvhan@itsm.local',     '010-1111-0007', 'ACTIVE', NOW(), NOW(), 1),
(8,  'customer1', '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '고객일',     'EMP1001', 5,  'cust1@hkfs.co.kr',      '010-2222-0001', 'ACTIVE', NOW(), NOW(), 1),
(9,  'customer2', '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '고객이',     'EMP1002', 6,  'cust2@hkfs.co.kr',      '010-2222-0002', 'ACTIVE', NOW(), NOW(), 1),
(10, 'customer3', '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '김삼성',     'EMP2001', 7,  'kim@samsung-svc.co.kr', '010-3333-0001', 'ACTIVE', NOW(), NOW(), 1),
(11, 'customer4', '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '이엘지',     'EMP3001', 8,  'lee@lgcns.com',         '010-4444-0001', 'ACTIVE', NOW(), NOW(), 1),
(12, 'customer5', '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '박에스케이', 'EMP4001', 9,  'park@skt.co.kr',        '010-5555-0001', 'ACTIVE', NOW(), NOW(), 1),
(13, 'auditor1',  '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '감사원',     'EMP0010', 1,  'auditor@itsm.local',    '010-1111-0010', 'ACTIVE', NOW(), NOW(), 1),
(14, 'devjang',   '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '장개발',     'EMP0008', 4,  'devjang@itsm.local',    '010-1111-0008', 'ACTIVE', NOW(), NOW(), 1),
(15, 'itsadmin',  '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '관리자B',    'EMP0009', 1,  'itsadmin@itsm.local',   '010-1111-0009', 'ACTIVE', NOW(), NOW(), 1);

-- ============================================================
-- 4. 사용자-역할 매핑
-- ============================================================
INSERT INTO tb_user_role (user_id, role_id, granted_at, granted_by) VALUES
(2,  3,  NOW(), 1),   -- 김철수: PM
(3,  4,  NOW(), 1),   -- 이영희: 개발자
(4,  5,  NOW(), 1),   -- 박보안: 보안담당
(5,  6,  NOW(), 1),   -- 최디비: DBA
(6,  7,  NOW(), 1),   -- 정네트: 네트워크
(7,  8,  NOW(), 1),   -- 한서버: 서버
(8,  9,  NOW(), 1),   -- 고객일: 고객
(9,  9,  NOW(), 1),   -- 고객이: 고객
(10, 9,  NOW(), 1),   -- 김삼성: 고객
(11, 9,  NOW(), 1),   -- 이엘지: 고객
(12, 9,  NOW(), 1),   -- 박에스케이: 고객
(13, 11, NOW(), 1),   -- 감사원: 감사자
(14, 4,  NOW(), 1),   -- 장개발: 개발자
(15, 2,  NOW(), 1);   -- 관리자B: ITSM관리자

-- 회사 대표PM 지정
UPDATE tb_company SET default_pm_id = 2 WHERE company_id IN (2, 3, 4, 5);

-- ============================================================
-- 5. HW 자산 (20건)
-- ============================================================
INSERT INTO tb_asset_hw (asset_hw_id, asset_nm, asset_type_cd, asset_category, asset_sub_category, manufacturer, model_nm, serial_no, ip_address, mac_address, location, introduced_at, warranty_end_at, company_id, manager_id, status, description, created_at, created_by) VALUES
-- 서버 6대
(1,  'WEB-SVR-01',    'SERVER',   'INFRA_HW', NULL, 'Dell',       'PowerEdge R750',    'SVR-2024-001', '10.1.1.11',  'AA:BB:CC:01:01:01', 'IDC A동 1층 랙A01', '2024-01-15', '2027-01-15', 2, 7,  'ACTIVE',   '한국금융 웹서버 #1',           NOW(), 1),
(2,  'WEB-SVR-02',    'SERVER',   'INFRA_HW', NULL, 'Dell',       'PowerEdge R750',    'SVR-2024-002', '10.1.1.12',  'AA:BB:CC:01:01:02', 'IDC A동 1층 랙A01', '2024-01-15', '2027-01-15', 2, 7,  'ACTIVE',   '한국금융 웹서버 #2',           NOW(), 1),
(3,  'DB-SVR-01',     'SERVER',   'INFRA_HW', NULL, 'HP',         'ProLiant DL380 G10','SVR-2023-010', '10.1.2.21',  'AA:BB:CC:02:02:01', 'IDC A동 1층 랙A03', '2023-06-01', '2026-06-01', 2, 5,  'ACTIVE',   '한국금융 DB서버 (Oracle)',      NOW(), 1),
(4,  'AP-SVR-01',     'SERVER',   'INFRA_HW', NULL, 'Dell',       'PowerEdge R650',    'SVR-2024-015', '10.2.1.11',  'AA:BB:CC:03:01:01', 'IDC B동 2층 랙B02', '2024-03-20', '2027-03-20', 3, 7,  'ACTIVE',   '삼성서비스 AP서버',            NOW(), 1),
(5,  'BATCH-SVR-01',  'SERVER',   'INFRA_HW', NULL, 'HP',         'ProLiant DL360 G10','SVR-2023-020', '10.3.1.11',  'AA:BB:CC:04:01:01', 'IDC B동 2층 랙B05', '2023-09-10', '2026-09-10', 4, 7,  'ACTIVE',   'LG CNS 배치서버',              NOW(), 1),
(6,  'MAIL-SVR-01',   'SERVER',   'INFRA_HW', NULL, 'Lenovo',     'ThinkSystem SR650', 'SVR-2024-030', '10.4.1.11',  'AA:BB:CC:05:01:01', 'IDC A동 1층 랙A05', '2024-05-01', '2027-05-01', 5, 7,  'ACTIVE',   'SK텔레콤 메일서버',            NOW(), 1),
-- 네트워크 4대
(7,  'CORE-SW-01',    'NETWORK',  'INFRA_HW', NULL, 'Cisco',      'Catalyst 9500',     'NET-2023-001', '10.0.0.1',   'CC:DD:EE:01:01:01', 'IDC A동 1층 MDF',   '2023-03-01', '2028-03-01', 1, 6,  'ACTIVE',   '코어 스위치 #1',               NOW(), 1),
(8,  'CORE-SW-02',    'NETWORK',  'INFRA_HW', NULL, 'Cisco',      'Catalyst 9500',     'NET-2023-002', '10.0.0.2',   'CC:DD:EE:01:01:02', 'IDC A동 1층 MDF',   '2023-03-01', '2028-03-01', 1, 6,  'ACTIVE',   '코어 스위치 #2 (이중화)',       NOW(), 1),
(9,  'ACC-SW-01',     'NETWORK',  'INFRA_HW', NULL, 'Juniper',    'EX4300-48T',        'NET-2024-010', '10.0.0.11',  'CC:DD:EE:02:01:01', 'IDC A동 2층 IDF',   '2024-02-15', '2027-02-15', 1, 6,  'ACTIVE',   '액세스 스위치 A동 2층',         NOW(), 1),
(10, 'VPN-GW-01',     'NETWORK',  'INFRA_HW', NULL, 'Cisco',      'ASA 5525-X',        'NET-2022-005', '10.0.0.100', 'CC:DD:EE:03:01:01', 'IDC A동 1층 MDF',   '2022-06-01', '2025-06-01', 1, 6,  'INACTIVE', 'VPN 게이트웨이 (교체 예정)',    NOW(), 1),
-- 스토리지 4대
(11, 'SAN-STG-01',    'STORAGE',  'INFRA_HW', NULL, 'NetApp',     'FAS8700',           'STG-2023-001', '10.1.5.10',  'DD:EE:FF:01:01:01', 'IDC A동 1층 랙A10', '2023-04-01', '2028-04-01', 2, 5,  'ACTIVE',   '한국금융 SAN 스토리지',         NOW(), 1),
(12, 'NAS-STG-01',    'STORAGE',  'INFRA_HW', NULL, 'Synology',   'SA3600',            'STG-2024-005', '10.1.5.20',  'DD:EE:FF:02:01:01', 'IDC A동 1층 랙A10', '2024-01-10', '2027-01-10', 2, 5,  'ACTIVE',   '한국금융 NAS (파일서버)',       NOW(), 1),
(13, 'BKP-STG-01',    'STORAGE',  'INFRA_HW', NULL, 'Dell EMC',   'Data Domain 6300',  'STG-2023-010', '10.0.5.30',  'DD:EE:FF:03:01:01', 'IDC B동 1층 랙C01', '2023-07-15', '2026-07-15', 1, 5,  'ACTIVE',   '백업 스토리지',                NOW(), 1),
(14, 'SAN-STG-02',    'STORAGE',  'INFRA_HW', NULL, 'Dell EMC',   'Unity XT 480',      'STG-2022-001', '10.3.5.10',  'DD:EE:FF:04:01:01', 'IDC B동 2층 랙B10', '2022-02-01', '2025-02-01', 4, 5,  'INACTIVE', 'LG CNS 스토리지 (노후)',       NOW(), 1),
-- 보안장비 3대
(15, 'FW-01',         'SECURITY', 'INFRA_HW', NULL, 'Fortinet',   'FortiGate 600E',    'SEC-2023-001', '10.0.0.201', 'EE:FF:00:01:01:01', 'IDC A동 1층 MDF',   '2023-05-01', '2026-05-01', 1, 4,  'ACTIVE',   '외부 방화벽',                  NOW(), 1),
(16, 'IPS-01',        'SECURITY', 'INFRA_HW', NULL, 'Palo Alto',  'PA-3260',           'SEC-2024-005', '10.0.0.202', 'EE:FF:00:02:01:01', 'IDC A동 1층 MDF',   '2024-01-20', '2027-01-20', 1, 4,  'ACTIVE',   'IPS 장비',                     NOW(), 1),
(17, 'WAF-01',        'SECURITY', 'INFRA_HW', NULL, 'F5 Networks', 'BIG-IP i5800',     'SEC-2024-010', '10.0.0.203', 'EE:FF:00:03:01:01', 'IDC A동 1층 MDF',   '2024-06-01', '2027-06-01', 1, 4,  'ACTIVE',   'WAF 장비',                     NOW(), 1),
-- PC 3대
(18, 'IDC-PC-01',     'PC',       'INFRA_HW', NULL, 'Dell',       'OptiPlex 7090',     'PC-2024-001',  '10.0.10.101','FF:00:11:01:01:01',  'IDC A동 관제실',    '2024-02-01', '2027-02-01', 1, 7,  'ACTIVE',   'IDC 관제실 PC #1',             NOW(), 1),
(19, 'IDC-PC-02',     'PC',       'INFRA_HW', NULL, 'Dell',       'OptiPlex 7090',     'PC-2024-002',  '10.0.10.102','FF:00:11:01:01:02',  'IDC A동 관제실',    '2024-02-01', '2027-02-01', 1, 7,  'ACTIVE',   'IDC 관제실 PC #2',             NOW(), 1),
(20, 'IDC-PC-OLD',    'PC',       'INFRA_HW', NULL, 'HP',         'EliteDesk 800 G5',  'PC-2020-010',  NULL,         'FF:00:11:02:01:01',  'IDC A동 창고',      '2020-03-01', '2023-03-01', 1, NULL,'DISPOSED', '폐기된 관제실 구형 PC',        NOW(), 1);

-- ============================================================
-- 6. SW 자산 (15건)
-- ============================================================
INSERT INTO tb_asset_sw (asset_sw_id, sw_nm, sw_type_cd, asset_category, asset_sub_category, version, license_key, license_cnt, installed_at, expired_at, company_id, manager_id, status, description, created_at, created_by) VALUES
-- OS 3건
(1,  'Red Hat Enterprise Linux',  'OS',       'INFRA_SW', NULL, '8.9',   'RHEL-2024-XXXX-YYYY-ZZZZ', 10, '2024-01-15', '2027-01-15', 2, 7,  'ACTIVE',   'RHEL 서버 라이선스',             NOW(), 1),
(2,  'Windows Server',            'OS',       'INFRA_SW', NULL, '2022',  'WSVR-N9GH-J4KL-M2NP-Q8RS',  5, '2024-03-01', '2027-03-01', 3, 7,  'ACTIVE',   'Windows Server Datacenter',      NOW(), 1),
(3,  'Ubuntu Server',             'OS',       'INFRA_SW', NULL, '22.04 LTS', NULL,                     0, '2023-09-10', NULL,         4, 7,  'ACTIVE',   'Ubuntu 오픈소스 (무료)',          NOW(), 1),
-- DB 3건
(4,  'Oracle Database',           'DB',       'INFRA_SW', NULL, '19c',   'ORCL-A1B2-C3D4-E5F6-G7H8', 2, '2023-06-01', '2026-06-01', 2, 5,  'ACTIVE',   'Oracle EE 프로세서 라이선스',     NOW(), 1),
(5,  'PostgreSQL',                'DB',       'INFRA_SW', NULL, '16.2',  NULL,                         0, '2024-03-20', NULL,         3, 5,  'ACTIVE',   'PostgreSQL 오픈소스',             NOW(), 1),
(6,  'MariaDB',                   'DB',       'INFRA_SW', NULL, '10.11', NULL,                         0, '2023-09-10', NULL,         4, 5,  'ACTIVE',   'MariaDB 오픈소스',               NOW(), 1),
-- WAS 3건
(7,  'Apache Tomcat',             'WAS',      'INFRA_SW', NULL, '10.1',  NULL,                         0, '2024-01-15', NULL,         2, 7,  'ACTIVE',   'Tomcat (웹서버 #1,#2)',           NOW(), 1),
(8,  'Nginx',                     'WAS',      'INFRA_SW', NULL, '1.25',  NULL,                         0, '2024-03-20', NULL,         3, 7,  'ACTIVE',   'Nginx 리버스프록시',              NOW(), 1),
(9,  'JBoss EAP',                 'WAS',      'INFRA_SW', NULL, '8.0',   'JBOS-P7Q8-R9S0-T1U2-V3W4', 3, '2023-09-10', '2026-09-10', 4, 7,  'ACTIVE',   'JBoss EAP 서브스크립션',          NOW(), 1),
-- 보안 3건
(10, 'AhnLab V3 Endpoint',       'SECURITY', 'INFRA_SW', NULL, '9.0',   'V3EP-X1Y2-Z3A4-B5C6-D7E8', 100, '2024-04-01', '2025-04-01', 1, 4, 'ACTIVE',   'V3 엔드포인트 전사 라이선스',    NOW(), 1),
(11, 'CrowdStrike Falcon',       'SECURITY', 'INFRA_SW', NULL, '6.x',   'CRWD-F8G9-H0I1-J2K3-L4M5',  20, '2024-06-01', '2025-06-01', 2, 4, 'ACTIVE',   'CrowdStrike 서버 EDR',           NOW(), 1),
(12, 'Symantec Endpoint',        'SECURITY', 'INFRA_SW', NULL, '14.3',  'SYMC-N5O6-P7Q8-R9S0-T1U2',  50, '2022-01-01', '2025-01-01', 1, 4, 'DISPOSED', 'Symantec (V3로 교체됨)',          NOW(), 1),
-- 오피스/기타 3건
(13, 'Microsoft 365 Business',   'OFFICE',   'INFRA_SW', NULL, 'E3',    'M365-V2W3-X4Y5-Z6A7-B8C9', 200, '2024-01-01', '2025-12-31', 1, 2, 'ACTIVE',   'M365 E3 전사 구독',              NOW(), 1),
(14, 'Atlassian Jira',           'OTHER',    'INFRA_SW', NULL, '9.x',   'JIRA-D9E0-F1G2-H3I4-J5K6',  50, '2024-03-01', '2025-03-01', 1, 2, 'ACTIVE',   'Jira 프로젝트 관리',             NOW(), 1),
(15, 'Confluence',               'OTHER',    'INFRA_SW', NULL, '8.x',   'CONF-L6M7-N8O9-P0Q1-R2S3',  50, '2024-03-01', '2025-03-01', 1, 2, 'INACTIVE', 'Confluence (Wiki → Notion 전환중)', NOW(), 1);

-- ============================================================
-- 7. HW-SW 연관관계 (10건)
-- ============================================================
INSERT INTO tb_asset_relation (asset_hw_id, asset_sw_id, installed_at, removed_at, created_at, created_by) VALUES
(1,  1,  '2024-01-15', NULL, NOW(), 1),   -- WEB-SVR-01 ← RHEL
(1,  7,  '2024-01-15', NULL, NOW(), 1),   -- WEB-SVR-01 ← Tomcat
(2,  1,  '2024-01-15', NULL, NOW(), 1),   -- WEB-SVR-02 ← RHEL
(2,  7,  '2024-01-15', NULL, NOW(), 1),   -- WEB-SVR-02 ← Tomcat
(3,  1,  '2023-06-01', NULL, NOW(), 1),   -- DB-SVR-01  ← RHEL
(3,  4,  '2023-06-01', NULL, NOW(), 1),   -- DB-SVR-01  ← Oracle
(4,  2,  '2024-03-20', NULL, NOW(), 1),   -- AP-SVR-01  ← Windows Server
(4,  5,  '2024-03-20', NULL, NOW(), 1),   -- AP-SVR-01  ← PostgreSQL
(5,  3,  '2023-09-10', NULL, NOW(), 1),   -- BATCH-SVR  ← Ubuntu
(5,  6,  '2023-09-10', NULL, NOW(), 1);   -- BATCH-SVR  ← MariaDB

-- ============================================================
-- 8. OA 자산 (25건)
-- ============================================================
INSERT INTO tb_asset_oa (asset_oa_id, asset_nm, asset_type_cd, manufacturer, model_nm, serial_no, ip_address, mac_address, location, introduced_at, warranty_end_at, company_id, manager_id, status, description, created_at, created_by) VALUES
-- 데스크톱 5대
(1,  '사무실PC-A01',   'DESKTOP',  'Dell',     'OptiPlex 7010',     'OA-DT-2024-001', '192.168.1.101', '11:22:33:01:01:01', '본사 3층 운영팀',    '2024-02-01', '2027-02-01', 1, 2,  'ACTIVE',   '김철수PM 업무용',             NOW(), 1),
(2,  '사무실PC-A02',   'DESKTOP',  'HP',       'EliteDesk 800 G9',  'OA-DT-2024-002', '192.168.1.102', '11:22:33:01:01:02', '본사 3층 운영팀',    '2024-02-01', '2027-02-01', 1, 3,  'ACTIVE',   '이영희 업무용',               NOW(), 1),
(3,  '사무실PC-A03',   'DESKTOP',  'Lenovo',   'ThinkCentre M90q',  'OA-DT-2024-003', '192.168.1.103', '11:22:33:01:01:03', '본사 3층 개발팀',    '2024-02-01', '2027-02-01', 1, 14, 'ACTIVE',   '장개발 업무용',               NOW(), 1),
(4,  '금융-PC-01',     'DESKTOP',  'Dell',     'OptiPlex 5000',     'OA-DT-2023-010', '192.168.10.101','11:22:33:02:01:01', '한국금융 전산실',    '2023-05-15', '2026-05-15', 2, NULL,'ACTIVE',  '전산실 공용 PC',              NOW(), 1),
(5,  '금융-PC-02',     'DESKTOP',  'Dell',     'OptiPlex 5000',     'OA-DT-2023-011', '192.168.10.102','11:22:33:02:01:02', '한국금융 전산실',    '2023-05-15', '2026-05-15', 2, NULL,'ACTIVE',  '전산실 공용 PC #2',           NOW(), 1),
-- 노트북 5대
(6,  '노트북-PM-01',   'LAPTOP',   'Dell',     'Latitude 7440',     'OA-LT-2024-001', '192.168.1.201', '11:22:33:03:01:01', '본사 3층 운영팀',    '2024-03-01', '2027-03-01', 1, 2,  'ACTIVE',   '김철수PM 외근용',             NOW(), 1),
(7,  '노트북-DEV-01',  'LAPTOP',   'Apple',    'MacBook Pro 14 M3', 'OA-LT-2024-002', '192.168.1.202', '11:22:33:03:01:02', '본사 3층 개발팀',    '2024-04-01', '2027-04-01', 1, 3,  'ACTIVE',   '이영희 개발용',               NOW(), 1),
(8,  '노트북-DEV-02',  'LAPTOP',   'Apple',    'MacBook Pro 16 M3', 'OA-LT-2024-003', '192.168.1.203', '11:22:33:03:01:03', '본사 3층 개발팀',    '2024-04-01', '2027-04-01', 1, 14, 'ACTIVE',   '장개발 개발용',               NOW(), 1),
(9,  '노트북-삼성-01', 'LAPTOP',   'Samsung',  'Galaxy Book3 Pro',  'OA-LT-2023-010', NULL,            '11:22:33:04:01:01', '삼성서비스 사무실',  '2023-08-01', '2026-08-01', 3, 10, 'ACTIVE',   '김삼성 업무용',               NOW(), 1),
(10, '노트북-구형',    'LAPTOP',   'HP',       'EliteBook 840 G7',  'OA-LT-2021-001', NULL,            '11:22:33:05:01:01', '본사 창고',          '2021-03-01', '2024-03-01', 1, NULL,'DISPOSED','교체 후 폐기',                NOW(), 1),
-- 모니터 4대
(11, '모니터-27-01',   'MONITOR',  'Dell',     'U2723QE 27" 4K',   'OA-MN-2024-001', NULL, NULL, '본사 3층 운영팀',   '2024-02-01', '2027-02-01', 1, 2,  'ACTIVE',  '김철수 듀얼모니터 L',          NOW(), 1),
(12, '모니터-27-02',   'MONITOR',  'Dell',     'U2723QE 27" 4K',   'OA-MN-2024-002', NULL, NULL, '본사 3층 운영팀',   '2024-02-01', '2027-02-01', 1, 2,  'ACTIVE',  '김철수 듀얼모니터 R',          NOW(), 1),
(13, '모니터-34-01',   'MONITOR',  'LG',       '34WN80C-B 34" UW', 'OA-MN-2024-003', NULL, NULL, '본사 3층 개발팀',   '2024-04-01', '2027-04-01', 1, 3,  'ACTIVE',  '이영희 울트라와이드',           NOW(), 1),
(14, '모니터-삼성-01', 'MONITOR',  'Samsung',  'S34J55 34"',        'OA-MN-2023-010', NULL, NULL, '삼성서비스 사무실', '2023-08-01', '2026-08-01', 3, 10, 'ACTIVE',  '김삼성 모니터',                NOW(), 1),
-- 프린터 3대
(15, '프린터-3층',     'PRINTER',  'HP',       'LaserJet Pro M404dn','OA-PR-2024-001', '192.168.1.240', '11:22:33:10:01:01', '본사 3층 복도',    '2024-01-10', '2027-01-10', 1, 2,  'ACTIVE',  '3층 공용 레이저 프린터',        NOW(), 1),
(16, '프린터-금융',    'PRINTER',  'Canon',    'imageCLASS MF269dw','OA-PR-2023-005', '192.168.10.240','11:22:33:10:02:01', '한국금융 사무실',  '2023-06-01', '2026-06-01', 2, NULL,'ACTIVE',  '한국금융 프린터',              NOW(), 1),
(17, '프린터-구형',    'PRINTER',  'HP',       'LaserJet Pro M402', 'OA-PR-2021-001', NULL,            NULL,                '본사 창고',        '2021-01-01', '2024-01-01', 1, NULL,'INACTIVE','토너 단종 예정',               NOW(), 1),
-- 복합기 2대
(18, '복합기-3층',     'MFP',      'HP',       'OfficeJet Pro 9025','OA-MF-2024-001', '192.168.1.241', '11:22:33:11:01:01', '본사 3층 탕비실',  '2024-03-01', '2027-03-01', 1, 2,  'ACTIVE',  '3층 복합기 (스캔/팩스)',        NOW(), 1),
(19, '복합기-LGCNS',   'MFP',      'Canon',    'iR-ADV C5560',     'OA-MF-2023-005', '192.168.30.241','11:22:33:11:02:01', 'LG CNS 사무실',    '2023-10-01', '2026-10-01', 4, 11, 'ACTIVE',  'LG CNS 대형복합기',            NOW(), 1),
-- 전화 2대
(20, 'IP전화-운영팀',  'PHONE',    'Cisco',    'IP Phone 8845',     'OA-PH-2024-001', '192.168.1.50',  '11:22:33:20:01:01', '본사 3층 운영팀',  '2024-02-01', '2029-02-01', 1, 2,  'ACTIVE',  '운영팀 IP전화',                NOW(), 1),
(21, 'IP전화-금융',    'PHONE',    'Yealink',  'T46U',              'OA-PH-2023-005', '192.168.10.50', '11:22:33:20:02:01', '한국금융 전산실',  '2023-07-01', '2028-07-01', 2, NULL,'ACTIVE',  '한국금융 전산실 전화',          NOW(), 1),
-- 태블릿 2대
(22, '태블릿-현장점검','TABLET',   'Apple',    'iPad Pro 12.9 M2',  'OA-TB-2024-001', NULL, NULL, '본사 3층 운영팀',  '2024-01-15', '2027-01-15', 1, 2,  'ACTIVE',  '현장점검용 태블릿',            NOW(), 1),
(23, '태블릿-SKT',     'TABLET',   'Samsung',  'Galaxy Tab S9 FE',  'OA-TB-2024-005', NULL, NULL, 'SK텔레콤 사무실',  '2024-06-01', '2027-06-01', 5, 12, 'ACTIVE',  'SK텔레콤 업무용',              NOW(), 1),
-- 프로젝터 1대
(24, '회의실프로젝터',  'PROJECTOR','Epson',    'EB-2265U',          'OA-PJ-2023-001', '192.168.1.245', NULL, '본사 4층 대회의실','2023-05-01', '2026-05-01', 1, NULL,'ACTIVE',  '대회의실 프로젝터',            NOW(), 1),
-- 주변기기 1대
(25, '회의실캠',       'PERIPHERAL','Logitech', 'MeetUp',           'OA-PR-2024-010', '192.168.1.246', NULL, '본사 4층 대회의실','2024-01-10', '2027-01-10', 1, NULL,'INACTIVE','펌웨어 업데이트 중',           NOW(), 1);

-- ============================================================
-- 9. 장애 (12건)
-- ============================================================
INSERT INTO tb_incident (incident_id, title, content, incident_type_cd, priority_cd, status_cd, occurred_at, completed_at, closed_at, sla_deadline_at, company_id, main_manager_id, process_content, created_at, created_by, updated_at, updated_by) VALUES
-- CLOSED (2건)
(1,  '웹서버 #1 다운',
     '한국금융 웹서버 WEB-SVR-01이 오전 9시경 응답 불가 상태. 접속 시 503 에러 발생. 내부 업무 시스템 전면 장애.',
     'SYSTEM_DOWN', 'CRITICAL', 'CLOSED',
     '2026-02-10 09:05:00', '2026-02-10 11:30:00', '2026-02-10 14:00:00', '2026-02-10 13:05:00',
     2, 7,
     'Tomcat 프로세스 OOM으로 중단됨. JVM 힙 메모리 4GB→8GB 증설 후 재기동. GC 로그 분석 결과 메모리 누수 패턴 확인, 개발팀 핫픽스 배포 예정.',
     '2026-02-10 09:10:00', 8, '2026-02-10 14:00:00', 7),

(2,  '메일서버 SMTP 장애',
     'SK텔레콤 메일서버 MAIL-SVR-01에서 외부 발송 메일이 모두 큐에 적체. 약 200건 이상 발송 지연.',
     'SYSTEM_DOWN', 'HIGH', 'CLOSED',
     '2026-02-18 14:20:00', '2026-02-18 18:00:00', '2026-02-19 10:00:00', '2026-02-18 22:20:00',
     5, 7,
     'SMTP relay 서버 인증서 만료로 TLS 핸드셰이크 실패. 인증서 갱신 후 큐 적체 메일 일괄 재전송 완료.',
     '2026-02-18 14:30:00', 12, '2026-02-19 10:00:00', 7),

-- COMPLETED (3건)
(3,  'VPN 접속 불가',
     '삼성전자서비스 직원 VPN 접속 시 "인증 실패" 오류. RADIUS 서버 연동 문제 추정.',
     'NETWORK_ISSUE', 'HIGH', 'COMPLETED',
     '2026-02-25 08:30:00', '2026-02-25 12:00:00', NULL, '2026-02-25 16:30:00',
     3, 6,
     'RADIUS 서버 인증서 갱신 후 VPN 연동 정상화. 영향받은 사용자 50명 접속 확인 완료.',
     '2026-02-25 08:35:00', 10, '2026-02-25 12:00:00', 6),

(4,  '방화벽 정책 오류로 내부 접근 차단',
     'LG CNS 배치서버 BATCH-SVR-01에서 DB서버 접근 불가. 금일 방화벽 정책 변경 후 발생.',
     'SECURITY', 'HIGH', 'COMPLETED',
     '2026-03-01 10:15:00', '2026-03-01 13:00:00', NULL, '2026-03-01 18:15:00',
     4, 4,
     '방화벽 정책 변경 시 source IP 대역 오기재 확인. 10.3.1.0/24 → 10.3.0.0/16으로 수정 적용.',
     '2026-03-01 10:20:00', 11, '2026-03-01 13:00:00', 4),

(5,  'DNS 조회 실패 (간헐적)',
     '삼성전자서비스 내부 DNS 간헐적 응답 지연. nslookup 타임아웃 빈발.',
     'NETWORK_ISSUE', 'MEDIUM', 'COMPLETED',
     '2026-03-05 11:00:00', '2026-03-06 09:30:00', NULL, '2026-03-06 11:00:00',
     3, 6,
     'DNS 캐시 서버 메모리 부족. 캐시 플러시 후 메모리 2GB→4GB 증설. 모니터링 추가.',
     '2026-03-05 11:10:00', 10, '2026-03-06 09:30:00', 6),

-- IN_PROGRESS (3건)
(6,  'DB 응답지연 (Oracle)',
     '한국금융 DB-SVR-01 Oracle 쿼리 응답 시간 평소 대비 5배 이상 증가. AWR 리포트 확인 중.',
     'PERFORMANCE', 'MEDIUM', 'IN_PROGRESS',
     '2026-03-10 15:00:00', NULL, NULL, '2026-03-11 15:00:00',
     2, 5, NULL,
     '2026-03-10 15:05:00', 9, NULL, NULL),

(7,  '스토리지 용량 부족 경고',
     'SK텔레콤 로그 파티션 사용률 92% 도달. 7일 내 100% 예상.',
     'PERFORMANCE', 'MEDIUM', 'IN_PROGRESS',
     '2026-03-12 09:00:00', NULL, NULL, '2026-03-13 09:00:00',
     5, 7, NULL,
     '2026-03-12 09:05:00', 1, NULL, NULL),

(8,  '백업 실패 알림',
     '한국금융 야간 풀백업 3일 연속 실패. Data Domain 연결 타임아웃.',
     'DATA_ISSUE', 'LOW', 'IN_PROGRESS',
     '2026-03-13 07:00:00', NULL, NULL, '2026-03-16 07:00:00',
     2, 5, NULL,
     '2026-03-13 07:10:00', 1, NULL, NULL),

-- RECEIVED (2건)
(9,  'SSL 인증서 만료 임박',
     'LG CNS 외부 서비스 SSL 인증서 2026-03-25 만료 예정. 갱신 작업 필요.',
     'SECURITY', 'MEDIUM', 'RECEIVED',
     '2026-03-14 10:00:00', NULL, NULL, '2026-03-15 10:00:00',
     4, NULL, NULL,
     '2026-03-14 10:00:00', 11, NULL, NULL),

(10, '서버 CPU 과부하 (AP서버)',
     '삼성전자서비스 AP-SVR-01 CPU 사용률 95% 이상 지속. 사용자 응답 지연.',
     'PERFORMANCE', 'HIGH', 'RECEIVED',
     '2026-03-15 16:30:00', NULL, NULL, '2026-03-16 00:30:00',
     3, NULL, NULL,
     '2026-03-15 16:35:00', 10, NULL, NULL),

-- REJECTED (1건)
(11, '사무실 에어컨 고장',
     '본사 3층 에어컨이 작동하지 않습니다. 시설팀에 문의 필요.',
     'OTHER', 'LOW', 'REJECTED',
     '2026-03-08 13:00:00', NULL, NULL, NULL,
     1, NULL,
     '본 건은 IT 장애가 아닌 시설 관련 건으로, 시설관리팀(내선 3333)으로 접수 부탁드립니다.',
     '2026-03-08 13:00:00', 1, '2026-03-08 13:30:00', 1),

-- CRITICAL IN_PROGRESS (1건)
(12, '코어스위치 #1 장애 (긴급)',
     'IDC A동 코어스위치 CORE-SW-01 포트 다수 링크다운. 이중화 절체는 되었으나 대역폭 50% 감소.',
     'NETWORK_ISSUE', 'CRITICAL', 'IN_PROGRESS',
     '2026-03-16 08:00:00', NULL, NULL, '2026-03-16 12:00:00',
     1, 6, NULL,
     '2026-03-16 08:02:00', 1, NULL, NULL);

-- ============================================================
-- 10. 서비스요청 (10건)
-- ============================================================
INSERT INTO tb_service_request (request_id, title, content, request_type_cd, priority_cd, status_cd, occurred_at, completed_at, closed_at, sla_deadline_at, reject_cnt, company_id, satisfaction_score, satisfaction_comment, satisfaction_submitted_at, created_at, created_by, updated_at, updated_by) VALUES
-- CLOSED (2건)
(1,  '신규 입사자 계정 생성',
     '한국금융시스템 IT전략팀 신규 입사자 3명 계정 생성 요청. AD 계정 + VPN + ERP 접근 권한 필요.',
     'ACCOUNT', 'MEDIUM', 'CLOSED',
     '2026-02-03 09:00:00', '2026-02-03 14:00:00', '2026-02-04 10:00:00', '2026-02-04 09:00:00',
     0, 2, 5, '빠른 처리 감사합니다!', '2026-02-04 10:00:00',
     '2026-02-03 09:00:00', 8, '2026-02-04 10:00:00', 2),

(2,  'ERP 시스템 접근 권한 추가',
     '삼성전자서비스 김삼성 ERP 재무모듈 읽기 권한 추가 요청.',
     'ACCOUNT', 'MEDIUM', 'CLOSED',
     '2026-02-15 10:30:00', '2026-02-15 15:00:00', '2026-02-16 09:00:00', '2026-02-16 10:30:00',
     0, 3, 4, '잘 처리되었습니다.', '2026-02-16 09:00:00',
     '2026-02-15 10:30:00', 10, '2026-02-16 09:00:00', 2),

-- PENDING_COMPLETE (2건)
(3,  '모니터 교체 요청',
     '한국금융 전산실 모니터 1대 화면 깜빡임 증상. 교체 요청.',
     'INSTALL', 'LOW', 'PENDING_COMPLETE',
     '2026-03-01 11:00:00', '2026-03-03 10:00:00', NULL, '2026-03-04 11:00:00',
     0, 2, NULL, NULL, NULL,
     '2026-03-01 11:00:00', 9, '2026-03-03 10:00:00', 2),

(4,  'VPN 분할 터널링 설정 변경',
     'LG CNS 이엘지 VPN 사용 시 내부 네트워크만 VPN 경유하도록 split tunnel 설정 요청.',
     'CONFIGURATION', 'MEDIUM', 'PENDING_COMPLETE',
     '2026-03-05 14:00:00', '2026-03-06 11:00:00', NULL, '2026-03-06 14:00:00',
     0, 4, NULL, NULL, NULL,
     '2026-03-05 14:00:00', 11, '2026-03-06 11:00:00', 6),

-- IN_PROGRESS (2건)
(5,  'DB 데이터 추출 요청',
     'SK텔레콤 2025년 12월~2026년 2월 통화 이력 데이터 CSV 추출 요청. 마케팅 분석용.',
     'DATA_REQUEST', 'LOW', 'IN_PROGRESS',
     '2026-03-10 09:00:00', NULL, NULL, '2026-03-13 09:00:00',
     0, 5, NULL, NULL, NULL,
     '2026-03-10 09:00:00', 12, NULL, NULL),

(6,  '프린터 드라이버 설치 요청',
     '본사 3층 신규 프린터 HP LaserJet Pro M404dn 드라이버 설치 요청. 사무실PC 5대.',
     'INSTALL', 'LOW', 'IN_PROGRESS',
     '2026-03-11 13:30:00', NULL, NULL, '2026-03-14 13:30:00',
     0, 1, NULL, NULL, NULL,
     '2026-03-11 13:30:00', 1, NULL, NULL),

-- ASSIGNED (2건)
(7,  '이메일 배포 그룹 생성',
     '한국금융 IT전략팀 메일링 리스트 신규 생성 요청. 그룹명: it-strategy@hkfs.co.kr, 구성원 8명.',
     'CONFIGURATION', 'LOW', 'ASSIGNED',
     '2026-03-12 10:00:00', NULL, NULL, '2026-03-15 10:00:00',
     0, 2, NULL, NULL, NULL,
     '2026-03-12 10:00:00', 8, '2026-03-12 11:00:00', 2),

(8,  '서버 SSH 접근 권한 요청',
     '삼성전자서비스 김삼성 AP-SVR-01 SSH 접근 권한 요청. 애플리케이션 로그 확인 목적.',
     'ACCOUNT', 'MEDIUM', 'ASSIGNED',
     '2026-03-13 15:00:00', NULL, NULL, '2026-03-14 15:00:00',
     0, 3, NULL, NULL, NULL,
     '2026-03-13 15:00:00', 10, '2026-03-13 16:00:00', 2),

-- RECEIVED (1건)
(9,  '네트워크 포트 개통 요청',
     'LG CNS 클라우드사업부 신규 서버 네트워크 포트 2개 개통 요청. VLAN 30, 10Gbps.',
     'CONFIGURATION', 'MEDIUM', 'RECEIVED',
     '2026-03-15 11:00:00', NULL, NULL, '2026-03-16 11:00:00',
     0, 4, NULL, NULL, NULL,
     '2026-03-15 11:00:00', 11, NULL, NULL),

-- CANCELLED (1건)
(10, 'MS Office 재설치 (취소)',
     'SK텔레콤 MS Office 재설치 요청. → 자체 해결하여 취소합니다.',
     'INSTALL', 'LOW', 'CANCELLED',
     '2026-03-08 16:00:00', NULL, NULL, NULL,
     0, 5, NULL, NULL, NULL,
     '2026-03-08 16:00:00', 12, '2026-03-08 17:00:00', 12);
