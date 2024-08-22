package com.mediforme.mediforme.Repository;

import com.mediforme.mediforme.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisterRepository  extends JpaRepository<Member, Long> {
    Optional<Member> findByName(String name);
    Optional<Member> findByMemberID(String memberID);
    Member findByPhone(String phone);// 전화번호가 존재하는지 확인하는 메서드
}
