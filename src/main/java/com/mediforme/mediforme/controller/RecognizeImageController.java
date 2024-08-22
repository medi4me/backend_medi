package com.mediforme.mediforme.controller;


import com.mediforme.mediforme.dto.OnboardingDto;
import com.mediforme.mediforme.service.MedicineCameraSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;

@RestController

public class RecognizeImageController {
    private final MedicineCameraSearchService medicineCameraService;

    @Autowired
    public RecognizeImageController(MedicineCameraSearchService medicineCameraService) {
        this.medicineCameraService = medicineCameraService;
    }

    // 카메라 약물 인식
    @PostMapping("/camera")
    public ResponseEntity<Map<String, String>> recognizeImage(@RequestParam("file") MultipartFile file) throws IOException, ParseException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No file selected"));
        }

        try {
            // 이미지 처리 및 약물 정보 조회
            OnboardingDto.OnboardingResponseDto responseDto = medicineCameraService.processImageAndRecognizeMedicine(file);
            // 약물 이름을 추출
            if (responseDto.getMedicines().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            String recognizedMedicineName = responseDto.getMedicines().get(0).getItemName();
            return ResponseEntity.ok(Collections.singletonMap("medicine", recognizedMedicineName));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to process image"));
        }
    }
}
