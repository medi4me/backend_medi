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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 상호작용 Check 도메인 통합 테스트 — PR #37 ApiResponse 래핑 회귀 가드
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CheckIntegrationTest {

    private static final String LOGIN_ID = "integ-check@test.com";

    @MockBean private ImageAnnotatorClient imageAnnotatorClient;
    @MockBean private RedisConnectionFactory redisConnectionFactory;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserTokenRedisService userTokenRedisService;

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private String accessToken;
    private Long userId;

    @BeforeEach
    void seed() {
        given(tokenBlacklistService.isTokenBlacklisted(anyString())).willReturn(false);

        User user = userRepository.save(User.builder()
            .userLoginId(LOGIN_ID)
            .password(passwordEncoder.encode("pw"))
            .userName("체크")
            .phone("01055555555")
            .roleCd(1001L).consentCd(1L).statusCd(2001L)
            .build());
        userId = user.getUserId();
        accessToken = jwtTokenProvider.createAccessToken(userId, LOGIN_ID, 1001L);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /v2/medicine-interactions/check → 200 + ApiResponse<List<String>> (PR #37 회귀 가드)")
    void checkDrugInteractions_returnsApiResponseList() throws Exception {
        mvc.perform(post("/v2/medicine-interactions/check")
                .header("Authorization", "Bearer " + accessToken)
                .param("userId", String.valueOf(userId))
                .param("newMedication", "아스피린"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result").isArray());
    }

    @Test
    @DisplayName("GET /v2/medicine-interactions/info?medicineName=타이레놀 → 200 + Mock 데이터 ApiResponse 래핑")
    void info_mockHasData_returnsApiResponseWithData() throws Exception {
        mvc.perform(get("/v2/medicine-interactions/info")
                .header("Authorization", "Bearer " + accessToken)
                .param("medicineName", "타이레놀"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result[0].name").value("타이레놀"))
            .andExpect(jsonPath("$.result[0].interactionWarnings").isNotEmpty());
    }

    @Test
    @DisplayName("GET /v2/medicine-interactions/info?medicineName=없는약 → 200 + 빈 배열 (PR #37 404→200 semantic 유지)")
    void info_mockNoData_returnsApiResponseEmptyArray() throws Exception {
        mvc.perform(get("/v2/medicine-interactions/info")
                .header("Authorization", "Bearer " + accessToken)
                .param("medicineName", "없는약"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result").isArray())
            .andExpect(jsonPath("$.result").isEmpty());
    }
}
