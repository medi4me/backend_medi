package com.mediforme.mediforme;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.mediforme.lib.redis.service.UserTokenRedisService;
import com.mediforme.mediforme.global.security.TokenBlacklistService;
import com.mediforme.mediforme.global.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.status.domain.Status;
import com.mediforme.mediforme.status.repository.StatusRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Status CRUD 통합 테스트 + PR #29 DateTimeParseException 회귀 가드
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StatusIntegrationTest {

    private static final String LOGIN_ID = "integ-status@test.com";
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 4, 15);

    @MockBean private ImageAnnotatorClient imageAnnotatorClient;
    @MockBean private RedisConnectionFactory redisConnectionFactory;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserTokenRedisService userTokenRedisService;

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private User user;
    private String accessToken;

    @BeforeEach
    void seed() {
        given(tokenBlacklistService.isTokenBlacklisted(anyString())).willReturn(false);

        user = userRepository.save(User.builder()
            .userLoginId(LOGIN_ID)
            .password(passwordEncoder.encode("pw"))
            .userName("상태")
            .phone("01044444444")
            .roleCd(1001L).consentCd(1L).statusCd(2001L)
            .build());

        accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), LOGIN_ID, 1001L);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /users/me/statuses 생성 → 200 + ApiResponse")
    void createStatus_returns200() throws Exception {
        mvc.perform(post("/users/me/statuses")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"statusDate":"2025-04-15","defaultStatusCd":1,"drinkCd":1,"conditionCd":1,"statusMemo":"컨디션 좋음"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"));
    }

    @Test
    @DisplayName("GET /users/me/statuses/{date} 조회 → 200 + 해당 날짜 상태")
    void getStatusByDate_returns200() throws Exception {
        statusRepository.save(Status.builder()
            .defaultStatusCd(1L).drinkCd(1L).conditionCd(1L)
            .statusMemo("시드").statusDate(TEST_DATE).userId(user.getUserId())
            .build());

        mvc.perform(get("/users/me/statuses/2025-04-15")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"));
    }

    @Test
    @DisplayName("GET /users/me/statuses/week-summary?startDate=잘못된날짜 → 400 COMMON400 (PR #29 회귀 가드)")
    void weekSummary_badDate_returns400() throws Exception {
        mvc.perform(get("/users/me/statuses/week-summary")
                .header("Authorization", "Bearer " + accessToken)
                .param("startDate", "2025-13-45")
                .param("endDate", "2025-04-22"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("COMMON400"))
            .andExpect(jsonPath("$.message").value("날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"));
    }

    @Test
    @DisplayName("PUT /users/me/statuses/{date} 수정 → 200")
    void updateStatusByDate_returns200() throws Exception {
        statusRepository.save(Status.builder()
            .defaultStatusCd(1L).drinkCd(1L).conditionCd(1L)
            .statusMemo("old").statusDate(TEST_DATE).userId(user.getUserId())
            .build());

        mvc.perform(put("/users/me/statuses/2025-04-15")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"statusDate":"2025-04-15","defaultStatusCd":2,"drinkCd":1,"conditionCd":1,"statusMemo":"updated"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("COMMON200"));
    }

    @Test
    @DisplayName("DELETE /users/me/statuses/id/{statusId} 삭제 → 200")
    void deleteStatusById_returns200() throws Exception {
        Status saved = statusRepository.save(Status.builder()
            .defaultStatusCd(1L).drinkCd(1L).conditionCd(1L)
            .statusMemo("to delete").statusDate(TEST_DATE).userId(user.getUserId())
            .build());

        mvc.perform(delete("/users/me/statuses/id/" + saved.getStatusId())
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("COMMON200"));
    }
}
