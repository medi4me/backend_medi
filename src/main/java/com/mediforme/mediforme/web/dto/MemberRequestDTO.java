package com.mediforme.mediforme.web.dto;

import lombok.Builder;
import lombok.Getter;

public class MemberRequestDTO
{
    @Builder
    @Getter
    public static class LoginRequestDto{
        private String memberID;
        private String password;
    }
}
