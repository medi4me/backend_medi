package com.mediforme.mediforme.user.service;

import com.mediforme.mediforme.global.security.jwt.JwtToken;
import com.mediforme.mediforme.user.dto.UserLoginRequestDto;
import com.mediforme.mediforme.user.dto.UserRegisterRequestDto;
import com.mediforme.mediforme.user.dto.UserLoginResponseDto;

public interface AuthService {
    UserLoginResponseDto login(UserLoginRequestDto.LoginRequestDto request);      // 로그인 (JWT 발급 포함)
    UserLoginResponseDto register(UserRegisterRequestDto.JoinRequest request);    // 회원가입과 동시에 자동 로그인 처리
    JwtToken reissue(String refreshToken);
    void checkDuplicateLoginId(String userLoginId);

}
