package com.mediforme.mediforme.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberLoginResponseDTO {
    private String memberID;
    private String accessToken;
    private String refreshToken;
}
