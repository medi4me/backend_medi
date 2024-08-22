package com.mediforme.mediforme.controller;


import com.mediforme.mediforme.dto.OnboardingDto;
import com.mediforme.mediforme.dto.response.MedicineResponseDto;
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
import java.util.List;
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
    public ResponseEntity<List<MedicineResponseDto>> recognizeImage(@RequestParam("file") MultipartFile file) throws IOException, ParseException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 이미지 처리 및 약물 정보 조회
            List<MedicineResponseDto> responseDtoList = medicineCameraService.processImageAndRecognizeMedicine(file);

            // 약물 정보를 추출하여 응답
            if (responseDtoList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(responseDtoList);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
}
