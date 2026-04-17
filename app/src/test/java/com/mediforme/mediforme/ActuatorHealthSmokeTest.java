package com.mediforme.mediforme;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Actuator 엔드포인트가 인증 없이 정상 노출되는지 검증하는 스모크 테스트
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ActuatorHealthSmokeTest {

    @MockBean
    private ImageAnnotatorClient imageAnnotatorClient;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("/actuator/health 는 인증 없이 200 + status UP 반환")
    void health_isPublic_andReportsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("JWT 보호 엔드포인트는 여전히 401 반환 (회귀 방지)")
    void protectedEndpoint_stillRequiresAuth() {
        ResponseEntity<String> response = restTemplate.getForEntity("/users/me", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("JWT401");
    }
}
