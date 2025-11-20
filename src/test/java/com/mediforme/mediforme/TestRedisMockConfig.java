package com.mediforme.mediforme;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 테스트 환경에서 Redis 관련 빈 의존성을 Mock으로 대체
 */
@Disabled
@TestConfiguration
public class TestRedisMockConfig {

    @Bean
    @Primary
    public RedisTemplate<?, ?> redisTemplate() {
        return new RedisTemplate<>(); // 실제 연결 없는 더미 RedisTemplate
    }
}
