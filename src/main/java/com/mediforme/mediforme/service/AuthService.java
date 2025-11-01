package com.mediforme.mediforme.service;

import com.mediforme.mediforme.config.security.jwt.JwtToken;
import com.mediforme.mediforme.domain.User;

public interface AuthService {
    JwtToken getToken(User user);

    String getLoginUserLoginId();

    User getLoginUser();
}
