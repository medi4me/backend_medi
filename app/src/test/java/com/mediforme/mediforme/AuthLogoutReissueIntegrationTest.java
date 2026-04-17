package com.mediforme.mediforme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.mediforme.lib.redis.entity.UserToken;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 로그아웃 + 토큰 재발급 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthLogoutReissueIntegrationTest {

    private static final String ACTIVE_LOGIN_ID = "integ-logout@test.com";
    private static final String RESIGNED_LOGIN_ID = "integ-logout-resigned@test.com";

    @MockBean private ImageAnnotatorClient imageAnnotatorClient;
    @MockBean private RedisConnectionFactory redisConnectionFactory;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserTokenRedisService userTokenRedisService;

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private ObjectMapper objectMapper;

    private User activeUser;
    private User resignedUser;

    @BeforeEach
    void seed() {
        given(tokenBlacklistService.isTokenBlacklisted(anyString())).willReturn(false);

        activeUser = userRepository.save(User.builder()
            .userLoginId(ACTIVE_LOGIN_ID)
            .password(passwordEncoder.encode("pw"))
            .userName("활성")
            .phone("01011111111")
            .roleCd(1001L).consentCd(1L).statusCd(2001L)
            .build());

        resignedUser = userRepository.save(User.builder()
            .userLoginId(RESIGNED_LOGIN_ID)
            .password(passwordEncoder.encode("pw"))
            .userName("탈퇴")
            .phone("01022222222")
            .roleCd(1001L).consentCd(1L).statusCd(9999L)
            .build());
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /auth/logout 정상 → 200 + TokenBlacklistService.addToBlacklist 호출")
    void logout_valid_blacklistsToken() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(activeUser.getUserId(), ACTIVE_LOGIN_ID, 1001L);

        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"));

        verify(tokenBlacklistService).addToBlacklist(accessToken);
    }

    @Test
    @DisplayName("POST /auth/logout 토큰 없음 → 401")
    void logout_noToken_returns401() throws Exception {
        mvc.perform(post("/auth/logout"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/reissue 유효한 refresh → 200 + 새 accessToken/refreshToken")
    void reissue_validRefresh_returnsNewTokens() throws Exception {
        String refreshToken = jwtTokenProvider.createRefreshToken(activeUser.getUserId(), ACTIVE_LOGIN_ID, 1001L);
        given(userTokenRedisService.findByRefreshToken(refreshToken))
            .willReturn(Optional.of(UserToken.builder()
                .refreshToken(refreshToken)
                .userLoginId(ACTIVE_LOGIN_ID)
                .userId(activeUser.getUserId())
                .role("1001")
                .ttlSeconds(604800L)
                .build()));

        mvc.perform(post("/auth/reissue").header("Authorization", "Bearer " + refreshToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.result.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/reissue 저장소에 없는 refresh → 401 JWT401")
    void reissue_unknownRefresh_returns401() throws Exception {
        String refreshToken = jwtTokenProvider.createRefreshToken(activeUser.getUserId(), ACTIVE_LOGIN_ID, 1001L);
        given(userTokenRedisService.findByRefreshToken(refreshToken)).willReturn(Optional.empty());

        mvc.perform(post("/auth/reissue").header("Authorization", "Bearer " + refreshToken))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("JWT401"));
    }

    @Test
    @DisplayName("POST /auth/reissue 탈퇴 사용자 refresh → 409 USER_403_001")
    void reissue_resignedUserRefresh_returns409() throws Exception {
        String refreshToken = jwtTokenProvider.createRefreshToken(resignedUser.getUserId(), RESIGNED_LOGIN_ID, 1001L);
        given(userTokenRedisService.findByRefreshToken(refreshToken))
            .willReturn(Optional.of(UserToken.builder()
                .refreshToken(refreshToken)
                .userLoginId(RESIGNED_LOGIN_ID)
                .userId(resignedUser.getUserId())
                .role("1001")
                .ttlSeconds(604800L)
                .build()));

        mvc.perform(post("/auth/reissue").header("Authorization", "Bearer " + refreshToken))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("USER_403_001"));
    }
}
