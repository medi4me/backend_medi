package com.mediforme.mediforme.dto.request;

import lombok.Getter;
import lombok.Setter;

public class UserRegisterRequestDto {
    @Getter
    @Setter
    // 회원가입 요청
    public static class JoinRequest {
        private String userLoginId;  // 사용자가 입력하는 로그인용 ID
        private String userName;     // 이름
        private String password;     // 비밀번호
        private String phone;        // 전화번호
        private Long consentCd;      // 약관 동의 코드 (공통코드 ID)
    }
}
