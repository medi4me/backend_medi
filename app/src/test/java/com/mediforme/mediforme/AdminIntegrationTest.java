package com.mediforme.mediforme;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.mediforme.lib.redis.service.UserTokenRedisService;
import com.mediforme.mediforme.global.security.TokenBlacklistService;
import com.mediforme.mediforme.global.security.jwt.JwtTokenProvider;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Admin 엔드포인트 역할 기반 인가 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminIntegrationTest {

    private static final String ADMIN_LOGIN_ID = "integ-admin@test.com";
    private static final String USER_LOGIN_ID = "integ-user@test.com";

    @MockBean private ImageAnnotatorClient imageAnnotatorClient;
    @MockBean private RedisConnectionFactory redisConnectionFactory;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserTokenRedisService userTokenRedisService;

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private Long adminUserId;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void seed() {
        given(tokenBlacklistService.isTokenBlacklisted(anyString())).willReturn(false);

        User admin = userRepository.save(User.builder()
            .userLoginId(ADMIN_LOGIN_ID)
            .password(passwordEncoder.encode("pw"))
            .userName("관리자")
            .phone("01066666666")
            .roleCd(1002L).consentCd(1L).statusCd(2001L)  // ADMIN
            .build());
        adminUserId = admin.getUserId();
        adminToken = jwtTokenProvider.createAccessToken(admin.getUserId(), ADMIN_LOGIN_ID, 1002L);

        User regular = userRepository.save(User.builder()
            .userLoginId(USER_LOGIN_ID)
            .password(passwordEncoder.encode("pw"))
            .userName("일반")
            .phone("01077777777")
            .roleCd(1001L).consentCd(1L).statusCd(2001L)  // USER
            .build());
        userToken = jwtTokenProvider.createAccessToken(regular.getUserId(), USER_LOGIN_ID, 1001L);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /admin/users/{id} ADMIN 토큰 → 200")
    void adminEndpoint_withAdminToken_returns200() throws Exception {
        mvc.perform(get("/admin/users/" + adminUserId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /admin/users/{id} USER 토큰 → 403 ACTION403")
    void adminEndpoint_withUserToken_returns403() throws Exception {
        mvc.perform(get("/admin/users/" + adminUserId)
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("ACTION403"));
    }

    @Test
    @DisplayName("GET /admin/users/{id} 토큰 없음 → 401 JWT401")
    void adminEndpoint_noToken_returns401() throws Exception {
        mvc.perform(get("/admin/users/" + adminUserId))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("JWT401"));
    }
}
