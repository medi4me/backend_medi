package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserLoginId(String userLoginId);       // 사용자 로그인 id로 회원 조회
    Optional<User> findByUserName(String userName);             // 사용자 이름 조회
    Optional<User> findByPhone(String phone);                   // 전화번호 기반 조회 (중복 체크 용도)
    boolean existsByPhone(String phone);                        // 전화번호 존재 여부 확인 (아이디 찾기 시 사용)
}
