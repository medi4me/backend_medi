package com.mediforme.mediforme.search.application;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.dto.MedicineSearchResponseDto;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 약 검색 유스케이스
 */
@Service
@RequiredArgsConstructor
public class MedicineSearchService {

    private final MedicineSearchPort medicineSearchPort;

    public MedicineSearchResponseDto searchByName(String name) {
        List<MedicineSearchItemDto> medicines = medicineSearchPort.searchByName(name);
        return MedicineSearchResponseDto.builder()
            .medicines(medicines)
            .build();
    }
}
