package com.mediforme.mediforme.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAdminResponseDto {
    private Long userId;
    private String userLoginId;
    private String userName;
    private String phone;
    private Long roleCd;
    private Long consentCd;
    private Long statusCd;
}

