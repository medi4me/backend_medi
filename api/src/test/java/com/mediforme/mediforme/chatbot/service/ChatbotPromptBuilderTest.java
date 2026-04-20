package com.mediforme.mediforme.chatbot.service;

import com.mediforme.mediforme.chatbot.dto.ChatbotRequestDto.Message;
import com.mediforme.mediforme.chatbot.dto.MedicineContextDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatbotPromptBuilderTest {

    @Test
    void 컨텍스트가_없으면_user_메시지만_포함한다() {
        List<Message> messages = ChatbotPromptBuilder.build("타이레놀 먹어도 돼?", null);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getRole()).isEqualTo("user");
        assertThat(messages.get(0).getContent()).isEqualTo("타이레놀 먹어도 돼?");
    }

    @Test
    void 모든_필드가_null_이면_user_메시지만_포함한다() {
        MedicineContextDto empty = MedicineContextDto.builder().build();

        List<Message> messages = ChatbotPromptBuilder.build("질문", empty);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getRole()).isEqualTo("user");
    }

    @Test
    void 공백만_있는_필드는_무시된다() {
        MedicineContextDto blank = MedicineContextDto.builder()
                .nameKo("   ")
                .ingredientKo("")
                .build();

        List<Message> messages = ChatbotPromptBuilder.build("질문", blank);

        assertThat(messages).hasSize(1);
    }

    @Test
    void 컨텍스트가_있으면_system_메시지가_앞에_붙는다() {
        MedicineContextDto ctx = MedicineContextDto.builder()
                .nameKo("타이레놀")
                .ingredientKo("아세트아미노펜")
                .rxOtc("OTC")
                .build();

        List<Message> messages = ChatbotPromptBuilder.build("술 마셔도 돼?", ctx);

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getRole()).isEqualTo("system");
        assertThat(messages.get(1).getRole()).isEqualTo("user");
        assertThat(messages.get(1).getContent()).isEqualTo("술 마셔도 돼?");
    }

    @Test
    void 시스템_프롬프트에_제공된_필드만_포함된다() {
        MedicineContextDto ctx = MedicineContextDto.builder()
                .nameKo("마운자로")
                .ingredientKo("티르제파타이드")
                .build();

        String sys = ChatbotPromptBuilder.buildSystemContent(ctx);

        assertThat(sys).contains("제품명: 마운자로");
        assertThat(sys).contains("성분: 티르제파타이드");
        assertThat(sys).doesNotContain("구분:");
        assertThat(sys).doesNotContain("적응증 요약:");
        assertThat(sys).doesNotContain("주의사항 요약:");
    }

    @Test
    void 시스템_프롬프트에_안전_가드레일_문구가_포함된다() {
        MedicineContextDto ctx = MedicineContextDto.builder()
                .nameKo("팍스로비드")
                .build();

        String sys = ChatbotPromptBuilder.buildSystemContent(ctx);

        assertThat(sys).contains("의사·약사와 상담");
        assertThat(sys).contains("처방 변경을 단독으로 지시하지 마세요");
    }

    @Test
    void 모든_메타데이터_필드가_순서대로_포함된다() {
        MedicineContextDto ctx = MedicineContextDto.builder()
                .nameKo("위고비")
                .ingredientKo("세마글루티드")
                .rxOtc("RX")
                .indicationsSummary("만성 체중관리")
                .warningsSummary("MTC/MEN 2 금기")
                .build();

        String sys = ChatbotPromptBuilder.buildSystemContent(ctx);

        int nameIdx = sys.indexOf("제품명");
        int ingredientIdx = sys.indexOf("성분");
        int rxIdx = sys.indexOf("구분");
        int indIdx = sys.indexOf("적응증 요약");
        int warnIdx = sys.indexOf("주의사항 요약");

        assertThat(nameIdx).isPositive();
        assertThat(ingredientIdx).isGreaterThan(nameIdx);
        assertThat(rxIdx).isGreaterThan(ingredientIdx);
        assertThat(indIdx).isGreaterThan(rxIdx);
        assertThat(warnIdx).isGreaterThan(indIdx);
    }
}
