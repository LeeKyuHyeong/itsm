-- ============================================================
-- ITSM DDL Script (MySQL/MariaDB)
-- 물리적 ERD 기반 전체 테이블 생성
-- ============================================================

-- 실행 순서: FK 의존관계 고려하여 순서 정렬

-- ============================================================
-- 1. 시스템/계정 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_company (
    company_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '회사ID',
    company_nm      VARCHAR(100)    NOT NULL                 COMMENT '회사명',
    biz_no          VARCHAR(20)     NULL                     COMMENT '사업자번호',
    ceo_nm          VARCHAR(50)     NULL                     COMMENT '대표자명',
    tel             VARCHAR(20)     NULL                     COMMENT '연락처',
    default_pm_id   BIGINT          NULL                     COMMENT '대표 PM (담당자 공백 시 자동 배정)',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자',
    PRIMARY KEY (company_id),
    UNIQUE KEY uk_company_biz_no (biz_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회사';

CREATE TABLE IF NOT EXISTS tb_department (
    dept_id         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '부서ID',
    dept_nm         VARCHAR(100)    NOT NULL                 COMMENT '부서명',
    company_id      BIGINT          NOT NULL                 COMMENT '회사ID',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자',
    PRIMARY KEY (dept_id),
    INDEX idx_department_company_id (company_id),
    CONSTRAINT fk_department_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부서';

CREATE TABLE IF NOT EXISTS tb_user (
    user_id         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '사용자ID (절대 재사용 안 됨)',
    login_id        VARCHAR(50)     NULL                     COMMENT '로그인ID (DELETED 시 마스킹)',
    password        VARCHAR(255)    NOT NULL                 COMMENT '비밀번호 (BCrypt 암호화)',
    user_nm         VARCHAR(50)     NOT NULL                 COMMENT '이름',
    employee_no     VARCHAR(30)     NULL                     COMMENT '사번 (재사용 가능)',
    dept_id         BIGINT          NULL                     COMMENT '부서ID',
    email           VARCHAR(100)    NULL                     COMMENT '이메일',
    tel             VARCHAR(20)     NULL                     COMMENT '연락처',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE/RESIGNED/ABSENT/DELETED',
    valid_from      DATETIME        NOT NULL                 COMMENT '유효 시작일시',
    valid_to        DATETIME        NULL                     COMMENT '유효 종료일시 (NULL=현재 유효)',
    last_login_at   DATETIME        NULL                     COMMENT '마지막 로그인 일시',
    pwd_changed_at  DATETIME        NULL                     COMMENT '비밀번호 변경일시',
    login_fail_cnt  INT             NOT NULL DEFAULT 0       COMMENT '로그인 실패 횟수',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_login_id (login_id),
    INDEX idx_user_dept_id (dept_id),
    INDEX idx_user_status (status),
    CONSTRAINT fk_user_department FOREIGN KEY (dept_id) REFERENCES tb_department (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자';

-- tb_company FK (default_pm_id -> tb_user) : 순환참조이므로 ALTER로 추가
ALTER TABLE tb_company
    ADD CONSTRAINT fk_company_default_pm FOREIGN KEY (default_pm_id) REFERENCES tb_user (user_id);
ALTER TABLE tb_company
    ADD CONSTRAINT fk_company_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id);
ALTER TABLE tb_company
    ADD CONSTRAINT fk_company_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id);

ALTER TABLE tb_department
    ADD CONSTRAINT fk_department_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id);
ALTER TABLE tb_department
    ADD CONSTRAINT fk_department_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id);

-- tb_user 자기참조 FK
ALTER TABLE tb_user
    ADD CONSTRAINT fk_user_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id);
ALTER TABLE tb_user
    ADD CONSTRAINT fk_user_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id);

CREATE TABLE IF NOT EXISTS tb_user_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    user_id         BIGINT          NOT NULL                 COMMENT '사용자ID',
    login_id        VARCHAR(50)     NOT NULL                 COMMENT '변경 시점 로그인ID',
    user_nm         VARCHAR(50)     NOT NULL                 COMMENT '변경 시점 이름',
    employee_no     VARCHAR(30)     NULL                     COMMENT '변경 시점 사번',
    dept_id         BIGINT          NULL                     COMMENT '변경 시점 부서ID',
    email           VARCHAR(100)    NULL                     COMMENT '변경 시점 이메일',
    tel             VARCHAR(20)     NULL                     COMMENT '변경 시점 연락처',
    status          VARCHAR(20)     NOT NULL                 COMMENT '변경 시점 상태',
    changed_field   VARCHAR(100)    NULL                     COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    valid_from      DATETIME        NOT NULL                 COMMENT '유효 시작일시',
    valid_to        DATETIME        NULL                     COMMENT '유효 종료일시',
    batch_job_id    VARCHAR(50)     NULL                     COMMENT '일괄변경 시 배치작업ID',
    created_at      DATETIME        NOT NULL                 COMMENT '이력 생성일시',
    created_by      BIGINT          NULL                     COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_user_history_user_id (user_id),
    INDEX idx_user_history_valid_from (valid_from),
    INDEX idx_user_history_batch_job_id (batch_job_id),
    CONSTRAINT fk_user_history_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_user_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 변경 이력 (Temporal Data Modeling)';

