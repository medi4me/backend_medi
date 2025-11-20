package com.mediforme.mediforme;

import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.dto.object.StatusSummaryDto;
import com.mediforme.mediforme.dto.request.StatusRequestDto;
import com.mediforme.mediforme.dto.response.StatusResponseDto;
import com.mediforme.mediforme.mapper.StatusMapper;
import com.mediforme.mediforme.repository.StatusRepository;
import com.mediforme.mediforme.service.impl.StatusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 서비스 단위 테스트 환경 -> (Mock 기반)
public class StatusServiceImplTest {

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private StatusMapper statusMapper;

    @InjectMocks
    private StatusServiceImpl statusService;

    private StatusRequestDto requestDto;
    private Status statusEntity;
    private Status savedStatus;
    private StatusResponseDto responseDto;

    @BeforeEach
    void setUp() {

        requestDto = StatusRequestDto.builder()
                .userId(10L)
                .defaultStatusCd(1001L)
                .drinkCd(2001L)
                .conditionCd(3001L)
                .statusMemo("테스트 메모")
                .statusDate(LocalDate.of(2025, 11, 10))
                .build();

        statusEntity = Status.builder()
                .userId(10L)
                .defaultStatusCd(1001L)
                .drinkCd(2001L)
                .conditionCd(3001L)
                .statusMemo("테스트 메모")
                .statusDate(LocalDate.of(2025, 11, 10))
                .build();

        savedStatus = Status.builder()
                .statusId(1L)
                .userId(10L)
                .defaultStatusCd(1001L)
                .drinkCd(2001L)
                .conditionCd(3001L)
                .statusMemo("테스트 메모")
                .statusDate(LocalDate.of(2025, 11, 10))
                .build();

        responseDto = StatusResponseDto.builder()
                .statusId(1L)
                .userId(10L)
                .defaultStatusCd(1001L)
                .drinkCd(2001L)
                .conditionCd(3001L)
                .statusMemo("테스트 메모")
                .statusDate(LocalDate.of(2025, 11, 10))
                .build();
    }

    @Test
    @DisplayName("saveStatus() - 상태 저장 로직 테스트 ")
    void saveStatus_success() {
        //  요청 → 엔티티 변환 → 저장 → 응답 변환
        given(statusMapper.toEntity(requestDto)).willReturn(statusEntity);
        given(statusRepository.save(statusEntity)).willReturn(savedStatus);
        given(statusMapper.toResponse(savedStatus)).willReturn(responseDto);

        StatusResponseDto result = statusService.saveStatus(requestDto);

        assertThat(result.getStatusId()).isEqualTo(1L);
        verify(statusRepository).save(statusEntity);
    }

    @Test
    @DisplayName("getStatusById() - ID 조회 테스트")
    void getStatusById_success() {
        given(statusRepository.findById(1L)).willReturn(Optional.of(savedStatus));
        given(statusMapper.toResponse(savedStatus)).willReturn(responseDto);

        StatusResponseDto result = statusService.getStatusById(1L);

        assertThat(result.getStatusId()).isEqualTo(1L);
        verify(statusRepository).findById(1L);
    }

    @Test
    @DisplayName("getStatusByUserAndDate() - user/date 조회 테스트")
    void getStatusByUserAndDate_success() {
        LocalDate date = LocalDate.of(2025, 11, 10);

        given(statusRepository.findByUserIdAndStatusDate(10L, date))
                .willReturn(Optional.of(savedStatus));
        given(statusMapper.toResponse(savedStatus)).willReturn(responseDto);

        StatusResponseDto result = statusService.getStatusByUserAndDate(10L, date);

        assertThat(result.getStatusId()).isEqualTo(1L);
        verify(statusRepository).findByUserIdAndStatusDate(10L, date);
    }

    @Test
    @DisplayName("getStatusesByUser() - 유저 전체 상태 조회")
    void getStatusesByUser_success() {
        // 두 개의 상태가 존재하는 경우를 시뮬레이션
        Status second = Status.builder()
                .statusId(2L)
                .userId(10L)
                .defaultStatusCd(2000L)
                .drinkCd(3000L)
                .conditionCd(4000L)
                .statusMemo("두번째")
                .statusDate(LocalDate.of(2025, 11, 11))
                .build();

        StatusResponseDto secondDto = StatusResponseDto.builder()
                .statusId(2L)
                .userId(10L)
                .defaultStatusCd(2000L)
                .drinkCd(3000L)
                .conditionCd(4000L)
                .statusMemo("두번째")
                .statusDate(LocalDate.of(2025, 11, 11))
                .build();

        given(statusRepository.findByUserId(10L))
                .willReturn(List.of(savedStatus, second));
        given(statusMapper.toResponse(savedStatus)).willReturn(responseDto);
        given(statusMapper.toResponse(second)).willReturn(secondDto);

        List<StatusResponseDto> results = statusService.getStatusesByUser(10L);

        assertThat(results).hasSize(2);
        verify(statusRepository).findByUserId(10L);
    }

    @Test
    @DisplayName("deleteStatus() - 삭제 여부 테스트")
    void deleteStatus_success() {
        // 단순 삭제 흐름 호출만 검증
        statusService.deleteStatus(1L);
        verify(statusRepository).deleteById(1L);
    }

    @Test
    @DisplayName("getStatusSummaryForWeek() - 주간 데이터 조회 테스트")
    void getStatusSummaryForWeek_basic() {
        LocalDate start = LocalDate.of(2025, 11, 10);
        LocalDate end = LocalDate.of(2025, 11, 16);

        given(statusRepository.findByUserIdAndStatusDateBetween(10L, start, end))
                .willReturn(List.of(savedStatus));

        List<StatusSummaryDto> result =
                statusService.getStatusSummaryForWeek(10L, start, end);

        assertThat(result).isNotNull();
        verify(statusRepository).findByUserIdAndStatusDateBetween(10L, start, end);
    }
}
