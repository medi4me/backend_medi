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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 회원 탈퇴 통합 테스트 — PR #21 정책 실체 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ResignIntegrationTest {

    private static final String LOGIN_ID = "integ-resign@test.com";

    @MockBean private ImageAnnotatorClient imageAnnotatorClient;
    @MockBean private RedisConnectionFactory redisConnectionFactory;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserTokenRedisService userTokenRedisService;

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private User user;

    @BeforeEach
    void seed() {
        given(tokenBlacklistService.isTokenBlacklisted(anyString())).willReturn(false);

        user = userRepository.save(User.builder()
            .userLoginId(LOGIN_ID)
            .password(passwordEncoder.encode("pw"))
            .userName("탈퇴대상")
            .phone("01033333333")
            .roleCd(1001L).consentCd(1L).statusCd(2001L)
            .build());
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("DELETE /users/me/resign 정상 → 200 + DB statusCd=9999 + blacklist 추가")
    void resign_valid_marksResignedAndBlacklists() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), LOGIN_ID, 1001L);

        mvc.perform(delete("/users/me/resign").header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"));

        User updated = userRepository.findById(user.getUserId()).orElseThrow();
        assertThat(updated.getStatusCd()).isEqualTo(9999L);

        verify(tokenBlacklistService).addToBlacklist(accessToken);
        verify(userTokenRedisService).deleteByUserLoginId(LOGIN_ID);
    }

    @Test
    @DisplayName("DELETE /users/me/resign 토큰 없음 → 401 JWT401")
    void resign_noToken_returns401() throws Exception {
        mvc.perform(delete("/users/me/resign"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("JWT401"));
    }
}
