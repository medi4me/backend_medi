package com.mediforme.mediforme.user.dto;

import com.mediforme.mediforme.user.dto.OnboardingDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OnboardingResponseDto {
    private List<OnboardingDto> medicines;
}
