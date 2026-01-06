package com.mediforme.lib.redis.service;

public interface BlacklistRedisService {
    void addBlacklistedToken(String accessToken, long ttlMs, String reason);
    boolean isBlacklisted(String accessToken);
}