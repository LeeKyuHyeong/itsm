-- Phase 26: ServiceRequest 일정 관리 컬럼 추가
-- 적용 대상: 운영 DB (ddl-auto: update 가 자동 생성하지만, 백필이 필요하여 수동 실행 권장)

-- 1) 컬럼 추가
ALTER TABLE tb_service_request
    ADD COLUMN received_at            DATETIME      NULL COMMENT '접수일',
    ADD COLUMN scheduled_at           DATETIME      NULL COMMENT '처리예정일 (담당자 배정 시 설정)',
    ADD COLUMN revised_scheduled_at   DATETIME      NULL COMMENT '처리변경예정일',
    ADD COLUMN schedule_change_reason VARCHAR(500)  NULL COMMENT '처리예정일 변경 사유',
    ADD COLUMN processed_at           DATETIME      NULL COMMENT '처리일 (PENDING_COMPLETE 전환 시 자동)';

-- 2) 기존 데이터 백필
-- 접수일 = 생성일
UPDATE tb_service_request
   SET received_at = created_at
 WHERE received_at IS NULL;

-- 처리일 = 기존 완료일 (PENDING_COMPLETE 이후 상태인 건만)
UPDATE tb_service_request
   SET processed_at = completed_at
 WHERE processed_at IS NULL
   AND completed_at IS NOT NULL;
