package com.mediforme.mediforme.medicine.external.port;

/**
 * 이미지 OCR 포트
 */
public interface OcrPort {

    /**
     * 이미지 바이트에서 raw 텍스트 추출
     * 실패/빈 이미지는 null 반환
     */
    String extractText(byte[] image);
}
