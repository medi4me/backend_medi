package com.mediforme.mediforme.search.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.external.client.FdaDrugLabelClient;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * openFDA drug label 기반 어댑터
 * - brand_name, 없으면 generic_name 우선순위로 이름 매핑
 * - imageUrl 은 openFDA 미제공으로 null
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FdaMedicineSearchAdapter implements MedicineSearchPort {

    private static final String SOURCE = "FDA";

    private final FdaDrugLabelClient client;

    @Override
    public String name() { return SOURCE; }

    @Override
    public List<MedicineSearchItemDto> searchByName(String itemName) {
        try {
            List<Map<String, Object>> openfdas = client.fetchOpenFdaByName(itemName);
            if (openfdas.isEmpty()) return Collections.emptyList();

            List<MedicineSearchItemDto> list = new ArrayList<>();
            for (Map<String, Object> openfda : openfdas) {
                String name = firstElement(openfda, "brand_name");
                if (name == null) name = firstElement(openfda, "generic_name");
                if (name == null || name.isBlank()) continue;

                list.add(MedicineSearchItemDto.builder()
                    .name(name)
                    .imageUrl(null)
                    .source(SOURCE)
                    .build());
            }
            return list;

        } catch (Exception e) {
            log.warn("FDA search failed. itemName={}", itemName, e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private String firstElement(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof List<?> l && !l.isEmpty()) {
            Object first = l.get(0);
            return first == null ? null : String.valueOf(first).trim();
        }
        return null;
    }
}
