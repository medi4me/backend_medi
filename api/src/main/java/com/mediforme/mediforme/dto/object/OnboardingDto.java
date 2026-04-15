package com.mediforme.mediforme.dto.object;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class OnboardingDto {
    private Long userMedicineId;
    private String itemName;
    private String itemImage;
    private String description;
    private String benefit;
    private String drugInteraction;

    private Long mealCd;          // 공통코드 ID
    private String mealName;      // 공통코드 이름 (조회 시 join, caching 해서 내려줌)

    private Long timeCd;          // 공통코드 ID
    private String timeName;      // 공통코드 이름

    private Long daysOfWeekCd;    // 공통코드 ID
    private String daysOfWeekName;// 공통코드 이름

    private String dosage;
    private boolean isCheck;
    private boolean isAlarm;
}
