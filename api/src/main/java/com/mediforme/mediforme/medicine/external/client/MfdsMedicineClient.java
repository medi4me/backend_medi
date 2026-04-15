package com.mediforme.mediforme.medicine.external.client;

import com.mediforme.mediforme.medicine.external.MfdsApiConfig;
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
 * 식약처(MFDS) 공공데이터 API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MfdsMedicineClient {

    private static final int CONNECT_TIMEOUT_MS = 2000;
    private static final int READ_TIMEOUT_MS = 3000;
    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_NUM_OF_ROWS = 10;

    private final MfdsApiConfig mfdsApiConfig;

    /**
     * 약 이름으로 MFDS 공공 API 를 호출, body.items 배열을 그대로 반환 (결과 없으면 null 반환)
     */
    public JSONArray fetchItemsByName(String itemName) throws IOException, ParseException {
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
            log.warn("MFDS API call failed. status={}, body={}", status, responseBody);
            throw new IOException("MFDS API error. status=" + status);
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
        return mfdsApiConfig.getServiceUrl()
            + "?serviceKey=" + mfdsApiConfig.getServiceKey()
            + "&itemName=" + encoded
            + "&pageNo=" + DEFAULT_PAGE_NO
            + "&numOfRows=" + DEFAULT_NUM_OF_ROWS
            + "&type=json";
    }

    // MFDS 응답 파싱 공용 유틸
    public static String getString(JSONObject obj, String key) {
        if (obj == null || key == null) return null;
        Object v = obj.get(key);
        return v == null ? null : String.valueOf(v).trim();
    }

    public static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
