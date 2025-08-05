package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.dto.object.StatusDto;
import com.mediforme.mediforme.dto.object.StatusSummaryDto;
import com.mediforme.mediforme.repository.StatusRepository;
import com.mediforme.mediforme.service.StatusService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;

    @Override
    public StatusDto saveStatus(StatusDto statusDto) {
        Status status = toEntity(statusDto);
        Status savedStatus = statusRepository.save(status);
        return toDto(savedStatus);
    }

    @Override
    public List<StatusDto> getAllStatuses() {
        return statusRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public StatusDto getStatusById(Long id) {
        return statusRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Status not found"));
    }

    @Override
    public void deleteStatus(Long id) {
        statusRepository.deleteById(id);
    }

    @Override
    public StatusDto getStatusByDate(LocalDate date) {
        return statusRepository.findByDate(date)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    public StatusDto updateStatusByDate(LocalDate date, StatusDto statusDto) {
        Status existing = statusRepository.findByDate(date)
                .orElseThrow(() -> new EntityNotFoundException("Status not found for the date: " + date));

        existing.setStatus(statusDto.getStatus());
        existing.setDrink(statusDto.getDrink());
        existing.setStatusCondition(statusDto.getStatusCondition());
        existing.setMemo(statusDto.getMemo());
        existing.setDate(statusDto.getDate());

        Status updated = statusRepository.save(existing);
        return toDto(updated);
    }

    @Override
    public List<StatusSummaryDto> getStatusSummaryForWeek(LocalDate startDate, LocalDate endDate) {
        List<Status> statuses = statusRepository.findByDateBetween(startDate, endDate);
        return statuses.stream().map(status -> {
            StatusSummaryDto dto = new StatusSummaryDto();
            dto.setDate(status.getDate().toString());
            dto.setStatus(status.getStatus().toString());
            return dto;
        }).collect(Collectors.toList());
    }

    private Status toEntity(StatusDto statusDto) {
        return Status.builder()
                .status(statusDto.getStatus())
                .drink(statusDto.getDrink())
                .statusCondition(statusDto.getStatusCondition())
                .memo(statusDto.getMemo())
                .date(statusDto.getDate())
                .build();
    }

    private StatusDto toDto(Status status) {
        StatusDto dto = new StatusDto();
        dto.setStatus(status.getStatus());
        dto.setDrink(status.getDrink());
        dto.setStatusCondition(status.getStatusCondition());
        dto.setMemo(status.getMemo());
        dto.setDate(status.getDate());
        return dto;
    }
}
