package com.mediforme.lib.redis.service.impl;

import com.mediforme.lib.redis.entity.BlacklistToken;
import com.mediforme.lib.redis.repository.BlacklistRedisRepository;
import com.mediforme.lib.redis.service.BlacklistRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistRedisServiceImpl implements BlacklistRedisService {

    private final BlacklistRedisRepository blacklistRedisRepository;

    @Override
    public void addBlacklistedToken(String accessToken, String reason) {
        BlacklistToken token = BlacklistToken.builder()
                .accessToken(accessToken)
                .reason(reason)
                .build();

        blacklistRedisRepository.save(token);
        log.info("블랙리스트 등록 완료 - token={}, reason={}", accessToken, reason);
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        boolean result = blacklistRedisRepository.existsByAccessToken(accessToken);
        if (result) log.warn("블랙리스트 토큰 감지됨 - {}", accessToken);
        return result;
    }
}