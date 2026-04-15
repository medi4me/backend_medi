package com.mediforme.mediforme.global.security;

public interface TokenBlacklistService {
    void addToBlacklist(String accessToken);
    boolean isTokenBlacklisted(String accessToken);
}
