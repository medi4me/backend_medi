package com.mediforme.mediforme.Repository;

import com.mediforme.mediforme.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterRepository  extends JpaRepository<Member, Long> {
    Member findByName(String name);
    Member findByMemberID(String memberID);
    Member findByPhone(String phone);
}
