package com.mediforme.mediforme;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.mediforme.lib.redis.service.UserTokenRedisService;
import com.mediforme.mediforme.global.security.TokenBlacklistService;
import com.mediforme.mediforme.user.domain.User;
import com.mediforme.mediforme.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Auth 플로우 + /users/me 보호 경로 HTTP 통합 테스트
 *
 * MockMvc 기반으로 필터 체인 → 컨트롤러 → 서비스 → H2 DB 까지 한 번에 거치는 회귀 검증
 * Redis 연관 서비스는 mock, 외부 API 도 mock
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthEndpointIntegrationTest {

    private static final String ACTIVE_LOGIN_ID = "integ-active@test.com";
    private static final String RESIGNED_LOGIN_ID = "integ-resigned@test.com";
    private static final String PASSWORD = "password123";

    @MockBean
    private ImageAnnotatorClient imageAnnotatorClient;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private UserTokenRedisService userTokenRedisService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void seed() {
        given(tokenBlacklistService.isTokenBlacklisted(anyString())).willReturn(false);

        userRepository.save(User.builder()
            .userLoginId(ACTIVE_LOGIN_ID)
            .password(passwordEncoder.encode(PASSWORD))
            .userName("통합테스트-활성")
            .phone("01000001111")
            .roleCd(1001L)
            .consentCd(1L)
            .statusCd(2001L)    // ACTIVE
            .build());

        userRepository.save(User.builder()
            .userLoginId(RESIGNED_LOGIN_ID)
            .password(passwordEncoder.encode(PASSWORD))
            .userName("통합테스트-탈퇴")
            .phone("01000002222")
            .roleCd(1001L)
            .consentCd(1L)
            .statusCd(9999L)    // RESIGNED
            .build());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /auth/login 정상 계정 → 200 + accessToken/refreshToken 발급")
    void login_validCredentials_returnsTokens() throws Exception {
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(ACTIVE_LOGIN_ID, PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result.userLoginId").value(ACTIVE_LOGIN_ID))
            .andExpect(jsonPath("$.result.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.result.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login 비밀번호 오류 → 401 INVALID_LOGIN (AUTH406)")
    void login_wrongPassword_returns401() throws Exception {
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(ACTIVE_LOGIN_ID, "wrong-password")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("AUTH406"));
    }

    @Test
    @DisplayName("POST /auth/login 존재하지 않는 계정 → 401 (hide-user-not-found, 비번오류와 동일)")
    void login_nonExistent_returns401WithSameCode() throws Exception {
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("ghost@nowhere.com", PASSWORD)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("AUTH406"));
    }

    @Test
    @DisplayName("POST /auth/login 탈퇴 계정(status=9999) → 409 USER_403_001 (PR #21 회귀 가드)")
    void login_resignedUser_returns409() throws Exception {
        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(RESIGNED_LOGIN_ID, PASSWORD)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("USER_403_001"));
    }

    @Test
    @DisplayName("GET /users/me 토큰 없음 → 401 + ApiResponse + JWT401 (PR #37 회귀 가드)")
    void me_noToken_returns401ApiResponse() throws Exception {
        mvc.perform(get("/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("JWT401"));
    }

    @Test
    @DisplayName("GET /users/me 잘못된 토큰 → 401 + JWT401")
    void me_invalidToken_returns401() throws Exception {
        mvc.perform(get("/users/me")
                .header("Authorization", "Bearer bad.token.value"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("JWT401"));
    }

    @Test
    @DisplayName("GET /users/me 정상 토큰 → 200 + 내 정보")
    void me_validToken_returnsUserInfo() throws Exception {
        MvcResult loginResult = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(ACTIVE_LOGIN_ID, PASSWORD)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.path("result").path("accessToken").asText();

        mvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result.userLoginId").value(ACTIVE_LOGIN_ID))
            .andExpect(jsonPath("$.result.userName").value("통합테스트-활성"))
            .andExpect(jsonPath("$.result.statusCd").value(2001));
    }

    private static String loginBody(String loginId, String password) {
        return """
            {"userLoginId":"%s","password":"%s"}
            """.formatted(loginId, password);
    }
}
