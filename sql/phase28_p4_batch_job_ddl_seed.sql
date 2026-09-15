-- ============================================================
-- Phase 28 (2026-09-16 전수조사 P4) — 운영 DB 반영용
-- tb_batch_job DDL/시드가 리포 sql/ 에 없던 것을 보강. 운영에 이미 테이블·행이 있으면 아무것도 바꾸지 않는다
-- (CREATE IF NOT EXISTS + INSERT IGNORE, UNIQUE job_name). 기존 행의 cron/활성 여부는 그대로 둔다.
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_batch_job (
    batch_job_id        BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '배치작업ID',
    job_name            VARCHAR(100)    NOT NULL                 COMMENT '잡 클래스명 (스케줄러 빈 조회 키)',
    job_name_en         VARCHAR(100)    NULL                     COMMENT '영문 표시명',
    job_description     VARCHAR(300)    NULL                     COMMENT '설명',
    cron_expression     VARCHAR(50)     NOT NULL                 COMMENT 'Spring CRON (초 분 시 일 월 요일)',
    is_active           CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '활성 여부',
    last_executed_at    DATETIME        NULL                     COMMENT '마지막 실행 일시',
    last_result         VARCHAR(20)     NULL                     COMMENT '마지막 결과 (SUCCESS/FAILURE)',
    last_result_message TEXT            NULL                     COMMENT '마지막 결과 메시지',
    trigger_now         CHAR(1)         NOT NULL DEFAULT 'N'     COMMENT '수동 실행 요청 플래그 (5초 폴링)',
    created_at          DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by          BIGINT          NULL                     COMMENT '등록자ID',
    updated_at          DATETIME        NULL                     COMMENT '수정일시',
    updated_by          BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (batch_job_id),
    UNIQUE KEY uk_batch_job_name (job_name),
    INDEX idx_batch_job_trigger_now (trigger_now)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='배치 작업 정의';

INSERT IGNORE INTO tb_batch_job (job_name, job_name_en, job_description, cron_expression, is_active, trigger_now, created_at, created_by) VALUES
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

-- 검증
SELECT job_name, cron_expression, is_active, last_executed_at, last_result FROM tb_batch_job ORDER BY job_name;
-- 기대: 15행. job_name 이 잡 클래스명과 다르면 스케줄러가 등록하지 못한다 (batch 로그 "배치 등록 실패").

-- (선택) 제거된 Spring Batch 스타터가 만들어 둔 메타 테이블 정리 — 앱은 이제 쓰지 않는다. 있을 때만.
-- SHOW TABLES LIKE 'BATCH_%';
-- DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT, BATCH_JOB_EXECUTION_CONTEXT, BATCH_STEP_EXECUTION,
--   BATCH_JOB_EXECUTION_PARAMS, BATCH_JOB_EXECUTION, BATCH_JOB_INSTANCE,
--   BATCH_STEP_EXECUTION_SEQ, BATCH_JOB_EXECUTION_SEQ, BATCH_JOB_SEQ;
