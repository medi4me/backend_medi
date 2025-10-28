package com.mediforme.mediforme.service;

import com.mediforme.mediforme.dto.request.StatusRequestDto;
import com.mediforme.mediforme.dto.response.StatusResponseDto;
import com.mediforme.mediforme.dto.object.StatusSummaryDto;

import java.time.LocalDate;
import java.util.List;

public interface StatusService {

    StatusResponseDto saveStatus(StatusRequestDto requestDto);

    StatusResponseDto getStatusById(Long statusId);

    StatusResponseDto getStatusByUserAndDate(Long userId, LocalDate date);

    List<StatusResponseDto> getStatusesByUser(Long userId);

    List<StatusSummaryDto> getStatusSummaryForWeek(Long userId, LocalDate startDate, LocalDate endDate);

    StatusResponseDto updateStatusByDate(Long userId, LocalDate date, StatusRequestDto requestDto);

    void deleteStatus(Long id);
}
