-- 로컬 개발용 데이터베이스(스키마) 생성
-- Database: medi_com_database
CREATE DATABASE medi_com_database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- 공통 코드 그룹
CREATE TABLE t_common_group_code (
                                     com_code_group_cd VARCHAR(50) PRIMARY KEY,
                                     com_code_group_name VARCHAR(100) NOT NULL,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     creator_id BIGINT NOT NULL,
                                     modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     modifier_id BIGINT NOT NULL
);


-- 공통 코드
CREATE TABLE t_common_code (
                               com_code_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               com_code_group_cd VARCHAR(50) NOT NULL,
                               com_code_cd VARCHAR(50) NOT NULL,
                               com_code_name VARCHAR(50) NOT NULL,
                               sort_order INT NOT NULL,
                               usage_yn TINYINT(1) NOT NULL DEFAULT 1,      -- 사용하면 1, 안 하면 0 디폴트
                               attribute1 VARCHAR(100) NULL,
                               attribute2 VARCHAR(100) NULL,
                               attribute3 VARCHAR(100) NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               creator_id BIGINT NOT NULL,
                               modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               modifier_id BIGINT NOT NULL,
                               CONSTRAINT fk_common_code_group FOREIGN KEY (com_code_group_cd) REFERENCES t_common_group_code(com_code_group_cd)
);

-- 사용자
CREATE TABLE t_user (
                        user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_login_id VARCHAR(30) NOT NULL UNIQUE,          -- 로그인용 ID 추가
                        user_name VARCHAR(30) NOT NULL,
                        password VARCHAR(100) NOT NULL,
                        phone VARCHAR(15) NOT NULL,
                        role_cd BIGINT NOT NULL COMMENT '공통코드 참조 - ROLE_USER / ROLE_ADMIN',
                        consent_cd BIGINT NOT NULL COMMENT '공통코드 참조',
                        status_cd BIGINT NOT NULL COMMENT '공통코드 참조',
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        creator_id BIGINT NOT NULL,
                        modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        modifier_id BIGINT NOT NULL,
                        refresh_token VARCHAR(255) NULL,
                        CONSTRAINT fk_user_consent FOREIGN KEY (consent_cd) REFERENCES t_common_code(com_code_id),
                        CONSTRAINT fk_user_status FOREIGN KEY (status_cd) REFERENCES t_common_code(com_code_id)
);

-- 약 정보
CREATE TABLE t_medicine (
                            medicine_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            medicine_name VARCHAR(30) NOT NULL,
                            description TEXT NULL,
                            medicine_benefit TEXT NULL,
                            medicine_component VARCHAR(50) NULL,
                            drug_interaction TEXT NULL,
                            medicine_amount INT NULL,
                            image_url VARCHAR(255) NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            creator_id BIGINT NOT NULL,
                            modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            modifier_id BIGINT NOT NULL
);

-- 사용자별 약 정보
CREATE TABLE t_user_medicine (
                                 user_medicine_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 dosage VARCHAR(30) NULL,
                                 meal_cd BIGINT NULL COMMENT '공통코드 참조 - before_meal/after_meal',
                                 time_cd BIGINT NULL COMMENT '공통코드 참조 - morning/lunch/dinner',
                                 days_of_week_cd BIGINT NULL COMMENT '공통코드 참조 - mon/wed/fri 등',
                                 is_alarm TINYINT(1) NULL DEFAULT 0,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 creator_id BIGINT NOT NULL,
                                 modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 modifier_id BIGINT NOT NULL,
                                 user_id BIGINT NOT NULL,
                                 medicine_id BIGINT NOT NULL,
                                 CONSTRAINT fk_user_medicine_user FOREIGN KEY (user_id) REFERENCES t_user(user_id),
                                 CONSTRAINT fk_user_medicine_medicine FOREIGN KEY (medicine_id) REFERENCES t_medicine(medicine_id),
                                 CONSTRAINT fk_user_medicine_meal FOREIGN KEY (meal_cd) REFERENCES t_common_code(com_code_id),
                                 CONSTRAINT fk_user_medicine_time FOREIGN KEY (time_cd) REFERENCES t_common_code(com_code_id),
                                 CONSTRAINT fk_user_medicine_days FOREIGN KEY (days_of_week_cd) REFERENCES t_common_code(com_code_id)
);

-- 약 복용 기록
CREATE TABLE t_medicine_intake_log (
                                       intake_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       intake_date DATE NOT NULL,
                                       intake_time TIME NOT NULL,
                                       is_taken TINYINT(1) DEFAULT 0 COMMENT '0: 안 먹음, 1: 먹음',
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       creator_id BIGINT NOT NULL,
                                       modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       modifier_id BIGINT NOT NULL,
                                       user_medicine_id BIGINT NOT NULL,
                                       CONSTRAINT fk_intake_user_medicine FOREIGN KEY (user_medicine_id) REFERENCES t_user_medicine(user_medicine_id)
);

-- 사용자 상태 기록
CREATE TABLE t_status (
                          status_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          default_status_cd BIGINT NOT NULL COMMENT '공통코드 참조 - 좋음/보통/나쁨',
                          drink_cd BIGINT NOT NULL COMMENT '공통코드 참조 - 안마심/소주/맥주',
                          condition_cd BIGINT NOT NULL COMMENT '공통코드 참조 - 피곤/상쾌',
                          status_memo TEXT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          creator_id BIGINT NOT NULL,
                          modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          modifier_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL,
                          CONSTRAINT fk_status_user FOREIGN KEY (user_id) REFERENCES t_user(user_id),
                          CONSTRAINT fk_status_default FOREIGN KEY (default_status_cd) REFERENCES t_common_code(com_code_id),
                          CONSTRAINT fk_status_drink FOREIGN KEY (drink_cd) REFERENCES t_common_code(com_code_id),
                          CONSTRAINT fk_status_condition FOREIGN KEY (condition_cd) REFERENCES t_common_code(com_code_id)
);

-- 하루 요약 로그
CREATE TABLE t_daily_log (
                             daily_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             log_date DATE NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             creator_id BIGINT NOT NULL,
                             modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             modifier_id BIGINT NOT NULL,
                             user_id BIGINT NOT NULL,
                             status_id BIGINT NOT NULL,
                             CONSTRAINT fk_dailylog_user FOREIGN KEY (user_id) REFERENCES t_user(user_id),
                             CONSTRAINT fk_dailylog_status FOREIGN KEY (status_id) REFERENCES t_status(status_id),
                             UNIQUE KEY uq_dailylog_user_date (user_id, log_date)
);
