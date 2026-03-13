CREATE TABLE tb_batch_job (
    batch_job_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL UNIQUE COMMENT '배치 작업명 (빈 이름과 동일)',
    job_description VARCHAR(300) COMMENT '배치 작업 설명',
    cron_expression VARCHAR(50) NOT NULL COMMENT 'CRON 표현식',
    is_active CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_executed_at DATETIME COMMENT '마지막 실행 시각',
    last_result VARCHAR(20) COMMENT '마지막 실행 결과 (SUCCESS/FAILURE)',
    last_result_message TEXT COMMENT '마지막 실행 결과 메시지',
    created_at DATETIME DEFAULT NOW(),
    created_by BIGINT,
    updated_at DATETIME DEFAULT NOW(),
    updated_by BIGINT
);

-- Register all 8 existing batch jobs
INSERT INTO tb_batch_job (job_name, job_description, cron_expression, is_active, created_by) VALUES
('AssetExpiryJob', 'HW 자산 보증 만료 알림 (30일 이내)', '0 0 7 * * *', 'Y', 1),
('SlaWarningJob', 'SLA 경고 알림 (경과율 80% 이상)', '0 0 * * * *', 'Y', 1),
('SlaOverdueJob', 'SLA 초과 알림', '0 10 * * * *', 'Y', 1),
('UnassignedIncidentJob', '미배정 장애 알림', '0 20 * * * *', 'Y', 1),
('RepeatIncidentJob', '반복 장애 자산 알림 (30일간 3건+)', '0 0 8 * * *', 'Y', 1),
('LongPendingSrJob', '장기 미처리 서비스요청 알림 (2일+)', '0 0 9 * * *', 'Y', 1),
('InspectionAlertJob', '점검 임박 알림 (7일 이내)', '0 0 7 * * *', 'Y', 1),
('MissedInspectionJob', '미실시 점검 알림', '0 30 8 * * *', 'Y', 1);
