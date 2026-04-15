package com.mediforme.mediforme.user.service;

import com.mediforme.mediforme.user.domain.User;
import com.mediforme.mediforme.user.dto.UserRegisterRequestDto;
import com.mediforme.mediforme.admin.dto.UserAdminResponseDto;
import com.mediforme.mediforme.user.dto.UserMeResponseDto;

public interface UserService {
    String findUserNameByLoginId(String userLoginId);
    User createUser(UserRegisterRequestDto.JoinRequest request);
    User getByLoginId(String userLoginId);
    UserMeResponseDto getMe(Long userId);   // 내 정보 조회 (토큰 기반 userId)
    UserAdminResponseDto getUserForAdmin(Long userId);  // 관리자용 유저 조회
}