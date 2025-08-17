package com.mediforme.mediforme.controller;

import com.google.protobuf.Api;
import com.google.type.DateTime;
import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.dto.object.StatusDto;
import com.mediforme.mediforme.dto.object.StatusSummaryDto;
import com.mediforme.mediforme.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Path;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "상태 API", description = "상태 관련 API")
@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
@Validated

public class StatusController {

    @Autowired
    private StatusService statusService;

    @Operation(summary = "상태 추가", description = "상태 추가")
    @PostMapping
   public ResponseEntity<ApiResponse<StatusDto>> createStautus(@Valid @RequestBody StatusDto statusDto){
        StatusDto saved = statusService.saveStatus(statusDto);
        return ResponseEntity.ok(ApiResponse.onSuccess(saved));
    }

    @Operation(summary = "모든 상태 조회", description = "상태 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<StatusDto>>> getAllStatus() {
        return ResponseEntity.ok(ApiResponse.onSuccess(statusService.getAllStatuses()));
    }

    @Operation(summary = "ID로 상태 조회", description = "ID로 특정 상태를 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StatusDto>> getStatusById(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.onSuccess(statusService.getStatusById(id)));
    }

    @Operation(summary = "상태 삭제", description = "상태 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatus(@PathVariable Long id){
        statusService.deleteStatus(id);
        return ResponseEntity.noContent().build(); // 204

    }


    @GetMapping("/date/{date}")
   public ResponseEntity<ApiResponse<StatusDto>> getStatusByDate(
           @PathVariable @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date) {
               return ResponseEntity.ok(ApiResponse.onSuccess(statusService.getStatusByDate(date)));

    }


    @GetMapping("/week-summary")
   public ResponseEntity<ApiResponse<List<StatusSummaryDto>>> getStatusSummaryForWeek(
           @RequestParam @DateTimeFormat ( iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
           @RequestParam @DateTimeFormat (iso = DateTimeFormat.ISO.DATE)LocalDate endDate){

               if (endDate.isBefore(startDate)) {
                   throw new IllegalArgumentException("");

               }
               return ResponseEntity.ok(ApiResponse.onSuccess(statusService.getStatusSummaryForWeek(startDate,endDate)));

    }



    @Operation(summary = "날짜로 상태 수정", description = "특정 날짜로 상태를 수정")
    @PutMapping("/date/{date}")
    public ResponseEntity<ApiResponse<StatusDto>> UpdateStatusByDate(
            @PathVariable @DateTimeFormat(iso= DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody StatusDto statusDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(statusService.updateStatusByDate(date, statusDto)));

    }
    //
}

