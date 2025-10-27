package com.mediforme.mediforme.service;

import com.mediforme.mediforme.config.jwt.JwtToken;
import com.mediforme.mediforme.domain.User;

public interface AuthService {
    JwtToken getToken(User user);

    String getLoginUserLoginId();

    User getLoginUser();
}
