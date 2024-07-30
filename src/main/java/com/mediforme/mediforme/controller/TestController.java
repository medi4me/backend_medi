package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@RestController
public class TestController {
    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    // 기본 호출
    @GetMapping("/api/medi")
    public String callApi() throws IOException {
        return testService.getDefaultMedicineInfo(); // 기본값 호출
    }

    // 약 이름으로 검색
    @GetMapping("/api/medi/itemName")
    public String getMedicineByItemName(@RequestParam String itemName) throws IOException {
        return testService.getMedicineInfoByName(itemName);
    }

}
