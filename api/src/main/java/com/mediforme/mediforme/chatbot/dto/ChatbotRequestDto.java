package com.mediforme.mediforme.chatbot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
public class ChatbotRequestDto implements Serializable {

    private String model;
    private List<Message> messages;

    @Builder
    public ChatbotRequestDto(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
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
