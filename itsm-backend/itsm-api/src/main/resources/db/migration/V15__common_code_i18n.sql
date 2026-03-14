-- Common Code i18n: Add English name columns

ALTER TABLE tb_common_code ADD COLUMN group_nm_en VARCHAR(100) AFTER group_nm;
ALTER TABLE tb_common_code_detail ADD COLUMN code_nm_en VARCHAR(100) AFTER code_nm;

-- Update existing code groups with English names
UPDATE tb_common_code SET group_nm_en = 'Priority' WHERE group_cd = 'PRIORITY';
UPDATE tb_common_code SET group_nm_en = 'Incident Type' WHERE group_cd = 'INCIDENT_TYPE';
UPDATE tb_common_code SET group_nm_en = 'Request Type' WHERE group_cd = 'REQUEST_TYPE';
UPDATE tb_common_code SET group_nm_en = 'Change Type' WHERE group_cd = 'CHANGE_TYPE';
UPDATE tb_common_code SET group_nm_en = 'HW Asset Type' WHERE group_cd = 'ASSET_HW_TYPE';
UPDATE tb_common_code SET group_nm_en = 'SW Asset Type' WHERE group_cd = 'ASSET_SW_TYPE';
UPDATE tb_common_code SET group_nm_en = 'Inspection Type' WHERE group_cd = 'INSPECTION_TYPE';
UPDATE tb_common_code SET group_nm_en = 'Report Form Type' WHERE group_cd = 'REPORT_FORM_TYPE';
UPDATE tb_common_code SET group_nm_en = 'Board Type' WHERE group_cd = 'BOARD_TYPE';
UPDATE tb_common_code SET group_nm_en = 'Notification Type' WHERE group_cd = 'NOTIFICATION_TYPE';
UPDATE tb_common_code SET group_nm_en = 'Asset Category' WHERE group_cd = 'ASSET_CATEGORY';
UPDATE tb_common_code SET group_nm_en = 'Infra HW Sub-category' WHERE group_cd = 'ASSET_SUB_INFRA_HW';
UPDATE tb_common_code SET group_nm_en = 'Infra SW Sub-category' WHERE group_cd = 'ASSET_SUB_INFRA_SW';
UPDATE tb_common_code SET group_nm_en = 'OA Sub-category' WHERE group_cd = 'ASSET_SUB_OA';

-- PRIORITY
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'CRITICAL' THEN 'Critical'
    WHEN 'HIGH' THEN 'High'
    WHEN 'MEDIUM' THEN 'Medium'
    WHEN 'LOW' THEN 'Low'
END WHERE g.group_cd = 'PRIORITY';

-- INCIDENT_TYPE
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'SYSTEM_DOWN' THEN 'System Down'
    WHEN 'NETWORK_ISSUE' THEN 'Network Issue'
    WHEN 'SECURITY' THEN 'Security Incident'
    WHEN 'PERFORMANCE' THEN 'Performance Degradation'
    WHEN 'DATA_ISSUE' THEN 'Data Error'
    WHEN 'OTHER' THEN 'Other'
END WHERE g.group_cd = 'INCIDENT_TYPE';

-- REQUEST_TYPE
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'ACCOUNT' THEN 'Account Request'
    WHEN 'INSTALL' THEN 'Installation Request'
    WHEN 'CONFIGURATION' THEN 'Configuration Change'
    WHEN 'DATA_REQUEST' THEN 'Data Request'
    WHEN 'INQUIRY' THEN 'Inquiry'
    WHEN 'OTHER' THEN 'Other'
END WHERE g.group_cd = 'REQUEST_TYPE';

-- CHANGE_TYPE
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'EMERGENCY' THEN 'Emergency Change'
    WHEN 'NORMAL' THEN 'Normal Change'
    WHEN 'STANDARD' THEN 'Standard Change'
END WHERE g.group_cd = 'CHANGE_TYPE';

-- ASSET_HW_TYPE
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'SERVER' THEN 'Server'
    WHEN 'NETWORK' THEN 'Network Equipment'
    WHEN 'PC' THEN 'PC/Laptop'
    WHEN 'STORAGE' THEN 'Storage'
    WHEN 'SECURITY' THEN 'Security Equipment'
    WHEN 'OTHER' THEN 'Other'
END WHERE g.group_cd = 'ASSET_HW_TYPE';

-- ASSET_SW_TYPE
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'OS' THEN 'Operating System'
    WHEN 'WAS' THEN 'WAS'
    WHEN 'DB' THEN 'DBMS'
    WHEN 'SECURITY' THEN 'Security Software'
    WHEN 'OFFICE' THEN 'Office/Business'
    WHEN 'OTHER' THEN 'Other'
END WHERE g.group_cd = 'ASSET_SW_TYPE';

