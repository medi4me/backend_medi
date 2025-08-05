package com.mediforme.mediforme.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicineResponseDto {
    private String name;             // 약 이름
    private String benefit;          // 약 효능 & 설명
    private String drugInteraction;  // 약물 상호작용
    private String imageUrl;         // 약의 이미지 URL
    private String dosage;           // 용법/용량
    private String alcoholWarning;   // 음주 후 복용 가능 여부
}
