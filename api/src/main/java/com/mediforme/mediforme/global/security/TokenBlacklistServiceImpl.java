package com.mediforme.mediforme.global.security;

import com.mediforme.lib.redis.service.BlacklistRedisService;
import com.mediforme.mediforme.global.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.global.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String REASON_LOGOUT = "LOGOUT";

    private final BlacklistRedisService blacklistRedisService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void addToBlacklist(String accessToken) {
        // 토큰이 비어있으면 아무것도 하지 않음
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("[BLACKLIST] skip: empty token");
            return;
        }

        // 토큰 만료까지 남은 TTL 계산
        long ttlMs = jwtTokenProvider.getRemainingTtlMs(accessToken);
        log.info("[BLACKLIST] try: tokenHead={}, ttlMs={}",
            accessToken.substring(0, Math.min(15, accessToken.length())),
            ttlMs);

        if (ttlMs <= 0) {   // 이미 만료된 토큰은 블랙리스트에 넣어도 의미 없음
            log.warn("[BLACKLIST] skip: ttlMs<=0");
            return;
        }

        // Redis에 블랙리스트 등록 (TTL 포함)
        blacklistRedisService.addBlacklistedToken(accessToken, ttlMs, REASON_LOGOUT);
        log.info("token blacklisted. reason={}, ttlMs={}", REASON_LOGOUT, ttlMs);
    }

    @Override
    public boolean isTokenBlacklisted(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        return blacklistRedisService.isBlacklisted(accessToken);
    }
}
