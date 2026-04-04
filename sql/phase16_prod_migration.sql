-- ============================================================
-- Phase 16 운영 DB 마이그레이션 스크립트
-- 실행 대상: 운영 MariaDB
-- 실행 전 반드시 DB 백업 수행할 것!
-- ============================================================

-- ============================================================
-- 1. OA 자산 테이블 생성
--    (ddl-auto: update로 자동생성될 수 있으나, 인덱스/FK 정확성을 위해 수동 실행 권장)
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_asset_oa (
    asset_oa_id     BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'OA자산ID',
    asset_nm        VARCHAR(100)    NOT NULL                 COMMENT '자산명',
    asset_type_cd   VARCHAR(50)     NOT NULL                 COMMENT 'OA유형코드 (공통코드 - 프린터/복합기/데스크톱 등)',
    manufacturer    VARCHAR(100)    NULL                     COMMENT '제조사',
    model_nm        VARCHAR(100)    NULL                     COMMENT '모델명',
    serial_no       VARCHAR(100)    NULL                     COMMENT '시리얼번호',
    ip_address      VARCHAR(50)     NULL                     COMMENT 'IP주소',
    mac_address     VARCHAR(50)     NULL                     COMMENT 'MAC주소',
    location        VARCHAR(200)    NULL                     COMMENT '설치위치',
    introduced_at   DATE            NULL                     COMMENT '도입일',
    warranty_end_at DATE            NULL                     COMMENT '유지보수 만료일',
    company_id      BIGINT          NOT NULL                 COMMENT '소속 고객사ID',
    manager_id      BIGINT          NULL                     COMMENT '담당자ID',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE/DISPOSED',
    description     TEXT            NULL                     COMMENT '비고',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자',
    PRIMARY KEY (asset_oa_id),
    UNIQUE KEY uk_asset_oa_serial_no (serial_no),
    INDEX idx_asset_oa_company_id (company_id),
    INDEX idx_asset_oa_status (status),
    INDEX idx_asset_oa_asset_type_cd (asset_type_cd),
    CONSTRAINT fk_asset_oa_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id),
    CONSTRAINT fk_asset_oa_manager FOREIGN KEY (manager_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_asset_oa_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_asset_oa_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OA 자산';

CREATE TABLE IF NOT EXISTS tb_asset_oa_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    asset_oa_id     BIGINT          NOT NULL                 COMMENT 'OA자산ID',
    changed_field   VARCHAR(100)    NOT NULL                 COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    batch_job_id    VARCHAR(50)     NULL                     COMMENT '일괄변경 시 배치작업ID',
    created_at      DATETIME        NOT NULL                 COMMENT '이력 생성일시',
    created_by      BIGINT          NULL                     COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_asset_oa_history_oa_id (asset_oa_id),
    CONSTRAINT fk_asset_oa_history_oa FOREIGN KEY (asset_oa_id) REFERENCES tb_asset_oa (asset_oa_id),
    CONSTRAINT fk_asset_oa_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OA 자산 변경 이력';

-- ============================================================
-- 2. OA 메뉴 추가
--    주의: menu_id 20이 이미 존재하면 SKIP됨
-- ============================================================

INSERT IGNORE INTO tb_menu (menu_id, parent_menu_id, menu_nm, menu_nm_en, menu_url, icon, sort_order, is_visible, status, created_at, created_by)
VALUES (20, 5, 'OA 자산 목록', 'OA Asset List', '/assets/oa', NULL, 3, 'Y', 'ACTIVE', NOW(), 1);

-- ============================================================
-- 3. OA 메뉴 역할 매핑 (RBAC)
--    menu_id=20에 대한 권한 부여
-- ============================================================

-- 슈퍼관리자, ITSM관리자: 읽기+쓰기
INSERT IGNORE INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by) VALUES
(1,  20, 'Y', 'Y', NOW(), 1),
(2,  20, 'Y', 'Y', NOW(), 1);

-- PM, 개발자, 보안담당자, DB담당자, 네트워크담당자, 서버담당자: 읽기+쓰기
INSERT IGNORE INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by) VALUES
(3,  20, 'Y', 'Y', NOW(), 1),
(4,  20, 'Y', 'Y', NOW(), 1),
(5,  20, 'Y', 'Y', NOW(), 1),
(6,  20, 'Y', 'Y', NOW(), 1),
(7,  20, 'Y', 'Y', NOW(), 1),
(8,  20, 'Y', 'Y', NOW(), 1);

-- 감사자: 읽기 전용
INSERT IGNORE INTO tb_role_menu (role_id, menu_id, can_read, can_write, created_at, created_by) VALUES
(11, 20, 'Y', 'N', NOW(), 1);

-- ============================================================
-- 4. OA 자산유형 공통코드 추가
--    주의: group_id 12가 이미 존재하면 SKIP됨
-- ============================================================

INSERT IGNORE INTO tb_common_code (group_id, group_nm, group_nm_en, group_cd, description, is_active, created_at, created_by)
VALUES (12, 'OA 자산유형', 'OA Asset Type', 'ASSET_OA_TYPE', 'OA 자산 유형 분류', 'Y', NOW(), 1);

INSERT IGNORE INTO tb_common_code_detail (group_id, code_val, code_nm, code_nm_en, sort_order, is_active, created_at, created_by) VALUES
(12, 'DESKTOP',    '데스크톱',       'Desktop',    1,  'Y', NOW(), 1),
(12, 'LAPTOP',     '노트북',         'Laptop',     2,  'Y', NOW(), 1),
(12, 'MONITOR',    '모니터',         'Monitor',    3,  'Y', NOW(), 1),
(12, 'PRINTER',    '프린터',         'Printer',    4,  'Y', NOW(), 1),
(12, 'MFP',        '복합기',         'MFP',        5,  'Y', NOW(), 1),
(12, 'PHONE',      '전화/VoIP',      'Phone/VoIP', 6,  'Y', NOW(), 1),
(12, 'TABLET',     '태블릿',         'Tablet',     7,  'Y', NOW(), 1),
(12, 'PROJECTOR',  '프로젝터',       'Projector',  8,  'Y', NOW(), 1),
(12, 'PERIPHERAL', '주변기기',       'Peripheral', 9,  'Y', NOW(), 1),
(12, 'OTHER',      '기타',           'Other',      10, 'Y', NOW(), 1);

-- ============================================================
-- 5. 검증 쿼리 (실행 후 확인용)
-- ============================================================

-- 테이블 생성 확인
SELECT 'tb_asset_oa' AS table_name, COUNT(*) AS row_count FROM tb_asset_oa
UNION ALL
SELECT 'tb_asset_oa_history', COUNT(*) FROM tb_asset_oa_history;

-- OA 메뉴 확인
SELECT menu_id, menu_nm, menu_nm_en, menu_url, is_visible, status
FROM tb_menu WHERE menu_id = 20;

-- OA 메뉴 역할 매핑 확인
SELECT r.role_nm, rm.can_read, rm.can_write
FROM tb_role_menu rm
JOIN tb_role r ON r.role_id = rm.role_id
WHERE rm.menu_id = 20
ORDER BY rm.role_id;

-- OA 공통코드 확인
SELECT cc.group_cd, ccd.code_val, ccd.code_nm, ccd.code_nm_en, ccd.sort_order
FROM tb_common_code cc
JOIN tb_common_code_detail ccd ON cc.group_id = ccd.group_id
WHERE cc.group_cd = 'ASSET_OA_TYPE'
ORDER BY ccd.sort_order;
