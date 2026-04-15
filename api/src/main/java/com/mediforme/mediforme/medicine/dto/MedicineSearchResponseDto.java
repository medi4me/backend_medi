package com.mediforme.mediforme.medicine.dto;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MedicineSearchResponseDto {
    private List<MedicineSearchItemDto> medicines;
}
