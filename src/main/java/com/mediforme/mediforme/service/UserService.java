package com.mediforme.mediforme.service;


import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;

public interface UserService {
    String findUserNameByLoginId(String userLoginId);
    User createUser(UserRegisterRequestDto.JoinRequest request);
    User getByLoginId(String userLoginId);

}