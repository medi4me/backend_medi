package com.mediforme.mediforme;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.mediforme.lib.redis.service.UserTokenRedisService;
import com.mediforme.mediforme.global.security.TokenBlacklistService;
import com.mediforme.mediforme.global.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.medicine.domain.UserMedicine;
import com.mediforme.mediforme.medicine.repository.UserMedicineRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserMedicine DB CRUD 엔드포인트 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserMedicineIntegrationTest {

    private static final String LOGIN_ID = "integ-usermed@test.com";

    @MockBean private ImageAnnotatorClient imageAnnotatorClient;
    @MockBean private RedisConnectionFactory redisConnectionFactory;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserTokenRedisService userTokenRedisService;

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserMedicineRepository userMedicineRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private String accessToken;
    private Long userMedicineId;

    @BeforeEach
    void seed() {
        given(tokenBlacklistService.isTokenBlacklisted(anyString())).willReturn(false);

        User user = userRepository.save(User.builder()
            .userLoginId(LOGIN_ID)
            .password(passwordEncoder.encode("pw"))
            .userName("복약")
            .phone("01088888888")
            .roleCd(1001L).consentCd(1L).statusCd(2001L)
            .build());
        accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), LOGIN_ID, 1001L);

        UserMedicine saved = userMedicineRepository.save(UserMedicine.builder()
            .userId(user.getUserId())
            .medicineId(1L)
            .daysOfWeekCd(1234567L)
            .dosage("1정")
            .isAlarm(false)
            .mealCd(1L)
            .timeCd(1L)
            .build());
        userMedicineId = saved.getUserMedicineId();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("PUT /v2/user-medicine/{id}/check → 200")
    void check_valid_returns200() throws Exception {
        mvc.perform(put("/v2/user-medicine/" + userMedicineId + "/check")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("COMMON200"));
    }

    @Test
    @DisplayName("PUT /v2/user-medicine/{id}/alarm?isOn=true → 200")
    void toggleAlarm_valid_returns200() throws Exception {
        mvc.perform(put("/v2/user-medicine/" + userMedicineId + "/alarm")
                .header("Authorization", "Bearer " + accessToken)
                .param("isOn", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("COMMON200"));
    }

    @Test
    @DisplayName("PUT /v2/user-medicine/{non-existent}/check → 404 MEDICINE402")
    void check_nonExistent_returns404() throws Exception {
        mvc.perform(put("/v2/user-medicine/999999/check")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("MEDICINE402"));
    }
}
