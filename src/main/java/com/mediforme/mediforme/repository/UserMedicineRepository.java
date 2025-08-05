package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.mapping.UserMedicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMedicineRepository extends JpaRepository<UserMedicine, Long> {
    List<UserMedicine> findByMemberId(Long memberId);
}
