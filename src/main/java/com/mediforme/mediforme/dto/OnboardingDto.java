package com.mediforme.mediforme.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class OnboardingDto {

    @Getter
    @Builder
    public static class OnboardingRequestDto {
        private String itemName;
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
    }
}
