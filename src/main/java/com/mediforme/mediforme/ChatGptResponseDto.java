package com.mediforme.mediforme;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ChatGptResponseDto implements Serializable {

    private String id;
    private String object;
    private Long created;  // 타입을 LocalDateTime에서 Long으로 변경
    private String model;
    private List<Choice> choices;

    @Builder
    public ChatGptResponseDto(String id, String object,
                              Long created, String model,
                              List<Choice> choices) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.model = model;
        this.choices = choices;
    }

    @Getter
    @NoArgsConstructor
    public static class Choice {
        private Message message;
        private Integer index;
        @JsonProperty("finish_reason")
        private String finishReason;

        @Builder
        public Choice(Message message, Integer index, String finishReason) {
            this.message = message;
            this.index = index;
            this.finishReason = finishReason;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;

        @Builder
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

