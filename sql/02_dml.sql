-- ============================================================
-- ITSM DML Script (MySQL/MariaDB)
-- 초기 데이터 적재: 역할, 메뉴, 슈퍼관리자, 공통코드, SLA 정책
-- ============================================================

-- ============================================================
-- 1. 슈퍼관리자 계정 (admin / admin123!@#)
--    비밀번호: BCrypt 암호화 ($2a$10$ prefix)
-- ============================================================

INSERT INTO tb_company (company_id, company_nm, biz_no, ceo_nm, tel, status, created_at)
VALUES (1, 'ITSM 운영사', '000-00-00000', '관리자', '000-0000-0000', 'ACTIVE', NOW());

INSERT INTO tb_department (dept_id, dept_nm, company_id, status, created_at)
VALUES (1, '시스템관리', 1, 'ACTIVE', NOW());

INSERT INTO tb_user (user_id, login_id, password, user_nm, employee_no, dept_id, email, tel, status, valid_from, created_at)
VALUES (1, 'admin', '$2a$10$c5Jwkaus0cKAjnVAyj8mrOAEWAdvJuXbeKYEcV5fGzYz7Y0TGcYsG', '슈퍼관리자', 'EMP0001', 1, 'admin@itsm.local', '000-0000-0000', 'ACTIVE', NOW(), NOW());

-- ============================================================
-- 2. 역할 (11개)
-- ============================================================

INSERT INTO tb_role (role_id, role_nm, role_cd, description, status, created_at, created_by) VALUES
(1,  '슈퍼관리자',         'ROLE_SUPER_ADMIN',   '시스템 전체 설정, ITSM관리자 계정 관리',  'ACTIVE', NOW(), 1),
(2,  'ITSM관리자',         'ROLE_ITSM_ADMIN',    '운영 전반 관리',                          'ACTIVE', NOW(), 1),
(3,  '유지보수팀 - PM',    'ROLE_PM',            '프로젝트 관리',                           'ACTIVE', NOW(), 1),
(4,  '유지보수팀 - 개발자','ROLE_DEVELOPER',      '개발 담당',                               'ACTIVE', NOW(), 1),
(5,  '유지보수팀 - 보안담당자','ROLE_SECURITY',   '보안 담당',                               'ACTIVE', NOW(), 1),
(6,  '유지보수팀 - DB담당자',  'ROLE_DBA',        'DB 담당',                                 'ACTIVE', NOW(), 1),
(7,  '유지보수팀 - 네트워크담당자','ROLE_NETWORK', '네트워크 담당',                           'ACTIVE', NOW(), 1),
(8,  '유지보수팀 - 서버담당자',  'ROLE_SERVER',   '서버 담당',                               'ACTIVE', NOW(), 1),
(9,  '고객사 인원',         'ROLE_CUSTOMER',      '부서별 일반 사용자',                      'ACTIVE', NOW(), 1),
(10, '외부사용자',          'ROLE_EXTERNAL',      '협력업체 등 정기점검 인원',               'ACTIVE', NOW(), 1),
(11, '감사자',              'ROLE_AUDITOR',       '읽기 전용, 전체 이력 조회',               'ACTIVE', NOW(), 1);

-- ============================================================
-- 3. 슈퍼관리자 역할 부여
-- ============================================================

INSERT INTO tb_user_role (user_id, role_id, granted_at, granted_by)
VALUES (1, 1, NOW(), 1);

-- ============================================================
-- 4. 메뉴 (전체 트리)
-- ============================================================

-- 1레벨 메뉴
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by) VALUES
(1,  NULL, '대시보드',       'Dashboard',              '/dashboard',        'mdi-view-dashboard',  1,  'Y', 'ACTIVE', NOW(), 1),
(2,  NULL, '장애관리',       'Incident Management',    NULL,                'mdi-alert-circle',    2,  'Y', 'ACTIVE', NOW(), 1),
(3,  NULL, '서비스요청',     'Service Request',        NULL,                'mdi-file-document',   3,  'Y', 'ACTIVE', NOW(), 1),
(4,  NULL, '변경관리',       'Change Management',      NULL,                'mdi-swap-horizontal', 4,  'Y', 'ACTIVE', NOW(), 1),
(5,  NULL, '자산관리',       'Asset Management',       NULL,                'mdi-server',          5,  'Y', 'ACTIVE', NOW(), 1),
(6,  NULL, '정기점검관리',   'Inspection Management',  NULL,                'mdi-clipboard-check', 6,  'Y', 'ACTIVE', NOW(), 1),
(7,  NULL, '보고관리',       'Report Management',      NULL,                'mdi-chart-bar',       7,  'Y', 'ACTIVE', NOW(), 1),
(8,  NULL, '게시판',         'Board',                  NULL,                'mdi-bulletin-board',  8,  'Y', 'ACTIVE', NOW(), 1),
(9,  NULL, '설정관리',       'Settings',               NULL,                'mdi-cog',             9,  'Y', 'ACTIVE', NOW(), 1),
(10, NULL, '계정관리',       'Account Management',     NULL,                'mdi-account-group',   10, 'Y', 'ACTIVE', NOW(), 1);

