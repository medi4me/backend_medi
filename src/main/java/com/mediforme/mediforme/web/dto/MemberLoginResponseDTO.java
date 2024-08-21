package com.mediforme.mediforme.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberLoginResponseDTO {
    private Long memberId;
    private String accessToken;
    private String refreshToken;
}
