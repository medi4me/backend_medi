package com.mediforme.mediforme;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.mediforme.lib.redis.service.UserTokenRedisService;
import com.mediforme.mediforme.global.security.TokenBlacklistService;
import com.mediforme.mediforme.global.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.medicine.external.client.FdaDrugLabelClient;
import com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient;
import com.mediforme.mediforme.medicine.external.client.RxNormClient;
import com.mediforme.mediforme.user.domain.User;
import com.mediforme.mediforme.user.repository.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /medicines 및 GET /medicines/info 엔드포인트 HTTP 통합 테스트
 *
 * 외부 API 클라이언트(MFDS / FDA / RxNorm) 는 @MockBean 으로 차단
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MedicinesSearchIntegrationTest {

    private static final String LOGIN_ID = "integ-med-search@test.com";

    @MockBean private ImageAnnotatorClient imageAnnotatorClient;
    @MockBean private RedisConnectionFactory redisConnectionFactory;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserTokenRedisService userTokenRedisService;

    @MockBean private MfdsMedicineClient mfdsClient;
    @MockBean private FdaDrugLabelClient fdaClient;
    @MockBean private RxNormClient rxNormClient;

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private String accessToken;

    @BeforeEach
    void seed() {
        given(tokenBlacklistService.isTokenBlacklisted(anyString())).willReturn(false);

        User user = userRepository.save(User.builder()
            .userLoginId(LOGIN_ID)
            .password(passwordEncoder.encode("pw"))
            .userName("검색")
            .phone("01022223333")
            .roleCd(1001L).consentCd(1L).statusCd(2001L)
            .build());

        accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), LOGIN_ID, 1001L);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private JSONArray mfdsResponse(String name, String image) {
        JSONObject item = new JSONObject();
        item.put("itemName", name);
        item.put("itemImage", image);
        JSONArray arr = new JSONArray();
        arr.add(item);
        return arr;
    }

    // ───────────────────── GET /medicines ─────────────────────

    @Test
    @DisplayName("GET /medicines 는 공개 엔드포인트 — 토큰 없이도 200 (온보딩)")
    void search_noToken_publicEndpoint() throws Exception {
        given(mfdsClient.fetchItemsByName(anyString())).willReturn(new JSONArray());
        given(fdaClient.fetchOpenFdaByName(anyString())).willReturn(List.of());
        given(rxNormClient.fetchApproximateCandidates(anyString())).willReturn(List.of());

        mvc.perform(get("/medicines").param("name", "타이레놀"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("COMMON200"));
    }

    @Test
    @DisplayName("GET /medicines?name= (빈 문자열) → 400 COMMON400")
    void search_blankName_returns400() throws Exception {
        mvc.perform(get("/medicines")
                .header("Authorization", "Bearer " + accessToken)
                .param("name", ""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON400"));
    }

    @Test
    @DisplayName("GET /medicines 정상 호출 → 200 + ApiResponse<MedicineSearchResponseDto>")
    void search_valid_returns200Wrapped() throws Exception {
        given(mfdsClient.fetchItemsByName(anyString()))
            .willReturn(mfdsResponse("타이레놀", "http://img/tylenol.png"));
        given(fdaClient.fetchOpenFdaByName(anyString())).willReturn(List.of());
        given(rxNormClient.fetchApproximateCandidates(anyString())).willReturn(List.of());

        mvc.perform(get("/medicines")
                .header("Authorization", "Bearer " + accessToken)
                .param("name", "타이레놀"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result.medicines").isArray())
            .andExpect(jsonPath("$.result.medicines[0].name").value("타이레놀"))
            .andExpect(jsonPath("$.result.medicines[0].source").value("MFDS"));
    }

    @Test
    @DisplayName("GET /medicines 모든 어댑터 빈 결과 → 200 + medicines=[]")
    void search_allAdaptersEmpty_returnsEmptyArray() throws Exception {
        given(mfdsClient.fetchItemsByName(anyString())).willReturn(new JSONArray());
        given(fdaClient.fetchOpenFdaByName(anyString())).willReturn(List.of());
        given(rxNormClient.fetchApproximateCandidates(anyString())).willReturn(List.of());

        mvc.perform(get("/medicines")
                .header("Authorization", "Bearer " + accessToken)
                .param("name", "없는약"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.medicines").isArray())
            .andExpect(jsonPath("$.result.medicines").isEmpty());
    }

    @Test
    @DisplayName("GET /medicines MFDS 예외여도 다른 어댑터 결과는 살아남음 → 200")
    void search_mfdsFails_otherAdaptersStillContribute() throws Exception {
        given(mfdsClient.fetchItemsByName(anyString()))
            .willThrow(new IOException("MFDS down"));
        given(fdaClient.fetchOpenFdaByName(anyString())).willReturn(List.of(
            Map.of("brand_name", List.of("TYLENOL"))
        ));
        given(rxNormClient.fetchApproximateCandidates(anyString())).willReturn(List.of());

        mvc.perform(get("/medicines")
                .header("Authorization", "Bearer " + accessToken)
                .param("name", "tylenol"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.medicines[0].name").value("TYLENOL"))
            .andExpect(jsonPath("$.result.medicines[0].source").value("FDA"));
    }

    // ─────────────────── GET /medicines/info ───────────────────

    @Test
    @DisplayName("GET /medicines/info?name= (빈 문자열) → 400 COMMON400")
    void info_blankName_returns400() throws Exception {
        mvc.perform(get("/medicines/info")
                .header("Authorization", "Bearer " + accessToken)
                .param("name", ""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON400"));
    }

    @Test
    @DisplayName("GET /medicines/info 정상 → 200 + ApiResponse<List<MedicineInfoDto>>")
    void info_valid_returns200Wrapped() throws Exception {
        JSONObject item = new JSONObject();
        item.put("itemName", "타이레놀");
        item.put("itemImage", "http://img/tylenol.png");
        item.put("efcyQesitm", "해열·진통");
        item.put("useMethodQesitm", "1일 3회");
        item.put("intrcQesitm", "주의");
        item.put("atpnWarnQesitm", "음주 금지");
        JSONArray arr = new JSONArray();
        arr.add(item);
        given(mfdsClient.fetchItemsByName(anyString())).willReturn(arr);

        mvc.perform(get("/medicines/info")
                .header("Authorization", "Bearer " + accessToken)
                .param("name", "타이레놀"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value("COMMON200"))
            .andExpect(jsonPath("$.result").isArray())
            .andExpect(jsonPath("$.result[0].name").value("타이레놀"))
            .andExpect(jsonPath("$.result[0].benefit").value("해열·진통"));
    }

    @Test
    @DisplayName("GET /medicines/info 결과 없음 → 200 + result=[]")
    void info_noMatch_returnsEmpty() throws Exception {
        given(mfdsClient.fetchItemsByName(anyString())).willReturn(new JSONArray());

        mvc.perform(get("/medicines/info")
                .header("Authorization", "Bearer " + accessToken)
                .param("name", "없는약"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").isArray())
            .andExpect(jsonPath("$.result").isEmpty());
    }
}
