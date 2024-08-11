package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.dto.OnboardingDto;
import com.mediforme.mediforme.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
public class MedicineController {
    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }


    @Operation(summary = "이름으로 약 검색하기")
    @GetMapping("/api/medi/itemName")
    public OnboardingDto.OnboardingResponseDto getMedicineByItemName(@RequestParam String itemName) throws IOException, ParseException {
        return medicineService.getMedicineInfoByName(itemName);
    }

}
