-- ============================================================
-- Phase 27 (2026-09-16 전수조사 P3) — 운영 DB 반영용
-- 장애보고서 동적 폼 양식 시드. 운영 tb_report_form 은 비어 있고(덤프 itsm_data.sql 확인),
-- 프론트가 report_form_id=1 로 저장을 시도해 FK 위반 500 이 나던 것을 고친다.
-- 멱등: 이미 form_id=1 이 있으면 건너뛴다.
-- ============================================================

INSERT IGNORE INTO tb_report_form (form_id, form_nm, form_type_cd, form_schema, is_active, created_at, created_by) VALUES
(1, '장애보고서 기본 양식', 'INCIDENT',
 '[{"key":"summary","label":"장애 요약","type":"text","required":true,"placeholder":"현상을 한 줄로"},{"key":"impact","label":"영향 범위","type":"textarea","required":true},{"key":"cause","label":"원인","type":"textarea","required":true},{"key":"solution","label":"조치 내용","type":"textarea","required":true},{"key":"prevention","label":"재발 방지 대책","type":"textarea"},{"key":"resolvedAt","label":"복구 완료 일시","type":"date"}]',
 'Y', NOW(), 1);

-- 검증
SELECT form_id, form_nm, form_type_cd, is_active, JSON_VALID(form_schema) AS schema_ok FROM tb_report_form;
-- 기대: 1행, schema_ok = 1
