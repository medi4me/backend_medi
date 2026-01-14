package com.mediforme.mediforme.service;

import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;
import com.mediforme.mediforme.dto.response.UserAdminResponseDto;
import com.mediforme.mediforme.dto.response.UserMeResponseDto;

public interface UserService {
    String findUserNameByLoginId(String userLoginId);
    User createUser(UserRegisterRequestDto.JoinRequest request);
    User getByLoginId(String userLoginId);
    UserMeResponseDto getMe(Long userId);   // 내 정보 조회 (토큰 기반 userId)
    UserAdminResponseDto getUserForAdmin(Long userId);  // 관리자용 유저 조회
}