package com.mediforme.mediforme.Repository;

import com.mediforme.mediforme.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisterRepository  extends JpaRepository<Member, Long> {
    Optional<Member> findByName(String name);
    Optional<Member> findByMemberID(String memberID);
    Member findByPhone(String phone);
}
