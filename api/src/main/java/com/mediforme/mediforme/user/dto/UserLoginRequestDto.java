package com.mediforme.mediforme.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserLoginRequestDto
{
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequestDto {
        private String userLoginId;
        private String password;
    }
}
