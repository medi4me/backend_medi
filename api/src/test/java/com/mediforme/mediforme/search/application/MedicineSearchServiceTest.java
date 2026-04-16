package com.mediforme.mediforme.search.application;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.dto.MedicineSearchResponseDto;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MedicineSearchServiceTest {

    @Mock
    private MedicineSearchPort port;

    @InjectMocks
    private MedicineSearchService service;

    @Test
    @DisplayName("포트가 빈 리스트 반환 시 빈 응답 DTO 로 래핑")
    void searchByName_emptyResult_returnsEmptyWrappedResponse() {
        given(port.searchByName("없는약")).willReturn(Collections.emptyList());

        MedicineSearchResponseDto response = service.searchByName("없는약");

        assertThat(response).isNotNull();
        assertThat(response.getMedicines()).isEmpty();
    }

    @Test
    @DisplayName("포트가 여러 항목 반환 시 순서 유지하여 그대로 래핑")
    void searchByName_multipleItems_wrapsInOrder() {
        List<MedicineSearchItemDto> items = List.of(
            MedicineSearchItemDto.builder().name("타이레놀").imageUrl("url1").build(),
            MedicineSearchItemDto.builder().name("타이레놀펜").imageUrl("url2").build(),
            MedicineSearchItemDto.builder().name("타이레놀콜드").imageUrl("url3").build()
        );
        given(port.searchByName("타이레")).willReturn(items);

        MedicineSearchResponseDto response = service.searchByName("타이레");

        assertThat(response.getMedicines())
            .hasSize(3)
            .containsExactlyElementsOf(items);
    }

    @Test
    @DisplayName("포트에서 예외 발생 시 유스케이스가 swallow 없이 그대로 전파")
    void searchByName_portThrows_propagatesException() {
        RuntimeException boom = new RuntimeException("adapter failed");
        given(port.searchByName("타이레놀")).willThrow(boom);

        assertThatThrownBy(() -> service.searchByName("타이레놀"))
            .isSameAs(boom);
    }
}
