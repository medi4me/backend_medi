package com.mediforme.mediforme.search.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.external.client.RxNormClient;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RxNorm approximateTerm 기반 어댑터
 * - OCR 오타 복원 용도 — 이름 후보만 제공 (imageUrl 없음)
 * - 동일 rxcui 중복 제거
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RxNormMedicineSearchAdapter implements MedicineSearchPort {

    private static final String SOURCE = "RxNorm";

    private final RxNormClient client;

    @Override
    public String name() { return SOURCE; }

    @Override
    public List<MedicineSearchItemDto> searchByName(String itemName) {
        try {
            List<Map<String, Object>> candidates = client.fetchApproximateCandidates(itemName);
            if (candidates.isEmpty()) return Collections.emptyList();

            Set<String> seen = new LinkedHashSet<>();
            List<MedicineSearchItemDto> list = new ArrayList<>();

            for (Map<String, Object> c : candidates) {
                String name = stringValue(c.get("name"));
                if (name == null || name.isBlank()) continue;

                String rxcui = stringValue(c.get("rxcui"));
                String dedupKey = rxcui != null ? rxcui : name;
                if (!seen.add(dedupKey)) continue;

                list.add(MedicineSearchItemDto.builder()
                    .name(name)
                    .imageUrl(null)
                    .source(SOURCE)
                    .build());
            }
            return list;

        } catch (Exception e) {
            log.warn("RxNorm search failed. itemName={}", itemName, e);
            return Collections.emptyList();
        }
    }

    private String stringValue(Object v) {
        return v == null ? null : String.valueOf(v).trim();
    }
}
