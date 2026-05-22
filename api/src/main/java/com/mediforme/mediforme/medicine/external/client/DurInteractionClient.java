package com.mediforme.mediforme.medicine.external.client;

import com.mediforme.mediforme.medicine.external.DurApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 식약처 DUR 병용금기 API 클라이언트 (data.go.kr 15059486)
 *
 * 기존 MfdsMedicineClient 와 같은 data.go.kr 응답 envelope(body.items) 패턴을 따른다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DurInteractionClient {

    private static final int CONNECT_TIMEOUT_MS = 2000;
    // 병용금기가 많은 약(예: 와파린)은 응답이 커서 여유 있는 read timeout 필요
    private static final int READ_TIMEOUT_MS = 6000;
    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_NUM_OF_ROWS = 100;

    private final DurApiConfig durApiConfig;

    /**
     * 품목명으로 병용금기 목록 조회. body.items 배열 반환 (결과 없으면 null)
     */
    public JSONArray fetchUsjntTabooByName(String itemName) throws IOException, ParseException {
        if (itemName == null || itemName.isBlank()) {
            return null;
        }

        String urlStr = buildUrl(itemName);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);

        int status = conn.getResponseCode();
        InputStream stream = (status >= 200 && status < 300)
            ? conn.getInputStream()
            : conn.getErrorStream();

        String responseBody;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            responseBody = sb.toString();
        } finally {
            conn.disconnect();
        }

        if (status < 200 || status >= 300) {
            log.warn("DUR API call failed. status={}, body={}", status, responseBody);
            throw new IOException("DUR API error. status=" + status);
        }

        if (responseBody == null || responseBody.isBlank()) return null;

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseBody);

        JSONObject body = (JSONObject) json.get("body");
        if (body == null) return null;

        return (JSONArray) body.get("items");
    }

    private String buildUrl(String itemName) {
        String encoded = URLEncoder.encode(itemName, StandardCharsets.UTF_8);
        return durApiConfig.getServiceUrl()
            + "?serviceKey=" + durApiConfig.getServiceKey()
            + "&itemName=" + encoded
            + "&pageNo=" + DEFAULT_PAGE_NO
            + "&numOfRows=" + DEFAULT_NUM_OF_ROWS
            + "&type=json";
    }
}
