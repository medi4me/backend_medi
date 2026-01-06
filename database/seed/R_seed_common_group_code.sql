INSERT INTO t_common_group_code (
    com_code_group_id,
    com_code_group_name,
    created_at,
    modified_at
) VALUES
      ('ROLE',   '사용자 역할', NOW(), NOW()),
      ('STATUS', '사용자 상태', NOW(), NOW()),
      ('CONSENT','약관 동의 상태', NOW(), NOW()),
      ('MEAL_CD',        '식사 구분',   NOW(6), NOW(6)),
      ('TIME_CD',        '복용 시간',   NOW(6), NOW(6)),
      ('DAYS_OF_WEEK_CD','요일',       NOW(6), NOW(6)),
      ('ALARM_YN',       '알람 여부',   NOW(6), NOW(6));
