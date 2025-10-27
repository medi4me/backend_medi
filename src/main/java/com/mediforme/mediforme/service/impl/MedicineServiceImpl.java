package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.config.ApiConfig;
import com.mediforme.mediforme.dto.object.OnboardingDto;
import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import com.mediforme.mediforme.repository.MedicineRepository;
import com.mediforme.mediforme.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {
    // 공공 약물 데이터 API를 통해 약 정보 조회
    private final MedicineRepository medicineRepository;
    private final ApiConfig apiConfig;

    @Override
    public OnboardingResponseDto getMedicineInfoByName(String itemName) throws IOException, ParseException {
        StringBuilder result = new StringBuilder();

        String urlStr = apiConfig.getSERVICE_URL() + "?serviceKey=" + apiConfig.getSERVICE_KEY() +
                "&itemName=" + URLEncoder.encode(itemName, "UTF-8") +
                "&pageNo=1&numOfRows=10&type=json";

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) result.append(line);
        } finally {
            conn.disconnect();
        }

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result.toString());
        JSONObject body = (JSONObject) json.get("body");
        JSONArray items = (JSONArray) body.get("items");

        if (items == null) {
            return OnboardingResponseDto.builder().medicines(Collections.emptyList()).build();
        }

        List<OnboardingDto> list = new ArrayList<>();
        for (Object o : items) {
            JSONObject item = (JSONObject) o;
            list.add(OnboardingDto.builder()
                    .itemName((String) item.get("itemName"))
                    .imageUrl((String) item.get("itemImage"))
                    .build());
        }

        return OnboardingResponseDto.builder().medicines(list).build();
    }
}
