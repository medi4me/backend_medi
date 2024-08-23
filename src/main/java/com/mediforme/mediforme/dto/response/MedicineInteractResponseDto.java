package com.mediforme.mediforme.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MedicineInteractResponseDto {
    // 복용 약과의 상호작용 경고 용도
    private String name;
    private String interactionWarnings;
}
