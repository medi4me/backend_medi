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

    Optional<Status> findByStatusIdAndUserId(Long statusId, Long userId);   // 사용자 소유 검증용 (삭제/상세조회 등 사용)

    boolean existsByStatusIdAndUserId(Long statusId, Long userId);          // 존재 여부 확인 용도
}
