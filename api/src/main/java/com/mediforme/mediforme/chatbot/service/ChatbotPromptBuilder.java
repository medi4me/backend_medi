package com.mediforme.mediforme.chatbot.service;

import com.mediforme.mediforme.chatbot.dto.ChatbotRequestDto.Message;
import com.mediforme.mediforme.chatbot.dto.MedicineContextDto;

import java.util.ArrayList;
import java.util.List;

public final class ChatbotPromptBuilder {

    private static final String SYSTEM_PROMPT_HEADER =
            "당신은 한국 사용자에게 의약품 정보를 설명하는 보조 챗봇입니다.\n"
                    + "현재 대화는 아래 약에 대한 질문이므로, 이 약의 범위를 벗어난 일반론으로 빗나가지 마세요.\n"
                    + "불확실하거나 안전에 영향이 있는 사안은 반드시 '의사·약사와 상담'을 안내하고, "
                    + "처방 변경을 단독으로 지시하지 마세요.\n\n"
                    + "약 컨텍스트:";

    private ChatbotPromptBuilder() {
    }

    public static List<Message> build(String question, MedicineContextDto ctx) {
        List<Message> messages = new ArrayList<>();
        if (ctx != null && ctx.hasAny()) {
            messages.add(new Message("system", buildSystemContent(ctx)));
        }
        messages.add(new Message("user", question));
        return messages;
    }

    static String buildSystemContent(MedicineContextDto ctx) {
        StringBuilder sb = new StringBuilder(SYSTEM_PROMPT_HEADER);
        appendIfPresent(sb, "제품명", ctx.getNameKo());
        appendIfPresent(sb, "성분", ctx.getIngredientKo());
        appendIfPresent(sb, "구분", ctx.getRxOtc());
        appendIfPresent(sb, "적응증 요약", ctx.getIndicationsSummary());
        appendIfPresent(sb, "주의사항 요약", ctx.getWarningsSummary());
        return sb.toString();
    }

    private static void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        sb.append('\n').append("- ").append(label).append(": ").append(value);
    }
}
