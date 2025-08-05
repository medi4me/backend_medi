package com.mediforme.mediforme.service;

import com.mediforme.mediforme.config.jwt.JwtToken;
import com.mediforme.mediforme.domain.Member;

public interface AuthService {
    JwtToken getToken(Member member);

    Long getLoginMemberId();

    Member getLoginMember();
}
