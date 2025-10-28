package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.config.OpenAIConfig;
import com.mediforme.mediforme.dto.request.ChatbotRequestDto;
import com.mediforme.mediforme.dto.request.ChatbotQuestionRequestDto;
import com.mediforme.mediforme.dto.response.ChatbotResponseDto;
import com.mediforme.mediforme.service.ChatbotService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

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
        ChatbotRequestDto.Message message = new ChatbotRequestDto.Message("user", requestDto.getQuestion());
        ChatbotRequestDto chatGptRequestDto = new ChatbotRequestDto(
                OpenAIConfig.MODEL,
                Collections.singletonList(message)
        );
        return this.getResponse(this.buildHttpEntity(chatGptRequestDto));
    }
}
