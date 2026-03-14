-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: ITSM
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `tb_access_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_access_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '로그ID',
  `user_id` bigint DEFAULT NULL COMMENT '사용자ID (로그인 실패 시 NULL 가능)',
  `login_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '입력한 로그인ID',
  `action_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '행위유형 (LOGIN/LOGOUT/LOGIN_FAIL)',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '접속 IP',
  `success_yn` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '성공여부',
  `fail_reason` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '실패 사유',
  `created_at` datetime NOT NULL COMMENT '발생일시',
  PRIMARY KEY (`log_id`),
  KEY `idx_access_log_user_id` (`user_id`),
  KEY `idx_access_log_login_id` (`login_id`),
  KEY `idx_access_log_created_at` (`created_at`),
  CONSTRAINT `fk_access_log_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='접속 로그';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_asset_hw`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_asset_hw` (
  `asset_hw_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'HW자산ID',
  `asset_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '자산명',
  `asset_type_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '자산유형코드 (공통코드 - 서버/네트워크/PC 등)',
  `manufacturer` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '제조사',
  `model_nm` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '모델명',
  `serial_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '시리얼번호',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP주소',
  `mac_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'MAC주소',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '설치위치 (랙/층/건물 등)',
  `introduced_at` date DEFAULT NULL COMMENT '도입일',
  `warranty_end_at` date DEFAULT NULL COMMENT '유지보수 만료일',
  `company_id` bigint NOT NULL COMMENT '소속 고객사ID',
  `manager_id` bigint DEFAULT NULL COMMENT '담당자ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE/DISPOSED',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '비고',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자',
  `asset_category` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'INFRA_HW',
  `asset_sub_category` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`asset_hw_id`),
  UNIQUE KEY `uk_asset_hw_serial_no` (`serial_no`),
  KEY `idx_asset_hw_company_id` (`company_id`),
  KEY `idx_asset_hw_status` (`status`),
  KEY `idx_asset_hw_asset_type_cd` (`asset_type_cd`),
  KEY `fk_asset_hw_manager` (`manager_id`),
  KEY `fk_asset_hw_created_by` (`created_by`),
  KEY `fk_asset_hw_updated_by` (`updated_by`),
  CONSTRAINT `fk_asset_hw_company` FOREIGN KEY (`company_id`) REFERENCES `tb_company` (`company_id`),
  CONSTRAINT `fk_asset_hw_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_asset_hw_manager` FOREIGN KEY (`manager_id`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_asset_hw_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HW 자산';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_asset_hw_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_asset_hw_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력ID',
  `asset_hw_id` bigint NOT NULL COMMENT 'HW자산ID',
  `changed_field` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경된 항목명',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `batch_job_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '일괄변경 시 배치작업ID',
  `created_at` datetime NOT NULL COMMENT '이력 생성일시',
  `created_by` bigint DEFAULT NULL COMMENT '변경자ID',
  PRIMARY KEY (`history_id`),
  KEY `idx_asset_hw_history_hw_id` (`asset_hw_id`),
  KEY `fk_asset_hw_history_created_by` (`created_by`),
  CONSTRAINT `fk_asset_hw_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_asset_hw_history_hw` FOREIGN KEY (`asset_hw_id`) REFERENCES `tb_asset_hw` (`asset_hw_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HW 자산 변경 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_asset_relation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_asset_relation` (
  `asset_hw_id` bigint NOT NULL COMMENT 'HW자산ID',
  `asset_sw_id` bigint NOT NULL COMMENT 'SW자산ID',
  `installed_at` date DEFAULT NULL COMMENT '설치일',
  `removed_at` date DEFAULT NULL COMMENT '삭제일 (NULL=현재 설치 중)',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자',
  PRIMARY KEY (`asset_hw_id`,`asset_sw_id`),
  KEY `idx_asset_relation_sw_id` (`asset_sw_id`),
  KEY `fk_asset_relation_created_by` (`created_by`),
  CONSTRAINT `fk_asset_relation_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_asset_relation_hw` FOREIGN KEY (`asset_hw_id`) REFERENCES `tb_asset_hw` (`asset_hw_id`),
  CONSTRAINT `fk_asset_relation_sw` FOREIGN KEY (`asset_sw_id`) REFERENCES `tb_asset_sw` (`asset_sw_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HW-SW 연관관계';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_asset_sw`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_asset_sw` (
  `asset_sw_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'SW자산ID',
  `sw_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '소프트웨어명',
  `sw_type_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SW유형코드 (공통코드 - OS/WAS/DB/보안 등)',
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '버전',
  `license_key` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '라이선스키',
  `license_cnt` int DEFAULT NULL COMMENT '라이선스 수량',
  `installed_at` date DEFAULT NULL COMMENT '설치일',
  `expired_at` date DEFAULT NULL COMMENT '라이선스 만료일',
  `company_id` bigint NOT NULL COMMENT '소속 고객사ID',
  `manager_id` bigint DEFAULT NULL COMMENT '담당자ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE/DISPOSED',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '비고',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자',
  `asset_category` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'INFRA_SW',
  `asset_sub_category` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`asset_sw_id`),
  KEY `idx_asset_sw_company_id` (`company_id`),
  KEY `idx_asset_sw_status` (`status`),
  KEY `idx_asset_sw_sw_type_cd` (`sw_type_cd`),
  KEY `fk_asset_sw_manager` (`manager_id`),
  KEY `fk_asset_sw_created_by` (`created_by`),
  KEY `fk_asset_sw_updated_by` (`updated_by`),
  CONSTRAINT `fk_asset_sw_company` FOREIGN KEY (`company_id`) REFERENCES `tb_company` (`company_id`),
  CONSTRAINT `fk_asset_sw_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_asset_sw_manager` FOREIGN KEY (`manager_id`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_asset_sw_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SW 자산';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_asset_sw_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_asset_sw_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력ID',
  `asset_sw_id` bigint NOT NULL COMMENT 'SW자산ID',
  `changed_field` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경된 항목명',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `batch_job_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '일괄변경 시 배치작업ID',
  `created_at` datetime NOT NULL COMMENT '이력 생성일시',
  `created_by` bigint DEFAULT NULL COMMENT '변경자ID',
  PRIMARY KEY (`history_id`),
  KEY `idx_asset_sw_history_sw_id` (`asset_sw_id`),
  KEY `fk_asset_sw_history_created_by` (`created_by`),
  CONSTRAINT `fk_asset_sw_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_asset_sw_history_sw` FOREIGN KEY (`asset_sw_id`) REFERENCES `tb_asset_sw` (`asset_sw_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SW 자산 변경 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_audit_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_audit_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '로그ID',
  `user_id` bigint DEFAULT NULL COMMENT '행위자ID',
  `action_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '행위유형 (CREATE/UPDATE/DELETE/STATUS_CHANGE 등)',
  `target_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '대상유형 (INCIDENT/SR/CHANGE 등)',
  `target_id` bigint DEFAULT NULL COMMENT '대상ID',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '요청 IP',
  `created_at` datetime NOT NULL COMMENT '발생일시',
  PRIMARY KEY (`log_id`),
  KEY `idx_audit_log_user_id` (`user_id`),
  KEY `idx_audit_log_target` (`target_type`,`target_id`),
  KEY `idx_audit_log_created_at` (`created_at`),
  CONSTRAINT `fk_audit_log_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='감사 로그';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_batch_job`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_batch_job` (
  `batch_job_id` bigint NOT NULL AUTO_INCREMENT,
  `job_name` varchar(100) NOT NULL COMMENT '배치 작업명 (빈 이름과 동일)',
  `job_name_en` varchar(100) DEFAULT NULL COMMENT '배치작업명(영문)',
  `job_description` varchar(300) DEFAULT NULL COMMENT '배치 작업 설명',
  `cron_expression` varchar(50) NOT NULL COMMENT 'CRON 표현식',
  `is_active` char(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
  `last_executed_at` datetime DEFAULT NULL COMMENT '마지막 실행 시각',
  `last_result` varchar(20) DEFAULT NULL COMMENT '마지막 실행 결과 (SUCCESS/FAILURE)',
  `last_result_message` text COMMENT '마지막 실행 결과 메시지',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`batch_job_id`),
  UNIQUE KEY `job_name` (`job_name`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_board_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_board_comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '댓글ID',
  `post_id` bigint NOT NULL COMMENT '게시글ID',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '내용',
  `created_at` datetime NOT NULL COMMENT '작성일시',
  `created_by` bigint NOT NULL COMMENT '작성자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`comment_id`),
  KEY `idx_board_comment_post_id` (`post_id`),
  KEY `fk_board_comment_created_by` (`created_by`),
  KEY `fk_board_comment_updated_by` (`updated_by`),
  CONSTRAINT `fk_board_comment_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_board_comment_post` FOREIGN KEY (`post_id`) REFERENCES `tb_board_post` (`post_id`),
  CONSTRAINT `fk_board_comment_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 댓글';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_board_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_board_config` (
  `board_id` bigint NOT NULL AUTO_INCREMENT COMMENT '게시판ID',
  `board_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '게시판명',
  `board_nm_en` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '게시판명(영문)',
  `board_type_cd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '게시판유형 (NOTICE/FREE/ARCHIVE 등)',
  `allow_ext` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '허용 확장자 (콤마 구분, NULL이면 전체 허용)',
  `max_file_size` int DEFAULT NULL COMMENT '파일 최대 용량 (MB)',
  `allow_comment` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '댓글 허용여부',
  `role_permission` json NOT NULL COMMENT '역할별 읽기/쓰기 권한 JSON',
  `is_active` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '메뉴 노출 순서',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint NOT NULL COMMENT '등록자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`board_id`),
  KEY `fk_board_config_created_by` (`created_by`),
  KEY `fk_board_config_updated_by` (`updated_by`),
  CONSTRAINT `fk_board_config_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_board_config_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시판 설정';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_board_file`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_board_file` (
  `file_id` bigint NOT NULL AUTO_INCREMENT COMMENT '파일ID',
  `post_id` bigint NOT NULL COMMENT '게시글ID',
  `original_nm` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '원본 파일명',
  `saved_nm` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '저장 파일명 (UUID 기반)',
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '저장 경로',
  `file_size` bigint NOT NULL COMMENT '파일 크기 (byte)',
  `extension` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '확장자',
  `created_at` datetime NOT NULL COMMENT '업로드일시',
  `created_by` bigint NOT NULL COMMENT '업로드자ID',
  PRIMARY KEY (`file_id`),
  KEY `idx_board_file_post_id` (`post_id`),
  KEY `fk_board_file_created_by` (`created_by`),
  CONSTRAINT `fk_board_file_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_board_file_post` FOREIGN KEY (`post_id`) REFERENCES `tb_board_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 첨부파일';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_board_post`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_board_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '게시글ID',
  `board_id` bigint NOT NULL COMMENT '게시판ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '제목',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '내용',
  `view_cnt` int NOT NULL DEFAULT '0' COMMENT '조회수',
  `is_notice` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N' COMMENT '공지 고정 여부',
  `created_at` datetime NOT NULL COMMENT '작성일시',
  `created_by` bigint NOT NULL COMMENT '작성자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`post_id`),
  KEY `idx_board_post_board_id` (`board_id`),
  KEY `idx_board_post_created_at` (`created_at`),
  KEY `fk_board_post_created_by` (`created_by`),
  KEY `fk_board_post_updated_by` (`updated_by`),
  CONSTRAINT `fk_board_post_board` FOREIGN KEY (`board_id`) REFERENCES `tb_board_config` (`board_id`),
  CONSTRAINT `fk_board_post_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_board_post_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_change`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_change` (
  `change_id` bigint NOT NULL AUTO_INCREMENT COMMENT '변경ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '제목',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경 내용',
  `change_type_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경유형코드 (공통코드 - 긴급/일반/정기 등)',
  `priority_cd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '우선순위코드 (공통코드)',
  `status_cd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '상태 (DRAFT/APPROVAL_REQUESTED/APPROVED/IN_PROGRESS/COMPLETED/CLOSED/REJECTED)',
  `scheduled_at` datetime DEFAULT NULL COMMENT '변경 예정일시',
  `rollback_plan` text COLLATE utf8mb4_unicode_ci COMMENT '롤백 계획',
  `company_id` bigint NOT NULL COMMENT '고객사ID',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint NOT NULL COMMENT '등록자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`change_id`),
  KEY `idx_change_company_id` (`company_id`),
  KEY `idx_change_status_cd` (`status_cd`),
  KEY `idx_change_priority_cd` (`priority_cd`),
  KEY `fk_change_created_by` (`created_by`),
  KEY `fk_change_updated_by` (`updated_by`),
  CONSTRAINT `fk_change_company` FOREIGN KEY (`company_id`) REFERENCES `tb_company` (`company_id`),
  CONSTRAINT `fk_change_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_change_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경 요청';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_change_approver`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_change_approver` (
  `change_id` bigint NOT NULL COMMENT '변경ID',
  `user_id` bigint NOT NULL COMMENT '승인자ID',
  `approve_order` int NOT NULL COMMENT '승인 순서 (1부터 순차 승인)',
  `approve_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '승인상태 (PENDING/APPROVED/REJECTED)',
  `approved_at` datetime DEFAULT NULL COMMENT '승인/반려 일시',
  `comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '승인 의견',
  PRIMARY KEY (`change_id`,`user_id`),
  KEY `idx_change_approver_user_id` (`user_id`),
  CONSTRAINT `fk_change_approver_change` FOREIGN KEY (`change_id`) REFERENCES `tb_change` (`change_id`),
  CONSTRAINT `fk_change_approver_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경 승인자';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_change_asset`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_change_asset` (
  `change_id` bigint NOT NULL COMMENT '변경ID',
  `asset_type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '자산유형 (HW/SW)',
  `asset_id` bigint NOT NULL COMMENT '자산ID',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자ID',
  PRIMARY KEY (`change_id`,`asset_type`,`asset_id`),
  KEY `fk_change_asset_created_by` (`created_by`),
  CONSTRAINT `fk_change_asset_change` FOREIGN KEY (`change_id`) REFERENCES `tb_change` (`change_id`),
  CONSTRAINT `fk_change_asset_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경-자산 연관';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_change_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_change_comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '댓글ID',
  `change_id` bigint NOT NULL COMMENT '변경ID',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '댓글 내용',
  `created_at` datetime NOT NULL COMMENT '작성일시',
  `created_by` bigint NOT NULL COMMENT '작성자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`comment_id`),
  KEY `idx_change_comment_change_id` (`change_id`),
  KEY `fk_change_comment_created_by` (`created_by`),
  KEY `fk_change_comment_updated_by` (`updated_by`),
  CONSTRAINT `fk_change_comment_change` FOREIGN KEY (`change_id`) REFERENCES `tb_change` (`change_id`),
  CONSTRAINT `fk_change_comment_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_change_comment_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경 댓글';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_change_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_change_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력ID',
  `change_id` bigint NOT NULL COMMENT '변경ID',
  `changed_field` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경된 항목명',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `created_at` datetime NOT NULL COMMENT '변경일시',
  `created_by` bigint NOT NULL COMMENT '변경자ID',
  PRIMARY KEY (`history_id`),
  KEY `idx_change_history_change_id` (`change_id`),
  KEY `fk_change_history_created_by` (`created_by`),
  CONSTRAINT `fk_change_history_change` FOREIGN KEY (`change_id`) REFERENCES `tb_change` (`change_id`),
  CONSTRAINT `fk_change_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='변경 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_common_code`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_common_code` (
  `group_id` bigint NOT NULL AUTO_INCREMENT COMMENT '코드그룹ID',
  `group_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '그룹명',
  `group_nm_en` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `group_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '그룹코드',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '설명',
  `is_active` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자ID',
  PRIMARY KEY (`group_id`),
  UNIQUE KEY `uk_common_code_group_cd` (`group_cd`),
  KEY `fk_common_code_created_by` (`created_by`),
  CONSTRAINT `fk_common_code_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공통코드 그룹';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_common_code_detail`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_common_code_detail` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '상세ID',
  `group_id` bigint NOT NULL COMMENT '코드그룹ID',
  `code_val` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '코드값',
  `code_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '코드명',
  `code_nm_en` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '정렬순서',
  `is_active` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자ID',
  PRIMARY KEY (`detail_id`),
  UNIQUE KEY `uk_common_code_detail` (`group_id`,`code_val`),
  KEY `idx_common_code_detail_group_id` (`group_id`),
  KEY `fk_common_code_detail_created_by` (`created_by`),
  CONSTRAINT `fk_common_code_detail_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_common_code_detail_group` FOREIGN KEY (`group_id`) REFERENCES `tb_common_code` (`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공통코드 상세';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_company`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_company` (
  `company_id` bigint NOT NULL AUTO_INCREMENT COMMENT '회사ID',
  `company_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '회사명',
  `biz_no` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '사업자번호',
  `ceo_nm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '대표자명',
  `tel` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '연락처',
  `default_pm_id` bigint DEFAULT NULL COMMENT '대표 PM (담당자 공백 시 자동 배정)',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자',
  PRIMARY KEY (`company_id`),
  UNIQUE KEY `uk_company_biz_no` (`biz_no`),
  KEY `fk_company_default_pm` (`default_pm_id`),
  KEY `fk_company_created_by` (`created_by`),
  KEY `fk_company_updated_by` (`updated_by`),
  CONSTRAINT `fk_company_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_company_default_pm` FOREIGN KEY (`default_pm_id`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_company_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회사';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_company_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_company_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력ID',
  `company_id` bigint NOT NULL COMMENT '회사ID',
  `company_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경 전 회사명',
  `biz_no` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '변경 전 사업자번호',
  `ceo_nm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '변경 전 대표자명',
  `changed_field` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '변경된 항목명',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `created_at` datetime NOT NULL COMMENT '이력 생성일시',
  `created_by` bigint DEFAULT NULL COMMENT '변경자ID',
  PRIMARY KEY (`history_id`),
  KEY `idx_company_history_company_id` (`company_id`),
  KEY `fk_company_history_created_by` (`created_by`),
  CONSTRAINT `fk_company_history_company` FOREIGN KEY (`company_id`) REFERENCES `tb_company` (`company_id`),
  CONSTRAINT `fk_company_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회사 변경 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_daily_statistics`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_daily_statistics` (
  `stat_id` bigint NOT NULL AUTO_INCREMENT,
  `stat_date` date NOT NULL,
  `stat_type` varchar(50) NOT NULL,
  `stat_key` varchar(100) NOT NULL,
  `stat_value` decimal(15,2) NOT NULL DEFAULT '0.00',
  `stat_detail` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`stat_id`),
  UNIQUE KEY `idx_daily_stat_unique` (`stat_date`,`stat_type`,`stat_key`),
  KEY `idx_daily_stat_date` (`stat_date`),
  KEY `idx_daily_stat_type` (`stat_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_department`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_department` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '부서ID',
  `dept_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '부서명',
  `company_id` bigint NOT NULL COMMENT '회사ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자',
  PRIMARY KEY (`dept_id`),
  KEY `idx_department_company_id` (`company_id`),
  KEY `fk_department_created_by` (`created_by`),
  KEY `fk_department_updated_by` (`updated_by`),
  CONSTRAINT `fk_department_company` FOREIGN KEY (`company_id`) REFERENCES `tb_company` (`company_id`),
  CONSTRAINT `fk_department_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_department_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부서';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_department_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_department_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력ID',
  `dept_id` bigint NOT NULL COMMENT '부서ID',
  `dept_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경 전 부서명',
  `company_id` bigint DEFAULT NULL COMMENT '변경 전 소속 회사ID',
  `changed_field` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '변경된 항목명',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `created_at` datetime NOT NULL COMMENT '이력 생성일시',
  `created_by` bigint DEFAULT NULL COMMENT '변경자ID',
  PRIMARY KEY (`history_id`),
  KEY `idx_dept_history_dept_id` (`dept_id`),
  KEY `fk_dept_history_created_by` (`created_by`),
  CONSTRAINT `fk_dept_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_dept_history_dept` FOREIGN KEY (`dept_id`) REFERENCES `tb_department` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부서 변경 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_incident`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_incident` (
  `incident_id` bigint NOT NULL AUTO_INCREMENT COMMENT '장애ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '제목',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '장애 내용',
  `incident_type_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '장애유형코드 (공통코드)',
  `priority_cd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '우선순위코드 (CRITICAL/HIGH/MEDIUM/LOW)',
  `status_cd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RECEIVED' COMMENT '상태 (RECEIVED/IN_PROGRESS/COMPLETED/CLOSED/REJECTED)',
  `occurred_at` datetime NOT NULL COMMENT '장애 발생일시',
  `completed_at` datetime DEFAULT NULL COMMENT '처리 완료일시',
  `closed_at` datetime DEFAULT NULL COMMENT '최종 종료일시',
  `sla_deadline_at` datetime DEFAULT NULL COMMENT 'SLA 처리 기한',
  `company_id` bigint NOT NULL COMMENT '고객사ID',
  `main_manager_id` bigint DEFAULT NULL COMMENT '주담당자ID',
  `process_content` text COLLATE utf8mb4_unicode_ci COMMENT '처리내용 (주담당자 작성)',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint NOT NULL COMMENT '등록자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`incident_id`),
  KEY `idx_incident_company_id` (`company_id`),
  KEY `idx_incident_status_cd` (`status_cd`),
  KEY `idx_incident_priority_cd` (`priority_cd`),
  KEY `idx_incident_occurred_at` (`occurred_at`),
  KEY `idx_incident_main_manager_id` (`main_manager_id`),
  KEY `fk_incident_created_by` (`created_by`),
  KEY `fk_incident_updated_by` (`updated_by`),
  CONSTRAINT `fk_incident_company` FOREIGN KEY (`company_id`) REFERENCES `tb_company` (`company_id`),
  CONSTRAINT `fk_incident_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_incident_main_manager` FOREIGN KEY (`main_manager_id`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_incident_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애 티켓';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_incident_asset`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_incident_asset` (
  `incident_id` bigint NOT NULL COMMENT '장애ID',
  `asset_type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '자산유형 (HW/SW)',
  `asset_id` bigint NOT NULL COMMENT '자산ID',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자ID',
  PRIMARY KEY (`incident_id`,`asset_type`,`asset_id`),
  KEY `fk_incident_asset_created_by` (`created_by`),
  CONSTRAINT `fk_incident_asset_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_incident_asset_incident` FOREIGN KEY (`incident_id`) REFERENCES `tb_incident` (`incident_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애-자산 연관';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_incident_assignee`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_incident_assignee` (
  `incident_id` bigint NOT NULL COMMENT '장애ID',
  `user_id` bigint NOT NULL COMMENT '담당자ID',
  `granted_at` datetime NOT NULL COMMENT '지정일시',
  `granted_by` bigint DEFAULT NULL COMMENT '지정자ID',
  PRIMARY KEY (`incident_id`,`user_id`),
  KEY `idx_incident_assignee_user_id` (`user_id`),
  KEY `fk_incident_assignee_granted_by` (`granted_by`),
  CONSTRAINT `fk_incident_assignee_granted_by` FOREIGN KEY (`granted_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_incident_assignee_incident` FOREIGN KEY (`incident_id`) REFERENCES `tb_incident` (`incident_id`),
  CONSTRAINT `fk_incident_assignee_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애 담당자';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_incident_comment`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_incident_comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '댓글ID',
  `incident_id` bigint NOT NULL COMMENT '장애ID',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '댓글 내용',
  `created_at` datetime NOT NULL COMMENT '작성일시',
  `created_by` bigint NOT NULL COMMENT '작성자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`comment_id`),
  KEY `idx_incident_comment_incident_id` (`incident_id`),
  KEY `fk_incident_comment_created_by` (`created_by`),
  KEY `fk_incident_comment_updated_by` (`updated_by`),
  CONSTRAINT `fk_incident_comment_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_incident_comment_incident` FOREIGN KEY (`incident_id`) REFERENCES `tb_incident` (`incident_id`),
  CONSTRAINT `fk_incident_comment_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애 댓글';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_incident_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_incident_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력ID',
  `incident_id` bigint NOT NULL COMMENT '장애ID',
  `changed_field` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경된 항목명',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `created_at` datetime NOT NULL COMMENT '변경일시',
  `created_by` bigint NOT NULL COMMENT '변경자ID',
  PRIMARY KEY (`history_id`),
  KEY `idx_incident_history_incident_id` (`incident_id`),
  KEY `fk_incident_history_created_by` (`created_by`),
  CONSTRAINT `fk_incident_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_incident_history_incident` FOREIGN KEY (`incident_id`) REFERENCES `tb_incident` (`incident_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애 변경 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_incident_report`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_incident_report` (
  `report_id` bigint NOT NULL AUTO_INCREMENT COMMENT '보고서ID',
  `incident_id` bigint NOT NULL COMMENT '장애ID (장애당 1개)',
  `report_form_id` bigint NOT NULL COMMENT '보고서양식ID (동적 폼)',
  `report_content` json NOT NULL COMMENT '보고서 내용 (양식 기반 JSON)',
  `created_at` datetime NOT NULL COMMENT '작성일시',
  `created_by` bigint NOT NULL COMMENT '작성자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `uk_incident_report_incident_id` (`incident_id`),
  KEY `fk_incident_report_form` (`report_form_id`),
  KEY `fk_incident_report_created_by` (`created_by`),
  KEY `fk_incident_report_updated_by` (`updated_by`),
  CONSTRAINT `fk_incident_report_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_incident_report_form` FOREIGN KEY (`report_form_id`) REFERENCES `tb_report_form` (`form_id`),
  CONSTRAINT `fk_incident_report_incident` FOREIGN KEY (`incident_id`) REFERENCES `tb_incident` (`incident_id`),
  CONSTRAINT `fk_incident_report_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장애보고서';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_inspection`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_inspection` (
  `inspection_id` bigint NOT NULL AUTO_INCREMENT COMMENT '점검ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '점검 제목',
  `inspection_type_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '점검유형코드 (공통코드 - 월간/분기/연간 등)',
  `status_cd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SCHEDULED' COMMENT '상태 (SCHEDULED/IN_PROGRESS/COMPLETED/CLOSED/ON_HOLD)',
  `scheduled_at` date NOT NULL COMMENT '점검 예정일',
  `completed_at` datetime DEFAULT NULL COMMENT '점검 완료일시',
  `closed_at` datetime DEFAULT NULL COMMENT '종료일시',
  `company_id` bigint NOT NULL COMMENT '고객사ID',
  `manager_id` bigint DEFAULT NULL COMMENT '담당자ID',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '점검 설명',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint NOT NULL COMMENT '등록자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`inspection_id`),
  KEY `idx_inspection_company_id` (`company_id`),
  KEY `idx_inspection_status_cd` (`status_cd`),
  KEY `idx_inspection_scheduled_at` (`scheduled_at`),
  KEY `fk_inspection_manager` (`manager_id`),
  KEY `fk_inspection_created_by` (`created_by`),
  KEY `fk_inspection_updated_by` (`updated_by`),
  CONSTRAINT `fk_inspection_company` FOREIGN KEY (`company_id`) REFERENCES `tb_company` (`company_id`),
  CONSTRAINT `fk_inspection_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_inspection_manager` FOREIGN KEY (`manager_id`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_inspection_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='정기점검';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_inspection_asset`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_inspection_asset` (
  `inspection_id` bigint NOT NULL COMMENT '점검ID',
  `asset_type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '자산유형 (HW/SW)',
  `asset_id` bigint NOT NULL COMMENT '자산ID',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자ID',
  PRIMARY KEY (`inspection_id`,`asset_type`,`asset_id`),
  KEY `fk_inspection_asset_created_by` (`created_by`),
  CONSTRAINT `fk_inspection_asset_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_inspection_asset_inspection` FOREIGN KEY (`inspection_id`) REFERENCES `tb_inspection` (`inspection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='점검-자산 연관';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_inspection_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_inspection_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력ID',
  `inspection_id` bigint NOT NULL COMMENT '점검ID',
  `changed_field` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경된 항목명',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `created_at` datetime NOT NULL COMMENT '변경일시',
  `created_by` bigint NOT NULL COMMENT '변경자ID',
  PRIMARY KEY (`history_id`),
  KEY `idx_inspection_history_inspection_id` (`inspection_id`),
  KEY `fk_inspection_history_created_by` (`created_by`),
  CONSTRAINT `fk_inspection_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_inspection_history_inspection` FOREIGN KEY (`inspection_id`) REFERENCES `tb_inspection` (`inspection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='점검 변경 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_inspection_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_inspection_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '항목ID',
  `inspection_id` bigint NOT NULL COMMENT '점검ID',
  `item_nm` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '항목명',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '항목 순서',
  `is_required` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '필수 여부',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint NOT NULL COMMENT '등록자ID',
  PRIMARY KEY (`item_id`),
  KEY `idx_inspection_item_inspection_id` (`inspection_id`),
  KEY `fk_inspection_item_created_by` (`created_by`),
  CONSTRAINT `fk_inspection_item_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_inspection_item_inspection` FOREIGN KEY (`inspection_id`) REFERENCES `tb_inspection` (`inspection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='점검 체크리스트 항목';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_inspection_result`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_inspection_result` (
  `result_id` bigint NOT NULL AUTO_INCREMENT COMMENT '결과ID',
  `inspection_id` bigint NOT NULL COMMENT '점검ID',
  `item_id` bigint NOT NULL COMMENT '항목ID',
  `result_value` text COLLATE utf8mb4_unicode_ci COMMENT '결과값',
  `is_normal` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '정상여부 (Y/N)',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '비고',
  `created_at` datetime NOT NULL COMMENT '작성일시',
  `created_by` bigint NOT NULL COMMENT '작성자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`result_id`),
  KEY `idx_inspection_result_inspection_id` (`inspection_id`),
  KEY `idx_inspection_result_item_id` (`item_id`),
  KEY `fk_inspection_result_created_by` (`created_by`),
  KEY `fk_inspection_result_updated_by` (`updated_by`),
  CONSTRAINT `fk_inspection_result_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_inspection_result_inspection` FOREIGN KEY (`inspection_id`) REFERENCES `tb_inspection` (`inspection_id`),
  CONSTRAINT `fk_inspection_result_item` FOREIGN KEY (`item_id`) REFERENCES `tb_inspection_item` (`item_id`),
  CONSTRAINT `fk_inspection_result_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='점검 결과';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_login_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_login_history` (
  `login_history_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `login_at` datetime NOT NULL,
  `logout_at` datetime DEFAULT NULL,
  `ip_address` varchar(50) DEFAULT NULL,
  `user_agent` varchar(300) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`login_history_id`),
  KEY `idx_login_history_user` (`user_id`),
  KEY `idx_login_history_login_at` (`login_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '메뉴ID',
  `parent_menu_id` bigint DEFAULT NULL COMMENT '상위메뉴ID (NULL=최상위)',
  `menu_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '메뉴명',
  `menu_nm_en` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '메뉴명(영문)',
  `menu_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'URL',
  `icon` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '아이콘',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '정렬순서',
  `is_visible` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '노출여부',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자',
  PRIMARY KEY (`menu_id`),
  KEY `idx_menu_parent_id` (`parent_menu_id`),
  KEY `fk_menu_created_by` (`created_by`),
  KEY `fk_menu_updated_by` (`updated_by`),
  CONSTRAINT `fk_menu_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_menu_parent` FOREIGN KEY (`parent_menu_id`) REFERENCES `tb_menu` (`menu_id`),
  CONSTRAINT `fk_menu_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_menu_access_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_menu_access_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '로그ID',
  `user_id` bigint NOT NULL COMMENT '사용자ID',
  `menu_id` bigint NOT NULL COMMENT '메뉴ID',
  `role_cd` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '접근 시점 역할코드',
  `ip_address` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '접속 IP',
  `created_at` datetime NOT NULL COMMENT '접근일시',
  PRIMARY KEY (`log_id`),
  KEY `idx_menu_access_log_user_id` (`user_id`),
  KEY `idx_menu_access_log_menu_id` (`menu_id`),
  KEY `idx_menu_access_log_created_at` (`created_at`),
  CONSTRAINT `fk_menu_access_log_menu` FOREIGN KEY (`menu_id`) REFERENCES `tb_menu` (`menu_id`),
  CONSTRAINT `fk_menu_access_log_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴 접근 로그';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_notification`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_notification` (
  `noti_id` bigint NOT NULL AUTO_INCREMENT COMMENT '알림ID',
  `user_id` bigint NOT NULL COMMENT '수신자ID',
  `noti_type_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '알림유형코드 (INCIDENT/SR/CHANGE/INSPECTION 등)',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '알림 제목',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '알림 내용',
  `ref_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '참조유형',
  `ref_id` bigint DEFAULT NULL COMMENT '참조ID',
  `read_at` datetime DEFAULT NULL COMMENT '읽음 처리 일시 (NULL=미읽음)',
  `created_at` datetime NOT NULL COMMENT '발송일시',
  PRIMARY KEY (`noti_id`),
  KEY `idx_notification_user_id` (`user_id`),
  KEY `idx_notification_read_at` (`read_at`),
  KEY `idx_notification_created_at` (`created_at`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_notification_policy`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_notification_policy` (
  `policy_id` bigint NOT NULL AUTO_INCREMENT COMMENT '정책ID',
  `noti_type_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '알림유형코드',
  `trigger_condition` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '발송 조건 설명',
  `target_role_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '수신 대상 역할코드',
  `is_active` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자ID',
  PRIMARY KEY (`policy_id`),
  KEY `fk_noti_policy_created_by` (`created_by`),
  CONSTRAINT `fk_noti_policy_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림 발송 정책';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_report`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_report` (
  `report_id` bigint NOT NULL AUTO_INCREMENT COMMENT '보고서ID',
  `form_id` bigint NOT NULL COMMENT '양식ID',
  `ref_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '참조유형 (INCIDENT/SERVICE_REQUEST/CHANGE/INSPECTION)',
  `ref_id` bigint NOT NULL COMMENT '참조ID',
  `report_content` json NOT NULL COMMENT '보고서 내용 (작성 당시 JSON 스냅샷)',
  `created_at` datetime NOT NULL COMMENT '작성일시',
  `created_by` bigint NOT NULL COMMENT '작성자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`report_id`),
  KEY `idx_report_form_id` (`form_id`),
  KEY `idx_report_ref` (`ref_type`,`ref_id`),
  KEY `fk_report_created_by` (`created_by`),
  KEY `fk_report_updated_by` (`updated_by`),
  CONSTRAINT `fk_report_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_report_form` FOREIGN KEY (`form_id`) REFERENCES `tb_report_form` (`form_id`),
  CONSTRAINT `fk_report_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='보고서';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_report_form`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_report_form` (
  `form_id` bigint NOT NULL AUTO_INCREMENT COMMENT '양식ID',
  `form_nm` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '양식명',
  `form_type_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '양식유형코드 (공통코드 - 장애/서비스요청/변경/점검 등)',
  `form_schema` json NOT NULL COMMENT '양식 구조 JSON (동적 폼 정의)',
  `is_active` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint NOT NULL COMMENT '등록자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`form_id`),
  KEY `idx_report_form_type_cd` (`form_type_cd`),
  KEY `fk_report_form_created_by` (`created_by`),
  KEY `fk_report_form_updated_by` (`updated_by`),
  CONSTRAINT `fk_report_form_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_report_form_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='보고서 양식';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '역할ID',
  `role_nm` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '역할명',
  `role_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '역할코드 (ex. ROLE_ADMIN)',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '설명',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '상태',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_cd` (`role_cd`),
  KEY `fk_role_created_by` (`created_by`),
  CONSTRAINT `fk_role_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역할';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_role_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_role_menu` (
  `role_id` bigint NOT NULL COMMENT '역할ID',
  `menu_id` bigint NOT NULL COMMENT '메뉴ID',
  `can_read` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '읽기 권한',
  `can_write` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N' COMMENT '쓰기 권한',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자',
  PRIMARY KEY (`role_id`,`menu_id`),
  KEY `idx_role_menu_menu_id` (`menu_id`),
  KEY `fk_role_menu_created_by` (`created_by`),
  CONSTRAINT `fk_role_menu_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_role_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `tb_menu` (`menu_id`),
  CONSTRAINT `fk_role_menu_role` FOREIGN KEY (`role_id`) REFERENCES `tb_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역할-메뉴 매핑';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_service_request`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_service_request` (
  `request_id` bigint NOT NULL AUTO_INCREMENT COMMENT '요청ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '제목',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '요청 내용',
  `request_type_cd` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '요청유형코드 (공통코드)',
  `priority_cd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '우선순위코드 (공통코드)',
  `status_cd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RECEIVED' COMMENT '상태 (RECEIVED/ASSIGNED/IN_PROGRESS/PENDING_COMPLETE/CLOSED/CANCELLED/REJECTED)',
  `occurred_at` datetime NOT NULL COMMENT '요청일시',
  `completed_at` datetime DEFAULT NULL COMMENT '완료일시',
  `closed_at` datetime DEFAULT NULL COMMENT '종료일시',
  `sla_deadline_at` datetime DEFAULT NULL COMMENT 'SLA 처리 기한',
  `reject_cnt` int NOT NULL DEFAULT '0' COMMENT '반려 횟수',
  `company_id` bigint NOT NULL COMMENT '고객사ID',
  `satisfaction_score` tinyint DEFAULT NULL COMMENT '만족도 점수 (1~5)',
  `satisfaction_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '만족도 의견',
  `satisfaction_submitted_at` datetime DEFAULT NULL COMMENT '만족도 제출일시',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint NOT NULL COMMENT '등록자ID',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`request_id`),
  KEY `idx_sr_company_id` (`company_id`),
  KEY `idx_sr_status_cd` (`status_cd`),
  KEY `idx_sr_priority_cd` (`priority_cd`),
  KEY `idx_sr_occurred_at` (`occurred_at`),
  KEY `fk_sr_created_by` (`created_by`),
  KEY `fk_sr_updated_by` (`updated_by`),
  CONSTRAINT `fk_sr_company` FOREIGN KEY (`company_id`) REFERENCES `tb_company` (`company_id`),
  CONSTRAINT `fk_sr_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_sr_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스 요청';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_service_request_asset`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_service_request_asset` (
  `request_id` bigint NOT NULL COMMENT '요청ID',
  `asset_type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '자산유형 (HW/SW)',
  `asset_id` bigint NOT NULL COMMENT '자산ID',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자ID',
  PRIMARY KEY (`request_id`,`asset_type`,`asset_id`),
  KEY `fk_sr_asset_created_by` (`created_by`),
  CONSTRAINT `fk_sr_asset_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_sr_asset_request` FOREIGN KEY (`request_id`) REFERENCES `tb_service_request` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스요청-자산 연관';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_service_request_assignee`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_service_request_assignee` (
  `request_id` bigint NOT NULL COMMENT '요청ID',
  `user_id` bigint NOT NULL COMMENT '담당자ID',
  `process_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '처리상태 (PENDING/IN_PROGRESS/COMPLETED)',
  `granted_at` datetime NOT NULL COMMENT '지정일시',
  `granted_by` bigint DEFAULT NULL COMMENT '지정자ID',
  PRIMARY KEY (`request_id`,`user_id`),
  KEY `idx_sr_assignee_user_id` (`user_id`),
  KEY `fk_sr_assignee_granted_by` (`granted_by`),
  CONSTRAINT `fk_sr_assignee_granted_by` FOREIGN KEY (`granted_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_sr_assignee_request` FOREIGN KEY (`request_id`) REFERENCES `tb_service_request` (`request_id`),
  CONSTRAINT `fk_sr_assignee_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스요청 담당자';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_service_request_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_service_request_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력ID',
  `request_id` bigint NOT NULL COMMENT '요청ID',
  `changed_field` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경된 항목명',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `created_at` datetime NOT NULL COMMENT '변경일시',
  `created_by` bigint NOT NULL COMMENT '변경자ID',
  PRIMARY KEY (`history_id`),
  KEY `idx_sr_history_request_id` (`request_id`),
  KEY `fk_sr_history_created_by` (`created_by`),
  CONSTRAINT `fk_sr_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_sr_history_request` FOREIGN KEY (`request_id`) REFERENCES `tb_service_request` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스요청 변경 이력';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_service_request_process`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_service_request_process` (
  `process_id` bigint NOT NULL AUTO_INCREMENT COMMENT '처리ID',
  `request_id` bigint NOT NULL COMMENT '요청ID',
  `user_id` bigint NOT NULL COMMENT '담당자ID',
  `process_content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '처리내용',
  `is_completed` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'N' COMMENT '완료여부',
  `completed_at` datetime DEFAULT NULL COMMENT '완료일시',
  `created_at` datetime NOT NULL COMMENT '작성일시',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  PRIMARY KEY (`process_id`),
  KEY `idx_sr_process_request_id` (`request_id`),
  KEY `idx_sr_process_user_id` (`user_id`),
  CONSTRAINT `fk_sr_process_request` FOREIGN KEY (`request_id`) REFERENCES `tb_service_request` (`request_id`),
  CONSTRAINT `fk_sr_process_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스요청 처리내역';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_sim_menu_access_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sim_menu_access_log` (
  `access_log_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `menu_path` varchar(200) NOT NULL,
  `menu_nm` varchar(100) DEFAULT NULL,
  `accessed_at` datetime NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`access_log_id`),
  KEY `idx_sim_menu_access_user` (`user_id`),
  KEY `idx_sim_menu_access_at` (`accessed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_sla_policy`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sla_policy` (
  `policy_id` bigint NOT NULL AUTO_INCREMENT COMMENT '정책ID',
  `company_id` bigint DEFAULT NULL COMMENT '고객사ID (NULL이면 전체 기본값)',
  `priority_cd` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '우선순위코드',
  `deadline_hours` int NOT NULL COMMENT '처리 기한 (시간)',
  `warning_pct` int NOT NULL DEFAULT '80' COMMENT '경고 기준 (% - 기한의 80% 초과 시 알림)',
  `is_active` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Y' COMMENT '사용여부',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자ID',
  PRIMARY KEY (`policy_id`),
  KEY `idx_sla_policy_company_id` (`company_id`),
  KEY `fk_sla_policy_created_by` (`created_by`),
  CONSTRAINT `fk_sla_policy_company` FOREIGN KEY (`company_id`) REFERENCES `tb_company` (`company_id`),
  CONSTRAINT `fk_sla_policy_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SLA 기준값';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_system_config`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_system_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '설정ID',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '설정키',
  `config_val` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '설정값',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '설명',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자ID',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_system_config_key` (`config_key`),
  KEY `fk_system_config_updated_by` (`updated_by`),
  CONSTRAINT `fk_system_config_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='시스템 설정';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '사용자ID (절대 재사용 안 됨)',
  `login_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '로그인ID (DELETED 시 마스킹)',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '비밀번호 (BCrypt 암호화)',
  `user_nm` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '이름',
  `employee_no` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '사번 (재사용 가능)',
  `dept_id` bigint DEFAULT NULL COMMENT '부서ID',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '이메일',
  `tel` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '연락처',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE/RESIGNED/ABSENT/DELETED',
  `valid_from` datetime NOT NULL COMMENT '유효 시작일시',
  `valid_to` datetime DEFAULT NULL COMMENT '유효 종료일시 (NULL=현재 유효)',
  `last_login_at` datetime DEFAULT NULL COMMENT '마지막 로그인 일시',
  `pwd_changed_at` datetime DEFAULT NULL COMMENT '비밀번호 변경일시',
  `login_fail_cnt` int NOT NULL DEFAULT '0' COMMENT '로그인 실패 횟수',
  `created_at` datetime NOT NULL COMMENT '등록일시',
  `created_by` bigint DEFAULT NULL COMMENT '등록자',
  `updated_at` datetime DEFAULT NULL COMMENT '수정일시',
  `updated_by` bigint DEFAULT NULL COMMENT '수정자',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_login_id` (`login_id`),
  KEY `idx_user_dept_id` (`dept_id`),
  KEY `idx_user_status` (`status`),
  KEY `fk_user_created_by` (`created_by`),
  KEY `fk_user_updated_by` (`updated_by`),
  CONSTRAINT `fk_user_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_user_department` FOREIGN KEY (`dept_id`) REFERENCES `tb_department` (`dept_id`),
  CONSTRAINT `fk_user_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력ID',
  `user_id` bigint NOT NULL COMMENT '사용자ID',
  `login_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경 시점 로그인ID',
  `user_nm` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경 시점 이름',
  `employee_no` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '변경 시점 사번',
  `dept_id` bigint DEFAULT NULL COMMENT '변경 시점 부서ID',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '변경 시점 이메일',
  `tel` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '변경 시점 연락처',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '변경 시점 상태',
  `changed_field` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '변경된 항목명',
  `before_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 전 값',
  `after_value` text COLLATE utf8mb4_unicode_ci COMMENT '변경 후 값',
  `valid_from` datetime NOT NULL COMMENT '유효 시작일시',
  `valid_to` datetime DEFAULT NULL COMMENT '유효 종료일시',
  `batch_job_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '일괄변경 시 배치작업ID',
  `created_at` datetime NOT NULL COMMENT '이력 생성일시',
  `created_by` bigint DEFAULT NULL COMMENT '변경자ID',
  PRIMARY KEY (`history_id`),
  KEY `idx_user_history_user_id` (`user_id`),
  KEY `idx_user_history_valid_from` (`valid_from`),
  KEY `idx_user_history_batch_job_id` (`batch_job_id`),
  KEY `fk_user_history_created_by` (`created_by`),
  CONSTRAINT `fk_user_history_created_by` FOREIGN KEY (`created_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_user_history_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 변경 이력 (Temporal Data Modeling)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tb_user_role`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_role` (
  `user_id` bigint NOT NULL COMMENT '사용자ID',
  `role_id` bigint NOT NULL COMMENT '역할ID',
  `granted_at` datetime NOT NULL COMMENT '부여일시',
  `granted_by` bigint DEFAULT NULL COMMENT '부여자ID',
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `idx_user_role_role_id` (`role_id`),
  KEY `fk_user_role_granted_by` (`granted_by`),
  CONSTRAINT `fk_user_role_granted_by` FOREIGN KEY (`granted_by`) REFERENCES `tb_user` (`user_id`),
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `tb_role` (`role_id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자-역할 매핑';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'ITSM'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-14 12:01:15
