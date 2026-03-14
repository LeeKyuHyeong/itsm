-- V17: Add English name columns for Menu, BatchJob, BoardConfig i18n support

-- 1. tb_menu: menu_nm_en
ALTER TABLE tb_menu ADD COLUMN menu_nm_en VARCHAR(100) NULL COMMENT '메뉴명(영문)' AFTER menu_nm;

UPDATE tb_menu SET menu_nm_en = 'Dashboard' WHERE menu_nm = '대시보드';
UPDATE tb_menu SET menu_nm_en = 'Incident Management' WHERE menu_nm = '장애관리';
UPDATE tb_menu SET menu_nm_en = 'Incident List' WHERE menu_nm = '장애 목록';
UPDATE tb_menu SET menu_nm_en = 'Register Incident' WHERE menu_nm = '장애 등록';
UPDATE tb_menu SET menu_nm_en = 'Service Request' WHERE menu_nm = '서비스요청';
UPDATE tb_menu SET menu_nm_en = 'Request List' WHERE menu_nm = '요청 목록';
UPDATE tb_menu SET menu_nm_en = 'Register Request' WHERE menu_nm = '요청 등록';
UPDATE tb_menu SET menu_nm_en = 'Change Management' WHERE menu_nm = '변경관리';
UPDATE tb_menu SET menu_nm_en = 'Change List' WHERE menu_nm = '변경 목록';
UPDATE tb_menu SET menu_nm_en = 'Register Change' WHERE menu_nm = '변경 등록';
UPDATE tb_menu SET menu_nm_en = 'Asset Management' WHERE menu_nm = '자산관리';
UPDATE tb_menu SET menu_nm_en = 'HW Asset List' WHERE menu_nm = 'HW 자산 목록';
UPDATE tb_menu SET menu_nm_en = 'SW Asset List' WHERE menu_nm = 'SW 자산 목록';
UPDATE tb_menu SET menu_nm_en = 'Inspection Management' WHERE menu_nm = '정기점검관리';
UPDATE tb_menu SET menu_nm_en = 'Inspection List' WHERE menu_nm = '점검 목록';
UPDATE tb_menu SET menu_nm_en = 'Register Inspection' WHERE menu_nm = '점검 등록';
UPDATE tb_menu SET menu_nm_en = 'Report Management' WHERE menu_nm = '보고관리';
UPDATE tb_menu SET menu_nm_en = 'Report List' WHERE menu_nm = '보고서 목록';
UPDATE tb_menu SET menu_nm_en = 'Board' WHERE menu_nm = '게시판';
UPDATE tb_menu SET menu_nm_en = 'Settings' WHERE menu_nm = '설정관리';
UPDATE tb_menu SET menu_nm_en = 'Menu Management' WHERE menu_nm = '메뉴 관리';
UPDATE tb_menu SET menu_nm_en = 'Common Code' WHERE menu_nm = '공통코드 관리';
UPDATE tb_menu SET menu_nm_en = 'Board Management' WHERE menu_nm = '게시판 관리';
UPDATE tb_menu SET menu_nm_en = 'SLA Management' WHERE menu_nm = 'SLA 관리';
UPDATE tb_menu SET menu_nm_en = 'Notification Policy' WHERE menu_nm = '알림 정책 관리';
UPDATE tb_menu SET menu_nm_en = 'Batch Management' WHERE menu_nm = '배치 관리';
UPDATE tb_menu SET menu_nm_en = 'Account Management' WHERE menu_nm = '계정관리';
UPDATE tb_menu SET menu_nm_en = 'User Management' WHERE menu_nm = '사용자 관리';
UPDATE tb_menu SET menu_nm_en = 'Organization Management' WHERE menu_nm = '조직 관리';

-- 2. tb_batch_job: job_name_en
ALTER TABLE tb_batch_job ADD COLUMN job_name_en VARCHAR(100) NULL COMMENT '배치작업명(영문)' AFTER job_name;

UPDATE tb_batch_job SET job_name_en = 'Asset Expiry Alert' WHERE job_name = 'AssetExpiryJob';
UPDATE tb_batch_job SET job_name_en = 'SLA Warning Alert' WHERE job_name = 'SlaWarningJob';
UPDATE tb_batch_job SET job_name_en = 'SLA Overdue Alert' WHERE job_name = 'SlaOverdueJob';
UPDATE tb_batch_job SET job_name_en = 'Unassigned Incident Alert' WHERE job_name = 'UnassignedIncidentJob';
UPDATE tb_batch_job SET job_name_en = 'Repeat Incident Alert' WHERE job_name = 'RepeatIncidentJob';
UPDATE tb_batch_job SET job_name_en = 'License Expiry Alert' WHERE job_name = 'SwLicenseExpiryJob';
UPDATE tb_batch_job SET job_name_en = 'Inspection Overdue Alert' WHERE job_name = 'InspectionOverdueJob';
UPDATE tb_batch_job SET job_name_en = 'Pending Change Alert' WHERE job_name = 'PendingChangeApprovalJob';
UPDATE tb_batch_job SET job_name_en = 'Asset Auto Register' WHERE job_name = 'AssetAutoRegisterJob';
UPDATE tb_batch_job SET job_name_en = 'Incident Simulation' WHERE job_name = 'IncidentSimulationJob';
UPDATE tb_batch_job SET job_name_en = 'Service Request Simulation' WHERE job_name = 'ServiceRequestSimulationJob';
UPDATE tb_batch_job SET job_name_en = 'Change Simulation' WHERE job_name = 'ChangeSimulationJob';
UPDATE tb_batch_job SET job_name_en = 'Inspection Simulation' WHERE job_name = 'InspectionSimulationJob';
UPDATE tb_batch_job SET job_name_en = 'Report Simulation' WHERE job_name = 'ReportSimulationJob';
UPDATE tb_batch_job SET job_name_en = 'Daily Stats Aggregation' WHERE job_name = 'DailyStatsAggregationJob';

-- 3. tb_board_config: board_nm_en
ALTER TABLE tb_board_config ADD COLUMN board_nm_en VARCHAR(100) NULL COMMENT '게시판명(영문)' AFTER board_nm;

UPDATE tb_board_config SET board_nm_en = 'Notice' WHERE board_nm = '공지사항';
UPDATE tb_board_config SET board_nm_en = 'Free Board' WHERE board_nm = '자유게시판';
UPDATE tb_board_config SET board_nm_en = 'Data Room' WHERE board_nm = '자료실';
UPDATE tb_board_config SET board_nm_en = 'FAQ' WHERE board_nm = 'FAQ';
