package com.mediforme.lib.redis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * 사용자 인증 토큰 정보 (Redis 저장용)
 * - timeToLive: 7일 (초 단위)
 */
@Getter
@Builder
@AllArgsConstructor
@RedisHash("auth:userToken")
public class UserToken {

    @Id
    private String refreshToken;

    @Indexed  // 인덱싱 - 특정 필드 기반 검색 지원
    private String userLoginId;

    private Long userId;

    private String role;

    @TimeToLive
    private Long ttlSeconds;

}
