package com.mediforme.mediforme.service;

import com.mediforme.mediforme.dto.QuestionRequestDto;
import com.mediforme.mediforme.config.ChatGptConfig;
import com.mediforme.mediforme.dto.ChatGptRequestDto;
import com.mediforme.mediforme.dto.ChatGptResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class ChatGptService {

    private static final RestTemplate restTemplate = new RestTemplate();

    public HttpEntity<ChatGptRequestDto> buildHttpEntity(ChatGptRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(ChatGptConfig.MEDIA_TYPE));
        headers.add(ChatGptConfig.AUTHORIZATION, ChatGptConfig.BEARER + ChatGptConfig.API_KEY);
        return new HttpEntity<>(requestDto, headers);
    }

    public ChatGptResponseDto getResponse(HttpEntity<ChatGptRequestDto> chatGptRequestDtoHttpEntity) {
        ResponseEntity<ChatGptResponseDto> responseEntity = restTemplate.postForEntity(
                ChatGptConfig.URL,
                chatGptRequestDtoHttpEntity,
                ChatGptResponseDto.class
        );
        return responseEntity.getBody();
    }

    public ChatGptResponseDto askQuestion(QuestionRequestDto requestDto) {
        ChatGptRequestDto.Message message = new ChatGptRequestDto.Message("user", requestDto.getQuestion());
        ChatGptRequestDto chatGptRequestDto = new ChatGptRequestDto(
                ChatGptConfig.MODEL,
                Collections.singletonList(message)
        );
        return this.getResponse(this.buildHttpEntity(chatGptRequestDto));
    }
}
