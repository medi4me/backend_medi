package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userLoginId) {
        // 사용자 조회
        User user = userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        // 공통 코드 기반 ROLE 매핑
        String roleName = normalizeRole(user.getRoleCd());

        // Spring Security User 객체 생성
        return org.springframework.security.core.userdetails.User.builder()
                .username(userLoginId)              // 로그인 식별자
                .password(user.getPassword())       // DB 인코딩된 비밀번호 그대로 사용
                .roles(roleName)                    // ROLE_ 접두사 자동 추가
                .build();
    }

    // 공통코드(role_cd)에 ROLE명 매핑 (1001(USER), 1002(ADMIN))
    private String normalizeRole(Long roleCd) {
        if (roleCd == null) {
            return "USER"; // 기본 권한
        }

        return switch (roleCd.intValue()){
            case 1002 -> "ADMIN";
            case 1003 -> "MANAGER";
            default -> "USER";
        };
    }
}
