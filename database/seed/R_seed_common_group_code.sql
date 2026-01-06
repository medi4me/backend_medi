INSERT INTO t_common_group_code (
    com_code_group_id,
    com_code_group_name,
    created_at,
    modified_at
) VALUES
      ('ROLE',   '사용자 역할', NOW(), NOW()),
      ('STATUS', '사용자 상태', NOW(), NOW()),
      ('CONSENT','약관 동의 상태', NOW(), NOW());
