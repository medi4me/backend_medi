package com.mediforme.mediforme.chatbot.external.port;

/**
 * RAG 검색으로 받은 라벨 청크 1건
 */
public record RagChunk(
        String text,
        String drugName,
        String section,
        String source,
        double similarity
) {
}
