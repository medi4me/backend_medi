package com.mediforme.mediforme.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.mediforme.dto.response.ChatbotResponseDto;
import com.mediforme.mediforme.dto.request.ChatbotQuestionRequestDto;
import com.mediforme.mediforme.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v2/chatbot")
@RequiredArgsConstructor
@Tag(name = "챗봇 API", description = "OpenAI 기반 챗봇 질의응답 API")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Operation(summary = "질문 전송", description = "사용자의 질문을 OpenAI API에 전달하고 응답을 반환합니다.")
    @PostMapping("/question")
    public ApiResponse<ChatbotResponseDto> sendQuestion(@RequestBody ChatbotQuestionRequestDto requestDto) {
        ChatbotResponseDto response = chatbotService.askQuestion(requestDto);
        return ApiResponse.onSuccess(response);
    }
}

