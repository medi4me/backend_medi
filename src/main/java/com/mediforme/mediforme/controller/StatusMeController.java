package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.config.security.CurrentUserUtils;
import com.mediforme.mediforme.dto.object.StatusSummaryDto;
import com.mediforme.mediforme.dto.request.StatusMeRequestDto;
import com.mediforme.mediforme.dto.response.StatusMeResponseDto;
import com.mediforme.mediforme.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "상태(Me) API", description = "현재 로그인 사용자의 상태 추가, 조회, 삭제 등 상태 관련 처리를 담당합니다.")
@RestController
@RequestMapping("/users/me/statuses")
public class StatusMeController {

    private StatusService statusService;

    public StatusMeController(StatusService statusService) {
        this.statusService = statusService;
    }

    @PostMapping
    @Operation(summary = "상태 추가/저장", description = "내 상태를 저장합니다.")
    public ApiResponse<StatusMeResponseDto> createStatus(@Valid @RequestBody StatusMeRequestDto requestDto) {
        Long userId = CurrentUserUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(statusService.saveStatus(userId, requestDto));
    }

    @Operation(summary = "날짜별 상태 조회", description = "내 상태를 날짜로 상태 조회합니다.")
    @GetMapping("/{date}")
    public ApiResponse<StatusMeResponseDto> getMyStatusByDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = CurrentUserUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(statusService.getStatusByUserAndDate(userId, date));
    }


    @Operation(summary = "기간 상태 요약", description = "기간 내 내 상태 요약을 조회합니다.")
    @GetMapping("/week-summary")
    public ApiResponse<List<StatusSummaryDto>> getMyWeekSummary(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Long userId = CurrentUserUtils.currentUserIdOrThrow();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return ApiResponse.onSuccess(statusService.getStatusSummaryForWeek(userId, start, end));
    }

    @Operation(summary = "날짜로 상태 수정", description = "내 상태를 특정 날짜 기준으로 수정합니다.")
    @PutMapping("/{date}")
    public ApiResponse<StatusMeResponseDto> updateMyStatusByDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @Valid @RequestBody StatusMeRequestDto dto) {

        Long userId = CurrentUserUtils.currentUserIdOrThrow();
        return ApiResponse.onSuccess(statusService.updateStatusByDate(userId, date, dto));
    }

    @Operation(summary = "내 상태 삭제", description = "현재 로그인한 사용자의 상태를 삭제합니다.")
    @DeleteMapping("/id/{statusId}")
    public ApiResponse<String> deleteMyStatus(@PathVariable Long statusId) {
        Long currentUserId = CurrentUserUtils.currentUserIdOrThrow();
        statusService.deleteStatusById(currentUserId, statusId);
        return ApiResponse.onSuccess("상태가 삭제되었습니다.");
    }
}