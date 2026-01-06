## ROLE
INSERT INTO t_common_code (
    com_code_group_cd, com_code_cd, com_code_name,
    sort_order, usage_yn, created_at, modified_at
) VALUES
      ('ROLE', 'USER',  '일반 사용자', 1, 1, NOW(), NOW()),
      ('ROLE', 'ADMIN', '관리자',     2, 1, NOW(), NOW());

## STATUS
INSERT INTO t_common_code (
    com_code_group_cd, com_code_cd, com_code_name,
    sort_order, usage_yn, created_at, modified_at
) VALUES
      ('STATUS', 'ACTIVE',   '활성', 1, 1, NOW(), NOW()),
      ('STATUS', 'INACTIVE', '비활성', 2, 1, NOW(), NOW());

## CONSENT
INSERT INTO t_common_code (
    com_code_group_cd, com_code_cd, com_code_name,
    sort_order, usage_yn, created_at, modified_at
) VALUES
      ('CONSENT', 'AGREED', '동의',     1, 1, NOW(), NOW()),
      ('CONSENT', 'REQUIRED','미동의',  2, 1, NOW(), NOW());
