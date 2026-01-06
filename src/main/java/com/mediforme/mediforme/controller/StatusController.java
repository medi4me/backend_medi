package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.dto.object.StatusSummaryDto;
import com.mediforme.mediforme.dto.request.StatusRequestDto;
import com.mediforme.mediforme.dto.response.StatusResponseDto;
import com.mediforme.mediforme.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "상태 API", description = "상태 추가, 조회, 삭제 등 상태 관련 처리를 담당합니다.")
@RestController
@RequestMapping("/status")
public class StatusController {

    @Autowired
    private StatusService statusService;

    @PostMapping
    @Operation(summary = "상태 추가/저장", description = "상태 추가")
    public StatusResponseDto createStatus(@Valid @RequestBody StatusRequestDto requestDto) {
        return statusService.saveStatus(requestDto);
    }

    @Operation(summary = "상태 id로 상태 조회", description = "(관리자 체크 용도) 상태 id로 특정 상태를 조회합니다.")
    @GetMapping("/{statusId}")
    public StatusResponseDto getStatusById(@PathVariable Long statusId) {
        return statusService.getStatusById(statusId);
    }

    @Operation(summary = "사용자 + 날짜별 상태 조회", description = "특정 사용자와 날짜로 상태 조회합니다.")
    @GetMapping("/user/{userId}/date/{date}")
    public StatusResponseDto getStatusByUserAndDate(@PathVariable Long userId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return statusService.getStatusByUserAndDate(userId, date);
    }

    @Operation(summary = "사용자별 상태 전체 조회", description = "특정 사용자의 모든 상태를 조회합니다.")
    @GetMapping("/user/{userId}")
    public List<StatusResponseDto> getStatusesByUser(@PathVariable Long userId) {
        return statusService.getStatusesByUser(userId);
    }

    @Operation(summary = "주간 상태 요약", description = "특정 기간 동안의 상태 요약을 조회합니다.")
    @GetMapping("/user/{userId}/week-summary")
    public List<StatusSummaryDto> getStatusSummaryForWeek(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return statusService.getStatusSummaryForWeek(userId, start, end);
    }

    @Operation(summary = "날짜로 상태 수정", description = "특정 날짜로 상태를 수정합니다.")
    @PutMapping("/user/{userId}/date/{date}")
    public StatusResponseDto updateStatusByDate(
            @PathVariable Long userId,
            @PathVariable String date,
            @Valid @RequestBody StatusRequestDto dto) {
        LocalDate localDate = LocalDate.parse(date);
        return statusService.updateStatusByDate(userId, localDate, dto);
    }

    @Operation(summary = "상태 삭제", description = "상태 삭제합니다.")
    @DeleteMapping("/{statusId}")
    public void deleteStatus(@PathVariable Long statusId) {
        statusService.deleteStatus(statusId);
    }
}