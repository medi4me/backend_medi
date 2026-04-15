package com.mediforme.mediforme.search.adapter;

import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient.firstNonBlank;
import static com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient.getString;


@Slf4j
@Component
@RequiredArgsConstructor
public class MfdsMedicineSearchAdapter implements MedicineSearchPort {

    private final MfdsMedicineClient mfdsClient;

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
                    .build());
            }
            return list;

        } catch (IOException | ParseException e) {
            log.warn("MFDS search failed. itemName={}", itemName, e);
            throw new CustomApiException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
