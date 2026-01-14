package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.dto.request.StatusAdminRequestDto;
import com.mediforme.mediforme.dto.request.StatusMeRequestDto;
import com.mediforme.mediforme.dto.response.StatusAdminResponseDto;
import com.mediforme.mediforme.dto.object.StatusSummaryDto;
import com.mediforme.mediforme.dto.response.StatusMeResponseDto;
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

    /**
     * 상태 저장 (사용자용)
     */
    @Override
    public StatusMeResponseDto saveStatus(Long currentUserId, StatusMeRequestDto dto) {
        Status entity = Status.builder()
            .defaultStatusCd(dto.getDefaultStatusCd())
            .drinkCd(dto.getDrinkCd())
            .conditionCd(dto.getConditionCd())
            .statusMemo(dto.getStatusMemo())
            .statusDate(dto.getStatusDate())
            .userId(currentUserId)
            .build();

        Status saved = statusRepository.save(entity);
        return statusMapper.toMeResponse(saved);
    }


    /**
     * statusId로 조회 (관리자/디버그용)
     */
    @Override
    @Transactional(readOnly = true)
    public StatusAdminResponseDto getStatusById(Long statusId) {
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.STATUS_NOT_FOUND));
        return statusMapper.toAdminResponse(status);
    }

    /**
     * 사용자 + 날짜별 조회 (사용자용)
     */
    @Override
    @Transactional(readOnly = true)
    public StatusMeResponseDto getStatusByUserAndDate(Long userId, LocalDate date) {
        Status status = statusRepository.findByUserIdAndStatusDate(userId, date)
            .orElseThrow(() -> new CustomApiException(ErrorCode.STATUS_NOT_FOUND));
        return statusMapper.toMeResponse(status);
    }

    /**
     * 사용자별 전체 상태 조회 (사용자용)
     */
    @Override
    @Transactional(readOnly = true)
    public List<StatusMeResponseDto> getStatusesByUser(Long userId) {
        return statusRepository.findByUserId(userId)
                .stream()
                .map(statusMapper::toMeResponse)
                .collect(Collectors.toList());
    }

    /**
     * 주간 상태 요약 (사용자용)
     */
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

    /**
     * 상태 수정 (날짜 기준 / 사용자용)
     * - userId 기준으로 먼저 찾아서 소유 검증 겸용
     */
    @Override
    public StatusMeResponseDto updateStatusByDate(Long userId, LocalDate date, StatusMeRequestDto dto) {
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
        return statusMapper.toMeResponse(updated);
    }

    /**
     * 상태 삭제 (사용자용)
     * @param currentUserId, statusId
     */
    @Override
    public void deleteStatusById(Long currentUserId, Long statusId) {
        Status status = statusRepository.findByStatusIdAndUserId(statusId, currentUserId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.STATUS_NOT_FOUND));
        statusRepository.delete(status);
    }


    /**
     * 상태 삭제 (관리자/매니저용)
     * - 운영 목적으로 소유 검증 없이 삭제
     */
    @Override
    public void deleteStatusAdmin(Long statusId) {
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