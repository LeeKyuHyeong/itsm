-- Phase 12: Asset Category Restructuring

-- Add category columns to tb_asset_hw
ALTER TABLE tb_asset_hw ADD COLUMN asset_category VARCHAR(20) DEFAULT 'INFRA_HW';
ALTER TABLE tb_asset_hw ADD COLUMN asset_sub_category VARCHAR(30);

-- Add category columns to tb_asset_sw
ALTER TABLE tb_asset_sw ADD COLUMN asset_category VARCHAR(20) DEFAULT 'INFRA_SW';
ALTER TABLE tb_asset_sw ADD COLUMN asset_sub_category VARCHAR(30);

-- Common code group: ASSET_CATEGORY
INSERT INTO tb_common_code (group_cd, group_nm, description, is_active, created_at, created_by) VALUES ('ASSET_CATEGORY', '자산 대분류', '자산관리 대분류 코드', 'Y', NOW(), 1);
SET @cat_id = LAST_INSERT_ID();
INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, sort_order, is_active, created_at, created_by) VALUES
(@cat_id, 'INFRA_HW', '운영장비', 1, 'Y', NOW(), 1),
(@cat_id, 'INFRA_SW', '운영SW', 2, 'Y', NOW(), 1),
(@cat_id, 'OA', 'OA자산', 3, 'Y', NOW(), 1);

-- Common code group: ASSET_SUB_INFRA_HW (15 items)
INSERT INTO tb_common_code (group_cd, group_nm, description, is_active, created_at, created_by) VALUES ('ASSET_SUB_INFRA_HW', '운영장비 중분류', '운영장비 자산 중분류 코드', 'Y', NOW(), 1);
SET @hw_id = LAST_INSERT_ID();
INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, sort_order, is_active, created_at, created_by) VALUES
(@hw_id, 'SERVER_RACK', '랙마운트 서버', 1, 'Y', NOW(), 1),
(@hw_id, 'SERVER_BLADE', '블레이드 서버', 2, 'Y', NOW(), 1),
(@hw_id, 'SERVER_TOWER', '타워 서버', 3, 'Y', NOW(), 1),
(@hw_id, 'STORAGE_SAN', 'SAN 스토리지', 4, 'Y', NOW(), 1),
(@hw_id, 'STORAGE_NAS', 'NAS', 5, 'Y', NOW(), 1),
(@hw_id, 'NETWORK_SWITCH', '네트워크 스위치', 6, 'Y', NOW(), 1),
(@hw_id, 'NETWORK_ROUTER', '라우터', 7, 'Y', NOW(), 1),
(@hw_id, 'NETWORK_FW', '방화벽', 8, 'Y', NOW(), 1),
(@hw_id, 'NETWORK_LB', '로드밸런서', 9, 'Y', NOW(), 1),
(@hw_id, 'NETWORK_AP', '무선AP', 10, 'Y', NOW(), 1),
(@hw_id, 'SECURITY_IDS', 'IDS/IPS', 11, 'Y', NOW(), 1),
(@hw_id, 'SECURITY_WAF', 'WAF', 12, 'Y', NOW(), 1),
(@hw_id, 'POWER_UPS', 'UPS', 13, 'Y', NOW(), 1),
(@hw_id, 'POWER_PDU', 'PDU', 14, 'Y', NOW(), 1),
(@hw_id, 'INFRA_KVM', 'KVM/콘솔', 15, 'Y', NOW(), 1);

-- Common code group: ASSET_SUB_INFRA_SW (11 items)
INSERT INTO tb_common_code (group_cd, group_nm, description, is_active, created_at, created_by) VALUES ('ASSET_SUB_INFRA_SW', '운영SW 중분류', '운영SW 자산 중분류 코드', 'Y', NOW(), 1);
SET @sw_id = LAST_INSERT_ID();
INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, sort_order, is_active, created_at, created_by) VALUES
(@sw_id, 'SW_OS', '운영체제', 1, 'Y', NOW(), 1),
(@sw_id, 'SW_DB', '데이터베이스', 2, 'Y', NOW(), 1),
(@sw_id, 'SW_WAS', 'WAS/웹서버', 3, 'Y', NOW(), 1),
(@sw_id, 'SW_MIDDLEWARE', '미들웨어', 4, 'Y', NOW(), 1),
(@sw_id, 'SW_MONITORING', '모니터링', 5, 'Y', NOW(), 1),
(@sw_id, 'SW_BACKUP', '백업솔루션', 6, 'Y', NOW(), 1),
(@sw_id, 'SW_SECURITY', '보안솔루션', 7, 'Y', NOW(), 1),
(@sw_id, 'SW_VIRTUALIZATION', '가상화', 8, 'Y', NOW(), 1),
(@sw_id, 'SW_CONTAINER', '컨테이너/오케', 9, 'Y', NOW(), 1),
(@sw_id, 'SW_CICD', 'CI/CD', 10, 'Y', NOW(), 1),
(@sw_id, 'SW_LICENSE', '상용라이선스', 11, 'Y', NOW(), 1);

-- Common code group: ASSET_SUB_OA (8 items)
INSERT INTO tb_common_code (group_cd, group_nm, description, is_active, created_at, created_by) VALUES ('ASSET_SUB_OA', 'OA자산 중분류', 'OA자산 중분류 코드', 'Y', NOW(), 1);
SET @oa_id = LAST_INSERT_ID();
INSERT INTO tb_common_code_detail (group_id, code_val, code_nm, sort_order, is_active, created_at, created_by) VALUES
(@oa_id, 'OA_DESKTOP', '데스크톱', 1, 'Y', NOW(), 1),
(@oa_id, 'OA_LAPTOP', '노트북', 2, 'Y', NOW(), 1),
(@oa_id, 'OA_MONITOR', '모니터', 3, 'Y', NOW(), 1),
(@oa_id, 'OA_PRINTER', '프린터/복합기', 4, 'Y', NOW(), 1),
(@oa_id, 'OA_PHONE', '전화/VoIP', 5, 'Y', NOW(), 1),
(@oa_id, 'OA_TABLET', '태블릿', 6, 'Y', NOW(), 1),
(@oa_id, 'OA_PERIPHERAL', '주변기기', 7, 'Y', NOW(), 1),
(@oa_id, 'OA_PROJECTOR', '프로젝터', 8, 'Y', NOW(), 1);

-- Migrate existing data
UPDATE tb_asset_hw SET asset_category = 'INFRA_HW' WHERE asset_category IS NULL;
UPDATE tb_asset_sw SET asset_category = 'INFRA_SW' WHERE asset_category IS NULL;
