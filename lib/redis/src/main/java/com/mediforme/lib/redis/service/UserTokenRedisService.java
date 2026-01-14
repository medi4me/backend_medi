package com.mediforme.lib.redis.service;

import com.mediforme.lib.redis.entity.UserToken;

import java.util.Optional;

public interface UserTokenRedisService {
    void saveUserToken(UserToken userToken);
    Optional<UserToken> findByRefreshToken(String refreshToken);
    Optional<UserToken> findByUserLoginId(String userLoginId);
    void deleteByRefreshToken(String refreshToken);
    void deleteByUserLoginId(String userLoginId);
}
