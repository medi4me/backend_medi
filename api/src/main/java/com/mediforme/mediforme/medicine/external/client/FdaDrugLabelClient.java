package com.mediforme.mediforme.medicine.external.client;

import com.mediforme.mediforme.medicine.external.FdaApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * openFDA drug label API 클라이언트
 */
@Slf4j
@Component
public class FdaDrugLabelClient {

    private static final int DEFAULT_LIMIT = 10;

    private final RestClient restClient;

    public FdaDrugLabelClient(FdaApiConfig config) {
        this.restClient = RestClient.builder()
            .baseUrl(config.getBaseUrl())
            .build();
    }

    /**
     * 이름(브랜드/제네릭)으로 검색하여 각 결과의 openfda 섹션을 리스트로 반환
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchOpenFdaByName(String name) {
        if (name == null || name.isBlank()) return Collections.emptyList();

        String safe = sanitize(name);
        if (safe.isBlank()) return Collections.emptyList();

        String search = "openfda.brand_name:\"" + safe + "\""
            + " OR openfda.generic_name:\"" + safe + "\"";

        try {
            Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/drug/label.json")
                    .queryParam("search", search)
                    .queryParam("limit", DEFAULT_LIMIT)
                    .build())
                .retrieve()
                .body(Map.class);

            if (response == null) return Collections.emptyList();
            Object results = response.get("results");
            if (!(results instanceof List<?> list)) return Collections.emptyList();

            List<Map<String, Object>> out = new ArrayList<>();
            for (Object r : list) {
                if (r instanceof Map<?, ?> m) {
                    Object openfda = m.get("openfda");
                    if (openfda instanceof Map<?, ?> o) {
                        out.add((Map<String, Object>) o);
                    }
                }
            }
            return out;

        } catch (HttpClientErrorException.NotFound e) {
            // openFDA: 매칭 없음 = 404
            return Collections.emptyList();
        }
    }

    /**
     * Lucene 쿼리 인젝션 방지용 최소 sanitization
     * 영문·숫자·한글·공백만 허용
     */
    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9가-힣 ]", "").trim();
    }
}
