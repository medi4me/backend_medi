package com.mediforme.mediforme.chatbot.external;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RagApiConfig {     // Chatbot RAG 검색 서비스 (mediforme-chatbot-rag)

    @Value("${api.rag.base-url}")
    private String baseUrl;
}
