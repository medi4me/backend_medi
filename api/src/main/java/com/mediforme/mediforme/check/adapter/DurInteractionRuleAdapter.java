package com.mediforme.mediforme.check.adapter;

import com.mediforme.mediforme.check.port.InteractionRulePort;
import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import com.mediforme.mediforme.medicine.external.client.DurInteractionClient;
import com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 식약처 DUR 병용금기 기반 {@link InteractionRulePort} 구현
 *
 * 품목명으로 병용금기 목록을 조회해, 함께 복용하면 안 되는 약/성분명을 경고 문자열로 모은다.
 * 호출 실패·결과 없음은 빈 리스트로 흡수한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DurInteractionRuleAdapter implements InteractionRulePort {

    private final DurInteractionClient client;

    @Override
    public List<MedicineInteractResponseDto> lookupByMedicineName(String medicineName) {
        try {
            JSONArray items = client.fetchUsjntTabooByName(medicineName);
            if (items == null || items.isEmpty()) {
                return List.of();
            }

            Set<String> contraindicated = new LinkedHashSet<>();
            for (Object o : items) {
                if (!(o instanceof JSONObject it)) continue;
                // 병용금기 상대 성분명 우선 (같은 성분의 제품이 여러 개라 성분 기준이 간결).
                // 성분명이 없으면 품목명 → 영문 성분명 순으로 fallback
                String mix = MfdsMedicineClient.firstNonBlank(
                    MfdsMedicineClient.getString(it, "MIXTURE_INGR_KOR_NAME"),
                    MfdsMedicineClient.getString(it, "MIXTURE_ITEM_NAME"),
                    MfdsMedicineClient.getString(it, "MIXTURE_INGR_ENG_NAME")
                );
                if (mix != null && !mix.isBlank()) {
                    contraindicated.add(mix);
                }
            }

            if (contraindicated.isEmpty()) {
                return List.of();
            }

            return List.of(MedicineInteractResponseDto.builder()
                .name(medicineName)
                .interactionWarnings(String.join(", ", contraindicated))
                .build());

        } catch (Exception e) {
            log.warn("DUR 병용금기 조회 실패 medicineName={}: {}", medicineName, e.getMessage());
            return List.of();
        }
    }
}
