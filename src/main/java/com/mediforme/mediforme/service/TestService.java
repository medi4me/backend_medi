package com.mediforme.mediforme.service;

import com.mediforme.mediforme.config.ApiConfig;
import com.mediforme.mediforme.domain.Medicine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;
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
public class TestService {

    private final String SERVICE_URL;
    private final String SERVICE_KEY;

    @Autowired
    public TestService(ApiConfig apiConfig) {
        this.SERVICE_URL = apiConfig.getSERVICE_URL();
        this.SERVICE_KEY = apiConfig.getSERVICE_KEY();
    }

    // 기본 호출
    public String getDefaultMedicineInfo() throws IOException {
        return getMedicineInfo(null);
    }

    // 약 이름으로 검색
    public String getMedicineInfoByName(String itemName) throws IOException {
        return getMedicineInfo(itemName);
    }

    private String getMedicineInfo(String itemName) throws IOException {
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

        return result.toString();
    }
}
