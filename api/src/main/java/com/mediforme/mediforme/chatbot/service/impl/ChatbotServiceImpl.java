package com.mediforme.mediforme.chatbot.service.impl;

import com.mediforme.mediforme.chatbot.external.OpenAIConfig;
import com.mediforme.mediforme.chatbot.dto.ChatbotRequestDto;
import com.mediforme.mediforme.chatbot.dto.ChatbotQuestionRequestDto;
import com.mediforme.mediforme.chatbot.dto.ChatbotResponseDto;
import com.mediforme.mediforme.chatbot.dto.MedicineContextDto;
import com.mediforme.mediforme.chatbot.external.port.ChatbotRagClient;
import com.mediforme.mediforme.chatbot.external.port.RagChunk;
import com.mediforme.mediforme.chatbot.service.ChatbotPromptBuilder;
import com.mediforme.mediforme.chatbot.service.ChatbotService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final int RAG_TOP_K = 5;

    private final ChatbotRagClient ragClient;

    public ChatbotServiceImpl(ChatbotRagClient ragClient) {
        this.ragClient = ragClient;
    }

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
        MedicineContextDto ctx = requestDto.getMedicineContext();
        List<RagChunk> chunks = ragClient.retrieve(
                requestDto.getQuestion(), resolveDrugId(ctx), RAG_TOP_K);

        // 검색 청크가 있으면 RAG 컨텍스트로, 없으면(미커버·장애) 기존 앵커링으로 폴백
        List<ChatbotRequestDto.Message> messages = chunks.isEmpty()
                ? ChatbotPromptBuilder.build(requestDto.getQuestion(), ctx)
                : ChatbotPromptBuilder.buildRag(requestDto.getQuestion(), ctx, chunks);

        ChatbotRequestDto chatGptRequestDto = new ChatbotRequestDto(OpenAIConfig.MODEL, messages);
        return this.getResponse(this.buildHttpEntity(chatGptRequestDto));
    }

    /**
     * /retrieve 의 drug_id 로 쓸 약 식별자 결정
     * 한국어 성분명을 우선하고, 없으면 제품명 사용 (없으면 null → 필터 없이 검색)
     */
    private static String resolveDrugId(MedicineContextDto ctx) {
        if (ctx == null) {
            return null;
        }
        if (isPresent(ctx.getIngredientKo())) {
            return ctx.getIngredientKo();
        }
        if (isPresent(ctx.getNameKo())) {
            return ctx.getNameKo();
        }
        return null;
    }

    private static boolean isPresent(String v) {
        return v != null && !v.isBlank();
    }
}
