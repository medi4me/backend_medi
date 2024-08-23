package com.mediforme.mediforme;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
public class ChatGptRequestDto implements Serializable {

    private String model;
    private List<Message> messages;

    @Builder
    public ChatGptRequestDto(String model, List<Message> messages) {
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