-- 2레벨 메뉴: 장애관리
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by) VALUES
(11, 2, '장애 목록',     'Incident List',          '/incidents',        NULL, 1, 'Y', 'ACTIVE', NOW(), 1),
(12, 2, '장애 등록',     'Register Incident',      '/incidents/new',    NULL, 2, 'Y', 'ACTIVE', NOW(), 1);

-- 2레벨 메뉴: 서비스요청
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by) VALUES
(14, 3, '요청 목록',     'Request List',           '/service-requests',       NULL, 1, 'Y', 'ACTIVE', NOW(), 1),
(15, 3, '요청 등록',     'Register Request',       '/service-requests/new',   NULL, 2, 'Y', 'ACTIVE', NOW(), 1);

-- 2레벨 메뉴: 변경관리
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by) VALUES
(16, 4, '변경 목록',     'Change List',            '/changes',        NULL, 1, 'Y', 'ACTIVE', NOW(), 1),
(17, 4, '변경 등록',     'Register Change',        '/changes/new',    NULL, 2, 'Y', 'ACTIVE', NOW(), 1);

-- 2레벨 메뉴: 자산관리 (CMDB)
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by) VALUES
(18, 5, 'HW 자산 목록',   'HW Asset List',        '/assets/hw',          NULL, 1, 'Y', 'ACTIVE', NOW(), 1),
(19, 5, 'SW 자산 목록',   'SW Asset List',        '/assets/sw',          NULL, 2, 'Y', 'ACTIVE', NOW(), 1),
(20, 5, 'OA 자산 목록',   'OA Asset List',        '/assets/oa',          NULL, 3, 'Y', 'ACTIVE', NOW(), 1);

-- 2레벨 메뉴: 정기점검관리
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by) VALUES
(21, 6, '점검 목록',       'Inspection List',      '/inspections',          NULL, 1, 'Y', 'ACTIVE', NOW(), 1),
(22, 6, '점검 등록',       'Register Inspection',  '/inspections/new',      NULL, 2, 'Y', 'ACTIVE', NOW(), 1);

-- 2레벨 메뉴: 보고관리
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by) VALUES
(23, 7, '보고서 목록',         'Report List',      '/reports',          NULL, 1, 'Y', 'ACTIVE', NOW(), 1);

