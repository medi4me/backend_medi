package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.dto.response.MedicineInteractResponseDto;
import com.mediforme.mediforme.service.MedicineInteractionService;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
/*@RequestMapping("/api/medicine")*/
public class MedicineInteractionController {
    private final MedicineInteractionService medicineInteractionService;

    @Autowired
    public MedicineInteractionController(MedicineInteractionService medicineInteractionService) {
        this.medicineInteractionService = medicineInteractionService;
    }

    // 사용자 복용 약물과 새로운 약물의 상호작용 체크
    @PostMapping("/interactions/check")
    public List<String> checkDrugInteractions(@RequestParam Long memberId, @RequestParam String newMedication) throws IOException, ParseException {
        return medicineInteractionService.checkDrugInteractions(memberId, newMedication);
    }

    // 특정 약물 이름으로 상호작용 정보 조회
    @GetMapping("/interactionsinfo")
    public List<MedicineInteractResponseDto> getMedicineInfo(@RequestParam String medicineName) {
        return medicineInteractionService.getMedicineInteractionInfoByName(medicineName);
    }
}
