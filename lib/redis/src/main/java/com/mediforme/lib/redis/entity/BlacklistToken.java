package com.mediforme.lib.redis.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * 블랙리스트 토큰 관리 (로그아웃 처리용)
 * - Access Token을 Redis에 저장하고 TTL 이후 자동 삭제
 * - TTL: 1일 (초 단위)
 */
@Getter
@NoArgsConstructor
@RedisHash(value = "blacklistToken", timeToLive = 60 * 60 * 24) // 1일
public class BlacklistToken {

    @Id
    private String accessToken;

    private String reason;  // (logout, expired 등)

    @Builder
    public BlacklistToken(String accessToken, String reason) {
        this.accessToken = accessToken;
        this.reason = reason;
    }
}