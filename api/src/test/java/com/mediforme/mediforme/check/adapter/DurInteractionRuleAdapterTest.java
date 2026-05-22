package com.mediforme.mediforme.check.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import com.mediforme.mediforme.medicine.external.client.DurInteractionClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DurInteractionRuleAdapterTest {

    @Test
    void DUR_병용금기를_경고_문자열로_매핑한다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        JSONArray items = new JSONArray();
        items.add(item("와파린정"));
        items.add(item("아스피린장용정"));
        when(client.fetchUsjntTabooByName("이부프로펜")).thenReturn(items);

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);
        List<MedicineInteractResponseDto> rules = adapter.lookupByMedicineName("이부프로펜");

        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).getName()).isEqualTo("이부프로펜");
        assertThat(rules.get(0).getInteractionWarnings())
            .contains("와파린정")
            .contains("아스피린장용정");
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

    @SuppressWarnings("unchecked")
    private static JSONObject item(String mixtureItemName) {
        JSONObject o = new JSONObject();
        o.put("MIXTURE_ITEM_NAME", mixtureItemName);
        return o;
    }
}
