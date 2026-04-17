package com.mediforme.mediforme.search.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * openFDA drug label 어댑터
 */
@Slf4j
@Component
public class FdaMedicineSearchAdapter implements MedicineSearchPort {

    @Override
    public List<MedicineSearchItemDto> searchByName(String itemName) {
        return Collections.emptyList();
    }
}
