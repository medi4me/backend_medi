package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Medicine findByName(String name);
}
