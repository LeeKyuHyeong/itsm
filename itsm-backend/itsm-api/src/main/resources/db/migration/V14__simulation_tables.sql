-- ============================================
-- V14: Phase 13 시뮬레이션 배치용 테이블
-- ============================================

-- 1. 로그인 이력 테이블
CREATE TABLE tb_login_history (
    login_history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_at DATETIME NOT NULL,
    logout_at DATETIME,
    ip_address VARCHAR(50),
    user_agent VARCHAR(300),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    INDEX idx_login_history_user (user_id),
    INDEX idx_login_history_login_at (login_at)
);

-- 2. 시뮬레이션 메뉴 접근 로그 테이블
CREATE TABLE tb_sim_menu_access_log (
    access_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    menu_path VARCHAR(200) NOT NULL,
    menu_nm VARCHAR(100),
    accessed_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    INDEX idx_sim_menu_access_user (user_id),
    INDEX idx_sim_menu_access_at (accessed_at)
);

-- 3. 일별 통계 테이블
CREATE TABLE tb_daily_statistics (
    stat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL,
    stat_type VARCHAR(50) NOT NULL,
    stat_key VARCHAR(100) NOT NULL,
    stat_value DECIMAL(15,2) NOT NULL DEFAULT 0,
    stat_detail TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    UNIQUE INDEX idx_daily_stat_unique (stat_date, stat_type, stat_key),
    INDEX idx_daily_stat_date (stat_date),
    INDEX idx_daily_stat_type (stat_type)
);

-- 4. 시뮬레이션 배치 Job 등록
INSERT INTO tb_batch_job (job_name, job_description, cron_expression, is_active, created_at, created_by, updated_at, updated_by) VALUES
('AssetAutoRegisterJob', '자산 자동 등록 배치 (초기 세팅 + 월 1~2건 추가)', '0 0 1 1 * *', 'Y', NOW(), 1, NOW(), 1),
('IncidentSimulationJob', '장애 시뮬레이션 배치 (일 2~5건 자동 생성/처리)', '0 */30 * * * *', 'Y', NOW(), 1, NOW(), 1),
('ServiceRequestSimulationJob', '서비스요청 시뮬레이션 배치 (일 3~8건 자동 생성/처리)', '0 0 * * * *', 'Y', NOW(), 1, NOW(), 1),
('ChangeSimulationJob', '변경관리 시뮬레이션 배치 (주 2~4건 자동 생성/승인)', '0 0 2 * * *', 'Y', NOW(), 1, NOW(), 1),
('InspectionSimulationJob', '정기점검 시뮬레이션 배치 (점검 자동 수행)', '0 0 3 * * *', 'Y', NOW(), 1, NOW(), 1),
('TrafficSimulationJob', '트래픽/모니터링 시뮬레이션 배치 (로그인/메뉴접근 로그)', '0 */15 * * * *', 'Y', NOW(), 1, NOW(), 1),
('StatisticsAggregationJob', '통계 집계 배치 (대시보드용 일별 통계)', '0 0 2 * * *', 'Y', NOW(), 1, NOW(), 1);
