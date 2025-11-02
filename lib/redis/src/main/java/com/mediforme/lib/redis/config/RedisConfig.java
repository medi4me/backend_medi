package com.mediforme.lib.redis.config;

import com.mediforme.lib.redis.enums.RedisDBIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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
     * - Redis 연결을 설정하기 위한 빈
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        redisConfig.setDatabase(RedisDBIndex.AUTH.getIndex());       // 기본 AUTH DB 사용

        if (password != null && !password.isEmpty()) {
            redisConfig.setPassword(password);
        }

        log.info("Redis 연결 설정 완료 → Host: {}, Port: {}, DB: {}", host, port, RedisDBIndex.AUTH.getIndex());
        return new LettuceConnectionFactory(redisConfig);
    }



    /**
     * RedisTemplate 설정
     * - Redis 데이터베이스에 대한 연산을 수행하는 데 사용되는 빈
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // Redis 서버 연결
        redisTemplate.setConnectionFactory(connectionFactory);

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
     * 블랙리스트용 별도 RedisTemplate (DB1)
     */
    @Bean
    public RedisConnectionFactory blacklistConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(RedisDBIndex.BLACKLIST.getIndex());
        if (password != null && !password.isEmpty()) config.setPassword(password);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> blacklistRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(blacklistConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
