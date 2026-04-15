package com.mediforme.mediforme.medicine.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MedicineRecognitionResultDto {
    private String fullText;            // OCR 전체 텍스트
    private List<String> candidates;    // 정제된 후보 약 이름 리스트
}
