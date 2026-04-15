package com.mediforme.mediforme.dto.response;

import com.mediforme.mediforme.dto.object.MedicineSearchItemDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MedicineSearchResponseDto {
    private List<MedicineSearchItemDto> medicines;
}
