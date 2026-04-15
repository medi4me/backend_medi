package com.mediforme.mediforme.service;

import com.mediforme.mediforme.dto.request.ChatbotQuestionRequestDto;
import com.mediforme.mediforme.dto.response.ChatbotResponseDto;

public interface ChatbotService {
    ChatbotResponseDto askQuestion(ChatbotQuestionRequestDto requestDto);
}
