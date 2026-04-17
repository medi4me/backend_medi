package com.mediforme.mediforme.medicine.service.impl;

import com.mediforme.mediforme.medicine.dto.MedicineRecognitionResultDto;
import com.mediforme.mediforme.medicine.external.port.OcrPort;
import com.mediforme.mediforme.medicine.service.MedicineImageRecognitionService;
import com.mediforme.mediforme.medicine.support.OcrMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineImageRecognitionServiceImpl implements MedicineImageRecognitionService {

    private final OcrPort ocrPort;
    private final OcrMetrics metrics;

    // 후보 최대 개수 (외부 API 호출 폭증 방지)
    private static final int MAX_CANDIDATES = 5;

    // 약봉투/약통/처방전에서 흔히 나오는 불필요 단어(후보 제거)
    private static final Set<String> STOPWORDS = Set.of(
        "처방", "조제", "복용", "용법", "용량", "주의", "환자", "의사", "약국",
        "성분", "효능", "보관", "유통기한", "일", "회", "아침", "점심", "저녁",
        "정", "캡슐", "시럽", "연고", "외용", "밀리그람"
    );

    // 용량/단위 제거(500mg, 160밀리그램 등)
    private static final Pattern DOSAGE_PATTERN =
        Pattern.compile("(?i)\\b\\d+(\\.\\d+)?\\s*(mg|g|ml|mcg|ug|밀리그램|그램|밀리리터)\\b");

    // 괄호 제거(성분명 등이 들어가는 경우가 많음)
    private static final Pattern PAREN_PATTERN =
        Pattern.compile("\\([^)]*\\)");

    /**
     * 가장 유력한 후보 1개만 반환
     */
    @Override
    public String recognizeMedicineName(MultipartFile imageFile) {
        MedicineRecognitionResultDto result = recognizeMedicineCandidates(imageFile);
        if (result == null || result.getCandidates() == null || result.getCandidates().isEmpty()) {
            return null;
        }
        return result.getCandidates().get(0);
    }

    /**
     * OCR 후 후보 여러 개 + fullText 반환
     */
    @Override
    public MedicineRecognitionResultDto recognizeMedicineCandidates(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return empty();
        }

        long start = System.nanoTime();
        try {
            String fullText = ocrPort.extractText(imageFile.getBytes());
            Duration duration = Duration.ofNanos(System.nanoTime() - start);

            if (fullText == null || fullText.isBlank()) {
                metrics.recordExtract(duration, OcrMetrics.Outcome.EMPTY, 0);
                log.info("ocr result outcome=empty duration={}ms", duration.toMillis());
                return empty();
            }

            List<String> candidates = extractCandidates(fullText);
            metrics.recordExtract(duration, OcrMetrics.Outcome.SUCCESS, fullText.length());
            log.info("ocr result raw_length={} candidates={} outcome=success duration={}ms",
                fullText.length(), candidates.size(), duration.toMillis());

            return MedicineRecognitionResultDto.builder()
                .fullText(fullText)
                .candidates(candidates)
                .build();

        } catch (IOException e) {
            Duration duration = Duration.ofNanos(System.nanoTime() - start);
            metrics.recordExtract(duration, OcrMetrics.Outcome.FAILURE, -1);
            log.warn("ocr image read failed duration={}ms: {}",
                duration.toMillis(), e.getMessage());
            return empty();
        } catch (Exception e) {
            Duration duration = Duration.ofNanos(System.nanoTime() - start);
            metrics.recordExtract(duration, OcrMetrics.Outcome.FAILURE, -1);
            log.warn("ocr unexpected failure duration={}ms: {}",
                duration.toMillis(), e.getMessage());
            return empty();
        }
    }

    private MedicineRecognitionResultDto empty() {
        return MedicineRecognitionResultDto.builder()
            .fullText(null)
            .candidates(Collections.emptyList())
            .build();
    }

    /**
     * OCR 텍스트에서 약 이름 후보를 여러 개 추출
     * - 라인 기준 후보 + 토큰 기준 후보를 결합
     * - 불필요 문구/용량 제거 후 score로 정렬
     */
    private List<String> extractCandidates(String fullText) {
        if (fullText == null || fullText.isBlank()) return Collections.emptyList();

        String normalized = Normalizer.normalize(fullText, Normalizer.Form.NFKC);
        List<String> raw = new ArrayList<>();

        // 1) 라인 단위
        for (String line : normalized.split("\\R")) {
            String cleaned = clean(line);
            if (isCandidate(cleaned)) raw.add(cleaned);
        }

        // 2) 토큰 단위
        for (String token : normalized.split("[\\s,;:/\\\\|]+")) {
            String cleaned = clean(token);
            if (isCandidate(cleaned)) raw.add(cleaned);
        }

        Map<String, Integer> scored = new HashMap<>();
        for (String t : raw) {
            scored.put(t, Math.max(scored.getOrDefault(t, 0), score(t)));
        }

        return scored.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .map(Map.Entry::getKey)
            .distinct()
            .limit(MAX_CANDIDATES)
            .collect(Collectors.toList());
    }

    private String clean(String token) {
        if (token == null) return null;
        String t = token.trim();
        if (t.isBlank()) return t;

        t = PAREN_PATTERN.matcher(t).replaceAll("").trim();
        t = DOSAGE_PATTERN.matcher(t).replaceAll("").trim();
        t = t.replaceAll("[\\[\\]{}<>\"'`~!@#$%^&*_+=?]", "").trim();

        if (t.length() > 40) t = t.substring(0, 40);
        return t;
    }

    private boolean isCandidate(String token) {
        if (token == null) return false;
        String t = token.trim();
        if (t.isBlank()) return false;
        if (t.length() < 2) return false;
        if (t.matches("\\d+")) return false;
        if (!t.matches(".*[가-힣A-Za-z].*")) return false;
        if (STOPWORDS.contains(t)) return false;
        return true;
    }

    /**
     * 간단 스코어링: 약품명 suffix + 한글 포함 가산
     */
    private int score(String token) {
        if (token == null) return 0;
        int s = 0;
        if (token.matches(".*[가-힣].*")) s += 2;
        if (token.endsWith("정")) s += 3;
        if (token.contains("서방")) s += 2;
        if (token.contains("캡슐")) s += 2;
        if (token.contains("현탁")) s += 2;
        if (token.contains("시럽")) s += 2;
        if (token.length() > 25) s -= 1;
        return s;
    }
}
