package com.mediforme.mediforme.service;

import com.mediforme.mediforme.config.security.jwt.JwtToken;
import com.mediforme.mediforme.dto.request.UserLoginRequestDto;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;
import com.mediforme.mediforme.dto.response.UserLoginResponseDto;

public interface AuthService {
    UserLoginResponseDto login(UserLoginRequestDto.LoginRequestDto request);      // 로그인 (JWT 발급 포함)
    UserLoginResponseDto register(UserRegisterRequestDto.JoinRequest request);    // 회원가입과 동시에 자동 로그인 처리
    JwtToken reissue(String refreshToken);
    void checkDuplicateLoginId(String userLoginId);

}
