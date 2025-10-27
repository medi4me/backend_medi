package com.mediforme.mediforme.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OnboardingResponseDto {
    private Long userMedicineId;
    private String itemName;
    private String imageUrl;
    private String description;
    private String benefit;
    private String drugInteraction;

    private Long mealCd;
    private String mealName;

    private Long timeCd;
    private String timeName;

    private Long daysOfWeekCd;
    private String daysOfWeekName;

    private String dosage;
    private boolean isCheck;
    private boolean isAlarm;
}
