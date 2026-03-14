-- Add batch job management menu under Settings (menu_id=9)
INSERT INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_url, icon, sort_order, is_visible, status, created_at, created_by)
VALUES (41, 9, '배치 관리', '/admin/batch-jobs', NULL, 6, 'Y', 'ACTIVE', NOW(), 1);

-- Grant access to SUPER_ADMIN (role_id=1) and ITSM_ADMIN (role_id=2)
INSERT INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by)
VALUES (1, 41, 'Y', 'Y', NOW(), 1),
       (2, 41, 'Y', 'Y', NOW(), 1);
