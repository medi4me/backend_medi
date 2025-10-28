package com.mediforme.mediforme.service;

import org.springframework.web.multipart.MultipartFile;

public interface MedicineImageRecognitionService {
    String recognizeMedicineName(MultipartFile imageFile); // GCP Vision API (이미지를 통해 약물명 인식하여 반환)
}
