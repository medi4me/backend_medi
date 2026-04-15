package com.mediforme.mediforme.medicine.service.impl;


import com.mediforme.mediforme.medicine.domain.Medicine;
import com.mediforme.mediforme.medicine.domain.UserMedicine;
import com.mediforme.mediforme.medicine.dto.MedicineCameraResponseDto;
import com.mediforme.mediforme.medicine.dto.MedicineInteractionDto;
import com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient;
import com.mediforme.mediforme.medicine.repository.MedicineRepository;
import com.mediforme.mediforme.medicine.repository.UserMedicineRepository;
import com.mediforme.mediforme.medicine.service.MedicineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient.firstNonBlank;
import static com.mediforme.mediforme.medicine.external.client.MfdsMedicineClient.getString;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final UserMedicineRepository userMedicineRepository;
    private final MfdsMedicineClient mfdsMedicineClient;

    /**
     * 카메라 인식 이후 상세 정보까지 포함한 조회
     */
    @Override
    public List<MedicineCameraResponseDto.MedicineInfoDto> getMedicineInfoSimple(String itemName)
        throws IOException, ParseException {

        JSONArray items = mfdsMedicineClient.fetchItemsByName(itemName);
        if (items == null || items.isEmpty()) return Collections.emptyList();

        List<MedicineCameraResponseDto.MedicineInfoDto> list = new ArrayList<>();
        for (Object o : items) {
            JSONObject item = (JSONObject) o;

            list.add(MedicineCameraResponseDto.MedicineInfoDto.builder()
                .name(firstNonBlank(getString(item, "itemName"), getString(item, "ITEM_NAME"), "이름 없음"))
                .imageUrl(firstNonBlank(getString(item, "itemImage"), getString(item, "ITEM_IMAGE"), "이미지 없음"))
                .benefit(firstNonBlank(getString(item, "efcyQesitm"), "효능 정보 없음"))
                .dosage(firstNonBlank(getString(item, "useMethodQesitm"), "복용량 정보 없음"))
                .drugInteraction(firstNonBlank(getString(item, "intrcQesitm"), "상호작용 정보 없음"))
                .alcoholWarning(firstNonBlank(getString(item, "atpnWarnQesitm"), "음주 주의 없음"))
                .build());
        }

        return list;
    }


    @Override
    public List<MedicineInteractionDto> getUserMedicineSummaries(Long userId) {
        List<UserMedicine> list = userMedicineRepository.findByUserId(userId);

        return list.stream()
                .map(um -> MedicineInteractionDto.builder()
                        .userMedicineId(um.getUserMedicineId())
                        .medicineName(medicineRepository.findById(um.getMedicineId())
                                .map(Medicine::getMedicineName)
                                .orElse("알 수 없음"))
                        .component(null)
                        .build())
                .toList();
    }
}
