package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.UserMedicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMedicineRepository extends JpaRepository<UserMedicine, Long> {
    // 특정 사용자 전체 복용 약 조회
    List<UserMedicine> findByUserId(Long userId);

    // (특정 사용자, 약 Id)로 복용 정보 조회 (중복 등록 방지 용도)
    Optional<UserMedicine> findByUserIdAndMedicineId(Long userId, Long medicineId);

    // 알람이 켜져 있는 약만 조회 (푸시/스케줄러 서비스 용도)
    List<UserMedicine> findByUserIdAndIsAlarmTrue(Long userId);
}
