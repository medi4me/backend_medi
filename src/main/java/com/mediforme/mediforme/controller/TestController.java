package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.service.MedicineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
public class TestController {
    private final MedicineService medicineService;

    public TestController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    // 기본 호출
    @GetMapping("/api/medi")
    public String callApi() throws IOException {
        return medicineService.getDefaultMedicineInfo(); // 기본값 호출
    }

    // 약 이름으로 검색
    @GetMapping("/api/medi/itemName")
    public String getMedicineByItemName(@RequestParam String itemName) throws IOException {
        return medicineService.getMedicineInfoByName(itemName);
    }

}
