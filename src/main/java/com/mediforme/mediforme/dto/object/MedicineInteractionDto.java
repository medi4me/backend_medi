package com.mediforme.mediforme.dto.object;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicineInteractionDto {
    private Long userMedicineId;    // 사용자 약 id
    private String medicineName;    // 약 이름
    private String component;       // 주요 성분
}
