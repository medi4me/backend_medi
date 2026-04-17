package com.mediforme.mediforme.search.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.external.client.FdaDrugLabelClient;
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
class FdaMedicineSearchAdapterTest {

    @Mock private FdaDrugLabelClient client;

    @InjectMocks private FdaMedicineSearchAdapter adapter;

    @Test
    @DisplayName("brand_name 우선으로 DTO 매핑, source=FDA")
    void brandNamePriority() {
        given(client.fetchOpenFdaByName("tylenol")).willReturn(List.of(
            Map.of(
                "brand_name", List.of("TYLENOL"),
                "generic_name", List.of("ACETAMINOPHEN")
            )
        ));

        List<MedicineSearchItemDto> result = adapter.searchByName("tylenol");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("TYLENOL");
        assertThat(result.get(0).getSource()).isEqualTo("FDA");
        assertThat(result.get(0).getImageUrl()).isNull();
    }

    @Test
    @DisplayName("brand_name 없으면 generic_name 으로 fallback")
    void fallbackToGenericName() {
        given(client.fetchOpenFdaByName("acetaminophen")).willReturn(List.of(
            Map.of("generic_name", List.of("ACETAMINOPHEN"))
        ));

        List<MedicineSearchItemDto> result = adapter.searchByName("acetaminophen");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ACETAMINOPHEN");
    }

    @Test
    @DisplayName("이름이 하나도 없는 결과는 스킵")
    void skipEntriesWithoutName() {
        given(client.fetchOpenFdaByName("unknown")).willReturn(List.of(
            Map.of("manufacturer_name", List.of("ACME"))
        ));

        List<MedicineSearchItemDto> result = adapter.searchByName("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("클라이언트가 빈 리스트 반환하면 빈 결과")
    void clientReturnsEmpty() {
        given(client.fetchOpenFdaByName("없는약")).willReturn(Collections.emptyList());

        assertThat(adapter.searchByName("없는약")).isEmpty();
    }

    @Test
    @DisplayName("클라이언트 예외 시 빈 리스트로 격리")
    void clientThrows_returnsEmpty() {
        given(client.fetchOpenFdaByName(anyString()))
            .willThrow(new RuntimeException("network down"));

        assertThat(adapter.searchByName("tylenol")).isEmpty();
    }
}
