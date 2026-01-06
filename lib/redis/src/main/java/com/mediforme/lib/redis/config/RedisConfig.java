package com.mediforme.lib.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정
 * - Redis 연결 설정 (Lettuce 기반)
 * - RedisTemplate Bean 등록
 * - Redis 캐싱, 토큰 저장, 세션 관리에 활용
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}") // 비밀번호가 없으면 빈 문자열로 처리
    private String password;

    /**
     * RedisConnectionFactory 설정
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);

        if (password != null && !password.isBlank()) {
            redisConfig.setPassword(password);
        }

        log.info("Redis 연결 설정 완료 → Host: {}, Port: {}", host, port);
        return new LettuceConnectionFactory(redisConfig);
    }



    /**
     * RedisTemplate 설정
     * - @RedisHash repository가 내부적으로도 사용 가능
     * - 직접 RedisTemplate 쓸 경우를 대비해 둠
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory cf) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // Redis 서버 연결
        redisTemplate.setConnectionFactory(cf);

        // 문자열 기반 직렬화 (Key/Hash Key)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // JSON 기반 직렬화 (Value/Hash Value)
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        log.info("RedisTemplate 등록 완료");
        return redisTemplate;
    }

    /**
     * 단순 문자열 저장용 (블랙리스트/인증코드 등)
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        log.info("StringRedisTemplate 등록 완료 (단순 문자열 Key-Value 저장용)");
        return template;
    }
}
