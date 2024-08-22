package com.mediforme.mediforme.dto;

import com.mediforme.mediforme.domain.enums.UserMedicineMeal;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class OnboardingDto {

    @Getter
    @Builder
    public static class OnboardingRequestDto {
        private String itemName;
        private UserMedicineMeal meal;
        private String time;
        private String dosage;
    }

    @Getter
    @Builder
    public static class OnboardingResponseDto {
        private List<MedicineInfoDto> medicines;
    }

    @Getter
    @Builder
    public static class MedicineInfoDto {
        private String itemName;
        private String itemImage;
        private String description;
        private String benefit;
        private String drugInteraction;
        private UserMedicineMeal meal;
        private String time;
        private String dosage;
        private boolean isCheck;
        private boolean isAlarm;
    }
}
