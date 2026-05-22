package com.mediforme.mediforme.chatbot.external.adapter;

import com.mediforme.mediforme.chatbot.external.RagApiConfig;
import com.mediforme.mediforme.chatbot.external.port.ChatbotRagClient;
import com.mediforme.mediforme.chatbot.external.port.RagChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mediforme-chatbot-rag /retrieve 호출 어댑터
 *
 * 검색 서비스 장애·빈 결과는 빈 리스트로 흡수해, 호출부가 앵커링으로 폴백하도록 한다.
 */
@Slf4j
@Component
public class HttpRagClient implements ChatbotRagClient {

    private final RestClient restClient;

    public HttpRagClient(RagApiConfig config) {
        // 기본 요청 팩토리(JDK HttpClient)는 HTTP/2 협상을 시도해 uvicorn(HTTP/1.1) 대상
        // POST 가 깨진다(빈 body → 422). HTTP/1.1 전용 팩토리로 고정한다.
        this.restClient = RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RagChunk> retrieve(String query, String drugId, int topK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        if (drugId != null && !drugId.isBlank()) {
            body.put("drug_id", drugId);
        }
        body.put("top_k", topK);

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/retrieve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return List.of();
            }
            Object chunks = response.get("chunks");
            if (!(chunks instanceof List<?> list)) {
                return List.of();
            }

            List<RagChunk> out = new ArrayList<>();
            for (Object c : list) {
                if (c instanceof Map<?, ?> m) {
                    out.add(new RagChunk(
                            str(m.get("text")),
                            str(m.get("drug_name")),
                            str(m.get("section")),
                            str(m.get("source")),
                            dbl(m.get("similarity"))
                    ));
                }
            }
            return out;

        } catch (Exception e) {
            log.warn("RAG /retrieve 호출 실패, 앵커링으로 폴백합니다: {}", e.getMessage());
            return List.of();
        }
    }

    private static String str(Object v) {
        return v == null ? "" : v.toString();
    }

    private static double dbl(Object v) {
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }
}
