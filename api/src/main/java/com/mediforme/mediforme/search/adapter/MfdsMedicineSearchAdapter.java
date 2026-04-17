package com.mediforme.mediforme.search.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient.firstNonBlank;
import static com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient.getString;


@Slf4j
@Component
@RequiredArgsConstructor
public class MfdsMedicineSearchAdapter implements MedicineSearchPort {

    private static final String SOURCE = "MFDS";

    private final MfdsMedicineClient mfdsClient;

    @Override
    public String name() { return SOURCE; }

    @Override
    public List<MedicineSearchItemDto> searchByName(String itemName) {
        try {
            JSONArray items = mfdsClient.fetchItemsByName(itemName);
            if (items == null || items.isEmpty()) {
                return Collections.emptyList();
            }

            List<MedicineSearchItemDto> list = new ArrayList<>();
            for (Object o : items) {
                JSONObject item = (JSONObject) o;
                list.add(MedicineSearchItemDto.builder()
                    .name(firstNonBlank(
                        getString(item, "itemName"),
                        getString(item, "ITEM_NAME"),
                        "이름 없음"))
                    .imageUrl(firstNonBlank(
                        getString(item, "itemImage"),
                        getString(item, "ITEM_IMAGE"),
                        "이미지 없음"))
                    .source(SOURCE)
                    .build());
            }
            return list;

        } catch (Exception e) {
            // 병렬 호출 실패 격리 - 다른 어댑터 결과는 살림
            log.warn("MFDS search failed. itemName={}", itemName, e);
            return Collections.emptyList();
        }
    }
}
