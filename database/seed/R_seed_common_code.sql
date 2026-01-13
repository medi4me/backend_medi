## ROLE
INSERT INTO t_common_code (
    com_code_group_cd, com_code_cd, com_code_name,
    sort_order, usage_yn, created_at, modified_at
) VALUES
      ('ROLE', 'USER',  '일반 사용자', 1001, 1, NOW(), NOW()),
      ('ROLE', 'ADMIN', '관리자',     1002, 1, NOW(), NOW()),
      ('ROLE', 'MANAGER', '운영자',     1003, 1, NOW(), NOW());

## STATUS
INSERT INTO t_common_code (
    com_code_group_cd, com_code_cd, com_code_name,
    sort_order, usage_yn, created_at, modified_at
) VALUES
      ('STATUS', 'ACTIVE',   '활성', 1, 1, NOW(), NOW()),
      ('STATUS', 'INACTIVE', '비활성', 2, 1, NOW(), NOW()),
      ('STATUS', 'RESIGNED', '탈퇴', 3, 1, NOW(), NOW());

## CONSENT
INSERT INTO t_common_code (
    com_code_group_cd, com_code_cd, com_code_name,
    sort_order, usage_yn, created_at, modified_at
) VALUES
      ('CONSENT', 'AGREED', '동의',     1, 1, NOW(), NOW()),
      ('CONSENT', 'REQUIRED','미동의',  2, 1, NOW(), NOW());

## MEAL_CD (식사 코드)
INSERT INTO t_common_code (
    com_code_group_cd,
    com_code_cd,
    com_code_name,
    sort_order,
    usage_yn,
    created_at,
    modified_at
) VALUES
('MEAL_CD', 'BEFORE', '식전', 1, b'1', NOW(6), NOW(6)),
('MEAL_CD', 'AFTER',  '식후', 2, b'1', NOW(6), NOW(6));


## TIME_CD (복용 시간 코드)
INSERT INTO t_common_code (
    com_code_group_cd,
    com_code_cd,
    com_code_name,
    sort_order,
    usage_yn,
    created_at,
    modified_at
) VALUES
('TIME_CD', 'MORNING', '아침', 1, b'1', NOW(6), NOW(6)),
('TIME_CD', 'LUNCH',   '점심', 2, b'1', NOW(6), NOW(6)),
('TIME_CD', 'DINNER',  '저녁', 3, b'1', NOW(6), NOW(6));


## DAYS_OF_WEEK_CD (요일 코드)
INSERT INTO t_common_code (
    com_code_group_cd,
    com_code_cd,
    com_code_name,
    sort_order,
    usage_yn,
    created_at,
    modified_at
) VALUES
('DAYS_OF_WEEK_CD', 'MON', '월', 1, b'1', NOW(6), NOW(6)),
('DAYS_OF_WEEK_CD', 'TUE', '화', 2, b'1', NOW(6), NOW(6)),
('DAYS_OF_WEEK_CD', 'WED', '수', 3, b'1', NOW(6), NOW(6)),
('DAYS_OF_WEEK_CD', 'THU', '목', 4, b'1', NOW(6), NOW(6)),
('DAYS_OF_WEEK_CD', 'FRI', '금', 5, b'1', NOW(6), NOW(6)),
('DAYS_OF_WEEK_CD', 'SAT', '토', 6, b'1', NOW(6), NOW(6)),
('DAYS_OF_WEEK_CD', 'SUN', '일', 7, b'1', NOW(6), NOW(6));

## ALARM_YN (알람 여부)
INSERT INTO t_common_code (
    com_code_group_cd,
    com_code_cd,
    com_code_name,
    sort_order,
    usage_yn,
    created_at,
    modified_at
) VALUES
('ALARM_YN', 'Y', '사용', 1, b'1', NOW(6), NOW(6)),
('ALARM_YN', 'N', '미사용', 2, b'1', NOW(6), NOW(6));
