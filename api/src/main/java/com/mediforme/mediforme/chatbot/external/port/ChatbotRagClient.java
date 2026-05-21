package com.mediforme.mediforme.chatbot.external.port;

import java.util.List;

/**
 * Chatbot RAG 검색 포트
 *
 * 사용자 질문과 약 식별자로 의약품 라벨 청크를 검색한다.
 * 구현은 mediforme-chatbot-rag 의 /retrieve 를 호출한다.
 */
public interface ChatbotRagClient {

    /**
     * 질문·약 식별자로 라벨 청크 top-k 검색
     *
     * @param query  사용자 자연어 질문
     * @param drugId 약 식별자(성분/이름). null/blank 면 필터 없이 검색
     * @param topK   최대 청크 수
     * @return 검색된 청크 리스트. 실패·빈 결과는 빈 리스트(호출부가 앵커링으로 폴백)
     */
    List<RagChunk> retrieve(String query, String drugId, int topK);
}
