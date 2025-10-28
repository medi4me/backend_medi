package com.mediforme.mediforme.dto.request;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class ChatbotQuestionRequestDto implements Serializable {
    @jakarta.validation.constraints.NotBlank(message = "질문 내용은 필수 입력 항목입니다.")
    private String question;
}
