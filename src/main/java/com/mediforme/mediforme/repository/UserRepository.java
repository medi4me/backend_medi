package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 조회용 (인증, 비지니스 로직)
    Optional<User> findByUserLoginId(String userLoginId);       // 사용자 로그인ID로 사용자 조회
    Optional<User> findByUserName(String userName);             // 사용자 이름 사용자 조회

    // 아이디 찾기/비밀번호 재설정 등 사용자 정보가 필요한 유즈케이스에서 사용
    Optional<User> findByPhone(String phone);               // phone으로 사용자 조회

    // 중복 검증용 (회원가입/검증 단계에서 존재 여부만 필요할 때)
    boolean existsByUserLoginId(String userLoginId);        // 로그인 ID 중복 여부 확인
    boolean existsByPhone(String phone);                    // 전화번호 중복 여부 확인(회원가입 중복 검증, 가입 여부 확인 등)
}
