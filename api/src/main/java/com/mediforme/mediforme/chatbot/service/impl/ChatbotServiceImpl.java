package com.mediforme.mediforme.chatbot.service.impl;

import com.mediforme.mediforme.chatbot.external.OpenAIConfig;
import com.mediforme.mediforme.chatbot.dto.ChatbotRequestDto;
import com.mediforme.mediforme.chatbot.dto.ChatbotQuestionRequestDto;
import com.mediforme.mediforme.chatbot.dto.ChatbotResponseDto;
import com.mediforme.mediforme.chatbot.service.ChatbotPromptBuilder;
import com.mediforme.mediforme.chatbot.service.ChatbotService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final RestTemplate restTemplate = new RestTemplate();

    private HttpEntity<ChatbotRequestDto> buildHttpEntity(ChatbotRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(OpenAIConfig.MEDIA_TYPE));
        headers.add(OpenAIConfig.AUTHORIZATION, OpenAIConfig.BEARER + OpenAIConfig.API_KEY);
        return new HttpEntity<>(requestDto, headers);
    }

    private ChatbotResponseDto getResponse(HttpEntity<ChatbotRequestDto> chatGptRequestDtoHttpEntity) {
        ResponseEntity<ChatbotResponseDto> responseEntity = restTemplate.postForEntity(
                OpenAIConfig.URL,
                chatGptRequestDtoHttpEntity,
                ChatbotResponseDto.class
        );
        return responseEntity.getBody();
    }

    @Override
    public ChatbotResponseDto askQuestion(ChatbotQuestionRequestDto requestDto) {
        ChatbotRequestDto chatGptRequestDto = new ChatbotRequestDto(
                OpenAIConfig.MODEL,
                ChatbotPromptBuilder.build(requestDto.getQuestion(), requestDto.getMedicineContext())
        );
        return this.getResponse(this.buildHttpEntity(chatGptRequestDto));
    }
}