-- INSPECTION_TYPE
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'MONTHLY' THEN 'Monthly Inspection'
    WHEN 'QUARTERLY' THEN 'Quarterly Inspection'
    WHEN 'ANNUAL' THEN 'Annual Inspection'
    WHEN 'SPECIAL' THEN 'Special Inspection'
END WHERE g.group_cd = 'INSPECTION_TYPE';

-- REPORT_FORM_TYPE
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'INCIDENT' THEN 'Incident Report'
    WHEN 'SERVICE_REQUEST' THEN 'Service Request Report'
    WHEN 'CHANGE' THEN 'Change Report'
    WHEN 'INSPECTION' THEN 'Inspection Report'
END WHERE g.group_cd = 'REPORT_FORM_TYPE';

-- BOARD_TYPE
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'NOTICE' THEN 'Notice'
    WHEN 'FREE' THEN 'Free Board'
    WHEN 'ARCHIVE' THEN 'Archive'
END WHERE g.group_cd = 'BOARD_TYPE';

-- NOTIFICATION_TYPE
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'INCIDENT' THEN 'Incident Notification'
    WHEN 'SERVICE_REQUEST' THEN 'Service Request Notification'
    WHEN 'CHANGE' THEN 'Change Notification'
    WHEN 'INSPECTION' THEN 'Inspection Notification'
    WHEN 'SLA_WARNING' THEN 'SLA Warning'
    WHEN 'SYSTEM' THEN 'System Notification'
END WHERE g.group_cd = 'NOTIFICATION_TYPE';

-- ASSET_CATEGORY
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'INFRA_HW' THEN 'Infrastructure HW'
    WHEN 'INFRA_SW' THEN 'Infrastructure SW'
    WHEN 'OA' THEN 'OA Assets'
END WHERE g.group_cd = 'ASSET_CATEGORY';

-- ASSET_SUB_INFRA_HW
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'SERVER_RACK' THEN 'Rack Mount Server'
    WHEN 'SERVER_BLADE' THEN 'Blade Server'
    WHEN 'SERVER_TOWER' THEN 'Tower Server'
    WHEN 'STORAGE_SAN' THEN 'SAN Storage'
    WHEN 'STORAGE_NAS' THEN 'NAS'
    WHEN 'NETWORK_SWITCH' THEN 'Network Switch'
    WHEN 'NETWORK_ROUTER' THEN 'Router'
    WHEN 'NETWORK_FW' THEN 'Firewall'
    WHEN 'NETWORK_LB' THEN 'Load Balancer'
    WHEN 'NETWORK_AP' THEN 'Wireless AP'
    WHEN 'SECURITY_IDS' THEN 'IDS/IPS'
    WHEN 'SECURITY_WAF' THEN 'WAF'
    WHEN 'POWER_UPS' THEN 'UPS'
    WHEN 'POWER_PDU' THEN 'PDU'
    WHEN 'INFRA_KVM' THEN 'KVM/Console'
END WHERE g.group_cd = 'ASSET_SUB_INFRA_HW';

-- ASSET_SUB_INFRA_SW
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'SW_OS' THEN 'Operating System'
    WHEN 'SW_DB' THEN 'Database'
    WHEN 'SW_WAS' THEN 'WAS/Web Server'
    WHEN 'SW_MIDDLEWARE' THEN 'Middleware'
    WHEN 'SW_MONITORING' THEN 'Monitoring'
    WHEN 'SW_BACKUP' THEN 'Backup Solution'
    WHEN 'SW_SECURITY' THEN 'Security Solution'
    WHEN 'SW_VIRTUALIZATION' THEN 'Virtualization'
    WHEN 'SW_CONTAINER' THEN 'Container/Orchestration'
    WHEN 'SW_CICD' THEN 'CI/CD'
    WHEN 'SW_LICENSE' THEN 'Commercial License'
END WHERE g.group_cd = 'ASSET_SUB_INFRA_SW';

-- ASSET_SUB_OA
UPDATE tb_common_code_detail d JOIN tb_common_code g ON d.group_id = g.group_id
SET d.code_nm_en = CASE d.code_val
    WHEN 'OA_DESKTOP' THEN 'Desktop'
    WHEN 'OA_LAPTOP' THEN 'Laptop'
    WHEN 'OA_MONITOR' THEN 'Monitor'
    WHEN 'OA_PRINTER' THEN 'Printer/MFP'
    WHEN 'OA_PHONE' THEN 'Phone/VoIP'
    WHEN 'OA_TABLET' THEN 'Tablet'
    WHEN 'OA_PERIPHERAL' THEN 'Peripheral'
    WHEN 'OA_PROJECTOR' THEN 'Projector'
END WHERE g.group_cd = 'ASSET_SUB_OA';
