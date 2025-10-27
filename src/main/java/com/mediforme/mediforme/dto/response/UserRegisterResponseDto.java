package com.mediforme.mediforme.dto.response;

import com.mediforme.mediforme.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserRegisterResponseDto {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    // 회원가입 요청 응답
    public static class JoinResultDTO{
        private Long userId;            // 내부 PK
        private String userLoginId;     // 사용자가 입력한 로그인 ID
        private String userName;        // 사용자 이름
        private LocalDateTime createdAt; // 가입 일시
    }
}
