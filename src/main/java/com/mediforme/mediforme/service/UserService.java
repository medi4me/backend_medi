package com.mediforme.mediforme.service;

import com.mediforme.mediforme.dto.request.UserLoginRequestDto;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;
import com.mediforme.mediforme.dto.response.UserLoginResponseDto;

public interface UserService {
    UserLoginResponseDto login(UserLoginRequestDto.LoginRequestDto request);
    UserLoginResponseDto register(UserRegisterRequestDto.JoinRequest request);    // 회원가입과 동시에 자동 로그인 처리
    String findUserNameByLoginId(String userLoginId);
}