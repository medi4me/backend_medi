package com.mediforme.mediforme.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class ChatbotQuestionRequestDto implements Serializable {

    @NotBlank(message = "질문 내용은 필수 입력 항목입니다.")
    private String question;

    @Valid
    private MedicineContextDto medicineContext;
}