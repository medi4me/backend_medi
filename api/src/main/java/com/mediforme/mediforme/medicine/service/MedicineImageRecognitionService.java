package com.mediforme.mediforme.medicine.service;

import com.mediforme.mediforme.medicine.dto.MedicineRecognitionResultDto;
import org.springframework.web.multipart.MultipartFile;

public interface MedicineImageRecognitionService {
    String recognizeMedicineName(MultipartFile imageFile);                              // 가장 유력한 후보 1개만 반환

    MedicineRecognitionResultDto recognizeMedicineCandidates(MultipartFile imageFile);  // 후보 여러 개인 경우, OCR 전체 텍스트 반환
}