-- 2레벨 메뉴: 설정관리
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by) VALUES
(25, 9, '메뉴 관리',            'Menu Management',        '/admin/menus',                  NULL, 1, 'Y', 'ACTIVE', NOW(), 1),
(27, 9, '공통코드 관리',        'Common Code',            '/admin/common-codes',           NULL, 2, 'Y', 'ACTIVE', NOW(), 1),
(30, 9, '게시판 관리',          'Board Management',       '/admin/boards',                 NULL, 3, 'Y', 'ACTIVE', NOW(), 1),
(32, 9, 'SLA 관리',             'SLA Management',         '/admin/sla',                    NULL, 4, 'Y', 'ACTIVE', NOW(), 1),
(33, 9, '알림 정책 관리',       'Notification Policy',    '/admin/notification-policy',    NULL, 5, 'Y', 'ACTIVE', NOW(), 1),
(41, 9, '배치 관리',           'Batch Management',       '/admin/batch-jobs',             NULL, 6, 'Y', 'ACTIVE', NOW(), 1);

-- 2레벨 메뉴: 계정관리
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by) VALUES
(39, 10, '사용자 관리',        'User Management',        '/admin/accounts',           NULL, 1, 'Y', 'ACTIVE', NOW(), 1),
(40, 10, '조직 관리',          'Organization Management', '/admin/organizations',      NULL, 2, 'Y', 'ACTIVE', NOW(), 1);

-- ============================================================
-- 5. 역할-메뉴 매핑 (RBAC)
--    ITSM.md 역할별 메뉴 접근 권한 테이블 기준
-- ============================================================

-- 슈퍼관리자 (ROLE_SUPER_ADMIN, role_id=1) : 전체 메뉴 읽기+쓰기
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 1, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu;

-- ITSM관리자 (ROLE_ITSM_ADMIN, role_id=2) : 전체 메뉴 읽기+쓰기
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 2, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu;

-- PM (role_id=3) : 대시보드, 장애관리, 서비스요청, 변경관리, 자산관리, 정기점검관리, 보고관리
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 3, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu
WHERE menu_id IN (1, 2,11,12,13, 3,14,15, 4,16,17, 5,18,19,20, 6,21,22, 7,23,24);

-- 개발자 (role_id=4) : 대시보드, 장애관리, 서비스요청, 변경관리, 자산관리
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 4, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu
WHERE menu_id IN (1, 2,11,12,13, 3,14,15, 4,16,17, 5,18,19,20);

-- 보안담당자 (role_id=5) : 대시보드, 장애관리, 서비스요청, 변경관리, 자산관리, 정기점검관리
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 5, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu
WHERE menu_id IN (1, 2,11,12,13, 3,14,15, 4,16,17, 5,18,19,20, 6,21,22);

-- DB담당자 (role_id=6) : 대시보드, 장애관리, 서비스요청, 변경관리, 자산관리, 정기점검관리
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 6, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu
WHERE menu_id IN (1, 2,11,12,13, 3,14,15, 4,16,17, 5,18,19,20, 6,21,22);

-- 네트워크담당자 (role_id=7) : 대시보드, 장애관리, 서비스요청, 변경관리, 자산관리, 정기점검관리
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 7, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu
WHERE menu_id IN (1, 2,11,12,13, 3,14,15, 4,16,17, 5,18,19,20, 6,21,22);

-- 서버담당자 (role_id=8) : 대시보드, 장애관리, 서비스요청, 변경관리, 자산관리, 정기점검관리
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 8, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu
WHERE menu_id IN (1, 2,11,12,13, 3,14,15, 4,16,17, 5,18,19,20, 6,21,22);

-- 고객사 인원 (role_id=9) : 대시보드, 장애관리, 서비스요청, 보고관리
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 9, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu
WHERE menu_id IN (1, 2,11,12,13, 3,14,15, 7,23,24);

-- 외부사용자 (role_id=10) : 정기점검관리만
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 10, menu_id, 'Y', 'Y', NOW(), 1 FROM tb_menu
WHERE menu_id IN (6,21,22);

