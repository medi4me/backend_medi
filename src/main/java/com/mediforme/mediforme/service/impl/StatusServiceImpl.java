package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.dto.request.StatusRequestDto;
import com.mediforme.mediforme.dto.response.StatusResponseDto;
import com.mediforme.mediforme.dto.object.StatusSummaryDto;
import com.mediforme.mediforme.mapper.StatusMapper;
import com.mediforme.mediforme.repository.StatusRepository;
import com.mediforme.mediforme.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;
    private final StatusMapper statusMapper;

    // 상태 저장
    @Override
    public StatusResponseDto saveStatus(StatusRequestDto dto) {
        Status entity = statusMapper.toEntity(dto);
        Status saved = statusRepository.save(entity);
        return statusMapper.toResponse(saved);
    }

    // statusId로 조회
    @Override
    @Transactional(readOnly = true)
    public StatusResponseDto getStatusById(Long statusId) {
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.STATUS_NOT_FOUND));
        return statusMapper.toResponse(status);
    }

    // 사용자 + 날짜별 조회
    @Override
    @Transactional(readOnly = true)
    public StatusResponseDto getStatusByUserAndDate(Long userId, LocalDate date) {
        Status status = statusRepository.findByUserIdAndStatusDate(userId, date)
            .orElseThrow(() -> new CustomApiException(ErrorCode.STATUS_NOT_FOUND));
        return statusMapper.toResponse(status);
    }

    // 사용자별 전체 상태 조회
    @Override
    @Transactional(readOnly = true)
    public List<StatusResponseDto> getStatusesByUser(Long userId) {
        return statusRepository.findByUserId(userId)
                .stream()
                .map(statusMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 주간 상태 요약
    @Override
    @Transactional(readOnly = true)
    public List<StatusSummaryDto> getStatusSummaryForWeek(Long userId, LocalDate start, LocalDate end) {
        return statusRepository.findByUserIdAndStatusDateBetween(userId, start, end)
                .stream()
                .map(status -> {
                    StatusSummaryDto dto = new StatusSummaryDto();
                    dto.setStatus(getDefaultStatusName(status.getDefaultStatusCd()));
                    dto.setDate(status.getStatusDate().toString());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 상태 수정 (날짜 기준)
    @Override
    public StatusResponseDto updateStatusByDate(Long userId, LocalDate date, StatusRequestDto dto) {
        Status existing = statusRepository.findByUserIdAndStatusDate(userId, date)
            .orElseThrow(() -> new CustomApiException(ErrorCode.STATUS_NOT_FOUND));

        existing.updateStatus(
                dto.getDefaultStatusCd(),
                dto.getDrinkCd(),
                dto.getConditionCd(),
                dto.getStatusMemo(),
                dto.getStatusDate(),
                userId
        );

        Status updated = statusRepository.save(existing);
        return statusMapper.toResponse(updated);
    }

    // 상태 삭제
    @Override
    public void deleteStatus(Long statusId) {
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.STATUS_NOT_FOUND));
        statusRepository.delete(status);
    }

    private String getDefaultStatusName(Long code) {
        if (code == null) return "미정";

        return switch (code.intValue()) {
            case 1 -> "좋음";
            case 2 -> "보통";
            case 3 -> "나쁨";
            default -> "미정";
        };
    }
}