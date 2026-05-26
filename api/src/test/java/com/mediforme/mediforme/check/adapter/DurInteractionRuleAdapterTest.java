package com.mediforme.mediforme.check.adapter;

import com.mediforme.mediforme.medicine.dto.MedicineInteractResponseDto;
import com.mediforme.mediforme.medicine.external.client.DurInteractionClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DurInteractionRuleAdapterTest {

    @Test
    void info_응답은_성분과_사유를_괄호로_묶어_노출한다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        JSONArray items = new JSONArray();
        items.add(item("메토트렉세이트", "혈액학적 독성"));
        items.add(item("케토롤락트로메타민", "위장관 출혈 위험"));
        when(client.fetchUsjntTabooByName("이부프로펜")).thenReturn(items);

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);
        List<MedicineInteractResponseDto> rules = adapter.lookupByMedicineName("이부프로펜");

        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).getName()).isEqualTo("이부프로펜");
        assertThat(rules.get(0).getInteractionWarnings())
            .contains("메토트렉세이트(혈액학적 독성)")
            .contains("케토롤락트로메타민(위장관 출혈 위험)");
    }

    @Test
    void 사유가_없으면_성분명만_노출한다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        JSONArray items = new JSONArray();
        items.add(item("메토트렉세이트", null));
        when(client.fetchUsjntTabooByName("이부프로펜")).thenReturn(items);

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);

        assertThat(adapter.lookupByMedicineName("이부프로펜").get(0).getInteractionWarnings())
            .isEqualTo("메토트렉세이트");
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
    void 병용금기_상대_성분과_사유_매핑을_반환한다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        JSONArray items = new JSONArray();
        items.add(item("메토트렉세이트", "혈액학적 독성"));
        items.add(item("와파린", "출혈 위험"));
        when(client.fetchUsjntTabooByName("이부프로펜")).thenReturn(items);

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);
        Map<String, String> reasons = adapter.lookupContraindicatedIngredients("이부프로펜");

        assertThat(reasons).containsOnly(
            entry("메토트렉세이트", "혈액학적 독성"),
            entry("와파린", "출혈 위험")
        );
    }

    @Test
    void 같은_성분이_여러번_나오면_첫_사유를_채택한다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        JSONArray items = new JSONArray();
        items.add(item("메토트렉세이트", "혈액학적 독성"));
        items.add(item("메토트렉세이트", "신독성"));
        when(client.fetchUsjntTabooByName("이부프로펜")).thenReturn(items);

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);

        assertThat(adapter.lookupContraindicatedIngredients("이부프로펜"))
            .containsOnly(entry("메토트렉세이트", "혈액학적 독성"));
    }

    @Test
    void 예외시_성분_매핑도_비어있다() throws Exception {
        DurInteractionClient client = mock(DurInteractionClient.class);
        when(client.fetchUsjntTabooByName(anyString())).thenThrow(new RuntimeException("network"));

        DurInteractionRuleAdapter adapter = new DurInteractionRuleAdapter(client);

        assertThat(adapter.lookupContraindicatedIngredients("이부프로펜")).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static JSONObject item(String mixtureIngrKorName, String prohbtContent) {
        JSONObject o = new JSONObject();
        o.put("MIXTURE_INGR_KOR_NAME", mixtureIngrKorName);
        if (prohbtContent != null) {
            o.put("PROHBT_CONTENT", prohbtContent);
        }
        return o;
    }
}