-- 감사자 (role_id=11) : 대시보드, 장애관리, 서비스요청, 변경관리, 자산관리, 정기점검관리, 보고관리 (읽기 전용)
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT 11, menu_id, 'Y', 'N', NOW(), 1 FROM tb_menu
WHERE menu_id IN (1, 2,11,12,13, 3,14,15, 4,16,17, 5,18,19,20, 6,21,22, 7,23,24);

-- ============================================================
-- 6. 공통코드 (기본)
-- ============================================================

-- 우선순위
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (1, '우선순위', 'Priority', 'PRIORITY', '장애/서비스요청/변경 우선순위', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(1, 'CRITICAL', '긴급',  'Critical', 1, 'Y', NOW(), 1),
(1, 'HIGH',     '높음',  'High',     2, 'Y', NOW(), 1),
(1, 'MEDIUM',   '보통',  'Medium',   3, 'Y', NOW(), 1),
(1, 'LOW',      '낮음',  'Low',      4, 'Y', NOW(), 1);

-- 장애유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (2, '장애유형', 'Incident Type', 'INCIDENT_TYPE', '장애 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(2, 'SYSTEM_DOWN',    '시스템 장애',    'System Down',              1, 'Y', NOW(), 1),
(2, 'NETWORK_ISSUE',  '네트워크 장애',  'Network Issue',            2, 'Y', NOW(), 1),
(2, 'SECURITY',       '보안 사고',      'Security Incident',        3, 'Y', NOW(), 1),
(2, 'PERFORMANCE',    '성능 저하',      'Performance Degradation',  4, 'Y', NOW(), 1),
(2, 'DATA_ISSUE',     '데이터 오류',    'Data Error',               5, 'Y', NOW(), 1),
(2, 'OTHER',          '기타',           'Other',                    6, 'Y', NOW(), 1);

-- 서비스요청 유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (3, '서비스요청 유형', 'Request Type', 'REQUEST_TYPE', '서비스 요청 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(3, 'ACCOUNT',         '계정 요청',      'Account Request',        1, 'Y', NOW(), 1),
(3, 'INSTALL',         '설치 요청',      'Installation Request',   2, 'Y', NOW(), 1),
(3, 'CONFIGURATION',   '설정 변경',      'Configuration Change',   3, 'Y', NOW(), 1),
(3, 'DATA_REQUEST',    '데이터 요청',    'Data Request',           4, 'Y', NOW(), 1),
(3, 'INQUIRY',         '문의',           'Inquiry',                5, 'Y', NOW(), 1),
(3, 'OTHER',           '기타',           'Other',                  6, 'Y', NOW(), 1);

-- 변경유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (4, '변경유형', 'Change Type', 'CHANGE_TYPE', '변경 요청 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(4, 'EMERGENCY', '긴급 변경', 'Emergency Change', 1, 'Y', NOW(), 1),
(4, 'NORMAL',    '일반 변경', 'Normal Change',    2, 'Y', NOW(), 1),
(4, 'STANDARD',  '정기 변경', 'Standard Change',  3, 'Y', NOW(), 1);

-- HW 자산유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (5, 'HW 자산유형', 'HW Asset Type', 'ASSET_HW_TYPE', 'HW 자산 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(5, 'SERVER',    '서버',         'Server',             1, 'Y', NOW(), 1),
(5, 'NETWORK',   '네트워크장비', 'Network Equipment',  2, 'Y', NOW(), 1),
(5, 'PC',        'PC/노트북',    'PC/Laptop',          3, 'Y', NOW(), 1),
(5, 'STORAGE',   '스토리지',     'Storage',            4, 'Y', NOW(), 1),
(5, 'SECURITY',  '보안장비',     'Security Equipment', 5, 'Y', NOW(), 1),
(5, 'OTHER',     '기타',         'Other',              6, 'Y', NOW(), 1);

-- SW 자산유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (6, 'SW 자산유형', 'SW Asset Type', 'ASSET_SW_TYPE', 'SW 자산 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(6, 'OS',        '운영체제',        'Operating System',   1, 'Y', NOW(), 1),
(6, 'WAS',       'WAS',             'WAS',                2, 'Y', NOW(), 1),
(6, 'DB',        'DBMS',            'DBMS',               3, 'Y', NOW(), 1),
(6, 'SECURITY',  '보안 소프트웨어', 'Security Software',  4, 'Y', NOW(), 1),
(6, 'OFFICE',    '오피스/업무용',   'Office/Business',    5, 'Y', NOW(), 1),
(6, 'OTHER',     '기타',            'Other',              6, 'Y', NOW(), 1);

-- OA 자산유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (12, 'OA 자산유형', 'OA Asset Type', 'ASSET_OA_TYPE', 'OA 자산 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(12, 'DESKTOP',   '데스크톱',       'Desktop',            1, 'Y', NOW(), 1),
(12, 'LAPTOP',    '노트북',         'Laptop',             2, 'Y', NOW(), 1),
(12, 'MONITOR',   '모니터',         'Monitor',            3, 'Y', NOW(), 1),
(12, 'PRINTER',   '프린터',         'Printer',            4, 'Y', NOW(), 1),
(12, 'MFP',       '복합기',         'MFP',                5, 'Y', NOW(), 1),
(12, 'PHONE',     '전화/VoIP',      'Phone/VoIP',         6, 'Y', NOW(), 1),
(12, 'TABLET',    '태블릿',         'Tablet',             7, 'Y', NOW(), 1),
(12, 'PROJECTOR', '프로젝터',       'Projector',          8, 'Y', NOW(), 1),
(12, 'PERIPHERAL','주변기기',       'Peripheral',         9, 'Y', NOW(), 1),
(12, 'OTHER',     '기타',           'Other',             10, 'Y', NOW(), 1);

-- 점검유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (7, '점검유형', 'Inspection Type', 'INSPECTION_TYPE', '정기점검 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(7, 'MONTHLY',   '월간 점검',  'Monthly Inspection',    1, 'Y', NOW(), 1),
(7, 'QUARTERLY', '분기 점검',  'Quarterly Inspection',  2, 'Y', NOW(), 1),
(7, 'ANNUAL',    '연간 점검',  'Annual Inspection',     3, 'Y', NOW(), 1),
(7, 'SPECIAL',   '특별 점검',  'Special Inspection',    4, 'Y', NOW(), 1);

-- 보고서 양식 유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (8, '보고서 양식유형', 'Report Form Type', 'REPORT_FORM_TYPE', '보고서 양식 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(8, 'INCIDENT',         '장애 보고서',         'Incident Report',         1, 'Y', NOW(), 1),
(8, 'SERVICE_REQUEST',  '서비스요청 보고서',   'Service Request Report',  2, 'Y', NOW(), 1),
(8, 'CHANGE',           '변경 보고서',         'Change Report',           3, 'Y', NOW(), 1),
(8, 'INSPECTION',       '점검 보고서',         'Inspection Report',       4, 'Y', NOW(), 1);

-- 게시판 유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (9, '게시판유형', 'Board Type', 'BOARD_TYPE', '게시판 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(9, 'NOTICE',   '공지형',  'Notice',      1, 'Y', NOW(), 1),
(9, 'FREE',     '자유형',  'Free Board',  2, 'Y', NOW(), 1),
(9, 'ARCHIVE',  '자료형',  'Archive',     3, 'Y', NOW(), 1);

-- 알림유형
INSERT INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (10, '알림유형', 'Notification Type', 'NOTIFICATION_TYPE', '알림 유형 분류', 'Y', NOW(), 1);

INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(10, 'INCIDENT',         '장애 알림',         'Incident Notification',         1, 'Y', NOW(), 1),
(10, 'SERVICE_REQUEST',  '서비스요청 알림',   'Service Request Notification',  2, 'Y', NOW(), 1),
(10, 'CHANGE',           '변경 알림',         'Change Notification',           3, 'Y', NOW(), 1),
(10, 'INSPECTION',       '점검 알림',         'Inspection Notification',       4, 'Y', NOW(), 1),
(10, 'SLA_WARNING',      'SLA 경고 알림',     'SLA Warning',                   5, 'Y', NOW(), 1),
(10, 'SYSTEM',           '시스템 알림',       'System Notification',           6, 'Y', NOW(), 1);

-- ============================================================
-- 7. SLA 기본 정책 (전체 기본값, company_id = NULL)
-- ============================================================

INSERT INTO tb_sla_policy (company_id, priority_cd, deadline_hours, warning_pct, is_active, created_at, created_by) VALUES
(NULL, 'CRITICAL', 4,   80, 'Y', NOW(), 1),
(NULL, 'HIGH',     8,   80, 'Y', NOW(), 1),
(NULL, 'MEDIUM',   24,  80, 'Y', NOW(), 1),
(NULL, 'LOW',      72,  80, 'Y', NOW(), 1);

-- ============================================================
-- 8. 시스템 기본 설정
-- ============================================================

INSERT INTO tb_system_config (config_key, config_val, description, updated_at, updated_by) VALUES
('system.name',              'ITSM',                  '시스템명',                    NOW(), 1),
('system.maintenance.yn',    'N',                     '시스템 점검 모드 여부',        NOW(), 1),
('system.maintenance.msg',   '',                      '시스템 점검 안내 메시지',      NOW(), 1),
('password.min.length',      '8',                     '비밀번호 최소 길이',           NOW(), 1),
('password.expire.days',     '90',                    '비밀번호 만료 기간 (일)',      NOW(), 1),
('login.fail.lock.count',    '5',                     '로그인 실패 잠금 횟수',        NOW(), 1);

-- ============================================================
-- 9. 보고서 양식 (동적 폼) — 2026-09-16 전수조사 P3
--    장애보고서(IncidentDetailView)가 report_form_id=1 을 참조하는데 시드가 없어 FK 위반으로 저장이 불가능했다.
--    form_schema 는 프론트 DynamicForm.vue 가 읽는 필드 배열: key/label/type(text|textarea|number|date|select|checkbox)/required/placeholder/options
-- ============================================================

INSERT INTO tb_report_form (form_id, form_nm, form_type_cd, form_schema, is_active, created_at, created_by) VALUES
(1, '장애보고서 기본 양식', 'INCIDENT',
 '[{"key":"summary","label":"장애 요약","type":"text","required":true,"placeholder":"현상을 한 줄로"},{"key":"impact","label":"영향 범위","type":"textarea","required":true},{"key":"cause","label":"원인","type":"textarea","required":true},{"key":"solution","label":"조치 내용","type":"textarea","required":true},{"key":"prevention","label":"재발 방지 대책","type":"textarea"},{"key":"resolvedAt","label":"복구 완료 일시","type":"date"}]',
 'Y', NOW(), 1);

-- ============================================================
-- 10. 배치 작업 시드 (2026-09-16 전수조사 P4) — job_name 은 잡 클래스명과 1:1 (BatchJobSeedTest 가 검증)
--     시뮬레이션 잡 6종은 운영 데이터에 가짜 행을 섞으므로 기본 비활성. 관리자 화면에서 켠다.
-- ============================================================

INSERT INTO tb_batch_job (job_name, job_name_en, job_description, cron_expression, is_active, trigger_now, created_at, created_by) VALUES
('SlaWarningJob', 'SLA Warning', 'SLA 경과율이 정책 warning_pct 이상인 장애의 주담당자에게 경고 알림', '0 0 * * * *', 'Y', 'N', NOW(), 1),
('SlaOverdueJob', 'SLA Overdue', 'SLA 기한을 넘긴 장애의 주담당자에게 초과 알림', '0 30 * * * *', 'Y', 'N', NOW(), 1),
('UnassignedIncidentJob', 'Unassigned Incidents', '주담당자가 없는 접수/처리중 장애를 시스템 사용자에게 알림', '0 0 9 * * *', 'Y', 'N', NOW(), 1),
('RepeatIncidentJob', 'Repeat Incidents', '최근 30일 같은 자산에서 반복 발생한 장애 알림', '0 0 8 * * MON', 'Y', 'N', NOW(), 1),
('LongPendingSrJob', 'Long-pending Service Requests', '완료대기 상태로 2일 이상 머문 서비스요청 알림', '0 0 9 * * *', 'Y', 'N', NOW(), 1),
('InspectionAlertJob', 'Inspection Reminder', '7일 내 예정된 정기점검을 담당자에게 알림', '0 0 8 * * *', 'Y', 'N', NOW(), 1),
('MissedInspectionJob', 'Missed Inspections', '예정일이 지났는데 미실시된 정기점검 알림', '0 5 8 * * *', 'Y', 'N', NOW(), 1),
('AssetExpiryJob', 'Asset Warranty Expiry', '30일 내 보증 만료 HW 자산 알림', '0 10 8 * * *', 'Y', 'N', NOW(), 1),
('StatisticsAggregationJob', 'Daily Statistics', '전일 장애/SR/변경/자산/접속 통계를 tb_daily_statistics 에 집계', '0 10 0 * * *', 'Y', 'N', NOW(), 1),
('IncidentSimulationJob', '[Demo] Incident Simulation', '데모용 장애 자동 생성/진행 (운영 데이터에 가짜 장애가 섞이므로 기본 비활성)', '0 0 9-18/3 * * MON-FRI', 'N', 'N', NOW(), 1),
('ServiceRequestSimulationJob', '[Demo] SR Simulation', '데모용 서비스요청 자동 생성/배정 (기본 비활성)', '0 15 9-18/3 * * MON-FRI', 'N', 'N', NOW(), 1),
('ChangeSimulationJob', '[Demo] Change Simulation', '데모용 변경요청 자동 생성/승인 (기본 비활성)', '0 30 10 * * MON-FRI', 'N', 'N', NOW(), 1),
('InspectionSimulationJob', '[Demo] Inspection Simulation', '데모용 정기점검 자동 생성/결과 입력 (기본 비활성)', '0 45 9 * * MON-FRI', 'N', 'N', NOW(), 1),
('TrafficSimulationJob', '[Demo] Traffic Simulation', '데모용 로그인/메뉴 접근 이력 생성 (기본 비활성)', '0 */10 8-20 * * *', 'N', 'N', NOW(), 1),
('AssetAutoRegisterJob', '[Demo] Asset Auto-register', '데모용 자산 자동 등록 (기본 비활성)', '0 0 7 * * MON', 'N', 'N', NOW(), 1);

-- ============================================================
-- 11. 시스템 설정 메뉴 (2026-09-16 전수조사 P2) — ITSM.md 메뉴 트리에 있으나 시드·화면이 없던 항목
--     프론트 라우트 /admin/system-configs, 관리자(SUPER_ADMIN·ITSM_ADMIN) 전용. 멱등: 같은 URL 이 있으면 건너뜀.
-- ============================================================

INSERT INTO tb_menu (parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by)
SELECT 9, '시스템 설정', 'System Settings', '/admin/system-configs', 'mdi-cog', 6, 'Y', 'ACTIVE', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM tb_menu WHERE menu_url = '/admin/system-configs');

INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
SELECT r.role_id, m.menu_id, 'Y', 'Y', NOW(), 1
FROM tb_menu m JOIN tb_role r ON r.role_id IN (1, 2)
WHERE m.menu_url = '/admin/system-configs'
  AND NOT EXISTS (SELECT 1 FROM tb_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);
