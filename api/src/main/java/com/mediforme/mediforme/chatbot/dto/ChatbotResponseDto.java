package com.mediforme.mediforme.chatbot.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ChatbotResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String object;
    private Long created;
    private String model;
    private List<ChatbotChoice> choices;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ChatbotChoice {
        private ChatbotMessage message;
        private Integer index;
        private String finishReason;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatbotMessage {
        private String role;
        private String content;
    }

    // 사람이 읽을 수 있는 형태로 변환된 created 시간 반환
    public String getCreatedAt() {
        return Instant.ofEpochSecond(created)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime()
                .toString();
    }

}
