package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository  extends JpaRepository<Member, Long> {
    Optional<Member> findById(Long id);
    Optional<Member> findByName(String name);
    Optional<Member> findByMemberID(String memberID);
    // Member 타입을 반환하는 메소드는 디폴트 메소드로 정의
    /*default Member getMemberByMemberID(String memberID) {
        return findByMemberID(memberID)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    }*/
    Member findByPhone(String phone);// 전화번호가 존재하는지 확인하는 메서드
}
