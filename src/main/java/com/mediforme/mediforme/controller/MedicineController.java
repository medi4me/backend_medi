package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.domain.enums.UserMedicineMeal;
import com.mediforme.mediforme.dto.OnboardingDto;
import com.mediforme.mediforme.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
public class MedicineController {
    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }


    @Operation(summary = "이름으로 약 검색하기")
    @GetMapping("/api/medi/itemName")
    public OnboardingDto.OnboardingResponseDto getMedicineByItemName(@RequestParam(name = "name") String itemName) throws IOException, ParseException {
        return medicineService.getMedicineInfoByName(itemName);
    }

    @Operation(summary = "복용하는 약 추가하기")
    @PostMapping("/api/medi/save")
    public ResponseEntity<OnboardingDto.OnboardingResponseDto> saveMedicineInfo(@RequestParam(name = "memberID") String memberID
                                                                                @RequestParam(name = "name") String itemName,
                                                                                @RequestParam(name = "meal") UserMedicineMeal meal,
                                                                                @RequestParam(name = "time") String time,
                                                                                @RequestParam(name = "dosage") String dosage,
                                                                                @RequestParam(name = "memberId") Long memberId) throws IOException, ParseException{
        OnboardingDto.OnboardingRequestDto requestDto = OnboardingDto.OnboardingRequestDto.builder()
                .memberID(memberID)
                .itemName(itemName)
                .meal(meal)
                .time(time)
                .dosage(dosage)
                .build();

        OnboardingDto.OnboardingResponseDto responseDto = medicineService.saveMedicineInfo(requestDto, memberId);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "사용자가 복용하는 약 조회하기")
    @GetMapping("/list/medicines")
    public ResponseEntity<OnboardingDto.OnboardingResponseDto> getUserMedicines(@RequestParam Long memberId) {
        OnboardingDto.OnboardingResponseDto responseDto = medicineService.getUserMedicines(memberId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "복용하는 약 삭제하기")
    @DeleteMapping("/delete/userMedicine")
    public ResponseEntity<String> deleteUserMedicine(
            @RequestParam Long memberId,
            @RequestParam Long userMedicineId) {

        try {
            medicineService.deleteUserMedicine(memberId, userMedicineId);
            return ResponseEntity.ok("User medicine deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
