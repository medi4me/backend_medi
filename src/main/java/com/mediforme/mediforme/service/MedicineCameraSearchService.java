package com.mediforme.mediforme.service;


import com.mediforme.mediforme.config.ApiConfig;
import com.mediforme.mediforme.converter.MedicineConverter;
import com.mediforme.mediforme.domain.Medicine;
import com.mediforme.mediforme.dto.object.MedicineDto;
import com.mediforme.mediforme.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class MedicineCameraSearchService {

    private final MedicineService medicineService;

    @Autowired
    public MedicineCameraSearchService(MedicineService medicineService) {
        this.medicineService = medicineService;
    }


    // 이미지 처리 및 약물 이름 인식 로직
    public String processImageAndRecognizeMedicine(String imagePath) throws IOException{
        // 이미지 처리 및 약물 이름 인식 로직을 구현 예정
        // \Python 스크립트 호출 또는 AI 모델 사용
        String recognizedMedicineName = recognizeMedicineNameFromImage(imagePath);

        // 인식된 약물 이름을 통해 약물 정보를 조회
        return medicineService.getMedicineInfoByName(recognizedMedicineName);
    }

    private String recognizeMedicineNameFromImage(String imagePath) {
        // AI 모델을 사용하여 이미지를 처리하고 약물 이름을 반환 예정
        return "recogEmg"; // 예시 반환 값 : 인식된 약물 이름
    }
}
