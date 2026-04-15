package com.mediforme.mediforme.search.port;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;

import java.util.List;

/**
 * 약 검색을 위한 도메인 포트
 */
public interface MedicineSearchPort {

    /**
     * 약 이름을 통한 검색
     */
    List<MedicineSearchItemDto> searchByName(String itemName);
}
