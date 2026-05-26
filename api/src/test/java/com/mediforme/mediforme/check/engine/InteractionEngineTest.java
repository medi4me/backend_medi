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
import java.util.Map;

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
        given(rulePort.lookupContraindicatedIngredients("이부프로펜")).willReturn(Map.of());
        MedicineInteractionDto med = MedicineInteractionDto.builder()
            .medicineName("타이레놀").component("아세트아미노펜").build();

        assertThat(engine.evaluate(List.of(med), "이부프로펜")).isEmpty();
    }

    @Test
    @DisplayName("충돌 시 경고 문구에 사유(PROHBT_CONTENT)를 괄호로 부착")
    void evaluate_includesReasonInWarning() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜"))
            .willReturn(Map.of("메토트렉세이트", "혈액학적 독성", "와파린", "출혈 위험"));
        MedicineInteractionDto med = MedicineInteractionDto.builder()
            .medicineName("메토잘").component("메토트렉세이트").build();

        List<String> result = engine.evaluate(List.of(med), "이부프로펜");

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .contains("메토잘")
            .contains("이부프로펜")
            .contains("병용금기입니다(혈액학적 독성)");
    }

    @Test
    @DisplayName("사유가 비어 있으면 경고는 사유 없이 생성")
    void evaluate_emptyReason_warningWithoutSuffix() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜"))
            .willReturn(Map.of("메토트렉세이트", ""));
        MedicineInteractionDto med = MedicineInteractionDto.builder()
            .medicineName("메토잘").component("메토트렉세이트").build();

        List<String> result = engine.evaluate(List.of(med), "이부프로펜");

        assertThat(result).hasSize(1);
        // 사유가 비면 "병용금기입니다(...)." 가 아니라 "병용금기입니다." 로 마침표가 바로 뒤따름
        assertThat(result.get(0)).contains("병용금기입니다.");
    }

    @Test
    @DisplayName("성분 표기 차이(용량·공백)는 정규화로 매칭")
    void evaluate_normalizesComponent() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜"))
            .willReturn(Map.of("메토트렉세이트", "혈액학적 독성"));
        MedicineInteractionDto med = MedicineInteractionDto.builder()
            .medicineName("메토잘정").component("메토트렉세이트 2.5mg").build();

        assertThat(engine.evaluate(List.of(med), "이부프로펜")).hasSize(1);
    }

    @Test
    @DisplayName("충돌하지 않는 성분은 경고 없음")
    void evaluate_noConflict_returnsEmpty() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜"))
            .willReturn(Map.of("메토트렉세이트", "혈액학적 독성"));
        MedicineInteractionDto med = MedicineInteractionDto.builder()
            .medicineName("타이레놀").component("아세트아미노펜").build();

        assertThat(engine.evaluate(List.of(med), "이부프로펜")).isEmpty();
    }

    @Test
    @DisplayName("component 가 없으면 약 이름으로 매칭")
    void evaluate_fallsBackToMedicineName() {
        given(rulePort.lookupContraindicatedIngredients("이부프로펜"))
            .willReturn(Map.of("와파린", "출혈 위험"));
        MedicineInteractionDto med = MedicineInteractionDto.builder().medicineName("와파린").build();

        assertThat(engine.evaluate(List.of(med), "이부프로펜")).hasSize(1);
    }
}
