package com.mediforme.lib.redis.service;

public interface BlacklistRedisService {
    void addBlacklistedToken(String accessToken, String reason);
    boolean isBlacklisted(String accessToken);
}