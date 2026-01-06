package com.mediforme.lib.redis.service.impl;

import com.mediforme.lib.redis.service.BlacklistRedisService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistRedisServiceImpl implements BlacklistRedisService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final HexFormat HEX = HexFormat.of();
    private static final String PREFIX = "auth:blacklist:";

    @Override
    public void addBlacklistedToken(String accessToken, long ttlMs, String reason) {
        validate(accessToken, ttlMs, reason);

        String key = keyOf(accessToken);
        stringRedisTemplate.opsForValue().set(key, reason, ttlMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        // 운영 보안상 토큰 원문 로그 금지. 해시 prefix만 남김
        log.info("token blacklisted. keySuffix = {}, ttlMs = {}, reason = {}", keySuffix(key), ttlMs, reason);
    }


    @Override
    public boolean isBlacklisted(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        String key = keyOf(accessToken);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    private void validate(String accessToken, long ttlMs, String reason) {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        if (accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken must not be blank");
        }
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        if (ttlMs <= 0) {
            // 이미 만료된 토큰이면 저장 의미가 없음
            throw new IllegalArgumentException("ttlMs must be positive");
        }
    }

    private String keyOf(String accessToken) {
        return PREFIX + sha256(accessToken);
    }

    private String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // JVM에 SHA-256이 없는 경우는 사실상 없음. 그래도 방어
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String keySuffix(String key) {
        // 해시 뒤 8자리 정도만 로그용으로 사용
        int len = key.length();
        return len <= 8 ? key : key.substring(len - 8);
    }


}