package com.mediforme.mediforme.medicine.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class MedicineCameraResponseDto {
    private String recognizedText;                      // 카메라로 인식된 전체 텍스트 (OCR 결과)
    private String recognizedMedicine;                  // 최종 약 이름 (대표 1개)
    private List<MedicineInfoDto> medicineInfo;         // 공공 약물 API에서 가져온 약 리스트 (대표 1개에 대한 조회 결과)

    private List<String> recognizedMedicines;           // 약 이름 후보 리스트 (우선순위 순)
    private List<RecognizedMedicineResultDto> results; // 후보별 조회 결과

    @Getter
    @Builder
    public static class RecognizedMedicineResultDto {
        private String queryName;                       // 후보 문자열
        private List<MedicineInfoDto> medicineInfo;     // 해당 후보로 조회한 결과
    }

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

    // 실패 응답을 한 곳에서 일관되게 만들기 위한 헬퍼
    public static MedicineCameraResponseDto empty(String message) {
        return MedicineCameraResponseDto.builder()
            .recognizedText(message)
            .recognizedMedicine(null)
            .medicineInfo(Collections.emptyList())
            .recognizedMedicines(Collections.emptyList())
            .results(Collections.emptyList())
            .build();
    }
}
