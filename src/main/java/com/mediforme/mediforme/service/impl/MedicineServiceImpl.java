package com.mediforme.mediforme.service.impl;


import com.mediforme.mediforme.config.ApiConfig;
import com.mediforme.mediforme.domain.Medicine;
import com.mediforme.mediforme.domain.UserMedicine;
import com.mediforme.mediforme.dto.object.MedicineInteractionDto;
import com.mediforme.mediforme.dto.object.MedicineSearchItemDto;
import com.mediforme.mediforme.dto.object.OnboardingDto;
import com.mediforme.mediforme.dto.response.MedicineCameraResponseDto;
import com.mediforme.mediforme.dto.response.MedicineSearchResponseDto;
import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import com.mediforme.mediforme.repository.MedicineRepository;
import com.mediforme.mediforme.repository.UserMedicineRepository;
import com.mediforme.mediforme.service.MedicineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private static final int CONNECT_TIMEOUT_MS = 2000;
    private static final int READ_TIMEOUT_MS = 3000;
    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_NUM_OF_ROWS = 10;

    private final MedicineRepository medicineRepository;
    private final ApiConfig apiConfig;
    private final UserMedicineRepository userMedicineRepository;

    /**
     * (온보딩용) 약 이름, 이미지 위주만 내려주는 검색
     */
    @Override
    public MedicineSearchResponseDto getMedicineInfoByName(String itemName) throws IOException, ParseException {
        JSONArray items = fetchPublicApiItems(itemName);

        if (items == null || items.isEmpty()) {
            return MedicineSearchResponseDto.builder()
                .medicines(Collections.emptyList())
                .build();
        }

        List<MedicineSearchItemDto> list = new ArrayList<>();
        for (Object o : items) {
            JSONObject item = (JSONObject) o;

            // 공공 API에서 키가 혼용될 수 있어 다중 키 접근
            String name = firstNonBlank(
                getString(item, "itemName"),
                getString(item, "ITEM_NAME"),
                "이름 없음"
            );

            String imageurl = firstNonBlank(
                getString(item, "itemImage"),
                getString(item, "ITEM_IMAGE"),
                "이미지 없음"
            );

            list.add(MedicineSearchItemDto.builder()
                .name(name)
                .imageUrl(imageurl)
                .build());
        }

        return MedicineSearchResponseDto.builder()
            .medicines(list)
            .build();
    }

    /**
     * 카메라 인식 이후 상세 정보까지 포함한 조회
     */
    @Override
    public List<MedicineCameraResponseDto.MedicineInfoDto> getMedicineInfoSimple(String itemName)
        throws IOException, ParseException {

        JSONArray items = fetchPublicApiItems(itemName);
        if (items == null || items.isEmpty()) return Collections.emptyList();

        List<MedicineCameraResponseDto.MedicineInfoDto> list = new ArrayList<>();
        for (Object o : items) {
            JSONObject item = (JSONObject) o;

            list.add(MedicineCameraResponseDto.MedicineInfoDto.builder()
                .name(firstNonBlank(getString(item, "itemName"), getString(item, "ITEM_NAME"), "이름 없음"))
                .imageUrl(firstNonBlank(getString(item, "itemImage"), getString(item, "ITEM_IMAGE"), "이미지 없음"))
                .benefit(firstNonBlank(getString(item, "efcyQesitm"), "효능 정보 없음"))
                .dosage(firstNonBlank(getString(item, "useMethodQesitm"), "복용량 정보 없음"))
                .drugInteraction(firstNonBlank(getString(item, "intrcQesitm"), "상호작용 정보 없음"))
                .alcoholWarning(firstNonBlank(getString(item, "atpnWarnQesitm"), "음주 주의 없음"))
                .build());
        }

        return list;
    }


    @Override
    public List<MedicineInteractionDto> getUserMedicineSummaries(Long userId) {
        List<UserMedicine> list = userMedicineRepository.findByUserId(userId);

        return list.stream()
                .map(um -> MedicineInteractionDto.builder()
                        .userMedicineId(um.getUserMedicineId())
                        .medicineName(medicineRepository.findById(um.getMedicineId())
                                .map(Medicine::getMedicineName)
                                .orElse("알 수 없음"))
                        .component(null)
                        .build())
                .toList();
    }

    /**
     * 공공데이터 API 호출, JSON 파싱 후 items 추출
     */
    private JSONArray fetchPublicApiItems(String itemName) throws IOException, ParseException {
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
            // 외부 API 장애/키 문제 로깅
            log.warn("Public API call failed. status={}, body={}", status, responseBody);
            throw new IOException("Public API error. status=" + status);
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

        return apiConfig.getServiceUrl()
            + "?serviceKey=" + apiConfig.getServiceKey()
            + "&itemName=" + encoded
            + "&pageNo=" + DEFAULT_PAGE_NO
            + "&numOfRows=" + DEFAULT_NUM_OF_ROWS
            + "&type=json";
    }

    private static String getString(JSONObject obj, String key) {
        if (obj == null || key == null) return null;
        Object v = obj.get(key);
        return v == null ? null : String.valueOf(v).trim();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
