package com.mediforme.mediforme.search.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * NIH RxNorm 어댑터
 */
@Slf4j
@Component
public class RxNormMedicineSearchAdapter implements MedicineSearchPort {

    @Override
    public List<MedicineSearchItemDto> searchByName(String itemName) {
        return Collections.emptyList();
    }
}
