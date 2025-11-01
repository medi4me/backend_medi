package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.config.security.jwt.JwtToken;
import com.mediforme.mediforme.config.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.repository.UserRepository;
import com.mediforme.mediforme.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // 사용자 정보로 JWT 토큰을 생성하고 Refresh Token을 저장
    @Override
    @Transactional
    public JwtToken getToken(User user) {
        // 로그인 Id 기준 JWT 발급
        JwtToken jwtToken = jwtTokenProvider.generateToken(user.getUserLoginId());
        // Refresh Token을 엔티티에 저장
        user.updateRefreshToken(jwtToken.getRefreshToken(), user.getUserId());
        return jwtToken;
    }

    // 현재 로그인한 사용자의 로그인 Id 반환
    @Override
    public String getLoginUserLoginId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 현재 로그인한 사용자 정보 반환
    @Override
    public User getLoginUser() {
        String userLoginId = getLoginUserLoginId();
        return userRepository.findByUserLoginId(userLoginId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    }
}
