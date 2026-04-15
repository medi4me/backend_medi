package com.mediforme.mediforme.chatbot.service;

import com.mediforme.mediforme.chatbot.dto.ChatbotQuestionRequestDto;
import com.mediforme.mediforme.chatbot.dto.ChatbotResponseDto;

public interface ChatbotService {
    ChatbotResponseDto askQuestion(ChatbotQuestionRequestDto requestDto);
}
