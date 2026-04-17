package com.mediforme.mediforme.medicine.external.client;

import com.mediforme.mediforme.medicine.external.RxNormApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * NIH RxNorm REST API 클라이언트
 * - 엔드포인트: /approximateTerm.json
 * - 퍼지 매칭 기반 후보 이름 제안 (OCR 오타 복원용)
 */
@Slf4j
@Component
public class RxNormClient {

    private static final int DEFAULT_MAX_ENTRIES = 10;

    private final RestClient restClient;

    public RxNormClient(RxNormApiConfig config) {
        this.restClient = RestClient.builder()
            .baseUrl(config.getBaseUrl())
            .build();
    }

    /**
     * 입력 이름 기반 후보 리스트 반환 (유사도 내림차순은 RxNorm 측에서 보장)
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchApproximateCandidates(String term) {
        if (term == null || term.isBlank()) return Collections.emptyList();

        try {
            Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/approximateTerm.json")
                    .queryParam("term", term)
                    .queryParam("maxEntries", DEFAULT_MAX_ENTRIES)
                    .build())
                .retrieve()
                .body(Map.class);

            if (response == null) return Collections.emptyList();
            Object group = response.get("approximateGroup");
            if (!(group instanceof Map<?, ?> g)) return Collections.emptyList();

            Object candidates = g.get("candidate");
            if (!(candidates instanceof List<?> list)) return Collections.emptyList();

            List<Map<String, Object>> out = new ArrayList<>();
            for (Object c : list) {
                if (c instanceof Map<?, ?> m) {
                    out.add((Map<String, Object>) m);
                }
            }
            return out;
        } catch (Exception e) {
            log.warn("RxNorm approximateTerm call failed. term={}", term, e);
            return Collections.emptyList();
        }
    }
}
