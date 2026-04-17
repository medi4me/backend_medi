package com.mediforme.mediforme.search.application;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.dto.MedicineSearchResponseDto;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import com.mediforme.mediforme.search.support.JaroWinklerSimilarity;
import com.mediforme.mediforme.search.support.SearchMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MedicineSearchServiceTest {

    @Mock private MedicineSearchPort portA;
    @Mock private MedicineSearchPort portB;

    private MeterRegistry registry;
    private MedicineSearchService service;

    @BeforeEach
    void setup() {
        registry = new SimpleMeterRegistry();
        SearchMetrics metrics = new SearchMetrics(registry);
        service = new MedicineSearchService(
            List.of(portA, portB),
            new JaroWinklerSimilarity(),
            Runnable::run,  // 동기 실행 executor
            metrics
        );
        lenient().when(portA.name()).thenReturn("A");
        lenient().when(portB.name()).thenReturn("B");
    }

    @Test
    @DisplayName("모든 포트가 빈 리스트면 빈 응답")
    void allEmpty_returnsEmpty() {
        given(portA.searchByName(anyString())).willReturn(Collections.emptyList());
        given(portB.searchByName(anyString())).willReturn(Collections.emptyList());

        MedicineSearchResponseDto result = service.searchByName("없는약");

        assertThat(result.getMedicines()).isEmpty();
    }

    @Test
    @DisplayName("여러 포트 결과 병합 후 쿼리 유사도 내림차순 정렬")
    void mergeAndSortBySimilarity() {
        given(portA.searchByName("타이레놀")).willReturn(List.of(
            MedicineSearchItemDto.builder().name("타이레놀펜").source("MFDS").build()
        ));
        given(portB.searchByName("타이레놀")).willReturn(List.of(
            MedicineSearchItemDto.builder().name("타이레놀").source("FDA").build()
        ));

        MedicineSearchResponseDto result = service.searchByName("타이레놀");

        assertThat(result.getMedicines())
            .extracting(MedicineSearchItemDto::getName)
            .containsExactly("타이레놀", "타이레놀펜");
    }

    @Test
    @DisplayName("정규화된 이름이 같으면 중복 제거")
    void dedupByNormalizedName() {
        given(portA.searchByName("타이레놀")).willReturn(List.of(
            MedicineSearchItemDto.builder().name("타이레놀 500mg").source("MFDS").build()
        ));
        given(portB.searchByName("타이레놀")).willReturn(List.of(
            MedicineSearchItemDto.builder().name("타이레놀").source("FDA").build()
        ));

        MedicineSearchResponseDto result = service.searchByName("타이레놀");

        assertThat(result.getMedicines()).hasSize(1);
    }

    @Test
    @DisplayName("한 어댑터가 예외를 던져도 다른 어댑터 결과는 살아남는다")
    void adapterFailureIsolated() {
        given(portA.searchByName(anyString())).willThrow(new RuntimeException("down"));
        given(portB.searchByName(anyString())).willReturn(List.of(
            MedicineSearchItemDto.builder().name("타이레놀").source("FDA").build()
        ));

        MedicineSearchResponseDto result = service.searchByName("타이레놀");

        assertThat(result.getMedicines())
            .hasSize(1)
            .extracting(MedicineSearchItemDto::getName)
            .containsExactly("타이레놀");
    }

    @Test
    @DisplayName("포트가 null 리턴해도 NPE 없이 빈 결과 처리")
    void nullResult_handledSafely() {
        given(portA.searchByName(anyString())).willReturn(null);
        given(portB.searchByName(anyString())).willReturn(Collections.emptyList());

        MedicineSearchResponseDto result = service.searchByName("x");

        assertThat(result.getMedicines()).isEmpty();
    }

    @Test
    @DisplayName("성공 호출은 SUCCESS outcome 메트릭 기록")
    void metric_successOutcome() {
        given(portA.searchByName(anyString())).willReturn(List.of(
            MedicineSearchItemDto.builder().name("타이레놀").build()
        ));
        given(portB.searchByName(anyString())).willReturn(Collections.emptyList());

        service.searchByName("타이레놀");

        assertThat(registry.find("search.adapter.duration")
            .tag("adapter", "A").tag("outcome", "success").timer().count())
            .isEqualTo(1);
        assertThat(registry.find("search.adapter.duration")
            .tag("adapter", "B").tag("outcome", "success").timer().count())
            .isEqualTo(1);
    }

    @Test
    @DisplayName("예외 호출은 FAILURE outcome 메트릭 기록")
    void metric_failureOutcome() {
        given(portA.searchByName(anyString())).willThrow(new RuntimeException("down"));
        given(portB.searchByName(anyString())).willReturn(Collections.emptyList());

        service.searchByName("x");

        assertThat(registry.find("search.adapter.duration")
            .tag("adapter", "A").tag("outcome", "failure").timer().count())
            .isEqualTo(1);
    }

    @Test
    @DisplayName("소스 히트는 결과를 기여한 어댑터만 기록")
    void metric_sourceHit_onlyContributing() {
        given(portA.searchByName(anyString())).willReturn(List.of(
            MedicineSearchItemDto.builder().name("타이레놀").build()
        ));
        given(portB.searchByName(anyString())).willReturn(Collections.emptyList());

        service.searchByName("타이레놀");

        assertThat(registry.find("search.query.sources_hit")
            .tag("adapter", "A").counter().count()).isEqualTo(1.0);
        assertThat(registry.find("search.query.sources_hit")
            .tag("adapter", "B").counter()).isNull();
    }

    @Test
    @DisplayName("쿼리 총계는 결과 유무에 따라 분기")
    void metric_queryHasResultsTag() {
        given(portA.searchByName(anyString())).willReturn(List.of(
            MedicineSearchItemDto.builder().name("타이레놀").build()
        ));
        given(portB.searchByName(anyString())).willReturn(Collections.emptyList());
        service.searchByName("타이레놀");

        given(portA.searchByName(anyString())).willReturn(Collections.emptyList());
        service.searchByName("없는약");

        assertThat(registry.find("search.query.total")
            .tag("has_results", "true").counter().count()).isEqualTo(1.0);
        assertThat(registry.find("search.query.total")
            .tag("has_results", "false").counter().count()).isEqualTo(1.0);
    }
}
