package com.mediforme.mediforme.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class UserRegisterRequestDto {
    @Getter
    @Setter
    // 회원가입 요청
    public static class JoinRequest {
        @NotBlank
        private String userLoginId;  // 로그인 ID

        @NotBlank
        private String userName;     // 이름

        @NotBlank
        private String password;     // 비밀번호

        @NotBlank
        private String phone;        // 전화번호

        @NotNull
        private Boolean agreeToTerms; // 약관 동의 여부 (true만 허용)
    }
}
