-- ============================================================
-- Phase 30 (2026-09-16 전수조사 P6) — 운영 DB 반영용 (멱등)
-- 리포 01_ddl.sql 에 없던 것을 보강. 운영은 ddl-auto=validate 로 이미 기동 중이므로 대부분 "이미 있음" 으로 끝나야 정상.
-- 결과가 실제로 무언가를 만들었다면 운영 스키마와 코드가 어긋나 있었다는 뜻이니 기록해 둘 것.
-- ============================================================

ALTER TABLE tb_service_request
    ADD COLUMN IF NOT EXISTS received_at            DATETIME      NULL COMMENT '접수일',
    ADD COLUMN IF NOT EXISTS scheduled_at           DATETIME      NULL COMMENT '처리예정일 (담당자 배정 시 설정)',
    ADD COLUMN IF NOT EXISTS revised_scheduled_at   DATETIME      NULL COMMENT '처리변경예정일',
    ADD COLUMN IF NOT EXISTS schedule_change_reason VARCHAR(500)  NULL COMMENT '처리예정일 변경 사유',
    ADD COLUMN IF NOT EXISTS processed_at           DATETIME      NULL COMMENT '처리일 (PENDING_COMPLETE 전환 시 자동)';

-- ============================================================
-- 배치 시뮬레이션·통계 테이블 (2026-09-16 전수조사 P6 — 엔티티는 있었으나 DDL 이 없어 validate 기동이 불가능했다)
-- tb_login_history / tb_sim_menu_access_log : TrafficSimulationJob 이 쓰고 StatisticsAggregationJob 이 읽는 데모용 접속 이력
-- tb_daily_statistics : StatisticsAggregationJob 의 일별 집계 (2026-09-16 현재 읽는 화면은 없음)
-- ============================================================
CREATE TABLE IF NOT EXISTS tb_login_history (
    login_history_id BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '로그인이력ID',
    user_id          BIGINT          NOT NULL                 COMMENT '사용자ID',
    login_at         DATETIME        NOT NULL                 COMMENT '로그인 일시',
    logout_at        DATETIME        NULL                     COMMENT '로그아웃 일시',
    ip_address       VARCHAR(50)     NULL                     COMMENT '접속 IP',
    user_agent       VARCHAR(300)    NULL                     COMMENT 'User-Agent',
    created_at       DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by       BIGINT          NULL                     COMMENT '등록자ID',
    updated_at       DATETIME        NULL                     COMMENT '수정일시',
    updated_by       BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (login_history_id),
    INDEX idx_login_history_user_id (user_id),
    INDEX idx_login_history_login_at (login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='로그인 이력 (시뮬레이션)';

CREATE TABLE IF NOT EXISTS tb_sim_menu_access_log (
    access_log_id    BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '접근로그ID',
    user_id          BIGINT          NOT NULL                 COMMENT '사용자ID',
    menu_path        VARCHAR(200)    NOT NULL                 COMMENT '메뉴 경로',
    menu_nm          VARCHAR(100)    NULL                     COMMENT '메뉴명',
    accessed_at      DATETIME        NOT NULL                 COMMENT '접근 일시',
    created_at       DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by       BIGINT          NULL                     COMMENT '등록자ID',
    updated_at       DATETIME        NULL                     COMMENT '수정일시',
    updated_by       BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (access_log_id),
    INDEX idx_sim_menu_access_log_user_id (user_id),
    INDEX idx_sim_menu_access_log_accessed_at (accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴 접근 로그 (시뮬레이션)';

CREATE TABLE IF NOT EXISTS tb_daily_statistics (
    stat_id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '통계ID',
    stat_date        DATE            NOT NULL                 COMMENT '통계 일자',
    stat_type        VARCHAR(50)     NOT NULL                 COMMENT '통계 유형 (INCIDENT/SERVICE_REQUEST/CHANGE/ASSET/USER)',
    stat_key         VARCHAR(100)    NOT NULL                 COMMENT '통계 항목 키',
    stat_value       DECIMAL(15,2)   NOT NULL                 COMMENT '값',
    stat_detail      TEXT            NULL                     COMMENT '상세 (JSON 등)',
    created_at       DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by       BIGINT          NULL                     COMMENT '등록자ID',
    updated_at       DATETIME        NULL                     COMMENT '수정일시',
    updated_by       BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (stat_id),
    UNIQUE KEY uk_daily_statistics (stat_date, stat_type, stat_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일별 통계 집계';

-- 검증
SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('tb_login_history','tb_sim_menu_access_log','tb_daily_statistics');
-- 기대: 3행. 시뮬레이션 잡이 켜져 있었다면 TABLE_ROWS 가 크다.
