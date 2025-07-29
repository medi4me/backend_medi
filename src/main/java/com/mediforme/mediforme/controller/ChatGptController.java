package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.dto.ChatGptResponseDto;
import com.mediforme.mediforme.service.ChatGptService;
import com.mediforme.mediforme.dto.QuestionRequestDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat-gpt")
public class ChatGptController {

    private final ChatGptService chatGptService;

    public ChatGptController(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    @PostMapping("/question")
    public ChatGptResponseDto sendQuestion(@RequestBody QuestionRequestDto requestDto) {
        return chatGptService.askQuestion(requestDto);
    }
}