CREATE TABLE IF NOT EXISTS tb_role (
    role_id         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '역할ID',
    role_nm         VARCHAR(50)     NOT NULL                 COMMENT '역할명',
    role_cd         VARCHAR(50)     NOT NULL                 COMMENT '역할코드 (ex. ROLE_ADMIN)',
    description     VARCHAR(200)    NULL                     COMMENT '설명',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_cd (role_cd),
    CONSTRAINT fk_role_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역할';

CREATE TABLE IF NOT EXISTS tb_user_role (
    user_id         BIGINT          NOT NULL                 COMMENT '사용자ID',
    role_id         BIGINT          NOT NULL                 COMMENT '역할ID',
    granted_at      DATETIME        NOT NULL                 COMMENT '부여일시',
    granted_by      BIGINT          NULL                     COMMENT '부여자ID',
    PRIMARY KEY (user_id, role_id),
    INDEX idx_user_role_role_id (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES tb_role (role_id),
    CONSTRAINT fk_user_role_granted_by FOREIGN KEY (granted_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자-역할 매핑';

CREATE TABLE IF NOT EXISTS tb_menu (
    menu_id         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '메뉴ID',
    parent_menu_id  BIGINT          NULL                     COMMENT '상위메뉴ID (NULL=최상위)',
    menu_nm         VARCHAR(100)    NOT NULL                 COMMENT '메뉴명',
    menu_nm_en      VARCHAR(100)    NULL                     COMMENT '메뉴명(영문)',
    menu_url        VARCHAR(200)    NULL                     COMMENT 'URL',
    icon            VARCHAR(50)     NULL                     COMMENT '아이콘',
    sort_order      INT             NOT NULL DEFAULT 0       COMMENT '정렬순서',
    is_visible      CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '노출여부',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자',
    PRIMARY KEY (menu_id),
    INDEX idx_menu_parent_id (parent_menu_id),
    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_menu_id) REFERENCES tb_menu (menu_id),
    CONSTRAINT fk_menu_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_menu_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴';

CREATE TABLE IF NOT EXISTS tb_role_menu (
    role_id         BIGINT          NOT NULL                 COMMENT '역할ID',
    menu_id         BIGINT          NOT NULL                 COMMENT '메뉴ID',
    can_read        CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '읽기 권한',
    can_write       CHAR(1)         NOT NULL DEFAULT 'N'     COMMENT '쓰기 권한',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자',
    PRIMARY KEY (role_id, menu_id),
    INDEX idx_role_menu_menu_id (menu_id),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES tb_role (role_id),
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES tb_menu (menu_id),
    CONSTRAINT fk_role_menu_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역할-메뉴 매핑';

CREATE TABLE IF NOT EXISTS tb_company_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    company_id      BIGINT          NOT NULL                 COMMENT '회사ID',
    company_nm      VARCHAR(100)    NOT NULL                 COMMENT '변경 전 회사명',
    biz_no          VARCHAR(20)     NULL                     COMMENT '변경 전 사업자번호',
    ceo_nm          VARCHAR(50)     NULL                     COMMENT '변경 전 대표자명',
    changed_field   VARCHAR(100)    NULL                     COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    created_at      DATETIME        NOT NULL                 COMMENT '이력 생성일시',
    created_by      BIGINT          NULL                     COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_company_history_company_id (company_id),
    CONSTRAINT fk_company_history_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id),
    CONSTRAINT fk_company_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회사 변경 이력';

CREATE TABLE IF NOT EXISTS tb_department_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    dept_id         BIGINT          NOT NULL                 COMMENT '부서ID',
    dept_nm         VARCHAR(100)    NOT NULL                 COMMENT '변경 전 부서명',
    company_id      BIGINT          NULL                     COMMENT '변경 전 소속 회사ID',
    changed_field   VARCHAR(100)    NULL                     COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    created_at      DATETIME        NOT NULL                 COMMENT '이력 생성일시',
    created_by      BIGINT          NULL                     COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_dept_history_dept_id (dept_id),
    CONSTRAINT fk_dept_history_dept FOREIGN KEY (dept_id) REFERENCES tb_department (dept_id),
    CONSTRAINT fk_dept_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부서 변경 이력';

-- ============================================================
-- 2. 자산(CMDB) 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_asset_hw (
    asset_hw_id     BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'HW자산ID',
    asset_nm        VARCHAR(100)    NOT NULL                 COMMENT '자산명',
    asset_type_cd   VARCHAR(50)     NOT NULL                 COMMENT '자산유형코드 (공통코드 - 서버/네트워크/PC 등)',
    manufacturer    VARCHAR(100)    NULL                     COMMENT '제조사',
    model_nm        VARCHAR(100)    NULL                     COMMENT '모델명',
    serial_no       VARCHAR(100)    NULL                     COMMENT '시리얼번호',
    ip_address      VARCHAR(50)     NULL                     COMMENT 'IP주소',
    mac_address     VARCHAR(50)     NULL                     COMMENT 'MAC주소',
    location        VARCHAR(200)    NULL                     COMMENT '설치위치 (랙/층/건물 등)',
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
    PRIMARY KEY (asset_hw_id),
    UNIQUE KEY uk_asset_hw_serial_no (serial_no),
    INDEX idx_asset_hw_company_id (company_id),
    INDEX idx_asset_hw_status (status),
    INDEX idx_asset_hw_asset_type_cd (asset_type_cd),
    CONSTRAINT fk_asset_hw_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id),
    CONSTRAINT fk_asset_hw_manager FOREIGN KEY (manager_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_asset_hw_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_asset_hw_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HW 자산';

CREATE TABLE IF NOT EXISTS tb_asset_sw (
    asset_sw_id     BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'SW자산ID',
    sw_nm           VARCHAR(100)    NOT NULL                 COMMENT '소프트웨어명',
    sw_type_cd      VARCHAR(50)     NOT NULL                 COMMENT 'SW유형코드 (공통코드 - OS/WAS/DB/보안 등)',
    version         VARCHAR(50)     NULL                     COMMENT '버전',
    license_key     VARCHAR(200)    NULL                     COMMENT '라이선스키',
    license_cnt     INT             NULL                     COMMENT '라이선스 수량',
    installed_at    DATE            NULL                     COMMENT '설치일',
    expired_at      DATE            NULL                     COMMENT '라이선스 만료일',
    company_id      BIGINT          NOT NULL                 COMMENT '소속 고객사ID',
    manager_id      BIGINT          NULL                     COMMENT '담당자ID',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE/DISPOSED',
    description     TEXT            NULL                     COMMENT '비고',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자',
    PRIMARY KEY (asset_sw_id),
    INDEX idx_asset_sw_company_id (company_id),
    INDEX idx_asset_sw_status (status),
    INDEX idx_asset_sw_sw_type_cd (sw_type_cd),
    CONSTRAINT fk_asset_sw_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id),
    CONSTRAINT fk_asset_sw_manager FOREIGN KEY (manager_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_asset_sw_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_asset_sw_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SW 자산';

CREATE TABLE IF NOT EXISTS tb_asset_relation (
    asset_hw_id     BIGINT          NOT NULL                 COMMENT 'HW자산ID',
    asset_sw_id     BIGINT          NOT NULL                 COMMENT 'SW자산ID',
    installed_at    DATE            NULL                     COMMENT '설치일',
    removed_at      DATE            NULL                     COMMENT '삭제일 (NULL=현재 설치 중)',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자',
    PRIMARY KEY (asset_hw_id, asset_sw_id),
    INDEX idx_asset_relation_sw_id (asset_sw_id),
    CONSTRAINT fk_asset_relation_hw FOREIGN KEY (asset_hw_id) REFERENCES tb_asset_hw (asset_hw_id),
    CONSTRAINT fk_asset_relation_sw FOREIGN KEY (asset_sw_id) REFERENCES tb_asset_sw (asset_sw_id),
    CONSTRAINT fk_asset_relation_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HW-SW 연관관계';

CREATE TABLE IF NOT EXISTS tb_asset_hw_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    asset_hw_id     BIGINT          NOT NULL                 COMMENT 'HW자산ID',
    changed_field   VARCHAR(100)    NOT NULL                 COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    batch_job_id    VARCHAR(50)     NULL                     COMMENT '일괄변경 시 배치작업ID',
    created_at      DATETIME        NOT NULL                 COMMENT '이력 생성일시',
    created_by      BIGINT          NULL                     COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_asset_hw_history_hw_id (asset_hw_id),
    CONSTRAINT fk_asset_hw_history_hw FOREIGN KEY (asset_hw_id) REFERENCES tb_asset_hw (asset_hw_id),
    CONSTRAINT fk_asset_hw_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HW 자산 변경 이력';

CREATE TABLE IF NOT EXISTS tb_asset_sw_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    asset_sw_id     BIGINT          NOT NULL                 COMMENT 'SW자산ID',
    changed_field   VARCHAR(100)    NOT NULL                 COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    batch_job_id    VARCHAR(50)     NULL                     COMMENT '일괄변경 시 배치작업ID',
    created_at      DATETIME        NOT NULL                 COMMENT '이력 생성일시',
    created_by      BIGINT          NULL                     COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_asset_sw_history_sw_id (asset_sw_id),
    CONSTRAINT fk_asset_sw_history_sw FOREIGN KEY (asset_sw_id) REFERENCES tb_asset_sw (asset_sw_id),
    CONSTRAINT fk_asset_sw_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SW 자산 변경 이력';

-- ============================================================
-- 3. 장애관리 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_incident (
    incident_id     BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '장애ID',
    title           VARCHAR(200)    NOT NULL                 COMMENT '제목',
    content         TEXT            NOT NULL                 COMMENT '장애 내용',
    incident_type_cd VARCHAR(50)    NOT NULL                 COMMENT '장애유형코드 (공통코드)',
    priority_cd     VARCHAR(20)     NOT NULL                 COMMENT '우선순위코드 (CRITICAL/HIGH/MEDIUM/LOW)',
    status_cd       VARCHAR(20)     NOT NULL DEFAULT 'RECEIVED' COMMENT '상태 (RECEIVED/IN_PROGRESS/COMPLETED/CLOSED/REJECTED)',
    occurred_at     DATETIME        NOT NULL                 COMMENT '장애 발생일시',
    completed_at    DATETIME        NULL                     COMMENT '처리 완료일시',
    closed_at       DATETIME        NULL                     COMMENT '최종 종료일시',
    sla_deadline_at DATETIME        NULL                     COMMENT 'SLA 처리 기한',
    company_id      BIGINT          NOT NULL                 COMMENT '고객사ID',
    main_manager_id BIGINT          NULL                     COMMENT '주담당자ID',
    process_content TEXT            NULL                     COMMENT '처리내용 (주담당자 작성)',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NOT NULL                 COMMENT '등록자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (incident_id),
    INDEX idx_incident_company_id (company_id),
    INDEX idx_incident_status_cd (status_cd),
    INDEX idx_incident_priority_cd (priority_cd),
    INDEX idx_incident_occurred_at (occurred_at),
    INDEX idx_incident_main_manager_id (main_manager_id),
    CONSTRAINT fk_incident_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id),
    CONSTRAINT fk_incident_main_manager FOREIGN KEY (main_manager_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_incident_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_incident_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애 티켓';

CREATE TABLE IF NOT EXISTS tb_incident_asset (
    incident_id     BIGINT          NOT NULL                 COMMENT '장애ID',
    asset_type      VARCHAR(10)     NOT NULL                 COMMENT '자산유형 (HW/SW)',
    asset_id        BIGINT          NOT NULL                 COMMENT '자산ID',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자ID',
    PRIMARY KEY (incident_id, asset_type, asset_id),
    CONSTRAINT fk_incident_asset_incident FOREIGN KEY (incident_id) REFERENCES tb_incident (incident_id),
    CONSTRAINT fk_incident_asset_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애-자산 연관';

CREATE TABLE IF NOT EXISTS tb_incident_assignee (
    incident_id     BIGINT          NOT NULL                 COMMENT '장애ID',
    user_id         BIGINT          NOT NULL                 COMMENT '담당자ID',
    granted_at      DATETIME        NOT NULL                 COMMENT '지정일시',
    granted_by      BIGINT          NULL                     COMMENT '지정자ID',
    PRIMARY KEY (incident_id, user_id),
    INDEX idx_incident_assignee_user_id (user_id),
    CONSTRAINT fk_incident_assignee_incident FOREIGN KEY (incident_id) REFERENCES tb_incident (incident_id),
    CONSTRAINT fk_incident_assignee_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_incident_assignee_granted_by FOREIGN KEY (granted_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애 담당자';

CREATE TABLE IF NOT EXISTS tb_incident_comment (
    comment_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '댓글ID',
    incident_id     BIGINT          NOT NULL                 COMMENT '장애ID',
    content         TEXT            NOT NULL                 COMMENT '댓글 내용',
    created_at      DATETIME        NOT NULL                 COMMENT '작성일시',
    created_by      BIGINT          NOT NULL                 COMMENT '작성자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (comment_id),
    INDEX idx_incident_comment_incident_id (incident_id),
    CONSTRAINT fk_incident_comment_incident FOREIGN KEY (incident_id) REFERENCES tb_incident (incident_id),
    CONSTRAINT fk_incident_comment_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_incident_comment_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애 댓글';

CREATE TABLE IF NOT EXISTS tb_incident_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    incident_id     BIGINT          NOT NULL                 COMMENT '장애ID',
    changed_field   VARCHAR(100)    NOT NULL                 COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    created_at      DATETIME        NOT NULL                 COMMENT '변경일시',
    created_by      BIGINT          NOT NULL                 COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_incident_history_incident_id (incident_id),
    CONSTRAINT fk_incident_history_incident FOREIGN KEY (incident_id) REFERENCES tb_incident (incident_id),
    CONSTRAINT fk_incident_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애 변경 이력';

-- ============================================================
-- 4. 서비스요청 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_service_request (
    request_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '요청ID',
    title           VARCHAR(200)    NOT NULL                 COMMENT '제목',
    content         TEXT            NOT NULL                 COMMENT '요청 내용',
    request_type_cd VARCHAR(50)     NOT NULL                 COMMENT '요청유형코드 (공통코드)',
    priority_cd     VARCHAR(20)     NOT NULL                 COMMENT '우선순위코드 (공통코드)',
    status_cd       VARCHAR(20)     NOT NULL DEFAULT 'RECEIVED' COMMENT '상태 (RECEIVED/ASSIGNED/IN_PROGRESS/PENDING_COMPLETE/CLOSED/CANCELLED/REJECTED)',
    occurred_at     DATETIME        NOT NULL                 COMMENT '요청일시',
    completed_at    DATETIME        NULL                     COMMENT '완료일시',
    closed_at       DATETIME        NULL                     COMMENT '종료일시',
    sla_deadline_at DATETIME        NULL                     COMMENT 'SLA 처리 기한',
    reject_cnt      INT             NOT NULL DEFAULT 0       COMMENT '반려 횟수',
    company_id      BIGINT          NOT NULL                 COMMENT '고객사ID',
    satisfaction_score     TINYINT  NULL                     COMMENT '만족도 점수 (1~5)',
    satisfaction_comment   VARCHAR(500) NULL                 COMMENT '만족도 의견',
    satisfaction_submitted_at DATETIME NULL                  COMMENT '만족도 제출일시',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NOT NULL                 COMMENT '등록자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (request_id),
    INDEX idx_sr_company_id (company_id),
    INDEX idx_sr_status_cd (status_cd),
    INDEX idx_sr_priority_cd (priority_cd),
    INDEX idx_sr_occurred_at (occurred_at),
    CONSTRAINT fk_sr_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id),
    CONSTRAINT fk_sr_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_sr_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스 요청';

CREATE TABLE IF NOT EXISTS tb_service_request_asset (
    request_id      BIGINT          NOT NULL                 COMMENT '요청ID',
    asset_type      VARCHAR(10)     NOT NULL                 COMMENT '자산유형 (HW/SW)',
    asset_id        BIGINT          NOT NULL                 COMMENT '자산ID',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자ID',
    PRIMARY KEY (request_id, asset_type, asset_id),
    CONSTRAINT fk_sr_asset_request FOREIGN KEY (request_id) REFERENCES tb_service_request (request_id),
    CONSTRAINT fk_sr_asset_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스요청-자산 연관';

CREATE TABLE IF NOT EXISTS tb_service_request_assignee (
    request_id      BIGINT          NOT NULL                 COMMENT '요청ID',
    user_id         BIGINT          NOT NULL                 COMMENT '담당자ID',
    process_status  VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '처리상태 (PENDING/IN_PROGRESS/COMPLETED)',
    granted_at      DATETIME        NOT NULL                 COMMENT '지정일시',
    granted_by      BIGINT          NULL                     COMMENT '지정자ID',
    PRIMARY KEY (request_id, user_id),
    INDEX idx_sr_assignee_user_id (user_id),
    CONSTRAINT fk_sr_assignee_request FOREIGN KEY (request_id) REFERENCES tb_service_request (request_id),
    CONSTRAINT fk_sr_assignee_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_sr_assignee_granted_by FOREIGN KEY (granted_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스요청 담당자';

CREATE TABLE IF NOT EXISTS tb_service_request_process (
    process_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '처리ID',
    request_id      BIGINT          NOT NULL                 COMMENT '요청ID',
    user_id         BIGINT          NOT NULL                 COMMENT '담당자ID',
    process_content TEXT            NOT NULL                 COMMENT '처리내용',
    is_completed    CHAR(1)         NOT NULL DEFAULT 'N'     COMMENT '완료여부',
    completed_at    DATETIME        NULL                     COMMENT '완료일시',
    created_at      DATETIME        NOT NULL                 COMMENT '작성일시',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    PRIMARY KEY (process_id),
    INDEX idx_sr_process_request_id (request_id),
    INDEX idx_sr_process_user_id (user_id),
    CONSTRAINT fk_sr_process_request FOREIGN KEY (request_id) REFERENCES tb_service_request (request_id),
    CONSTRAINT fk_sr_process_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스요청 처리내역';

CREATE TABLE IF NOT EXISTS tb_service_request_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    request_id      BIGINT          NOT NULL                 COMMENT '요청ID',
    changed_field   VARCHAR(100)    NOT NULL                 COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    created_at      DATETIME        NOT NULL                 COMMENT '변경일시',
    created_by      BIGINT          NOT NULL                 COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_sr_history_request_id (request_id),
    CONSTRAINT fk_sr_history_request FOREIGN KEY (request_id) REFERENCES tb_service_request (request_id),
    CONSTRAINT fk_sr_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스요청 변경 이력';

-- ============================================================
-- 5. 변경관리 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_change (
    change_id       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '변경ID',
    title           VARCHAR(200)    NOT NULL                 COMMENT '제목',
    content         TEXT            NOT NULL                 COMMENT '변경 내용',
    change_type_cd  VARCHAR(50)     NOT NULL                 COMMENT '변경유형코드 (공통코드 - 긴급/일반/정기 등)',
    priority_cd     VARCHAR(20)     NOT NULL                 COMMENT '우선순위코드 (공통코드)',
    status_cd       VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '상태 (DRAFT/APPROVAL_REQUESTED/APPROVED/IN_PROGRESS/COMPLETED/CLOSED/REJECTED)',
    scheduled_at    DATETIME        NULL                     COMMENT '변경 예정일시',
    rollback_plan   TEXT            NULL                     COMMENT '롤백 계획',
    company_id      BIGINT          NOT NULL                 COMMENT '고객사ID',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NOT NULL                 COMMENT '등록자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (change_id),
    INDEX idx_change_company_id (company_id),
    INDEX idx_change_status_cd (status_cd),
    INDEX idx_change_priority_cd (priority_cd),
    CONSTRAINT fk_change_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id),
    CONSTRAINT fk_change_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_change_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경 요청';

CREATE TABLE IF NOT EXISTS tb_change_asset (
    change_id       BIGINT          NOT NULL                 COMMENT '변경ID',
    asset_type      VARCHAR(10)     NOT NULL                 COMMENT '자산유형 (HW/SW)',
    asset_id        BIGINT          NOT NULL                 COMMENT '자산ID',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자ID',
    PRIMARY KEY (change_id, asset_type, asset_id),
    CONSTRAINT fk_change_asset_change FOREIGN KEY (change_id) REFERENCES tb_change (change_id),
    CONSTRAINT fk_change_asset_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경-자산 연관';

CREATE TABLE IF NOT EXISTS tb_change_approver (
    change_id       BIGINT          NOT NULL                 COMMENT '변경ID',
    user_id         BIGINT          NOT NULL                 COMMENT '승인자ID',
    approve_order   INT             NOT NULL                 COMMENT '승인 순서 (1부터 순차 승인)',
    approve_status  VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '승인상태 (PENDING/APPROVED/REJECTED)',
    approved_at     DATETIME        NULL                     COMMENT '승인/반려 일시',
    comment         VARCHAR(500)    NULL                     COMMENT '승인 의견',
    PRIMARY KEY (change_id, user_id),
    INDEX idx_change_approver_user_id (user_id),
    CONSTRAINT fk_change_approver_change FOREIGN KEY (change_id) REFERENCES tb_change (change_id),
    CONSTRAINT fk_change_approver_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경 승인자';

CREATE TABLE IF NOT EXISTS tb_change_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    change_id       BIGINT          NOT NULL                 COMMENT '변경ID',
    changed_field   VARCHAR(100)    NOT NULL                 COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    created_at      DATETIME        NOT NULL                 COMMENT '변경일시',
    created_by      BIGINT          NOT NULL                 COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_change_history_change_id (change_id),
    CONSTRAINT fk_change_history_change FOREIGN KEY (change_id) REFERENCES tb_change (change_id),
    CONSTRAINT fk_change_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경 이력';

CREATE TABLE IF NOT EXISTS tb_change_comment (
    comment_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '댓글ID',
    change_id       BIGINT          NOT NULL                 COMMENT '변경ID',
    content         TEXT            NOT NULL                 COMMENT '댓글 내용',
    created_at      DATETIME        NOT NULL                 COMMENT '작성일시',
    created_by      BIGINT          NOT NULL                 COMMENT '작성자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (comment_id),
    INDEX idx_change_comment_change_id (change_id),
    CONSTRAINT fk_change_comment_change FOREIGN KEY (change_id) REFERENCES tb_change (change_id),
    CONSTRAINT fk_change_comment_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_change_comment_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경 댓글';

-- ============================================================
-- 6. 정기점검 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_inspection (
    inspection_id   BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '점검ID',
    title           VARCHAR(200)    NOT NULL                 COMMENT '점검 제목',
    inspection_type_cd VARCHAR(50)  NOT NULL                 COMMENT '점검유형코드 (공통코드 - 월간/분기/연간 등)',
    status_cd       VARCHAR(20)     NOT NULL DEFAULT 'SCHEDULED' COMMENT '상태 (SCHEDULED/IN_PROGRESS/COMPLETED/CLOSED/ON_HOLD)',
    scheduled_at    DATE            NOT NULL                 COMMENT '점검 예정일',
    completed_at    DATETIME        NULL                     COMMENT '점검 완료일시',
    closed_at       DATETIME        NULL                     COMMENT '종료일시',
    company_id      BIGINT          NOT NULL                 COMMENT '고객사ID',
    manager_id      BIGINT          NULL                     COMMENT '담당자ID',
    description     TEXT            NULL                     COMMENT '점검 설명',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NOT NULL                 COMMENT '등록자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (inspection_id),
    INDEX idx_inspection_company_id (company_id),
    INDEX idx_inspection_status_cd (status_cd),
    INDEX idx_inspection_scheduled_at (scheduled_at),
    CONSTRAINT fk_inspection_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id),
    CONSTRAINT fk_inspection_manager FOREIGN KEY (manager_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_inspection_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_inspection_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='정기점검';

CREATE TABLE IF NOT EXISTS tb_inspection_asset (
    inspection_id   BIGINT          NOT NULL                 COMMENT '점검ID',
    asset_type      VARCHAR(10)     NOT NULL                 COMMENT '자산유형 (HW/SW)',
    asset_id        BIGINT          NOT NULL                 COMMENT '자산ID',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자ID',
    PRIMARY KEY (inspection_id, asset_type, asset_id),
    CONSTRAINT fk_inspection_asset_inspection FOREIGN KEY (inspection_id) REFERENCES tb_inspection (inspection_id),
    CONSTRAINT fk_inspection_asset_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='점검-자산 연관';

CREATE TABLE IF NOT EXISTS tb_inspection_item (
    item_id         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '항목ID',
    inspection_id   BIGINT          NOT NULL                 COMMENT '점검ID',
    item_nm         VARCHAR(200)    NOT NULL                 COMMENT '항목명',
    sort_order      INT             NOT NULL DEFAULT 0       COMMENT '항목 순서',
    is_required     CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '필수 여부',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NOT NULL                 COMMENT '등록자ID',
    PRIMARY KEY (item_id),
    INDEX idx_inspection_item_inspection_id (inspection_id),
    CONSTRAINT fk_inspection_item_inspection FOREIGN KEY (inspection_id) REFERENCES tb_inspection (inspection_id),
    CONSTRAINT fk_inspection_item_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='점검 체크리스트 항목';

CREATE TABLE IF NOT EXISTS tb_inspection_result (
    result_id       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '결과ID',
    inspection_id   BIGINT          NOT NULL                 COMMENT '점검ID',
    item_id         BIGINT          NOT NULL                 COMMENT '항목ID',
    result_value    TEXT            NULL                     COMMENT '결과값',
    is_normal       CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '정상여부 (Y/N)',
    remark          VARCHAR(500)    NULL                     COMMENT '비고',
    created_at      DATETIME        NOT NULL                 COMMENT '작성일시',
    created_by      BIGINT          NOT NULL                 COMMENT '작성자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (result_id),
    INDEX idx_inspection_result_inspection_id (inspection_id),
    INDEX idx_inspection_result_item_id (item_id),
    CONSTRAINT fk_inspection_result_inspection FOREIGN KEY (inspection_id) REFERENCES tb_inspection (inspection_id),
    CONSTRAINT fk_inspection_result_item FOREIGN KEY (item_id) REFERENCES tb_inspection_item (item_id),
    CONSTRAINT fk_inspection_result_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_inspection_result_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='점검 결과';

CREATE TABLE IF NOT EXISTS tb_inspection_history (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '이력ID',
    inspection_id   BIGINT          NOT NULL                 COMMENT '점검ID',
    changed_field   VARCHAR(100)    NOT NULL                 COMMENT '변경된 항목명',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    created_at      DATETIME        NOT NULL                 COMMENT '변경일시',
    created_by      BIGINT          NOT NULL                 COMMENT '변경자ID',
    PRIMARY KEY (history_id),
    INDEX idx_inspection_history_inspection_id (inspection_id),
    CONSTRAINT fk_inspection_history_inspection FOREIGN KEY (inspection_id) REFERENCES tb_inspection (inspection_id),
    CONSTRAINT fk_inspection_history_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='점검 변경 이력';

-- ============================================================
-- 7. 보고관리 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_report_form (
    form_id         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '양식ID',
    form_nm         VARCHAR(100)    NOT NULL                 COMMENT '양식명',
    form_type_cd    VARCHAR(50)     NOT NULL                 COMMENT '양식유형코드 (공통코드 - 장애/서비스요청/변경/점검 등)',
    form_schema     JSON            NOT NULL                 COMMENT '양식 구조 JSON (동적 폼 정의)',
    is_active       CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '사용여부',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NOT NULL                 COMMENT '등록자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (form_id),
    INDEX idx_report_form_type_cd (form_type_cd),
    CONSTRAINT fk_report_form_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_report_form_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='보고서 양식';

CREATE TABLE IF NOT EXISTS tb_report (
    report_id       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '보고서ID',
    form_id         BIGINT          NOT NULL                 COMMENT '양식ID',
    ref_type        VARCHAR(20)     NOT NULL                 COMMENT '참조유형 (INCIDENT/SERVICE_REQUEST/CHANGE/INSPECTION)',
    ref_id          BIGINT          NOT NULL                 COMMENT '참조ID',
    report_content  JSON            NOT NULL                 COMMENT '보고서 내용 (작성 당시 JSON 스냅샷)',
    created_at      DATETIME        NOT NULL                 COMMENT '작성일시',
    created_by      BIGINT          NOT NULL                 COMMENT '작성자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (report_id),
    INDEX idx_report_form_id (form_id),
    INDEX idx_report_ref (ref_type, ref_id),
    CONSTRAINT fk_report_form FOREIGN KEY (form_id) REFERENCES tb_report_form (form_id),
    CONSTRAINT fk_report_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_report_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='보고서';

-- tb_incident_report (장애보고서 - 장애당 1개, 동적 폼 기반)
CREATE TABLE IF NOT EXISTS tb_incident_report (
    report_id       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '보고서ID',
    incident_id     BIGINT          NOT NULL                 COMMENT '장애ID (장애당 1개)',
    report_form_id  BIGINT          NOT NULL                 COMMENT '보고서양식ID (동적 폼)',
    report_content  JSON            NOT NULL                 COMMENT '보고서 내용 (양식 기반 JSON)',
    created_at      DATETIME        NOT NULL                 COMMENT '작성일시',
    created_by      BIGINT          NOT NULL                 COMMENT '작성자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (report_id),
    UNIQUE KEY uk_incident_report_incident_id (incident_id),
    CONSTRAINT fk_incident_report_incident FOREIGN KEY (incident_id) REFERENCES tb_incident (incident_id),
    CONSTRAINT fk_incident_report_form FOREIGN KEY (report_form_id) REFERENCES tb_report_form (form_id),
    CONSTRAINT fk_incident_report_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_incident_report_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애보고서';

-- ============================================================
-- 8. 게시판 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_board_config (
    board_id        BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '게시판ID',
    board_nm        VARCHAR(100)    NOT NULL                 COMMENT '게시판명',
    board_nm_en     VARCHAR(100)    NULL                     COMMENT '게시판명(영문)',
    board_type_cd   VARCHAR(20)     NOT NULL                 COMMENT '게시판유형 (NOTICE/FREE/ARCHIVE 등)',
    allow_ext       VARCHAR(200)    NULL                     COMMENT '허용 확장자 (콤마 구분, NULL이면 전체 허용)',
    max_file_size   INT             NULL                     COMMENT '파일 최대 용량 (MB)',
    allow_comment   CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '댓글 허용여부',
    role_permission JSON            NOT NULL                 COMMENT '역할별 읽기/쓰기 권한 JSON',
    is_active       CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '사용여부',
    sort_order      INT             NOT NULL DEFAULT 0       COMMENT '메뉴 노출 순서',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NOT NULL                 COMMENT '등록자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (board_id),
    CONSTRAINT fk_board_config_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_board_config_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시판 설정';

CREATE TABLE IF NOT EXISTS tb_board_post (
    post_id         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '게시글ID',
    board_id        BIGINT          NOT NULL                 COMMENT '게시판ID',
    title           VARCHAR(200)    NOT NULL                 COMMENT '제목',
    content         TEXT            NOT NULL                 COMMENT '내용',
    view_cnt        INT             NOT NULL DEFAULT 0       COMMENT '조회수',
    is_notice       CHAR(1)         NOT NULL DEFAULT 'N'     COMMENT '공지 고정 여부',
    created_at      DATETIME        NOT NULL                 COMMENT '작성일시',
    created_by      BIGINT          NOT NULL                 COMMENT '작성자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (post_id),
    INDEX idx_board_post_board_id (board_id),
    INDEX idx_board_post_created_at (created_at),
    CONSTRAINT fk_board_post_board FOREIGN KEY (board_id) REFERENCES tb_board_config (board_id),
    CONSTRAINT fk_board_post_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_board_post_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글';

CREATE TABLE IF NOT EXISTS tb_board_comment (
    comment_id      BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '댓글ID',
    post_id         BIGINT          NOT NULL                 COMMENT '게시글ID',
    content         TEXT            NOT NULL                 COMMENT '내용',
    created_at      DATETIME        NOT NULL                 COMMENT '작성일시',
    created_by      BIGINT          NOT NULL                 COMMENT '작성자ID',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (comment_id),
    INDEX idx_board_comment_post_id (post_id),
    CONSTRAINT fk_board_comment_post FOREIGN KEY (post_id) REFERENCES tb_board_post (post_id),
    CONSTRAINT fk_board_comment_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id),
    CONSTRAINT fk_board_comment_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 댓글';

CREATE TABLE IF NOT EXISTS tb_board_file (
    file_id         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '파일ID',
    post_id         BIGINT          NOT NULL                 COMMENT '게시글ID',
    original_nm     VARCHAR(255)    NOT NULL                 COMMENT '원본 파일명',
    saved_nm        VARCHAR(255)    NOT NULL                 COMMENT '저장 파일명 (UUID 기반)',
    file_path       VARCHAR(500)    NOT NULL                 COMMENT '저장 경로',
    file_size       BIGINT          NOT NULL                 COMMENT '파일 크기 (byte)',
    extension       VARCHAR(20)     NOT NULL                 COMMENT '확장자',
    created_at      DATETIME        NOT NULL                 COMMENT '업로드일시',
    created_by      BIGINT          NOT NULL                 COMMENT '업로드자ID',
    PRIMARY KEY (file_id),
    INDEX idx_board_file_post_id (post_id),
    CONSTRAINT fk_board_file_post FOREIGN KEY (post_id) REFERENCES tb_board_post (post_id),
    CONSTRAINT fk_board_file_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 첨부파일';

-- ============================================================
-- 9. 공통 / 설정 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_common_code (
    group_id        BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '코드그룹ID',
    group_nm        VARCHAR(100)    NOT NULL                 COMMENT '그룹명',
    group_nm_en     VARCHAR(100)    NULL                     COMMENT '그룹명(영문)',
    group_cd        VARCHAR(50)     NOT NULL                 COMMENT '그룹코드',
    description     VARCHAR(200)    NULL                     COMMENT '설명',
    is_active       CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '사용여부',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자ID',
    PRIMARY KEY (group_id),
    UNIQUE KEY uk_common_code_group_cd (group_cd),
    CONSTRAINT fk_common_code_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공통코드 그룹';

CREATE TABLE IF NOT EXISTS tb_common_code_detail (
    detail_id       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '상세ID',
    group_id        BIGINT          NOT NULL                 COMMENT '코드그룹ID',
    code_val        VARCHAR(50)     NOT NULL                 COMMENT '코드값',
    code_nm         VARCHAR(100)    NOT NULL                 COMMENT '코드명',
    code_nm_en      VARCHAR(100)    NULL                     COMMENT '코드명(영문)',
    sort_order      INT             NOT NULL DEFAULT 0       COMMENT '정렬순서',
    is_active       CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '사용여부',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자ID',
    PRIMARY KEY (detail_id),
    INDEX idx_common_code_detail_group_id (group_id),
    UNIQUE KEY uk_common_code_detail (group_id, code_val),
    CONSTRAINT fk_common_code_detail_group FOREIGN KEY (group_id) REFERENCES tb_common_code (group_id),
    CONSTRAINT fk_common_code_detail_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공통코드 상세';

CREATE TABLE IF NOT EXISTS tb_notification (
    noti_id         BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '알림ID',
    user_id         BIGINT          NOT NULL                 COMMENT '수신자ID',
    noti_type_cd    VARCHAR(50)     NOT NULL                 COMMENT '알림유형코드 (INCIDENT/SR/CHANGE/INSPECTION 등)',
    title           VARCHAR(200)    NOT NULL                 COMMENT '알림 제목',
    content         TEXT            NULL                     COMMENT '알림 내용',
    ref_type        VARCHAR(20)     NULL                     COMMENT '참조유형',
    ref_id          BIGINT          NULL                     COMMENT '참조ID',
    read_at         DATETIME        NULL                     COMMENT '읽음 처리 일시 (NULL=미읽음)',
    created_at      DATETIME        NOT NULL                 COMMENT '발송일시',
    PRIMARY KEY (noti_id),
    INDEX idx_notification_user_id (user_id),
    INDEX idx_notification_read_at (read_at),
    INDEX idx_notification_created_at (created_at),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림';

CREATE TABLE IF NOT EXISTS tb_notification_policy (
    policy_id       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '정책ID',
    noti_type_cd    VARCHAR(50)     NOT NULL                 COMMENT '알림유형코드',
    trigger_condition VARCHAR(200)  NOT NULL                 COMMENT '발송 조건 설명',
    target_role_cd  VARCHAR(50)     NOT NULL                 COMMENT '수신 대상 역할코드',
    is_active       CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '사용여부',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자ID',
    PRIMARY KEY (policy_id),
    CONSTRAINT fk_noti_policy_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림 발송 정책';

CREATE TABLE IF NOT EXISTS tb_sla_policy (
    policy_id       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '정책ID',
    company_id      BIGINT          NULL                     COMMENT '고객사ID (NULL이면 전체 기본값)',
    priority_cd     VARCHAR(20)     NOT NULL                 COMMENT '우선순위코드',
    deadline_hours  INT             NOT NULL                 COMMENT '처리 기한 (시간)',
    warning_pct     INT             NOT NULL DEFAULT 80      COMMENT '경고 기준 (% - 기한의 80% 초과 시 알림)',
    is_active       CHAR(1)         NOT NULL DEFAULT 'Y'     COMMENT '사용여부',
    created_at      DATETIME        NOT NULL                 COMMENT '등록일시',
    created_by      BIGINT          NULL                     COMMENT '등록자ID',
    PRIMARY KEY (policy_id),
    INDEX idx_sla_policy_company_id (company_id),
    CONSTRAINT fk_sla_policy_company FOREIGN KEY (company_id) REFERENCES tb_company (company_id),
    CONSTRAINT fk_sla_policy_created_by FOREIGN KEY (created_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SLA 기준값';

CREATE TABLE IF NOT EXISTS tb_system_config (
    config_id       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '설정ID',
    config_key      VARCHAR(100)    NOT NULL                 COMMENT '설정키',
    config_val      TEXT            NOT NULL                 COMMENT '설정값',
    description     VARCHAR(200)    NULL                     COMMENT '설명',
    updated_at      DATETIME        NULL                     COMMENT '수정일시',
    updated_by      BIGINT          NULL                     COMMENT '수정자ID',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_system_config_key (config_key),
    CONSTRAINT fk_system_config_updated_by FOREIGN KEY (updated_by) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='시스템 설정';

-- ============================================================
-- 10. 로그 영역
-- ============================================================

CREATE TABLE IF NOT EXISTS tb_audit_log (
    log_id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '로그ID',
    user_id         BIGINT          NULL                     COMMENT '행위자ID',
    action_type     VARCHAR(50)     NOT NULL                 COMMENT '행위유형 (CREATE/UPDATE/DELETE/STATUS_CHANGE 등)',
    target_type     VARCHAR(50)     NOT NULL                 COMMENT '대상유형 (INCIDENT/SR/CHANGE 등)',
    target_id       BIGINT          NULL                     COMMENT '대상ID',
    before_value    TEXT            NULL                     COMMENT '변경 전 값',
    after_value     TEXT            NULL                     COMMENT '변경 후 값',
    ip_address      VARCHAR(50)     NULL                     COMMENT '요청 IP',
    created_at      DATETIME        NOT NULL                 COMMENT '발생일시',
    PRIMARY KEY (log_id),
    INDEX idx_audit_log_user_id (user_id),
    INDEX idx_audit_log_target (target_type, target_id),
    INDEX idx_audit_log_created_at (created_at),
    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='감사 로그';

CREATE TABLE IF NOT EXISTS tb_access_log (
    log_id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '로그ID',
    user_id         BIGINT          NULL                     COMMENT '사용자ID (로그인 실패 시 NULL 가능)',
    login_id        VARCHAR(50)     NULL                     COMMENT '입력한 로그인ID',
    action_type     VARCHAR(20)     NOT NULL                 COMMENT '행위유형 (LOGIN/LOGOUT/LOGIN_FAIL)',
    ip_address      VARCHAR(50)     NOT NULL                 COMMENT '접속 IP',
    success_yn      CHAR(1)         NOT NULL                 COMMENT '성공여부',
    fail_reason     VARCHAR(200)    NULL                     COMMENT '실패 사유',
    created_at      DATETIME        NOT NULL                 COMMENT '발생일시',
    PRIMARY KEY (log_id),
    INDEX idx_access_log_user_id (user_id),
    INDEX idx_access_log_login_id (login_id),
    INDEX idx_access_log_created_at (created_at),
    CONSTRAINT fk_access_log_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='접속 로그';

CREATE TABLE IF NOT EXISTS tb_menu_access_log (
    log_id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '로그ID',
    user_id         BIGINT          NOT NULL                 COMMENT '사용자ID',
    menu_id         BIGINT          NOT NULL                 COMMENT '메뉴ID',
    role_cd         VARCHAR(50)     NULL                     COMMENT '접근 시점 역할코드',
    ip_address      VARCHAR(50)     NULL                     COMMENT '접속 IP',
    created_at      DATETIME        NOT NULL                 COMMENT '접근일시',
    PRIMARY KEY (log_id),
    INDEX idx_menu_access_log_user_id (user_id),
    INDEX idx_menu_access_log_menu_id (menu_id),
    INDEX idx_menu_access_log_created_at (created_at),
    CONSTRAINT fk_menu_access_log_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id),
    CONSTRAINT fk_menu_access_log_menu FOREIGN KEY (menu_id) REFERENCES tb_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴 접근 로그';
