package com.mediforme.mediforme.check.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import com.mediforme.mediforme.medicine.external.client.DurInteractionClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DurInteractionRuleAdapterTest {

    @Test
    void DUR_병용금기를_경고_문자열로_매핑한다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        JSONArray items = new JSONArray();
        items.add(item("메토트렉세이트"));
        items.add(item("케토롤락트로메타민"));
        when(client.fetchUsjntTabooByName("이부프로펜")).thenReturn(items);

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);
        List<MedicineInteractResponseDto> rules = adapter.lookupByMedicineName("이부프로펜");

        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).getName()).isEqualTo("이부프로펜");
        assertThat(rules.get(0).getInteractionWarnings())
            .contains("메토트렉세이트")
            .contains("케토롤락트로메타민");
    }

    @Test
    void 결과가_없으면_빈_리스트를_반환한다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        when(client.fetchUsjntTabooByName(anyString())).thenReturn(null);

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);

        assertThat(adapter.lookupByMedicineName("존재하지않는약")).isEmpty();
    }

    @Test
    void 호출이_예외를_던져도_빈_리스트로_흡수한다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        when(client.fetchUsjntTabooByName(anyString())).thenThrow(new RuntimeException("network"));

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);

        assertThat(adapter.lookupByMedicineName("이부프로펜")).isEmpty();
    }

    @Test
    void 병용금기_상대_성분_집합을_반환한다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        JSONArray items = new JSONArray();
        items.add(item("메토트렉세이트"));
        items.add(item("와파린"));
        when(client.fetchUsjntTabooByName("이부프로펜")).thenReturn(items);

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);
        Set<String> contraindicated = adapter.lookupContraindicatedIngredients("이부프로펜");

        assertThat(contraindicated).containsExactlyInAnyOrder("메토트렉세이트", "와파린");
    }

    @Test
    void 예외시_성분_집합도_비어있다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        when(client.fetchUsjntTabooByName(anyString())).thenThrow(new RuntimeException("network"));

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);

        assertThat(adapter.lookupContraindicatedIngredients("이부프로펜")).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static JSONObject item(String mixtureIngrKorName) {
        JSONObject o = new JSONObject();
        o.put("MIXTURE_INGR_KOR_NAME", mixtureIngrKorName);
        return o;
    }
}
