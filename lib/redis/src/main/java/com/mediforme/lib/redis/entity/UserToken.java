package com.mediforme.lib.redis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * 사용자 인증 토큰 정보 (Redis 저장용)
 *  - key: "userToken:{accessToken}"
 * - timeToLive: 7일 (초 단위)
 */
@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "userToken", timeToLive = 60 * 60 * 24 * 7)
public class UserToken {

    @Id
    private String accessToken;

    private Long userId;

    @Indexed  // 인덱싱 - 특정 필드 기반 검색 지원
    private String userLoginId;

    private String role;

    private String refreshToken;

    public UserToken(String accessToken, String userLoginId, Long userId, String role, String refreshToken) {
        this.accessToken = accessToken;
        this.userLoginId = userLoginId;
        this.userId = userId;
        this.role = role;
        this.refreshToken = refreshToken;
    }
}
