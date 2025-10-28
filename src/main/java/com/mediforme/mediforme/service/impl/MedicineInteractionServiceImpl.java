package com.mediforme.mediforme.service.impl;


import com.mediforme.mediforme.dto.object.MedicineInteractionDto;
import com.mediforme.mediforme.dto.response.MedicineInteractResponseDto;
import com.mediforme.mediforme.service.MedicineService;
import com.mediforme.mediforme.service.MedicineInteractionService;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineInteractionServiceImpl implements MedicineInteractionService {

    private final MedicineService medicineService;

    @Override
    public List<String> checkDrugInteractions(Long userId, String newMedication)
            throws IOException, ParseException {

        List<String> interactions = new ArrayList<>();

        // 사용자가 복용 중인 약 리스트 조회
        List<MedicineInteractionDto> userMedicines = medicineService.getUserMedicineSummaries(userId);

        // 복용 중인 각 약물의 상호작용 데이터 확인
        for (MedicineInteractionDto userMed : userMedicines) {

            List<MedicineInteractResponseDto> interactionList =
                    getMedicineInteractionInfoByName(userMed.getMedicineName());

            for (MedicineInteractResponseDto medInfo : interactionList) {
                if (medInfo.getInteractionWarnings() != null &&
                        medInfo.getInteractionWarnings().contains(newMedication)) {
                    interactions.add(
                            String.format("%s과(와) %s는 함께 복용하면 안 됩니다! (주의: %s)",
                                    userMed.getMedicineName(),
                                    newMedication,
                                    medInfo.getInteractionWarnings())
                    );
                }
            }
        }

        return interactions;
    }

    @Override
    public List<MedicineInteractResponseDto> getMedicineInteractionInfoByName(String medicineName) {
        // 임시 Mock 데이터 (공공 API 연동 예정)
        List<MedicineInteractResponseDto> list = new ArrayList<>();

        if ("타이레놀".equalsIgnoreCase(medicineName)) {
            list.add(MedicineInteractResponseDto.builder()
                    .name("타이레놀")
                    .interactionWarnings("아세트아미노펜, 와파린")
                    .build());
        }

        if ("이부프로펜".equalsIgnoreCase(medicineName)) {
            list.add(MedicineInteractResponseDto.builder()
                    .name("이부프로펜")
                    .interactionWarnings("아스피린, 와파린")
                    .build());
        }

        return list;
    }
}
