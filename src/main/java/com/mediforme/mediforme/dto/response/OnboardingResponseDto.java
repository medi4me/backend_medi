package com.mediforme.mediforme.dto.response;

import com.mediforme.mediforme.dto.object.OnboardingDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OnboardingResponseDto {
    private List<OnboardingDto> medicines;
}
