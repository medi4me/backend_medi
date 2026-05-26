package com.mediforme.mediforme.check.engine;

import com.mediforme.mediforme.check.port.InteractionRulePort;
import com.mediforme.mediforme.medicine.dto.MedicineInteractionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InteractionEngineTest {

    @Mock
    private InteractionRulePort rulePort;

    @InjectMocks
    private InteractionEngine engine;

    @Test
    @DisplayName("사용자 복용 약이 비어있으면 경고도 없고 조회도 하지 않는다")
    void evaluate_emptyUserMeds_returnsEmptyAndSkipsLookup() {
        List<String> result = engine.evaluate(Collections.emptyList(), "이부프로펜");

        assertThat(result).isEmpty();
        verify(rulePort, never()).lookupContraindicatedIngredients(any());
    }

    @Test
    @DisplayName("새 약의 병용금기 성분이 없으면 경고 없음")
    void evaluate_noContraindication_returnsEmpty() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜")).willReturn(Set.of());
        MedicineInteractionDto med = MedicineInteractionDto.builder()
            .medicineName("타이레놀").component("아세트아미노펜").build();

        assertThat(engine.evaluate(List.of(med), "이부프로펜")).isEmpty();
    }

    @Test
    @DisplayName("기존 약 성분이 새 약의 병용금기에 있으면 경고 생성")
    void evaluate_componentInContraindicated_producesAlert() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜"))
            .willReturn(Set.of("메토트렉세이트", "와파린"));
        MedicineInteractionDto med = MedicineInteractionDto.builder()
            .medicineName("메토잘").component("메토트렉세이트").build();

        List<String> result = engine.evaluate(List.of(med), "이부프로펜");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).contains("메토잘").contains("이부프로펜").contains("병용금기");
    }

    @Test
    @DisplayName("성분 표기 차이(용량·공백)는 정규화로 매칭")
    void evaluate_normalizesComponent() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜")).willReturn(Set.of("메토트렉세이트"));
        MedicineInteractionDto med = MedicineInteractionDto.builder()
            .medicineName("메토잘정").component("메토트렉세이트 2.5mg").build();

        assertThat(engine.evaluate(List.of(med), "이부프로펜")).hasSize(1);
    }

    @Test
    @DisplayName("충돌하지 않는 성분은 경고 없음")
    void evaluate_noConflict_returnsEmpty() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜")).willReturn(Set.of("메토트렉세이트"));
        MedicineInteractionDto med = MedicineInteractionDto.builder()
            .medicineName("타이레놀").component("아세트아미노펜").build();

        assertThat(engine.evaluate(List.of(med), "이부프로펜")).isEmpty();
    }

    @Test
    @DisplayName("component 가 없으면 약 이름으로 매칭")
    void evaluate_fallsBackToMedicineName() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜")).willReturn(Set.of("와파린"));
        MedicineInteractionDto med = MedicineInteractionDto.builder().medicineName("와파린").build();

        assertThat(engine.evaluate(List.of(med), "이부프로펜")).hasSize(1);
    }
}
