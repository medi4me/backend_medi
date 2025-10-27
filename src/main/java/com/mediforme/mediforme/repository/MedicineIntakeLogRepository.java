package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.MedicineIntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MedicineIntakeLogRepository extends JpaRepository<MedicineIntakeLog, Long> {
    List<MedicineIntakeLog> findByUserMedicineIdAndIntakeDate(Long userMedicineId, LocalDate intakeDate);   // 특정 UserMedicine에 대해, 특정 날짜의 복용 기록 조회
}