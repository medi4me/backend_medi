package com.mediforme.mediforme.check.engine;

import com.mediforme.mediforme.check.port.InteractionRulePort;
import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import com.mediforme.mediforme.medicine.dto.MedicineInteractionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("사용자 복용 약이 비어있으면 경고도 없고 규칙 조회도 하지 않는다")
    void evaluate_emptyUserMeds_returnsEmptyAndSkipsLookup() {
        List<String> result = engine.evaluate(Collections.emptyList(), "아스피린");

        assertThat(result).isEmpty();
        verify(rulePort, never()).lookupByMedicineName(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("포트가 빈 규칙 리스트를 반환하면 경고 없음")
    void evaluate_emptyRules_returnsEmpty() {
        MedicineInteractionDto userMed = MedicineInteractionDto.builder()
            .userMedicineId(1L)
            .medicineName("타이레놀")
            .build();
        given(rulePort.lookupByMedicineName("타이레놀")).willReturn(Collections.emptyList());

        List<String> result = engine.evaluate(List.of(userMed), "아스피린");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("interactionWarnings 에 새 약 이름이 포함되면 경고 메시지 생성")
    void evaluate_warningContainsNewMedication_producesAlert() {
        MedicineInteractionDto userMed = MedicineInteractionDto.builder()
            .userMedicineId(1L)
            .medicineName("타이레놀")
            .build();
        MedicineInteractResponseDto rule = MedicineInteractResponseDto.builder()
            .name("타이레놀")
            .interactionWarnings("아세트아미노펜, 와파린")
            .build();
        given(rulePort.lookupByMedicineName("타이레놀")).willReturn(List.of(rule));

        List<String> result = engine.evaluate(List.of(userMed), "와파린");

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
            .contains("타이레놀")
            .contains("와파린")
            .contains("아세트아미노펜, 와파린");
    }

    @Test
    @DisplayName("여러 약 × 여러 규칙 조합에서 매칭된 건수만 정확히 반환")
    void evaluate_multipleMedsAndRules_returnsOnlyMatchedCount() {
        MedicineInteractionDto tylenol = MedicineInteractionDto.builder()
            .userMedicineId(1L).medicineName("타이레놀").build();
        MedicineInteractionDto ibuprofen = MedicineInteractionDto.builder()
            .userMedicineId(2L).medicineName("이부프로펜").build();

        given(rulePort.lookupByMedicineName("타이레놀")).willReturn(List.of(
            MedicineInteractResponseDto.builder()
                .name("타이레놀").interactionWarnings("아스피린").build(),
            MedicineInteractResponseDto.builder()
                .name("타이레놀").interactionWarnings("와파린").build()      // 매칭 안됨
        ));
        given(rulePort.lookupByMedicineName("이부프로펜")).willReturn(List.of(
            MedicineInteractResponseDto.builder()
                .name("이부프로펜").interactionWarnings("아스피린, 와파린").build()
        ));

        List<String> result = engine.evaluate(List.of(tylenol, ibuprofen), "아스피린");

        // 타이레놀 첫 규칙 1건 + 이부프로펜 규칙 1건 = 2건
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("interactionWarnings 가 null 인 규칙은 NPE 없이 스킵")
    void evaluate_nullWarnings_skipsWithoutNpe() {
        MedicineInteractionDto userMed = MedicineInteractionDto.builder()
            .userMedicineId(1L).medicineName("타이레놀").build();
        given(rulePort.lookupByMedicineName("타이레놀")).willReturn(List.of(
            MedicineInteractResponseDto.builder().name("타이레놀").interactionWarnings(null).build()
        ));

        List<String> result = engine.evaluate(List.of(userMed), "와파린");

        assertThat(result).isEmpty();
    }
}
