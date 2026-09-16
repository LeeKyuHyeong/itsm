-- ============================================================
-- Phase 29 (2026-09-16 전수조사 P2) — 운영 DB 반영용
-- 시스템 설정 메뉴(/admin/system-configs) + 관리자 역할 매핑. 이미 있으면 아무것도 바꾸지 않는다.
-- 메뉴 캐시(Caffeine, 5분) 때문에 반영 직후에는 사이드바에 바로 안 보일 수 있다: 관리자 메뉴 화면에서 아무 메뉴나 1건 저장(캐시 evict)하거나 5분 기다린다.
-- ============================================================

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

-- 검증
SELECT m.menu_id, m.menu_nm, m.menu_url, GROUP_CONCAT(rm.role_id) AS roles
FROM tb_menu m LEFT JOIN tb_role_menu rm ON rm.menu_id = m.menu_id
WHERE m.menu_url = '/admin/system-configs' GROUP BY m.menu_id;
-- 기대: 1행, roles = 1,2
