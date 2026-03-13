-- Step 1: role_menu 매핑 먼저 제거
DELETE FROM tb_role_menu WHERE menu_id IN (13, 20, 24, 26, 34, 35, 36, 37, 38);

-- Step 2: 자식 메뉴 먼저 삭제 (3레벨)
DELETE FROM tb_menu WHERE menu_id IN (34, 35, 36, 37, 38);

-- Step 3: 부모 메뉴 role_menu 제거 후 삭제 (2레벨 중간 그룹)
DELETE FROM tb_role_menu WHERE menu_id IN (28, 29, 31);
DELETE FROM tb_menu WHERE menu_id IN (28, 29, 31);

-- Step 4: 기타 미사용 메뉴 삭제
DELETE FROM tb_role_menu WHERE menu_id IN (13, 20, 24, 26);
DELETE FROM tb_menu WHERE menu_id IN (13, 20, 24, 26);

-- Step 5: 메뉴 URL을 프론트엔드 라우터 경로에 맞게 수정
UPDATE tb_menu SET menu_url = '/incidents'         WHERE menu_id = 11;
UPDATE tb_menu SET menu_url = '/incidents/new'     WHERE menu_id = 12;
UPDATE tb_menu SET menu_url = '/service-requests'     WHERE menu_id = 14;
UPDATE tb_menu SET menu_url = '/service-requests/new' WHERE menu_id = 15;
UPDATE tb_menu SET menu_url = '/changes'       WHERE menu_id = 16;
UPDATE tb_menu SET menu_url = '/changes/new'   WHERE menu_id = 17;
UPDATE tb_menu SET menu_url = '/assets/hw'     WHERE menu_id = 18;
UPDATE tb_menu SET menu_url = '/assets/sw'     WHERE menu_id = 19;
UPDATE tb_menu SET menu_url = '/inspections'       WHERE menu_id = 21;
UPDATE tb_menu SET menu_url = '/inspections/new'   WHERE menu_id = 22;
UPDATE tb_menu SET menu_url = '/reports'           WHERE menu_id = 23;

-- Step 6: 설정관리 하위 메뉴 URL 수정
UPDATE tb_menu SET menu_url = '/admin/menus',       menu_nm = '메뉴 관리',      sort_order = 1 WHERE menu_id = 25;
UPDATE tb_menu SET menu_url = '/admin/common-codes', sort_order = 2 WHERE menu_id = 27;
UPDATE tb_menu SET menu_url = '/admin/boards',      menu_nm = '게시판 관리',    sort_order = 3 WHERE menu_id = 30;
UPDATE tb_menu SET menu_url = '/admin/sla',         menu_nm = 'SLA 관리',       parent_menu_id = 9, sort_order = 4 WHERE menu_id = 32;
UPDATE tb_menu SET menu_url = '/admin/notification-policy', menu_nm = '알림 정책 관리', parent_menu_id = 9, sort_order = 5 WHERE menu_id = 33;

-- Step 7: 계정관리 하위 메뉴
UPDATE tb_menu SET menu_url = '/admin/accounts',       menu_nm = '사용자 관리' WHERE menu_id = 39;
UPDATE tb_menu SET menu_url = '/admin/organizations',  menu_nm = '조직 관리'   WHERE menu_id = 40;
