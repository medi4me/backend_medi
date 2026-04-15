package com.mediforme.mediforme.status.repository;


import com.mediforme.mediforme.status.domain.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    Optional<DailyLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);  // (user_id, log_date) 유니크 조건을 기준으로 조회
}
