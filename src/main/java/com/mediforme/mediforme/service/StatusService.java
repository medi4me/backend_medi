package com.mediforme.mediforme.service;

import com.mediforme.mediforme.dto.object.StatusDto;
import com.mediforme.mediforme.dto.object.StatusSummaryDto;

import java.time.LocalDate;
import java.util.List;

public interface StatusService {

    StatusDto saveStatus(StatusDto statusDto);
    List<StatusDto> getAllStatuses();
    StatusDto getStatusById(Long id);
    void deleteStatus(Long id);
    StatusDto getStatusByDate(LocalDate date);
    StatusDto updateStatusByDate(LocalDate date, StatusDto statusDto);
    List<StatusSummaryDto> getStatusSummaryForWeek(LocalDate startDate, LocalDate endDate);
}
