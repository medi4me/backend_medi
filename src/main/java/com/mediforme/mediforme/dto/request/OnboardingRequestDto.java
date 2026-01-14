package com.mediforme.mediforme.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OnboardingRequestDto {
    private String itemName;      // 약 이름 (Medicine)
    private Long mealCd;          // 식사 코드 (before/after meal)
    private Long timeCd;          // 복용 시간 코드 (morning/lunch/dinner)
    private Long daysOfWeekCd;    // 요일 코드
    private String dosage;        // 복용량
}
