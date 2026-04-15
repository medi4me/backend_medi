package com.mediforme.mediforme.config.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtToken {
    private String grantType;   // Bearer
    private String accessToken;
    private String refreshToken;
}