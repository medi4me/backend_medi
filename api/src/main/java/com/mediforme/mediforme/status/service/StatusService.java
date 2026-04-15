package com.mediforme.mediforme.status.service;

import com.mediforme.mediforme.status.dto.StatusAdminRequestDto;
import com.mediforme.mediforme.status.dto.StatusMeRequestDto;
import com.mediforme.mediforme.status.dto.StatusAdminResponseDto;
import com.mediforme.mediforme.status.dto.StatusSummaryDto;
import com.mediforme.mediforme.status.dto.StatusMeResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface StatusService {

    StatusMeResponseDto saveStatus(Long currentUserId, StatusMeRequestDto requestDto);
    StatusMeResponseDto getStatusByUserAndDate(Long currentUserId, LocalDate date);
    List<StatusMeResponseDto> getStatusesByUser(Long currentUserId);
    List<StatusSummaryDto> getStatusSummaryForWeek(Long currentUserId, LocalDate startDate, LocalDate endDate);
    StatusMeResponseDto updateStatusByDate(Long currentUserId, LocalDate date, StatusMeRequestDto requestDto);
    void deleteStatusById(Long currentUserId, Long statusId);   // 본인 삭제(소유 검증 필수)

    // admin용
    StatusAdminResponseDto getStatusById(Long statusId);
    void deleteStatusAdmin(Long statusId);
}
