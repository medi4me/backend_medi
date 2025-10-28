package com.mediforme.mediforme.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MedicineCameraResponseDto {
    private String recognizedText;              // 카메라로 인식된 텍스트
    private String recognizedMedicine;          // 최종 약 이름
    private List<MedicineInfoDto> medicineInfo;   // 공공 약물 API에서 가져온 약 리스트

    @Getter
    @Builder
    public static class MedicineInfoDto {
        private String name;              // 약 이름
        private String imageUrl;          // 약 이미지
        private String benefit;           // 효능
        private String dosage;            // 복용량
        private String drugInteraction;   // 상호작용
        private String alcoholWarning;    // 음주 주의
    }
}
