package com.mediforme.mediforme.service;

import com.mediforme.mediforme.config.ApiConfig;
import com.mediforme.mediforme.converter.MedicineConverter;
import com.mediforme.mediforme.dto.OnboardingDto;
import com.mediforme.mediforme.repository.MedicineRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicineService {
    private final MedicineRepository medicineRepository;
    private final MedicineConverter medicineConverter;
    private final String SERVICE_URL;
    private final String SERVICE_KEY;

    @Autowired
    public MedicineService(MedicineRepository medicineRepository,MedicineConverter medicineConverter, ApiConfig apiConfig) {
        this.medicineRepository = medicineRepository;
        this.medicineConverter = medicineConverter;
        this.SERVICE_URL = apiConfig.getSERVICE_URL();
        this.SERVICE_KEY = apiConfig.getSERVICE_KEY();
    }

    // 약 이름으로 검색
    public OnboardingDto.OnboardingResponseDto getMedicineInfoByName(String itemName) throws IOException, ParseException {
        return getMedicineInfo(itemName);
    }


    private OnboardingDto.OnboardingResponseDto getMedicineInfo(String itemName) throws IOException, ParseException {
        StringBuilder result = new StringBuilder();

        StringBuilder urlStr = new StringBuilder(SERVICE_URL + "?");
        urlStr.append("serviceKey=").append(SERVICE_KEY);

        if (itemName != null) {
            urlStr.append("&itemName=").append(URLEncoder.encode(itemName, "UTF-8"));
        }

        urlStr.append("&pageNo=1");
        urlStr.append("&numOfRows=10");
        urlStr.append("&type=json");

        URL url = new URL(urlStr.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))) {
            String returnLine;
            while ((returnLine = br.readLine()) != null) {
                result.append(returnLine).append("\n");
            }
        } finally {
            urlConnection.disconnect();
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result.toString());

        JSONObject jsonBody = (JSONObject) jsonObject.get("body");
        JSONArray jsonItems = (JSONArray) jsonBody.get("items");

        List<OnboardingDto.MedicineInfoDto> medicines = new ArrayList<>();

        // items 배열을 순회하며 각 아이템의 이름과 이미지를 가져옴
        for (Object itemObj : jsonItems) {
            JSONObject item = (JSONObject) itemObj;
            String itemNameValue = (String) item.get("itemName");
            String itemImageValue = (String) item.get("itemImage");

            medicines.add(OnboardingDto.MedicineInfoDto.builder()
                    .itemName(itemNameValue)
                    .itemImage(itemImageValue)
                    .build());
        }

        return OnboardingDto.OnboardingResponseDto.builder()
                .medicines(medicines)
                .build();
    }

}
