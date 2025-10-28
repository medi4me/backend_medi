package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.config.ApiConfig;
import com.mediforme.mediforme.domain.Medicine;
import com.mediforme.mediforme.domain.UserMedicine;
import com.mediforme.mediforme.dto.object.MedicineInteractionDto;
import com.mediforme.mediforme.dto.object.OnboardingDto;
import com.mediforme.mediforme.dto.response.MedicineCameraResponseDto;
import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import com.mediforme.mediforme.repository.MedicineRepository;
import com.mediforme.mediforme.repository.UserMedicineRepository;
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
    private final UserMedicineRepository userMedicineRepository;

    // 온보딩 내 사용
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

            // 대소문자 모두 대응 & 기본값 지정
            String image = item.containsKey("itemImage")
                    ? (String) item.get("itemImage")
                    : (String) item.getOrDefault("ITEM_IMAGE", "이미지 없음");

            String name = (String) item.getOrDefault("itemName", "이름 없음");

            list.add(OnboardingDto.builder()
                    .itemName(name)
                    .itemImage(image)
                    .build());
        }


        return OnboardingResponseDto.builder().medicines(list).build();
    }

    // 카메라 약물 인식 내 사용
    @Override
    public List<MedicineCameraResponseDto.MedicineInfoDto> getMedicineInfoSimple(String itemName) throws IOException, ParseException {
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

        List<MedicineCameraResponseDto.MedicineInfoDto> list = new ArrayList<>();

        if (items != null) {
            for (Object o : items) {
                JSONObject item = (JSONObject) o;
                list.add(MedicineCameraResponseDto.MedicineInfoDto.builder()
                        .name((String) item.getOrDefault("itemName", "이름 없음"))
                        .imageUrl((String) item.getOrDefault("itemImage", "이미지 없음"))
                        .benefit((String) item.getOrDefault("efcyQesitm", "효능 정보 없음"))
                        .dosage((String) item.getOrDefault("useMethodQesitm", "복용량 정보 없음"))
                        .drugInteraction((String) item.getOrDefault("intrcQesitm", "상호작용 정보 없음"))
                        .alcoholWarning((String) item.getOrDefault("atpnWarnQesitm", "음주 주의 없음"))
                        .build());
            }
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
                                .map(Medicine::getName)
                                .orElse("알 수 없음"))
                        .component(null)
                        .build())
                .toList();
    }


}
