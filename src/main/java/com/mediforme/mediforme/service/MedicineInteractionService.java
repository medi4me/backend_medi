package com.mediforme.mediforme.service;

import com.mediforme.mediforme.dto.response.MedicineInteractResponseDto;
import com.mediforme.mediforme.dto.OnboardingDto;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicineInteractionService {
    private final MedicineService medicineService;

    public MedicineInteractionService(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    public List<String> checkDrugInteractions(Long memberId, String newMedication) throws IOException, ParseException {
        List<String> interactions = new ArrayList<>();

        // 현재 사용자가 복용 중인 약물 목록을 가져옴
        OnboardingDto.OnboardingResponseDto userMedicines = medicineService.getUserMedicines();

        // 사용자가 복용 중인 각 약물을 순회
        for (OnboardingDto.MedicineInfoDto userMed : userMedicines.getMedicines()) {
            // 각 약물에 대한 상호작용 정보를 가져옴
            List<MedicineInteractResponseDto> medicineInfo = getMedicineInteractionInfoByName(userMed.getItemName());

            for (MedicineInteractResponseDto medInfo : medicineInfo) {
                // 각 약물의 상호작용 필드(intrcQesitm)를 확인
                if (medInfo.getInteractionWarnings().contains(newMedication)) {
                    interactions.add(userMed.getItemName() + "과(와) " + newMedication + "는 같이 복용하면 안됩니다!: " + medInfo.getInteractionWarnings());
                }
            }
        }
        return interactions;
    }

    // [MedicineInteraction 사용] 약물의 이름을 기반으로 약물 정보를 반환
    public List<MedicineInteractResponseDto> getMedicineInteractionInfoByName(String medicineName) {
        // 공공 데이터 API나 데이터베이스를 호출하여 약물 정보를 가져오는 로직을 추가 예정.

        List<MedicineInteractResponseDto> medicineList = new ArrayList<>();

        if (medicineName.equals("타이레놀")) {
            MedicineInteractResponseDto medicineResponse = MedicineInteractResponseDto.builder()
                    .name("타이레놀")
                    .interactionWarnings("아세트아미노펜, 와파린")
                    .build();

            medicineList.add(medicineResponse);
        }
        // 추후 더 많은 약물 정보 추가 예정.

        return medicineList;
    }
}
