package com.mediforme.mediforme.search.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.external.client.RxNormClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RxNormMedicineSearchAdapterTest {

    @Mock private RxNormClient client;

    @InjectMocks private RxNormMedicineSearchAdapter adapter;

    @Test
    @DisplayName("후보 이름을 DTO 로 매핑, source=RxNorm")
    void mapCandidates() {
        given(client.fetchApproximateCandidates("tyenol")).willReturn(List.of(
            Map.of("rxcui", "202433", "name", "TYLENOL", "score", "82"),
            Map.of("rxcui", "307696", "name", "Tylenol Extra Strength", "score", "60")
        ));

        List<MedicineSearchItemDto> result = adapter.searchByName("tyenol");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MedicineSearchItemDto::getName)
            .containsExactly("TYLENOL", "Tylenol Extra Strength");
        assertThat(result).allMatch(i -> "RxNorm".equals(i.getSource()));
        assertThat(result).allMatch(i -> i.getImageUrl() == null);
    }

    @Test
    @DisplayName("같은 rxcui 는 중복 제거")
    void dedupByRxcui() {
        given(client.fetchApproximateCandidates("tylenol")).willReturn(List.of(
            Map.of("rxcui", "202433", "name", "TYLENOL"),
            Map.of("rxcui", "202433", "name", "TYLENOL")
        ));

        List<MedicineSearchItemDto> result = adapter.searchByName("tylenol");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("이름 없는 후보는 스킵")
    void skipEntriesWithoutName() {
        given(client.fetchApproximateCandidates("x")).willReturn(List.of(
            Map.of("rxcui", "1", "score", "50")
        ));

        assertThat(adapter.searchByName("x")).isEmpty();
    }

    @Test
    @DisplayName("클라이언트 빈 리스트면 빈 결과")
    void emptyClientResult() {
        given(client.fetchApproximateCandidates("없는약")).willReturn(Collections.emptyList());

        assertThat(adapter.searchByName("없는약")).isEmpty();
    }

    @Test
    @DisplayName("클라이언트 예외도 빈 결과로 격리")
    void clientThrows_returnsEmpty() {
        given(client.fetchApproximateCandidates(anyString()))
            .willThrow(new RuntimeException("network down"));

        assertThat(adapter.searchByName("tylenol")).isEmpty();
    }
}
