package com.mediforme.mediforme;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.mediforme.mediforme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 전체 Spring context 가 오류 없이 기동되는지 검증하는 스모크 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
class MediformeApplicationTests {

    @MockBean
    private ImageAnnotatorClient imageAnnotatorClient;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private List<SecurityFilterChain> securityFilterChains;

    @Test
    @DisplayName("전체 Spring context 가 정상 기동 (bean wiring / auditing / @RestControllerAdvice 등록 검증)")
    void contextLoads() {
        assertThat(securityFilterChains).isNotEmpty();
    }

    @Test
    @DisplayName("SecurityFilterChain 에 JwtAuthenticationFilter 가 등록되어 있다")
    void securityFilterChain_containsJwtAuthenticationFilter() {
        boolean hasJwtFilter = securityFilterChains.stream()
            .filter(DefaultSecurityFilterChain.class::isInstance)
            .map(DefaultSecurityFilterChain.class::cast)
            .flatMap(chain -> chain.getFilters().stream())
            .anyMatch(JwtAuthenticationFilter.class::isInstance);

        assertThat(hasJwtFilter)
            .as("보안 체인에 JwtAuthenticationFilter 가 등록되어야 함")
            .isTrue();
    }
}
