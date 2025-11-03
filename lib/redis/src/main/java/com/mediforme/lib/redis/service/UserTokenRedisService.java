package com.mediforme.lib.redis.service;

import com.mediforme.lib.redis.entity.UserToken;

import java.util.Optional;

public interface UserTokenRedisService {
    void saveUserToken(UserToken userToken);
    Optional<UserToken> findByAccessToken(String accessToken);
    Optional<UserToken> findByUserLoginId(String userLoginId);
    void deleteByAccessToken(String accessToken);
}
