package com.mediforme.lib.redis.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisDBIndex {
    AUTH(0),          // 사용자 인증 토큰 (AccessToken, RefreshToken)
    BLACKLIST(1),     // 로그아웃 토큰 (JWT 블랙리스트)
    CACHE(2);         // 임시 캐시

    private final int index;
}