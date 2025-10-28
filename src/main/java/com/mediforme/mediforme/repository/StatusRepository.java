package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

    Optional<Status> findByUserIdAndStatusDate(Long userId, LocalDate statusDate);

    List<Status> findByUserIdAndStatusDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Status> findByUserId(Long userId);
}
