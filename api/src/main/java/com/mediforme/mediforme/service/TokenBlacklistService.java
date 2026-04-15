package com.mediforme.mediforme.service;

public interface TokenBlacklistService {
    void addToBlacklist(String accessToken);
    boolean isTokenBlacklisted(String accessToken);
}
