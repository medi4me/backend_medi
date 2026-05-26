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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, String> contraindicated = lookupContraindicatedIngredients(medicineName);
        if (contraindicated.isEmpty()) {
            return List.of();
        }
        // /info 표시용 문자열: 성분(사유) 포맷으로 결합. 사유가 비면 성분만
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, String> e : contraindicated.entrySet()) {
            entries.add(e.getValue() == null || e.getValue().isBlank()
                ? e.getKey()
                : e.getKey() + "(" + e.getValue() + ")");
        }
        return List.of(MedicineInteractResponseDto.builder()
            .name(medicineName)
            .interactionWarnings(String.join(", ", entries))
            .build());
    }

    @Override
    public Map<String, String> lookupContraindicatedIngredients(String medicineName) {
        try {
            JSONArray items = client.fetchUsjntTabooByName(medicineName);
            if (items == null || items.isEmpty()) {
                return Map.of();
            }

            // 같은 상대 성분에 여러 사유가 등재돼 있을 수 있어 첫 사유를 채택(putIfAbsent)
            Map<String, String> contraindicated = new LinkedHashMap<>();
            for (Object o : items) {
                if (!(o instanceof JSONObject it)) continue;
                // 병용금기 상대 성분명 우선 (같은 성분의 제품이 여러 개라 성분 기준이 간결).
                // 성분명이 없으면 품목명 → 영문 성분명 순으로 fallback
                String mix = MfdsMedicineClient.firstNonBlank(
                    MfdsMedicineClient.getString(it, "MIXTURE_INGR_KOR_NAME"),
                    MfdsMedicineClient.getString(it, "MIXTURE_ITEM_NAME"),
                    MfdsMedicineClient.getString(it, "MIXTURE_INGR_ENG_NAME")
                );
                if (mix == null || mix.isBlank()) continue;
                String reason = MfdsMedicineClient.getString(it, "PROHBT_CONTENT");
                contraindicated.putIfAbsent(mix, reason == null ? "" : reason);
            }
            return contraindicated;

        } catch (Exception e) {
            log.warn("DUR 병용금기 조회 실패 medicineName={}: {}", medicineName, e.getMessage());
            return Map.of();
        }
    }
}
