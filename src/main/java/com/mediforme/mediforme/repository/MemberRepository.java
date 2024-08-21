package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository  extends JpaRepository<Member, Long> {
    Optional<Member> findById(Long id);
}
