package com.mediforme.mediforme.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicineIngredientDto {
    private String name;                            // 약 이름
    private String componentName;                   // 성분
    private String amount;                          // 함량
}

